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

public class Pudding {

    private Storage storage;
    private TaskList tasks;
    private Ui ui;

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

    public static void main(String[] args) {
        new Pudding("src/main/java/Pudding/dataLog.txt").run();
    }

    public static class PuddingException extends Exception {
        public PuddingException(String message) {
            super(message);
        }
    }

    public abstract static class Command {
        public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException;
        public boolean isExit() { return false; }
    }

    public static class ExitCommand extends Command {
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showBye();
        }
        @Override
        public boolean isExit() { return true; }
    }

    public static class ListCommand extends Command {
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showTaskList(tasks);
        }
    }

    public static class AddCommand extends Command {
        private final Task task;
        public AddCommand(Task task) { this.task = task; }
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.add(task);
            storage.save(tasks);
            ui.showTaskAdded(tasks.get(tasks.size()), tasks.size());
        }
    }

    public static class DeleteCommand extends Command {
        private final int index;
        public DeleteCommand(int index) { this.index = index; }
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            Task removed = tasks.remove(index);
            storage.save(tasks);
            ui.showTaskDeleted(removed, tasks.size());
        }
    }

    public static class MarkCommand extends Command {
        private final int index;
        public MarkCommand(int index) { this.index = index; }
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.get(index).isDone = true;
            storage.save(tasks);
            ui.showMarked(tasks.get(index));
        }
    }

    public static class UnmarkCommand extends Command {
        private final int index;
        public UnmarkCommand(int index) { this.index = index; }
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.get(index).isDone = false;
            storage.save(tasks);
            ui.showUnmarked(tasks.get(index));
        }
    }

    public static class Parser {

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

    public static class Ui {
        private static final String LINE = "____________________________________________________________";
        private static final String LOGO = """
                 
        █████▄ ██  ██ ████▄  ████▄  ██ ███  ██  ▄████  
        ██▄▄█▀ ██  ██ ██  ██ ██  ██ ██ ██ ▀▄██ ██  ▄▄▄ 
        ██     ▀████▀ ████▀  ████▀  ██ ██   ██  ▀███▀  
        """;
        private final Scanner scanner;

        public Ui() {
            this.scanner = new Scanner(System.in);
        }

        public void showLine() {
            System.out.println(LINE);
        }

        public void showWelcome() {
            showLine();
            System.out.println(LOGO);
            System.out.println("Hello! I'm Pudding");
            System.out.println("What can I do for you?");
            showLine();
        }

        public void showBye() {
            System.out.println("\nBye. Hope to see you again soon!\n");
            showLine();
        }

        public void showError(String message) {
            System.out.println(message);
        }

        public void showLoadingError() {
            System.out.println("Warning: failed to load saved tasks. Starting with an empty list.");
        }

        public String readCommand() {
            return scanner.nextLine();
        }

        public void showTaskList(TaskList tasks) {
            System.out.println("Here are the tasks in your list:");
            for (int i = 1; i <= tasks.size(); i++) {
                System.out.println(i + "." + tasks.get(i).toString());
            }
        }

        public void showTaskAdded(Task task, int totalTasks) {
            System.out.println("Got it. I've added this task:");
            System.out.println("  " + task.toString());
            System.out.println("Now you have " + totalTasks + " tasks in the list.");
        }

        public void showMarked(Task task) {
            System.out.println("Nice! I've marked this task as done:");
            System.out.println("  [" + task.getStatusIcon() + "] " + task.description);
        }

        public void showUnmarked(Task task) {
            System.out.println("OK, I've marked this task as not done yet:");
            System.out.println("  [" + task.getStatusIcon() + "] " + task.description);
        }

        public void showTaskDeleted(Task task, int remainingTasks) {
            System.out.println("Noted. I've removed this task:");
            System.out.println("  " + task.toString());
            System.out.println("Now you have " + remainingTasks + " tasks in the list.");
        }
    }

    public static class Task {
        protected String description;
        protected boolean isDone;

        public Task(String description) {
            this.description = description;
            this.isDone = false;
        }
        public String toString() {
            return description;
        }

        public String getStatusIcon() {
            return (isDone ? "X" : " ");
        }

    }

    public static class Todo extends Task{

        public Todo(String description) {
            super(description);
        }

        @Override
        public String toString() {
            return "[T]" +"["+getStatusIcon()+"] "+ super.toString();
        }
    }

    public static class Deadline extends Task {

        protected LocalDate by;

        public Deadline(String description, String byStr) {
            super(description);
            this.by = parseDate(byStr);
        }

        public Deadline(String description, LocalDate by) {
            super(description);
            this.by = by;
        }

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

        @Override
        public String toString() {
            String display = by.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));
            return "[D]" + "[" + getStatusIcon() + "] " + super.toString() + " (by: " + display + ")";
        }
    }

    public static class Events extends Task {

        protected LocalDate from, to;

        public Events(String description, String fromStr, String toStr) {
            super(description);
            this.from = parseDate(fromStr);
            this.to = parseDate(toStr);
        }

        public Events(String description, LocalDate from, LocalDate to) {
            super(description);
            this.from = from;
            this.to = to;
        }

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

        @Override
        public String toString() {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy");
            return "[E]" + "[" + getStatusIcon() + "] " + super.toString()
                    + " (from: " + from.format(fmt) + ", to: " + to.format(fmt) + ")";
        }
    }




    public static class TaskList {
        private final ArrayList<Task> tasks;

        public TaskList() {
            this.tasks = new ArrayList<>();
        }

        public TaskList(ArrayList<Task> tasks) {
            this.tasks = tasks;
        }

        public void add(Task task) {
            tasks.add(task);
        }

        public Task get(int oneBasedIndex) {
            return tasks.get(oneBasedIndex - 1);
        }

        public Task remove(int oneBasedIndex) {
            return tasks.remove(oneBasedIndex - 1);
        }

        public int size() {
            return tasks.size();
        }

        public void clear() {
            tasks.clear();
        }

        public ArrayList<Task> getTasks() {
            return tasks;
        }
    }

    public static class Storage {
        private final Path filePath;

        public Storage(String filePath) {
            this.filePath = Paths.get(filePath);
        }

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

        private void ensureExists() throws IOException {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        }

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
