package tools;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ResizableImageHelper {
    private static final double HANDLE_SIZE = 10;

    public static Pane makeResizable(ImageView imageView) {
        imageView.setPreserveRatio(false);
        Pane container = new Pane(imageView);
        container.setPrefSize(imageView.getFitWidth(), imageView.getFitHeight());

        Rectangle[] handles = new Rectangle[8]; // 4 corners + 4 sides

        for (int i = 0; i < 8; i++) {
            Rectangle handle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
            handle.setFill(Color.web("#444444", 0.8));  // semi-transparent dark fill
            handle.setStroke(Color.web("#888888"));     // soft border
            handle.setArcWidth(5);
            handle.setArcHeight(5);

// Optional: Add glow on hover
            handle.setOnMouseEntered(e -> handle.setEffect(new javafx.scene.effect.DropShadow(8, Color.web("#439329", 0.85))));
            handle.setOnMouseExited(e -> handle.setEffect(null));
            handle.setCursor(getCursorForHandle(i));
            handle.setVisible(false); // start hidden
            handles[i] = handle;
            container.getChildren().add(handle);
        }

        // Resize logic
        for (int i = 0; i < handles.length; i++) {
            final int index = i;
            final double[] dragStart = new double[2];

            handles[i].setOnMousePressed(event -> {
                dragStart[0] = event.getSceneX();
                dragStart[1] = event.getSceneY();
                event.consume();
            });

            handles[i].setOnMouseDragged(event -> {
                double dx = event.getSceneX() - dragStart[0];
                double dy = event.getSceneY() - dragStart[1];

                double newWidth = imageView.getFitWidth();
                double newHeight = imageView.getFitHeight();

                if (index <= 3) {
                    double delta = Math.max(dx, dy);
                    if (index == 0 || index == 3) delta = Math.min(dx, dy);
                    newWidth += delta;
                    newHeight += delta;
                } else {
                    switch (index) {
                        case 4 -> newHeight += dy; // Top
                        case 5 -> newWidth += dx;  // Right
                        case 6 -> newHeight += dy; // Bottom
                        case 7 -> newWidth += dx;  // Left
                    }
                }

                imageView.setFitWidth(Math.max(40, newWidth));
                imageView.setFitHeight(Math.max(40, newHeight));

                dragStart[0] = event.getSceneX();
                dragStart[1] = event.getSceneY();

                updateHandlePositions(imageView, handles);
                event.consume();
            });
        }

        // Reposition handles on resize
        container.layoutBoundsProperty().addListener((obs, o, n) -> updateHandlePositions(imageView, handles));
        updateHandlePositions(imageView, handles);

        // Click to select and show handles
        container.setOnMouseClicked(e -> {
            showOnlyMyHandles(container);
            e.consume();
        });

        return container;
    }

    private static Cursor getCursorForHandle(int i) {
        return switch (i) {
            case 0 -> Cursor.NW_RESIZE;
            case 1 -> Cursor.NE_RESIZE;
            case 2 -> Cursor.SE_RESIZE;
            case 3 -> Cursor.SW_RESIZE;
            case 4 -> Cursor.N_RESIZE;
            case 5 -> Cursor.E_RESIZE;
            case 6 -> Cursor.S_RESIZE;
            case 7 -> Cursor.W_RESIZE;
            default -> Cursor.DEFAULT;
        };
    }

    private static void updateHandlePositions(ImageView imageView, Rectangle[] handles) {
        double w = imageView.getFitWidth();
        double h = imageView.getFitHeight();
        double half = HANDLE_SIZE / 2.0;

        // Corners
        handles[0].setLayoutX(0 - half);         handles[0].setLayoutY(0 - half);
        handles[1].setLayoutX(w - half);         handles[1].setLayoutY(0 - half);
        handles[2].setLayoutX(w - half);         handles[2].setLayoutY(h - half);
        handles[3].setLayoutX(0 - half);         handles[3].setLayoutY(h - half);

        // Sides
        handles[4].setLayoutX(w / 2 - half);     handles[4].setLayoutY(0 - half);
        handles[5].setLayoutX(w - half);         handles[5].setLayoutY(h / 2 - half);
        handles[6].setLayoutX(w / 2 - half);     handles[6].setLayoutY(h - half);
        handles[7].setLayoutX(0 - half);         handles[7].setLayoutY(h / 2 - half);
    }

    /**
     * Hides all handles for all other resizable containers,
     * and shows only for the one clicked.
     */
    private static void showOnlyMyHandles(Pane selectedContainer) {
        Pane parent = (Pane) selectedContainer.getParent();
        for (Node node : parent.getChildren()) {
            if (node instanceof Pane pane) {
                for (Node child : pane.getChildren()) {
                    if (child instanceof Rectangle rect) {
                        rect.setVisible(pane == selectedContainer);
                    }
                }
            }
        }
    }

    // Call this before snapshotting to hide handles
    public static void setHandlesVisible(Pane container, boolean visible) {
        for (Node node : container.getChildren()) {
            if (node instanceof Rectangle) {
                node.setVisible(visible);
            }
        }
    }

    public static void hideAllHandlesDeep(Pane root) {
        for (Node node : root.getChildrenUnmodifiable()) {
            if (node instanceof Pane pane) {
                setHandlesVisible(pane, false);
                // Recursively go deeper
                hideAllHandlesDeep(pane);
            }
        }
    }


}