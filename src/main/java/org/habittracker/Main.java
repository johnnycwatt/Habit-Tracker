package org.habittracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.habittracker.controller.MainController;

import java.io.IOException;

public class Main extends Application {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1024, 768);

            mainController = fxmlLoader.getController(); // Capture MainController instance
            mainController.setMainApp(this);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle("Habit Tracker");
            primaryStage.setScene(scene);
            primaryStage.show();

            LOGGER.info("Application started successfully");

        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error loading FXML file: ", e);
            }

        }
    }

    public MainController getMainController() {
        return mainController;
    }

    public static void main(String[] args) {
        LOGGER.info("Launching Habit Tracker application");
        launch(args);
    }
}
