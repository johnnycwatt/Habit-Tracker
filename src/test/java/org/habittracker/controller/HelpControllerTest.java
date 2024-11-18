package org.habittracker.controller;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.habittracker.util.JavaFxInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HelpControllerTest {

    private HelpController helpController;
    private ListView<String> topicsList;
    private TextArea contentArea;
    private MainController mockMainController;

    @BeforeEach
    void setUp() {
        JavaFxInitializer.initToolkit();
        helpController = new HelpController();

        topicsList = new ListView<>(FXCollections.observableArrayList());
        contentArea = new TextArea();

        helpController.topicsList = topicsList;
        helpController.contentArea = contentArea;

        mockMainController = mock(MainController.class);
    }





    @Test
    void testInitializeTopicsList() {
        Map<String, String> mockHelpContent = Map.of(
                "Topic One", "Content for topic one",
                "Topic Two", "Content for topic two"
        );

        helpController.helpContent = mockHelpContent;

        helpController.initializeTopicsList();

        // Verify that the topics list contains all keys from helpContent
        assertEquals(2, topicsList.getItems().size());
        assertTrue(topicsList.getItems().contains("Topic One"));
        assertTrue(topicsList.getItems().contains("Topic Two"));
    }

    @Test
    void testDisplayContent_ExistingTopic() {
        helpController.helpContent = Map.of("Topic One", "Content for topic one");

        topicsList.getItems().add("Topic One");
        topicsList.getSelectionModel().select("Topic One");

        helpController.displayContent();

        // Verify the correct content is displayed in the TextArea
        assertEquals("Content for topic one", contentArea.getText());
    }

    @Test
    void testDisplayContent_NonExistingTopic() {
        helpController.helpContent = new HashMap<>();

        topicsList.getItems().add("Unknown Topic");
        topicsList.getSelectionModel().select("Unknown Topic");

        helpController.displayContent();

        // Verify default content is displayed in the TextArea
        assertEquals("Content not available.", contentArea.getText());
    }

    @Test
    void testGoBack_WithMainController() {
        helpController.setMainController(mockMainController);

        helpController.goBack();

        // Verify the method call on the MainController
        verify(mockMainController).showSettingsView();
    }

    @Test
    void testGoBack_WithoutMainController() {
        // Do not assign a MainController (null)
        helpController.setMainController(null);

        // Verify no exceptions are thrown
        assertDoesNotThrow(() -> helpController.goBack());
    }

    @Test
    void testInitialize() {
        HelpController spyController = spy(helpController);
        doNothing().when(spyController).loadHelpContent();
        doNothing().when(spyController).initializeTopicsList();

        spyController.initialize();
        verify(spyController, times(1)).loadHelpContent();
        verify(spyController, times(1)).initializeTopicsList();

        assertTrue(topicsList.getSelectionModel().isEmpty() || topicsList.getSelectionModel().getSelectedItem() != null);
    }
}
