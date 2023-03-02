package dal.dmw.w23.models;
import java.util.ArrayList;
import java.util.List;

/**
 * Parent Query Class that acts as a base class to represent all the Query types
 */
public class Query {
    /**
     * Type of the Query
     */
    QueryType queryType;
    /**
     * Name of the table on which query is to be executed
     */
    String tableName;
    /**
     * List of the columnNames provided
     */
    List<String> columnNames;
    /**
     * List of the conditions in the WHERE clause
     */
    List<Condition> conditions;
    /**
     * Logical operator in the WHERE clause
     */
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