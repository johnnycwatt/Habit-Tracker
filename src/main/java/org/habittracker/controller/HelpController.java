package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HelpController {

    @FXML
    private ListView<String> topicsList;

    @FXML
    private TextArea contentArea;

    private Map<String, String> helpContent;
    private MainController mainController; // Reference to MainController

    // Method to set MainController from outside this class
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadHelpContent();
        topicsList.getSelectionModel().select(0);
        displayContent();
    }

    private void loadHelpContent() {
        helpContent = new HashMap<>();
        Properties properties = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/help_content.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find help_content.properties");
                return;
            }
            properties.load(input);

            helpContent.put("Getting Started", properties.getProperty("Getting_Started"));
            helpContent.put("Habit Management", properties.getProperty("Habit_Management"));
            helpContent.put("Tracking and Progress", properties.getProperty("Tracking_and_Progress"));
            helpContent.put("Reports", properties.getProperty("Reports"));
            helpContent.put("Settings", properties.getProperty("Settings"));
            helpContent.put("Backup and Restore", properties.getProperty("Backup_and_Restore"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @FXML
    private void displayContent() {
        String selectedTopic = topicsList.getSelectionModel().getSelectedItem();
        contentArea.setText(helpContent.getOrDefault(selectedTopic, "Content not available."));
    }

    @FXML
    private void goBack() {
        // Switch back to SettingsView
        if (mainController != null) {
            mainController.showSettingsView();
        }
    }
}
