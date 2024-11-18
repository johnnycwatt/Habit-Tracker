package org.habittracker.util;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class JavaFxInitializer {
    private static boolean initialized;
    private static final Logger LOGGER = LogManager.getLogger(JavaFxInitializer.class);


    public static void initToolkit() {
        if (!initialized) {
            try {
                Platform.startup(() -> {});
                initialized = true;
            } catch (IllegalStateException e) {
                LOGGER.warn("JavaFX platform is already initialized, so we can ignore this exception.");
            }
        }
    }
}
