package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Columns extends Aggregate {
    public static Columns fromColumnNames(final String tableName, List columnNameStrings) {
        if (tableName == null) {
            throw new NullPointerException("must pass table to fromColumnNames");
        }

        L columnList =
            new L(columnNameStrings)
                .collect(
                    new Transformer() {
                        public Object transform(Object from) {
                            return new Column(tableName, (String) from);
                        }
                    }
                );

        return new Columns(columnList.asImmutable());
    }


    private final ImmutableList columns;

    public Columns(ImmutableList columns) {
        this.columns = columns;
    }

    protected Aggregate createNew(Iterable items) {
        return new Columns(new L().addAll(items).asImmutable());
    }

    public Iterator iterator() {
        return columns.iterator();
    }

    public L asNames() {
        return collect(new ToName());
    }

    public List asLowercaseNames() {
        return collect(new ToLowercaseName());
    }


    static class ToName implements Transformer {
        public Object transform(Object from) {
            return ((Column)from).columnName();
        }
    }

    static class ToLowercaseName implements Transformer {
        public Object transform(Object from) {
            return ((Column)from).columnName().toLowerCase();
        }
    }

    public ImmutableList asImmutableList() {
        return columns;
    }

    public Column get(int index) {
        return (Column) columns.get(index);
    }

    public Column columnFromName(String columnName) {
        return columnFromName(null, columnName);
    }

    public Column columnFromName(String tableOrAlias, String columnName) {
        Column found = null;
        for (Iterator iter = columns.iterator(); iter.hasNext(); ) {
            Column column = (Column) iter.next();
            if (column.matches(tableOrAlias, columnName)) {
                if (found != null) {
                    throw new MayflyException("ambiguous column " + columnName);
                } else {
                    found = column;
                }
            }
        }
        if (found == null) {
            throw new MayflyException("no column " + Column.displayName(tableOrAlias, columnName));
        } else {
            return found;
        }
    }

    public void checkForDuplicates() {
        Set names = new HashSet();
        for (Iterator iter = iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            if (!names.add(column.columnName().toLowerCase())) {
                throw new MayflyException("duplicate column " + column.columnName());
            }
        }
    }

}
