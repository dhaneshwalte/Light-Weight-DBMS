package dal.dmw.w23;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dal.dmw.w23.models.Column;
import dal.dmw.w23.models.ColumnConstraint;
import dal.dmw.w23.models.Condition;
import dal.dmw.w23.models.Table;

/**
 * This is a service class which represents the DB APIs
 * APIs are for the following queries - 
 * CREATE, SELECT, INSERT, UPDATE, DELETE
 */
public class DbService {
    /**
     * This is the relative path to the tables in the specified database.
     */
    private String dataDirectory;
    /**
     * This is the in memory table data structure that will be in sync with the physical file
     */
    private Map<String, Table> tables;

    public DbService(String dbName) {
        this.dataDirectory = Constants.databasePath + dbName + "/";
        tables = new HashMap<>();
    }
    
    /**
     * This method returns the mentioned table
     * from the in memory structure if exists, else it will
     * fetch the table from the physical storage, put it in-memory and return
     * @param tableName - name of the table to be retrieved
     * @return returns the fetched table if it exists else return null
     */
    public Table getTable(String tableName) {
        Table table = null;
        if (tables.containsKey(tableName)){
            table = tables.get(tableName);
        } else {
            table = loadTable(tableName);
        }
        if (table != null){
            tables.put(tableName, table);
        }
        return table;
    }

    /**
     * This method acts as an API for the CREATE SQL Query
     * @param tableName - name of the table to be created
     * @param columns - list of the columns associated with the table
     * @return - returns true if the table creation was successful
     */
    public boolean createTable(String tableName, List<Column> columns) {
        if (tables.containsKey(tableName)){
            System.out.println("Table Already Exists");
            return false;
        }
        tables.put(tableName, new Table(columns, new ArrayList<>()));
        saveTable(tableName);
        saveMeta(tableName, columns);
        return true;
    }

    /**
     * This method acts as an API for the INSERT SQL Query.
     * @param tableName - name of the table in which row is to be inserted.
     * @param row - the values that will be inserted in the table.
     * @return - returns true if insertion was successful.
     */
    public boolean insert(String tableName, LinkedHashMap<String, Object> row) {
        Table table = tables.get(tableName);
        if (!checkIntegrityConstraints(table, row)){
            System.out.println("Integrity Contraints failed");
            return false;
        }
        List<LinkedHashMap<String, Object>> values = table.getValues();
        values.add(row);
        table.setValues(values);
        saveTable(tableName);
        return true;
    }

