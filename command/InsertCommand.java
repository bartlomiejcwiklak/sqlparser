package command;

import engine.DatabaseEngine;
import engine.Result;
import exception.DatabaseException;
import model.Row;

public class InsertCommand implements Command {
    private final String tableName;
    private final Row row;

    public InsertCommand(String tableName, Row row) {
        this.tableName = tableName;
        this.row = row;
    }

    @Override
    public Result execute(DatabaseEngine engine) throws DatabaseException {
        return engine.executeInsert(tableName, row);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}