package tools;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.util.Duration;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import org.kordamp.ikonli.javafx.FontIcon;

public class effects {
    private static FadeTransition fadeOut; // Store transition globally

    public static void vanishText(Label chosenLabel) {
        // Ensure label is visible and reset opacity
        chosenLabel.setVisible(true);
        chosenLabel.setOpacity(1.0);

        // If an animation is running, stop it before starting a new one
        if (fadeOut != null && fadeOut.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            fadeOut.stop();
        }

        // Create a new FadeTransition
        fadeOut = new FadeTransition(Duration.seconds(1), chosenLabel);
        fadeOut.setFromValue(1.0); // Start fully visible
        fadeOut.setToValue(0.0);   // Fade to transparent
        fadeOut.setOnFinished(event -> {
            chosenLabel.setVisible(false); // Hide after fade
            chosenLabel.setOpacity(1.0);  // Reset for future use
        });

        fadeOut.play(); // Start the fade-out effect
    }

    public static void vanishText(Label chosenLabel, int timeinSecs) {
        // Ensure label is visible and reset opacity
        chosenLabel.setVisible(true);
        chosenLabel.setOpacity(1.0);

        // If an animation is running, stop it before starting a new one
        if (fadeOut != null && fadeOut.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            fadeOut.stop();
        }

        // Create a new FadeTransition
        fadeOut = new FadeTransition(Duration.seconds(3), chosenLabel);
        fadeOut.setFromValue(1.0); // Start fully visible
        fadeOut.setToValue(0.0);   // Fade to transparent
        fadeOut.setOnFinished(event -> {
            chosenLabel.setVisible(false); // Hide after fade
            chosenLabel.setOpacity(1.0);  // Reset for future use
        });

        fadeOut.play(); // Start the fade-out effect
    }

    public static void applyRoundedCorners(ImageView imageView, double radius) {
        Rectangle clip = new Rectangle(
                imageView.getFitWidth(),
                imageView.getFitHeight()
        );
        clip.setArcWidth(radius);
        clip.setArcHeight(radius);
        imageView.setClip(clip);
    }


    // === ✅ NEW: Generic fade-in ============
    public static FadeTransition fadeIn(Node node, double durationSecs) {
        FadeTransition fade = new FadeTransition(Duration.seconds(durationSecs), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        return fade;
    }

    // === ✅ NEW: Generic fade-out ============
    public static FadeTransition fadeOut(Node node, double durationSecs, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.seconds(durationSecs), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        return fade;
    }


    public static void addHoverOnMenu(Node node, String message) {
        Popup tooltipPopup = new Popup();
        Label tooltipLabel = new Label(message);
        tooltipLabel.setStyle(
                "-fx-background-color: #333333cc;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 6px 12px;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 12px;"
        );
        tooltipLabel.setOpacity(0);
        tooltipPopup.getContent().add(tooltipLabel);
        tooltipPopup.setAutoHide(true);

        PauseTransition hoverDelay = new PauseTransition(Duration.seconds(0.75));

        node.setOnMouseEntered(event -> {
            hoverDelay.setOnFinished(e -> {
                tooltipPopup.show(node, event.getScreenX() + 10, event.getScreenY() + 10);
                tooltipLabel.setOpacity(0);
                effects.fadeIn(tooltipLabel, 0.25).playFromStart();
            });
            hoverDelay.playFromStart();
        });

        node.setOnMouseExited(event -> {
            hoverDelay.stop();
            effects.fadeOut(tooltipLabel, 0.25, tooltipPopup::hide).playFromStart();
        });
    }


    public static void addHoverOnMenuIcon(Node node, String message, String iconCode) {
        Popup tooltipPopup = new Popup();

        HBox tooltipContent = new HBox(2);
        tooltipContent.setStyle(
                "-fx-background-color: #333333cc;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6px 12px;" +
                        "-fx-alignment: center-left;"
        );

        FontIcon iconLabel = new FontIcon(iconCode);
        iconLabel.setIconSize(14);
        iconLabel.setIconColor(javafx.scene.paint.Color.WHITE);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        messageLabel.setOpacity(1); // animation target

        tooltipContent.getChildren().addAll(iconLabel, messageLabel);
        tooltipContent.setOpacity(0);
        tooltipPopup.getContent().add(tooltipContent);
        tooltipPopup.setAutoHide(true);

        PauseTransition hoverDelay = new PauseTransition(Duration.seconds(0.75));

        node.setOnMouseEntered(event -> {
            hoverDelay.setOnFinished(e -> {
                tooltipPopup.show(node, event.getScreenX() + 10, event.getScreenY() + 10);
                effects.fadeIn(tooltipContent, 0.25).playFromStart();
            });
            hoverDelay.playFromStart();
        });

        node.setOnMouseExited(event -> {
            hoverDelay.stop();
            effects.fadeOut(tooltipContent, 0.25, tooltipPopup::hide).playFromStart();
        });
    }






}