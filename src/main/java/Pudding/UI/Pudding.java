package Pudding.UI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Pudding {
    private static final Path DATA_DIR =
            Paths.get("src", "main", "java", "Pudding");

    private static final Path DATA_LOG_FILE =
            DATA_DIR.resolve("dataLog.txt");


    public static void main(String[] args) {
        String logo = """                                               
             
        █████▄ ██  ██ ████▄  ████▄  ██ ███  ██  ▄████  
        ██▄▄█▀ ██  ██ ██  ██ ██  ██ ██ ██ ▀▄██ ██  ▄▄▄ 
        ██     ▀████▀ ████▀  ████▀  ██ ██   ██  ▀███▀  
                                                    """;
        final String LINE = "____________________________________________________________";
        System.out.println(LINE);
        System.out.println(logo);
        System.out.println("Hello! I'm Pudding");
        System.out.println("What can I do for you?");
        System.out.println(LINE);
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        ArrayList<Task> list = new ArrayList<>();

        try {
            ensureDataLogExists();      // <-- FORCE file + folder creation
            readDataFromLogFile(list);  // <-- then load data
        } catch (IOException e) {
            e.printStackTrace();        // show real error instead of "wrong"
        }




        while(!input.equals("bye")) {
            try{
                if(input.equals("list")) {
                    System.out.println(LINE);
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < list.size(); i++) {
                        System.out.println((i+1) + "." +list.get(i).toString());
                    }
                }
                else {
                    if (input.startsWith("mark")) {
                        int index = Integer.parseInt(input.substring(5));
                        if(0<index && index<=list.size()) {
                            System.out.println("Nice! I've marked this task as done:");
                            list.get(index - 1).isDone = true;

                            logListToDataLog(list);

                            System.out.println((" [" +list.get(index-1).getStatusIcon()+"] "+list.get(index-1).description));
                        }
                    }
                    else if (input.startsWith("unmark")) {
                        int index = Integer.parseInt(input.substring(7));
                        if(0<index && index<=list.size()) {
                            System.out.println("OK, I've marked this task as not done yet:");
                            list.get(index - 1).isDone = false;
                            logListToDataLog(list);
                            System.out.println((" [" +list.get(index-1).getStatusIcon()+"] "+list.get(index-1).description));
                        }
                    }
                    else if (input.startsWith("todo")) {
                        String[] subparts = input.split(" ");
                        list.add(new Todo(combineStr(subparts)));
                        logListToDataLog(list);
                        System.out.println(replyRoutine(list.get(list.size()-1), list.size()));
                    }
                    else if (input.startsWith("deadline")) {
                        String[] parts = input.split("/");
                        String[] subparts = parts[0].split(" ");
                        list.add(new Deadline(combineStr(subparts),parts[1].replace("by ","")));
                        logListToDataLog(list);
                        System.out.println(replyRoutine(list.get(list.size()-1), list.size()));
                    }
                    else if (input.startsWith("event")) {
                        String[] parts = input.split("/");
                        String[] subparts = parts[0].split(" ");
                        list.add(new Events(combineStr(subparts),parts[1].replace("from ", ""),
                                parts[2].replace("to ", "")));
                        logListToDataLog(list);
                        System.out.println(replyRoutine(list.get(list.size()-1), list.size()));
                        replyRoutine(list.get(list.size()-1), list.size());
                    }
                    else if (input.startsWith("delete")) {
                        String[] subparts = input.split(" ");
                        int index = Integer.parseInt(subparts[1]);
                        System.out.println("Noted. I've removed this task:");
                        System.out.println(list.get(index));
                        System.out.println("Now you have "+(list.size()-1)+" tasks in the list.");
                        list.remove(Integer.parseInt(subparts[1]));
                        logListToDataLog(list);
                    }
                    else{
                        System.out.println(getValidationMessage(input));
                    }
                }
                System.out.println(LINE);
            }
            catch(Exception e) {
                System.out.println(getValidationMessage(input));
                System.out.println(LINE);
            }
            finally {
                input = sc.nextLine();
                continue;
            }

        }
        System.out.println("\nBye. Hope to see you again soon!\n");
        System.out.println(LINE);
    }
    public static String combineStr(String[] strs){
        String result = "";
        for(int i=1;i<strs.length;i++){
            result += strs[i];
        }
        return result;
    }
    public static String replyRoutine(Task task, int n){
        String result = "";
        String LINE = "____________________________________________________________";
        result += LINE+"\nGot it. I've added this task:\n" +task.toString() +"\nNow you have "+n+" tasks in the list.";
        return result;
    }


    public static String getValidationMessage(String input) {
        String trimmed = input.trim();
        String[] words = trimmed.split(" ", 2);
        String command = words[0].toLowerCase();

        switch (command) {
            case "todo":
                if (words.length < 2 || words[1].trim().isEmpty()) {
                    return "The description of a todo cannot be empty.\n" +
                            "Correct format: todo [task name]";
                }
                break;

            case "deadline":
                if (!trimmed.contains("/by")) {
                    return "A deadline requires a '/by' parameter to specify the time.\n" +
                            "Correct format: deadline [task] /by [time]";
                }
                break;

            case "event":
                if (!trimmed.contains("/from") || !trimmed.contains("/to")) {
                    return "An event requires both '/from' and '/to' parameters.\n" +
                            "Correct format: event [task] /from [start] /to [end]";
                }
                break;

            case "mark":
            case "unmark":
                if (words.length < 2 || words[1].trim().isEmpty()) {
                    return "Please specify the task number you wish to " + command + ".\n" +
                            "Correct format: " + command + " [number]";
                }
                break;

            case "list":
            case "bye":
                return null;

            default:
                return "I'm sorry, but I don't recognize the command '" + command + "'.\n" +
                        "Valid commands are: todo, deadline, event, list, mark, unmark, bye";
        }

        return null;
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




    private static void ensureDataLogExists() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
        if (!Files.exists(DATA_LOG_FILE)) {
            Files.createFile(DATA_LOG_FILE);
        }
    }

    private static String taskToLogLine(Task t) {
        if (t instanceof Deadline d) {
            return "D | " + (d.isDone ? "1" : "0") + " | " + d.description + " | " + d.by;
        } else if (t instanceof Events e) {
            return "E | " + (e.isDone ? "1" : "0") + " | " + e.description + " | " + e.from + " | " + e.to;
        } else {
            return "T | " + (t.isDone ? "1" : "0") + " | " + t.description;
        }
    }

    private static void logListToDataLog(ArrayList<Task> list) throws IOException {
        ensureDataLogExists();

        ArrayList<String> lines = new ArrayList<>();
        for (Task t : list) {
            lines.add(taskToLogLine(t));
        }
        Files.write(DATA_LOG_FILE, lines);
    }

    private static Task logLineToTask(String line) {
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


    private static void readDataFromLogFile(ArrayList<Task> list) throws IOException {
        ensureDataLogExists();

        list.clear();
        ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(DATA_LOG_FILE);

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            list.add(logLineToTask(trimmed));
        }
    }







}

//event project meeting /from Mon 2pm /to 4pm
//event attend CS2113 lecture Friday 20 Feb 2026 /from 4 /to 6pm
//deadline yaqi submit UG draft /by today 2359