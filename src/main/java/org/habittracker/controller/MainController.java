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


package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.habittracker.Main;
import org.habittracker.model.Habit;
import org.habittracker.repository.HabitRepository;
import org.habittracker.service.HabitReminderScheduler;
import org.habittracker.service.ReportGenerator;
import org.habittracker.util.HabitCalendarPopulator;
import org.habittracker.util.NotificationHelper;
import org.habittracker.util.Notifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Consumer;

public class MainController {

    private static final Logger LOGGER = LogManager.getLogger(MainController.class);

    // Application reference
    private Main mainApp;

    // UI elements
    @FXML
    StackPane rootStackPane;
    @FXML
    VBox mainView;
    @FXML
    VBox dynamicViewContainer;
    @FXML
    GridPane calendarGrid;
    @FXML
    ListView<Habit> habitsDueTodayList;
    @FXML
    private Label notificationLabel;
    @FXML
    Label calendarMonthLabel;

    // Utility and service classes
    private Notifier notifier;
    private HabitReminderScheduler reminderScheduler;
    private HabitCalendarPopulator calendarPopulator;
    private ReportGenerator reportGenerator;

    // State
    private boolean darkModeStatus;

    @FXML
    void initialize() {
        HabitRepository habitRepository = HabitRepository.getInstance();

        // Initialize utilities and services
        notifier = new NotificationHelper(notificationLabel);
        reminderScheduler = new HabitReminderScheduler(notifier, habitRepository);
        calendarPopulator = new HabitCalendarPopulator(calendarGrid, calendarMonthLabel, habitRepository);
        reportGenerator = new ReportGenerator(habitRepository, notifier);

        // Start reminder scheduler
        reminderScheduler.start();

        // Check for missed reports
        reportGenerator.checkForMissedReports();

        // Start the monthly report scheduler
        reportGenerator.startMonthlyReportScheduler();

        // Set up shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            reminderScheduler.stop();
            reportGenerator.stopScheduler();
        }));

        // Show main view initially
        showMainView();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }


    public void showMainView() {
        mainView.setVisible(true);
        dynamicViewContainer.setVisible(false);
        dynamicViewContainer.getChildren().clear();
        updateHabitsDueToday();
        calendarPopulator.populateCalendar(LocalDate.now(), darkModeStatus);
    }

    public void updateHabitsDueToday() {
        habitsDueTodayList.getItems().clear();
        loadHabitsDueToday();
        habitsDueTodayList.setCellFactory(this::createHabitCellFactory);
    }

    private void loadHabitsDueToday() {
        HabitRepository.getInstance().getAllHabits().stream()
                .filter(this::isHabitDueToday)
                .forEach(habitsDueTodayList.getItems()::add);
    }

    private ListCell<Habit> createHabitCellFactory(ListView<Habit> listView) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Habit habit, boolean empty) {
                super.updateItem(habit, empty);
                if (empty || habit == null) {
                    clearCell();
                } else {
                    populateCell(habit);
                }
            }

            private void clearCell() {
                setText(null);
                setGraphic(null);
                setStyle("");
            }

            private void populateCell(Habit habit) {
                setText(habit.getName());
                boolean isCompletedToday = isHabitCompletedToday(habit);

                applyStyle(isCompletedToday);
                setCompletionGraphic(isCompletedToday);
            }

            private boolean isHabitCompletedToday(Habit habit) {
                return habit.getLastCompletedDate() != null && habit.getLastCompletedDate().equals(LocalDate.now());
            }

            private void applyStyle(boolean isCompletedToday) {
                String backgroundColor = isCompletedToday ? "#d4edda" : "#ffffff";
                String fontWeight = isCompletedToday ? "bold" : "normal";
                String textColor = isCompletedToday ? "#155724" : "#333333";

                setStyle(String.format(
                        "-fx-background-color: %s; -fx-font-weight: %s; -fx-text-fill: %s;",
                        backgroundColor, fontWeight, textColor
                ));
            }

            private void setCompletionGraphic(boolean isCompletedToday) {
                setGraphic(isCompletedToday ? new Label("✔") : null);
            }
        };
    }


    private boolean isHabitDueToday(Habit habit) {
        LocalDate today = LocalDate.now();
        return habit.getFrequency() == Habit.Frequency.DAILY ||
                habit.getCustomDays().contains(today.getDayOfWeek());
    }

    public void enableDarkMode() {
        darkModeStatus = true;
        rootStackPane.getScene().getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
    }

    public void disableDarkMode() {
        darkModeStatus = false;
        rootStackPane.getScene().getStylesheets().remove(getClass().getResource("/css/dark-theme.css").toExternalForm());
    }

    private void loadView(String fxmlPath, Consumer<Object> controllerSetup, String caller) {
        long startTime = System.currentTimeMillis();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            controllerSetup.accept(loader.getController());

            dynamicViewContainer.getChildren().setAll(view);
            mainView.setVisible(false);
            dynamicViewContainer.setVisible(true);

            long endTime = System.currentTimeMillis();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("View {} loaded successfully by {} in {} ms", fxmlPath, caller, (endTime - startTime));
            }
        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            if(LOGGER.isErrorEnabled()){
                LOGGER.error("Error loading FXML file: {} by {} (Elapsed Time: {} ms)", fxmlPath, caller, (endTime - startTime), e);
            }
        }
    }



    @FXML
    public void showSettingsView() {
        loadView("/view/SettingsView.fxml", controller -> {
            if (controller instanceof SettingsController) {
                SettingsController settingsController = (SettingsController) controller;
                settingsController.setMainApp(mainApp);
                settingsController.setMainController(this);
            }
        }, "showSettingsView");
    }

    public void openHelp() {
        loadView("/view/HelpView.fxml", controller -> {
            if (controller instanceof HelpController) {
                HelpController helpController = (HelpController) controller;
                helpController.setMainController(this);
            }
        }, "openHelp");
    }

    @FXML
    public void showAddHabitView() {
        loadView("/view/AddHabitView.fxml", controller -> {
            if (controller instanceof AddHabitController) {
                AddHabitController addHabitController = (AddHabitController) controller;
                addHabitController.setMainApp(mainApp);
            }
        }, "showAddHabitView");
    }

    public void showEditHabitView(Habit habit) {
        loadView("/view/EditHabitView.fxml", controller -> {
            if (controller instanceof EditHabitController) {
                EditHabitController editHabitController = (EditHabitController) controller;
                editHabitController.setHabit(habit);
                editHabitController.setHabitRepository(HabitRepository.getInstance());
                editHabitController.setMainApp(mainApp);
            }
        }, "showEditHabitView");
    }

    public void showProgressView(Habit habit) {
        loadView("/view/ProgressView.fxml", controller -> {
            if (controller instanceof ProgressController) {
                ProgressController progressController = (ProgressController) controller;
                progressController.setMainApp(mainApp);
                progressController.setMainController(this);
                progressController.setHabit(habit);
            }
        }, "showProgressView");
    }

    public void showReportView() {
        loadView("/view/ReportView.fxml", controller -> {
            if (controller instanceof ReportViewController) {
                ReportViewController reportViewController = (ReportViewController) controller;
                reportViewController.setMainController(this);
            }
        }, "showReportView");
    }

    public void showHabitListView() {
        loadView("/view/HabitListView.fxml", controller -> {
            if (controller instanceof HabitListController) {
                HabitListController habitListController = (HabitListController) controller;
                habitListController.setMainApp(mainApp);
                habitListController.setMainController(this);
            }
        }, "showHabitListView");
    }
    public HabitReminderScheduler getReminderScheduler() {
        return reminderScheduler;
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public HabitRepository getHabitRepository() {
        return HabitRepository.getInstance();
    }

    public boolean isDarkModeEnabled() {
        return darkModeStatus;
    }


    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    public void setReminderScheduler(HabitReminderScheduler reminderScheduler) {
        this.reminderScheduler = reminderScheduler;
    }

    public void setCalendarPopulator(HabitCalendarPopulator calendarPopulator) {
        this.calendarPopulator = calendarPopulator;
    }

}
