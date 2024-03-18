package sqlancer.sqlite3.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.gen.TypedExpressionGenerator;
import sqlancer.sqlite3.SQLite3GlobalState;
import sqlancer.sqlite3.ast.SQLite3Aggregate;
import sqlancer.sqlite3.ast.SQLite3Aggregate.SQLite3AggregateFunction;
import sqlancer.sqlite3.ast.SQLite3Case.CasePair;
import sqlancer.sqlite3.ast.SQLite3Case.SQLite3CaseWithBaseExpression;
import sqlancer.sqlite3.ast.SQLite3Case.SQLite3CaseWithoutBaseExpression;
import sqlancer.sqlite3.ast.SQLite3Constant;
import sqlancer.sqlite3.ast.SQLite3Constant.SQLite3TextConstant;
import sqlancer.sqlite3.ast.SQLite3Expression;
import sqlancer.sqlite3.ast.SQLite3Expression.BinaryComparisonOperation.BinaryComparisonOperator;
import sqlancer.sqlite3.ast.SQLite3Expression.CollateOperation;
import sqlancer.sqlite3.ast.SQLite3Expression.Join;
import sqlancer.sqlite3.ast.SQLite3Expression.Join.JoinType;
import sqlancer.sqlite3.ast.SQLite3Expression.MatchOperation;
import sqlancer.sqlite3.ast.SQLite3Expression.SQLite3ColumnName;
import sqlancer.sqlite3.ast.SQLite3Expression.SQLite3Distinct;
import sqlancer.sqlite3.ast.SQLite3Expression.SQLite3OrderingTerm;
import sqlancer.sqlite3.ast.SQLite3Expression.SQLite3OrderingTerm.Ordering;
import sqlancer.sqlite3.ast.SQLite3Expression.SQLite3PostfixUnaryOperation;
import sqlancer.sqlite3.ast.SQLite3Expression.SQLite3PostfixUnaryOperation.PostfixUnaryOperator;
import sqlancer.sqlite3.ast.SQLite3Expression.Sqlite3BinaryOperation;
import sqlancer.sqlite3.ast.SQLite3Expression.Sqlite3BinaryOperation.BinaryOperator;
import sqlancer.sqlite3.ast.SQLite3Expression.TypeLiteral;
import sqlancer.sqlite3.ast.SQLite3Function;
import sqlancer.sqlite3.ast.SQLite3Function.ComputableFunction;
import sqlancer.sqlite3.ast.SQLite3UnaryOperation.UnaryOperator;
import sqlancer.sqlite3.gen.SQLite3ExpressionGenerator.ExpressionType;
import sqlancer.sqlite3.ast.SQLite3UnaryOperation;
import sqlancer.sqlite3.schema.SQLite3DataType;
import sqlancer.sqlite3.schema.SQLite3Schema.SQLite3Column;
import sqlancer.sqlite3.schema.SQLite3Schema.SQLite3Column.SQLite3CollateSequence;
import sqlancer.sqlite3.schema.SQLite3Schema.SQLite3RowValue;
import sqlancer.sqlite3.schema.SQLite3Schema.SQLite3Table;

public class SQLite3TypedExpressionGenerator extends TypedExpressionGenerator<SQLite3Expression, SQLite3Column, SQLite3DataType> {

    private SQLite3RowValue rw;
    private final SQLite3GlobalState globalState;
    private boolean tryToGenerateKnownResult;
    private List<SQLite3Column> columns = Collections.emptyList();
    private final Randomly r;
    private boolean deterministicOnly;
    private boolean allowMatchClause;
    private boolean allowAggregateFunctions;
    private boolean allowSubqueries;
    private boolean allowAggreates;
    private boolean allowNullValue;

    public SQLite3TypedExpressionGenerator(SQLite3TypedExpressionGenerator other) {
        this.rw = other.rw;
        this.globalState = other.globalState;
        this.tryToGenerateKnownResult = other.tryToGenerateKnownResult;
        this.columns = new ArrayList<>(other.columns);
        this.r = other.r;
        this.deterministicOnly = other.deterministicOnly;
        this.allowMatchClause = other.allowMatchClause;
        this.allowAggregateFunctions = other.allowAggregateFunctions;
        this.allowSubqueries = other.allowSubqueries;
        this.allowAggreates = other.allowAggreates;
        this.allowNullValue = true;
    }

    public SQLite3TypedExpressionGenerator(SQLite3GlobalState globalState) {
        this.globalState = globalState;
        this.r = globalState.getRandomly();
    }

