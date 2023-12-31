package dal.dmw.w23.services;
import java.util.ArrayList;
import java.util.List;

import dal.dmw.w23.models.Column;
import dal.dmw.w23.models.ColumnConstraint;
import dal.dmw.w23.models.Condition;
import dal.dmw.w23.models.CreateQuery;
import dal.dmw.w23.models.DeleteQuery;
import dal.dmw.w23.models.InsertQuery;
import dal.dmw.w23.models.Query;
import dal.dmw.w23.models.QueryType;
import dal.dmw.w23.models.SelectQuery;
import dal.dmw.w23.models.UpdateQuery;

/**
 * This class has methods that parses the SQL Query and 
 * extracts tokens out of it.
 */
public class SqlParser {

    /**
     * This is the array contains the tokens extraced from the SQL String
     */
    private String[] tokens;

    /**
     * Constructor that accepts the SQL and sets the tokens[] attribute
     * @param sql - SQL String
     */
    public SqlParser(String sql) {
        // Replace spaces between double quotes with pipe (|)
        // eg name = "Alfred Barnes" is a single entity and should not be split 
        sql = replaceSpacesBetweenQuotes(sql); 
        if (!checkValidQuotes(sql)){
            throw new RuntimeException("Invalid Double Quotes");
        }
        sql = replaceVarcharParantheses(sql);
        // Tokenize the SQL string
        tokens = sql
                    .trim()
                    .replaceAll("\\(", " \\( ")
                    .replaceAll("\\)", " \\) ")
                    .replaceAll(",", " , ")
                    .replaceAll("=", " = ")
                    .replaceAll(";", " ; ")
                    .split("\\s+");
        for(int i = 0; i < tokens.length; i++){
            tokens[i] = tokens[i].replaceAll("\\|", " "); //Replace pipe back with space
        }
    }

    /**
     * This method is a general handler which calls the respective handler depending on
     * the query type
     * @return - returns the final Query object
     */
    public Query parse() {
        // Identify the statement type
        String firstToken = tokens[0].toUpperCase();
        if (firstToken.equals("SELECT")) {
            return parseSelectStatement();
        } else if (firstToken.equals("INSERT")) {
            return parseInsertStatement();
        } else if (firstToken.equals("UPDATE")) {
            return parseUpdateStatement();
        } else if (firstToken.equals("DELETE")) {
            return parseDeleteStatement();
        } else if (firstToken.equals("CREATE")) {
            return parseCreateTableStatement();
        } else if (firstToken.equals("EXIT")){
            System.exit(0);
            return null;
        }
        else {
            System.out.println("Unsupported SQL statement type");
            return null;
        }
    }

    /**
     * This method parses the SQL String for CREATE Query
     * @return - returns the Query object.
     */
    public Query parseCreateTableStatement() {
        int currentTokenIndex = 0;
        CreateQuery createQuery = new CreateQuery();
        createQuery.setQueryType(QueryType.CREATE);
        currentTokenIndex++;
    
        // Check for "TABLE"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("TABLE")) {
            throw new RuntimeException("Expecting TABLE keyword");
        }
    
        currentTokenIndex++; // Skip "TABLE"
    
        // Parse the table name
        String tableName = tokens[currentTokenIndex];
        createQuery.setTableName(tableName);
        currentTokenIndex++; // Skip TableName
    
        // Check for "("
        if (!tokens[currentTokenIndex].equals("(")) {
            throw new RuntimeException("Expecting ( after table name");
        }
    
        currentTokenIndex++;
    
        // Parse the column definitions
        boolean primaryColumnProvided = false;
        while (!tokens[currentTokenIndex].equals(")")) {
            String columnName = tokens[currentTokenIndex];
            String dataType = tokens[currentTokenIndex+1];
            currentTokenIndex += 2;
            
            if (!tokens[currentTokenIndex].equals(",")
                && !tokens[currentTokenIndex].equals(")")) {
                //Constraint detected
                if (tokens[currentTokenIndex].equalsIgnoreCase("UNIQUE")){
                    createQuery.getColumns().add(new Column(columnName, dataType, ColumnConstraint.UNIQUE));
                    currentTokenIndex++;
                } else if (tokens[currentTokenIndex].equalsIgnoreCase("NOT")){
                    createQuery.getColumns().add(new Column(columnName, dataType, ColumnConstraint.NOT_NULL));
                    currentTokenIndex += 2; //Skip NOT NULL
                } else if (tokens[currentTokenIndex].equalsIgnoreCase("PRIMARY")){
                    if (primaryColumnProvided){
                        throw new RuntimeException("Multiple primary columns not supported");
                    }
                    createQuery.getColumns().add(new Column(columnName, dataType, ColumnConstraint.PRIMARY_KEY));
                    primaryColumnProvided = true;
                    currentTokenIndex += 2; //Skip PRIMARY KEY
                } else {
                    throw new RuntimeException("Unsupported SQL constraint type");
                }
            } else {
                createQuery.getColumns().add(new Column(columnName, dataType, null));
            }
            
            if (tokens[currentTokenIndex].equals(")")) {
                currentTokenIndex++; //Skip )
                break;
            }

            currentTokenIndex++; // Skip past ","
        }
    
