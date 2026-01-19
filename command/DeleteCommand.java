package command;

import engine.DatabaseEngine;
import engine.Result;
import exception.DatabaseException;
import parser.WhereClause;

public class DeleteCommand implements Command {
    private final String tableName;
    private final WhereClause where;

    public DeleteCommand(String tableName, WhereClause where) {
        this.tableName = tableName;
        this.where = where;
    }

    @Override
    public Result execute(DatabaseEngine engine) throws DatabaseException {
        return engine.executeDelete(tableName, where);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}