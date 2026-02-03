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
                System.out.println("Here are the tasks in your list:");
                for (int i = 0; i < list.size(); i++) {
                    System.out.println((i+1) + ".[" +list.get(i).getStatusIcon()+"] "+list.get(i).description);
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

    public static class Task {
        protected String description;
        protected boolean isDone;

        public Task(String description) {
            this.description = description;
            this.isDone = false;
        }

        public String getStatusIcon() {
            return (isDone ? "X" : " ");
        }

    }

}