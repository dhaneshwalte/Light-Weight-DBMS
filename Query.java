import java.util.ArrayList;
import java.util.List;

public class Query {
    QueryType queryType;
    String tableName;
    List<String> columnNames;
    List<Condition> conditions;
    String logicalOperator;
    
    public Query() {
        this.conditions = new ArrayList<>();
        this.columnNames = new ArrayList<>();
    }

    public QueryType getQueryType() {
        return queryType;
    }
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public List<String> getColumnNames() {
        return columnNames;
    }
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
    public List<Condition> getConditions() {
        return conditions;
    }
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }
    public String getLogicalOperator() {
        return logicalOperator;
    }
    public void setLogicalOperator(String logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    @Override
    public String toString() {
        return "Query [queryType=" + queryType + ", tableName=" + tableName + ", columnNames=" + columnNames.toString() + ", conditions="
                + conditions.toString() + ", logicalOperator=" + logicalOperator + "]";
    }
}

enum QueryType {
    CREATE,
    SELECT,
    UPDATE,
    DELETE,
    INSERT
}

class Condition{
    String leftOperand;
    String rightOperand;
    String operator;
    public Condition(String leftOperand, String rightOperand, String operator) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }
    @Override
    public String toString() {
        return "Condition [leftOperand=" + leftOperand + ", rightOperand=" + rightOperand + ", operator=" + operator
                + "]";
    }
    
}