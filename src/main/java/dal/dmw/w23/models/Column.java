package dal.dmw.w23.models;

/**
 * Class to represent a Column of a table
 */
public class Column{
    /**
     * Name of the Column
     */
    String columnName;
    /**
     * Datatype of the column
     */
    String dataType;
    /**
     * Constraint to be imposed on the column, (UNIQUE, NOT NULL etc)
     */
    ColumnConstraint columnConstraint;
    public Column(String columnName, String dataType, ColumnConstraint columnConstraint) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.columnConstraint = columnConstraint;
    }
    
    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public ColumnConstraint getColumnConstraint() {
        return columnConstraint;
    }

    public void setColumnConstraint(ColumnConstraint columnConstraint) {
        this.columnConstraint = columnConstraint;
    }

    @Override
    public String toString() {
        return "ColumnDefinition [columnName=" + columnName + ", dataType=" + dataType + ", columnConstraints=" + columnConstraint
                + "] ";
    }
}
