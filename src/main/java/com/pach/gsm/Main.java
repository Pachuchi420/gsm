package com.pach.gsm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        supabaseAuthentication.sessionManager();

        String fxmlFile;
        if (supabaseAuthentication.autoLogin()){
            fxmlFile = "/com/pach/gsm/views/listView.fxml"; // Otherwise, show login
            stage.setTitle("GSM");
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
            stage.setScene(scene);
            stage.show();
        } else {
            fxmlFile = "/com/pach/gsm/views/loginView.fxml"; // Otherwise, show login
            stage.setTitle("GSM - Login");
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlFile));
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