import java.util.List;

public class CreateQuery extends Query {
    List<ColumnDefinition> columns;

    public List<ColumnDefinition> getColumnDefinitions() {
        return columns;
    }

    public void setColumnDefinitions (List<ColumnDefinition> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "CreateQuery [columns=" + columns + "] " + super.toString();
    }
    
    
}
enum ColumnConstraint {
    NOT_NULL,
    UNIQUE
}

class ColumnDefinition{
    String columnName;
    String dataType;
    List<ColumnConstraint> columnConstraints;
    public ColumnDefinition(String columnName, String dataType, List<ColumnConstraint> columnConstraints) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.columnConstraints = columnConstraints;
    }
    @Override
    public String toString() {
        return "ColumnDefinition [columnName=" + columnName + ", dataType=" + dataType + ", columnConstraints=" + columnConstraints.toString()
                + "] ";
    }
}