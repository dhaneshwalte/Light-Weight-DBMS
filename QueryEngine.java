import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryEngine {
    DbService dbService;
    QueryEngine(){
        dbService = new DbService();
    }
    public void executeQuery(String Sql){
        SqlParser sqlParser = new SqlParser(Sql);
        Query query = sqlParser.parse();
        System.out.println(query);
        if (query.getQueryType() == QueryType.CREATE){
            CreateQuery createQuery = (CreateQuery) query;
            dbService.createTable(createQuery.getTableName(), createQuery.getColumns());
        } else if (query.getQueryType() == QueryType.SELECT){
            //Table table = dbService.select(query.getTableName(), query.getColumns());
            //filterWhereCondition(table, query.getConditions());
        } else if (query.getQueryType() == QueryType.INSERT){
            InsertQuery insertQuery = (InsertQuery) query;
            Table table = dbService.getTable(query.getTableName());
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            for(String columnName: table.getColumnNames()){
                row.put(columnName, null);
            }
            List<String> queryColumnNames = insertQuery.getColumnNames();
            List<String> queryColumnValues = insertQuery.getValues();
            //This loop wont run if column names are not provided.
            for (int i = 0; i < queryColumnNames.size(); i++){
                row.put(queryColumnNames.get(i), queryColumnValues.get(i));
            }
            //If column names are provided but number of values dont match
            if (!queryColumnNames.isEmpty() 
                && insertQuery.getValues().size() != insertQuery.getColumnNames().size()){
                    throw new RuntimeException("Insufficient Values");
                }
            if (queryColumnNames.isEmpty()){
                if (insertQuery.getValues().size() != table.getColumnNames().size()){
                    throw new RuntimeException("Insufficient Values");
                }
                for(int i = 0; i < insertQuery.getValues().size(); i++){
                    row.put(table.getColumnNames().get(i), insertQuery.getValues().get(i));
                }
            }
            if (verifyDataTypes(table.getColumns(), row)){
                dbService.insert(insertQuery.getTableName(), row);
            } else {
                System.out.println("Data Type Mismatch");
            }
        }
    }
    private boolean verifyDataTypes(List<Column> columns, Map<String, Object> row) {
        return true;
    }
    private void filterWhereCondition(Table table, List<Condition> conditions) {
        Condition firsCondition = conditions.get(0);
    }   
}