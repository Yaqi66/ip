package Pudding.UI;

/**
 * Main class for the Pudding chatbot application.
 * Owns the top-level {@link Storage}, {@link TaskList}, and {@link Ui} instances
 * and drives the main command loop.
 */
public class Pudding {

    private Storage storage;
    private TaskList tasks;
    private Ui ui;

    /**
     * Initialises Pudding by creating the UI, loading saved tasks from {@code filePath}.
     * If the file cannot be read, starts with an empty task list.
     *
     * @param filePath path to the data file used for persistent storage
     */
    public Pudding(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            tasks = new TaskList(storage.load());
        } catch (PuddingException e) {
            ui.showLoadingError();
            tasks = new TaskList();
        }
    }

    /**
     * Starts the main event loop: reads commands, parses them, executes them,
     * and repeats until an {@link ExitCommand} signals the end.
     */
    public void run() {
        ui.showWelcome();
        boolean isExit = false;
        while (!isExit) {
            try {
                String fullCommand = ui.readCommand();
                ui.showLine();
                Command c = Parser.parse(fullCommand);
                c.execute(tasks, ui, storage);
                isExit = c.isExit();
            } catch (PuddingException e) {
                ui.showError(e.getMessage());
            } finally {
                ui.showLine();
            }
        }
    }

    /**
     * Entry point of the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        new Pudding("src/main/java/Pudding/dataLog.txt").run();
    }
}
