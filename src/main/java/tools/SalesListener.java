package tools;

import com.pach.gsm.Group;
import com.pach.gsm.storageManager;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.listener.RegisterListener;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.info.ChatMessageInfo;
import it.auties.whatsapp.model.info.QuotedMessageInfo;
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

        // ✅ Compare only the user part of the JID (phone number)
        var quotedSender = quoted.senderJid().user();
        var myUser = whatsapp.store().jid().get().user();

        if(checkIfMessageIsOnGroup(response) == null){
            System.out.println("🚫 Reply not found in selling groups");
            return;
        }

        if (!quotedSender.equals(myUser)) {
            System.out.println("🚫 Ignoring reply not directed at me.");
            return;
        }

        String reply = responseText.get().text().toLowerCase();
        System.out.println("✅ They replied to your message: " + reply);

        if (reply.contains("interested")) {
            whatsapp.sendMessage(response.chatJid(), """
                🛍️ Awesome! I'm glad you're interested!
                💬 This item is still available.
                📦 I can give you a little discount if you’re fast.

                🤖 Powered by GSM.
            """);
        }

        if (reply.contains("yo") || reply.contains("sigue disponible?")) {
            whatsapp.sendMessage(response.chatJid(), """
                🛍️ ¡Genial! ¡Me alegra que estés interesado!
                💬 Este artículo todavía está disponible.
                📦 Puedo ofrecerte un pequeño descuento si eres rápido.

                🤖 Desarrollado por GSM.
            """);
        }
    }
}