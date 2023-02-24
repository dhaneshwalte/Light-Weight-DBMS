import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlParser {

    private String[] tokens;
    public SqlParser(String sql) {
        // Replace spaces between double quotes with pipe (|)
        // eg name = "Alfred Barnes" is a single entity and should not be split 
        sql = replaceSpacesBetweenQuotes(sql); 
        if (!checkValidQuotes(sql)){
            throw new RuntimeException("Invalid Double Quotes");
        }
        // Tokenize the SQL string
        tokens = sql
                    .replaceAll("\\(", " \\( ")
                    .replaceAll("\\)", " \\) ")
                    .replaceAll(",", " , ")
                    .replaceAll("=", " = ")
                    .split("\\s+");
        for(int i = 0; i < tokens.length; i++){
            tokens[i] = tokens[i].replaceAll("\\|", " "); //Replace pipe back with space
        }
    }

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
        } else {
            throw new RuntimeException("Unsupported SQL statement type");
        }
    }

    private Query parseSelectStatement() {
        int currentTokenIndex = 0;
        System.out.println("Select statement");
        Query query = new Query();
        query.setQueryType(QueryType.SELECT);
        currentTokenIndex++;

        List<String> columnList = new ArrayList<>();
        while (!tokens[currentTokenIndex].equalsIgnoreCase("FROM")) {
            String[] columnNames = tokens[currentTokenIndex].split(",");
            for (String columnName : columnNames) {
                columnList.add(columnName);
                System.out.println("Selected column: " + columnName);
            }
            currentTokenIndex++;
        }
        query.setColumns(columnList);
        if (columnList.size() == 1 && columnList.get(0).equals("*")){
            query.setColumns(null);
        }

        currentTokenIndex++; // Skip past "FROM"

        // Parse the table being queried
        String tableName = tokens[currentTokenIndex];
        query.setTableName(tableName);
        currentTokenIndex++;

        handleWhereCondition(currentTokenIndex, query);
        return query;
    }

    private Query parseInsertStatement() {
        int currentTokenIndex = 0;
        currentTokenIndex++;
        Query query = new Query();
        query.setQueryType(QueryType.INSERT);
        // Skip past "INTO"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("INTO")) {
            throw new RuntimeException("Expecting INTO keyword");
        }

        currentTokenIndex++;

        // Parse the table being inserted into
        String tableName = tokens[currentTokenIndex];
        currentTokenIndex++;
        query.setTableName(tableName);

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
            query.setColumns(columnList);
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
            query.setValues(valueList);
            currentTokenIndex++; // Skip past ")"
        }

        return query;
    }

    private Query parseUpdateStatement() {
        int currentTokenIndex = 0;
        System.out.println("Update statement");
        Query query = new Query();
        query.setQueryType(QueryType.UPDATE);
        currentTokenIndex++;

        // Parse the table being updated
        String tableName = tokens[currentTokenIndex];
        query.setTableName(tableName);
        currentTokenIndex++;

        // Check for "SET"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("SET")) {
            throw new RuntimeException("Expecting SET keyword");
        }

        currentTokenIndex++;

        // Parse the new values for the columns
        while (!tokens[currentTokenIndex].equalsIgnoreCase("WHERE")) {
            String columnName = tokens[currentTokenIndex];
            String value = tokens[currentTokenIndex+2];
            query.getData().add(new HashMap<>(Map.of(columnName, value)));
            System.out.println(columnName + " " + value);
            currentTokenIndex += 3;
            if (!tokens[currentTokenIndex].equalsIgnoreCase(",")) {
                break;
            }
            currentTokenIndex++;
        }

        // Check for "WHERE"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("WHERE")) {
            throw new RuntimeException("Expecting WHERE keyword");
        }
        
        handleWhereCondition(currentTokenIndex, query);
        return query;
    }

    private Query parseDeleteStatement() {
        int currentTokenIndex = 0;
        System.out.println("Delete statement");
        Query query = new Query();
        query.setQueryType(QueryType.DELETE);
        currentTokenIndex++;

        // Check for "FROM"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("FROM")) {
            throw new RuntimeException("Expecting FROM keyword");
        }

        currentTokenIndex++;

        // Parse the table to delete from
        String tableName = tokens[currentTokenIndex];
        query.setTableName(tableName);
        currentTokenIndex++;

        // Check for "WHERE"
        if (!tokens[currentTokenIndex].equalsIgnoreCase("WHERE")) {
            throw new RuntimeException("Expecting WHERE keyword");
        }

        handleWhereCondition(currentTokenIndex, query);
        return query;
    }

    private void handleWhereCondition(int currentTokenIndex, Query query) {
        if (currentTokenIndex < tokens.length) {
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