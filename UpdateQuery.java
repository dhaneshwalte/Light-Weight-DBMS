import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateQuery extends Query{
    Map<String,Object> data;

    public UpdateQuery() {
        this.data = new LinkedHashMap<>();
    }
    public Map<String,Object> getData() {
        return data;
    }

    public void setData(Map<String,Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UpdateQuery [data=" + data + "] " + super.toString();
    }
}
