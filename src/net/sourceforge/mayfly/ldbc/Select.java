package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.parser.*;

import java.sql.*;
import java.util.*;

public class Select extends ValueObject {

    public static Select fromTree(Tree selectTree) {

        int[] typesToIgnore = new int[]{SQLTokenTypes.COMMA};

        L converted =
            selectTree.children().convertUsing(TreeConverters.forSelectTree(), typesToIgnore);

        Where where =
            converted.selectObjectsThatAre(Where.class).size() > 0 ?
                    (Where) converted.selectObjectThatIs(Where.class) :
                    new Where();

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new Froms(converted.selectObjectsThatAre(From.class)),
                where
            );
    }


    private What what;
    private Froms froms;
    private Where where;

    public Select(What what, Froms froms, Where where) {
        this.what = what;
        this.froms = froms;
        this.where = where;
    }

    public Froms from() {
        return froms;
    }

    public ResultSet select(final DataStore store) throws SQLException {
        Columns columns = what.selectedColumns();
        List columnNames = columns.asNames();
        checkColumns(store, columnNames);
        return new MyResultSet(columns, executeOn(store));
    }

    private void checkColumns(final DataStore store, List columns) throws SQLException {
        // This method could use some unit testing.
        // And also a comparison to make sure its rules correspond to executeOn
        Set possibleColumnNames = new HashSet();
        for (Iterator iter = froms.tableNames().iterator(); iter.hasNext();) {
            String tableName = (String) iter.next();
            possibleColumnNames.addAll(store.table(tableName).columns());
        }
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            String columnName = (String) iter.next();
            if (!possibleColumnNames.contains(new Column(columnName))) {
                throw new SQLException("no column " + columnName);
            }
        }
    }

    public Rows executeOn(DataStore store) throws SQLException {
        L tableNames = froms.tableNames();

        Iterator iterator = tableNames.iterator();
        String firstTable = (String) iterator.next();
        Rows joinedRows = store.table(firstTable).rows();
        while (iterator.hasNext()) {
            String tableName = (String) iterator.next();
            joinedRows = (Rows)joinedRows.cartesianJoin(store.table(tableName).rows());
        }

        return (Rows) joinedRows.select(where);
    }
}
