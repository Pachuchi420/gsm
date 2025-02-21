package effects;

import javafx.animation.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class TogglePane {

    private boolean isPaneActivated;
    private AnchorPane mainPane;

    double initialYPos;

    public TogglePane(AnchorPane paneToMove, AnchorPane mainPane) {
        this.initialYPos = paneToMove.getTranslateY();
        this.mainPane = mainPane;
        this.isPaneActivated = false;
    }

    public void togglePane(AnchorPane paneToMove) {
        if (!this.isPaneActivated) {
            activatePane(paneToMove);
        } else {
            deactivatePane(paneToMove);
        }
    }

    private void activatePane(AnchorPane paneToMove) {
        this.isPaneActivated = true;
        double mainPaneYSize = mainPane.getHeight();
        double paneToMoveYSize = paneToMove.getHeight();
        double visiblePosition =  mainPaneYSize - paneToMoveYSize + 5 ;


        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(0.15), paneToMove);
        slideUp.setToY(visiblePosition);
        slideUp.setInterpolator(Interpolator.EASE_OUT);

        Timeline jiggle = new Timeline(
                new KeyFrame(Duration.millis(50), new KeyValue(paneToMove.translateYProperty(), visiblePosition - 5)),
                new KeyFrame(Duration.millis(150), new KeyValue(paneToMove.translateYProperty(), visiblePosition))
        );

        slideUp.setOnFinished(event -> jiggle.play());
        slideUp.play();
    }

    private void deactivatePane(AnchorPane paneToMove) {
        TranslateTransition slideDown = new TranslateTransition(Duration.seconds(0.15), paneToMove);
        slideDown.setToY(this.initialYPos);
        slideDown.setInterpolator(Interpolator.EASE_IN);

       /* registerEmail.clear();
        registerPassword.clear();
        registerPassword2.clear();
        warningEmail2.setText("");
        warningPassword2.setText("");*/

        slideDown.setOnFinished(event -> {
            isPaneActivated = false;
        });

        slideDown.play();

    }
}