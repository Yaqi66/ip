package Pudding.UI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Pudding {

    public static void main(String[] args) {
        Ui ui = new Ui();
        Storage storage = new Storage("src/main/java/Pudding/dataLog.txt");
        ArrayList<Task> list = new ArrayList<>();

        try {
            storage.load(list);
        } catch (IOException e) {
            ui.showLoadingError();
        }

        ui.showWelcome();
        String input = ui.readCommand();

        while (!input.equals("bye")) {
            try {
                Parser.ParsedCommand cmd = Parser.parse(input);
                switch (cmd.type) {
                    case LIST:
                        ui.showLine();
                        ui.showTaskList(list);
                        break;
                    case MARK:
                        if (0 < cmd.index && cmd.index <= list.size()) {
                            list.get(cmd.index - 1).isDone = true;
                            storage.save(list);
                            ui.showMarked(list.get(cmd.index - 1));
                        }
                        break;
                    case UNMARK:
                        if (0 < cmd.index && cmd.index <= list.size()) {
                            list.get(cmd.index - 1).isDone = false;
                            storage.save(list);
                            ui.showUnmarked(list.get(cmd.index - 1));
                        }
                        break;
                    case ADD:
                        list.add(cmd.task);
                        storage.save(list);
                        ui.showTaskAdded(list.get(list.size() - 1), list.size());
                        break;
                    case DELETE:
                        Task removed = list.get(cmd.index - 1);
                        list.remove(cmd.index - 1);
                        storage.save(list);
                        ui.showTaskDeleted(removed, list.size());
                        break;
                    case UNKNOWN:
                        ui.showError(cmd.errorMessage);
                        break;
                }
                ui.showLine();
            } catch (Exception e) {
                ui.showError(Parser.getValidationMessage(input));
                ui.showLine();
            }
            input = ui.readCommand();
        }
        ui.showBye();
    }

    public static class Parser {

        enum CommandType { LIST, MARK, UNMARK, ADD, DELETE, UNKNOWN }

        static class ParsedCommand {
            CommandType type;
            int index;
            Task task;
            String errorMessage;

            ParsedCommand(CommandType type) { this.type = type; }
        }

        public static ParsedCommand parse(String input) {
            if (input.equals("list")) {
                return new ParsedCommand(CommandType.LIST);
            }
            if (input.startsWith("mark")) {
                ParsedCommand cmd = new ParsedCommand(CommandType.MARK);
                cmd.index = Integer.parseInt(input.substring(5).trim());
                return cmd;
            }
            if (input.startsWith("unmark")) {
                ParsedCommand cmd = new ParsedCommand(CommandType.UNMARK);
                cmd.index = Integer.parseInt(input.substring(7).trim());
                return cmd;
            }
            if (input.startsWith("todo")) {
                String[] subparts = input.split(" ");
                ParsedCommand cmd = new ParsedCommand(CommandType.ADD);
                cmd.task = new Todo(combineStr(subparts));
                return cmd;
            }
            if (input.startsWith("deadline")) {
                String[] parts = input.split("/");
                String[] subparts = parts[0].split(" ");
                ParsedCommand cmd = new ParsedCommand(CommandType.ADD);
                cmd.task = new Deadline(combineStr(subparts), parts[1].replace("by ", ""));
                return cmd;
            }
            if (input.startsWith("event")) {
                String[] parts = input.split("/");
                String[] subparts = parts[0].split(" ");
                ParsedCommand cmd = new ParsedCommand(CommandType.ADD);
                cmd.task = new Events(combineStr(subparts), parts[1].replace("from ", ""),
                        parts[2].replace("to ", ""));
                return cmd;
            }
            if (input.startsWith("delete")) {
                String[] subparts = input.split(" ");
                ParsedCommand cmd = new ParsedCommand(CommandType.DELETE);
                cmd.index = Integer.parseInt(subparts[1]);
                return cmd;
            }
            ParsedCommand cmd = new ParsedCommand(CommandType.UNKNOWN);
            cmd.errorMessage = getValidationMessage(input);
            return cmd;
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

        public void showTaskList(ArrayList<Task> list) {
            System.out.println("Here are the tasks in your list:");
            for (int i = 0; i < list.size(); i++) {
                System.out.println((i + 1) + "." + list.get(i).toString());
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




    public static class Storage {
        private final Path filePath;

        public Storage(String filePath) {
            this.filePath = Paths.get(filePath);
        }

        public void load(ArrayList<Task> list) throws IOException {
            ensureExists();
            list.clear();
            for (String line : Files.readAllLines(filePath)) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    list.add(lineToTask(trimmed));
                }
            }
        }

        public void save(ArrayList<Task> list) throws IOException {
            ensureExists();
            ArrayList<String> lines = new ArrayList<>();
            for (Task t : list) {
                lines.add(taskToLine(t));
            }
            Files.write(filePath, lines);
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
