package com.pach.gsm.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tools.ResizableImageHelper;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class editImageViewController {

    @FXML private HBox twoImageLayoutOne, twoImageLayoutTwo, threeImageLayoutOne, threeImageLayoutTwo, fourImageLayout;
    @FXML private ImageView image1, image2, image3;
    @FXML private StackPane twoImageLayoutOneSlot1, twoImageLayoutOneSlot2;
    @FXML private Button acceptImageEdit, cancelImageEdit;

    private Consumer<Image> onImageConfirmed;

    public void setOnImageConfirmed(Consumer<Image> callback) {
        this.onImageConfirmed = callback;
    }

    @FXML
    public void initialize() {
        makeDraggable(image1);
        makeDraggable(image2);
        makeDraggable(image3);

        applyRoundedCorners(image1, 10);
        applyRoundedCorners(image2, 10);
        applyRoundedCorners(image3, 10);

        makeSlotAcceptDrop(twoImageLayoutOneSlot1);
        makeSlotAcceptDrop(twoImageLayoutOneSlot2);

        acceptImageEdit.setOnAction(event -> handleAcceptImageEdit());
        cancelImageEdit.setOnAction(event -> handleCancelImageEdit());
    }

    public void loadImages(List<Image> images) {
        if (images.size() > 0) image1.setImage(images.get(0));
        if (images.size() > 1) image2.setImage(images.get(1));
        if (images.size() > 2) image3.setImage(images.get(2));
    }

    private void makeDraggable(ImageView thumbnail) {
        thumbnail.setOnDragDetected(event -> {
            if (thumbnail.getImage() == null) return;

            Dragboard db = thumbnail.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putImage(thumbnail.getImage());
            db.setContent(content);
            event.consume();
        });
    }


    private void makeSlotAcceptDrop(StackPane slot) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(slot.widthProperty());
        clip.heightProperty().bind(slot.heightProperty());
        slot.setClip(clip); // 👈 this is what you're adding

        // Rest of your logic stays the same
        slot.setOnDragOver(event -> {
            if (event.getDragboard().hasImage()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        slot.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasImage()) {
                ImageView droppedImage = new ImageView(db.getImage());
                droppedImage.setFitWidth(slot.getWidth());
                droppedImage.setFitHeight(slot.getHeight());
                droppedImage.setPreserveRatio(true);
                droppedImage.setPickOnBounds(true);

                Pane resizableContainer = ResizableImageHelper.makeResizable(droppedImage);
                makeImageMovable(resizableContainer); // ✅ move the container, not just image

                slot.getChildren().clear();
                slot.getChildren().add(resizableContainer);


                if (image1.getImage() == db.getImage()) image1.setVisible(false);
                if (image2.getImage() == db.getImage()) image2.setVisible(false);
                if (image3.getImage() == db.getImage()) image3.setVisible(false);

                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }
    private void makeImageMovable(Pane imageView) {
        final Delta dragDelta = new Delta();

        imageView.setOnMousePressed(event -> {
            dragDelta.x = event.getX();
            dragDelta.y = event.getY();
        });

        imageView.setOnMouseDragged(event -> {
            imageView.setTranslateX(imageView.getTranslateX() + event.getX() - dragDelta.x);
            imageView.setTranslateY(imageView.getTranslateY() + event.getY() - dragDelta.y);
        });
    }

    private static class Delta {
        double x, y;
    }

    public void handleCase(int window) {
        twoImageLayoutOne.setVisible(false);
      /*  twoImageLayoutTwo.setVisible(false);
        threeImageLayoutOne.setVisible(false);
        threeImageLayoutTwo.setVisible(false);
        fourImageLayout.setVisible(false);*/

        switch (window) {
            case 1 -> twoImageLayoutOne.setVisible(true);
           /* case 2 -> twoImageLayoutTwo.setVisible(true);
            case 3 -> threeImageLayoutOne.setVisible(true);
            case 4 -> threeImageLayoutTwo.setVisible(true);
            case 5 -> fourImageLayout.setVisible(true);*/
        }
    }

    private void applyRoundedCorners(ImageView imageView, double radius) {
        Rectangle clip = new Rectangle();
        clip.setWidth(imageView.getFitWidth());
        clip.setHeight(imageView.getFitHeight());
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        imageView.setClip(clip);

        // Ensure the clip resizes with the image
        imageView.fitWidthProperty().addListener((obs, oldVal, newVal) -> clip.setWidth(newVal.doubleValue()));
        imageView.fitHeightProperty().addListener((obs, oldVal, newVal) -> clip.setHeight(newVal.doubleValue()));
    }

    private void handleAcceptImageEdit() {
        WritableImage snapshot = twoImageLayoutOne.snapshot(new SnapshotParameters(), null);
        if (onImageConfirmed != null) {
            onImageConfirmed.accept(snapshot); // send back
        }

        // Close window
        Stage stage = (Stage) acceptImageEdit.getScene().getWindow();
        stage.close();
    }

    private void handleCancelImageEdit() {
        // close the window
        Stage stage = (Stage) cancelImageEdit.getScene().getWindow();
        stage.close();
    }
}