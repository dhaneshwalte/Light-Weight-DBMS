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
import java.util.function.Predicate;

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
        table.values.add(row);
        saveTable(tableName);
    }

    public Table select(String tableName, List<String> columnNames, 
                        List<Condition> conditions, String logicalOperator) {
        if (!tables.containsKey(tableName)){
            Table table = loadTable(tableName);
            if (table == null){
                //TODO: make custom exception
                System.out.println("Table DNE");
                return null;
            }
            tables.put(tableName, table);
        }
        Table table = tables.get(tableName);
        if (columnNames.isEmpty()){
            columnNames = table.getColumnNames();
        }
        //System.out.println();
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        for (LinkedHashMap<String, Object> row : table.values) {
            if(sastisfyConditions(row, conditions, logicalOperator)){
                LinkedHashMap<String, Object> selectedRow = new LinkedHashMap<>();
                for (String columnName : columnNames) {
                    selectedRow.put(columnName, row.get(columnName));
                }
                result.add(selectedRow);
            }
        }
        return new Table(null, result);
    }

    private boolean sastisfyConditions(LinkedHashMap<String, Object> row, 
                            List<Condition> conditions, String logicalOperator) {
        
        if (conditions.isEmpty()) return true;
        List<Boolean> conditionResults = new ArrayList<>();
        for(Condition condition : conditions){
            String conditionKey = condition.leftOperand;
            String conditionValue = condition.rightOperand;
            if (!row.containsKey(conditionKey)){
                throw new RuntimeException("Where clause key: "+ conditionKey + " Not found");
            }
            String rowValue = (String) row.get(conditionKey);
            conditionResults.add(testCondition(conditionKey, conditionValue, rowValue, condition.operator));
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

    public void delete(String tableName, Map<String, Object> conditions) {
        Table table = select(tableName, null, null, null);
        table.values.removeIf(row -> {
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                if (!entry.getValue().equals(row.get(entry.getKey()))) {
                    return false;
                }
            }
            return true;
        });
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
                List<String> values = new ArrayList<>(Arrays.asList(column.columnName, 
                                                                    column.dataType, 
                                                                    column.columnConstraint == null ? "NONE" : column.columnConstraint.toString()));
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
            table.columns.forEach(column -> columnNames.add(column.columnName));
            String header = String.join(",", columnNames);
            writer.println(header);
            for (Map<String, Object> row : table.values) {
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
