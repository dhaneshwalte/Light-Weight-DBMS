package dal.dmw.w23.models;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents the INSERT Query
 */
public class InsertQuery extends Query{
    /**
     * List of values to be inserted
     */
    List<String> values;

    public InsertQuery() {
        this.values = new ArrayList<>();
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "InsertQuery [values=" + values + "] " + super.toString();
    }
    
}
