package pudding.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Represents a task that spans a time range with a start and end date. */
public class Event extends Task {

    protected LocalDate from;
    protected LocalDate to;

    /**
     * Creates an Event by parsing {@code fromStr} and {@code toStr} as dates.
     * Accepted formats: {@code yyyy-MM-dd} or {@code d/M/yyyy}.
     *
     * @param description the task description
     * @param fromStr     the start-date string
     * @param toStr       the end-date string
     * @throws IllegalArgumentException if either date string cannot be parsed
     */
    public Event(String description, String fromStr, String toStr) {
        super(description);
        this.from = parseDate(fromStr);
        this.to = parseDate(toStr);
    }

    /**
     * Creates an Event with pre-parsed dates (used by {@link Storage}).
     *
     * @param description the task description
     * @param from        the start date
     * @param to          the end date
     */
    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
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

    /** @return string representation prefixed with {@code [E]}, dates shown as {@code MMM dd yyyy} */
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy");
        return "[E]" + "[" + getStatusIcon() + "] " + super.toString()
                + " (from: " + from.format(fmt) + ", to: " + to.format(fmt) + ")";
    }
}
