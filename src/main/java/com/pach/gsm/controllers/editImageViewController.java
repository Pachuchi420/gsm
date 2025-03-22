package com.pach.gsm.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import tools.ResizableImageHelper;

import java.util.List;
import java.util.function.Consumer;

public class editImageViewController {

    @FXML private AnchorPane rootAnchorPane;

    @FXML private HBox twoImageLayoutOne, threeImageLayoutOne;
    @FXML private VBox twoImageLayoutTwo, threeImageLayoutTwo;
    @FXML private VBox fourImageLayout;

    @FXML private ImageView image1, image2, image3, image4;

    @FXML private StackPane twoImageLayoutOneSlot1, twoImageLayoutOneSlot2;
    @FXML private StackPane twoImageLayoutTwoSlot1, twoImageLayoutTwoSlot2;
    @FXML private StackPane threeImageLayoutOneSlot1, threeImageLayoutOneSlot2, threeImageLayoutOneSlot3;
    @FXML private StackPane threeImageLayoutTwoSlot1, threeImageLayoutTwoSlot2, threeImageLayoutTwoSlot3;
    @FXML private StackPane fourImageLayoutSlot1,fourImageLayoutSlot2,fourImageLayoutSlot3,fourImageLayoutSlot4;

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
        makeDraggable(image4);

        applyRoundedCorners(image1, 10);
        applyRoundedCorners(image2, 10);
        applyRoundedCorners(image3, 10);
        applyRoundedCorners(image4, 10);

        makeSlotAcceptDrop(twoImageLayoutOneSlot1);
        makeSlotAcceptDrop(twoImageLayoutOneSlot2);
        makeSlotAcceptDrop(twoImageLayoutTwoSlot1);
        makeSlotAcceptDrop(twoImageLayoutTwoSlot2);

        makeSlotAcceptDrop(threeImageLayoutOneSlot1);
        makeSlotAcceptDrop(threeImageLayoutOneSlot2);
        makeSlotAcceptDrop(threeImageLayoutOneSlot3);

        makeSlotAcceptDrop(threeImageLayoutTwoSlot1);
        makeSlotAcceptDrop(threeImageLayoutTwoSlot2);
        makeSlotAcceptDrop(threeImageLayoutTwoSlot3);

        makeSlotAcceptDrop(fourImageLayoutSlot1);
        makeSlotAcceptDrop(fourImageLayoutSlot2);
        makeSlotAcceptDrop(fourImageLayoutSlot3);
        makeSlotAcceptDrop(fourImageLayoutSlot4);

        acceptImageEdit.setOnAction(event -> handleAcceptImageEdit());
        cancelImageEdit.setOnAction(event -> handleCancelImageEdit());

        rootAnchorPane.setOnMousePressed(event -> {
            Node clickedNode = event.getPickResult().getIntersectedNode();

            for (StackPane slot : getAllSlots()) {
                if (!slot.getChildren().isEmpty() && slot.getChildren().get(0) instanceof Pane container) {
                    boolean isClicked = isAncestorOf(container, clickedNode);
                    ResizableImageHelper.setHandlesVisible(container, isClicked);
                }
            }
        });

