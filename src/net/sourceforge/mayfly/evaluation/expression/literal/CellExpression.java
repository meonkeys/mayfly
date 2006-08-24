package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;

public class CellExpression extends Literal {

    private final Cell value;

    public CellExpression(Cell value) {
        this.value = value;
    }

    public boolean sameExpression(Expression other) {
//        return false;
        throw new UnimplementedException();
    }

    public String displayName() {
        return value.displayName();
    }

    public Cell valueAsCell() {
        return value;
    }

}