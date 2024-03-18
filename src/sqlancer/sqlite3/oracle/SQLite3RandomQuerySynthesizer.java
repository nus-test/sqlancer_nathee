package sqlancer.sqlite3.oracle;

import java.util.ArrayList;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.sqlite3.SQLite3GlobalState;
import sqlancer.sqlite3.ast.SQLite3Constant;
import sqlancer.sqlite3.ast.SQLite3Expression;
import sqlancer.sqlite3.ast.SQLite3Select;
import sqlancer.sqlite3.ast.SQLite3Select.SelectType;
import sqlancer.sqlite3.ast.SQLite3SetClause;
import sqlancer.sqlite3.ast.SQLite3SetClause.SQLite3ClauseType;
import sqlancer.sqlite3.ast.SQLite3WindowFunction;
import sqlancer.sqlite3.ast.SQLite3WindowFunctionExpression;
import sqlancer.sqlite3.ast.SQLite3WindowFunctionExpression.SQLite3FrameSpecExclude;
import sqlancer.sqlite3.ast.SQLite3WindowFunctionExpression.SQLite3FrameSpecKind;
import sqlancer.sqlite3.ast.SQLite3WindowFunctionExpression.SQLite3WindowFunctionFrameSpecBetween;
import sqlancer.sqlite3.ast.SQLite3WindowFunctionExpression.SQLite3WindowFunctionFrameSpecTerm;
import sqlancer.sqlite3.ast.SQLite3WindowFunctionExpression.SQLite3WindowFunctionFrameSpecTerm.SQLite3WindowFunctionFrameSpecTermKind;
import sqlancer.sqlite3.gen.SQLite3Common;
import sqlancer.sqlite3.gen.SQLite3ExpressionGenerator;
import sqlancer.sqlite3.gen.SQLite3TypedExpressionGenerator;
import sqlancer.sqlite3.schema.SQLite3DataType;
import sqlancer.sqlite3.schema.SQLite3Schema;
import sqlancer.sqlite3.schema.SQLite3Schema.SQLite3Table;
import sqlancer.sqlite3.schema.SQLite3Schema.SQLite3Tables;

public final class SQLite3RandomQuerySynthesizer {

    private SQLite3RandomQuerySynthesizer() {
    }

    // TODO join clauses
    // TODO union, intersect
    public static SQLite3Expression generate(SQLite3GlobalState globalState, int size) {
        Randomly r = globalState.getRandomly();
        SQLite3Schema s = globalState.getSchema();
        SQLite3Tables targetTables = s.getRandomTableNonEmptyTables();
        List<SQLite3Expression> expressions = new ArrayList<>();
        SQLite3TypedExpressionGenerator gen = new SQLite3TypedExpressionGenerator(globalState)
                .setColumns(s.getTables().getColumns());
                SQLite3TypedExpressionGenerator whereClauseGen = new SQLite3TypedExpressionGenerator(globalState);
                SQLite3TypedExpressionGenerator aggregateGen = ((SQLite3TypedExpressionGenerator) new SQLite3TypedExpressionGenerator(globalState)
                .setColumns(s.getTables().getColumns())).allowAggregateFunctions();

        // SELECT
        SQLite3Select select = new SQLite3Select();
        // DISTINCT or ALL
        select.setSelectType(Randomly.fromOptions(SelectType.values()));
        for (int i = 0; i < size; i++) {
            if (Randomly.getBooleanWithRatherLowProbability()) {
                SQLite3Expression baseWindowFunction;
                boolean normalAggregateFunction = Randomly.getBoolean();
                if (!normalAggregateFunction) {
                    baseWindowFunction = SQLite3WindowFunction.getRandom(targetTables.getColumns(), globalState);
                } else {
                    gen.allowNullValue(false);
                    baseWindowFunction = gen.getAggregateFunction(true);
                    gen.allowNullValue(true);
                    assert baseWindowFunction != null;
                }
                SQLite3WindowFunctionExpression windowFunction = new SQLite3WindowFunctionExpression(
                        baseWindowFunction);
                if (Randomly.getBooleanWithRatherLowProbability() && normalAggregateFunction) {
                    windowFunction.setFilterClause(gen.generateExpression(SQLite3DataType.BOOLEAN));
                }
                if (Randomly.getBooleanWithRatherLowProbability()) {
                    windowFunction.setOrderBy(gen.generateOrderBys());
                }
                if (Randomly.getBooleanWithRatherLowProbability()) {
                    windowFunction.setPartitionBy(gen.getRandomExpressions(Randomly.fromOptions(SQLite3DataType.values()), Randomly.smallNumber()));
                }
                if (Randomly.getBooleanWithRatherLowProbability()) {
                    windowFunction.setFrameSpecKind(SQLite3FrameSpecKind.getRandom());
                    SQLite3Expression windowFunctionTerm;
                    if (Randomly.getBoolean()) {
                        windowFunctionTerm = new SQLite3WindowFunctionFrameSpecTerm(
                                Randomly.fromOptions(SQLite3WindowFunctionFrameSpecTermKind.UNBOUNDED_PRECEDING,
                                        SQLite3WindowFunctionFrameSpecTermKind.CURRENT_ROW));
                    } else if (Randomly.getBoolean()) {
                        windowFunctionTerm = new SQLite3WindowFunctionFrameSpecTerm(SQLite3Constant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()),
                                SQLite3WindowFunctionFrameSpecTermKind.EXPR_PRECEDING);
                    } else {
                        SQLite3WindowFunctionFrameSpecTerm left = getTerm(true, gen);
                        SQLite3WindowFunctionFrameSpecTerm right = getTerm(false, gen);
                        windowFunctionTerm = new SQLite3WindowFunctionFrameSpecBetween(left, right);
                    }
                    windowFunction.setFrameSpec(windowFunctionTerm);
                    // if (Randomly.getBoolean()) {
                    //     windowFunction.setExclude(SQLite3FrameSpecExclude.getRandom());
                    // }
                }
                expressions.add(windowFunction);
            } else {
                expressions.add(aggregateGen.generateExpression(SQLite3DataType.INT));
            }
        }
        select.setFetchColumns(expressions);
        List<SQLite3Table> tables = targetTables.getTables();
        if (Randomly.getBooleanWithRatherLowProbability()) {
            // JOIN ... (might remove tables)
            select.setJoinClauses(gen.getRandomJoinClauses(tables));
        }
        // FROM ...
        select.setFromList(SQLite3Common.getTableRefs(tables, s));
        // TODO: no values are referenced from this sub query yet
        // if (Randomly.getBooleanWithSmallProbability()) {
        // select.getFromList().add(SQLite3RandomQuerySynthesizer.generate(globalState, Randomly.smallNumber() + 1));
        // }

