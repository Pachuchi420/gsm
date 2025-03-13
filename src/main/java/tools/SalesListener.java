package tools;

import com.pach.gsm.Group;
import com.pach.gsm.Item;
import com.pach.gsm.storageManager;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.listener.RegisterListener;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.info.ChatMessageInfo;
import it.auties.whatsapp.model.info.QuotedMessageInfo;
import it.auties.whatsapp.model.message.model.Message;
import it.auties.whatsapp.model.message.standard.ImageMessage;
import it.auties.whatsapp.model.message.standard.TextMessage;

import java.text.Normalizer;
import java.util.List;

@RegisterListener
public class SalesListener implements Listener {

    private static final List<String> interestKeywords = List.of(
            // Spanish
            "me interesa", "interesado", "interesada", "quiero", "lo quiero", "me gusta",
            "comprar", "lo compro", "yo", "yop", "todavia hay", "todavía hay", "aun hay", "aún hay",
            "disponible", "reservado", "lo tienes", "esta disponible", "sigue disponible",
            "hay stock", "tienes stock", "queda?", "quedan?", "hay?", "lo tienes?",
            "todavia tienes", "todavía tienes",

            // English
            "interested", "i'm interested", "i want", "want this", "i want this",
            "i'll take it", "i'll buy", "i like it", "buying", "is it available",
            "still available", "do you still have it", "available?", "can i buy",
            "do you have", "left?", "any left", "do you still have", "is this reserved",

            // German
            "interessiert", "ich will", "möchte", "ich möchte", "ich nehme es",
            "ich kaufe", "will kaufen", "noch da", "noch verfügbar",
            "hast du noch", "ist es noch da", "ist das verfügbar",
            "verfügbar?", "reserviert?", "ist es reserviert"
    );

    @Override
    public void onLoggedIn() {
        System.out.println("🕹️ Sales Listener Added!");
    }

    public Group checkIfMessageIsOnGroup(ChatMessageInfo message) {
        String chatName = message.chat().map(chat -> chat.name()).orElse("Unknown Group");
        System.out.println("📛 Group Name: " + chatName);
        return storageManager.getInstance().getGroupByName(chatName);
    }

    public static String normalize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase();
    }

    @Override
    public void onMessageReply(Whatsapp whatsapp, ChatMessageInfo response, QuotedMessageInfo quoted) {
        var responseText = response.message().textMessage();
        if (responseText.isEmpty()) return;

        var quotedSender = quoted.senderJid().user();
        var myUser = whatsapp.store().jid().get().user();

        if (checkIfMessageIsOnGroup(response) == null) {
            System.out.println("🚫 Reply not found in selling groups");
            return;
        }

        if (!quotedSender.equals(myUser)) {
            System.out.println("🚫 Ignoring reply not directed at me.");
            return;
        }

        String reply = responseText.get().text();
        String normalizedReply = normalize(reply);
        System.out.println("✅ They replied to your message: " + reply);

        // Detect interest keyword
        String matchedKeyword = interestKeywords.stream()
                .filter(keyword -> normalizedReply.contains(normalize(keyword)))
                .findFirst()
                .orElse(null);

        if (matchedKeyword == null) {
            System.out.println("🤖 Message didn't express interest.");
            return;
        }

        // Guess language
        String lang = "es"; // default
        if (matchedKeyword.matches(".*(interested|want|buy|available|left|have|take).*")) {
            lang = "en";
        } else if (matchedKeyword.matches(".*(interessiert|möchte|verfügbar|noch|kaufen).*")) {
            lang = "de";
        }

        // Language-specific messages
        String pmRedirect = switch (lang) {
            case "en" -> "I'll contact you via DM 😁";
            case "de" -> "Ich schreibe dir per PM 😁";
            default -> "Te contacto por PM 😁";
        };

        String pmMessage = switch (lang) {
            case "en" -> "Hey! I saw you're interested in this product!";
            case "de" -> "Hallo! Ich habe gesehen, dass du an diesem Produkt interessiert bist!";
            default -> "Hola! Vi que estás interesado en este producto!";
        };

        String reservedMessage = switch (lang) {
            case "en" -> "Hey! I saw you're interested in this product, but unfortunately it's already reserved!";
            case "de" -> "Hallo! Ich habe gesehen, dass du an diesem Produkt interessiert bist, aber leider ist es bereits reserviert!";
            default -> "Hola! Vi que estás interesado en este producto, pero lamentablemente ya está reservado!";
        };

        // Get the quoted message's text
        Message quotedContent = quoted.message().content();
        String quotedMessageText = "❌Message not available";
        if (quotedContent instanceof TextMessage textMsg) {
            quotedMessageText = textMsg.text();
        } else if (quotedContent instanceof ImageMessage imageMsg) {
            quotedMessageText = imageMsg.caption().orElse("❌Image with no text");
        }

        // Extract product name
        String[] lines = quotedMessageText.split("\\R");
        String productName = lines.length > 0 ? lines[0].trim() : "❌ Name not available";
        System.out.println("🛒 Product name: " + productName);

        // Search for the item
        storageManager.getInstance().getItemByName(productName, item -> {
            if (item != null) {
                System.out.println("✅ Found item: " + item.getName() + " $" + item.getPrice());

                if (item.isReserved()) {
                    System.out.println("📩 Sending reserved message to: " + response.chatName());
                    whatsapp.sendMessage(response.senderJid().toJid(), reservedMessage, quoted);
                } else {
                    System.out.println("📩 Sending PM redirect to: " + response.chatName());
                    whatsapp.sendMessage(response.chatJid().toJid(), pmRedirect, quoted);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println("📩 Sending follow-up DM to: " + response.senderJid().user());
                    whatsapp.sendMessage(response.senderJid().toJid(), pmMessage, quoted);
                }
            } else {
                System.out.println("❌ Item not found.");
            }
        });
    }
}