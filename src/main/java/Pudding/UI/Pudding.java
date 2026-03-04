package Pudding.UI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            if (input.equals("bye")) {
                return new ExitCommand();
            }
            if (input.equals("list")) {
                return new ListCommand();
            }
            if (input.startsWith("mark")) {
                int index = Integer.parseInt(input.substring(5).trim());
                return new MarkCommand(index);
            }
            if (input.startsWith("unmark")) {
                int index = Integer.parseInt(input.substring(7).trim());
                return new UnmarkCommand(index);
            }
            if (input.startsWith("todo")) {
                String[] subparts = input.split(" ");
                return new AddCommand(new Todo(combineStr(subparts)));
            }
            if (input.startsWith("deadline")) {
                String[] parts = input.split("/");
                String[] subparts = parts[0].split(" ");
                return new AddCommand(new Deadline(combineStr(subparts), parts[1].replace("by ", "")));
            }
            if (input.startsWith("event")) {
                String[] parts = input.split("/");
                String[] subparts = parts[0].split(" ");
                return new AddCommand(new Events(combineStr(subparts), parts[1].replace("from ", ""),
                        parts[2].replace("to ", "")));
            }
            if (input.startsWith("delete")) {
                String[] subparts = input.split(" ");
                return new DeleteCommand(Integer.parseInt(subparts[1]));
            }
            String errMsg = getValidationMessage(input);
            throw new PuddingException(errMsg != null ? errMsg : "Unknown command: " + input);
        }

        public static String combineStr(String[] strs) {
            String result = "";
            for (int i = 1; i < strs.length; i++) {
                result += strs[i];
            }
            return result;
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

        protected String by;

        public Deadline(String description, String by) {
            super(description);
            this.by = by;
        }

        @Override
        public String toString() {
            return "[D]" +"["+getStatusIcon()+"] "+ super.toString() + " (by: " + by + ")";
        }
    }

    public static class Events extends Task {

        protected String from, to;

        public Events(String description, String from, String to) {
            super(description);
            this.to = to;
            this.from = from;
        }

        @Override
        public String toString() {
            return "[E]" +"["+getStatusIcon()+"] "+ super.toString() + " (from: " + from + ", to: " + to + ")";
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
                return "D | " + (d.isDone ? "1" : "0") + " | " + d.description + " | " + d.by;
            } else if (t instanceof Events e) {
                return "E | " + (e.isDone ? "1" : "0") + " | " + e.description + " | " + e.from + " | " + e.to;
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
                    task = new Deadline(desc, parts[3]);
                    break;
                case "E":
                    if (parts.length < 5) throw new IllegalArgumentException("Corrupted event: " + line);
                    task = new Events(desc, parts[3], parts[4]);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown task type: " + type);
            }
            task.isDone = done;
            return task;
        }
    }

}