        rootAnchorPane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                for (StackPane slot : getAllSlots()) {
                    if (!slot.getChildren().isEmpty()) {
                        Node child = slot.getChildren().get(0);

                        // ✅ check focus inside resizable container
                        if (child instanceof Pane pane && pane.isFocused()) {
                            ImageView img = (ImageView) pane.getChildren().get(0);
                            restoreImageToSidebar(img.getImage());

                            slot.getChildren().clear();
                            break; // optional: delete only one per key press
                        }
                    }
                }
            }
        });
    }

    public void loadImages(List<Image> images) {
        if (images.size() > 0) image1.setImage(images.get(0));
        if (images.size() > 1) image2.setImage(images.get(1));
        if (images.size() > 2) image3.setImage(images.get(2));
        if (images.size() > 3) image4.setImage(images.get(3));
    }



    private void restoreImageToSidebar(Image image) {
        if (image1.getImage() == null) {
            image1.setImage(image);
            image1.setVisible(true);
            image1.setOpacity(1.0);
        } else if (image2.getImage() == null) {
            image2.setImage(image);
            image2.setVisible(true);
            image2.setOpacity(1.0);
        } else if (image3.getImage() == null) {
            image3.setImage(image);
            image3.setVisible(true);
            image3.setOpacity(1.0);
        } else if (image4.getImage() == null) {
            image4.setImage(image);
            image4.setVisible(true);
            image4.setOpacity(1.0);
        }
    }
    private void makeDraggable(ImageView thumbnail) {
        thumbnail.setOnDragDetected(event -> {
            if (thumbnail.getImage() == null) return;

            Dragboard db = thumbnail.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putImage(thumbnail.getImage());
            db.setContent(content);

            // Show smaller preview when dragging
            Image original = thumbnail.getImage();
            double previewSize = 64;
            double ratio = original.getWidth() / original.getHeight();
            double width = previewSize;
            double height = previewSize;

            if (ratio > 1) height = previewSize / ratio;
            else width = previewSize * ratio;

            ImageView preview = new ImageView(original);
            preview.setFitWidth(width);
            preview.setFitHeight(height);
            preview.setPreserveRatio(true);

            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(javafx.scene.paint.Color.TRANSPARENT);
            WritableImage dragImage = preview.snapshot(snapParams, null);

            db.setDragView(dragImage);
            thumbnail.setOpacity(0.3); // visually indicate "dragging"
            event.consume();
        });

        // ✅ Handle drop result
        thumbnail.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.COPY) {
                // Drop was successful → hide this image
                thumbnail.setImage(null);
            } else {
                // Not dropped anywhere → reset opacity
                thumbnail.setOpacity(1.0);
            }
        });
    }

    private void makeSlotAcceptDrop(StackPane slot) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(slot.widthProperty());
        clip.heightProperty().bind(slot.heightProperty());
        slot.setClip(clip);

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
                droppedImage.setPreserveRatio(true);
                droppedImage.setPickOnBounds(true);
                droppedImage.setFitHeight(slot.getHeight());
                droppedImage.setFitWidth(slot.getWidth());

                Rectangle imageClip = new Rectangle();
                imageClip.setArcWidth(20);
                imageClip.setArcHeight(20);
                imageClip.widthProperty().bind(droppedImage.fitWidthProperty());
                imageClip.heightProperty().bind(droppedImage.fitHeightProperty());
                droppedImage.setClip(imageClip);

                Pane resizableContainer = ResizableImageHelper.makeResizable(droppedImage);
                makeImageMovable(resizableContainer);

                resizableContainer.setFocusTraversable(true);
                resizableContainer.requestFocus();

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
            imageView.requestFocus();
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
        twoImageLayoutTwo.setVisible(false);
        threeImageLayoutOne.setVisible(false);
        threeImageLayoutTwo.setVisible(false);
        fourImageLayout.setVisible(false);

        switch (window) {
            case 1 -> twoImageLayoutOne.setVisible(true);
            case 2 -> twoImageLayoutTwo.setVisible(true);
            case 3 -> threeImageLayoutTwo.setVisible(true);
            case 4 -> threeImageLayoutOne.setVisible(true);
            case 5 -> fourImageLayout.setVisible(true);
        }
    }

    private void applyRoundedCorners(ImageView imageView, double radius) {
        Rectangle clip = new Rectangle();
        clip.setWidth(imageView.getFitWidth());
        clip.setHeight(imageView.getFitHeight());
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        imageView.setClip(clip);

        imageView.fitWidthProperty().addListener((obs, oldVal, newVal) -> clip.setWidth(newVal.doubleValue()));
        imageView.fitHeightProperty().addListener((obs, oldVal, newVal) -> clip.setHeight(newVal.doubleValue()));
    }

    private List<StackPane> getAllSlots() {
        return List.of(
                twoImageLayoutOneSlot1, twoImageLayoutOneSlot2,
                twoImageLayoutTwoSlot1, twoImageLayoutTwoSlot2,
                threeImageLayoutOneSlot1, threeImageLayoutOneSlot2, threeImageLayoutOneSlot3,
                threeImageLayoutTwoSlot1, threeImageLayoutTwoSlot2, threeImageLayoutTwoSlot3,
                fourImageLayoutSlot1, fourImageLayoutSlot2, fourImageLayoutSlot3, fourImageLayoutSlot4
        );
    }

    private boolean isAncestorOf(Node ancestor, Node node) {
        while (node != null) {
            if (node == ancestor) return true;
            node = node.getParent();
        }
        return false;
    }

    private void handleAcceptImageEdit() {
        ResizableImageHelper.hideAllHandlesDeep(rootAnchorPane);
        WritableImage snapshot = null;

        if (twoImageLayoutOne.isVisible()) {
            snapshot = twoImageLayoutOne.snapshot(new SnapshotParameters(), null);
        } else if (twoImageLayoutTwo.isVisible()) {
            snapshot = twoImageLayoutTwo.snapshot(new SnapshotParameters(), null);
        } else if(threeImageLayoutOne.isVisible()){
            snapshot = threeImageLayoutOne.snapshot(new SnapshotParameters(), null);
        } else if(threeImageLayoutTwo.isVisible()){
            snapshot = threeImageLayoutTwo.snapshot(new SnapshotParameters(), null);
        } else if (fourImageLayout.isVisible()){
            snapshot = fourImageLayout.snapshot(new SnapshotParameters(), null);
        }

        if (snapshot != null && onImageConfirmed != null) {
            onImageConfirmed.accept(snapshot);
        }

        Stage stage = (Stage) acceptImageEdit.getScene().getWindow();
        stage.close();
    }

    private void handleCancelImageEdit() {
        Stage stage = (Stage) cancelImageEdit.getScene().getWindow();
        stage.close();
    }



}