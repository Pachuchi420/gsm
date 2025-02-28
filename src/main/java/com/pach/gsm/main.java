package com.pach.gsm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        supabaseAuthentication authenticationInstance = supabaseAuthentication.getInstance();
        String refreshToken = authenticationInstance.getRefreshToken();




        String fxmlFile;

        int autoLoginState = supabaseAuthentication.autoLogin();

        if (autoLoginState == 4) {
            System.out.println("✅ Logged in! Redirecting to listView.fxml...");
            fxmlFile = "/com/pach/gsm/views/listView.fxml";
            stage.setTitle("GSM - Sales List");
            FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.show();
        } else if (autoLoginState == 3 || autoLoginState == 1){
            System.out.println("🔑 No valid session. Redirecting to loginView.fxml...");
            fxmlFile = "/com/pach/gsm/views/loginView.fxml"; // Otherwise, show login
            stage.setTitle("GSM - Login");
            FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } else if (autoLoginState == 2){
            System.out.println(" ⚠️ Session is offline, logging in with refresh token");
            fxmlFile = "/com/pach/gsm/views/listView.fxml";
            stage.setTitle("GSM - Sales List");
            FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.show();
        } else if (autoLoginState == 5){
            System.out.println("🔑 User ID's do not match . Redirecting to loginView.fxml...");
            fxmlFile = "/com/pach/gsm/views/loginView.fxml"; // Otherwise, show login
            stage.setTitle("GSM - Login");
            FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        }




    }

    public static void main(String[] args) {
        launch();
    }
}