package com.pach.gsm.controllers;

import com.pach.gsm.Item;
import com.pach.gsm.storageManager;
import com.pach.gsm.supabaseAuthentication;
import com.pach.gsm.supabaseDB;
import effects.TogglePane;
import effects.textEffects;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class listViewController {
    @FXML
    private Label warningAddMessage;

    @FXML
    private Button logoutButton, addItem, closeAddItemPane, confirmAddItem, cancelAdditem;

    @FXML
    private AnchorPane addItemPane, mainPane;

    @FXML
    private TableView itemList;


    @FXML
    private TableColumn<Item, String> itemColumnID, itemColumnName, itemColumnDescription, itemColumnReservation;


    @FXML
    private TableColumn<Item, Integer>  itemColumnPriority, itemColumnPrice;;


    @FXML
    private TableColumn<Item, Boolean> itemColumnReserved, itemColumnSold;


    @FXML
    private TextField itemAddName, itemAddPrice;

    @FXML
    private TextArea itemAddDescription;

    @FXML
    private ToggleButton itemAddPriorityHigh, itemAddPriorityMedium,itemAddPriorityLow;

    @FXML
    private RadioButton itemAddCurrencyUSD, itemAddCurrencyMXN, itemAddCurrencyEUR;


    private ToggleGroup currencyGroup;
    private ToggleGroup priorityGroup;


    @FXML
    public void initialize() throws IOException {

        storageManager storage = storageManager.getInstance();
        String userID = storage.getUserID();
        storage.initializeDatabase(userID);
        setupTableColumns();
        refreshTable(userID);


        addItemPane.setVisible(false);

        TogglePane addItemToggle = new TogglePane(addItemPane, mainPane, true);


        currencyGroup = new ToggleGroup();
        itemAddCurrencyUSD.setToggleGroup(currencyGroup);
        itemAddCurrencyMXN.setToggleGroup(currencyGroup);
        itemAddCurrencyEUR.setToggleGroup(currencyGroup);


        priorityGroup = new ToggleGroup();
        itemAddPriorityHigh.setToggleGroup(priorityGroup);
        itemAddPriorityMedium.setToggleGroup(priorityGroup);
        itemAddPriorityLow.setToggleGroup(priorityGroup);


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
        confirmAddItem.setOnAction(event -> addItem(userID, addItemToggle));
        closeAddItemPane.setOnAction(event -> cancelAddItem(addItemToggle));
        cancelAdditem.setOnAction(event -> cancelAddItem(addItemToggle));
        logoutButton.setOnAction(event -> {
            try {
                logout();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });








    }

    public void refreshTable(String userID) {
        storageManager storage = storageManager.getInstance();
        List<Item> updatedItems = storage.getAllLocalItems(userID); // Fetch only current user's items
        if (updatedItems != null) {
            itemList.getItems().setAll(updatedItems);
            System.out.println("✅ TableView updated with latest items for user: " + storage.getUserID());
        } else {
            System.out.println("⚠️ No items found for this user.");
        }
    }

    public boolean containsNonNumeric(String str) {
        // Check if the string contains at least one letter
        return str.matches(".*[a-zA-Z]+.*");
    }

    private void addItem(String userID, TogglePane addItemToggle) {
        String name = itemAddName.getText();
        String description = itemAddDescription.getText();
        String priceAsString = itemAddPrice.getText();
        int price = 0;

        if(name.isEmpty()){
            warningAddMessage.setText("Item name can't be empty!");
            textEffects.vanishText(warningAddMessage, 2);
            return;
        }

        if(description.isEmpty()){
            warningAddMessage.setText("Item description can't be empty!");
            textEffects.vanishText(warningAddMessage, 2);
            return;
        }

        if(priceAsString.isEmpty()){
            warningAddMessage.setText("Item price can't be empty!");
            textEffects.vanishText(warningAddMessage, 2);
            return;
        }


        if (!containsNonNumeric(priceAsString)) {
            price = Integer.parseInt(priceAsString);
            if (price <= 0) {
                warningAddMessage.setText("Price can only be positive values.");
                textEffects.vanishText(warningAddMessage, 2);
                return;
            }
        } else {
            warningAddMessage.setText("Price can only be digits.");
            textEffects.vanishText(warningAddMessage, 2);
            return;
        }

        String currency = null;
        if (itemAddCurrencyUSD.isSelected()) {
            currency = "USD";
        } else if (itemAddCurrencyMXN.isSelected()) {
            currency = "MXN";
        } else if (itemAddCurrencyEUR.isSelected()) {
            currency = "EUR";
        } else {
            warningAddMessage.setText("Select a currency!");
            textEffects.vanishText(warningAddMessage, 2);
            return;
        }

        int priority = 1;
        if (itemAddPriorityLow.isSelected()) {
            priority = 3;
        } else if (itemAddPriorityMedium.isSelected()) {
            priority = 2;
        } else if (itemAddPriorityHigh.isSelected()) {
            priority = 1;
        }


        Item newItem = new Item(name, description, null, price, currency, priority);
        storageManager storage = storageManager.getInstance();
        storage.addItemLocal(newItem);
        refreshTable(userID);
        addItemToggle.togglePane(addItemPane, null);



    }



    private void setupTableColumns() {
        itemColumnID.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        itemColumnName.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
        itemColumnDescription.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDescription()));
        itemColumnReservation.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getReservation() != null ? cellData.getValue().getReservation().toString() : "None"));
        itemColumnPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        itemColumnPriority.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPriority()));
        itemColumnReserved.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().isReserved()));
        itemColumnSold.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSold()));
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
        clearAddItemFields();
    }

    private void clearAddItemFields() {
        itemAddName.clear();
        itemAddDescription.clear();
        itemAddPrice.clear();
        currencyGroup.selectToggle(null);
        priorityGroup.selectToggle(null);
    }


    private void openAddItemPane(TogglePane addItemToggle) {
        addItemToggle.togglePane(addItemPane, null);
    }
}
