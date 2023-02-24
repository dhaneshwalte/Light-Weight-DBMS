import java.util.ArrayList;
import java.util.List;

public class Query {
    QueryType queryType;
    String table;
    List<String> columns;
    List<Condition> conditions;
    String logicalOperator;
    List<String> values;
    
    public Query() {
        this.conditions = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    public QueryType getQueryType() {
        return queryType;
    }
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
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
    
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "Query [queryType=" + queryType + ", table=" + table + ", columns=" + columns.toString() + ", conditions="
                + conditions.toString() + ", logicalOperator=" + logicalOperator + ", values=" + values.toString() + "]";
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