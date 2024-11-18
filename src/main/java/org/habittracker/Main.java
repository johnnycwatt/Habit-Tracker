package org.habittracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.habittracker.controller.MainController;
import org.habittracker.repository.HabitRepository;

import java.io.IOException;

public class Main extends Application {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private MainController mainController;
    private HabitRepository habitRepository;

    @Override
    public void init() {
        // Initialize HabitRepository here
        LOGGER.info("Initializing HabitRepository...");
        HabitRepository.initialize("habittracker");
        habitRepository = HabitRepository.getInstance();
    }

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
            LOGGER.error("Error loading FXML file: ", e);
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Shutting down application and closing HabitRepository...");
        habitRepository.close();
    }

    public MainController getMainController() {
        return mainController;
    }

    public HabitRepository getHabitRepository() {
        return habitRepository;
    }

    public static void main(String[] args) {
        LOGGER.info("Launching Habit Tracker application");
        launch(args);
    }
}
