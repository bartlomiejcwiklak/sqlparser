package command;

import engine.DatabaseEngine;
import engine.Result;

public class ExitCommand implements Command {
    @Override
    public Result execute(DatabaseEngine engine) {
        return new Result("Bye", true);
    }

    @Override
    public boolean shouldExit() {
        return true;
    }
}