package dal.dmw.w23.models;
import java.util.ArrayList;
import java.util.List;

public class CreateQuery extends Query {
    List<Column> columns;

    
    public CreateQuery() {
        this.columns = new ArrayList<>();
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns (List<Column> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "CreateQuery [columns=" + columns + "] " + super.toString();
    }
    
    
}