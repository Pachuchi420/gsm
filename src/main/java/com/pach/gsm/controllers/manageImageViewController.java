package com.pach.gsm.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class manageImageViewController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private HBox twoImageView, threeImageView;

    @FXML
    private Button twoImageLayoutOne, twoImageLayoutTwo, threeImageLayoutOne, threeImageLayoutTwo;


    @FXML
    public void initialize() throws IOException {
        twoImageLayoutOne.setOnAction(event -> openEditWindow(1));
        twoImageLayoutTwo.setOnAction(event -> openEditWindow(2));
        threeImageLayoutOne.setOnAction(event -> openEditWindow(3));
        threeImageLayoutTwo.setOnAction(event -> openEditWindow(4));
    }

    private void openEditWindow(int window){
        switch (window){
            case 1:
                System.out.println("Open two image layout 1");
                changeToEditWindow(1);
                break;
            case 2:
                System.out.println("Open two image layout 2");
                changeToEditWindow(2);
                break;
            case 3:
                System.out.println("Open three image layout 1");
                changeToEditWindow(3);
                break;
            case 4:
                System.out.println("Open three image layout 2");
                changeToEditWindow(4);
                break;
        }
    }


    private void changeToEditWindow(int window){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/pach/gsm/views/editImageView.fxml"));
            Parent editView = loader.load();

            // ✅ Grab the actual controller instance
            editImageViewController controller = loader.getController();

            // ✅ Replace view content
            rootPane.getChildren().setAll(editView);
            AnchorPane.setTopAnchor(editView, 0.0);
            AnchorPane.setBottomAnchor(editView, 0.0);
            AnchorPane.setLeftAnchor(editView, 0.0);
            AnchorPane.setRightAnchor(editView, 0.0);

            // ✅ Tell it which layout to show
            controller.handleCase(window);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showTwoImageLayout(){
        threeImageView.setVisible(false);
        twoImageView.setVisible(true);
    }

    public void showThreeImageLayout(){
        twoImageView.setVisible(false);
        threeImageView.setVisible(true);
    }



}
