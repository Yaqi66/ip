package Pudding.UI;

import java.util.Scanner;

/**
 * Handles all user-facing input and output for the application.
 */
public class Ui {

    private static final String LINE = "____________________________________________________________";
    private static final String LOGO = """
             
    █████▄ ██  ██ ████▄  ████▄  ██ ███  ██  ▄████  
    ██▄▄█▀ ██  ██ ██  ██ ██  ██ ██ ██ ▀▄██ ██  ▄▄▄ 
    ██     ▀████▀ ████▀  ████▀  ██ ██   ██  ▀███▀  
    """;
    private final Scanner scanner;

    /** Creates a new {@code Ui} backed by {@code System.in}. */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /** Prints the horizontal divider line. */
    public void showLine() {
        System.out.println(LINE);
    }

    /** Prints the logo and greeting banner. */
    public void showWelcome() {
        showLine();
        System.out.println(LOGO);
        System.out.println("Hello! I'm Pudding");
        System.out.println("What can I do for you?");
        showLine();
    }

    /** Prints the farewell message. */
    public void showBye() {
        System.out.println("\nBye. Hope to see you again soon!\n");
        showLine();
    }

    /**
     * Prints an error message to the user.
     *
     * @param message the error text to display
     */
    public void showError(String message) {
        System.out.println(message);
    }

    /** Prints a warning that tasks could not be loaded from disk. */
    public void showLoadingError() {
        System.out.println("Warning: failed to load saved tasks. Starting with an empty list.");
    }

    /**
     * Reads one line of user input from stdin.
     *
     * @return the raw input string
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Prints all tasks in the list with 1-based numbering.
     *
     * @param tasks the task list to display
     */
    public void showTaskList(TaskList tasks) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 1; i <= tasks.size(); i++) {
            System.out.println(i + "." + tasks.get(i).toString());
        }
    }

    /**
     * Prints all tasks whose description contains {@code keyword} (case-insensitive).
     *
     * @param tasks   the task list to search
     * @param keyword the search term
     */
    public void showMatchingTasks(TaskList tasks, String keyword) {
        System.out.println("Here are the matching tasks in your list:");
        int count = 0;
        for (int i = 1; i <= tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.description.toLowerCase().contains(keyword.toLowerCase())) {
                count++;
                System.out.println(count + "." + t.toString());
            }
        }
        if (count == 0) {
            System.out.println("No matching tasks found.");
        }
    }

    /**
     * Prints confirmation that a task was added.
     *
     * @param task       the task that was added
     * @param totalTasks the new total number of tasks
     */
    public void showTaskAdded(Task task, int totalTasks) {
        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task.toString());
        System.out.println("Now you have " + totalTasks + " tasks in the list.");
    }

    /**
     * Prints confirmation that a task was marked as done.
     *
     * @param task the task that was marked
     */
    public void showMarked(Task task) {
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  [" + task.getStatusIcon() + "] " + task.description);
    }

    /**
     * Prints confirmation that a task was marked as not done.
     *
     * @param task the task that was unmarked
     */
    public void showUnmarked(Task task) {
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  [" + task.getStatusIcon() + "] " + task.description);
    }

    /**
     * Prints confirmation that a task was deleted.
     *
     * @param task           the task that was removed
     * @param remainingTasks the number of tasks remaining after deletion
     */
    public void showTaskDeleted(Task task, int remainingTasks) {
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + task.toString());
        System.out.println("Now you have " + remainingTasks + " tasks in the list.");
    }
}
