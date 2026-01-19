package parser;

import command.*;
import exception.DatabaseException;
import model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParser {
    
    public Command parse(String input) throws DatabaseException {
        String normalized = input.trim().replaceAll("\\s+", " ");
        String upper = normalized.toUpperCase();

        if (upper.startsWith("INSERT")) {
            return parseInsert(normalized);
        } else if (upper.startsWith("SELECT")) {
            return parseSelect(normalized);
        } else if (upper.startsWith("DELETE")) {
            return parseDelete(normalized);
        } else if (upper.startsWith("UPDATE")) {
            return parseUpdate(normalized);
        } else if (upper.equals("EXIT") || upper.equals("QUIT")) {
            return new ExitCommand();
        }

        throw new DatabaseException("Unknown command syntax.");
    }

    private Command parseInsert(String query) throws DatabaseException {
        Pattern pattern = Pattern.compile("INSERT INTO ([a-zA-Z0-9_]+) \\((.+?)\\) VALUES \\((.+?)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        
        if (!matcher.find()) {
            throw new DatabaseException("Invalid INSERT syntax. Usage: INSERT INTO table (col1, col2) VALUES (val1, val2)");
        }

        String tableName = matcher.group(1);
        String[] columns = matcher.group(2).split(",");
        String[] values = matcher.group(3).split(",");

        if (columns.length != values.length) {
            throw new DatabaseException("Column count doesn't match value count.");
        }

        Row row = new Row();
        for (int i = 0; i < columns.length; i++) {
            row.put(columns[i].trim(), parseValue(values[i].trim()));
        }

        return new InsertCommand(tableName, row);
    }

    private Command parseSelect(String query) throws DatabaseException {
        Pattern pattern = Pattern.compile("SELECT (.+?) FROM ([a-zA-Z0-9_]+)(?: WHERE (.+))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) {
            throw new DatabaseException("Invalid SELECT syntax. Usage: SELECT * FROM table [WHERE col=val]");
        }

        String colsStr = matcher.group(1);
        String tableName = matcher.group(2);
        String whereClauseStr = matcher.group(3);
        
        List<String> columns = new ArrayList<>();
        for(String s : colsStr.split(",")) {
            columns.add(s.trim());
        }

        WhereClause where = parseWhere(whereClauseStr);

        return new SelectCommand(tableName, columns, where);
    }

    private Command parseDelete(String query) throws DatabaseException {
        Pattern pattern = Pattern.compile("DELETE FROM ([a-zA-Z0-9_]+)(?: WHERE (.+))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) {
            throw new DatabaseException("Invalid DELETE syntax. Usage: DELETE FROM table [WHERE col=val]");
        }

        String tableName = matcher.group(1);
        WhereClause where = parseWhere(matcher.group(2));

        return new DeleteCommand(tableName, where);
    }

    private Command parseUpdate(String query) throws DatabaseException {
        Pattern pattern = Pattern.compile("UPDATE ([a-zA-Z0-9_]+) SET (.+?)(?: WHERE (.+))?$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (!matcher.find()) {
            throw new DatabaseException("Invalid UPDATE syntax. Usage: UPDATE table SET col=val [WHERE col=val]");
        }

        String tableName = matcher.group(1);
        String setStr = matcher.group(2);
        String whereStr = matcher.group(3);

        Map<String, Object> setClauses = new HashMap<>();
        String[] assignments = setStr.split(",");
        for (String assign : assignments) {
            String[] parts = assign.split("=");
            if (parts.length != 2) {
                throw new DatabaseException("Invalid SET clause: " + assign);
            }
            setClauses.put(parts[0].trim(), parseValue(parts[1].trim()));
        }

        WhereClause where = parseWhere(whereStr);

        return new UpdateCommand(tableName, setClauses, where);
    }

    private WhereClause parseWhere(String whereStr) throws DatabaseException {
        if (whereStr == null) return null;
        
        Pattern pattern = Pattern.compile("(.+?)\\s*(>=|<=|!=|=|>|<)\\s*(.+)");
        Matcher matcher = pattern.matcher(whereStr.trim());
        
        if (matcher.find()) {
            String col = matcher.group(1).trim();
            String op = matcher.group(2).trim();
            Object val = parseValue(matcher.group(3).trim());
            return new WhereClause(col, op, val);
        }
        
        throw new DatabaseException("Invalid WHERE clause syntax: " + whereStr);
    }

    private Object parseValue(String raw) {
        if ((raw.startsWith("'") && raw.endsWith("'")) || (raw.startsWith("\"") && raw.endsWith("\""))) {
            return raw.substring(1, raw.length() - 1);
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return raw;
        }
    }
}