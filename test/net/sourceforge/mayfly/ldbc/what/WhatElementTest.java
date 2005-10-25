package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.*;

public class WhatElementTest extends TestCase {

    public void testSingleColumn() {
        WhatElement element = new SingleColumnExpression(new Column("table", "col"));
        Columns columns = element.columns();
        assertEquals(1, columns.size());
        Column column = (Column) columns.asImmutableList().get(0);
        assertEquals("table", column.table());
        assertEquals("col", column.columnName());
    }

}
