package com.pach.gsm.controllers;

import com.pach.gsm.supabaseAuthentication;
import tools.ToggleVerticalPane;
import tools.effects;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class loginViewController {


    @FXML
    public ToggleButton togglePassword, togglePassword1, togglePassword2;
    @FXML
    private TextField emailField, passwordTextField, emailField1, passwordTextField1, passwordTextField2;

    @FXML
    private Button registerButton, loginButton, closeRegisterPane, confirmRegistration;

    @FXML
    private AnchorPane registerPane, mainPane;

    @FXML
    private Label warningMessage, warningMessage1;

    @FXML
    private PasswordField passwordField, passwordField1, passwordField2;
    @FXML
    private ToggleButton rememberMeButton;

    private Boolean rememberMe = false;
    private String storedRefreshToken;
    @FXML
    public void initialize() throws IOException {


        ToggleVerticalPane registerToggle = new ToggleVerticalPane(registerPane, mainPane, false);

        mainPane.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                if (registerPane.isVisible()) { // Ensure ESC only works when the pane is open
                    cancelRegistration(registerToggle);
                }
            }
        });

        mainPane.setFocusTraversable(true); // Ensure the pane can receive key events

        ToggleGroup rememberMeGroup = new ToggleGroup();
        rememberMeButton.setToggleGroup(rememberMeGroup);
        rememberMeButton.setOnAction(e -> selectRememberMe());
        registerButton.setOnAction(event -> openRegisterPane(registerToggle));
        closeRegisterPane.setOnAction(event -> cancelRegistration(registerToggle));
        togglePassword.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // ✅ Toggle Password is selected
                passwordTextField.setText(passwordField.getText());
                passwordTextField.setManaged(true);
                passwordTextField.setVisible(true);

                // Hide PasswordField
                passwordField.setManaged(false);
                passwordField.setVisible(false);

            } else {
                // ❌ Toggle Password is not selected
                passwordField.setText(passwordTextField.getText());
                passwordField.setManaged(true);
                passwordField.setVisible(true);

                passwordTextField.setManaged(false);
                passwordTextField.setVisible(false);
            }
        });
        togglePassword1.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected){
                // ✅ Toggle Password is selected

                passwordTextField1.setText(passwordField1.getText());
                passwordTextField1.setManaged(true);
                passwordTextField1.setVisible(true);

                // Hide PasswordField
                passwordField1.setManaged(false);
                passwordField1.setVisible(false);

            } else {
                // ❌ Toggle Password is not selected
                passwordField1.setText(passwordTextField1.getText());
                passwordField1.setManaged(true);
                passwordField1.setVisible(true);

                passwordTextField1.setManaged(false);
                passwordTextField1.setVisible(false);
            }
        });
        togglePassword2.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // ✅ Toggle Password is selected

                passwordTextField2.setText(passwordField2.getText());
                passwordTextField2.setManaged(true);
                passwordTextField2.setVisible(true);

                // Hide PasswordField
                passwordField2.setManaged(false);
                passwordField2.setVisible(false);

            } else {
                // ❌ Toggle Password is not selected
                passwordField2.setText(passwordTextField2.getText());
                passwordField2.setManaged(true);
                passwordField2.setVisible(true);

                passwordTextField.setManaged(false);
                passwordTextField.setVisible(false);
            }
        });

        loginButton.setOnAction(event -> {
            try {
                loginUser();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        confirmRegistration.setOnAction(event -> {
            registerUser(registerToggle);
        });


        applyFocusStyle(togglePassword);



    }




    private void selectRememberMe() {
        rememberMe = rememberMeButton.isSelected();
    }



    private void openRegisterPane(ToggleVerticalPane registerToggle) {
        registerToggle.togglePane(registerPane,null, 0.15);
        emailField1.requestFocus();

    }

    private void cancelRegistration(ToggleVerticalPane registerToggle) {
        registerToggle.togglePane(registerPane,null, 0.15);

        // Clear email fields
        emailField1.clear();

        // Clear password fields
        passwordField1.clear();
        passwordField2.clear();

        // Clear text fields (if they exist)
        passwordTextField1.clear();
        passwordTextField2.clear();

        togglePassword1.setSelected(false);
        togglePassword2.setSelected(false);

    }


    private void registerUser(ToggleVerticalPane registerToggle) {
        if (emailField1.getText().isEmpty()
                || passwordField1.getText().isEmpty()
                || passwordField2.getText().isEmpty()) {
            warningMessage1.setText("Please fill in all fields!");
            effects.vanishText(warningMessage1, 2);
            return;
        }

        String email = emailField1.getText();
        String password1 = passwordField1.getText();
        String password2 = passwordField2.getText();

        if (!password1.equals(password2)) {
            warningMessage1.setText("Passwords do not match!");
            effects.vanishText(warningMessage1, 2);
            passwordField1.clear();
            passwordField2.clear();
            return;
        }

        new Thread(() -> {
            String registrationMessage = supabaseAuthentication.registerUser(email, password1);

            Platform.runLater(() -> {
                if (registrationMessage.equals("success")) {
                    emailField1.clear();
                    passwordField1.clear();
                    passwordField2.clear();
                    passwordTextField1.clear();
                    passwordTextField2.clear();
                    togglePassword1.setSelected(false);
                    togglePassword2.setSelected(false);
                    registerToggle.togglePane(registerPane, null);
                } else {
                    warningMessage1.setText(registrationMessage);
                    effects.vanishText(warningMessage1, 2);
                }


            });

        }).start();
    }



    private void loginUser() throws IOException {
        if(emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            warningMessage.setText("Please fill in all fields!");
            effects.vanishText(warningMessage, 2);
            return;
        }

        String email = emailField.getText();
        String password = passwordField.getText();



        new Thread(() -> {
            String loginMessage = supabaseAuthentication.loginUser(email, password, rememberMe);

            Platform.runLater(() -> {
                if (loginMessage.equals("success")) {
                    emailField.clear();
                    passwordField.clear();
                    passwordTextField.clear();
                    togglePassword.setSelected(false);

                    // Load the list view
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/listView.fxml"));
                    Scene listViewScene = null;
                    try {
                        listViewScene = new Scene(fxmlLoader.load());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setTitle("GSM - ListView");
                    stage.setResizable(true);
                    stage.setScene(listViewScene);
                    return;
                }
                warningMessage.setText(loginMessage);
                effects.vanishText(warningMessage, 2); // Make text fade out
            });

        }).start();
    }

    private void applyFocusStyle(ToggleButton togglePassword) {
        passwordField.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                // When focused, change border color
                togglePassword.setStyle("-fx-border-color: #439329;");
            } else {
                // Reset when unfocused
                togglePassword.setStyle("");
            }
        });
    }





}







