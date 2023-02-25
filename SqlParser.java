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
        sql = replaceVarcharParantheses(sql);
        System.out.println(sql);
        // Tokenize the SQL string
        tokens = sql
                    .trim()
                    .replaceAll("\\(", " \\( ")
                    .replaceAll("\\)", " \\) ")
                    .replaceAll(",", " , ")
                    .replaceAll("=", " = ")
                    .split("\\s+");
        for(int i = 0; i < tokens.length; i++){
            tokens[i] = tokens[i].replaceAll("\\|", " "); //Replace pipe back with space
            //System.out.println(tokens[i]);
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
        } else if (firstToken.equals("CREATE")) {
            return parseCreateTableStatement();
        } else {
            throw new RuntimeException("Unsupported SQL statement type");
        }
    }

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
            System.out.println(columnName + " " + dataType);
            currentTokenIndex += 2;
            
            if (!tokens[currentTokenIndex].equals(",")
                && !tokens[currentTokenIndex].equals(")")) {
                System.out.println("Constraint : " + tokens[currentTokenIndex]);
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
    private Query parseSelectStatement() {
        int currentTokenIndex = 0;
        System.out.println("Select statement");
        SelectQuery selectQuery = new SelectQuery();
        selectQuery.setQueryType(QueryType.SELECT);
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
        selectQuery.setColumnNames(columnList);
        if (columnList.size() == 1 && columnList.get(0).equals("*")){
            selectQuery.setColumnNames(null);
        }

        currentTokenIndex++; // Skip past "FROM"

        // Parse the table being queried
        String tableName = tokens[currentTokenIndex];
        selectQuery.setTableName(tableName);
        currentTokenIndex++;

        handleWhereCondition(currentTokenIndex, selectQuery);
        return selectQuery;
    }

    private Query parseInsertStatement() {
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

    private Query parseUpdateStatement() {
        int currentTokenIndex = 0;
        System.out.println("Update statement");
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
        while (!tokens[currentTokenIndex].equalsIgnoreCase("WHERE")) {
            String columnName = tokens[currentTokenIndex];
            String value = tokens[currentTokenIndex+2];
            updateQuery.getData().add(new HashMap<>(Map.of(columnName, value)));
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
        
        handleWhereCondition(currentTokenIndex, updateQuery);
        return updateQuery;
    }

    private Query parseDeleteStatement() {
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

    private String replaceVarcharParantheses(String input) {
        int index = input.toLowerCase().indexOf("varchar");
        while (index >= 0) { //varchar found
            int start = input.indexOf("(", index);
            //index+6 is 'r' +7 or +8 should be '('
            if (start - (index+6) > 2){
                throw new RuntimeException("Varchar syntax error");
            }
            if (start >= 0) { //opening brace for varchar
                // // Remove any spaces between "varchar" and the opening parenthesis
                // int spaceIndex = input.lastIndexOf(" ", start - 1);
                // if (spaceIndex >= index) {
                //     input = input.substring(0, spaceIndex) + input.substring(spaceIndex + 1);
                //     start--;
                // }
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