package com.pach.gsm.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class editImageViewController {

    @FXML
    private HBox twoImageLayoutOne,twoImageLayoutTwo,threeImageLayoutOne,threeImageLayoutTwo, fourImageLayout;



    public void handleCase(int window) {
        // Hide all first

        twoImageLayoutOne.setVisible(false);
        twoImageLayoutTwo.setVisible(false);
        threeImageLayoutOne.setVisible(false);
        threeImageLayoutTwo.setVisible(false);
        fourImageLayout.setVisible(false);

        switch (window) {
            case 1:
                twoImageLayoutOne.setVisible(true);
                break;
            case 2:
                twoImageLayoutTwo.setVisible(true);
                break;
            case 3:
                threeImageLayoutOne.setVisible(true);
                break;
            case 4:
                threeImageLayoutTwo.setVisible(true);
                break;
            case 5:
                fourImageLayout.setVisible(true);
                break;
        }
    }
    }

