import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateQuery extends Query{
    List<Map<String,Object>> data;

    public UpdateQuery() {
        this.data = new ArrayList<>();
    }
    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UpdateQuery [data=" + data + "] " + super.toString();
    }
}
