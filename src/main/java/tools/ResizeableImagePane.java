package tools;

import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ResizeableImagePane extends Pane {
    private final ImageView imageView;
    private final double HANDLE_SIZE = 8;

    private double clickX, clickY;

    public ResizeableImagePane(ImageView imageView) {
        this.imageView = imageView;
        getChildren().add(imageView);
        setPickOnBounds(false);
        initResizeHandles();
        makeMovable();
    }

    private void initResizeHandles() {
        Rectangle bottomRight = createHandle(Cursor.SE_RESIZE);

        bottomRight.setOnMouseDragged(e -> {
            double newWidth = e.getX();
            double newHeight = e.getY();

            imageView.setFitWidth(Math.max(20, newWidth));
            imageView.setFitHeight(Math.max(20, newHeight));

            layoutHandles();
            e.consume();
        });

        getChildren().add(bottomRight);
        layoutHandles();
    }

    private Rectangle createHandle(Cursor cursor) {
        Rectangle handle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        handle.setFill(Color.WHITE);
        handle.setStroke(Color.GRAY);
        handle.setStrokeWidth(1);
        handle.setCursor(cursor);
        handle.setManaged(false);
        return handle;
    }

    private void layoutHandles() {
        // Only using bottom-right for now (index 1)
        if (getChildren().size() > 1) {
            Rectangle br = (Rectangle) getChildren().get(1);
            br.setLayoutX(imageView.getLayoutX() + imageView.getFitWidth() - HANDLE_SIZE / 2);
            br.setLayoutY(imageView.getLayoutY() + imageView.getFitHeight() - HANDLE_SIZE / 2);
        }
    }

    private void makeMovable() {
        setOnMousePressed(event -> {
            clickX = event.getSceneX() - getLayoutX();
            clickY = event.getSceneY() - getLayoutY();
        });

        setOnMouseDragged(event -> {
            double newX = event.getSceneX() - clickX;
            double newY = event.getSceneY() - clickY;

            // Optional: Clamp within parent
            if (getParent() != null) {
                double parentWidth = getParent().getLayoutBounds().getWidth();
                double parentHeight = getParent().getLayoutBounds().getHeight();

                newX = Math.max(0, Math.min(newX, parentWidth - getWidth()));
                newY = Math.max(0, Math.min(newY, parentHeight - getHeight()));
            }

            setLayoutX(newX);
            setLayoutY(newY);
        });
    }

    public ImageView getImageView() {
        return imageView;
    }
}