    /**
     * This method acts as an API for the INSERT SQL Query
     * @param tableName - name of the table that is to be updated.
     * @param updateData - the data that is to be updated
     * @param conditions - conditions in the WHERE clause
     * @param logicalOperator - logical concatenation operator in the WHERE clause
     * @return returns true if the update was successful.
     */
    public boolean update(String tableName,  Map<String,Object> updateData, 
    List<Condition> conditions, String logicalOperator ){
        Table table = getTable(tableName);    
        if (table == null){
            System.out.println("Table not found");
            return false;
        }
        for(int i = 0; i < table.getValues().size(); i++){
            LinkedHashMap<String, Object> row = table.getValues().get(i);
            if(sastisfyConditions(row, conditions, logicalOperator)){
                for(Map.Entry<String, Object> entry: updateData.entrySet()){
                    if (!table.getValues().get(i).containsKey(entry.getKey())){
                        System.out.println("Update attribute: " + entry.getKey() + " Not found");
                        return false;
                    }
                }
                if (checkIntegrityConstraints(table, updateData)){
                    for(Map.Entry<String, Object> entry: updateData.entrySet()){
                            LinkedHashMap<String, Object> value = table.getValues().get(i);
                            value.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        saveTable(tableName);
        return true;
    }

    /**
     * This method acts as an API for the SELECT SQL Query
     * @param tableName - name of the table from which data is to be selected.
     * @param columnNames - name of the columns to be selected.
     * @param conditions - conditions in the WHERE clause.
     * @param logicalOperator - logical concatenation operator in the WHERE clause
     * @return
     */
    public Table select(String tableName, List<String> columnNames, 
                        List<Condition> conditions, String logicalOperator) {

        Table table = getTable(tableName);    
        if (table == null){
            System.out.println("Table not found");
            return null;
        }
        if (columnNames.isEmpty()){
            columnNames = table.getColumnNames();
        }
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (LinkedHashMap<String, Object> row : table.getValues()) {
            if(sastisfyConditions(row, conditions, logicalOperator)){
                LinkedHashMap<String, Object> selectedRow = new LinkedHashMap<>();
                for (String columnName : columnNames) {
                    selectedRow.put(columnName, row.get(columnName));
                }
                result.add(selectedRow);
            }
        }
        List<Column> resultColumns = new ArrayList<>();
        for(Column column: table.getColumns()){
            for(String columnName: columnNames){
                if(columnName.equals(column.getColumnName())){
                    resultColumns.add(column);
                }
            }
        }
        return new Table(resultColumns, result);
    }

    /**
     * This is a utility method that checks a row of the table against the WHERE conditions 
     * @param row - row against which the where conditions will be checked.
     * @param conditions - WHERE conditions to be checked
     * @param logicalOperator - logical operator in the WHERE clause.
     * @return returns true if all the conditons pass
     */
    private boolean sastisfyConditions(LinkedHashMap<String, Object> row, 
                            List<Condition> conditions, String logicalOperator) {
        
        if (conditions.isEmpty()) return true;
        List<Boolean> conditionResults = new ArrayList<>();
        for(Condition condition : conditions){
            String conditionKey = condition.getLeftOperand();
            String conditionValue = condition.getRightOperand();
            if (!row.containsKey(conditionKey)){
                System.out.println("Where clause key: "+ conditionKey + " Not found");
                return false;
            }
            String rowValue = (String) row.get(conditionKey);
            conditionResults.add(testCondition(conditionKey, conditionValue, rowValue, condition.getOperator()));
        }
        if (logicalOperator == null){
            return conditionResults.get(0);
        } else if (logicalOperator.equalsIgnoreCase("OR")){
            return (conditionResults.get(0) || conditionResults.get(1));
        } else if (logicalOperator.equalsIgnoreCase("AND")){
            return (conditionResults.get(0) && conditionResults.get(1));
        }
        return true;
    }

    /**
     * This is a utility method that verifies the equation that contains LHS and RHS
     * @param conditionKey - key specified in the condition
     * @param conditionValue - value associated with the condition
     * @param rowValue - value of the row, against which the equation will be verified
     * @param operator - operator of the equation
     * @return returns true if the condition is verified against the rowValue.
     */
    private boolean testCondition(String conditionKey, String conditionValue, 
                                  String rowValue, String operator) {
        
        if (operator.equals("=") || operator.equals("==")){
            if (rowValue.equals(conditionValue)) return true;
            else return false;
        }
        if (operator.equals("!=")){
            if (!rowValue.equals(conditionValue)) return true;
            else return false;
        }
        int rowIntValue = Integer.parseInt(rowValue);
        int conditionIntValue = Integer.parseInt(conditionValue);
        switch(operator){
            case ">":
                if (rowIntValue > conditionIntValue) return true;
            case "<":
                if (rowIntValue < conditionIntValue) return true;
            case ">=":
                if (rowIntValue >= conditionIntValue) return true;
            case "<=":
                if (rowIntValue <= conditionIntValue) return true;
            default:
                return false;
        }
    }

    /**
     * The method acts as an API for DELETE SQL Query
     * @param tableName - the name of the table on which delete is to be performed
     * @param conditions - WHERE conditions in the query.
     * @param logicalOperator - logical concatenation operator of WHERE clause.
     * @return - returns true if deletion was successful.
     */
    public boolean delete(String tableName, List<Condition> conditions, String logicalOperator) {
        Table table = getTable(tableName);    
        if (table == null){
            System.out.println("Table not found");
            return false;
        }
        List<LinkedHashMap<String, Object>> values = table.getValues();
        values.removeIf(row -> {
            if(sastisfyConditions(row, conditions, logicalOperator)){
                return true;
            }
            return false;
        });
        table.setValues(values);
        saveTable(tableName);
        return true;
    }

    /**
     * This is a utility method that checks the integrity constraints of the new row
     * that is to be inserted with the constrains of the table in which the row is 
     * to be inserted.
     * @param table - The table in which the row is to be inserted.
     * @param row - the values to be inserted in the row.
     * @return - returns true if the values pass the integrity contraints.
     */
    private boolean checkIntegrityConstraints(Table table, Map<String, Object> row){
        for(Map.Entry<String, Object> entry: row.entrySet()){
            for(Column column: table.getColumns()){
                if (column.getColumnName().equals(entry.getKey())){
                    if (column.getColumnConstraint() == ColumnConstraint.UNIQUE){
                        if (!checkUniqueConstraint(table, entry)){
                            System.out.println("UNIQUE CONSTRAINT VIOLATION");
                            return false;
                        }
                    }
                    else if (column.getColumnConstraint() == ColumnConstraint.NOT_NULL
                    && entry.getValue() == null){
                        System.out.println("NOT NULL CONSTRAINT VIOLATION: " + entry.getKey());
                        return false;
                    }
                    else if (column.getColumnConstraint() == ColumnConstraint.PRIMARY_KEY){
                        if (entry.getValue() == null || !checkUniqueConstraint(table, entry)){
                            System.out.println("PRIMARY KEY CONSTRAINT VIOLATION");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    /**
     * This is a utility method that checks the UNIQUE constraint by comparing the
     * row to be inserted with the entire data in the table.
     * @param table - The table in which new row is to be inserted.
     * @param entry - the entry object which is to be inserted.
     * @return returns true if the values pass the UNIQUE constraints.
     */
    public boolean checkUniqueConstraint(Table table, Map.Entry<String, Object> entry){
        String insertKey = entry.getKey();
        String insertValue = entry.getValue().toString();
        for(LinkedHashMap<String, Object> tableRow: table.getValues()){
            if (tableRow.get(insertKey).toString().equals(insertValue)){
                System.out.println("Duplicate value for UNIQUE constraint "+ insertKey + " : "+ insertValue);
                return false;
            }
        }
        return true;
    }

    /**
     * This is a utility method that saves the Column data phyisically which includes
     * the datatype of the column and the integrity constraints imposed on the column
     * @param tableName - name of the table whose columns these belong to
     * @param columns - list of the columns
     * @return - returns true if save was successful.
     */
    public boolean saveMeta(String tableName, List<Column> columns) {
        String metaPath = dataDirectory + tableName + Constants.metaFileExtension;
        File metaFile = new File(metaPath);
        if (!metaFile.exists()){
            System.out.println("File DNE");
            try {
                metaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(metaPath))) {
            for (Column column : columns) { 
                List<String> values = new ArrayList<>(Arrays.asList(column.getColumnName(), 
                                                                    column.getDataType(),
                                                                    handleNull(column.getColumnConstraint())));
                String line = String.join(Constants.fileSeparator, values);
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This is a utility method that saves the table physically
     * @param tableName - name of the table that is to be saved.
     * @return - returns true if save was successful.
     */
    public boolean saveTable(String tableName) {
        String tablePath = dataDirectory + tableName + Constants.tableFileExtension;
        File tableFile = new File(tablePath);
        if (!tableFile.exists()){
            System.out.println("File DNE");
            try {
                tableFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(tablePath))) {
            Table table = tables.get(tableName);
            List<String> columnNames = new ArrayList<>();
            table.getColumns().forEach(column -> columnNames.add(column.getColumnName()));
            String header = String.join(Constants.fileSeparator, columnNames);
            writer.println(header);
            for (Map<String, Object> row : table.getValues()) {
                List<String> values = new ArrayList<>();
                for (Object value : row.values()) {
                    values.add(handleNull(value));
                }
                String line = String.join(Constants.fileSeparator, values);
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This is a utility method that checks if the object is null
     * before invoking the toString() method
     * @param object - object that needs to be null checked.
     * @return - returns empty string if the object is null, else returns toString() invocation of the object.
     */
    private String handleNull(Object object) {
        return object == null ? " " : object.toString();
    }

    /**
     * This is a utility method that loads the Column information
     * from the physical storage.
     * @param tableName - table name whose columns are to be loaded
     * @return returns the list of the Columns associated with the table.
     */
    public List<Column> loadMeta(String tableName) {
        String metaPath = dataDirectory + tableName + ".meta";
        File metaFile = new File(metaPath);
        if (!metaFile.exists()){
            System.out.println("Meta does not exist");
            return null;
        }
        List<Column> metaList = new ArrayList<>();
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(metaPath);
            bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(Constants.fileSeparator);
                ColumnConstraint columnConstraint = parseColumnConstraint(values[2]);
                metaList.add(new Column(values[0], values[1], columnConstraint));
            }
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();   
            return null;
        }
        return metaList;
    }

    /**
     * This is a utility method that loads table from the physical storage.
     * @param tableName - table name to be loaded
     * @return returns the loaded table entity.
     */
    public Table loadTable(String tableName) {
        String tablePath = dataDirectory + tableName + Constants.tableFileExtension;
        File tableFile = new File(tablePath);
        if (!tableFile.exists()){
            System.out.println("Table does not exist");
            return null;
        }
        List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(tablePath);
            bufferedReader = new BufferedReader(reader);
            String[] headers = bufferedReader.readLine().split(Constants.fileSeparator);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(Constants.fileSeparator);
                int totalColumns = values.length;
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < totalColumns; i++) {
                    String column = headers[i];
                    String value = values[i];
                    row.put(column, value);
                }
                rows.add(row);
            }
            bufferedReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();   
            return null;
        }
        List<Column> metaList = loadMeta(tableName);
        return new Table(metaList, rows);
    }

    /**
     * This is a utlity method that parses the ColumnConstraint String into the respective enum values
     * @param columnConstraintString - column constraint string to be parsed.
     * @return returns the enum value of the column constraint.
     */
    private ColumnConstraint parseColumnConstraint(String columnConstraintString){
        if (columnConstraintString.equals("UNIQUE")){
            return ColumnConstraint.UNIQUE;
        } else if (columnConstraintString.equals("NOT_NULL")){
            return ColumnConstraint.NOT_NULL;
        } else if (columnConstraintString.equals(" ")){
            return null;
        } else {
            return ColumnConstraint.PRIMARY_KEY;
        }
    }
}