    public SQLite3TypedExpressionGenerator deterministicOnly() {
        SQLite3TypedExpressionGenerator gen = new SQLite3TypedExpressionGenerator(this);
        gen.deterministicOnly = true;
        return gen;
    }

    public SQLite3TypedExpressionGenerator allowAggregateFunctions() {
        SQLite3TypedExpressionGenerator gen = new SQLite3TypedExpressionGenerator(this);
        gen.allowAggregateFunctions = true;
        return gen;
    }

    public SQLite3TypedExpressionGenerator setRowValue(SQLite3RowValue rw) {
        SQLite3TypedExpressionGenerator gen = new SQLite3TypedExpressionGenerator(this);
        gen.rw = rw;
        return gen;
    }

    public SQLite3TypedExpressionGenerator allowMatchClause() {
        SQLite3TypedExpressionGenerator gen = new SQLite3TypedExpressionGenerator(this);
        gen.allowMatchClause = true;
        return gen;
    }

    public SQLite3TypedExpressionGenerator allowSubqueries() {
        SQLite3TypedExpressionGenerator gen = new SQLite3TypedExpressionGenerator(this);
        gen.allowSubqueries = true;
        return gen;
    }

    public SQLite3TypedExpressionGenerator tryToGenerateKnownResult() {
        SQLite3TypedExpressionGenerator gen = new SQLite3TypedExpressionGenerator(this);
        gen.tryToGenerateKnownResult = true;
        return gen;
    }

    public static SQLite3Expression getRandomLiteralValue(SQLite3GlobalState globalState, SQLite3DataType type) {
        return new SQLite3TypedExpressionGenerator(globalState).generateConstant(type);
    }

