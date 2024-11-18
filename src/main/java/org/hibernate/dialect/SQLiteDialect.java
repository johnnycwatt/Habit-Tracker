package org.hibernate.dialect;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.type.StringType;
import java.sql.Types;

public class SQLiteDialect extends Dialect {

    private static final String INTEGER_TYPE = "integer";

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public SQLiteDialect() {
        initializeDialect();
    }

    // Private helper method to encapsulate the initialization logic
    private void initializeDialect() {
        // Registering column types
        doRegisterColumnTypes();

        // Registering SQL functions
        doRegisterFunctions();
    }

    // Encapsulate column type registration in a private method
    private void doRegisterColumnTypes() {
        registerColumnType(Types.BIT, INTEGER_TYPE);
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.INTEGER, INTEGER_TYPE);
        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.VARCHAR, "varchar");
        registerColumnType(Types.BINARY, "blob");
        registerColumnType(Types.BOOLEAN, INTEGER_TYPE);
    }

    // Encapsulate function registration in a private method
    private void doRegisterFunctions() {
        registerFunction("concat", new VarArgsSQLFunction(StringType.INSTANCE, "", "||", ""));
        registerFunction("mod", new SQLFunctionTemplate(StringType.INSTANCE, "?1 % ?2"));
        registerFunction("substr", new StandardSQLFunction("substr", StringType.INSTANCE));
        registerFunction("substring", new StandardSQLFunction("substr", StringType.INSTANCE));
    }

    public boolean supportsIdentityColumns() {
        return true;
    }

    public String getIdentityColumnString() {
        return INTEGER_TYPE;
    }

    public String getIdentitySelectString() {
        return "select last_insert_rowid()";
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public String getLimitString(String query, int offset, int limit) {
        return query + (offset > 0 ? " limit " + limit + " offset " + offset : " limit " + limit);
    }

    @Override
    public boolean bindLimitParametersInReverseOrder() {
        return true;
    }

    public boolean supportsTemporaryTables() {
        return true;
    }

    public String getCreateTemporaryTableString() {
        return "create temporary table if not exists";
    }

    public boolean dropTemporaryTableAfterUse() {
        return false;
    }

    @Override
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    @Override
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    @Override
    public String getCurrentTimestampSelectString() {
        return "select current_timestamp";
    }

    @Override
    public String getAddColumnString() {
        throw new UnsupportedOperationException("SQLite does not support the addition of columns through ALTER TABLE");
    }
}
