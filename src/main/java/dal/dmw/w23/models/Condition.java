package dal.dmw.w23.models;

/**
 * Class that represents a condition of the WHERE clause.
 */
public class Condition{
    /**
     * leftOperand of the where conditon
     */
    String leftOperand;
    /**
     * rightOperand of the where condition
     */
    String rightOperand;
    /**
     * operator of the where condition
     */
    String operator;
    
    public Condition(String leftOperand, String rightOperand, String operator) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.operator = operator;
    }
    
    public String getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(String leftOperand) {
        this.leftOperand = leftOperand;
    }

    public String getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(String rightOperand) {
        this.rightOperand = rightOperand;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "Condition [leftOperand=" + leftOperand + ", rightOperand=" + rightOperand + ", operator=" + operator
                + "]";
    }
    
}