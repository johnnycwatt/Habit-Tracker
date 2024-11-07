package org.habittracker.util;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class NotificationHelper {

    public Label notificationLabel;

    public NotificationHelper(Label notificationLabel) {
        this.notificationLabel = notificationLabel;
    }

    public void showTemporaryMessage(String message, String color) {
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

    public void showMilestoneNotification(String message, String color) {
        Platform.runLater(() -> {
            notificationLabel.setText(message);
            notificationLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
            notificationLabel.setVisible(true);
        });

        new Thread(() -> {
            try {
                Thread.sleep(3000); // 3-second delay for milestones
                Platform.runLater(() -> notificationLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSuccessMessage(String message) {
        showTemporaryMessage(message, "green"); // Uses green for success
    }

    private void showWarningMessage(String message) {
        showTemporaryMessage(message, "red"); // Uses red for warnings
    }
}
