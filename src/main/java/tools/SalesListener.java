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
import java.util.Locale;

@RegisterListener
public class SalesListener implements Listener {

    private static final List<String> interestKeywords = List.of(
            // Spanish
            "yo", "quiero", "me interesa", "interesado", "interesada", "disponible", "reservado",
            // English
            "interested", "i want", "buy", "available", "is it", "left", "still", "can i", "do you",
            // German
            "ich", "will", "möchte", "interessiert", "verfügbar", "noch", "reserviert", "interesse"
    );

    @Override
    public void onLoggedIn() {
        System.out.println("🛎️ SalesListener activated.");
    }

    private static String normalize(String input) {
        String noAccents = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccents.replaceAll("[^\\p{L}\\p{Nd} ]+", "").toLowerCase(Locale.ROOT);
    }

    private Group getGroup(ChatMessageInfo message) {
        return message.chat()
                .map(chat -> storageManager.getInstance().getGroupByName(chat.name()))
                .orElse(null);
    }

    private String detectLanguage(String text) {
        if (text.matches(".*\\b(interested|want|buy|available|still|left|can i|do you)\\b.*")) {
            return "en";
        } else if (text.matches(".*\\b(interessiert|möchte|verfügbar|reserviert|noch|interesse)\\b.*")) {
            return "de";
        } else {
            return "es"; // default to Spanish
        }
    }

    private void sendReplies(Whatsapp whatsapp, ChatMessageInfo context, String lang, boolean isReserved, QuotedMessageInfo quoted) {
        String pmRedirect = switch (lang) {
            case "en" -> "I'll contact you via DM 😁";
            case "de" -> "Ich schreibe dir per PM 😁";
            default -> "Te contacto por PM 😁";
        };

        String pmMessage = switch (lang) {
            case "en" -> "Hey! I saw you're interested in this product!";
            case "de" -> "Hallo! Ich habe gesehen, dass du an diesem Produkt interessiert bist!";
            default -> "¡Hola! Vi que estás interesado en este producto!";
        };

        String reservedMessage = switch (lang) {
            case "en" -> "Hey! I saw you're interested in this product, but unfortunately it's already reserved!";
            case "de" -> "Hallo! Ich habe gesehen, dass du an diesem Produkt interessiert bist, aber leider ist es bereits reserviert!";
            default -> "¡Hola! Vi que estás interesado en este producto, pero lamentablemente ya está reservado!";
        };

        if (isReserved) {
            whatsapp.sendMessage(context.chatJid().toJid(), reservedMessage, context);
        } else {
            whatsapp.sendMessage(context.chatJid().toJid(), pmRedirect, context);
            try {
                Thread.sleep(1000); // brief pause before DM
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            whatsapp.sendMessage(context.senderJid().toJid(), pmMessage, quoted);
        }
    }

    private String extractProductName(QuotedMessageInfo quoted) {
        Message content = quoted.message().content();
        String text = "Unknown";

        if (content instanceof TextMessage textMsg) {
            text = textMsg.text();
        } else if (content instanceof ImageMessage imageMsg) {
            text = imageMsg.caption().orElse("Image with no caption");
        }

        String[] lines = text.split("\\R");
        return lines.length > 0
                ? lines[0].replaceAll("^[^\\p{L}\\p{N}]+", "").trim()
                : "Unknown";
    }

    @Override
    public void onMessageReply(Whatsapp whatsapp, ChatMessageInfo replyMsg, QuotedMessageInfo quoted) {
        var replyTextOpt = replyMsg.message().textMessage();
        if (replyTextOpt.isEmpty()) return;

        String replyText = normalize(replyTextOpt.get().text());
        String myUser = whatsapp.store().jid().get().user();
        String quotedSender = quoted.senderJid().user();

        if (!quotedSender.equals(myUser)) {
            System.out.println("⛔ Not replying to our message.");
            return;
        }

        if (getGroup(replyMsg) == null) {
            System.out.println("📭 Not in a tracked group.");
            return;
        }

        boolean expressedInterest = interestKeywords.stream().anyMatch(replyText::contains);
        if (!expressedInterest) {
            System.out.println("🤷 No keyword detected in reply.");
            return;
        }

        String lang = detectLanguage(replyText);
        String productName = extractProductName(quoted);
        System.out.println("📦 Product candidate: " + productName);

        storageManager.getInstance().getItemByName(productName, item -> {
            if (item != null) {
                System.out.println("✅ Found item: " + item.getName());
                sendReplies(whatsapp, replyMsg, lang, item.isReserved(), quoted);
            } else {
                System.out.println("❌ Couldn't match item.");
            }
        });
    }
}