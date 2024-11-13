package org.habittracker.util;

import javafx.application.Platform;

public class JavaFxInitializer {
    private static boolean initialized = false;

    public static void initToolkit() {
        if (!initialized) {
            try {
                Platform.startup(() -> {});
                initialized = true;
            } catch (IllegalStateException e) {
                // Ignore if JavaFX is already initialized
            }
        }
    }
}