    public List<SQLite3Expression> generateOrderBys() {
        List<SQLite3Expression> expressions = new ArrayList<>();
        for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
            expressions.add(generateOrderingTerm());
        }
        return expressions;
    }

    public List<Join> getRandomJoinClauses(List<SQLite3Table> tables) {
        List<Join> joinStatements = new ArrayList<>();
        if (!globalState.getDbmsSpecificOptions().testJoins) {
            return joinStatements;
        }
        List<JoinType> options = new ArrayList<>(Arrays.asList(JoinType.values()));
        if (Randomly.getBoolean() && tables.size() > 1) {
            int nrJoinClauses = (int) Randomly.getNotCachedInteger(0, tables.size());
            // Natural join is incompatible with other joins
            // because it needs unique column names
            // while other joins will produce duplicate column names
            if (nrJoinClauses > 1) {
                options.remove(JoinType.NATURAL);
            }
            for (int i = 0; i < nrJoinClauses; i++) {
                SQLite3Expression joinClause = generateExpression(SQLite3DataType.BOOLEAN);
                SQLite3Table table = Randomly.fromList(tables);
                tables.remove(table);
                JoinType selectedOption = Randomly.fromList(options);
                if (selectedOption == JoinType.NATURAL) {
                    // NATURAL joins do not have an ON clause
                    joinClause = null;
                }
                Join j = new SQLite3Expression.Join(table, joinClause, selectedOption);
                joinStatements.add(j);
            }

        }
        return joinStatements;
    }

    public SQLite3Expression generateOrderingTerm() {
        if (columns.isEmpty()) {
            throw new IgnoreMeException();
        }
        SQLite3Column column = Randomly.fromList(columns);
        SQLite3Expression expr = new SQLite3ColumnName(column, rw == null ? null : rw.getValues().get(column));
        // COLLATE is potentially already generated
        if (Randomly.getBoolean()) {
            expr = new SQLite3OrderingTerm(expr, Ordering.getRandomValue());
        }
        // if (globalState.getDbmsSpecificOptions().testNullsFirstLast && Randomly.getBoolean()) {
        //     expr = new SQLite3PostfixText(expr, Randomly.fromOptions(" NULLS FIRST", " NULLS LAST"),
        //             null /* expr.getExpectedValue() */) {
        //         @Override
        //         public boolean omitBracketsWhenPrinting() {
        //             return true;
        //         }
        //     };
        // }
        return expr;
    }

    public SQLite3Expression generateExpression(SQLite3DataType type) {
        return generateExpression(type, 0);
    }

    public List<SQLite3Expression> getRandomExpressions(SQLite3DataType type, int size) {
        List<SQLite3Expression> expressions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            expressions.add(generateExpression(type));
        }
        return expressions;
    }

    public List<SQLite3Expression> getRandomExpressions(SQLite3DataType type, int size, int depth) {
        List<SQLite3Expression> expressions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            expressions.add(generateExpression(type, depth));
        }
        return expressions;
    }

    private enum BooleanExpression {
        RANDOM_QUERY, UNARY_OPERATOR, POSTFIX_UNARY_OPERATOR, MATCH, AND_OR_CHAIN,
        BETWEEN_OPERATOR, BINARY_COMPARISON_OPERATOR, IN_OPERATOR, CASE_OPERATOR;
    }

    private SQLite3Expression generateBooleaExpression(int depth) {
        List<BooleanExpression> list = new ArrayList<>(Arrays.asList(BooleanExpression.values()));
        if (!allowMatchClause) {
            list.remove(BooleanExpression.MATCH);
        }
        if (!allowSubqueries) {
            list.remove(BooleanExpression.RANDOM_QUERY);
        }
        if (!globalState.getDbmsSpecificOptions().testMatch) {
            list.remove(BooleanExpression.MATCH);
        }
        if (!globalState.getDbmsSpecificOptions().testIn) {
            list.remove(BooleanExpression.IN_OPERATOR);
        }
        BooleanExpression option = Randomly.fromList(list);
        switch (option) {
            case RANDOM_QUERY:
            case UNARY_OPERATOR:
                SQLite3Expression subExpression = getRandomExpression(SQLite3DataType.BOOLEAN, depth + 1);
                return new SQLite3UnaryOperation(UnaryOperator.NOT, subExpression);
            case POSTFIX_UNARY_OPERATOR:
                return getRandomPostfixUnaryOperator(depth + 1);
            case MATCH:
                return getMatchClause(depth + 1);
            case AND_OR_CHAIN:
                return getAndOrChain(depth + 1);
            case BETWEEN_OPERATOR:
                return getBetweenOperator(depth + 1);
            case BINARY_COMPARISON_OPERATOR:
                return getBinaryComparisonOperator(depth + 1);
            case IN_OPERATOR:
                return getInOperator(depth + 1);
            case CASE_OPERATOR:
                allowNullValue = false;
                SQLite3Expression expression = getCaseOperator(SQLite3DataType.BOOLEAN, depth + 1);
                allowNullValue = true;
                return expression;
            default:
                throw new AssertionError();
        }
    }

    private enum TextExpression {
        RANDOM_QUERY, BINARY_OPERATOR, COLLATE, CAST_EXPRESSION, FUNCTION, CASE_OPERATOR
    }

    private SQLite3Expression generateTextExpression(int depth) {
        List<TextExpression> list = new ArrayList<>(Arrays.asList(TextExpression.values()));
        if (!allowSubqueries) {
            list.remove(TextExpression.RANDOM_QUERY);
        }
        if (!globalState.getDbmsSpecificOptions().testFunctions) {
            list.remove(TextExpression.FUNCTION);
        }
        TextExpression option = Randomly.fromList(list);
        switch (option) {
            case RANDOM_QUERY:
            case FUNCTION:
                return getFunction(SQLite3DataType.TEXT, globalState, depth + 1);
            case BINARY_OPERATOR:
                SQLite3Expression leftExpression = getRandomExpression(SQLite3DataType.TEXT, depth + 1);
                SQLite3Expression rightExpression = getRandomExpression(SQLite3DataType.TEXT, depth + 1);
                return new SQLite3Expression.Sqlite3BinaryOperation(leftExpression, rightExpression, BinaryOperator.CONCATENATE);
            case COLLATE:
                return new CollateOperation(getRandomExpression(SQLite3DataType.TEXT, depth + 1), SQLite3CollateSequence.random());
            case CAST_EXPRESSION:
                return getCastOperator(TypeLiteral.Type.TEXT, depth + 1);
            case CASE_OPERATOR:
                allowNullValue = false;
                SQLite3Expression expression = getCaseOperator(SQLite3DataType.TEXT, depth + 1);
                allowNullValue = true;
                return expression;
            default:
                throw new AssertionError();
        }
    }

    private enum IntExpression {
        RANDOM_QUERY, UNARY_OPERATOR, BINARY_OPERATOR/*, CAST_EXPRESSION*/, AGGREGATE_FUNCTION, FUNCTION, CASE_OPERATOR
    }

    private SQLite3Expression generateIntExpression(int depth) {
        List<IntExpression> list = new ArrayList<>(Arrays.asList(IntExpression.values()));
        if (!allowAggregateFunctions) {
            list.remove(IntExpression.AGGREGATE_FUNCTION);
        }
        if (!allowSubqueries) {
            list.remove(IntExpression.RANDOM_QUERY);
        }
        if (!globalState.getDbmsSpecificOptions().testFunctions) {
            list.remove(IntExpression.FUNCTION);
        }
        IntExpression option = Randomly.fromList(list);
        switch (option) {
            case RANDOM_QUERY:
            case FUNCTION:
                return getFunction(SQLite3DataType.INT, globalState, depth + 1);
            case UNARY_OPERATOR:
                List<UnaryOperator> options = new ArrayList<>(Arrays.asList(UnaryOperator.values()));
                options.remove(UnaryOperator.NOT);
                UnaryOperator unaryOperator = Randomly.fromList(options);
                SQLite3Expression subExpression = getRandomExpression(SQLite3DataType.INT, depth + 1);
                return new SQLite3UnaryOperation(unaryOperator, subExpression);
            case BINARY_OPERATOR:
                SQLite3Expression leftExpression = getRandomExpression(SQLite3DataType.INT, depth + 1);
                List<BinaryOperator> validOptions = new ArrayList<>(Arrays.asList(BinaryOperator.values()));
                validOptions.remove(BinaryOperator.AND);
                validOptions.remove(BinaryOperator.OR);
                validOptions.remove(BinaryOperator.CONCATENATE);
                BinaryOperator operator = Randomly.fromList(validOptions);
                SQLite3Expression rightExpression = getRandomExpression(SQLite3DataType.INT, depth + 1);
                return new SQLite3Expression.Sqlite3BinaryOperation(leftExpression, rightExpression, operator);
            // case CAST_EXPRESSION:
            //     return getCastOperator(TypeLiteral.Type.INTEGER, depth + 1);
            case AGGREGATE_FUNCTION:
                allowNullValue = false;
                SQLite3Expression expr = getAggregateFunction(depth + 1);
                allowNullValue = true;
                return expr;
            case CASE_OPERATOR:
                allowNullValue = false;
                SQLite3Expression expression = getCaseOperator(SQLite3DataType.INT, depth + 1);
                allowNullValue = true;
                return expression;
            default:
                throw new AssertionError();
        }
    }

    public SQLite3Expression getRandomExpression(SQLite3DataType type, int depth) {
        return generateExpression(type, depth);
    }

    @Override
    public SQLite3Expression generateExpression(SQLite3DataType type, int depth) {
        if (allowAggreates && Randomly.getBoolean()) {
            allowNullValue = false;
            SQLite3Expression expr = getAggregateFunction(depth + 1);
            allowNullValue = true;
            return expr;
        }
        if (depth >= globalState.getOptions().getMaxExpressionDepth()) {
            if (Randomly.getBooleanWithRatherLowProbability() || columns.isEmpty()) {
                return generateConstant(type);
            } else {
                return generateColumn(type);
            }
        }
        switch (type) {
            case BOOLEAN:
                return generateBooleaExpression(depth);
            case INT:
                return generateIntExpression(depth);
            case NONE:
            case TEXT:
            // case BINARY:
                return generateTextExpression(depth);
            case REAL:
                return generateLeafNode(type);
            case NULL:
                return generateConstant(type);
            default:
                throw new AssertionError(type);
        }
    }

    private SQLite3Expression getRandomPostfixUnaryOperator(int depth) {
        SQLite3Expression subExpression = getRandomExpression(SQLite3DataType.BOOLEAN, depth + 1);
        PostfixUnaryOperator operator = PostfixUnaryOperator.getRandomOperator();
        return new SQLite3Expression.SQLite3PostfixUnaryOperation(operator, subExpression);
    }

    private SQLite3Expression getMatchClause(int depth) {
        SQLite3Expression left = getRandomExpression(SQLite3DataType.TEXT, depth + 1);
        SQLite3Expression right;
        if (Randomly.getBoolean()) {
            right = getRandomExpression(SQLite3DataType.TEXT, depth + 1);
        } else {
            right = SQLite3Constant.createTextConstant(SQLite3MatchStringGenerator.generateMatchString(r));
        }
        return new MatchOperation(left, right);
    }

    private SQLite3Expression getAndOrChain(int depth) {
        int num = Randomly.smallNumber() + 2;
        SQLite3Expression expr = getRandomExpression(SQLite3DataType.BOOLEAN, depth + 1);
        for (int i = 0; i < num; i++) {
            BinaryOperator operator = Randomly.fromOptions(BinaryOperator.AND, BinaryOperator.OR);
            expr = new Sqlite3BinaryOperation(expr, getRandomExpression(SQLite3DataType.BOOLEAN, depth + 1), operator);
        }
        return expr;
    }

    private SQLite3Expression getBetweenOperator(int depth) {
        boolean tr = Randomly.getBoolean();
        SQLite3DataType type = getRandomType();
        SQLite3Expression expr = getRandomExpression(type, depth + 1);
        SQLite3Expression left = getRandomExpression(type, depth + 1);
        SQLite3Expression right = getRandomExpression(type, depth + 1);
        return new SQLite3Expression.BetweenOperation(expr, tr, left, right);
    }

    private SQLite3Expression getBinaryComparisonOperator(int depth) {
        SQLite3DataType type = getRandomType();
        List<BinaryComparisonOperator> validOptions = new ArrayList<>(Arrays.asList(BinaryComparisonOperator.values()));
        if (type != SQLite3DataType.TEXT && type != SQLite3DataType.NONE/* || type != SQLite3DataType.BINARY*/) {
            validOptions.remove(BinaryComparisonOperator.LIKE);
            validOptions.remove(BinaryComparisonOperator.GLOB);
        }
        // if (type != SQLite3DataType.BOOLEAN) {
        //     validOptions.remove(BinaryComparisonOperator.IS);
        //     validOptions.remove(BinaryComparisonOperator.IS_NOT);
        // }
        SQLite3Expression leftExpression = getRandomExpression(type, depth + 1);
        BinaryComparisonOperator operator = Randomly.fromList(validOptions);
        SQLite3Expression rightExpression = getRandomExpression(type, depth + 1);
        return new SQLite3Expression.BinaryComparisonOperation(leftExpression, rightExpression, operator);
    }

    private SQLite3Expression getInOperator(int depth) {
        SQLite3DataType type = getRandomType();
        SQLite3Expression leftExpression = getRandomExpression(type, depth + 1);
        List<SQLite3Expression> right = new ArrayList<>();
        for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
            right.add(getRandomExpression(type, depth + 1));
        }
        return new SQLite3Expression.InOperation(leftExpression, right);
    }

    private SQLite3Expression getCaseOperator(SQLite3DataType type, int depth) {
        int nrCaseExpressions = 1 + Randomly.smallNumber();
        CasePair[] pairs = new CasePair[nrCaseExpressions];
        for (int i = 0; i < pairs.length; i++) {
            SQLite3Expression whenExpr = getRandomExpression(SQLite3DataType.BOOLEAN, depth + 1);
            SQLite3Expression thenExpr = getRandomExpression(type, depth + 1);
            CasePair pair = new CasePair(whenExpr, thenExpr);
            pairs[i] = pair;
        }
        SQLite3Expression elseExpr;
        if (Randomly.getBoolean()) {
            elseExpr = getRandomExpression(type, depth + 1);
        } else {
            elseExpr = null;
        }
        if (Randomly.getBoolean()) {
            return new SQLite3CaseWithoutBaseExpression(pairs, elseExpr);
        } else {
            SQLite3Expression baseExpr = getRandomExpression(SQLite3DataType.BOOLEAN, depth + 1);
            return new SQLite3CaseWithBaseExpression(baseExpr, pairs, elseExpr);
        }
    }

    private SQLite3Expression getCastOperator(TypeLiteral.Type type, int depth) {
        SQLite3Expression expr = getRandomExpression(getRandomType(), depth + 1);
        return new SQLite3Expression.Cast(new SQLite3Expression.TypeLiteral(type), expr);
    }

    public SQLite3Expression getAggregateFunction(boolean asWindowFunction) {
        SQLite3AggregateFunction random = SQLite3AggregateFunction.getRandom();
        if (asWindowFunction) {
            while (/* random == SQLite3AggregateFunction.ZIPFILE || */random == SQLite3AggregateFunction.MAX
                    || random == SQLite3AggregateFunction.MIN) {
                // ZIPFILE() may not be used as a window function
                random = SQLite3AggregateFunction.getRandom();
            }
        }
        return getAggregate(0, random);
    }

    private SQLite3Expression getAggregateFunction(int depth) {
        SQLite3AggregateFunction random = SQLite3AggregateFunction.getRandom();
        return getAggregate(depth, random);
    }

    private SQLite3Expression getAggregate(int depth, SQLite3AggregateFunction random) {
        int nrArgs;
        // if (random == SQLite3AggregateFunction.ZIPFILE) {
        // nrArgs = Randomly.fromOptions(2, 4);
        // } else {
        // nrArgs = 1;
        // }
        nrArgs = 1;
        return new SQLite3Aggregate(getRandomExpressions(SQLite3DataType.INT, nrArgs, depth + 1), random);
    }

    enum Attribute {
        VARIADIC, NONDETERMINISTIC
    };

    private enum AnyFunction {
        // ABS("ABS", 1), //
        // CHANGES("CHANGES", 0, Attribute.NONDETERMINISTIC), //
        // CHAR("CHAR", 1, Attribute.VARIADIC), //
        // COALESCE("COALESCE", 2, Attribute.VARIADIC), //
        GLOB("GLOB", 2), //
        IFNULL("IFNULL", 2), //
        // HEX("HEX", 1), //
        // INSTR("INSTR", 2), //
        // LAST_INSERT_ROWID("LAST_INSERT_ROWID", 0, Attribute.NONDETERMINISTIC), //
        // LENGTH("LENGTH", 1, SQLite3DataType.INT, SQLite3DataType.TEXT), //
        LIKE("LIKE", 2), //
        LIKE2("LIKE", 3) {
            @Override
            List<SQLite3Expression> generateArguments(int nrArgs, int depth, SQLite3TypedExpressionGenerator gen) {
                List<SQLite3Expression> args = super.generateArguments(nrArgs, depth, gen);
                args.set(2, gen.getRandomSingleCharString());
                return args;
            }
        }, //
        LIKELIHOOD("LIKELIHOOD", 2), //
        LIKELY("LIKELY", 1), //
        LOAD_EXTENSION("load_extension", 1), //
        LOAD_EXTENSION2("load_extension", 2, Attribute.NONDETERMINISTIC),// LOWER("LOWER", 1), //
        LTRIM1("LTRIM", 1, SQLite3DataType.TEXT, SQLite3DataType.TEXT), //
        // LTRIM2("LTRIM", 2), //
        // MAX("MAX", 2, Attribute.VARIADIC), //
        // MIN("MIN", 2, Attribute.VARIADIC), //
        // NULLIF("NULLIF", 2), //
        PRINTF("PRINTF", 1, Attribute.VARIADIC), //
        // QUOTE("QUOTE", 1), //
        ROUND("ROUND", 2, SQLite3DataType.REAL, SQLite3DataType.REAL, SQLite3DataType.INT), //
        RTRIM("RTRIM", 1, SQLite3DataType.TEXT, SQLite3DataType.TEXT); //
        // SOUNDEX("soundex", 1), //
        // SQLITE_COMPILEOPTION_GET("SQLITE_COMPILEOPTION_GET", 1, Attribute.NONDETERMINISTIC), //
        // SQLITE_COMPILEOPTION_USED("SQLITE_COMPILEOPTION_USED", 1, Attribute.NONDETERMINISTIC), //
        // SQLITE_OFFSET(1), //
        // SQLITE_SOURCE_ID("SQLITE_SOURCE_ID", 0, Attribute.NONDETERMINISTIC),
        // SQLITE_VERSION("SQLITE_VERSION", 0, Attribute.NONDETERMINISTIC), //
        // SUBSTR("SUBSTR", 2, SQLite3DataType.TEXT, SQLite3DataType.TEXT, SQLite3DataType.INT); //
        // TOTAL_CHANGES("TOTAL_CHANGES", 0, Attribute.NONDETERMINISTIC), //
        // TRIM("TRIM", 1), //
        // TYPEOF("TYPEOF", 1), //
        /*UNICODE("UNICODE", 1),*/ UNLIKELY("UNLIKELY", 1), //
        // UPPER("UPPER", 1); // "ZEROBLOB"
        // ZEROBLOB("ZEROBLOB", 1),
        DATE("DATE", 3, Attribute.VARIADIC), //
        TIME("TIME", 3, Attribute.VARIADIC), //
        DATETIME("DATETIME", 3, Attribute.VARIADIC), //
        JULIANDAY("JULIANDAY", 3, Attribute.VARIADIC), //
        STRFTIME("STRFTIME", 3, Attribute.VARIADIC),
        // json functions
        JSON("json", 1), //
        JSON_ARRAY("json_array", 2, Attribute.VARIADIC), JSON_ARRAY_LENGTH("json_array_length", 1), //
        JSON_ARRAY_LENGTH2("json_array_length", 2), //
        JSON_EXTRACT("json_extract", 2, Attribute.VARIADIC), JSON_INSERT("json_insert", 3, Attribute.VARIADIC),
        JSON_OBJECT("json_object", 2, Attribute.VARIADIC), JSON_PATCH("json_patch", 2),
        JSON_REMOVE("json_remove", 2, Attribute.VARIADIC), JSON_TYPE("json_type", 1), //
        JSON_VALID("json_valid", 1), //
        JSON_QUOTE("json_quote", 1), //

        RTREENODE("rtreenode", 2),

        // FTS
        HIGHLIGHT("highlight", 4);

        // testing functions
        // EXPR_COMPARE("expr_compare", 2), EXPR_IMPLIES_EXPR("expr_implies_expr", 2);

        // fts5_decode("fts5_decode", 2),
        // fts5_decode_none("fts5_decode_none", 2),
        // fts5_expr("fts5_expr", 1),
        // fts5_expr_tcl("fts5_expr_tcl", 1),
        // fts5_fold("fts5_fold", 1),
        // fts5_isalnum("fts5_isalnum", 1);

        private int minNrArgs;
        private boolean variadic;
        private boolean deterministic;
        private String name;
        private SQLite3DataType returnType;
        private SQLite3DataType[] argTypes;

        AnyFunction(String name, int minNrArgs, Attribute... attributes) {
            this.name = name;
            List<Attribute> attrs = Arrays.asList(attributes);
            this.minNrArgs = minNrArgs;
            this.variadic = attrs.contains(Attribute.VARIADIC);
            this.deterministic = !attrs.contains(Attribute.NONDETERMINISTIC);
        }

        AnyFunction(String name, int minNrArgs, SQLite3DataType returnType, SQLite3DataType... indexType) {
            this.name = name;
            this.minNrArgs = minNrArgs;
            this.returnType = returnType;
            this.argTypes = indexType.clone();
        }

        public boolean isVariadic() {
            return variadic;
        }

        public int getMinNrArgs() {
            return minNrArgs;
        }

        public boolean isCompatibleWithReturnType(SQLite3DataType type) {
            return type == returnType;
        }

        static AnyFunction getRandom(SQLite3GlobalState globalState) {
            return Randomly.fromList(getAllFunctions(globalState));
        }

        private static List<AnyFunction> getAllFunctions(SQLite3GlobalState globalState) {
            List<AnyFunction> functions = new ArrayList<>(Arrays.asList(AnyFunction.values()));
            if (!globalState.getDbmsSpecificOptions().testSoundex) {
                boolean removed = functions.removeIf(f -> f.name.equals("soundex"));
                if (!removed) {
                    throw new IllegalStateException();
                }
            }
            return functions;
        }

        static AnyFunction getRandomDeterministic(SQLite3GlobalState globalState) {
            return Randomly.fromList(
                    getAllFunctions(globalState).stream().filter(f -> f.deterministic).collect(Collectors.toList()));
        }

        @Override
        public String toString() {
            return name;
        }

        List<SQLite3Expression> generateArguments(int nrArgs, int depth, SQLite3TypedExpressionGenerator gen) {
            List<SQLite3Expression> expressions = new ArrayList<>();
            for (int i = 0; i < nrArgs; i++) {
                expressions.add(gen.getRandomExpression(argTypes[i], depth + 1));
            }
            return expressions;
        }
    }

    private SQLite3Expression getFunction(SQLite3DataType type, SQLite3GlobalState globalState, int depth) {
        if (tryToGenerateKnownResult || Randomly.getBoolean()) {
            return getComputableFunction(type, depth + 1);
        } else {
            List<AnyFunction> functions = Stream.of(AnyFunction.values()).filter(f -> f.isCompatibleWithReturnType(type)).collect(Collectors.toList());

            if (functions.isEmpty()) {
                throw new IgnoreMeException();
            }
            AnyFunction randomFunction = Randomly.fromList(functions);
            // if (deterministicOnly) {
            //     randomFunction = AnyFunction.getRandomDeterministic(globalState);
            // } else {
            //     randomFunction = AnyFunction.getRandom(globalState);
            // }
            int nrArgs = randomFunction.getMinNrArgs();
            if (randomFunction.isVariadic()) {
                nrArgs += Randomly.smallNumber();
            }
            List<SQLite3Expression> expressions = randomFunction.generateArguments(nrArgs, depth + 1, this);
            // The second argument of LIKELIHOOD must be a float number within 0.0 -1.0
            if (randomFunction == AnyFunction.LIKELIHOOD) {
                SQLite3Expression lastArg = SQLite3Constant.createRealConstant(Randomly.getPercentage());
                expressions.remove(expressions.size() - 1);
                expressions.add(lastArg);
            }
            return new SQLite3Expression.Function(randomFunction.toString(),
                    expressions.toArray(new SQLite3Expression[0]));
        }

    }

    private SQLite3Expression getComputableFunction(SQLite3DataType type, int depth) {
        List<ComputableFunction> functions = Stream.of(ComputableFunction.values()).filter(f -> f.supportsReturnType(type)).collect(Collectors.toList());
        
        if (functions.isEmpty()) {
            throw new IgnoreMeException();
        }
        ComputableFunction func = Randomly.fromList(functions);
        int nrArgs = func.getNrArgs();
        if (func.isVariadic()) {
            nrArgs += Randomly.smallNumber();
        }
        SQLite3DataType[] argTypes = func.getInputTypesForReturnType(type, nrArgs);
        SQLite3Expression[] args = new SQLite3Expression[nrArgs];
        for (int i = 0; i < args.length; i++) {
            args[i] = getRandomExpression(argTypes[i], depth + 1);
            if (i == 0 && Randomly.getBoolean()) {
                args[i] = new SQLite3Distinct(args[i]);
            }
        }
        // The second argument of LIKELIHOOD must be a float number within 0.0 -1.0
        // if (func == ComputableFunction.LIKELIHOOD) {
        //     SQLite3Expression lastArg = SQLite3Constant.createRealConstant(Randomly.getPercentage());
        //     args[args.length - 1] = lastArg;
        // }
        return new SQLite3Function(func, args);
    }

    protected SQLite3Expression getRandomSingleCharString() {
        String s;
        do {
            s = r.getString();
        } while (s.isEmpty());
        return new SQLite3TextConstant(String.valueOf(s.charAt(0)));
    }

    public void allowNullValue(boolean value) {
        allowNullValue = value;
    }
    @Override
    protected SQLite3Expression generateColumn(SQLite3DataType type) {
        SQLite3Column column = Randomly.fromList(columns.stream().filter(c -> c.getType() == type).collect(Collectors.toList()));
        return new SQLite3ColumnName(column, rw == null ? null : rw.getValues().get(column));
    }

    @Override
    public SQLite3Expression generateConstant(SQLite3DataType type) {
        if (Randomly.getBooleanWithRatherLowProbability() && allowNullValue) {
            return SQLite3Constant.createNullConstant();
        }
        switch (type) {
        case BOOLEAN:
            return SQLite3Constant.createBooleanConstant(Randomly.getBoolean());
        case INT:
            // if (Randomly.getBoolean()) {
                return SQLite3Constant.createIntConstant(r.getInteger(), false);
            // } else {
            //     return SQLite3Constant.createTextConstant(String.valueOf(r.getInteger()));
            // }
        case REAL:
            return SQLite3Constant.createRealConstant(r.getDouble());
        case NONE:
        case TEXT:
            return SQLite3Constant.createTextConstant(r.getString());
        // case BINARY:
        //     return SQLite3Constant.getRandomBinaryConstant(r);
        case NULL:
            return SQLite3Constant.createNullConstant();
        default:
            throw new AssertionError(type);
        }
    }

    @Override
    protected SQLite3DataType getRandomType() {
        if (columns.isEmpty() || Randomly.getBooleanWithRatherLowProbability()) {
            return Randomly.fromOptions(SQLite3DataType.values());
        } else {
            return Randomly.fromList(columns).getType();
        }
    }

    @Override
    protected boolean canGenerateColumnOfType(SQLite3DataType type) {
        return columns.stream().anyMatch(c -> c.getType() == type);
    }

    public SQLite3Expression getHavingClause() {
        allowAggreates = true;
        return generateExpression(getRandomType());
    }
    
    @Override
    public SQLite3Expression generatePredicate() {
        return generateExpression(SQLite3DataType.BOOLEAN);
    }

    @Override
    public SQLite3Expression negatePredicate(SQLite3Expression predicate) {
        return new SQLite3UnaryOperation(UnaryOperator.NOT, predicate);
    }

    @Override
    public SQLite3Expression isNull(SQLite3Expression expr) {
        return new SQLite3PostfixUnaryOperation(PostfixUnaryOperator.ISNULL, expr);
    }

    public SQLite3Expression generateResultKnownExpression() {
        SQLite3Expression expr;
        do {
            expr = generateExpression(getRandomType());
        } while (expr.getExpectedValue() == null);
        return expr;
    }

}
