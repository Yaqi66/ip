package Pudding.UI;

/** Represents a todo task with no time constraint. */
public class Todo extends Task {

    /**
     * @param description the task description
     */
    public Todo(String description) {
        super(description);
    }

    /** @return string representation prefixed with {@code [T]} */
    @Override
    public String toString() {
        return "[T]" + "[" + getStatusIcon() + "] " + super.toString();
    }
}
