package org.habittracker.util;

import javafx.application.Platform;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NotificationHelper implements Notifier {
    private final Label notificationLabel;
    private static final Logger LOGGER = LogManager.getLogger(NotificationHelper.class);

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
                Thread.sleep(4000); // 4-second delay
                Platform.runLater(() -> notificationLabel.setVisible(false));
            } catch (InterruptedException e) {
                LOGGER.error("Thread was interrupted while trying to hide the notification label.", e);
            }
        }).start();

    }
}
