package com.pach.gsm;

import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.api.Whatsapp;
import javafx.application.Platform;
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
                        })
                        .addDisconnectedListener(reason -> {
                            System.out.printf("❌ Whatsapp disconnected ", reason);
                            setLoggedIn(false);
                        })
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
                            System.out.println("✅ QR Code Image updated!");
                        });
                    } else if (isLoggedIn()){
                        qrImageView.setImage(null);
                    }
                }
                try {
                    Thread.sleep(2000); // Avoid infinite loop hogging CPU
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
            System.out.println("✅ WhatsApp session disconnected successfully!");
        } else {
            System.out.println("❌ Not connected, can't log out! ");
        }

    }







}
