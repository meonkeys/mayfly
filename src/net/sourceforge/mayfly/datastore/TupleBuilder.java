package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

public class TupleBuilder {
    
    private L elements = new L();

    public TupleBuilder append(TupleElement tuple) {
        elements.append(tuple);
        return this;
    }

    public TupleBuilder append(CellHeader column, Cell cell) {
        return append(new TupleElement(column, cell));
    }
    
    public TupleBuilder appendAll(Tuple elementsToAdd) {
        elements.addAll(elementsToAdd);
        return this;
    }

    public TupleBuilder appendColumnCellContents(String tableName, String columnName, Object cellValue) {
        Cell cell = Cell.fromContents(cellValue);
        return appendColumnCell(tableName, columnName, cell);
    }

    public TupleBuilder appendColumnCell(String tableName, String columnName, Cell cell) {
        return append(new Column(tableName, columnName), cell);
    }

    public TupleBuilder appendColumnCellContents(String columnName, Object cellValue) {
        Cell cell = Cell.fromContents(cellValue);
        return appendColumnCell(columnName, cell);
    }

    public TupleBuilder appendColumnCell(String columnName, Cell cell) {
        return append(new Column(columnName), cell);
    }

    public Tuple asTuple() {
        return new Tuple(elements.asImmutable());
    }

}
