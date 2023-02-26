package dal.dmw.w23;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dal.dmw.w23.models.Column;
import dal.dmw.w23.models.CreateQuery;
import dal.dmw.w23.models.DeleteQuery;
import dal.dmw.w23.models.InsertQuery;
import dal.dmw.w23.models.Query;
import dal.dmw.w23.models.QueryType;
import dal.dmw.w23.models.SelectQuery;
import dal.dmw.w23.models.Table;
import dal.dmw.w23.models.UpdateQuery;


public class QueryEngine {
    DbService dbService;
    QueryEngine(){
        dbService = new DbService();
    }
    public void executeQuery(String Sql){
        SqlParser sqlParser = new SqlParser(Sql);
        Query query = sqlParser.parse();
        if (query.getQueryType() == QueryType.CREATE){
            CreateQuery createQuery = (CreateQuery) query;
            dbService.createTable(createQuery.getTableName(), createQuery.getColumns());
        } else if (query.getQueryType() == QueryType.SELECT){
            SelectQuery selectQuery = (SelectQuery) query;
            //TODO: Check valid operators for the conditions
            Table result = dbService.select(selectQuery.getTableName(), 
                                            selectQuery.getColumnNames(), 
                                            selectQuery.getConditions(),
                                            selectQuery.getLogicalOperator());
            
            StringBuilder seperator = new StringBuilder("-");
            for(int i = 0; i < 16*result.getColumnNames().size(); i++) seperator.append("-");
            System.out.println(seperator);
            int index = 0;
            for(String columnName: result.getColumnNames()){
                if (index == 0) System.out.print("|");
                String paddedColumn = String.format("%-15s", columnName);
                System.out.print(paddedColumn+"|");
                index++;
            }
            System.out.println();
            System.out.println(seperator);
            for(Map<String, Object> row: result.getValues()){
                index=0;
                for(Map.Entry<String, Object> entry: row.entrySet()){
                    if (index == 0) System.out.print("|");
                    String paddedValue = String.format("%-15s", entry.getValue());
                    System.out.print(paddedValue+"|");
                    index++;
                }
                System.out.println();
            }
            System.out.println(seperator);
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
        } else if (query.getQueryType() == QueryType.UPDATE){
            UpdateQuery updateQuery = (UpdateQuery) query;
            Table table = dbService.getTable(updateQuery.getTableName());
            if (table == null){
                throw new RuntimeException("Table does not exist");
            }
            if (verifyDataTypes(table.getColumns(), updateQuery.getData())){
                dbService.update(updateQuery.getTableName(), 
                                 updateQuery.getData(), 
                                 updateQuery.getConditions(), 
                                 updateQuery.getLogicalOperator());
            }
            
        } else if (query.getQueryType() == QueryType.DELETE){
            DeleteQuery deleteQuery = (DeleteQuery) query;
            System.out.println(deleteQuery);
            //TODO: Verify data types in the set and integrity
            dbService.delete(deleteQuery.getTableName(),
                             deleteQuery.getConditions(), 
                             deleteQuery.getLogicalOperator());
        }
    }
    private boolean verifyDataTypes(List<Column> columns, Map<String, Object> row) {
        for(Map.Entry<String, Object> entry: row.entrySet()){
            //Skip if the value is null
            if (entry.getValue() == null) continue;
            for(Column column: columns){
                if (column.getColumnName().equals(entry.getKey())){
                    if (column.getDataType().toLowerCase().startsWith("varchar")){
                        int openBraceIndex = column.getDataType().toLowerCase().indexOf("[");
                        int closeBraceIndex = column.getDataType().toLowerCase().indexOf("]");
                        int varcharLength = Integer.parseInt(column.getDataType().substring(openBraceIndex+1, closeBraceIndex));
                        //System.out.println(varcharLength);
                        if (entry.getValue().toString().length() > varcharLength){
                            System.out.println("Length exceeds");
                            return false;
                        }
                    }
                    else {
                        try{
                            Integer.parseInt(entry.getValue().toString());
                            return true;
                        }
                        catch(NumberFormatException e){
                            System.out.println("Invalid value for INT");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
