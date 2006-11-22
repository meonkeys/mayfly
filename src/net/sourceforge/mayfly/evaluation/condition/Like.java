package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;

import java.util.regex.Pattern;

public class Like extends RowExpression {

    public Like(Expression left, Expression pattern) {
        super(left, pattern);
    }

    protected boolean compare(Cell left, Cell right) {
        if (left instanceof NullCell || right instanceof NullCell) {
            return false;
        }
        String candidate = left.asString();
        String pattern = right.asString();
        return compare(candidate, pattern);
    }

    static boolean compare(String candidate, String pattern) {
        String regex = 
            quote(pattern)
                .replaceAll("%", ".*")
                .replaceAll("_", ".")
                ;
        return Pattern.matches(regex, candidate);
    }

    static String quote(String in) {
        return in
            .replace("\\", "\\\\")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace(".", "\\.")
            .replace("$", "\\$")
            .replace("^", "\\^")
            .replace("?", "\\?")
            .replace("*", "\\*")
            .replace("+", "\\+")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("|", "\\|")
            .replace("(", "\\(")
            .replace(")", "\\)")
            ;
    }

}
