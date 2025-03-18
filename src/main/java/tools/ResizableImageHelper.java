package tools;

import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ResizableImageHelper {
    private static final double HANDLE_SIZE = 10;

    public static Pane makeResizable(ImageView imageView) {
        imageView.setPreserveRatio(false);
        Pane container = new Pane(imageView);
        container.setPrefSize(imageView.getFitWidth(), imageView.getFitHeight());

        Rectangle handle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        handle.setFill(Color.WHITE);
        handle.setStroke(Color.DARKGRAY);
        handle.setCursor(Cursor.SE_RESIZE);

        container.getChildren().add(handle);

        // 👉 Position the handle manually
        container.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            updateHandlePosition(imageView, handle);
        });

        // Handle dragging to resize
        final double[] dragStart = new double[2];

        handle.setOnMousePressed(event -> {
            dragStart[0] = event.getSceneX();
            dragStart[1] = event.getSceneY();
            event.consume();
        });

        handle.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - dragStart[0];
            double dy = event.getSceneY() - dragStart[1];

            double newWidth = imageView.getFitWidth() + dx;
            double newHeight = imageView.getFitHeight() + dy;

            imageView.setFitWidth(Math.max(40, newWidth));
            imageView.setFitHeight(Math.max(40, newHeight));

            dragStart[0] = event.getSceneX();
            dragStart[1] = event.getSceneY();

            updateHandlePosition(imageView, handle);
            event.consume();
        });

        updateHandlePosition(imageView, handle);

        return container;
    }

    private static void updateHandlePosition(ImageView imageView, Rectangle handle) {
        // Position relative to the image
        double w = imageView.getFitWidth();
        double h = imageView.getFitHeight();
        handle.setLayoutX(w - HANDLE_SIZE / 2);
        handle.setLayoutY(h - HANDLE_SIZE / 2);
    }
}