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
        ArrayList list = new ArrayList();
        while(!input.equals("bye")) {
            if(input.equals("list")) {
                for (int i = 0; i < list.size(); i++) {
                    System.out.println((i+1) + ". " +list.get(i));
                }
            }
            else {
                System.out.println("added: "+input);
                list.add(input);
            }
            System.out.println(line);
            input = sc.nextLine();
        }
        System.out.println("\nBye. Hope to see you again soon!\n");
        System.out.println(line);
    }
}