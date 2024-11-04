package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.habittracker.Main;

import java.io.IOException;

public class MainController {

    private Main mainApp;

    @FXML
    private StackPane rootStackPane;

    @FXML
    private VBox mainView;

    @FXML
    private VBox dynamicViewContainer;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        showMainView();
    }

    @FXML
    public void showMainView() {
        mainView.setVisible(true);
        dynamicViewContainer.setVisible(false);
        dynamicViewContainer.getChildren().clear();
    }

    @FXML
    public void showAddHabitView() {
        loadView("/view/AddHabitView.fxml");
    }


    @FXML
    public void showHabitListView() {
        loadView("/view/HabitListView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            if (loader.getController() instanceof AddHabitController) {
                ((AddHabitController) loader.getController()).setMainApp(mainApp);
            } else if (loader.getController() instanceof HabitListController) {
                ((HabitListController) loader.getController()).setMainApp(mainApp);
            }

            dynamicViewContainer.getChildren().setAll(view); // Set the loaded view
            mainView.setVisible(false);
            dynamicViewContainer.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML file: " + fxmlPath);
        }
    }
}
