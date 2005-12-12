package net.sourceforge.mayfly.ldbc.what;

import net.sourceforge.mayfly.datastore.*;

public class Count extends AggregateExpression {

    public Count(SingleColumn column, String functionName) {
        super(column, functionName);
    }

    protected Cell pickOne(Cell min, Cell max, Cell count, Cell sum, Cell average) {
        return count;
    }

}