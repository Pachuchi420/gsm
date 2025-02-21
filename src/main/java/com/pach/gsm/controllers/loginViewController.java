package com.pach.gsm.controllers;

import effects.textEffects;
import effects.TogglePane;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

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
    public void initialize(){


        TogglePane registerToggle = new TogglePane(registerPane, mainPane);
        registerButton.setOnAction(event -> registerToggle.togglePane(registerPane));

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

        loginButton.setOnAction(event -> loginUser());
        confirmRegistration.setOnAction(event -> registerUser());


    }

    private void cancelRegistration(TogglePane registerToggle) {
        registerToggle.togglePane(registerPane);

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

    private void loginUser() {
        if (emailField.getText().isEmpty() || passwordField.getText().isEmpty()){
            warningMessage.setText("Please fill in all fields!");
            textEffects.vanishText(warningMessage);

        }
    }

    private void registerUser(){
        if(emailField1.getText().isEmpty()
                || passwordField1.getText().isEmpty()
                || passwordField2.getText().isEmpty()) {
            warningMessage1.setText("Please fill in all fields!");
            textEffects.vanishText(warningMessage1);
        }
    }


}







