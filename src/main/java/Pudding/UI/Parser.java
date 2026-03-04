package Pudding.UI;

/**
 * Parses raw user input into the appropriate {@link Command}.
 */
public class Parser {

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
            return new Command.ExitCommand();
        }
        if (trimmed.equals("list")) {
            return new Command.ListCommand();
        }
        if (trimmed.startsWith("mark ")) {
            try {
                int index = Integer.parseInt(trimmed.substring(5).trim());
                return new Command.MarkCommand(index);
            } catch (NumberFormatException e) {
                throw new PuddingException("Please specify a valid task number.\nCorrect format: mark [number]");
            }
        }
        if (trimmed.startsWith("unmark ")) {
            try {
                int index = Integer.parseInt(trimmed.substring(7).trim());
                return new Command.UnmarkCommand(index);
            } catch (NumberFormatException e) {
                throw new PuddingException("Please specify a valid task number.\nCorrect format: unmark [number]");
            }
        }
        if (trimmed.startsWith("todo")) {
            String desc = trimmed.substring(4).trim();
            if (desc.isEmpty()) {
                throw new PuddingException("The description of a todo cannot be empty.\nCorrect format: todo [task name]");
            }
            return new Command.AddCommand(new Todo(desc));
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
                return new Command.AddCommand(new Deadline(desc, by));
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
                return new Command.AddCommand(new Event(desc, from, to));
            } catch (IllegalArgumentException e) {
                throw new PuddingException(e.getMessage());
            }
        }
        if (trimmed.startsWith("find")) {
            String keyword = trimmed.substring(4).trim();
            if (keyword.isEmpty()) {
                throw new PuddingException("Please specify a search keyword.\nCorrect format: find [keyword]");
            }
            return new Command.FindCommand(keyword);
        }
        if (trimmed.startsWith("delete")) {
            String[] subparts = trimmed.split(" ");
            if (subparts.length < 2) {
                throw new PuddingException("Please specify a task number.\nCorrect format: delete [number]");
            }
            try {
                return new Command.DeleteCommand(Integer.parseInt(subparts[1]));
            } catch (NumberFormatException e) {
                throw new PuddingException("Please specify a valid task number.\nCorrect format: delete [number]");
            }
        }
        String errMsg = getValidationMessage(trimmed);
        throw new PuddingException(errMsg != null ? errMsg
                : "I'm sorry, but I don't recognize the command '" + trimmed.split(" ")[0]
                + "'.\nValid commands are: todo, deadline, event, list, mark, unmark, delete, bye");
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
            // Fallthrough
        case "unmark":
            if (words.length < 2 || words[1].trim().isEmpty()) {
                return "Please specify the task number you wish to " + command
                        + ".\nCorrect format: " + command + " [number]";
            }
            break;
        case "list":
            // Fallthrough
        case "bye":
            return null;
        default:
            return "I'm sorry, but I don't recognize the command '" + command
                    + "'.\nValid commands are: todo, deadline, event, list, mark, unmark, bye";
        }
        return null;
    }
}
