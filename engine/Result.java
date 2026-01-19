package engine;

import model.Row;
import java.util.List;

public class Result {
    private final String message;
    private final List<Row> rows;
    private final boolean isSuccess;

    public Result(String message, boolean isSuccess) {
        this.message = message;
        this.rows = null;
        this.isSuccess = isSuccess;
    }

    public Result(List<Row> rows) {
        this.rows = rows;
        this.message = rows.size() + " rows in set";
        this.isSuccess = true;
    }

    public String getMessage() {
        return message;
    }

    public List<Row> getRows() {
        return rows;
    }

    public boolean hasData() {
        return rows != null;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}