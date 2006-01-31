package net.sourceforge.mayfly.ldbc;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCellContent;

import java.util.Arrays;
import java.util.Collections;

public class InsertTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Arrays.asList(new String[] {"a", "b"}),
                Arrays.asList(new Object[] {new Long(5), "Value"})
            ),
            Command.fromSql("insert into foo (a, b) values (5, 'Value')")
        );
    }
    
    public void testParseNull() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                Arrays.asList(new String[] {"a"}),
                Arrays.asList(new Object[] {NullCellContent.INSTANCE})
            ),
            Command.fromSql("insert into foo (a) values (null)")
        );
    }
    
    public void testParseAll() throws Exception {
        assertEquals(
            new Insert(
                new InsertTable("foo"),
                null,
                Arrays.asList(new Object[] {new Long(5)})
            ),
            Command.fromSql("insert into foo values (5)")
        );
    }
    
}
