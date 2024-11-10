package org.habittracker.util;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class NotificationHelper implements Notifier {
    private final Label notificationLabel;

    public NotificationHelper(Label notificationLabel) {
        this.notificationLabel = notificationLabel;
    }

    @Override
    public void showMessage(String message, String color) {
        Platform.runLater(() -> {
            notificationLabel.setText(message);
            notificationLabel.setStyle("-fx-text-fill: " + color + ";");
            notificationLabel.setVisible(true);
        });

        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2-second delay
                Platform.runLater(() -> notificationLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
