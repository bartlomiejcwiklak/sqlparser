package command;

import engine.DatabaseEngine;
import engine.Result;
import exception.DatabaseException;

public interface Command {
    Result execute(DatabaseEngine engine) throws DatabaseException;
    boolean shouldExit();
}