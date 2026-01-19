package storage;

import exception.DatabaseException;
import model.Row;
import model.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JsonStorage {
    private static final String FILE_EXTENSION = ".json";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public void saveTable(Table table) throws DatabaseException {
        String filename = table.getName() + FILE_EXTENSION;
        String jsonContent = serialize(table);
        
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(jsonContent);
        } catch (IOException e) {
            throw new DatabaseException("Could not save data to file: " + e.getMessage());
        }
    }

    public Table loadTable(String tableName) throws DatabaseException {
        String filename = tableName + FILE_EXTENSION;
        File file = new File(filename);

        if (!file.exists()) {
            return new Table(tableName);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return deserialize(tableName, content.toString());
        } catch (IOException e) {
            throw new DatabaseException("Could not load data from file: " + e.getMessage());
        }
    }

    private String serialize(Table table) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        json.append("  \"name\": \"").append(table.getName()).append("\",\n");
        
        Set<String> allColumns = new LinkedHashSet<>();
        
        boolean hasId = false;
        for (Row row : table.getRows()) {
             if (row.getColumns().contains("id")) {
                 hasId = true;
                 break;
             }
        }
        
        if (hasId) {
            allColumns.add("id");
        }

        for (Row row : table.getRows()) {
            allColumns.addAll(row.getColumns());
        }
        
        json.append("  \"columns\": [");
        int c = 0;
        for (String col : allColumns) {
            json.append("\"").append(col).append("\"");
            if (c < allColumns.size() - 1) {
                json.append(", ");
            }
            c++;
        }
        json.append("],\n");

        json.append("  \"rows\": [\n");
        List<Row> rows = table.getRows();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            json.append("    {");
            int colIndex = 0;
            
            for (String col : allColumns) {
                Object valObj = row.get(col);
                
                json.append("\"").append(col).append("\":");

                if (valObj == null) {
                    json.append("null");
                } else if (valObj instanceof Number) {
                    json.append(valObj);
                } else {
                    String val = valObj.toString()
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n");
                    json.append("\"").append(val).append("\"");
                }

                if (colIndex < allColumns.size() - 1) {
                    json.append(",");
                }
                colIndex++;
            }
            json.append("}");
            if (i < rows.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}");
        return json.toString();
    }

    private Table deserialize(String tableName, String json) {
        Table table = new Table(tableName);
        String content = json.trim();
        
        Set<String> columnsOrder = new LinkedHashSet<>();
        
        int colIndex = content.indexOf("\"columns\":");
        if (colIndex != -1) {
            int startBracket = content.indexOf("[", colIndex);
            int endBracket = content.indexOf("]", startBracket);
            if (startBracket != -1 && endBracket != -1) {
                String colStr = content.substring(startBracket + 1, endBracket);
                if (!colStr.trim().isEmpty()) {
                    String[] cols = colStr.split(",");
                    for (String c : cols) {
                        String colName = c.trim().replace("\"", "");
                        if (!colName.isEmpty()) {
                            columnsOrder.add(colName);
                        }
                    }
                }
            }
        }

        String rowsContent = "";

        if (content.startsWith("{")) {
            int rowsIndex = content.indexOf("\"rows\":");
            if (rowsIndex != -1) {
                int startBracket = content.indexOf("[", rowsIndex);
                int endBracket = content.lastIndexOf("]");
                if (startBracket != -1 && endBracket != -1) {
                    rowsContent = content.substring(startBracket + 1, endBracket).trim();
                }
            }
        }
        
        if (rowsContent.isEmpty()) {
            return table;
        }

        String[] objects = rowsContent.split("},\\s*\\{");

        if (columnsOrder.isEmpty()) {
             for (String obj : objects) {
                obj = obj.replace("{", "").replace("}", "").trim();
                if (obj.isEmpty()) continue;
                String[] entryPairs = obj.split(",\\s*\""); 
                for (int i = 0; i < entryPairs.length; i++) {
                    String pair = entryPairs[i];
                    if (i > 0) pair = "\"" + pair;
                    String[] entry = pair.split("\":");
                    if (entry.length >= 1) {
                         String key = entry[0].replace("\"", "").trim();
                         columnsOrder.add(key);
                    }
                }
             }
        }
        
        if (columnsOrder.contains("id")) {
            Set<String> reordered = new LinkedHashSet<>();
            reordered.add("id");
            reordered.addAll(columnsOrder);
            columnsOrder = reordered;
        }

        for (String obj : objects) {
            obj = obj.replace("{", "").replace("}", "").trim();
            if (obj.isEmpty()) continue;

            Row row = new Row();
            
            for (String col : columnsOrder) {
                row.put(col, null);
            }
            
            String[] entryPairs = obj.split(",\\s*\""); 
            
            for (int i = 0; i < entryPairs.length; i++) {
                String pair = entryPairs[i];
                if (i > 0) pair = "\"" + pair;

                String[] entry = pair.split("\":");
                if (entry.length >= 2) {
                    String key = entry[0].replace("\"", "").trim();
                    
                    StringBuilder valueBuilder = new StringBuilder();
                    for(int k=1; k<entry.length; k++) {
                        if(k > 1) valueBuilder.append("\":");
                        valueBuilder.append(entry[k]);
                    }
                    
                    String rawValue = valueBuilder.toString().trim();
                    Object finalValue;

                    if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
                        String valStr = rawValue.substring(1, rawValue.length() - 1);
                        finalValue = valStr.replace("\\\"", "\"").replace("\\n", "\n");
                    } else if ("null".equals(rawValue)) {
                        finalValue = null;
                    } else if (NUMBER_PATTERN.matcher(rawValue).matches()) {
                        try {
                            finalValue = Double.parseDouble(rawValue);
                        } catch (NumberFormatException e) {
                            finalValue = rawValue;
                        }
                    } else {
                        finalValue = rawValue;
                    }

                    row.put(key, finalValue);
                }
            }
            table.insert(row);
        }
        return table;
    }
}