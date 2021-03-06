package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.evaluation.expression.CurrentTimestampExpression;
import net.sourceforge.mayfly.evaluation.expression.literal.Literal;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.parser.Location;

/**
 * @internal
 * Expressions should be immutable.  I think that is probably true now
 * (certainly it is the intention, as they go in the store).
 */
abstract public class Expression extends WhatElement {
    
    /* Storing a location here is dubious because an expression can
       go into the store. */
    public final Location location;
    
    protected Expression(Location location) {
        this.location = location;
    }
    
    protected Expression() {
        this(Location.UNKNOWN);
    }

    @Override
    public Selected selected(ResultRow dummyRow) {
        return new Selected(resolve(dummyRow, Evaluator.NO_SUBSELECT_NEEDED));
    }

    public String firstColumn() {
        return null;
    }
    
    public String firstAggregate() {
        return null;
    }
    
    final public boolean isAggregate() {
        return firstAggregate() != null;
    }

    public Cell evaluate(ResultRow row) {
        return evaluate(row, Evaluator.NO_SUBSELECT_NEEDED);
    }

    abstract public Cell evaluate(ResultRow row, Evaluator evaluator);

    final public Cell evaluate(Row row, String table) {
        return evaluate(new ResultRow(row, table));
    }
    
    public void check(ResultRow row) {
    }

    abstract public Cell aggregate(ResultRows rows);

    abstract public boolean sameExpression(Expression other);

    public final Expression resolve(ResultRow row) {
        return resolve(row, Evaluator.NO_SUBSELECT_NEEDED);
    }
    
    public Expression resolve(ResultRow row, Evaluator evaluator) {
        return this;
    }
    
    public static String firstAggregate(Expression left, Expression right) {
        String firstInLeft = left.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }
        return right.firstAggregate();
    }

    public final boolean matches(Expression expression) {
        return expression.sameExpression(this);
    }

    public boolean matches(String columnName) {
        return false;
    }

    /**
     * @internal
     * Return a representation of this expression suitable for including in
     * a database dump.  Currently only needed, or implemented, for expressions
     * which can be default values (or on-update values), like {@link Literal} or 
     * {@link CurrentTimestampExpression}.
     */
    public String asSql() {
        return evaluate((ResultRow) null).asSql();
    }

}
