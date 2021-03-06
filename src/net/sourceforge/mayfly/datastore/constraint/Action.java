package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.condition.Equal;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.CellExpression;
import net.sourceforge.mayfly.util.ImmutableList;

import java.io.IOException;
import java.io.Writer;

public abstract class Action {

    abstract public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, 
        String referencerColumn, 
        TableReference targetTable, String targetColumn);

    abstract public DataStore handleUpdate(Cell oldValue, Cell newValue, 
        DataStore store, String referencerSchema, 
        String referencerTable, 
        String referencerColumn, TableReference targetTable, String targetColumn);

    protected DataStore setValue(Cell oldValue, Expression valueToAssign, 
        DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn) {
        UpdateStore update = store.update(referencerSchema, referencerTable,
            ImmutableList.singleton(
                new SetClause(referencerColumn, valueToAssign)), 
            where(oldValue, referencerTable, referencerColumn),
            //Location.UNKNOWN,
            new Options()
        );
        return update.store();
    }

    protected Condition where(Cell oldValue, String referencerTable, String referencerColumn) {
        return new Equal(
            new SingleColumn(referencerTable, referencerColumn),
            new CellExpression(oldValue)
        );
    }

    abstract public void dump(Writer out) throws IOException;

}
