package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.expression.literal.LongLiteral;

public class CellTest extends TestCase {
    public void testAsInt() throws Exception {
        assertEquals(6, new LongCell(6).asInt());
        assertEquals(6, new IntegerLiteral(6).valueAsCell().asInt());
        assertEquals(6, new LongLiteral(6).valueAsCell().asInt());
    }

    public void testAsLong() throws Exception {
        assertEquals(6L, new LongCell(6).asLong());
    }

    public void testAsString() throws Exception {
        assertEquals("a", new StringCell("a").asString());
    }
    
    public void testCompare() throws Exception {
        assertComparesEqual(new LongCell(6), new LongCell(6));
        assertLessThan(new LongCell(6), new LongCell(7));
        
        assertComparesEqual(new StringCell("foo"), new StringCell("foo"));
        assertLessThan(new StringCell("11"), new StringCell("5"));

        assertComparesEqual(NullCell.INSTANCE, NullCell.INSTANCE);
        assertLessThan(NullCell.INSTANCE, new StringCell(""));
        assertLessThan(NullCell.INSTANCE, new LongCell(0));
    }
    
    public void testDateVsString() throws Exception {
        assertLessThan(
            new DateCell(2008, 2, 29), new StringCell("2008-03-01"));
        assertLessThan(
            new StringCell("1999-12-31"), new DateCell(2000, 01, 01));
        assertComparesSqlEqual(
            new DateCell(2008, 11, 23), new StringCell("2008-11-23"));
        
        try {
            assertLessThan(
                new DateCell(2008, 2, 29), new StringCell("someday"));
            fail();
        }
        catch (MayflyException e) {
            assertEquals("'someday' is not in format yyyy-mm-dd",
                e.getMessage());
        }
    }

    private void assertComparesEqual(Cell first, Cell second) {
        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
        
        // I think the GROUP BY code makes more sense if
        // compareTo is consistent with equals()
        /* (TODO: Specifically what about the GROUP BY code? It doesn't seem
           right for a StringCell to .equals a DateCell, yet
           they might be sqlEquals.  So what is the impact on GROUP BY?
         */
        assertEquals(first, second);
        assertEquals(second, first);
    }

    private void assertComparesSqlEqual(Cell first, Cell second) {
        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
        
        assertTrue(first.sqlEquals(second));
        assertTrue(second.sqlEquals(first));
    }

    private void assertLessThan(Cell first, Cell second) {
        {
            int comparison = first.compareTo(second);
            assertTrue("expected <0 but was " + comparison, comparison < 0);
        }

        {
            int comparison = second.compareTo(first);
            assertTrue("expected >0 but was " + comparison, comparison > 0);
        }
    }
    
    public void testSqlEquals() throws Exception {
        assertSqlEqual(new LongCell(6), new LongCell(6));
        assertNotSqlEqual(new LongCell(6), new LongCell(7));
        
        assertSqlEqual(new StringCell("foo"), new StringCell("foo"));
        assertNotSqlEqual(new StringCell("11"), new StringCell("5"));

        assertNotSqlEqual(NullCell.INSTANCE, NullCell.INSTANCE);
        assertNotSqlEqual(NullCell.INSTANCE, new StringCell(""));
        assertNotSqlEqual(NullCell.INSTANCE, new LongCell(0));
    }

    private void assertSqlEqual(Cell first, Cell second) {
        assertTrue(first.sqlEquals(second));
        assertTrue(second.sqlEquals(first));
    }

    private void assertNotSqlEqual(Cell first, Cell second) {
        assertFalse(first.sqlEquals(second));
        assertFalse(second.sqlEquals(first));
    }

    public void testDisplayName() throws Exception {
        assertEquals("string 'foo'", new StringCell("foo").displayName());
        assertEquals("string 'don''t'", new StringCell("don't").displayName());
        assertEquals("number -5", new LongCell(-5).displayName());
        assertEquals("null", NullCell.INSTANCE.displayName());
    }

}
