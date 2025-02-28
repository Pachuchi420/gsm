package effects;

import javafx.animation.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TogglePane {

    private boolean isPaneActivated;
    private AnchorPane mainPane, paneToMove;

    private double initialYPos;

    public TogglePane(AnchorPane paneToMove, AnchorPane mainPane, boolean dynamic) {
        this.paneToMove = paneToMove;
        this.initialYPos = paneToMove.getTranslateY();
        this.mainPane = mainPane;
        this.isPaneActivated = false;

        if (dynamic) {
            mainPane.heightProperty().addListener((obs, oldHeight, newHeight) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
            mainPane.widthProperty().addListener((obs, oldWidth, newWidth) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
        }
    }



    private void updateMoveToPane(double width, double height) {
        double originalYPos = this.initialYPos;
        double originalHeight = this.paneToMove.getHeight();

        // Update the pane's size
        this.paneToMove.setPrefSize(width, height);

        // Calculate the new Y position
        double newYPos = originalYPos + (height - originalHeight);
        this.initialYPos = newYPos;

        // ✅ If the pane has never been opened, ensure it starts hidden
        if (!isPaneActivated) {
            this.paneToMove.setTranslateY(newYPos);
        }
    }

    // ✅ Toggle with Callback
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
        double mainPaneYSize = mainPane.getHeight();
        double paneToMoveYSize = paneToMove.getHeight();
        double visiblePosition = mainPaneYSize - paneToMoveYSize + 5;

        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.35), paneToMove);
        slideUp.setToY(visiblePosition);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        Timeline jiggle = new Timeline(
                new KeyFrame(Duration.millis(50), new KeyValue(paneToMove.translateYProperty(), visiblePosition - 5)),
                new KeyFrame(Duration.millis(150), new KeyValue(paneToMove.translateYProperty(), visiblePosition))
        );

        slideUp.setOnFinished(event -> {
            jiggle.play();
            if (onFinished != null) {
                onFinished.run();  // ✅ Run the callback AFTER the animation completes
            }
        });

        slideUp.play();
    }

    private void deactivatePane(AnchorPane paneToMove, Runnable onFinished) {
        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(0.35), paneToMove);
        slideDown.setToY(this.initialYPos);
        slideDown.setInterpolator(Interpolator.EASE_IN);

        slideDown.setOnFinished(event -> {
            isPaneActivated = false;
            this.paneToMove.setVisible(false);
            if (onFinished != null) {
                onFinished.run(); // ✅ Run the callback AFTER the animation completes
            }
        });
        slideDown.play();

    }



    public TogglePane(AnchorPane paneToMove, AnchorPane mainPane, boolean dynamic, double speed) {
        this.paneToMove = paneToMove;
        this.initialYPos = paneToMove.getTranslateY();
        this.mainPane = mainPane;
        this.isPaneActivated = false;

        if (dynamic) {
            mainPane.heightProperty().addListener((obs, oldHeight, newHeight) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
            mainPane.widthProperty().addListener((obs, oldWidth, newWidth) -> updateMoveToPane(mainPane.getWidth(), mainPane.getHeight()));
        }
    }



    public void togglePane(AnchorPane paneToMove, Runnable onFinished, double speed) {
        if (!this.isPaneActivated) {
            activatePane(paneToMove, onFinished, speed);
        } else {
            deactivatePane(paneToMove, onFinished, speed);
        }
    }


    private void activatePane(AnchorPane paneToMove, Runnable onFinished, double speed) {
        this.isPaneActivated = true;
        this.paneToMove.setVisible(true);
        double mainPaneYSize = mainPane.getHeight();
        double paneToMoveYSize = paneToMove.getHeight();
        double visiblePosition = mainPaneYSize - paneToMoveYSize + 5;

        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(speed), paneToMove);
        slideUp.setToY(visiblePosition);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        Timeline jiggle = new Timeline(
                new KeyFrame(Duration.millis(50), new KeyValue(paneToMove.translateYProperty(), visiblePosition - 5)),
                new KeyFrame(Duration.millis(150), new KeyValue(paneToMove.translateYProperty(), visiblePosition))
        );

        slideUp.setOnFinished(event -> {
            jiggle.play();
            if (onFinished != null) {
                onFinished.run();  // ✅ Run the callback AFTER the animation completes
            }
        });

        slideUp.play();
    }

    private void deactivatePane(AnchorPane paneToMove, Runnable onFinished, double speed) {
        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(speed), paneToMove);
        slideDown.setToY(this.initialYPos);
        slideDown.setInterpolator(Interpolator.EASE_IN);

        slideDown.setOnFinished(event -> {
            isPaneActivated = false;
            this.paneToMove.setVisible(false);
            if (onFinished != null) {
                onFinished.run(); // ✅ Run the callback AFTER the animation completes
            }
        });
        slideDown.play();

    }


}