        // WHERE
        if (Randomly.getBoolean()) {
            select.setWhereClause(whereClauseGen.generateExpression(SQLite3DataType.BOOLEAN));
        }
        boolean groupBy = Randomly.getBooleanWithRatherLowProbability();
        if (groupBy) {
            // GROUP BY
            gen.allowNullValue(false);
            select.setGroupByClause(gen.getRandomExpressions(Randomly.fromOptions(SQLite3DataType.values()), Randomly.smallNumber() + 1));
            gen.allowNullValue(true);
            if (Randomly.getBoolean()) {
                // HAVING
                select.setHavingClause(aggregateGen.generateExpression(SQLite3DataType.BOOLEAN));
            }
        }
        boolean orderBy = Randomly.getBooleanWithRatherLowProbability();
        if (orderBy) {
            // ORDER BY
            select.setOrderByExpressions(gen.generateOrderBys());
        }
        if (Randomly.getBooleanWithRatherLowProbability()) {
            // LIMIT
            select.setLimitClause(SQLite3Constant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()));
            if (Randomly.getBoolean()) {
                // OFFSET
                select.setOffsetClause(SQLite3Constant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()));
            }
        }
        if (!orderBy && !groupBy && Randomly.getBooleanWithSmallProbability()) {
            return new SQLite3SetClause(select, generate(globalState, size), SQLite3ClauseType.getRandom());
        }
        return select;
    }

    private static SQLite3WindowFunctionFrameSpecTerm getTerm(boolean isLeftTerm, SQLite3TypedExpressionGenerator gen) {
        if (Randomly.getBoolean()) {
            SQLite3Expression expr = SQLite3Constant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger());
            SQLite3WindowFunctionFrameSpecTermKind kind = Randomly.fromOptions(
                    SQLite3WindowFunctionFrameSpecTermKind.EXPR_FOLLOWING,
                    SQLite3WindowFunctionFrameSpecTermKind.EXPR_PRECEDING);
            return new SQLite3WindowFunctionFrameSpecTerm(expr, kind);
        } else if (Randomly.getBoolean()) {
            return new SQLite3WindowFunctionFrameSpecTerm(SQLite3WindowFunctionFrameSpecTermKind.CURRENT_ROW);
        } else {
            if (isLeftTerm) {
                return new SQLite3WindowFunctionFrameSpecTerm(
                        SQLite3WindowFunctionFrameSpecTermKind.UNBOUNDED_PRECEDING);
            } else {
                return new SQLite3WindowFunctionFrameSpecTerm(
                        SQLite3WindowFunctionFrameSpecTermKind.UNBOUNDED_FOLLOWING);
            }
        }
    }

}
