/*
 * MIT License
 *
 * Copyright (c) 2024 Johnny Chadwick-Watt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */



package org.habittracker;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.habittracker.controller.MainController;
import org.habittracker.repository.HabitRepository;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private MainController mainController;
    private HabitRepository habitRepository;

    // Variables to track startup time
    private static long startTimestamp; // From the main() method
    private long initStartTimestamp;   // Start of init() method
    private long startMethodTimestamp; // Start of start() method

    @Override
    public void init() {
        initStartTimestamp = System.currentTimeMillis();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Initializing HabitRepository...");
        }
        HabitRepository.initialize("habittracker");
        habitRepository = HabitRepository.getInstance();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("init() completed in {} ms", System.currentTimeMillis() - initStartTimestamp);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        startMethodTimestamp = System.currentTimeMillis(); // Timestamp at the start of start()
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1024, 768);

            mainController = fxmlLoader.getController();
            mainController.setMainApp(this);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setTitle("Habit Tracker");

            // Set the application icon
            try {
                Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/habitTrackerIcon.png")));
                primaryStage.getIcons().add(appIcon);
            } catch (NullPointerException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Application icon not found at '/habitTrackerIcon.ico'. Default icon will be used.");
                }
            }

            primaryStage.setScene(scene);

            // Add close request handler
            primaryStage.setOnCloseRequest(event -> {
                try {
                    LOGGER.info("Application closing...");
                    if (mainController != null) {
                        mainController.getReminderScheduler().stop(); // Stop reminders
                    }
                    if (habitRepository != null) {
                        habitRepository.close(); // Close database connections
                    }
                } finally {
                    Platform.exit();
                }
            });

            primaryStage.show();

            long endTimestamp = System.currentTimeMillis(); // Timestamp when the primary stage is shown

            // Log with guards
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Application started successfully");
                LOGGER.info("start() method completed in {} ms", endTimestamp - startMethodTimestamp);
                LOGGER.info("Total startup time: {} ms", endTimestamp - startTimestamp);
            }

        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error loading FXML file: ", e);
            }
        }
    }

    @SuppressWarnings("PMD.DoNotTerminateVM")
    @Override
    public void stop() {
        LOGGER.info("Shutting down application...");
        if (mainController != null) {
            if (mainController.getReminderScheduler() != null) {
                mainController.getReminderScheduler().shutdownNow();
            }
        }
        if (habitRepository != null) {
            habitRepository.close();
        }
        LOGGER.info("Application shutdown complete.");
        Platform.exit(); // Ensure JavaFX terminates
        System.exit(0); // Suppression annotation used
    }

    public MainController getMainController() {
        return mainController;
    }

    public HabitRepository getHabitRepository() {
        return habitRepository;
    }

    public static void main(String[] args) {
        startTimestamp = System.currentTimeMillis(); // Timestamp at the start of main()
        LOGGER.info("Launching Habit Tracker application");
        launch(args);
    }
}
