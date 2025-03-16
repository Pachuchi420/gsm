package com.pach.gsm.controllers;

import com.pach.gsm.*;
import it.auties.whatsapp.model.message.standard.ImageMessageSimpleBuilder;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class listViewController {
    @FXML
    private Label warningAddMessage, itemListWarning, addItemTitle, warningMessageWhatsAppLogout;

    @FXML
    private Button logoutButton, addItem, closeAddItemPane,
                   confirmAddItem, cancelAdditem, itemAddImage,
                   removeItem, editItem, whatsappPane, closeWhatsappPane,
                   whatsAppLogout, confirmWhatsapp, sellItem;

    @FXML
    private AnchorPane addItemPane, whatsAppPane, mainPane;

    @FXML
    private TableView<Item> itemList;

    @FXML
    private FontIcon whatsAppStatus, whatsAppOnline;

    @FXML
    private TableColumn<Item, String> itemColumnID, itemColumnName, itemColumnDescription, itemColumnReservationDate;


    @FXML
    private TableColumn<Item, Integer> itemColumnPriority, itemColumnPrice;
    ;


    @FXML
    private TableColumn<Item, Boolean> itemColumnReserved, itemColumnSold, itemColumnSync;


    @FXML
    private TextField itemAddName, itemAddPrice;

    @FXML
    private TextArea itemAddDescription;

    @FXML
    private ToggleButton itemAddPriorityHigh, itemAddPriorityMedium, itemAddPriorityLow;

    @FXML
    private RadioButton itemAddCurrencyUSD, itemAddCurrencyMXN, itemAddCurrencyEUR;

    @FXML
    private ImageView itemAddImageView, imageThumbnail, qrCodeImageView;
    @FXML
    private ImageViewPane imageViewPane;

    @FXML
    private ListView<CheckBox> itemAddGroupsList;

    private ToggleGroup currencyGroup;
    private ToggleGroup priorityGroup;
    private byte[] itemAddImageData;

    private Item itemBeingEdited = null;


    private static DBWorker dbWorker = new DBWorker();


    // Group Management
    @FXML
    private Label groupWarningMessage;

    @FXML
    private TextField groupName, groupInterval;

    @FXML
    private ComboBox<Integer> groupStartHour, groupStartMinute, groupEndHour, groupEndMinute;

    @FXML
    private Button addGroup, removeGroup, updateGroup;

    @FXML
    private TableView<Group> groupList;

    @FXML
    private TableColumn<Group, String> groupNameColumn, groupIntervalColumn, groupStartTimeColumn, groupEndTimeColumn;

    @FXML
    private ToggleButton enableChatbot;

    private Thread botThread;


    // Reserve Item

    @FXML
    private AnchorPane reserveItemPane;

    @FXML
    private Button reserveItem, closeReservePane, confirmReserveItem, cancelReserveitem;

    @FXML
    private TextField reserveItemBuyer, reserveItemPlace;

    @FXML
    private DatePicker reserveItemDate;

    @FXML
    private ComboBox<Integer> reserveItemTimeHour, reserveItemTimeMinutes;

    @FXML
    private Label reserveItemWarningMessage;



    @FXML
    public void initialize() throws IOException {
        storageManager storage = storageManager.getInstance();
        String userID = storage.getUserID();
        storage.initializeDatabase(userID);
        startChatBotThread();
        setupTableColumns();
        addTestMessageColumn();
        refreshTable(userID);
        populateGroupComboBoxes();
        Chatbot.getInstance().qrImageThread(qrCodeImageView);
        itemList.requestFocus();


        supabaseAuthentication.setRefreshTableCallback(() -> refreshTable(userID));

        addItemPane.setVisible(false);
        ToggleVerticalPane addItemToggle = new ToggleVerticalPane(addItemPane, mainPane, true);

        whatsAppPane.setVisible(false);
        ToggleHorizontalPane whatsAppToggle = new ToggleHorizontalPane(whatsAppPane, mainPane, true);

        reserveItemPane.setVisible(false);
        ToggleVerticalPane reserveItemToggle = new ToggleVerticalPane(reserveItemPane, mainPane, true);


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
                    if (whatsAppPane.isVisible()) {
                        closeWhatsappPane(whatsAppToggle);
                    }

                    if (reserveItemPane.isVisible()) {
                        cancelReservation(reserveItemToggle);
                    }
                    break;
                case ENTER:
                    if (whatsAppPane.isVisible()) {
                        addGroup(userID);
                        break;
                    }

                    if (addItemPane.isVisible()) {
                        if (itemBeingEdited != null) {
                            updateItem(itemBeingEdited, addItemToggle);
                        } else {
                            addItem(userID, addItemToggle);
                        }
                        break;
                    }

                    if (reserveItemPane.isVisible()){
                        addReservation(reserveItemToggle);
                        break;
                    }
                    openAddItemPane(addItemToggle);
                    break;
                case BACK_SPACE:
                    if (whatsAppPane.isVisible()) {
                        deleteGroup();
                        break;
                    }

                    if (itemList.getSelectionModel().getSelectedItems().size() > 1) {
                        ObservableList<Item> selectedItems = itemList.getSelectionModel().getSelectedItems();
                        openRemoveMultipleItemsDialog(selectedItems);
                        break;
                    }

                    openRemoveItemDialog();
                    break;
                case W:
                    if (!addItemPane.isVisible()) {
                        openWhatsappPane(whatsAppToggle);
                    }
                default:
                    break;

                case R:
                    if(!reserveItemPane.isVisible()){
                        openReserveItemPane(reserveItemToggle);
                    }


            }
        });


        addItem.setOnAction(event -> openAddItemPane(addItemToggle));
        editItem.setOnAction(event -> openEditItemPane(addItemToggle));
        sellItem.setOnAction(event -> toggleSold());
        removeItem.setOnAction(event -> openRemoveItemDialog());
        confirmAddItem.setOnAction(event -> addItem(userID, addItemToggle));
        closeAddItemPane.setOnAction(event -> cancelAddItem(addItemToggle));
        cancelAdditem.setOnAction(event -> cancelAddItem(addItemToggle));
        logoutButton.setOnAction(event -> openLogoutDialog());

        whatsappPane.setOnAction(event -> openWhatsappPane(whatsAppToggle));

        enableChatbot.setText("Disabled");
        enableChatbot.setSelected(Chatbot.getInstance().isEnabled()); // Optional: set initial state
        enableChatbot.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            Chatbot.getInstance().setEnabled(isSelected); // 🔄 toggle your bot logic

            if (isSelected) {
                enableChatbot.setText("Enabled ");
                setChatBot(true);

            } else {
                enableChatbot.setText("Disabled ");
                setChatBot(false);
            }
        });
        closeWhatsappPane.setOnAction(event -> closeWhatsappPane(whatsAppToggle));
        confirmWhatsapp.setOnAction(event -> closeWhatsappPane(whatsAppToggle));
        whatsAppLogout.setOnAction(event -> openWhatsAppLogoutDialog());
        addGroup.setOnAction(event -> addGroup(userID));
        removeGroup.setOnAction(event -> deleteGroup());
        updateGroup.setOnAction(event -> updateGroup());

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


        groupName.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                groupList.getSelectionModel().clearSelection();
                return;
            }

            for (Group group : groupList.getItems()) {
                if (group.getName().equalsIgnoreCase(newText.trim())) {
                    groupList.getSelectionModel().select(group);
                    populateGroupFields(group);
                    return;
                }
            }

            groupList.getSelectionModel().clearSelection();
        });

        groupList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                groupName.setText(newVal.getName());
                populateGroupFields(newVal);
            }
        });


        reserveItem.setOnAction(event -> openReserveItemPane(reserveItemToggle));
        closeReservePane.setOnAction(event -> cancelReservation(reserveItemToggle));
        confirmReserveItem.setOnAction(event -> addReservation(reserveItemToggle));
        cancelReserveitem.setOnAction(event -> cancelReservation(reserveItemToggle));
        reserveItemDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Disable all dates before today
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #888888;");
                }
            }
        });


    }



    private void cancelReservation(ToggleVerticalPane reserveItemToggle) {
        clearReserveItemFields();
        reserveItemToggle.togglePane(reserveItemPane, null, 0.35);
    }

    private void openReserveItemPane(ToggleVerticalPane reserveItemToggle) {
        Item selectedItem = itemList.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            itemListWarning.setText("Select an item to reserve!");
            effects.vanishText(itemListWarning, 2);
            return;
        }

        // Pre-fill if item already reserved
        Reservation res = selectedItem.getReservation();
        if (res != null && res.getReserved()) {
            reserveItemBuyer.setText(res.getBuyer());
            reserveItemPlace.setText(res.getPlace());
            reserveItemDate.setValue(res.getDate());
            reserveItemTimeHour.setValue(res.getHour());
            reserveItemTimeMinutes.setValue(res.getMinute());
        } else {
            clearReserveItemFields(); // Make sure it's clean for new
        }

        reserveItemToggle.togglePane(reserveItemPane, null, 0.35);
    }



    private void addReservation(ToggleVerticalPane reserveItemToggle) {
        Item selectedItem = itemList.getSelectionModel().getSelectedItem();

        if(reserveItemBuyer.getText().isEmpty()){
            reserveItemWarningMessage.setText("Buyer can't be empty...");
            effects.vanishText(reserveItemWarningMessage, 2);
            reserveItemBuyer.requestFocus();
            return;
        }

        String buyer = reserveItemBuyer.getText();
        String place = reserveItemPlace.getText();


        // Date & Time
        LocalDate date = reserveItemDate.getValue();
        Integer hour =  reserveItemTimeHour.getValue();
        Integer minute =  reserveItemTimeMinutes.getValue();


        // Setting the item reservation
        Reservation itemReservation = selectedItem.getReservation();

        itemReservation.setReserved(true);
        itemReservation.setBuyer(buyer);
        itemReservation.setPlace(place);
        itemReservation.setHour(hour);
        itemReservation.setMinute(minute);
        itemReservation.setDate(date);

        storageManager.getInstance().updateItemLocal(selectedItem);
        updateItemOnSupabase(selectedItem);
        refreshTable(storageManager.getInstance().getUserID());

        // Clearing fields & closing pane
        clearReserveItemFields();
        reserveItemToggle.togglePane(reserveItemPane, null, 0.35);



    }


    private void clearReserveItemFields(){
        reserveItemBuyer.clear();
        reserveItemPlace.clear();
        reserveItemDate.setValue(null);
        reserveItemTimeHour.setValue(null);
        reserveItemTimeMinutes.setValue(null);
    }


    private void toggleSold() {
        Item selectedItem = itemList.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            itemListWarning.setText("Select an item to sell!");
            effects.vanishText(itemListWarning, 2);
            return;
        }

        if (selectedItem.getSold()) {
            selectedItem.setSold(false);
        } else {
            selectedItem.setSold(true);
        }

        storageManager.getInstance().updateItemLocal(selectedItem);
        updateItemOnSupabase(selectedItem);
        refreshTable(selectedItem.getUserID());
    }


    private void populateGroupFields(Group group) {
        groupInterval.setText(String.valueOf(group.getInterval()));
        groupStartHour.setValue(group.getStartHour());
        groupStartMinute.setValue(group.getStartMinute());
        groupEndHour.setValue(group.getEndHour());
        groupEndMinute.setValue(group.getEndMinute());
    }

    private void openWhatsAppLogoutDialog() {
        if (!Chatbot.getInstance().isLoggedIn()) {
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

            if (controller.getGoAhead()) {
                Chatbot chatbotInstance = Chatbot.getInstance();
                chatbotInstance.logout();
                setChatBot(false);
                enableChatbot.setText("Disabled ");
                enableChatbot.setSelected(false);

                storageManager.getInstance().deleteAllGroups();
                storageManager.getInstance().deleteAllItemGroupLinks();

                refreshTable(storageManager.getInstance().getUserID());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startChatBotThread() {
        Thread chatBotThread = new Thread(() -> {
            boolean lastOnlineStatus = false;
            boolean lastLoginStatus = false;


            while (true) {
                boolean isOnline = supabaseAuthentication.checkIfOnline();
                boolean isLoggedIn = Chatbot.getInstance().isLoggedIn();
                boolean isDisconnected = Chatbot.getInstance().isDisconnected();

                // Run UI updates on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    whatsAppOnline.setVisible(isOnline && isLoggedIn);
                    whatsAppStatus.getStyleClass().removeAll("whatsAppStatusDisconnected", "whatsAppStatusOnline", "whatsAppStatusLoggedIn");

                    if (!isOnline) {
                        whatsAppStatus.getStyleClass().add("whatsAppStatusDisconnected");
                        qrCodeImageView.setImage(null);
                        whatsAppOnline.setVisible(false);
                    } else {
                        whatsAppStatus.getStyleClass().add("whatsAppStatusOnline");
                        if (isLoggedIn) {
                            whatsAppStatus.getStyleClass().add("whatsAppStatusLoggedIn");
                            qrCodeImageView.setImage(null);
                            whatsAppOnline.setVisible(true);
                        } else {
                            whatsAppOnline.setVisible(false);
                        }
                    }
                });

                try {
                    Thread.sleep(1000); // Check every second
                } catch (InterruptedException e) {
                    System.out.println("❌ Session thread interrupted: " + e.getMessage());
                    break;
                }
            }
        });

        chatBotThread.setDaemon(true);
        chatBotThread.start();
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

            if (controller.getGoAhead()) {
                logout();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void openEditItemPane(ToggleVerticalPane addItemToggle) {
        Item selectedItem = itemList.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            itemListWarning.setText("Select an item to edit!");
            effects.vanishText(itemListWarning, 2);
            return;
        }

        itemBeingEdited = selectedItem;
        addItemTitle.setText("Edit Item");
        confirmAddItem.setText("Ok!");
        itemAddName.setText(selectedItem.getName());

        List<String> linkedGroupIDs = storageManager.getInstance().getGroupIDsForItem(selectedItem.getId());

        for (CheckBox checkBox : itemAddGroupsList.getItems()) {
            Group group = (Group) checkBox.getUserData();
            checkBox.setSelected(linkedGroupIDs.contains(group.getId()));
        }

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

        selectedItem.setName(name);
        selectedItem.setDescription(description);

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

        // Update linked groups
        List<String> selectedGroupIDs = new ArrayList<>();
        for (CheckBox checkBox : itemAddGroupsList.getItems()) {
            if (checkBox.isSelected()) {
                Group group = (Group) checkBox.getUserData();
                selectedGroupIDs.add(group.getId());
            }
        }
        storageManager.getInstance().updateItemGroupLinks(selectedItem.getId(), selectedGroupIDs);

        refreshTable(storage.getUserID());
        updateItemOnSupabase(selectedItem);
        // Close the pane after updating
        addItemToggle.togglePane(addItemPane, () -> {
            System.out.println("✅ Item updated successfully!");
            System.out.println("✅ Item updated successfully!");
            clearAddItemFields();
        });

        itemBeingEdited = null;
    }

    private void openRemoveItemDialog() {

        if (itemList.getSelectionModel().isEmpty()) {
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

            if (controller.getGoAhead()) {
                storageManager storage = storageManager.getInstance();
                if (supabaseAuthentication.checkIfOnline()) {
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
                    populateAddItemGroupList();
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

        if (name.isEmpty()) {
            warningAddMessage.setText("Item name can't be empty!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }

        if (description.isEmpty()) {
            warningAddMessage.setText("Item description can't be empty!");
            effects.vanishText(warningAddMessage, 2);
            return;
        }

        if (priceAsString.isEmpty()) {
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


        if (itemAddImageView.getImage() == null) {
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

        List<String> selectedGroupIDs = new ArrayList<>();
        for (CheckBox checkBox : itemAddGroupsList.getItems()) {
            if (checkBox.isSelected()) {
                Group group = (Group) checkBox.getUserData();
                selectedGroupIDs.add(group.getId());
            }
        }

        if (selectedGroupIDs.isEmpty()) {
            for (CheckBox checkBox : itemAddGroupsList.getItems()) {
                Group group = (Group) checkBox.getUserData();
                selectedGroupIDs.add(group.getId());
            }
        }

        storage.linkItemToGroups(newItem.getId(), selectedGroupIDs);
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
        itemColumnSync.setCellFactory(column -> new TableCell<>() {
            private final FontIcon syncIcon = new FontIcon();

            {
                syncIcon.setIconSize(16); // Optional: adjust icon size
            }

            @Override
            protected void updateItem(Boolean isSynced, boolean empty) {
                super.updateItem(isSynced, empty);

                if (empty || isSynced == null) {
                    setGraphic(null);
                    return;
                }

                syncIcon.setIconLiteral(isSynced ? "fas-check-circle" : "fas-times-circle");
                syncIcon.getStyleClass().clear();
                syncIcon.getStyleClass().add(isSynced ? "sync-true" : "sync-false");

                setGraphic(syncIcon);
            }
        });

        groupNameColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getName()));
        groupIntervalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getInterval())));
        groupStartTimeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStartTime()));
        groupEndTimeColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getEndTime()));
    }

    private void logout() throws IOException {
        supabaseAuthentication instance = supabaseAuthentication.getInstance();
        if (instance.logoutUser()) {
            if (Chatbot.getInstance().isLoggedIn()) {
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
        addItemToggle.togglePane(addItemPane, null, 0.35);
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
        itemBeingEdited = null;

        for (CheckBox checkBox : itemAddGroupsList.getItems()) {
            checkBox.setSelected(false);
        }
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

                updateItemOnSupabase(givenItem);


            } else {
                System.out.println("⚠️ Failed to sync item to Supabase.");
            }
        }
    }


    private void updateItemOnSupabase(Item givenItem) {
        if (supabaseAuthentication.checkIfOnline()) {
            supabaseDB.updateItem(storageManager.getInstance().getUserID(), givenItem);
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


    private void populateGroupComboBoxes() {
        for (int i = 0; i < 24; i++) {
            groupStartHour.getItems().add(i);
            groupEndHour.getItems().add(i);
            reserveItemTimeHour.getItems().add(i);

        }

        for (int i = 0; i < 60; i += 5) {
            groupStartMinute.getItems().add(i);
            groupEndMinute.getItems().add(i);
            reserveItemTimeMinutes.getItems().add(i);
        }


    }

    private void addGroup(String userID) {
        String name = groupName.getText();
        String intervalString = groupInterval.getText();
        Integer startHour = groupStartHour.getValue();
        Integer startMinute = groupStartMinute.getValue();
        Integer endHour = groupEndHour.getValue();
        Integer endMinute = groupEndMinute.getValue();

        if (name.isEmpty() || intervalString.isEmpty() || startHour == null ||
                startMinute == null || endHour == null || endMinute == null) {
            groupWarningMessage.setText("Please fill in all fields!");
            effects.vanishText(groupWarningMessage, 2);
        }

        if (storageManager.getInstance().getGroupByName(name) != null) {
            groupWarningMessage.setText("Group already added!");
            effects.vanishText(groupWarningMessage, 2);
            groupName.clear();
            return;
        }

        if (Chatbot.getApi().store().findChatByName(name).isEmpty()) {
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
        storageManager storage = storageManager.getInstance();

        if (!itemList.getItems().isEmpty()) {
            openLinkGroupToAllDialog(newGroup, () -> {
                storage.addGroup(newGroup);
                // ✅ Link new group to all items
                List<Item> allItems = itemList.getItems();
                for (Item item : allItems) {
                    storage.linkItemToGroups(item.getId(), List.of(newGroup.getId()));
                }
                refreshTable(userID);
            }, () -> {
                // 👎 User said "no"
                storage.addGroup(newGroup);
                refreshTable(userID);
            });
        } else {
            storage.addGroup(newGroup);
            refreshTable(userID);
        }



    }

    private void openLinkGroupToAllDialog(Group newGroup, Runnable onConfirm, Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/generalDialogBox.fxml"));
            Parent dialogRoot = loader.load();

            generalDialogBoxController controller = loader.getController();
            controller.setDialogTitle("Link group to all items?");
            controller.setDialogBody("There are already items in your list.\nWould you like to link this new group to all of them?");
            controller.setConfirmButtonText("Yes, link");
            controller.setCancelButtonText("No");

            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Link to Items?");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.getGoAhead()) {
                onConfirm.run();
            } else {
                onCancel.run();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteGroup() {
        if (groupList.getSelectionModel().isEmpty()) {
            groupWarningMessage.setText("Select an item to remove!");
            effects.vanishText(groupWarningMessage, 2);
            return;
        }

        Group selectedGroup = groupList.getSelectionModel().getSelectedItem();
        storageManager storage = storageManager.getInstance();
        String userID = storage.getUserID();
        System.out.println("🚨Deleting item!");
        storage.deleteGroup(selectedGroup.getId());
        refreshTable(userID);
    }


    private void deleteAllGroupData() {
        ObservableList<Group> allGroups = groupList.getItems();
        storageManager storage = storageManager.getInstance();
        String userID = storage.getUserID();

        for (Group group : new ArrayList<>(allGroups)) {
            storage.deleteGroup(group.getId());
        }
        storage.deleteAllItemGroupLinks();

        refreshTable(userID);
    }


    private void updateGroup() {
        Group selectedGroup = groupList.getSelectionModel().getSelectedItem();

        if (selectedGroup == null) {
            groupWarningMessage.setText("Select a group to update!");
            effects.vanishText(groupWarningMessage, 2);
            return;
        }

        // Read updated values from UI
        String updatedName = groupName.getText();
        String updatedIntervalStr = groupInterval.getText();
        Integer updatedStartHour = groupStartHour.getValue();
        Integer updatedStartMinute = groupStartMinute.getValue();
        Integer updatedEndHour = groupEndHour.getValue();
        Integer updatedEndMinute = groupEndMinute.getValue();

        if (updatedName.isEmpty() || updatedIntervalStr.isEmpty() ||
                updatedStartHour == null || updatedStartMinute == null ||
                updatedEndHour == null || updatedEndMinute == null) {
            groupWarningMessage.setText("Please fill in all fields to update!");
            effects.vanishText(groupWarningMessage, 2);
            return;
        }

        int updatedInterval;
        try {
            updatedInterval = Integer.parseInt(updatedIntervalStr);
        } catch (NumberFormatException e) {
            groupWarningMessage.setText("Interval must be a valid number!");
            effects.vanishText(groupWarningMessage, 2);
            return;
        }

        // Apply updated values to the selected group
        selectedGroup.setName(updatedName);
        selectedGroup.setInterval(updatedInterval);
        selectedGroup.setStartHour(updatedStartHour);
        selectedGroup.setStartMinute(updatedStartMinute);
        selectedGroup.setEndHour(updatedEndHour);
        selectedGroup.setEndMinute(updatedEndMinute);

        // Save updated group to DB
        storageManager.getInstance().updateGroup(selectedGroup);
        refreshTable(storageManager.getInstance().getUserID());

        System.out.println("✅ Group updated from UI action!");
    }

    private void addTestMessageColumn() {
        TableColumn<Group, Void> testMessageCol = new TableColumn<>("Test");

        testMessageCol.setCellFactory(param -> new TableCell<>() {
            private final Button sendButton = new Button("Send");

            {
                sendButton.getStyleClass().add("sendTestButton"); // optional CSS styling
                sendButton.setOnAction(event -> {
                    Group group = getTableView().getItems().get(getIndex());

                    if (Chatbot.getInstance().isLoggedIn()) {
                        Chatbot.getInstance().sendTestMessage(group.getName());
                        System.out.println("📨 Test message sent to: " + group.getName());
                    } else {
                        groupWarningMessage.setText("You must be logged into WhatsApp!");
                        effects.vanishText(groupWarningMessage, 2);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(sendButton);
                }
            }
        });

        groupList.getColumns().add(testMessageCol); // 👈 adds it to your table
    }

    private void populateAddItemGroupList() {
        ObservableList<CheckBox> groupCheckboxes = FXCollections.observableArrayList();

        for (Group group : groupList.getItems()) {
            CheckBox checkBox = new CheckBox(group.getName());
            checkBox.setUserData(group); // so we can get the group later
            groupCheckboxes.add(checkBox);
        }

        itemAddGroupsList.setItems(groupCheckboxes);
    }

    public void setChatBot(Boolean state) {
        Chatbot.getInstance().setEnabled(state);

        if (state) {
            System.out.println("✅ Enabling chatbot!");
            startSendingMessages();
        } else {
            System.out.println("❌ Disabling chatbot...");
            stopSendingMessages();
        }
    }


    public void startSendingMessages() {
        if (botThread != null && botThread.isAlive()) {
            System.out.println("⚠️ Bot is already running.");
            return;
        }

        botThread = new Thread(() -> {
            int consecutiveFails = 0;

            while (Chatbot.getInstance().isEnabled()) {
                boolean somethingWasSent = false;

                try {
                    List<Item> items = storageManager.getInstance().getEligibleItems();

                    if (items.isEmpty()) {
                        System.out.println("⏳ No eligible items available to send. Retrying in 30s...");
                        consecutiveFails++;
                        if (consecutiveFails >= 5) break;
                        Thread.sleep(30_000);
                        continue;
                    }

                    for (Item item : items) {
                        System.out.println("\n📦 Selected item: " + item.getName());

                        List<Group> groups = storageManager.getInstance().getEligibleGroupsForItem(item);

                        for (Group group : groups) {
                            System.out.println("--------------------------------------");
                            System.out.println("👥 Group: " + group.getName());

                            // ✅ Check 1: Time frame
                            if (!group.isNowWithinTimeWindow()) {
                                System.out.println("❌ Outside our group's start and end times!");
                                continue;
                            }

                            // ✅ Check 2: Group-wide Interval
                            LocalDateTime groupLastSent = storageManager.getInstance().getGroupWideLastUpload(group.getId());
                            if (groupLastSent != null) {
                                long minsSinceLast = Duration.between(groupLastSent, LocalDateTime.now()).toMinutes();
                                if (minsSinceLast < group.getInterval()) {
                                    System.out.println("⏱️ Last message to group was " + minsSinceLast + " min ago. Need " + group.getInterval() + " min.");
                                    continue;
                                }
                            } else {
                                System.out.println("⏱️ Group has never received a message. Passing interval.");
                            }

                            // ✅ Send Message
                            var chat = Chatbot.getApi().store().findChatByName(group.getName()).orElseThrow();
                            var msg = new ImageMessageSimpleBuilder()
                                    .media(item.getImageData())
                                    .caption(
                                            "• " + item.getName() + "\n" +
                                                    "• " + item.getDescription() + "\n" +
                                                    "• " + item.getPrice() + " " + item.getCurrency()
                                    )
                                    .build();

                            Chatbot.getApi().sendMessage(chat, msg);
                            System.out.println("📤 Sent '" + item.getName() + "' to " + group.getName());

                            // ✅ Update timestamps
                            LocalDateTime now = LocalDateTime.now();
                            group.setLastUpload(now);
                            storageManager.getInstance().updateGroup(group);
                            storageManager.getInstance().updateItemGroupLastUploaded(item.getId(), group.getId(), now);
                            somethingWasSent = true;
                            System.out.println("--------------------------------------");
                            Thread.sleep(10000); // between groups
                        }

                        Thread.sleep(10000); // between items
                    }

                    if (somethingWasSent) {
                        consecutiveFails = 0;
                    } else {
                        consecutiveFails++;
                        System.out.println("⚠️ Nothing was sent this round. Consecutive fails: " + consecutiveFails);
                    }

                    if (consecutiveFails >= 20) {
                        System.out.println("❌ Stopping bot after 5 consecutive failed runs.");
                        javafx.application.Platform.runLater(() -> {
                            enableChatbot.setText("Disabled ");
                            enableChatbot.setSelected(false);
                            stopSendingMessages();
                        });

                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("🛑 Bot thread interrupted. Stopping...");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Chatbot.getInstance().setEnabled(false); // make sure flag reflects stopped state
            System.out.println("🛑 Bot thread has stopped.");
        });

        botThread.setDaemon(true);
        botThread.start();
    }


    public void stopSendingMessages() {
        if (botThread != null && botThread.isAlive()) {
            Chatbot.getInstance().setEnabled(false);
            botThread.interrupt();  // optional, in case it’s sleeping
            botThread = null;
            System.out.println("🛑 Messaging stopped.");
        } else {
            System.out.println("⚠️ No active bot thread to stop.");
        }
    }


    private void openRemoveMultipleItemsDialog(ObservableList<Item> selectedItems) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/generalDialogBox.fxml"));
            Parent dialogRoot = loader.load();

            generalDialogBoxController controller = loader.getController();
            controller.setDialogTitle("🗑️ Remove multiple items?");
            controller.setDialogBody("You are about to remove " + selectedItems.size() + " items. Are you sure?");
            controller.setConfirmButtonText("Yes, remove");
            controller.setCancelButtonText("Cancel");

            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Remove Items");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            if (controller.getGoAhead()) {
                storageManager storage = storageManager.getInstance();

                for (Item item : selectedItems) {
                    if (supabaseAuthentication.checkIfOnline()) {
                        System.out.println("🚨Deleting item!");
                        storage.deleteItem(item.getId());
                        refreshTable(storage.getUserID());
                    } else {
                        System.out.println("🚨 Offline! Marking for deletion: " + item.getName());
                        item.setToDelete(true);
                        storage.updateItemLocal(item);
                    }
                }

                refreshTable(storage.getUserID());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
