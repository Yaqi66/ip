package pudding.ui;

/**
 * Abstract base for all user commands.
 * Each subclass encapsulates one action and knows how to execute itself.
 */
public abstract class Command {

    /**
     * Executes this command against the given application state.
     *
     * @param tasks   the current task list
     * @param ui      the UI handler for output
     * @param storage the storage handler for persistence
     * @throws PuddingException if the command cannot complete (e.g. invalid index)
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException;

    /**
     * Returns {@code true} if this command should end the application loop.
     * Default is {@code false}; overridden by {@link ExitCommand}.
     */
    public boolean isExit() {
        return false;
    }

    // -------------------------------------------------------------------------
    // Concrete command implementations
    // -------------------------------------------------------------------------

    /** Command that prints the farewell message and terminates the loop. */
    public static class ExitCommand extends Command {
        /** Prints the farewell message via {@code ui}. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showBye();
        }
        /** @return {@code true} always, signalling the app should exit */
        @Override
        public boolean isExit() {
            return true;
        }
    }

    /** Command that displays all tasks currently in the list. */
    public static class ListCommand extends Command {
        /** Prints every task in {@code tasks} via {@code ui}. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showTaskList(tasks);
        }
    }

    /** Command that adds a new task to the list and persists the change. */
    public static class AddCommand extends Command {
        private final Task task;
        /** @param task the task to be added */
        public AddCommand(Task task) {
            this.task = task;
        }
        /** Adds {@code task} to the list, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.add(task);
            storage.save(tasks);
            ui.showTaskAdded(tasks.get(tasks.size()), tasks.size());
        }
    }

    /** Command that removes a task from the list by 1-based index. */
    public static class DeleteCommand extends Command {
        private final int index;
        /** @param index 1-based position of the task to delete */
        public DeleteCommand(int index) {
            this.index = index;
        }
        /** Removes the task at {@code index}, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            Task removed = tasks.remove(index);
            storage.save(tasks);
            ui.showTaskDeleted(removed, tasks.size());
        }
    }

    /** Command that marks a task as done by 1-based index. */
    public static class MarkCommand extends Command {
        private final int index;
        /** @param index 1-based position of the task to mark as done */
        public MarkCommand(int index) {
            this.index = index;
        }
        /** Sets the task at {@code index} as done, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.get(index).isDone = true;
            storage.save(tasks);
            ui.showMarked(tasks.get(index));
        }
    }

    /** Command that marks a task as not done by 1-based index. */
    public static class UnmarkCommand extends Command {
        private final int index;
        /** @param index 1-based position of the task to mark as not done */
        public UnmarkCommand(int index) {
            this.index = index;
        }
        /** Sets the task at {@code index} as not done, saves, and confirms to the user. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) throws PuddingException {
            tasks.get(index).isDone = false;
            storage.save(tasks);
            ui.showUnmarked(tasks.get(index));
        }
    }

    /** Command that searches all tasks for a keyword match in their description. */
    public static class FindCommand extends Command {
        private final String keyword;
        /** @param keyword the search term (case-insensitive) */
        public FindCommand(String keyword) {
            this.keyword = keyword;
        }
        /** Delegates to {@link Ui#showMatchingTasks} to display all matching tasks. */
        @Override
        public void execute(TaskList tasks, Ui ui, Storage storage) {
            ui.showMatchingTasks(tasks, keyword);
        }
    }
}
