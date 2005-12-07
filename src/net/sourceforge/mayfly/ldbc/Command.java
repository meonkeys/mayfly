package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public abstract class Command extends ValueObject {

    public static Command fromTree(Tree tree) {
        switch (tree.getType()) {
        case SQLTokenTypes.DROP_TABLE:
            return DropTable.dropTableFromTree(tree);
        case SQLTokenTypes.CREATE_TABLE:
            return CreateTable.createTableFromTree(tree);
        case SQLTokenTypes.INSERT:
            return Insert.insertFromTree(tree);
        case SQLTokenTypes.SELECT:
            return Select.selectFromTree(tree);
        default:
            throw new UnimplementedException("Unrecognized command " + tree);
        }
    }
    
    abstract public void substitute(Collection jdbcParameters);

    abstract public int parameterCount();
    
    abstract public DataStore update(DataStore store);

    abstract public int rowsAffected();

}