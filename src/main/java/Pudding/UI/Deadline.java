package pudding.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Represents a task that must be completed by a specific date. */
public class Deadline extends Task {

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
