package org.habittracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.habittracker.controller.MainController;


import java.io.IOException;

public class Main extends Application {
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

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML file: " + e.getMessage());
        }
    }

    public MainController getMainController() {
        return mainController;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
