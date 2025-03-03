package com.pach.gsm.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class generalDialogBoxController {
    @FXML
    private Label dialogTitle, dialogBody;

    @FXML
    private Button dialogConfirmButton, dialogCancelButton;

    @FXML
    private AnchorPane mainPane;

    private Boolean goAhead = null;


    // Setter for dialogTitle
    public void setDialogTitle(String title) {
        dialogTitle.setText(title);
    }

    // Setter for dialogBody
    public void setDialogBody(String body) {
        dialogBody.setText(body);
    }

    public void setConfirmButtonText(String text){
        dialogConfirmButton.setText(text);
    }

    public void setCancelButtonText(String text){
        dialogCancelButton.setText(text);
    }
    @FXML
    public void initialize() throws IOException {

        dialogConfirmButton.setOnAction(event -> confirmAction());
        dialogCancelButton.setOnAction(event -> cancelAction());

        mainPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    confirmAction();
                    break;
                case ESCAPE:
                    cancelAction();
                    break;
                default:
                    break;
            }
        });

    }

    private void cancelAction() {
        this.goAhead = false;
        Stage stage = (Stage) dialogCancelButton.getScene().getWindow();
        stage.close();
    }

    private void confirmAction() {
        this.goAhead = true;
        Stage stage = (Stage) dialogConfirmButton.getScene().getWindow();
        stage.close();
    }

    public Boolean getGoAhead(){
        return this.goAhead;
    }


}
