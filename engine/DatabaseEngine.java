package engine;

import exception.DatabaseException;
import model.Row;
import model.Table;
import parser.WhereClause;
import storage.JsonStorage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseEngine {
    private final JsonStorage storage;
    private Table currentTable;

    public DatabaseEngine() {
        this.storage = new JsonStorage();
    }

    public Result executeInsert(String tableName, Row row) throws DatabaseException {
        loadTableContext(tableName);
        currentTable.insert(row);
        storage.saveTable(currentTable);
        return new Result("Query OK, 1 row affected", true);
    }

    public Result executeSelect(String tableName, List<String> columns, WhereClause where) throws DatabaseException {
        loadTableContext(tableName);
        
        List<Row> filteredRows = currentTable.getRows().stream()
            .filter(row -> where == null || where.matches(row))
            .collect(Collectors.toList());

        List<String> finalColumnsOrder = new ArrayList<>();

        if (columns.size() == 1 && columns.get(0).equals("*")) {
            Set<String> allColumns = new LinkedHashSet<>();

            boolean hasId = currentTable.getRows().stream()
                    .anyMatch(r -> r.getColumns().contains("id") || r.getColumns().contains("ID"));
            
            if (hasId) {
                allColumns.add("id");
            }

            for (Row row : currentTable.getRows()) {
                allColumns.addAll(row.getColumns());
            }
            
            finalColumnsOrder.addAll(allColumns);
        } else {
            finalColumnsOrder.addAll(columns);
        }

        List<Row> projectedRows = new ArrayList<>();

        for (Row original : filteredRows) {
            Row newRow = new Row();
            for (String col : finalColumnsOrder) {
                Object val = original.get(col);
                newRow.put(col, val); 
            }
            projectedRows.add(newRow);
        }

        return new Result(projectedRows);
    }

    public Result executeDelete(String tableName, WhereClause where) throws DatabaseException {
        loadTableContext(tableName);
        
        List<Row> initialRows = currentTable.getRows();
        int initialSize = initialRows.size();
        
        if (where == null) {
            currentTable.setRows(new ArrayList<>());
        } else {
            List<Row> keptRows = initialRows.stream()
                .filter(row -> !where.matches(row))
                .collect(Collectors.toList());
            currentTable.setRows(keptRows);
        }
        
        int deletedCount = initialSize - currentTable.getRows().size();
        storage.saveTable(currentTable);
        
        return new Result("Query OK, " + deletedCount + " rows affected", true);
    }

    public Result executeUpdate(String tableName, Map<String, Object> setClauses, WhereClause where) throws DatabaseException {
        loadTableContext(tableName);
        
        int updatedCount = 0;
        for (Row row : currentTable.getRows()) {
            if (where == null || where.matches(row)) {
                for (Map.Entry<String, Object> entry : setClauses.entrySet()) {
                    row.put(entry.getKey(), entry.getValue());
                }
                updatedCount++;
            }
        }
        
        if (updatedCount > 0) {
            storage.saveTable(currentTable);
        }
        
        return new Result("Query OK, " + updatedCount + " rows affected", true);
    }

    private void loadTableContext(String tableName) throws DatabaseException {
        if (currentTable == null || !currentTable.getName().equals(tableName)) {
            currentTable = storage.loadTable(tableName);
        }
    }
}