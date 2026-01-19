package model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Row {
    private final Map<String, Object> data;

    public Row() {
        this.data = new LinkedHashMap<>();
    }

    public Row(Map<String, Object> data) {
        this.data = new LinkedHashMap<>(data);
    }

    public void put(String column, Object value) {
        data.put(column, value);
    }

    public Object get(String column) {
        return data.get(column);
    }

    public Set<String> getColumns() {
        return data.keySet();
    }

    public Map<String, Object> getData() {
        return new LinkedHashMap<>(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}