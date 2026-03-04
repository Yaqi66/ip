package Pudding.UI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main class for the Pudding chatbot application.
 * Owns the top-level {@link Storage}, {@link TaskList}, and {@link Ui} instances
 * and drives the main command loop.
 */
public class Pudding {

    private Storage storage;
    private TaskList tasks;
    private Ui ui;

    /**
     * Initialises Pudding by creating the UI, loading saved tasks from {@code filePath}.
     * If the file cannot be read, starts with an empty task list.
     *
     * @param filePath path to the data file used for persistent storage
     */
    public Pudding(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            tasks = new TaskList(storage.load());
        } catch (PuddingException e) {
            ui.showLoadingError();
            tasks = new TaskList();
        }
    }

    /**
     * Starts the main event loop: reads commands, parses them, executes them,
     * and repeats until an {@link ExitCommand} signals the end.
     */
    public void run() {
        ui.showWelcome();
        boolean isExit = false;
        while (!isExit) {
            try {
                String fullCommand = ui.readCommand();
                ui.showLine();
                Command c = Parser.parse(fullCommand);
                c.execute(tasks, ui, storage);
                isExit = c.isExit();
            } catch (PuddingException e) {
                ui.showError(e.getMessage());
            } finally {
                ui.showLine();
            }
        }
    }

    /**
     * Entry point of the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new Pudding("src/main/java/Pudding/dataLog.txt").run();
    }

    /**
     * Represents an application-level error in Pudding (e.g. bad user input,
     * missing file, corrupted data).
     */
    public static class PuddingException extends Exception {
        /**
         * @param message human-readable description of the error
         */
        public PuddingException(String message) {
            super(message);
        }
    }

    /**
     * Abstract base for all user commands.
     * Each subclass encapsulates one action and knows how to execute itself.
     */
    public abstract static class Command {
        /**
         * Executes this command against the given application state.
         *
         * @param tasks   the current task list
         * @param ui      the UI handler for output
         * @param storage the storage handler for persistence
         * @throws PuddingException if the command cannot complete (e.g. invalid index)
         */
        public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException;

        /**
         * Returns {@code true} if this command should end the application loop.
         * Default is {@code false}; overridden by {@link ExitCommand}.
         */
        public boolean isExit() { return false; }
    }

    /** Command that prints the farewell message and terminates the loop. */
    public static class ExitCommand extends Command {
        /** Prints the farewell message via {@code ui}. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showBye();
        }
        /** @return {@code true} always, signalling the app should exit */
        @Override
        public boolean isExit() { return true; }
    }

    /** Command that displays all tasks currently in the list. */
    public static class ListCommand extends Command {
        /** Prints every task in {@code tasks} via {@code ui}. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showTaskList(tasks);
        }
    }

    /** Command that adds a new task to the list and persists the change. */
    public static class AddCommand extends Command {
        private final Task task;
        /**
         * @param task the task to be added
         */
        public AddCommand(Task task) { this.task = task; }
        /** Adds {@code task} to the list, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.add(task);
            storage.save(tasks);
            ui.showTaskAdded(tasks.get(tasks.size()), tasks.size());
        }
    }

    /** Command that removes a task from the list by 1-based index. */
    public static class DeleteCommand extends Command {
        private final int index;
        /**
         * @param index 1-based position of the task to delete
         */
        public DeleteCommand(int index) { this.index = index; }
        /** Removes the task at {@code index}, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            Task removed = tasks.remove(index);
            storage.save(tasks);
            ui.showTaskDeleted(removed, tasks.size());
        }
    }

    /** Command that marks a task as done by 1-based index. */
    public static class MarkCommand extends Command {
        private final int index;
        /**
         * @param index 1-based position of the task to mark as done
         */
        public MarkCommand(int index) { this.index = index; }
        /** Sets the task at {@code index} as done, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.get(index).isDone = true;
            storage.save(tasks);
            ui.showMarked(tasks.get(index));
        }
    }

    /** Command that marks a task as not done by 1-based index. */
    public static class UnmarkCommand extends Command {
        private final int index;
        /**
         * @param index 1-based position of the task to mark as not done
         */
        public UnmarkCommand(int index) { this.index = index; }
        /** Sets the task at {@code index} as not done, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.get(index).isDone = false;
            storage.save(tasks);
            ui.showUnmarked(tasks.get(index));
        }
    }

    /** Command that searches all tasks for a keyword match in their description. */
    public static class FindCommand extends Command {
        private final String keyword;
        /**
         * @param keyword the search term (case-insensitive)
         */
        public FindCommand(String keyword) { this.keyword = keyword; }
        /** Delegates to {@link Ui#showMatchingTasks} to display all matching tasks. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showMatchingTasks(tasks, keyword);
        }
    }

    /**
     * Parses raw user input into the appropriate {@link Command}.
     */
    public static class Parser {

        /**
         * Parses a raw input string and returns the corresponding {@link Command}.
         *
         * @param input the full line typed by the user
         * @return the matching {@link Command} object
         * @throws PuddingException if the input is malformed or unrecognised
         */
        public static Command parse(String input) throws PuddingException {
            String trimmed = input.trim();
            if (trimmed.equals("bye")) {
                return new ExitCommand();
            }
            if (trimmed.equals("list")) {
                return new ListCommand();
            }
            if (trimmed.startsWith("mark ")) {
                try {
                    int index = Integer.parseInt(trimmed.substring(5).trim());
                    return new MarkCommand(index);
                } catch (NumberFormatException e) {
                    throw new PuddingException("Please specify a valid task number.\nCorrect format: mark [number]");
                }
            }
            if (trimmed.startsWith("unmark ")) {
                try {
                    int index = Integer.parseInt(trimmed.substring(7).trim());
                    return new UnmarkCommand(index);
                } catch (NumberFormatException e) {
                    throw new PuddingException("Please specify a valid task number.\nCorrect format: unmark [number]");
                }
            }
            if (trimmed.startsWith("todo")) {
                String desc = trimmed.substring(4).trim();
                if (desc.isEmpty()) {
                    throw new PuddingException("The description of a todo cannot be empty.\nCorrect format: todo [task name]");
                }
                return new AddCommand(new Todo(desc));
            }
            if (trimmed.startsWith("deadline")) {
                String rest = trimmed.substring(8).trim();
                int byIdx = rest.indexOf("/by");
                if (byIdx < 0) {
                    throw new PuddingException("A deadline requires a '/by' parameter.\nCorrect format: deadline [task] /by [date]");
                }
                String desc = rest.substring(0, byIdx).trim();
                String by = rest.substring(byIdx + 3).trim();
                if (desc.isEmpty()) {
                    throw new PuddingException("The description of a deadline cannot be empty.");
                }
                if (by.isEmpty()) {
                    throw new PuddingException("The date for '/by' cannot be empty.\nUse format: yyyy-MM-dd (e.g. 2019-12-02)");
                }
                try {
                    return new AddCommand(new Deadline(desc, by));
                } catch (IllegalArgumentException e) {
                    throw new PuddingException(e.getMessage());
                }
            }
            if (trimmed.startsWith("event")) {
                String rest = trimmed.substring(5).trim();
                int fromIdx = rest.indexOf("/from");
                int toIdx = rest.indexOf("/to");
                if (fromIdx < 0 || toIdx < 0) {
                    throw new PuddingException("An event requires both '/from' and '/to' parameters.\nCorrect format: event [task] /from [date] /to [date]");
                }
                String desc = rest.substring(0, fromIdx).trim();
                String from = rest.substring(fromIdx + 5, toIdx).trim();
                String to = rest.substring(toIdx + 3).trim();
                if (desc.isEmpty()) {
                    throw new PuddingException("The description of an event cannot be empty.");
                }
                if (from.isEmpty() || to.isEmpty()) {
                    throw new PuddingException("The dates for '/from' and '/to' cannot be empty.\nUse format: yyyy-MM-dd (e.g. 2019-12-02)");
                }
                try {
                    return new AddCommand(new Events(desc, from, to));
                } catch (IllegalArgumentException e) {
                    throw new PuddingException(e.getMessage());
                }
            }
            if (trimmed.startsWith("find")) {
                String keyword = trimmed.substring(4).trim();
                if (keyword.isEmpty()) {
                    throw new PuddingException("Please specify a search keyword.\nCorrect format: find [keyword]");
                }
                return new FindCommand(keyword);
            }
            if (trimmed.startsWith("delete")) {
                String[] subparts = trimmed.split(" ");
                if (subparts.length < 2) {
                    throw new PuddingException("Please specify a task number.\nCorrect format: delete [number]");
                }
                try {
                    return new DeleteCommand(Integer.parseInt(subparts[1]));
                } catch (NumberFormatException e) {
                    throw new PuddingException("Please specify a valid task number.\nCorrect format: delete [number]");
                }
            }
            String errMsg = getValidationMessage(trimmed);
            throw new PuddingException(errMsg != null ? errMsg : "I'm sorry, but I don't recognize the command '" + trimmed.split(" ")[0] + "'.\nValid commands are: todo, deadline, event, list, mark, unmark, delete, bye");
        }

        /**
         * Returns a human-readable error message for common input mistakes,
         * or {@code null} if no specific message is applicable.
         *
         * @param input the raw user input to validate
         * @return an error string, or {@code null}
         */
        public static String getValidationMessage(String input) {
            String trimmed = input.trim();
            String[] words = trimmed.split(" ", 2);
            String command = words[0].toLowerCase();
            switch (command) {
                case "todo":
                    if (words.length < 2 || words[1].trim().isEmpty()) {
                        return "The description of a todo cannot be empty.\nCorrect format: todo [task name]";
                    }
                    break;
                case "deadline":
                    if (!trimmed.contains("/by")) {
                        return "A deadline requires a '/by' parameter to specify the time.\nCorrect format: deadline [task] /by [time]";
                    }
                    break;
                case "event":
                    if (!trimmed.contains("/from") || !trimmed.contains("/to")) {
                        return "An event requires both '/from' and '/to' parameters.\nCorrect format: event [task] /from [start] /to [end]";
                    }
                    break;
                case "mark":
                case "unmark":
                    if (words.length < 2 || words[1].trim().isEmpty()) {
                        return "Please specify the task number you wish to " + command + ".\nCorrect format: " + command + " [number]";
                    }
                    break;
                case "list":
                case "bye":
                    return null;
                default:
                    return "I'm sorry, but I don't recognize the command '" + command + "'.\nValid commands are: todo, deadline, event, list, mark, unmark, bye";
            }
            return null;
        }
    }

    /**
     * Handles all user-facing input and output for the application.
     */
    public static class Ui {
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

    /**
     * Represents a generic task with a description and a completion status.
     */
    public static class Task {
        protected String description;
        protected boolean isDone;

        /**
         * Creates a new incomplete task with the given description.
         *
         * @param description the task description
         */
        public Task(String description) {
            this.description = description;
            this.isDone = false;
        }
        /** @return the description of this task */
        public String toString() {
            return description;
        }

        /** @return {@code "X"} if done, {@code " "} otherwise */
        public String getStatusIcon() {
            return (isDone ? "X" : " ");
        }

    }

    /** Represents a todo task with no time constraint. */
    public static class Todo extends Task{

        /**
         * @param description the task description
         */
        public Todo(String description) {
            super(description);
        }

        /** @return string representation prefixed with {@code [T]} */
        @Override
        public String toString() {
            return "[T]" +"["+ getStatusIcon()+"] "+ super.toString();
        }
    }

    /** Represents a task that must be completed by a specific date. */
    public static class Deadline extends Task {

        protected LocalDate by;

        /**
         * Creates a Deadline by parsing {@code byStr} as a date.
         * Accepted formats: {@code yyyy-MM-dd} or {@code d/M/yyyy}.
         *
         * @param description the task description
         * @param byStr       the due-date string
         * @throws IllegalArgumentException if {@code byStr} cannot be parsed
         */
        public Deadline(String description, String byStr) {
            super(description);
            this.by = parseDate(byStr);
        }

        /**
         * Creates a Deadline with a pre-parsed date (used by {@link Storage}).
         *
         * @param description the task description
         * @param by          the due date
         */
        public Deadline(String description, LocalDate by) {
            super(description);
            this.by = by;
        }

        /**
         * Parses a date string accepting {@code yyyy-MM-dd} or {@code d/M/yyyy}.
         *
         * @param s the date string to parse
         * @return the parsed {@link LocalDate}
         * @throws IllegalArgumentException if the string does not match any accepted format
         */
        private static LocalDate parseDate(String s) {
            s = s.trim();
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e1) {
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern("d/M/yyyy"));
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Invalid date format '" + s + "'. Use yyyy-MM-dd (e.g. 2019-12-02)");
                }
            }
        }

        /** @return string representation prefixed with {@code [D]}, date shown as {@code MMM dd yyyy} */
        @Override
        public String toString() {
            String display = by.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
            return "[D]" + "[" + getStatusIcon() + "] " + super.toString() + " (by: " + display + ")";
        }
    }

    /** Represents a task that spans a time range with a start and end date. */
    public static class Events extends Task {

        protected LocalDate from, to;

        /**
         * Creates an Events task by parsing {@code fromStr} and {@code toStr} as dates.
         * Accepted formats: {@code yyyy-MM-dd} or {@code d/M/yyyy}.
         *
         * @param description the task description
         * @param fromStr     the start-date string
         * @param toStr       the end-date string
         * @throws IllegalArgumentException if either date string cannot be parsed
         */
        public Events(String description, String fromStr, String toStr) {
            super(description);
            this.from = parseDate(fromStr);
            this.to = parseDate(toStr);
        }

        /**
         * Creates an Events task with pre-parsed dates (used by {@link Storage}).
         *
         * @param description the task description
         * @param from        the start date
         * @param to          the end date
         */
        public Events(String description, LocalDate from, LocalDate to) {
            super(description);
            this.from = from;
            this.to = to;
        }

        /**
         * Parses a date string accepting {@code yyyy-MM-dd} or {@code d/M/yyyy}.
         *
         * @param s the date string to parse
         * @return the parsed {@link LocalDate}
         * @throws IllegalArgumentException if the string does not match any accepted format
         */
        private static LocalDate parseDate(String s) {
            s = s.trim();
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e1) {
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern("d/M/yyyy"));
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Invalid date format '" + s + "'. Use yyyy-MM-dd (e.g. 2019-12-02)");
                }
            }
        }

        /** @return string representation prefixed with {@code [E]}, dates shown as {@code MMM dd yyyy} */
        @Override
        public String toString() {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy");
            return "[E]" + "[" + getStatusIcon() + "] " + super.toString()
                    + " (from: " + from.format(fmt) + ", to: " + to.format(fmt) + ")";
        }
    }




    /**
     * Holds the list of tasks and provides operations to add, retrieve, remove,
     * and iterate over them using 1-based indices.
     */
    public static class TaskList {
        private final ArrayList<Task> tasks;

        /** Creates an empty task list. */
        public TaskList() {
            this.tasks = new ArrayList<>();
        }

        /**
         * Creates a task list pre-populated with the given tasks.
         *
         * @param tasks the initial list of tasks
         */
        public TaskList(ArrayList<Task> tasks) {
            this.tasks = tasks;
        }

        /**
         * Appends a task to the end of the list.
         *
         * @param task the task to add
         */
        public void add(Task task) {
            tasks.add(task);
        }

        /**
         * Returns the task at the given 1-based index.
         *
         * @param oneBasedIndex 1-based position in the list
         * @return the task at that position
         */
        public Task get(int oneBasedIndex) {
            return tasks.get(oneBasedIndex - 1);
        }

        /**
         * Removes and returns the task at the given 1-based index.
         *
         * @param oneBasedIndex 1-based position in the list
         * @return the removed task
         */
        public Task remove(int oneBasedIndex) {
            return tasks.remove(oneBasedIndex - 1);
        }

        /** @return the number of tasks in the list */
        public int size() {
            return tasks.size();
        }

        /** Removes all tasks from the list. */
        public void clear() {
            tasks.clear();
        }

        /** @return the underlying {@link ArrayList} of tasks */
        public ArrayList<Task> getTasks() {
            return tasks;
        }
    }

    /**
     * Handles loading tasks from and saving tasks to a persistent data file.
     * Tasks are stored in a pipe-delimited text format, one per line.
     */
    public static class Storage {
        private final Path filePath;

        /**
         * @param filePath path to the data file (created automatically if absent)
         */
        public Storage(String filePath) {
            this.filePath = Paths.get(filePath);
        }

        /**
         * Loads tasks from the data file.
         *
         * @return list of tasks read from disk
         * @throws PuddingException if the file cannot be read
         */
        public ArrayList<Task> load() throws PuddingException {
            try {
                ensureExists();
                ArrayList<Task> loaded = new ArrayList<>();
                for (String line : Files.readAllLines(filePath)) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        loaded.add(lineToTask(trimmed));
                    }
                }
                return loaded;
            } catch (IOException e) {
                throw new PuddingException("Could not load data: " + e.getMessage());
            }
        }

        /**
         * Saves the current task list to the data file, overwriting any previous content.
         *
         * @param tasks the task list to persist
         * @throws PuddingException if the file cannot be written
         */
        public void save(TaskList tasks) throws PuddingException {
            try {
                ensureExists();
                ArrayList<String> lines = new ArrayList<>();
                for (Task t : tasks.getTasks()) {
                    lines.add(taskToLine(t));
                }
                Files.write(filePath, lines);
            } catch (IOException e) {
                throw new PuddingException("Could not save data: " + e.getMessage());
            }
        }

        /**
         * Creates the data file (and any missing parent directories) if they do not exist.
         *
         * @throws IOException if the file or directories cannot be created
         */
        private void ensureExists() throws IOException {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        }

        /**
         * Serialises a task to a pipe-delimited string for storage.
         * Format: {@code T|D|E | 0|1 | description [| extra fields]}
         *
         * @param t the task to serialise
         * @return the formatted line
         */
        private String taskToLine(Task t) {
            if (t instanceof Deadline d) {
                return "D | " + (d.isDone ? "1" : "0") + " | " + d.description + " | " + d.by.toString();
            } else if (t instanceof Events e) {
                return "E | " + (e.isDone ? "1" : "0") + " | " + e.description
                        + " | " + e.from.toString() + " | " + e.to.toString();
            } else {
                return "T | " + (t.isDone ? "1" : "0") + " | " + t.description;
            }
        }

        /**
         * Deserialises a single pipe-delimited line from the data file into a {@link Task}.
         *
         * @param line a non-empty line from the data file
         * @return the reconstructed task
         * @throws IllegalArgumentException if the line is malformed
         */
        private Task lineToTask(String line) {
            String[] parts = line.trim().split("\\s*\\|\\s*");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Corrupted line: " + line);
            }
            String type = parts[0];
            boolean done = parts[1].equals("1");
            String desc = parts[2];
            Task task;
            switch (type) {
                case "T":
                    task = new Todo(desc);
                    break;
                case "D":
                    if (parts.length < 4) throw new IllegalArgumentException("Corrupted deadline: " + line);
                    task = new Deadline(desc, parseStoredDate(parts[3].trim()));
                    break;
                case "E":
                    if (parts.length < 5) throw new IllegalArgumentException("Corrupted event: " + line);
                    task = new Events(desc, parseStoredDate(parts[3].trim()), parseStoredDate(parts[4].trim()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown task type: " + type);
            }
            task.isDone = done;
            return task;
        }

        /**
         * Parses a stored date string (ISO {@code yyyy-MM-dd} or {@code d/M/yyyy}).
         *
         * @param s the date string from the file
         * @return the parsed {@link LocalDate}
         * @throws IllegalArgumentException if the string is not a recognised date format
         */
        private LocalDate parseStoredDate(String s) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e1) {
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ofPattern("d/M/yyyy"));
                } catch (DateTimeParseException e2) {
                    throw new IllegalArgumentException("Unrecognised date in data file: '" + s + "'");
                }
            }
        }
    }

}
