package effects;

import javafx.animation.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class TogglePane {

    private boolean isPaneActivated;
    private AnchorPane mainPane;
    private double initialYPos;

    public TogglePane(AnchorPane paneToMove, AnchorPane mainPane) {
        this.initialYPos = paneToMove.getTranslateY();
        this.mainPane = mainPane;
        this.isPaneActivated = false;
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
        double mainPaneYSize = mainPane.getHeight();
        double paneToMoveYSize = paneToMove.getHeight();
        double visiblePosition = mainPaneYSize - paneToMoveYSize + 5;

        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.15), paneToMove);
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
        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(0.15), paneToMove);
        slideDown.setToY(this.initialYPos);
        slideDown.setInterpolator(Interpolator.EASE_IN);

        slideDown.setOnFinished(event -> {
            isPaneActivated = false;
            if (onFinished != null) {
                onFinished.run(); // ✅ Run the callback AFTER the animation completes
            }
        });
        slideDown.play();
    }
}