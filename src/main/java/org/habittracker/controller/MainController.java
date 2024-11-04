package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    private Main mainApp;
    private final HabitRepository habitRepository = HabitRepository.getInstance();
    @FXML
    private ListView<String> habitsDueTodayList;

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
        updateHabitsDueToday();
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


    public void updateHabitsDueToday() {
        habitsDueTodayList.getItems().clear(); // Clear existing entries
        HabitRepository habitRepository = HabitRepository.getInstance();

        List<Habit> habits = habitRepository.getAllHabits();
        LocalDate today = LocalDate.now();

        for (Habit habit : habits) {
            // Check if habit is due today based on start date and frequency
            if (isHabitDueToday(habit, today)) {
                habitsDueTodayList.getItems().add(habit.getName()); // Display only due habits
            }
        }
    }

    // Check if the habit is due today based on its frequency and last completed date
    private boolean isHabitDueToday(Habit habit, LocalDate today) {
        if (habit.getCreationDate().isAfter(today)) {
            return false;
        }

        switch (habit.getFrequency()) {
            case DAILY:
                return true;
            case WEEKLY:
                return !habit.getCreationDate().isAfter(today.minusWeeks(1)); // Due if a week has passed
            case MONTHLY:
                return !habit.getCreationDate().isAfter(today.minusMonths(1)); // Due if a month has passed
            default:
                return false;
        }
    }


}
