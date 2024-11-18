package org.habittracker.util;

import java.util.ArrayList;
import java.util.List;

public class MockNotifier implements Notifier {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void showMessage(String message, String color) {
        messages.add(message); // Capture messages for testing purposes
    }

    public List<String> getMessages() {
        return messages;
    }

    public void clearMessages() {
        messages.clear();
    }
}
