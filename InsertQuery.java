import java.util.ArrayList;
import java.util.List;

public class InsertQuery extends Query{
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
