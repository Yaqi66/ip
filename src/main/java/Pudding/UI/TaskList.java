package Pudding.UI;

import java.util.ArrayList;

/**
 * Holds the list of tasks and provides operations to add, retrieve, remove,
 * and iterate over them using 1-based indices.
 */
public class TaskList {

    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list pre-populated with the given tasks.
     *
     * @param tasks the initial list of tasks
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Appends a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task at the given 1-based index.
     *
     * @param oneBasedIndex 1-based position in the list
     * @return the task at that position
     */
    public Task get(int oneBasedIndex) {
        return tasks.get(oneBasedIndex - 1);
    }

    /**
     * Removes and returns the task at the given 1-based index.
     *
     * @param oneBasedIndex 1-based position in the list
     * @return the removed task
     */
    public Task remove(int oneBasedIndex) {
        return tasks.remove(oneBasedIndex - 1);
    }

    /** @return the number of tasks in the list */
    public int size() {
        return tasks.size();
    }

    /** Removes all tasks from the list. */
    public void clear() {
        tasks.clear();
    }

    /** @return the underlying {@link ArrayList} of tasks */
    public ArrayList<Task> getTasks() {
        return tasks;
    }
}
