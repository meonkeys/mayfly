package net.sourceforge.mayfly.acceptance;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * There is a question of whether our JDBC metadata should somehow be
 * limited to reflect what is portable against various JDBC drivers.
 * For now, it is merely limited by what we've gotten around to
 * implementing.
 * 
 * As for the general question of how portable DatabaseMetaData is,
 * there are issues.  See Hypersonic sources for example.
 */
public class MetaDataTest extends SqlTestCase {

    public void testMetaData() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        if (!dialect.wishThisWereTrue()) {
            assertFalse(metaData.supportsUnion());
        }
        else {
            assertTrue(metaData.supportsUnion());
        }
    }
 
    public void xtestNoTables() throws Exception {
        // not sure about null vs "" for schema, catalog.
        // But null is clearly what hypersonic wants for "across all schemas/catalogs".
        ResultSet tables = connection.getMetaData().getTables(null, null, "", null);
        assertFalse(tables.next());
        tables.close();
    }

    public void xtestOneTable() throws Exception {
        Statement statement = connection.createStatement();
        assertEquals(0, statement.executeUpdate("create table foo (a integer)"));
        statement.close();
        connection.commit();

        ResultSet tables = connection.getMetaData().getTables(null, null, "%", null);
        assertTrue("first row", tables.next());
        assertEquals("foo", tables.getString("TABLE_NAME"));
        assertFalse("no second row", tables.next());
        tables.close();
    }
    
    public void testListColumnsWithLowercaseSearch() throws Exception {
        execute("create table foo(a integer, b integer)");
        ResultSet columns = connection.getMetaData().getColumns(
            null, null, "foo", "a");
        if (dialect.metaDataExpectsUppercase()) {
            assertFalse(columns.next());
        }
        else {
            assertTrue(columns.next());
            assertEquals("a", columns.getString("COLUMN_NAME"));
            assertFalse(columns.next());
        }
    }

    public void testListColumnsMixedCase() throws Exception {
        execute("create table FOO(a integer, b integer)");
        ResultSet columns = connection.getMetaData().getColumns(
            null, null, "FOO", "A");
        if (!dialect.metaDataProblemWithUppercaseTableName()) {
            assertTrue(columns.next());
            if (dialect.metaDataExpectsUppercase()) {
                assertEquals("A", columns.getString("COLUMN_NAME"));
            }
            else {
                assertEquals("a", columns.getString("COLUMN_NAME"));
            }
        }
        assertFalse(columns.next());
    }

    public void testListColumnsSearchIsUppercase() throws Exception {
        execute("create table FOO(A integer, b integer)");
        ResultSet columns = connection.getMetaData().getColumns(
            null, null, "FOO", "A");
        if (!dialect.metaDataProblemWithUppercaseTableName()) {
            assertTrue(columns.next());
            assertEquals("A", columns.getString("COLUMN_NAME"));
        }
        assertFalse(columns.next());
    }

    public void testListColumnsNoColumns() throws Exception {
        execute("create table foo(a integer, b integer)");
        ResultSet columns = connection.getMetaData().getColumns(
            null, null, "FOO", "C");
        assertFalse(columns.next());
    }

    public void testListColumnsNoTable() throws Exception {
        execute("create table foo(a integer, b integer)");
        ResultSet columns = connection.getMetaData().getColumns(
            null, null, "NO_SUCH_TABLE", "A");
        assertFalse(columns.next());
    }
    
    public void testDatabaseName() throws Exception {
        assertEquals(
            dialect.productName(), 
            connection.getMetaData().getDatabaseProductName());
    }
    
}
