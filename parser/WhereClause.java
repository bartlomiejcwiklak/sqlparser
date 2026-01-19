package parser;

import model.Row;

public class WhereClause {
    private final String column;
    private final String operator;
    private final Object value;

    public WhereClause(String column, String operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public boolean matches(Row row) {
        Object rowValue = row.get(column);
        
        if (rowValue == null) return false;

        if (rowValue instanceof Number && value instanceof Number) {
            return compareNumbers(((Number) rowValue).doubleValue(), ((Number) value).doubleValue());
        }

        return compareStrings(rowValue.toString(), value.toString());
    }

    private boolean compareNumbers(double n1, double n2) {
        switch (operator) {
            case "=": return Double.compare(n1, n2) == 0;
            case "!=": return Double.compare(n1, n2) != 0;
            case ">": return n1 > n2;
            case "<": return n1 < n2;
            case ">=": return n1 >= n2;
            case "<=": return n1 <= n2;
            default: return false;
        }
    }

    private boolean compareStrings(String s1, String s2) {
        int comparison = s1.compareTo(s2);
        switch (operator) {
            case "=": return comparison == 0;
            case "!=": return comparison != 0;
            case ">": return comparison > 0;
            case "<": return comparison < 0;
            case ">=": return comparison >= 0;
            case "<=": return comparison <= 0;
            default: return false;
        }
    }
    
    @Override
    public String toString() {
        return column + " " + operator + " " + value;
    }
}