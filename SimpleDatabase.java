import engine.DatabaseEngine;
import ui.CommandLineInterface;

public class SimpleDatabase {
    public static void main(String[] args) {
        DatabaseEngine engine = new DatabaseEngine();
        CommandLineInterface cli = new CommandLineInterface(engine);
        cli.start();
    }
}