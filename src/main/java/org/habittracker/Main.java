package org.habittracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.habittracker.controller.MainController;

import java.io.IOException;

public class Main extends Application {
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 400);

            MainController controller = fxmlLoader.getController();
            controller.setMainApp(this);

            // Sets and shows the main scene
            primaryStage.setTitle("Habit Tracker");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML file: " + e.getMessage());
        }
    }

    public void openAddHabitView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/AddHabitView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 300); // Adjust size as needed
            Stage stage = new Stage();
            stage.setTitle("Add Habit");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading AddHabitView: " + e.getMessage());
        }
    }

    public void openHabitListView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/HabitListView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 450, 400); // Adjust size as needed
            Stage stage = new Stage();
            stage.setTitle("Habit List");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading HabitListView: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