        // Ensure the statement ends with ";"
        if (!tokens[currentTokenIndex].equals(";")) {
            throw new RuntimeException("Expecting ; at end of statement");
        }

        return createQuery;
    }

    /**
     * This method parses the SQL String for SELECT Query
     * @return - returns the Query object.
     */
    public Query parseSelectStatement() {
        int currentTokenIndex = 0;
        SelectQuery selectQuery = new SelectQuery();
        selectQuery.setQueryType(QueryType.SELECT);
        currentTokenIndex++;

        List<String> columnList = new ArrayList<>();
        while (!tokens[currentTokenIndex].equalsIgnoreCase("FROM")) {
            String[] columnNames = tokens[currentTokenIndex].split(",");
            for (String columnName : columnNames) {
                columnList.add(columnName);
            }
            currentTokenIndex++;
        }
        selectQuery.setColumnNames(columnList);
        if (columnList.size() == 1 && columnList.get(0).equals("*")){
            selectQuery.setColumnNames(new ArrayList<>());
        }

        currentTokenIndex++; // Skip past "FROM"

        // Parse the table being queried
        String tableName = tokens[currentTokenIndex];
        selectQuery.setTableName(tableName);
        currentTokenIndex++;

        handleWhereCondition(currentTokenIndex, selectQuery);
        return selectQuery;
    }

    /**
     * This method parses the SQL String for INSERT Query
     * @return - returns the Query object.
     */
    public Query parseInsertStatement() {
        int currentTokenIndex = 0;
        currentTokenIndex++;
        InsertQuery insertQuery = new InsertQuery();
        insertQuery.setQueryType(QueryType.INSERT);
        // Skip past "INTO"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("INTO")) {
            throw new RuntimeException("Expecting INTO keyword");
        }

        currentTokenIndex++;

        // Parse the table being inserted into
        String tableName = tokens[currentTokenIndex];
        currentTokenIndex++;
        insertQuery.setTableName(tableName);

        // Parse the columns (if present)
        if (tokens[currentTokenIndex].equalsIgnoreCase("(")) {
            currentTokenIndex++; // Skip (
            List<String> columnList = new ArrayList<>();
            while (!tokens[currentTokenIndex].equalsIgnoreCase(")")) {
                String[] columnNames = tokens[currentTokenIndex].split(",");
                for (String columnName : columnNames) {
                    columnList.add(columnName);
                }
                currentTokenIndex++;
            }
            insertQuery.setColumnNames(columnList);
            currentTokenIndex++; // Skip past ")"
        }

        // Skip past "VALUES"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("VALUES")) {
            throw new RuntimeException("Expecting VALUES keyword");
        }

        currentTokenIndex++;

        // Parse the values being inserted
        if (tokens[currentTokenIndex].equalsIgnoreCase("(")) {
            currentTokenIndex++;
            List<String> valueList = new ArrayList<>();
            while (!tokens[currentTokenIndex].equalsIgnoreCase(")")) {
                String[] valueNames = tokens[currentTokenIndex].split(",");
                for (String valueName : valueNames) {
                    valueList.add(valueName);
                }
                currentTokenIndex++;
            }
            insertQuery.setValues(valueList);
            currentTokenIndex++; // Skip past ")"
        }

        return insertQuery;
    }

    /**
     * This method parses the SQL String for UPDATE Query
     * @return - returns the Query object.
     */
    public Query parseUpdateStatement() {
        int currentTokenIndex = 0;
        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setQueryType(QueryType.UPDATE);
        currentTokenIndex++;

        // Parse the table being updated
        String tableName = tokens[currentTokenIndex];
        updateQuery.setTableName(tableName);
        currentTokenIndex++;

        // Check for "SET"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("SET")) {
            throw new RuntimeException("Expecting SET keyword");
        }

        currentTokenIndex++;

        // Parse the new values for the columns
        while (currentTokenIndex < tokens.length && !tokens[currentTokenIndex].equalsIgnoreCase("WHERE")) {
            String columnName = tokens[currentTokenIndex];
            String value = tokens[currentTokenIndex+2];
            updateQuery.getData().put(columnName, value);
            currentTokenIndex += 3;
            if (currentTokenIndex < tokens.length && !tokens[currentTokenIndex].equalsIgnoreCase(",")) {
                break;
            }
            currentTokenIndex++;
        }
        // Check for "WHERE"
        if (currentTokenIndex < tokens.length && tokens[currentTokenIndex].equalsIgnoreCase("WHERE")) {
            handleWhereCondition(currentTokenIndex, updateQuery);
        }
        return updateQuery;
    }

    /**
     * This method parses the SQL String for DELETE Query
     * @return - returns the Query object.
     */
    public Query parseDeleteStatement() {
        int currentTokenIndex = 0;
        System.out.println("Delete statement");
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQueryType(QueryType.DELETE);
        currentTokenIndex++;

        // Check for "FROM"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("FROM")) {
            throw new RuntimeException("Expecting FROM keyword");
        }

        currentTokenIndex++;

        // Parse the table to delete from
        String tableName = tokens[currentTokenIndex];
        deleteQuery.setTableName(tableName);
        currentTokenIndex++;

        // Check for "WHERE"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("WHERE")) {
            throw new RuntimeException("Expecting WHERE keyword");
        }

        handleWhereCondition(currentTokenIndex, deleteQuery);
        return deleteQuery;
    }

    /**
     * This is a utility method that updates the query object with where conditions
     * @param currentTokenIndex - token index of the "WHERE" clause
     * @param query - Query object that needs to be updated.
     */
    private void handleWhereCondition(int currentTokenIndex, Query query) {
        if (currentTokenIndex+2 < tokens.length) {
            currentTokenIndex++;
            // Parse first condition
            String leftOperand = tokens[currentTokenIndex];
            String operator = tokens[currentTokenIndex + 1];
            String rightOperand = tokens[currentTokenIndex + 2];
            StringBuilder whereCondition = new StringBuilder(leftOperand).append(" ").append(operator).append(" ").append(rightOperand);
            query.getConditions().add(new Condition(leftOperand, rightOperand, operator));
            currentTokenIndex += 3;
    
            // Parse any additional conditions
            while (currentTokenIndex < tokens.length
                    && !tokens[currentTokenIndex].equalsIgnoreCase(";")) {
    
                // Handle logical operators
                if (tokens[currentTokenIndex].equalsIgnoreCase("AND") 
                    || tokens[currentTokenIndex].equalsIgnoreCase("OR")) {
                    whereCondition.append(" ").append(tokens[currentTokenIndex]).append(" ");
                    query.setLogicalOperator(tokens[currentTokenIndex]);
                    currentTokenIndex++;
    
                    // Parse next condition
                    leftOperand = tokens[currentTokenIndex];
                    operator = tokens[currentTokenIndex + 1];
                    rightOperand = tokens[currentTokenIndex + 2];
                    whereCondition.append(leftOperand).append(" ").append(operator).append(" ").append(rightOperand);
                    query.getConditions().add(new Condition(leftOperand, rightOperand, operator));
                    currentTokenIndex += 3;
                } else {
                    System.out.println("Invalid Logical Operator Only AND/OR allowed");
                    break;
                }
            }
        }
    }

    /**
     * Utility method that converts the paranthese of varchar from () to []
     * This is needed to escape the addition of spaces around parantheses.
     * @param input - input SQL String
     * @return - returns the string after replacement.
     */
    private String replaceVarcharParantheses(String input) {
        int index = input.toLowerCase().indexOf("varchar");
        while (index >= 0) { //varchar found
            int start = input.indexOf("(", index);
            //index+6 is 'r' +7 or +8 should be '('
            if (start - (index+6) > 2){
                throw new RuntimeException("Varchar syntax error");
            }
            if (start >= 0) { //opening brace for varchar
                int end = input.indexOf(")", start);
                if (end >= 0) {
                    input = input.substring(0, start) + "[" + input.substring(start + 1, end) + "]" + input.substring(end + 1);
                }
                else { // corresponding ) not found
                    throw new RuntimeException("Varchar syntax error");
                }
            }
            index = input.toLowerCase().indexOf("varchar", index + 1);
        }
        return input;
    }

    /**
     * Utility method that handles space seperated values given in between double quotes
     * @param input - input SQL string
     * @return - returns the SQL string
     */
    private String replaceSpacesBetweenQuotes(String input) {
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        for (char c : input.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            }
            if (c == ' ' && inQuotes) {
                result.append('|');
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Utility method that checks if the double quotes are valid
     * @param input - input SQL string
     * @return returns true if the string is valid.
     */
    private boolean checkValidQuotes(String input) {
        int count = 0;
        for (char c : input.toCharArray()) {
            if (c == '\"') {
                count++;
            }
        }
        return count % 2 == 0;
    }
}