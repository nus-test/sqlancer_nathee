package sqlancer.mysql.gen;

import java.sql.SQLException;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.common.gen.AbstractUpdateGenerator;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.mysql.MySQLErrors;
import sqlancer.mysql.MySQLGlobalState;
import sqlancer.mysql.MySQLSchema.MySQLColumn;
import sqlancer.mysql.MySQLSchema.MySQLDataType;
import sqlancer.mysql.MySQLSchema.MySQLTable;
import sqlancer.mysql.MySQLVisitor;

public class MySQLUpdateGenerator extends AbstractUpdateGenerator<MySQLColumn> {

    private final MySQLGlobalState globalState;
    private MySQLTypedExpressionGenerator gen;

    public MySQLUpdateGenerator(MySQLGlobalState globalState) {
        this.globalState = globalState;
    }

    public static SQLQueryAdapter create(MySQLGlobalState globalState) throws SQLException {
        return new MySQLUpdateGenerator(globalState).generate();
    }

    private SQLQueryAdapter generate() throws SQLException {
        MySQLTable table = globalState.getSchema().getRandomTable(t -> !t.isView());
        List<MySQLColumn> columns = table.getRandomNonEmptyColumnSubset();
        gen = new MySQLTypedExpressionGenerator(globalState).setColumns(table.getColumns());
        sb.append("UPDATE ");
        sb.append(table.getName());
        sb.append(" SET ");
        updateColumns(columns);
        if (Randomly.getBoolean()) {
            sb.append(" WHERE ");
            MySQLErrors.addExpressionErrors(errors);
            sb.append(MySQLVisitor.asString(gen.generateExpression(MySQLDataType.BOOLEAN)));
        }
        errors.add("doesn't have a default value");
        errors.add("Data truncation");
        errors.add("Incorrect integer value");
        errors.add("Duplicate entry");
        errors.add("Data truncated for functional index");
        errors.add("Data truncated for column");
        errors.add("cannot be null");
        errors.add("Incorrect decimal value");

        return new SQLQueryAdapter(sb.toString(), errors);
    }

    @Override
    protected void updateValue(MySQLColumn column) {
        if (Randomly.getBoolean()) {
            sb.append(gen.generateConstant(column.getType()));
        } else if (Randomly.getBoolean()) {
            sb.append("DEFAULT");
        } else {
            sb.append(MySQLVisitor.asString(gen.generateExpression(column.getType())));
        }
    }

}
