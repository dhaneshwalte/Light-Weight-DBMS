package dal.dmw.w23.models;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that represents the UpdateQuery
 */
public class UpdateQuery extends Query{
    /**
     * Data to be updated, provided in the UpdateQuery
     */
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
