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

public class DbService {
    String dataDirectory = "database/dbname/";
    private Map<String, Table> tables; // Make this single instance only

    public DbService() {
        tables = new HashMap<>();
    }

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

    public void createTable(String tableName, List<Column> columns) {
        if (tables.containsKey(tableName)){
            throw new RuntimeException("Table Already Exists");
        }
        tables.put(tableName, new Table(columns, new ArrayList<>()));
        saveTable(tableName);
        saveMeta(tableName, columns);
    }

    public void insert(String tableName, LinkedHashMap<String, Object> row) {
        Table table = tables.get(tableName);
        //row.forEach((k,v) -> System.out.println(k + " " + v));
        if (!checkIntegrityConstraints(table, row)){
            throw new RuntimeException("Integrity Contraints failed");
        }
        List<LinkedHashMap<String, Object>> values = table.getValues();
        values.add(row);
        table.setValues(values);
        saveTable(tableName);
    }

    public boolean checkIntegrityConstraints(Table table, Map<String, Object> row){
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

    public void update(String tableName,  Map<String,Object> updateData, 
    List<Condition> conditions, String logicalOperator ){
        Table table = getTable(tableName);    
        if (table == null){
            System.out.println("Table not found");
            return;
        }
        for(int i = 0; i < table.getValues().size(); i++){
            LinkedHashMap<String, Object> row = table.getValues().get(i);
            if(sastisfyConditions(row, conditions, logicalOperator)){
                for(Map.Entry<String, Object> entry: updateData.entrySet()){
                    if (!table.getValues().get(i).containsKey(entry.getKey())){
                        System.out.println("Update attribute: " + entry.getKey() + " Not found");
                        return;
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
    }

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
        //System.out.println();
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

    private boolean sastisfyConditions(LinkedHashMap<String, Object> row, 
                            List<Condition> conditions, String logicalOperator) {
        
        if (conditions.isEmpty()) return true;
        List<Boolean> conditionResults = new ArrayList<>();
        for(Condition condition : conditions){
            String conditionKey = condition.getLeftOperand();
            String conditionValue = condition.getRightOperand();
            if (!row.containsKey(conditionKey)){
                throw new RuntimeException("Where clause key: "+ conditionKey + " Not found");
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

    public void delete(String tableName, List<Condition> conditions, String logicalOperator) {
        Table table = getTable(tableName);    
        if (table == null){
            System.out.println("Table not found");
            return;
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
    }

    public void saveMeta(String tableName, List<Column> columns) {
        String metaPath = dataDirectory + tableName + ".meta";
        File metaFile = new File(metaPath);
        if (!metaFile.exists()){
            //TODO: Create exception
            System.out.println("File DNE");
            try {
                metaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(metaPath))) {
            for (Column column : columns) { 
                List<String> values = new ArrayList<>(Arrays.asList(column.getColumnName(), 
                                                                    column.getDataType(), 
                                                                    column.getColumnConstraint() == null ? "NONE" : column.getColumnConstraint().toString()));
                String line = String.join(",", values);
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTable(String tableName) {
        String tablePath = dataDirectory + tableName + ".csv";
        File tableFile = new File(tablePath);
        if (!tableFile.exists()){
            //TODO: Create exception
            System.out.println("File DNE");
            try {
                tableFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(tablePath))) {
            Table table = tables.get(tableName);
            List<String> columnNames = new ArrayList<>();
            table.getColumns().forEach(column -> columnNames.add(column.getColumnName()));
            String header = String.join(",", columnNames);
            writer.println(header);
            for (Map<String, Object> row : table.getValues()) {
                List<String> values = new ArrayList<>();
                for (Object value : row.values()) {
                    values.add(handleNull(value));
                }
                String line = String.join(",", values);
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handleNull(Object object) {
        return object == null ? " " : object.toString();
    }

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
                String[] values = line.split(",");
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

    public Table loadTable(String tableName) {
        String tablePath = dataDirectory + tableName + ".csv";
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
            String[] headers = bufferedReader.readLine().split(",");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
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

    private ColumnConstraint parseColumnConstraint(String columnConstraintString){
        if (columnConstraintString.equals("UNIQUE")){
            return ColumnConstraint.UNIQUE;
        } else if (columnConstraintString.equals("NOT_NULL")){
            return ColumnConstraint.NOT_NULL;
        } else if (columnConstraintString.equals("NONE")){
            return null;
        } else {
            return ColumnConstraint.PRIMARY_KEY;
        }
    }
}
