package model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Table {
    private String name;
    private List<Row> rows;
    private int lastId = 0;
    
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+(\\.0)?$");

    public Table(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void insert(Row row) {
        Object existingId = null;
        if (row.getColumns().contains("id")) {
            existingId = row.get("id");
        } else if (row.getColumns().contains("ID")) {
            existingId = row.get("ID");
        }

        if (existingId != null) {
            if (existingId instanceof Number) {
                int idVal = ((Number) existingId).intValue();
                if (idVal > lastId) {
                    lastId = idVal;
                }
            } else {
                String idStr = existingId.toString();
                if (INT_PATTERN.matcher(idStr).matches()) {
                    double dVal = Double.parseDouble(idStr);
                    int idVal = (int) dVal;
                    if (idVal > lastId) {
                        lastId = idVal;
                    }
                }
            }
        } else {
            if (rows.isEmpty()) {
                lastId = 0;
            }
            lastId++;
            row.put("id", (double) lastId);
        }

        rows.add(row);
    }

    public void delete(Row row) {
        rows.remove(row);
        if (rows.isEmpty()) {
            lastId = 0;
        }
    }

    public void setRows(List<Row> newRows) {
        this.rows = newRows;
        if (this.rows.isEmpty()) {
            lastId = 0;
        } else {
            recalculateLastId();
        }
    }

    private void recalculateLastId() {
        lastId = 0;
        for (Row row : rows) {
            Object idObj = row.get("id");
            if (idObj == null) idObj = row.get("ID");
            
            if (idObj instanceof Number) {
                int idVal = ((Number) idObj).intValue();
                if (idVal > lastId) lastId = idVal;
            }
        }
    }
}