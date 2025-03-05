package tools;

import javafx.animation.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class ToggleHorizontalPane {
    private boolean isPaneActivated;
    private AnchorPane mainPane, paneToMove;

    private double initialXPos;

    public ToggleHorizontalPane(AnchorPane paneToMove, AnchorPane mainPane, boolean dynamic) {
        this.paneToMove = paneToMove;
        this.initialXPos = paneToMove.getTranslateX();
        this.mainPane = mainPane;
        this.isPaneActivated = false;

        if (dynamic) {
            mainPane.heightProperty().addListener((obs, oldHeight, newHeight) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
            mainPane.widthProperty().addListener((obs, oldWidth, newWidth) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
        }
    }

    private void updateMoveToPane(double width, double height) {
        double originalXPos = this.initialXPos;
        double originalWidth = this.paneToMove.getWidth();

        // Update the pane's size
        this.paneToMove.setPrefSize(width, height);

        // Calculate the new Y position
        double newXPos = originalXPos + (width - originalWidth);
        this.initialXPos = newXPos;

        // ✅ If the pane has never been opened, ensure it starts hidden
        if (!isPaneActivated) {
            this.paneToMove.setTranslateX(newXPos);
        }
    }

    public void togglePane(AnchorPane paneToMove, Runnable onFinished) {
        if (!this.isPaneActivated) {
            activatePane(paneToMove, onFinished);
        } else {
            deactivatePane(paneToMove, onFinished);
        }
    }

    private void activatePane(AnchorPane paneToMove, Runnable onFinished) {
        this.isPaneActivated = true;
        this.paneToMove.setVisible(true);
        double mainPaneXSize = mainPane.getWidth();
        double paneToMoveXSize = paneToMove.getWidth();
        double visiblePosition = mainPaneXSize - paneToMoveXSize + 5;

        TranslateTransition slideLeft = new TranslateTransition(Duration.seconds(0.35), paneToMove);
        slideLeft.setToX(visiblePosition);
        slideLeft.setInterpolator(Interpolator.EASE_OUT);

        Timeline jiggle = new Timeline(
                new KeyFrame(Duration.millis(50), new KeyValue(paneToMove.translateXProperty(), visiblePosition - 5)),
                new KeyFrame(Duration.millis(150), new KeyValue(paneToMove.translateXProperty(), visiblePosition))
        );

        slideLeft.setOnFinished(event -> {
            jiggle.play();
            if (onFinished != null) {
                onFinished.run();  // ✅ Run the callback AFTER the animation completes
            }
        });

        slideLeft.play();
    }

    private void deactivatePane(AnchorPane paneToMove, Runnable onFinished) {
        TranslateTransition slideRight = new TranslateTransition(Duration.seconds(0.35), paneToMove);
        slideRight.setToX(mainPane.getWidth()); // Move completely off-screen
        slideRight.setInterpolator(Interpolator.EASE_IN);

        slideRight.setOnFinished(event -> {
            isPaneActivated = false;
            this.paneToMove.setVisible(false);
            if (onFinished != null) {
                onFinished.run();
            }
        });

        slideRight.play();
    }

    public ToggleHorizontalPane(AnchorPane paneToMove, AnchorPane mainPane, boolean dynamic, double speed) {
        this.paneToMove = paneToMove;
        this.initialXPos = paneToMove.getTranslateX();
        this.mainPane = mainPane;
        this.isPaneActivated = false;

        if (dynamic) {
            mainPane.heightProperty().addListener((obs, oldHeight, newHeight) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
            mainPane.widthProperty().addListener((obs, oldWidth, newWidth) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
        }
    }








}
