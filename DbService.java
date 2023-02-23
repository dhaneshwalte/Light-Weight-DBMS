import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbService {
    String dataDirectory = "database/dbname/";
    private Map<String, Table> tables;

    public DbService() {
        tables = new HashMap<>();
    }

    public void createTable(String tableName, List<String> columns) {
        tables.put(tableName, new Table(columns, new ArrayList<>()));
        saveTable(tableName);
    }

    public void insert(String tableName, Map<String, Object> row) {
        Table table = tables.get(tableName);
        table.values.add(row);
        saveTable(tableName);
    }

    public Table select(String tableName, List<String> columns) {
        if (!tables.containsKey(tableName)){
            Table table = load(tableName);
            if (table == null){
                //TODO: make custom exception
                System.out.println("Table DNE");
                return null;
            }
            tables.put(tableName, table);
        }
        Table table = tables.get(tableName);
        if (columns == null){
            return tables.get(tableName);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : table.values) {
            Map<String, Object> selectedRow = new HashMap<>();
            for (String column : columns) {
                selectedRow.put(column, row.get(column));
            }
            result.add(selectedRow);
        }
        return new Table(columns, result);
    }

    public void delete(String tableName, Map<String, Object> conditions) {
        Table table = select(tableName, null);
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
            String header = String.join(",", table.columns);
            writer.println(header);
            for (Map<String, Object> row : table.values) {
                List<String> values = new ArrayList<>();
                for (Object value : row.values()) {
                    values.add(value.toString());
                }
                String line = String.join(",", values);
                writer.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Table load(String file) {
        String tablePath = dataDirectory + file + ".csv";
        File tableFile = new File(tablePath);
        if (!tableFile.exists()){
            System.out.println("Table does not exist");
            return null;
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> headersList = null;
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(tablePath);
            bufferedReader = new BufferedReader(reader);
            String[] headers = bufferedReader.readLine().split(",");
            headersList = Arrays.asList(headers);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                int totalColumns = values.length;
                Map<String, Object> row = new HashMap<>();
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
        return new Table(headersList, rows);
    }
}
