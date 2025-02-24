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

        if (refreshToken != null) {
            System.out.println("🔄 Auto-login enabled. Redirecting to listView.fxml...");
            fxmlFile = "/com/pach/gsm/views/listView.fxml"; // If token exists, go to listView
            stage.setTitle("GSM - Sales List");
            FXMLLoader fxmlLoader = new FXMLLoader(main.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.setScene(scene);
            stage.show();
        } else {
            System.out.println("🔑 No saved session. Redirecting to loginView.fxml...");
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