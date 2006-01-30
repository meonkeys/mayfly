package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.evaluation.Expression;

public class QuotedString extends Literal {
    private final String stringInQuotes;

    public QuotedString(String stringInQuotes) {
        this.stringInQuotes = stringInQuotes;
    }

    private String stringWithoutQuotes() {
        String withoutQuotes = stringInQuotes.substring(1, stringInQuotes.length()-1);
        return withoutQuotes.replaceAll("''", "'");
    }

    public Object valueForCellContentComparison() {
        return stringWithoutQuotes();
    }

    protected Cell valueAsCell() {
        return new StringCell(stringWithoutQuotes());
    }
    
    public String displayName() {
        return stringInQuotes;
    }

    public boolean sameExpression(Expression other) {
        if (other instanceof QuotedString) {
            QuotedString string = (QuotedString) other;
            return stringInQuotes == string.stringInQuotes;
        }
        else {
            return false;
        }
    }

}
