package command;

import engine.DatabaseEngine;
import engine.Result;
import exception.DatabaseException;
import parser.WhereClause;

import java.util.Map;

public class UpdateCommand implements Command {
    private final String tableName;
    private final Map<String, Object> setClauses;
    private final WhereClause where;

    public UpdateCommand(String tableName, Map<String, Object> setClauses, WhereClause where) {
        this.tableName = tableName;
        this.setClauses = setClauses;
        this.where = where;
    }

    @Override
    public Result execute(DatabaseEngine engine) throws DatabaseException {
        return engine.executeUpdate(tableName, setClauses, where);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}