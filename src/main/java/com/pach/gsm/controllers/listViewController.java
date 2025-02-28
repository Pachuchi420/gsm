package com.pach.gsm.controllers;

import com.pach.gsm.main;
import com.pach.gsm.supabaseAuthentication;
import effects.TogglePane;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class listViewController {
    @FXML
    private Button logoutButton, addItem, closeAddItemPane;

    @FXML
    private AnchorPane addItemPane, mainPane;

    @FXML
    private TextField itemName, itemPrice;

    @FXML
    private TextArea itemDescription;

    @FXML
    private ToggleButton itemPriorityHigh, itemPriorityMedium,itemPriorityLow;

    @FXML
    private RadioButton itemCurrencyUSD, itemCurrencyMXN, itemCurrencyEUR;







    @FXML
    public void initialize() throws IOException {

        addItemPane.setVisible(false);

        TogglePane addItemToggle = new TogglePane(addItemPane, mainPane, true);

        mainPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    if (!addItemPane.isVisible()) {
                        openAddItemPane(addItemToggle);
                    }
                    break;
                case ESCAPE:
                    if (addItemPane.isVisible()) {
                        cancelAddItem(addItemToggle);
                    }
                    break;
                default:
                    break;
            }
        });


        addItem.setOnAction(event -> openAddItemPane(addItemToggle));
        closeAddItemPane.setOnAction(event -> cancelAddItem(addItemToggle));
        logoutButton.setOnAction(event -> {
            try {
                logout();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


    }

    private void logout() throws IOException {
        supabaseAuthentication instance = supabaseAuthentication.getInstance();
        if (instance.logoutUser()) {
            System.out.println("🔑 User logged out. Redirecting to loginView.fxml...");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/loginView.fxml"));
            Parent root = fxmlLoader.load();

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("GSM - Login");
            newStage.setResizable(false);

            Stage currentStage = (Stage) mainPane.getScene().getWindow();
            currentStage.close();

            newStage.show();
        }
    }

    private void cancelAddItem(TogglePane addItemToggle) {
        addItemToggle.togglePane(addItemPane, null);
    }


    private void openAddItemPane(TogglePane addItemToggle) {
        addItemToggle.togglePane(addItemPane, null);
    }
}
