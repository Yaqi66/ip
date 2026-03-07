package Pudding.UI;

/**An automated smoke testing of your iP JAR file on Linux, Mac, and Windows found the application crashes with the following error(s) upon launching:

 On Linux, using Java 17, for the pudding.jar uploaded on 03-05-2026 (MM-DD-YYYY) at 04:00:03:
 Error: LinkageError occurred while loading main class pudding.ui.Pudding java.lang.UnsupportedClassVersionError: pudding/ui/Pudding has been compiled by a more recent version of the Java Runtime (class file version 65.0), this version of the Java Runtime only recognizes class file versions up to 61.0
 On Linux(WSL), using Java 17, for the pudding.jar uploaded on 03-05-2026 (MM-DD-YYYY) at 04:00:03:
 Error: LinkageError occurred while loading main class pudding.ui.Pudding java.lang.UnsupportedClassVersionError: pudding/ui/Pudding has been compiled by a more recent version of the Java Runtime (class file version 65.0), this version of the Java Runtime only recognizes class file versions up to 61.0
 On Mac(ARM), using Java 17, for the pudding.jar uploaded on 03-05-2026 (MM-DD-YYYY) at 04:00:03:
 Error: LinkageError occurred while loading main class pudding.ui.Pudding java.lang.UnsupportedClassVersionError: pudding/ui/Pudding has been compiled by a more recent version of the Java Runtime (class file version 65.0), this version of the Java Runtime only recognizes class file versions up to 61.0
 FYI, we smoke-tested the JAR on following environments: Linux, Linux(WSL), Mac(ARM).
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
// test