import java.util.List;
import java.util.Map;

public class Table {
    List<String> columns;
    List<Map<String, Object>> values;

    public Table(List<String> columns, List<Map<String, Object>> values) {
        this.columns = columns;
        this.values = values;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getValues() {
        return values;
    }

    public void setValues(List<Map<String, Object>> values) {
        this.values = values;
    }
}
