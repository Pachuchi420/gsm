package com.pach.gsm;

import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.api.Whatsapp;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Path;

public class Chatbot {
    private static Chatbot instance;



    private static Whatsapp api;



    private boolean loggedIn;
    private boolean turnedOn;



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


    public void initializeChatbot(ImageView qrCodeImage){
                String baseDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "GSM" + File.separator + "qr.png";
                api = Whatsapp.webBuilder() // Use the Web api
                        .lastConnection()
                        .name("GSM")
                        .unregistered(QrHandler.toFile(Path.of(baseDir), file -> {
                            System.out.println("✅ QR Code saved at: " + file.toAbsolutePath());
                            qrCodeImage.setImage(new Image(file.toUri().toString()));
                        }))
                        .addLoggedInListener(api -> {
                            System.out.println("✅ Whatsapp logged in:");
                            setLoggedIn(true);
                        })
                        .addDisconnectedListener(reason -> {
                            System.out.printf("❌ Whatsapp disconnected ", reason);
                            setLoggedIn(false);
                        })
                        .connect()
                        .join();



    }


    public void logout(){
        if (isLoggedIn()){
            api.logout();
            System.out.println("✅ WhatsApp session disconnected successfully!");
        } else {
            System.out.println("❌ Not connected, can't log out! ");
        }

    }







}
