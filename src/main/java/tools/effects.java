package tools;
import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

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


}