package org.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HelpController {

    private static final Logger LOGGER = LogManager.getLogger(HelpController.class);

    @FXML
    ListView<String> topicsList;

    @FXML
    TextArea contentArea;

    Map<String, String> helpContent;
    private MainController mainController; // Reference to MainController


    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadHelpContent();
        initializeTopicsList();
        topicsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayContent());
        topicsList.getSelectionModel().selectFirst(); // Automatically select the first topic
    }


    void loadHelpContent() {
        helpContent = new HashMap<>();
        Properties properties = new Properties();

        try (InputStream input = getClass().getResourceAsStream("/help_content.properties")) {
            if (input == null) {
                LOGGER.warn("Unable to find help_content.properties");
                return;
            }
            properties.load(input);

            for (String key : properties.stringPropertyNames()) {
                helpContent.put(key.replace("_", " "), properties.getProperty(key));
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to load help content from properties file", ex);
        }
    }


    void initializeTopicsList() {
        topicsList.getItems().clear();
        topicsList.getItems().addAll(helpContent.keySet());
    }

    @FXML
    void displayContent() {
        String selectedTopic = topicsList.getSelectionModel().getSelectedItem();
        contentArea.setText(helpContent.getOrDefault(selectedTopic, "Content not available."));
    }

    @FXML
    void goBack() {
        if (mainController != null) {
            mainController.showSettingsView();
        } else {
            LOGGER.warn("MainController reference is not set. Unable to navigate back.");
        }
    }
}
