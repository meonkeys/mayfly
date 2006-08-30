package net.sourceforge.mayfly.acceptance;

import net.sourceforge.mayfly.UnimplementedException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is for MySQL 4.x.  For 5.x, see {@link net.sourceforge.mayfly.acceptance.MySqlDialect}.
 * 
 * To make this work, install MySQL (the server), start it up on localhost,
 * and that might be all you need...
 */
public class MySql4Dialect extends Dialect {

    public Connection openConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection bootstrapConnection = DriverManager.getConnection("jdbc:mysql://localhost/");
        SqlTestCase.execute("CREATE DATABASE mayflytest", bootstrapConnection);
        bootstrapConnection.close();

        return openAdditionalConnection();
    }

    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/mayflytest");
    }

    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("DROP DATABASE mayflytest", connection);
        connection.close();
    }
    
    public boolean backslashInAStringIsAnEscape() {
        return true;
    }
    
    public boolean canQuoteIdentifiers() {
        return false;
    }
    
    public boolean isReservedWord(String word) {
        return word.equalsIgnoreCase("if");
    }

    public boolean verticalBarsMeanConcatenation() {
        return false;
    }
    
    public boolean tableNamesMightBeCaseSensitive() {
        // Whether table names are case sensitive in MySQL depends on whether
        // file names are.
        return true;
    }
    
    public boolean crossJoinCanHaveOn() {
        return true;
    }
    
    public boolean innerJoinRequiresOn() {
        return false;
    }

    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    public boolean considerTablesMentionedAfterJoin() {
        return true;
    }
    
    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return false;
    }
    
    public boolean stringComparisonsAreCaseInsensitive() {
        return true;
    }
    
    public boolean notBindsMoreTightlyThanIn() {
        return true;
    }

    public boolean notRequiresBoolean() {
        return false;
    }

    public boolean canHaveLimitWithoutOrderBy() {
        return true;
    }
    
    public boolean canOrderByExpression() {
        return true;
    }
    
    public boolean fromIsOptional() {
        return true;
    }
    
    public boolean maySpecifyTableDotColumnToJdbc() {
        return true;
    }
    
    public boolean schemasMissing() {
        // Not something missing in MySQL so much as something we haven't figured
        // out how to test.
        return true;
    }
    
    public boolean aggregateDistinctIsForCountOnly() {
        return true;
    }
    
    public boolean canSumStrings() {
        return true;
    }
    
    public boolean errorIfNotAggregateOrGrouped() {
        return false;
    }
    
    public boolean disallowColumnAndAggregateInExpression() {
        return false;
    }
    
    public boolean columnInHavingMustAlsoBeInSelect() {
        return true;
    }
    
    public boolean canHaveHavingWithoutGroupBy() {
        return true;
    }

    public boolean canGetValueViaExpressionName() {
        return true;
    }
    
    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    public boolean disallowNullOnRightHandSideOfIn() {
        return false;
    }

    public boolean allowMultipleNullsInUniqueColumn() {
        return true;
    }
    
    public boolean haveTransactions() {
        // If we could make sure we were using InnoDB, this
        // perhaps could be true.  At least for now, don't
        // worry about trying to make sure we have & use InnoDB.
        return false;
    }

    public String databaseTypeForForeignKeys() {
        return " type=innodb";
    }
    
    public boolean onDeleteSetDefaultMissing(boolean tableCreateTime) {
        // I'm guessing this is like MySQL5, but this is unconfirmed.
        throw new UnimplementedException();
    }
    
    public boolean haveDropTableFooIfExists() {
        return false;
    }
    
    public boolean allowJdbcParameterAsDefault() {
        // I guess this fits along with "from foo?.tab"
        // and other looseness allowed with ?
        return true;
    }
    
    public boolean notNullImpliesDefaults() {
        // An odd (though documented) quirk of MySQL:
        // declaring a field NOT NULL changes its
        // default value from NULL to some other
        // value (0, '', etc).
        return true;
    }
    
    public boolean haveAutoUnderbarIncrement() {
        return true;
    }
    
    public boolean haveSerial() {
        return true;
    }
    
    public boolean autoIncrementIsRelativeToLastValue() {
        return true;
    }

    public boolean datesAreOff() {
        // MySQL4 is presumed to be like MySQL5, but this is unconfirmed.
        throw new UnimplementedException();
    }

}
