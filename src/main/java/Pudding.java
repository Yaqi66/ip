import java.util.ArrayList;
import java.util.Scanner;
public class Pudding {
    public static void main(String[] args) {
        String logo = """                                               
             
        █████▄ ██  ██ ████▄  ████▄  ██ ███  ██  ▄████  
        ██▄▄█▀ ██  ██ ██  ██ ██  ██ ██ ██ ▀▄██ ██  ▄▄▄ 
        ██     ▀████▀ ████▀  ████▀  ██ ██   ██  ▀███▀  
                                                    """;
        String line = "____________________________________________________________";
        System.out.println(line);
        System.out.println(logo);
        System.out.println("Hello! I'm Pudding");
        System.out.println("What can I do for you?");
        System.out.println(line);
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();
        ArrayList<Task> list = new ArrayList<>();
        while(!input.equals("bye")) {
            if(input.equals("list")) {
                System.out.println(line);
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
                        System.out.println((" [" +list.get(index-1).getStatusIcon()+"] "+list.get(index-1).description));
                    }
                }
                else if (input.startsWith("unmark")) {
                    int index = Integer.parseInt(input.substring(7));
                    if(0<index && index<=list.size()) {
                        System.out.println("OK, I've marked this task as not done yet:");
                        list.get(index - 1).isDone = false;
                        System.out.println((" [" +list.get(index-1).getStatusIcon()+"] "+list.get(index-1).description));
                    }
                }
                else if (input.startsWith("todo")) {
                    String[] Subparts = input.split(" ");
                    list.add(new Todo(combineStr(Subparts)));
                    System.out.println(replyRoutine(list.get(list.size()-1), list.size()));
                }
                else if (input.startsWith("deadline")) {
                    String[] parts = input.split("/");
                    String[] Subparts = parts[0].split(" ");
                    list.add(new Deadline(combineStr(Subparts),parts[1].replace("by ","")));
                    System.out.println(replyRoutine(list.get(list.size()-1), list.size()));
                }
                else if (input.startsWith("event")) {
                    String[] parts = input.split("/");
                    String[] Subparts = parts[0].split(" ");
                    list.add(new Events(combineStr(Subparts),parts[1].replace("from ", ""),
                            parts[2].replace("to ", "")));
                    System.out.println(replyRoutine(list.get(list.size()-1), list.size()));
                    replyRoutine(list.get(list.size()-1), list.size());
                }
                else{
                    System.out.println("added: "+input);
                    list.add(new Task(input));
                }
            }
            System.out.println(line);
            input = sc.nextLine();
        }
        System.out.println("\nBye. Hope to see you again soon!\n");
        System.out.println(line);
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
        String line = "____________________________________________________________";
        result += line+"\nGot it. I've added this task:\n" +task.toString() +"\nNow you have "+n+" tasks in the list.";
        return result;
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


}