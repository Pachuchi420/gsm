package com.pach.gsm.controllers;

import com.pach.gsm.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.kordamp.ikonli.javafx.FontIcon;
import tools.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

public class listViewController {
    @FXML
    private Label warningAddMessage, itemListWarning, addItemTitle, warningMessageWhatsAppLogout;

    @FXML
    private Button logoutButton, addItem, closeAddItemPane, confirmAddItem, cancelAdditem, itemAddImage, removeItem, editItem, whatsappPane, closeWhatsappPane, refreshQR, whatsAppLogout;

    @FXML
    private AnchorPane addItemPane, whatsAppPane, mainPane;

    @FXML
    private TableView<Item> itemList;

    @FXML
    private FontIcon whatsAppStatus;

    @FXML
    private TableColumn<Item, String> itemColumnID, itemColumnName, itemColumnDescription, itemColumnReservationDate;


    @FXML
    private TableColumn<Item, Integer>  itemColumnPriority, itemColumnPrice;;


    @FXML
    private TableColumn<Item, Boolean> itemColumnReserved, itemColumnSold, itemColumnSync;


    @FXML
    private TextField itemAddName, itemAddPrice;

    @FXML
    private TextArea itemAddDescription;

    @FXML
    private ToggleButton itemAddPriorityHigh, itemAddPriorityMedium,itemAddPriorityLow;

    @FXML
    private RadioButton itemAddCurrencyUSD, itemAddCurrencyMXN, itemAddCurrencyEUR;

    @FXML
    private ImageView itemAddImageView, imageThumbnail, qrCodeImageView;
    @FXML
    private ImageViewPane imageViewPane;

    private ToggleGroup currencyGroup;
    private ToggleGroup priorityGroup;
    private byte[] itemAddImageData;

    private static DBWorker dbWorker = new DBWorker();



    // Group Management
    @FXML
    private Label groupWarningMessage;

    @FXML
    private TextField groupName, groupInterval;

    @FXML
    private ComboBox<Integer> groupStartHour, groupStartMinute, groupEndHour, groupEndMinute;

    @FXML
    private Button addGroup, removeGroup;

    @FXML
    private TableView<Group> groupList;

    @FXML
    private TableColumn<Group, String> groupNameColumn, groupIntervalColumn, groupStartTimeColumn, groupEndTimeColumn;



