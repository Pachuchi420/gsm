package tools;

import com.pach.gsm.Group;
import com.pach.gsm.Item;
import com.pach.gsm.storageManager;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.listener.RegisterListener;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.info.ChatMessageInfo;
import it.auties.whatsapp.model.info.QuotedMessageInfo;
import it.auties.whatsapp.model.jid.Jid;
import it.auties.whatsapp.model.message.model.Message;
import it.auties.whatsapp.model.message.standard.ImageMessage;
import it.auties.whatsapp.model.message.standard.TextMessage;

@RegisterListener
public class SalesListener implements Listener {

    @Override
    public void onLoggedIn() {
        System.out.println("🕹️ Sales Listener Added!");
    }

    public Group checkIfMessageIsOnGroup(ChatMessageInfo message){
        String chatName = message.chat().map(chat -> chat.name()).orElse("Unknown Group");
        System.out.println("📛 Group Name: " + chatName);

        Group group = storageManager.getInstance().getGroupByName(chatName);

        return group;
    }

    @Override
    public void onMessageReply(Whatsapp whatsapp, ChatMessageInfo response, QuotedMessageInfo quoted) {
        var responseText = response.message().textMessage();
        if (responseText.isEmpty()) return;

        // Get the jid of both the quoted sender and yourself.
        var quotedSender = quoted.senderJid().user();
        var myUser = whatsapp.store().jid().get().user();

        // Check if the message being replied to is in one of our selling groups
        if(checkIfMessageIsOnGroup(response) == null){
            System.out.println("🚫 Reply not found in selling groups");
            return;
        }

        // Check if the jid is the same as yours for the replied message
        if (!quotedSender.equals(myUser)) {
            System.out.println("🚫 Ignoring reply not directed at me.");
            return;
        }


        String reply = responseText.get().text().toLowerCase();
        System.out.println("✅ They replied to your message: " + reply);



        // Get the text of the quoted message
        Message quotedContent = quoted.message().content();
        String quotedMessageText = "❌Message not available";
        if (quotedContent instanceof TextMessage textMsg) {
            quotedMessageText = textMsg.text();
        } else if (quotedContent instanceof ImageMessage imageMsg) {
            quotedMessageText = imageMsg.caption().orElse("❌Image with no text");
        }


        // Get the product name
        String[] lines = quotedMessageText.split("\\R");
        String productName = lines.length > 0 ? lines[0].trim() : "❌ Name not available";
        System.out.println("🛒 Product name: " + productName);


        // With product name find the actual item to check for reservation/sell status.
        storageManager.getInstance().getItemByName(productName, item -> {
            if (item != null) {
                System.out.println("✅ Found item: " + item.getName() + " $" + item.getPrice());
                if(item.isReserved()){
                    System.out.println("📩 Sending an already reserved message to: " + response.chatName());
                    whatsapp.sendMessage(response.senderJid().toJid(), "Hola! Vi que estás interesado en este producto, pero lamentablemente ya esta reservado!", quoted);
                } else {
                    System.out.println("📩 Sending an available message to: " + response.chatName());
                    whatsapp.sendMessage(response.chatJid().toJid(), "Te contacto por PM 😁", quoted);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    whatsapp.sendMessage(response.senderJid().toJid(), "Hola! Vi que estás interesado en este producto!", quoted);
                }
            } else {
                System.out.println("❌ Item not found.");
            }
        });





        }

    }

