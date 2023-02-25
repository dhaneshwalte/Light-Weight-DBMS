import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Table {
    List<Column> columns;
    List<LinkedHashMap<String, Object>> values;

    public Table(List<Column> columns, List<LinkedHashMap<String, Object>> values) {
        this.columns = columns;
        this.values = values;
    }

    public List<String> getColumnNames(){
        List<String> columnNames = new ArrayList<>();
        this.columns.forEach(column -> columnNames.add(column.columnName));
        return columnNames;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<LinkedHashMap<String, Object>> getValues() {
        return values;
    }

    public void setValues(List<LinkedHashMap<String, Object>> values) {
        this.values = values;
    }
}

class Column{
    String columnName;
    String dataType;
    ColumnConstraint columnConstraint;
    public Column(String columnName, String dataType, ColumnConstraint columnConstraint) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.columnConstraint = columnConstraint;
    }
    @Override
    public String toString() {
        return "ColumnDefinition [columnName=" + columnName + ", dataType=" + dataType + ", columnConstraints=" + columnConstraint
                + "] ";
    }
}
