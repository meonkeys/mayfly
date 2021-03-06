package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.util.ImmutableList;

public class DataStoreTest extends TestCase {
    
    public void testAddRowWithSchemaAndColumnNames() throws Exception {
        DataStore store = new DataStore()
            .addSchema("mars", new Schema().createTable("foo", "x"));
        DataStore newStore = store.addRow(
            new TableReference("mars", "foo"), 
            ImmutableList.singleton("x"), ValueList.singleton(new LongCell(5)),
            new NullChecker());
        assertEquals(1, newStore.table("mars", "foo").rowCount());
    }

}
