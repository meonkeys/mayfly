package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

import java.util.*;

public class In extends BooleanExpression {

    private WhatElement leftSide;
	private List expressions;

    public In(WhatElement leftSide, List expressions) {
		this.leftSide = leftSide;
		this.expressions = expressions;
    }

	public boolean evaluate(Object rowObject) {
        Row row = (Row) rowObject;
        Cell cell = leftSide.evaluate(row);

        for (Iterator iter = expressions.iterator(); iter.hasNext();) {
			Literal element = (Literal) iter.next();
			if (element.matchesCell(cell)) {
                return true;
			}
		}
		return false;
	}

    public String firstAggregate() {
        String firstInLeft = leftSide.firstAggregate();
        if (firstInLeft != null) {
            return firstInLeft;
        }

        for (int i = 0; i < expressions.size(); ++i) {
            WhatElement element = (WhatElement) expressions.get(i);
            String first = element.firstAggregate();
            if (first != null) {
                return first;
            }
        }
        
        return null;
    }

}
