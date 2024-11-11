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
        if (notificationLabel != null) {
            Platform.runLater(() -> {
                notificationLabel.setText(message);
                notificationLabel.setStyle("-fx-text-fill: " + color + ";");
                notificationLabel.setVisible(true);
            });

            Platform.runLater(() -> {
                try {
                    Thread.sleep(4000);  //4 second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                notificationLabel.setVisible(false);
            });
        } else {
            System.out.println("Notification Label is not initialized.");
        }
    }
}