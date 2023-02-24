import java.util.ArrayList;
import java.util.List;

public class CreateQuery extends Query {
    List<ColumnDefinition> columns;

    
    public CreateQuery() {
        this.columns = new ArrayList<>();
    }

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
    UNIQUE,
    PRIMARY_KEY
}

class ColumnDefinition{
    String columnName;
    String dataType;
    ColumnConstraint columnConstraint;
    public ColumnDefinition(String columnName, String dataType, ColumnConstraint columnConstraint) {
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