    @FXML
    public void initialize() throws IOException {

        storageManager storage = storageManager.getInstance();
        String userID = storage.getUserID();
        storage.initializeDatabase(userID);
        startChatBotThread();
        setupTableColumns();
        refreshTable(userID);
        populateGroupComboBoxes();

        itemList.requestFocus();

        supabaseAuthentication.setRefreshTableCallback(() -> refreshTable(userID));

        addItemPane.setVisible(false);
        ToggleVerticalPane addItemToggle = new ToggleVerticalPane(addItemPane, mainPane, true);

        whatsAppPane.setVisible(false);
        ToggleHorizontalPane whatsAppToggle = new ToggleHorizontalPane(whatsAppPane, mainPane, true);



        currencyGroup = new ToggleGroup();
        itemAddCurrencyUSD.setToggleGroup(currencyGroup);
        itemAddCurrencyMXN.setToggleGroup(currencyGroup);
        itemAddCurrencyEUR.setToggleGroup(currencyGroup);

        itemAddImage.setOnAction(event -> selectImage());

        priorityGroup = new ToggleGroup();
        itemAddPriorityHigh.setToggleGroup(priorityGroup);
        itemAddPriorityMedium.setToggleGroup(priorityGroup);
        itemAddPriorityLow.setToggleGroup(priorityGroup);

        itemList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        mainPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SHIFT:
                    openEditItemPane(addItemToggle);
                    break;
                case ESCAPE:
                    if (addItemPane.isVisible()) {
                        cancelAddItem(addItemToggle);
                    }
                    if (whatsAppPane.isVisible()){
                        closeWhatsappPane(whatsAppToggle);
                    }
                    break;
                case ENTER:
                    if(addItemPane.isVisible()){
                        addItem(userID, addItemToggle);
                        break;
                    }
                    openAddItemPane(addItemToggle);
                    break;
                case BACK_SPACE:
                    openRemoveItemDialog();
                default:
                    break;
            }
        });


        addItem.setOnAction(event -> openAddItemPane(addItemToggle));
        editItem.setOnAction(event -> openEditItemPane(addItemToggle));
        removeItem.setOnAction(event -> openRemoveItemDialog());
        confirmAddItem.setOnAction(event -> addItem(userID, addItemToggle));
        closeAddItemPane.setOnAction(event -> cancelAddItem(addItemToggle));
        cancelAdditem.setOnAction(event -> cancelAddItem(addItemToggle));
        logoutButton.setOnAction(event -> openLogoutDialog());

        whatsappPane.setOnAction(event -> openWhatsappPane(whatsAppToggle));
        closeWhatsappPane.setOnAction(event -> closeWhatsappPane(whatsAppToggle));
        whatsAppLogout.setOnAction(event -> openWhatsAppLogoutDialog());
        refreshQR.setOnAction(event -> refreshQRImage());
        addGroup.setOnAction(event -> addGroup(userID));
        removeGroup.setOnAction(event -> openRemoveGroupDialog());

        itemList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayItemImage(newValue);
            } else {
                imageThumbnail.setImage(null); // Optionally, clear or set a default image when no item is selected
            }
        });


        itemAddDescription.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB && itemAddDescription.isFocused()) {
                itemAddPrice.requestFocus();
            }
        });

        itemAddDescription.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // If focus is lost
                itemAddDescription.setText(itemAddDescription.getText().replace("\t", ""));
            }
        });

    }


    private void openWhatsAppLogoutDialog() {
        if (!Chatbot.getInstance().isLoggedIn()){
            warningMessageWhatsAppLogout.setText("No session to log out of!");
            effects.vanishText(warningMessageWhatsAppLogout, 2);
            return;
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/generalDialogBox.fxml"));
            Parent dialogRoot = loader.load();

            generalDialogBoxController controller = loader.getController();
            controller.setDialogTitle(" 🚨Log out from WhatsApp? ");
            controller.setDialogBody("Are you sure you want to logout from your Whatsapp session?");
            controller.setConfirmButtonText("Yes");
            controller.setCancelButtonText("Actually, no");



            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Log out from WhatsApp?");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.getGoAhead()){
                Chatbot chatbotInstance = Chatbot.getInstance();
                chatbotInstance.logout();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startChatBotThread() {
        Thread chatBotThread = new Thread(() -> {
            while(true){
                Chatbot chatBotInstance = Chatbot.getInstance();
                if(chatBotInstance.isLoggedIn()){
                    whatsAppStatus.getStyleClass().add("whatsAppStatusLoggedIn");
                    qrCodeImageView.setImage(null);
                } try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("❌ Session thread interrupted: " + e.getMessage());
                    break;
                }
            }
        });


        chatBotThread.setDaemon(true);
        chatBotThread.start();

    }

    public void refreshQRImage(){

    }

    private void openLogoutDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/generalDialogBox.fxml"));
            Parent dialogRoot = loader.load();

            generalDialogBoxController controller = loader.getController();
            controller.setDialogTitle(" 🚨Log out? ");
            controller.setDialogBody("Are you sure you want to logout?");
            controller.setConfirmButtonText("Yes");
            controller.setCancelButtonText("Not really.");



            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Log out?");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.getGoAhead()){
                logout();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void openEditItemPane(ToggleVerticalPane addItemToggle) {
        Item selectedItem = itemList.getSelectionModel().getSelectedItem();

        if (selectedItem == null){
            itemListWarning.setText("Select an item to edit!");
            effects.vanishText(itemListWarning, 2);
            return;
        }

        addItemTitle.setText("Edit Item");
        confirmAddItem.setText("Ok!");
        itemAddName.setText(selectedItem.getName());
        itemAddDescription.setText(selectedItem.getDescription());
        itemAddPrice.setText(String.valueOf(selectedItem.getPrice()));

        // Set currency selection
        switch (selectedItem.getCurrency()) {
            case "USD":
                itemAddCurrencyUSD.setSelected(true);
                break;
            case "MXN":
                itemAddCurrencyMXN.setSelected(true);
                break;
            case "EUR":
                itemAddCurrencyEUR.setSelected(true);
                break;
        }

        // Set priority selection
        switch (selectedItem.getPriority()) {
            case 1:
                itemAddPriorityHigh.setSelected(true);
                break;
            case 2:
                itemAddPriorityMedium.setSelected(true);
                break;
            case 3:
                itemAddPriorityLow.setSelected(true);
                break;
        }

        // Set image if available
        if (selectedItem.getImageData() != null && selectedItem.getImageData().length > 0) {
            Image image = new Image(new ByteArrayInputStream(selectedItem.getImageData()));
            itemAddImageView.setImage(image);
            itemAddImageData = selectedItem.getImageData();
        } else {
            itemAddImageView.setImage(null);
            itemAddImageData = null;
        }

        // Open edit item pane
        addItemToggle.togglePane(addItemPane, null, 0.35);
        itemAddName.requestFocus();

        // Change confirmation button action to "update" instead of "add"
        confirmAddItem.setOnAction(event -> updateItem(selectedItem, addItemToggle));
    }


    private void updateItem(Item selectedItem, ToggleVerticalPane addItemToggle) {
        String name = itemAddName.getText();
        String description = itemAddDescription.getText();
        String priceAsString = itemAddPrice.getText();

        if (name.isEmpty() || description.isEmpty() || priceAsString.isEmpty()) {
            warningAddMessage.setText("All fields must be filled!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }

        if (!containsNonNumeric(priceAsString)) {
            int price = Integer.parseInt(priceAsString);
            if (price <= 0) {
                warningAddMessage.setText("Price must be positive!");
                effects.vanishText(warningAddMessage, 2);
                return;
            }
            selectedItem.setPrice(price);
        } else {
            warningAddMessage.setText("Price must be numeric!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }

        // Update currency
        if (itemAddCurrencyUSD.isSelected()) {
            selectedItem.setCurrency("USD");
        } else if (itemAddCurrencyMXN.isSelected()) {
            selectedItem.setCurrency("MXN");
        } else if (itemAddCurrencyEUR.isSelected()) {
            selectedItem.setCurrency("EUR");
        }

        // Update priority
        if (itemAddPriorityLow.isSelected()) {
            selectedItem.setPriority(3);
        } else if (itemAddPriorityMedium.isSelected()) {
            selectedItem.setPriority(2);
        } else if (itemAddPriorityHigh.isSelected()) {
            selectedItem.setPriority(1);
        }

        // Update image
        if (itemAddImageData != null) {
            selectedItem.setImageData(itemAddImageData);
        }

        // Save changes locally
        storageManager storage = storageManager.getInstance();
        storage.updateItemLocal(selectedItem);
        refreshTable(storage.getUserID());

        // Close the pane after updating
        addItemToggle.togglePane(addItemPane, () -> {
            System.out.println("✅ Item updated successfully!");
            updateItemOnSupabase(selectedItem);
            clearAddItemFields();
        });
    }
    private void openRemoveItemDialog() {

        if (itemList.getSelectionModel().isEmpty()){
            itemListWarning.setText("Select an item to remove!");
            effects.vanishText(itemListWarning, 2);
            return;
        }

        Item selectedItem = itemList.getSelectionModel().getSelectedItem();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/generalDialogBox.fxml"));
            Parent dialogRoot = loader.load();

            generalDialogBoxController controller = loader.getController();
            controller.setDialogTitle("️You are removing an item!");
            controller.setDialogBody("Are you sure you want to remove this item?");
            controller.setConfirmButtonText("Yes");
            controller.setCancelButtonText("Nevermind");



            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Remove Item");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.getGoAhead()){
                storageManager storage = storageManager.getInstance();
                if (supabaseAuthentication.checkIfOnline()){
                    System.out.println("🚨Deleting item!");
                    storage.deleteItem(selectedItem.getId());
                    refreshTable(storage.getUserID());
                } else {
                    System.out.println("🚨Offline! Item set for deletion!");
                    selectedItem.setToDelete(true);
                    storage.updateItemLocal(selectedItem);
                    refreshTable(storage.getUserID());
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshTable(String userID) {
        storageManager storage = storageManager.getInstance();

        storage.getAllLocalItems(userID, updatedItems -> {
            if (updatedItems != null) {
                ObservableList<Item> observableItems = FXCollections.observableArrayList(updatedItems);
                FilteredList<Item> filteredItems = new FilteredList<>(observableItems, item -> !item.getToDelete());


                // Ensure UI updates on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    itemList.setItems(filteredItems);
                    System.out.println("✅ TableView updated with latest items for user: " + storage.getUserID());
                });
            } else {
                System.out.println("⚠️ No items found for this user.");
            }
        });

        storage.getAllGroups(userID, updatedGroups -> {
            if (updatedGroups != null) {
                ObservableList<Group> observableGroups = FXCollections.observableArrayList(updatedGroups);

                javafx.application.Platform.runLater(() -> {
                    groupList.setItems(observableGroups);
                    System.out.println("✅ TableView updated with latest groups for user: " + storage.getUserID());
                });
            } else {
                System.out.println("⚠️ No groups found for this user.");
            }
        });
    }

    public boolean containsNonNumeric(String str) {
        // Check if the string contains at least one letter
        return str.matches(".*[a-zA-Z]+.*");
    }

    private void addItem(String userID, ToggleVerticalPane addItemToggle) {
        String name = itemAddName.getText();
        String description = itemAddDescription.getText();
        String priceAsString = itemAddPrice.getText();
        int price = 0;

        if(name.isEmpty()){
            warningAddMessage.setText("Item name can't be empty!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }

        if(description.isEmpty()){
            warningAddMessage.setText("Item description can't be empty!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }

        if(priceAsString.isEmpty()){
            warningAddMessage.setText("Item price can't be empty!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }


        if (priceAsString.contains(" ")) {
            warningAddMessage.setText("Price can't contain space \"_\" characters!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }

        priceAsString = priceAsString.trim();

        if (priceAsString.matches("\\d+")) {
            price = Integer.parseInt(priceAsString);
            if (price <= 0) {
                warningAddMessage.setText("Price can only be positive values.");
                effects.vanishText(warningAddMessage, 2);
                return;
            }
        } else {
            warningAddMessage.setText("Price can only be digits.");
            effects.vanishText(warningAddMessage, 2);
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
            effects.vanishText(warningAddMessage, 2);
            return;
        }


        if(itemAddImageView.getImage() == null){
            warningAddMessage.setText("Select at least one image!");
            effects.vanishText(warningAddMessage, 2);
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




        Item newItem = new Item(name, description, itemAddImageData, price, currency, priority);
        storageManager storage = storageManager.getInstance();
        storage.addItemLocal(newItem);
        refreshTable(userID);

        addItemToggle.togglePane(addItemPane, () -> {
            addItemToSupabase(newItem);
            clearAddItemFields();
        });
    }



    private void setupTableColumns() {
        itemColumnID.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        itemColumnName.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
        itemColumnDescription.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDescription()));
        itemColumnReservationDate.setCellValueFactory(cellData -> {
            LocalDate reservationDate = cellData.getValue().getReservationDate();
            return new SimpleStringProperty(reservationDate != null ? reservationDate.toString() : "");
        });
        itemColumnPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrice()));
        itemColumnPriority.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPriority()));
        itemColumnReserved.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().isReserved()));
        itemColumnSold.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSold()));
        itemColumnSync.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getSupabaseSync()));

        groupNameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
        groupIntervalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInterval())));
        groupStartTimeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStartTime()));
        groupEndTimeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEndTime()));
    }

    private void logout() throws IOException {
        supabaseAuthentication instance = supabaseAuthentication.getInstance();
        if (instance.logoutUser()) {
            if(Chatbot.getInstance().isLoggedIn()){
                Chatbot.getInstance().logout();
            }
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

    private void cancelAddItem(ToggleVerticalPane addItemToggle) {
        addItemToggle.togglePane(addItemPane, null,0.35);
        clearAddItemFields();
    }

    private void clearAddItemFields() {
        itemAddName.clear();
        itemAddDescription.clear();
        itemAddPrice.clear();
        currencyGroup.selectToggle(null);
        priorityGroup.selectToggle(null);
        itemAddImageView.setImage(null);
        itemAddImageData = null;
    }


    private void openAddItemPane(ToggleVerticalPane addItemToggle) {
        itemList.getSelectionModel().clearSelection();
        confirmAddItem.setOnAction(event -> addItem(storageManager.getInstance().getUserID(), addItemToggle));
        addItemTitle.setText("Add Item");
        confirmAddItem.setText("Add");
        addItemToggle.togglePane(addItemPane, null, 0.35);
        itemAddName.requestFocus();
    }


    public void selectImage() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Image image = new Image(fis);
                itemAddImageView.setImage(image);

                itemAddImageData = Files.readAllBytes(file.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addItemToSupabase(Item givenItem) {
        if (supabaseAuthentication.checkIfOnline()) {
            boolean success = supabaseDB.addItem(givenItem.getUserID(), givenItem);

            if (success) {
                System.out.println("✅ Item synced with Supabase.");

                givenItem.setSupabaseSync(true); // Ensure item is marked as synced
                storageManager storage = storageManager.getInstance();

                // Use dbWorker to update item

                dbWorker.submitTask(() -> {
                    storage.updateItemLocal(givenItem);

                    // Fetch items asynchronously and update UI
                    storage.getAllLocalItems(givenItem.getUserID(), updatedItems -> {
                        javafx.application.Platform.runLater(() -> {
                            ObservableList<Item> observableItems = FXCollections.observableArrayList(updatedItems);
                            itemList.setItems(observableItems);
                        });
                    });
                });
            } else {
                System.out.println("⚠️ Failed to sync item to Supabase.");
            }
        }
    }


    private void updateItemOnSupabase(Item givenItem){
        if (supabaseAuthentication.checkIfOnline()) {
            supabaseDB.updateItem(givenItem.getUserID(),givenItem);
        }
    }

    private void displayItemImage(Item item) {
        if (item != null && item.getImageData() != null) {
            Image image = new Image(new ByteArrayInputStream(item.getImageData()));
            imageThumbnail.setImage(image);
        } else {
            imageThumbnail.setImage(null);
        }
        itemList.refresh();
    }


    private void openWhatsappPane(ToggleHorizontalPane whatsAppToggle) {
        whatsAppToggle.togglePane(whatsAppPane, null);
    }

    private void closeWhatsappPane(ToggleHorizontalPane whatsAppToggle) {
        whatsAppToggle.togglePane(whatsAppPane, null);
    }


    private void populateGroupComboBoxes(){
        for (int i = 0; i < 24; i++) {
            groupStartHour.getItems().add(i);
            groupEndHour.getItems().add(i);
        }

        for (int i = 0; i < 60; i += 5) {
            groupStartMinute.getItems().add(i);
            groupEndMinute.getItems().add(i);
        }



    }

    private void addGroup(String userID) {
        String name = groupName.getText();
        String intervalString = groupInterval.getText();
        Integer startHour = groupStartHour.getValue();
        Integer startMinute = groupStartMinute.getValue();
        Integer endHour = groupEndHour.getValue();
        Integer endMinute = groupEndMinute.getValue();

        if(name.isEmpty() || intervalString.isEmpty() || startHour == null ||
           startMinute == null || endHour == null || endMinute == null) {
            groupWarningMessage.setText("Please fill in all fields!");
            effects.vanishText(groupWarningMessage, 2);
        }

        if(Chatbot.getApi().store().findChatByName(name).isEmpty()){
            groupWarningMessage.setText("No contact or group found with that name!");
            effects.vanishText(groupWarningMessage, 2);
            groupName.clear();
            return;
        }

        int interval;
        if (intervalString.contains(" ")) {
            groupWarningMessage.setText("Price can't contain space \"_\" characters!");
            effects.vanishText(groupWarningMessage, 2);
            return;
        }

        intervalString = intervalString.trim();

        if (intervalString.matches("\\d+")) {
            interval = Integer.parseInt(intervalString);
            if (interval <= 0) {
                groupWarningMessage.setText("Price can only be positive values.");
                effects.vanishText(groupWarningMessage, 2);
                return;
            }
        } else {
            groupWarningMessage.setText("Price can only be digits.");
            effects.vanishText(groupWarningMessage, 2);
            return;
        }

        // Create and add the group
        Group newGroup = new Group(name, interval, startHour, startMinute, endHour, endMinute);
        storageManager.getInstance().addGroup(newGroup);
        refreshTable(userID);

    }

    private void openRemoveGroupDialog() {

        if (groupList.getSelectionModel().isEmpty()){
            groupWarningMessage.setText("Select an item to remove!");
            effects.vanishText(groupWarningMessage, 2);
            return;
        }

        Group selectedGroup = groupList.getSelectionModel().getSelectedItem();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/generalDialogBox.fxml"));
            Parent dialogRoot = loader.load();

            generalDialogBoxController controller = loader.getController();
            controller.setDialogTitle("️You are removing a group!");
            controller.setDialogBody("Are you sure you want to remove this group?");
            controller.setConfirmButtonText("Yes");
            controller.setCancelButtonText("Nevermind");



            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Remove Group");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.getGoAhead()){
                String groupID = selectedGroup.getId();
                System.out.println("🚨Deleting group!");
                storageManager.getInstance().deleteGroup(selectedGroup.getId());
                refreshTable(groupID);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
