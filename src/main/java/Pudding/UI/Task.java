package pudding.ui;

/**
 * Represents a generic task with a description and a completion status.
 */
public class Task {

    protected String description;
    protected boolean isDone;

    /**
     * Creates a new incomplete task with the given description.
     *
     * @param description the task description
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /** @return the description of this task */
    @Override
    public String toString() {
        return description;
    }

    /** @return {@code "X"} if done, {@code " "} otherwise */
    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }
}
