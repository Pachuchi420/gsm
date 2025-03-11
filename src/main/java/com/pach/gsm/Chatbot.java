package com.pach.gsm;

import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.api.Whatsapp;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tools.SalesListener;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.NoSuchElementException;

public class Chatbot {
    private static Chatbot instance;



    private static Whatsapp api;



    private boolean loggedIn;
    private boolean turnedOn;



    private boolean enabled;
    private boolean disconnected;


    public static Chatbot getInstance() {
        if (instance == null) {
            instance = new Chatbot();
        }
        return instance;
    }


    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }


    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(boolean turnedOn) {
        this.turnedOn = turnedOn;
    }

    public static Whatsapp getApi() {
        return api;
    }

    public static void setApi(Whatsapp api) {
        Chatbot.api = api;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }


    public void initializeChatbot(){
                String baseDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "GSM" + File.separator + "qr.png";
                api = Whatsapp.webBuilder() // Use the Web api
                        .lastConnection()
                        .name("GSM")
                        .unregistered(QrHandler.toFile(Path.of(baseDir), file -> {
                            System.out.println("✅ QR Code saved at: " + file.toAbsolutePath());
                        }))
                        .addLoggedInListener(api -> {
                            System.out.println("✅ Whatsapp logged in:");
                            setLoggedIn(true);
                            setDisconnected(false);
                        })
                        .addDisconnectedListener(reason -> {
                            System.out.printf("❌ Whatsapp disconnected ", reason);
                            setDisconnected(true);
                            setLoggedIn(false);
                        })
                        .addListener(new SalesListener())
                        .connect()
                        .join();





    }

    public void qrImageThread(ImageView qrImageView) {
        Thread checker = new Thread(() -> {
            String baseDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "GSM" + File.separator + "qr.png";
            File qrFile = new File(baseDir);

            while (true) {
                if (supabaseAuthentication.checkIfOnline()) {
                    if (!isLoggedIn() && qrFile.exists()) { // Ensure the QR image exists
                        Platform.runLater(() -> {
                            qrImageView.setImage(new Image(qrFile.toURI().toString()));
                        });
                    } else if (isLoggedIn()){
                        qrImageView.setImage(null);
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.out.println("❌ QR Image thread interrupted: " + e.getMessage());
                    break;
                }
            }
        });

        checker.setDaemon(true);
        checker.start();
    }

    public void logout(){
        if (isLoggedIn()){
            api.logout();
            api.store().deleteSession();
            System.out.println("✅ WhatsApp session disconnected successfully & session deleted!");
            setLoggedIn(false);
            setDisconnected(true);
            Chatbot.getInstance().initializeChatbot();
        } else {
            System.out.println("❌ Not connected, can't log out! ");
        }

    }


    public void sendTestMessage(String groupName){
        var chat = Chatbot.getApi().store()
                .findChatByName(groupName)
                .orElseThrow(() -> new NoSuchElementException("❌ Chat not found!"));
        Chatbot.getApi().sendMessage(chat, "Hello there I'm using Garage Sale Manager 🤓");
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }






}
