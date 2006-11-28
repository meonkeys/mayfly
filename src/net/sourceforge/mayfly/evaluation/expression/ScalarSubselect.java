package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.evaluation.select.RowEvaluator;
import net.sourceforge.mayfly.evaluation.select.Select;

public class ScalarSubselect extends Expression {

    private final Select select;

    public ScalarSubselect(Select select) {
        this.select = select;
    }

    public Cell aggregate(ResultRows rows) {
        throw new UnimplementedException();
    }

    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        Evaluator innerEvaluator = new RowEvaluator(row, evaluator);
        return select.select(innerEvaluator, null).scalar(select.location);
    }

    public boolean sameExpression(Expression other) {
        throw new UnimplementedException();
    }

    public String displayName() {
        throw new UnimplementedException();
    }

}