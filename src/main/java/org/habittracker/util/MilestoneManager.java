package org.habittracker.util;

import java.util.Arrays;
import java.util.List;

public class MilestoneManager {
    public static final int FIRST_DAY = 1;
    public static final int SEVEN_DAYS = 7;
    public static final int TWENTY_ONE_DAYS = 21;
    public static final int FIFTY_DAYS = 50;
    public static final int SIXTY_SIX_DAYS = 66;
    public static final int ONE_HUNDRED_DAYS = 100;

    public static final List<Integer> MILESTONES = Arrays.asList(
            FIRST_DAY, SEVEN_DAYS, TWENTY_ONE_DAYS, FIFTY_DAYS, SIXTY_SIX_DAYS, ONE_HUNDRED_DAYS
    );
}