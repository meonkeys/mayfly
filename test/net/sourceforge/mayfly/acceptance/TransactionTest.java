package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionTest extends SqlTestCase {
    
    public void testAutoCommitDefaultsToTrue() throws Exception {
        assertEquals(true, connection.getAutoCommit());
    }

    public void testCommit() throws Exception {
        connection.setAutoCommit(false);
        execute("create table foo (x integer)");
        execute("insert into foo(x) values(5)");
        connection.commit();
        assertResultSet(new String[] { " 5 " }, query("select x from foo"));
        
        dialect.endTransaction(connection);
    }

    public void testAutoCommitIsPerConnection() throws Exception {
        Connection connection2 = dialect.openAdditionalConnection();
        try {
            connection2.setAutoCommit(false);
            assertEquals(false, connection2.getAutoCommit());
            assertEquals(true, connection.getAutoCommit());
        }
        finally {
            connection2.close();
        }
    }
    
    public void testRollback() throws Exception {
        if (!dialect.haveTransactions()) {
            return;
        }
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

        execute("create table foo (x integer)" +
            dialect.tableTypeForTransactions());
        connection.setAutoCommit(false);
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
        connection.rollback();
        // This will the symptom if using MySQL without InnoDB:
//        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
        assertResultSet(new String[] { }, query("select x from foo"));
        
        dialect.endTransaction(connection);
    }
    
    public void testRollbackAndAutoCommit() throws Exception {
        if (!dialect.haveTransactions()) {
            return;
        }

        execute("create table foo (x integer)");
        execute("insert into foo(x) values(5)");
        execute("insert into foo(x) values(7)");
        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
        
        if (dialect.autoCommitMustBeOffToCallRollback()) {
            try {
                connection.rollback();
                fail();
            }
            catch (SQLException e) {
                assertMessage("auto-commit must be off to call rollback", e);
            }
        }
        else {
            connection.rollback();
        }

        connection.setAutoCommit(false);
        connection.rollback();
        assertResultSet(new String[] { " 5 ", " 7 " }, query("select x from foo"));
        
        dialect.endTransaction(connection);
    }
    
    // Need to deal with whether SET SCHEMA is transactional (see Derby docs)
    
    // setAutoCommit(true) with a transaction in progress
    
    // commit or rollback with a result set, prepared statement, etc open
    // (this is mentioned in Derby docs, I think)
    
    public void testUncommittedInsert() throws Exception {
        if (!dialect.haveTransactions() || dialect.willWaitForWriterToCommit()) {
            return;
        }
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        
        execute("create table foo (x integer)"
            + dialect.tableTypeForTransactions());
        Connection writingConnection = dialect.openAdditionalConnection();
        writingConnection.setAutoCommit(false);
        execute("insert into foo (x) values (5)", writingConnection);
        
        if (dialect.willReadUncommitted()) {
            assertResultSet(new String[] { "5" }, query("select x from foo"));
        }
        else {
            assertResultSet(new String[] { }, query("select x from foo"));
            assertResultSet(new String[] { "5" }, "select x from foo", writingConnection);
        }
        writingConnection.commit();
        assertResultSet(new String[] { "5" }, query("select x from foo"));

        writingConnection.close();
    }

    public void testUncommittedUpdate() throws Exception {
        if (!dialect.haveTransactions() || dialect.willWaitForWriterToCommit()) {
            return;
        }
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        
        execute("create table foo (x integer)" +
            dialect.tableTypeForTransactions());
        execute("insert into foo (x) values (5)");

        Connection writingConnection = dialect.openAdditionalConnection();
        writingConnection.setAutoCommit(false);
        execute("update foo set x = 8 where x = 5", writingConnection);
        
        if (dialect.willReadUncommitted()) {
            assertResultSet(new String[] { "8" }, query("select x from foo"));
        }
        else {
            assertResultSet(new String[] { "5" }, query("select x from foo"));
            assertResultSet(new String[] { "8" }, "select x from foo", writingConnection);
        }
        writingConnection.commit();
        assertResultSet(new String[] { "8" }, query("select x from foo"));

        writingConnection.close();
    }
    
    public void testTwoInsertsGetMerged() throws Exception {
        if (dialect.willWaitForWriterToCommitOnTwoRowInserts()) {
            return;
        }
        execute("create table foo (x integer)");
        
        Connection connection2 = dialect.openAdditionalConnection();

        connection.setAutoCommit(false);
        connection2.setAutoCommit(false);
        execute("insert into foo (x) values (5)");
        execute("insert into foo (x) values (7)", connection2);
        
        connection.commit();
        connection2.commit();
        
        assertResultSet(new String[] { "5", "7" }, query ("select x from foo"));
        dialect.endTransaction(connection);
        connection2.close();
    }
    
    public void testReadIsNotRepeatable() throws Exception {
        checkRepeatableRead(false, Connection.TRANSACTION_READ_COMMITTED);
    }
    
    public void testReadIsRepeatable() throws Exception {
        checkRepeatableRead(dialect.canProvideRepeatableRead(), 
            Connection.TRANSACTION_REPEATABLE_READ);
    }

    /** transaction 1 reads (or just starts a transaction? what triggers start?)
     * transaction 2 updates a row that 1 has already read
     * transaction 1 re-reads
     * So is the update seen/not-seen? ("repeatable read" property)
     */
    private void checkRepeatableRead(
        boolean expectRepeatableRead, int isolationLevel) 
    throws Exception {
        if (!dialect.haveTransactions() || dialect.willWaitForWriterToCommit()) {
            /* I think the willWaitForWriteToCommit issue is that connection2
               will be waiting for connection to complete.  So it isn't
               "wait for writer" but "wait for reader which has a lock" */
            return;
        }
        execute("create table foo (x integer)" + 
            dialect.tableTypeForTransactions());
        execute("insert into foo(x) values(5)");
        
        connection.setTransactionIsolation(isolationLevel);
        /* Note that the concept of a commit in this case applies even to
           a transaction which is only reading. */
        connection.setAutoCommit(false);
        Connection connection2 = dialect.openAdditionalConnection();

        // Doesn't seem to matter what connection2's transaction isolation is.
//        connection2.setTransactionIsolation(
//            Connection.TRANSACTION_REPEATABLE_READ);

        try {
            // transaction 1 reads (or just starts a transaction? what triggers start?)
            assertResultSet(new String[] { "5" }, query("select x from foo"));
            
            // transaction 2 updates a row that 1 has already read
            execute("update foo set x = 8", connection2);
    
            // transaction 1 re-reads
            // So is the update seen/not-seen? ("repeatable read" property)
            assertResultSet(
                new String[] { 
                    expectRepeatableRead ? "5" : "8"
                }, query("select x from foo"));
            
            connection.commit();
            assertResultSet(new String[] { "8" }, query("select x from foo"));
        }
        finally {
            dialect.endTransaction(connection);
            connection2.close();
        }
    }

    // two connections - uncommitted update seen/not-seen by other
    // uncommitted create table seen/not-seen by other
    // uncommitted create schema seen/not-seen by other

    /* transaction 1 reads
     * transaction 2 inserts a row
     * transaction 1 re-reads
     * So is the insert seen/not-seen
     * ("phantom read" property)
     */
    
    // Multiple writes -- see hypersonic documentation for whether we
    // give an exception when two transactions commit a change to the
    // same row.
    
    // Conflict/merge situation - we have two commits (or a commit and
    // a rollback).  One is an ALTER TABLE; one is an update to a row in that table.
    // Hypersonic documentation discusses this.
    
    // SET CONSTRAINTS IMMEDIATE and SET CONSTRAINTS DEFERRED
    // Does IMMEDIATE mean a racy kind of thing where it might matter
    // what some other transaction does?  Or is it just a way of saying
    // that we check against what is visible in our own transaction, and
    // then check again on commit?
    
    public void testForUpdate() throws Exception {
        execute("create table foo(x integer)");
        execute("insert into foo(x) values(5)");
        String selectForUpdate = "select x from foo for update";
        if (dialect.haveForUpdate()) {
            assertResultSet(new String[] { "5" }, 
                query(selectForUpdate));
    
            /* I think here we have a second connection
               try to UPDATE FOO SET X = 6, or delete the row,
               and the second
               connection should fail or block or something.
             */
    
            execute("update foo set x = 7");
            assertResultSet(new String[] { " 7 " }, query("select x from foo"));
        }
        else {
            expectQueryFailure(
                selectForUpdate, "expected end of file but got FOR");
        }
    }

    public static void assertResultSet(String[] expectedRows, String sql, Connection connection)
    throws SQLException {
        Statement myStatement = connection.createStatement();
        assertResultSet(expectedRows, myStatement.executeQuery(sql));
        myStatement.close();
    }
    
}
