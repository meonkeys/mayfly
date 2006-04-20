package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.evaluation.command.UpdateSchema;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.ldbc.where.Where;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableMap;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A data store is an immutable object containing data
 * and metadata for a set of tables.
 * 
 * <p>You typically get one via {@link net.sourceforge.mayfly.Database#dataStore()}
 * and then pass it to {@link net.sourceforge.mayfly.Database#Database(DataStore)}.</p>
 * 
 * @internal
 * As with the rest of our immutable objects, all fields should
 * be final and have types which are immutable (like String,
 * Double, ImmutableList, long, etc).
 */
public class DataStore {

    public static final String ANONYMOUS_SCHEMA_NAME = "";
    public static final CaseInsensitiveString ANONYMOUS_SCHEMA = new CaseInsensitiveString(ANONYMOUS_SCHEMA_NAME);

    private final ImmutableMap namedSchemas;
    
    public DataStore() {
        this(new Schema());
    }

    public DataStore(Schema anonymousSchema) {
        this(new ImmutableMap().with(ANONYMOUS_SCHEMA, anonymousSchema));
    }

    private DataStore(ImmutableMap namedSchemas) {
        this.namedSchemas = namedSchemas;
    }

    public DataStore addSchema(String newSchemaName, Schema newSchema) {
        if (schemaExists(newSchemaName)) {
            throw new MayflyException("schema " + newSchemaName + " already exists");
        }
        return new DataStore(namedSchemas.with(new CaseInsensitiveString(newSchemaName), newSchema));
    }

    public DataStore replace(String newSchemaName, Schema newSchema) {
        if (schemaExists(newSchemaName)) {
            return new DataStore(namedSchemas.with(new CaseInsensitiveString(newSchemaName), newSchema));
        } else {
            throw new MayflyInternalException("no schema " + newSchemaName);
        }
    }

    private boolean schemaExists(String newSchemaName) {
        return namedSchemas.containsKey(new CaseInsensitiveString(newSchemaName));
    }

    public Schema schema(String schema) {
        if (schemaExists(schema)) {
            return (Schema) namedSchemas.get(new CaseInsensitiveString(schema));
        }
        throw new MayflyException("no schema " + schema);
    }

    public Schema anonymousSchema() {
        return (Schema) namedSchemas.get(ANONYMOUS_SCHEMA);
    }
    
    public DataStore dropTable(String schema, String table) {
        return replace(schema, schema(schema).dropTable(table));
    }

    public TableData table(String schema, String table) {
        return schema(schema).table(table);
    }

    public TableData table(String table) {
        return anonymousSchema().table(table);
    }

    public Set tables(String schema) {
        return schema(schema).tables();
    }

    public DataStore addRow(String schema, String table, List columnNames, List values) {
        return replace(schema, schema(schema).addRow(table, columnNames, values));
    }

    public DataStore addRow(String schema, String table, List values) {
        return replace(schema, schema(schema).addRow(table, values));
    }

    public Set schemas() {
        Set names = new TreeSet();
        for (Iterator iter = namedSchemas.keySet().iterator(); iter.hasNext();) {
            CaseInsensitiveString key = (CaseInsensitiveString) iter.next();
            if (!key.equals(ANONYMOUS_SCHEMA)) {
                names.add(key.getString());
            }
        }
        return names;
    }

    public UpdateStore update(String schema, String table, List setClauses, Where where) {
        UpdateSchema result = schema(schema).update(table, setClauses, where);
        return replaceSchema(schema, result);
    }

    public UpdateStore delete(String schema, String table, Where where) {
        UpdateSchema result = schema(schema).delete(table, where);
        return replaceSchema(schema, result);
    }

    private UpdateStore replaceSchema(String schema, UpdateSchema result) {
        return new UpdateStore(replace(schema, result.schema()), result.rowsAffected());
    }

}
