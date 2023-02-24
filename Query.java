import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query {
    QueryType queryType;
    String tableName;
    List<String> columns;
    List<Condition> conditions;
    String logicalOperator;
    List<String> values;
    List<Map<String,Object>> data;
    
    public Query() {
        this.conditions = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.values = new ArrayList<>();
        this.data = new ArrayList<>();
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
    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Query [queryType=" + queryType + ", tableName=" + tableName + ", columns=" + columns.toString() + ", conditions="
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