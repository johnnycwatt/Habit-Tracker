# Habit Tracker

## Overview

Habit Tracker is a simple application designed to help users build and maintain good habits through effective tracking and reporting. The app allows users to create customizable habits with various frequencies and provides insightful progress reports to keep motivation high.

## Features

- **Habit Management**: Add, edit, and delete habits with customizable frequency options (Daily, Weekly, Monthly, and custom days), colors for easy identification, and start dates.
- **Progress Tracking**: Monitor habit streaks and performance over time. View completed dates with a visual calendar.
- **Reports**: Access monthly summaries of habit completion rates and consistency.
- **Reminders and Notifications**: Enable reminders for upcoming weekly and monthly habits.
- **Dark Mode**: Toggle dark mode for a better viewing experience in low-light environments.
- **Backup and Restore**: Safeguard habit data with backup to JSON files and easy data restoration.

## Getting Started

### Prerequisites

The Habit Tracker Beta Release comes bundled with a Java Runtime Environment (JRE) and the necessary JavaFX modules. No additional software is required to run the application.

### Installation

1. **Download the Beta Release**
   - Download the `Habit-Tracker.zip` file from the release section of the repository.

2. **Extract the Zip File**
   - Use a tool like **7-Zip** or the default extraction tool in Windows to extract the contents of `Habit-Tracker.zip` to a folder of your choice.


### Running the Application

1. **Navigate to the Folder**
- Open the folder where you extracted the `Habit-Tracker` files.

2. **Launch the Application**
- Double-click on the `Habit-Tracker.exe` file to start the application.

3. **First-Time Setup**
- The application will automatically load the bundled runtime and required resources. No additional setup is needed.

