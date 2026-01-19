package command;

import engine.DatabaseEngine;
import engine.Result;
import exception.DatabaseException;
import parser.WhereClause;

import java.util.List;

public class SelectCommand implements Command {
    private final String tableName;
    private final List<String> columns;
    private final WhereClause where;

    public SelectCommand(String tableName, List<String> columns, WhereClause where) {
        this.tableName = tableName;
        this.columns = columns;
        this.where = where;
    }

    @Override
    public Result execute(DatabaseEngine engine) throws DatabaseException {
        return engine.executeSelect(tableName, columns, where);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }
}