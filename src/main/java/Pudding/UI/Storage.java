package Pudding.UI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Handles loading tasks from and saving tasks to a persistent data file.
 * Tasks are stored in a pipe-delimited text format, one per line.
 */
public class Storage {

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
        } else if (t instanceof Event e) {
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
                task = new Event(desc, parseStoredDate(parts[3].trim()), parseStoredDate(parts[4].trim()));
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