4. **Enjoy Using Habit Tracker**
- Follow the [Usage](#usage) instructions to explore the app's features.

### Troubleshooting

- **Error: Missing Configuration File**
- Ensure that the `habitTrackerconfig.xml` file is located in the same folder as `Habit-Tracker.exe`.

- **Error: Application Does Not Start**
- Verify that the extracted `runtime` folder contains the bundled JRE and JavaFX modules.

- **Logs and Error Reporting**
- Check the `logs/` directory for any application logs that can help diagnose issues.


## Usage

### Habit Management

#### Adding a New Habit
1. Click the **Add New Habit** button on the home screen.
2. Enter the habit name, choose a color, select a frequency, and set a start date.
3. Tap **Add Habit** to save the new habit.

#### Editing an Existing Habit
1. Go to the **View Habits** screen by clicking the **View Habits** button.
2. Select the habit you want to edit and click **Edit Habit**.
3. Modify its details and click **Save**.

#### Deleting a Habit
1. In the **View Habits** screen, select the habit you want to delete.
2. Click the **Delete Selected** button.
3. Confirm the deletion to permanently remove the habit.

*Note: Deleted habits cannot be recovered.*

### Tracking and Progress

#### Using ProgressView
1. In the **View Habits** screen, select a habit and click **View Progress** to open ProgressView.
2. View details like current streak and consistency statistics.
3. The calendar shows completion dates, and the bar chart displays completion trends over time.

#### Understanding Streak Tracking
- Streaks are calculated based on your habit's frequency.
- Completing a daily habit every day increases your daily streak.
- Use streaks as motivational milestones.

#### Viewing Statistics
- In ProgressView, check the statistics section for an overview of your performance.
- Analyze completion trends to adjust your habits accordingly.

### Reports

#### Accessing Monthly Reports
1. Go to the **View Habits** screen and click **View Monthly Reports**.
2. View a breakdown of each habit's completion rate, longest streak, and consistency for the selected month.

#### Analyzing Habit Trends
- Reports offer insights into your consistency and progress.
- Identify trends over time to improve habit formation.

#### Understanding Report Data
- Each monthly report includes completion rate, longest streak, and consistency.
- Gain an overview of how often you completed each habit within that month.

### Settings

#### Configuring Reminders
1. Open **Settings** by clicking the settings icon on the home screen.
2. Enable or disable reminders to receive notifications for upcoming weekly and monthly habits.

#### Dark Mode
- In **Settings**, toggle dark mode to switch to a darker theme, reducing eye strain in low-light environments.

### Backup and Restore

#### Backing Up Your Data
1. In the **Settings** menu, click the **Export to JSON** button to save your habit data to a JSON file.
2. Store backups locally to prevent accidental data loss.

#### Restoring Data
1. Click **Restore** in the **Settings** menu.
2. Select your backup file to reload all saved habits and progress.

*Important: Restoring data will overwrite any current data in the app. Be sure to back up any new or unsaved data before performing a restore.*

## Architecture Overview

The Habit Tracker app is built using JavaFX and follows the Model-View-Controller (MVC) design pattern for organized and maintainable code.

### Main Components

- **Model**: Represents the data and business logic.
  - **Habit**: Defines a habit's properties such as name, frequency, color, and start date.
  - **HabitRepository**: Manages CRUD operations for habits and handles data persistence.
- **View**: JavaFX `.fxml` files representing the UI elements.
  - **MainView**: Home screen displaying today’s habits and navigation options.
  - **AddHabitView** and **EditHabitView**: Screens for adding or editing habits.
  - **ProgressView**: Displays individual habit progress, statistics, and calendar completions.
  - **ReportView**: Shows monthly reports on habit performance.
  - **SettingsView** and **HelpView**: For configuration options and help documentation.
- **Controller**: Coordinates interactions between the Model and the View.
  - **MainController**: Manages application flow and updates the habit list and calendar.
  - **SettingsController**: Manages settings like reminders and dark mode.
  - **HelpController**: Loads help content and displays it in HelpView.
  - **ProgressController** and **ReportViewController**: Handle logic for progress and report data.
- **Utility Classes**:
  - **NotificationHelper**: Handles user notifications and reminders.
  - **BackupScheduler** and **JsonBackupHelper**: Manage data backup and restoration.

### Application Flow

- **Start-Up**: The `Main` class initializes `MainController`, which loads and manages views.
- **User Interactions**: Each view has a corresponding controller that listens for user actions and updates the Model via `HabitRepository`.
- **Data Persistence**: `HabitRepository` handles saving and retrieving data
- **Backup and Notifications**:
  - `BackupScheduler` manages scheduled data backups.
  - `JsonBackupHelper` enables manual backup and restore options.
  - `NotificationHelper` handles user notifications.

### Design Patterns and Principles

- **MVC Pattern**: Separates UI, business logic, and application flow for easier maintenance.
- **Singleton Pattern**: Ensures only one instance of `HabitRepository` manages habit data.
- **Observer Pattern**: Allows views to observe changes in the Model for real-time updates.

### Class Interactions and Dependencies

- **MainController** orchestrates navigation and view management across the app.
- Specialized controllers like `SettingsController` and `HelpController` handle their respective views but rely on `MainController` for navigation.
- Utility classes support core functionality without cluttering the main controllers.

## Contributing

Thank you for your interest in contributing to the Habit Tracker project!

At this time, this is a personal project, and I am not actively seeking external contributions. However, I welcome your feedback, suggestions, and feature requests. If you have ideas or notice areas for improvement, please feel free to:

1. Open an issue in the [GitHub Issues](https://github.com/johnnycwatt/Habit-Tracker/issues) section of this repository.
2. Include as much detail as possible about your suggestion or feedback, such as:
   - A clear description of the feature, enhancement, or bug.
   - Any supporting screenshots or examples.
   - How the change could improve the user experience or functionality.

### For Developers Interested in Contributing Code

While I am not accepting pull requests at this time, if you want to fork the project and experiment, feel free to do so under the terms of the MIT License. You can share your fork or modifications with me by:
1. Opening an issue to discuss your changes.
2. Including a link to your forked repository for me to review.

While I cannot guarantee that all requests will be implemented, I will review and consider each one carefully as part of the project’s development.

Thank you for understanding and for supporting the Habit Tracker project!


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.txt) file for details.

## Contact

For questions, feedback, or issues, please reach out:

- **Email**: [johnnycwatt@gmail.com](mailto:johnnycwatt@gmail.com)
- **GitHub**: [Johnny's GitHub](https://github.com/johnnycwatt)
- **Project Issues**: Use the [Issue Tracker](https://github.com/johnnycwatt/Habit-Tracker/issues) to report bugs or request features.


## Gallery:

**Main View**
Here you can view the Habits that are due today. You can also hover over the calendar days to view the habits that are due on those dates.

![image](https://github.com/user-attachments/assets/884bb7b5-4792-4898-819c-cf701e7b4935)

**Habit List View**
Here you can view all of your habits, mark habits as complete, edit/delete habits, and select and navigate to each habits progress view. 

![image](https://github.com/user-attachments/assets/54e576cc-f0b0-47a8-b19c-b736f90db60d)

**Progress View**
Here you can track progress of each individual habit. Showing the number of weeks and months that you have remained consistent, best/current streaks, and completion rates. This is calculated by your selected Habit Frequency. (Daily, Weekly, Monthly or Custom)

![image](https://github.com/user-attachments/assets/c4e9d5e7-e603-46a6-bb62-b84ad548ccba)

**Dark Mode**
![image](https://github.com/user-attachments/assets/85a5f50a-534b-4630-829a-455bbe8142da)
