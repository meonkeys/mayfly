package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.util.*;

public class And extends BooleanExpression {
    private BooleanExpression leftSide;
    private BooleanExpression rightSide;


    public static And fromAndTree(Tree andTree, TreeConverters treeConverters) {
        L both = andTree.children().convertUsing(treeConverters);
        return new And((BooleanExpression)both.get(0), (BooleanExpression)both.get(1));
    }


    public And(BooleanExpression leftSide, BooleanExpression rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public boolean evaluate(Object candidate) {
        return leftSide.evaluate(candidate) && rightSide.evaluate(candidate);
    }

    public int parameterCount() {
        return leftSide.parameterCount() + rightSide.parameterCount();
    }

}
