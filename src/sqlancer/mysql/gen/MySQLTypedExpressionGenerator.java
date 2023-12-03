package sqlancer.mysql.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.gen.TypedExpressionGenerator;
import sqlancer.mysql.MySQLBugs;
import sqlancer.mysql.MySQLGlobalState;
import sqlancer.mysql.MySQLSchema.MySQLColumn;
import sqlancer.mysql.MySQLSchema.MySQLDataType;
import sqlancer.mysql.MySQLSchema.MySQLRowValue;
import sqlancer.mysql.ast.MySQLBetweenOperation;
import sqlancer.mysql.ast.MySQLBinaryComparisonOperation;
import sqlancer.mysql.ast.MySQLBinaryComparisonOperation.BinaryComparisonOperator;
import sqlancer.mysql.ast.MySQLBinaryLogicalOperation;
import sqlancer.mysql.ast.MySQLBinaryLogicalOperation.MySQLBinaryLogicalOperator;
import sqlancer.mysql.ast.MySQLBinaryOperation;
import sqlancer.mysql.ast.MySQLBinaryOperation.MySQLBinaryOperator;
import sqlancer.mysql.ast.MySQLCastOperation;
import sqlancer.mysql.ast.MySQLColumnReference;
import sqlancer.mysql.ast.MySQLComputableFunction;
import sqlancer.mysql.ast.MySQLComputableFunction.MySQLFunction;
import sqlancer.mysql.ast.MySQLConstant;
import sqlancer.mysql.ast.MySQLConstant.MySQLDoubleConstant;
import sqlancer.mysql.ast.MySQLExists;
import sqlancer.mysql.ast.MySQLExpression;
import sqlancer.mysql.ast.MySQLInOperation;
import sqlancer.mysql.ast.MySQLOrderByTerm;
import sqlancer.mysql.ast.MySQLOrderByTerm.MySQLOrder;
import sqlancer.mysql.ast.MySQLStringExpression;
import sqlancer.mysql.ast.MySQLUnaryPostfixOperation;
import sqlancer.mysql.ast.MySQLUnaryPrefixOperation;
import sqlancer.mysql.ast.MySQLUnaryPrefixOperation.MySQLUnaryPrefixOperator;

public class MySQLTypedExpressionGenerator extends TypedExpressionGenerator<MySQLExpression, MySQLColumn, MySQLDataType> {

    private final MySQLGlobalState state;
    private MySQLRowValue rowVal;

    public MySQLTypedExpressionGenerator(MySQLGlobalState state) {
        this.state = state;
    }

    public MySQLTypedExpressionGenerator setRowVal(MySQLRowValue rowVal) {
        this.rowVal = rowVal;
        return this;
    }

    private enum BooleanExpression {
        BETWEEN_OPERATOR, EXISTS, UNARY_PREFIX_OPERATION, IN_OPERATION, BINARY_COMPARISON_OPERATION, BINARY_LOGICAL_OPERATOR, UNARY_POSTFIX;
    }

    private MySQLExpression generateBooleaExpression(int depth) {
        BooleanExpression option = Randomly.fromOptions(BooleanExpression.values());
        switch (option) {
            case BETWEEN_OPERATOR:
                if (MySQLBugs.bug99181) {
                    // TODO: there are a number of bugs that are triggered by the BETWEEN operator
                    throw new IgnoreMeException();
                }
                MySQLDataType randomType = getRandomType();
                return new MySQLBetweenOperation(generateExpression(randomType, depth + 1), generateExpression(randomType, depth + 1),
                        generateExpression(randomType, depth + 1));
            case EXISTS:
                return getExists();
            case UNARY_PREFIX_OPERATION:
                MySQLExpression subExpr = generateExpression(MySQLDataType.BOOLEAN, depth + 1);
                return new MySQLUnaryPrefixOperation(subExpr, MySQLUnaryPrefixOperator.NOT);
            case IN_OPERATION:
                MySQLDataType type = getRandomType();
                MySQLExpression expr = generateExpression(type, depth + 1);
                List<MySQLExpression> rightList = new ArrayList<>();
                for (int i = 0; i < 1 + Randomly.smallNumber(); i++) {
                    rightList.add(generateExpression(type, depth + 1));
                }
                return new MySQLInOperation(expr, rightList, Randomly.getBoolean());
            case BINARY_COMPARISON_OPERATION:
                MySQLDataType targetType = getRandomType();
                List<BinaryComparisonOperator> validOptions = new ArrayList<>(Arrays.asList(BinaryComparisonOperator.values()));
                if (targetType != MySQLDataType.VARCHAR) {
                    validOptions.remove(BinaryComparisonOperator.LIKE);
                }
                BinaryComparisonOperator operator = Randomly.fromList(validOptions);
                return new MySQLBinaryComparisonOperation(generateExpression(targetType, depth + 1), generateExpression(targetType, depth + 1),
                    operator);
            case BINARY_LOGICAL_OPERATOR:
                return new MySQLBinaryLogicalOperation(generateExpression(MySQLDataType.BOOLEAN, depth + 1), generateExpression(MySQLDataType.BOOLEAN, depth + 1),
                    MySQLBinaryLogicalOperator.getRandom());
            case UNARY_POSTFIX:
                return new MySQLUnaryPostfixOperation(generateExpression(MySQLDataType.BOOLEAN, depth + 1),
                        Randomly.fromOptions(MySQLUnaryPostfixOperation.UnaryPostfixOperator.values()),
                        Randomly.getBoolean());
            default:
                throw new AssertionError();
        }
    }

    private enum TextExpression {
        COMPUTABLE_FUNCTION;
    }

    private MySQLExpression generateTextExpression(int depth) {
        TextExpression option = Randomly.fromOptions(TextExpression.values());
        switch (option) {
            case COMPUTABLE_FUNCTION:
                return getComputableFunction(MySQLDataType.VARCHAR, depth + 1);
            default:
                throw new AssertionError();
        }
    }

    private enum IntExpression {
        BINARY_OPERATION, COMPUTABLE_FUNCTION, UNARY_PREFIX_OPERATION/*, CAST*/;
    }
    
    private MySQLExpression generateIntExpression(int depth) {
        IntExpression option = Randomly.fromOptions(IntExpression.values());
        switch (option) {
            case BINARY_OPERATION:
                // if (MySQLBugs.bug99135) {
                //     throw new IgnoreMeException();
                // }
                // return new MySQLBinaryOperation(generateExpression(MySQLDataType.INT, depth + 1), generateExpression(MySQLDataType.INT, depth + 1),
                //         MySQLBinaryOperator.getRandom());
            case COMPUTABLE_FUNCTION:
                return getComputableFunction(MySQLDataType.INT, depth + 1);
            case UNARY_PREFIX_OPERATION:
                MySQLExpression subExpr = generateExpression(MySQLDataType.INT, depth + 1);
                MySQLUnaryPrefixOperator random = Randomly.getBoolean() ? MySQLUnaryPrefixOperator.PLUS : MySQLUnaryPrefixOperator.MINUS;
                if (random == MySQLUnaryPrefixOperator.MINUS) {
                    // workaround for https://bugs.mysql.com/bug.php?id=99122
                    throw new IgnoreMeException();
                }
                return new MySQLUnaryPrefixOperation(subExpr, random);
            // case CAST:
            //     return new MySQLCastOperation(generateExpression(MySQLDataType.getRandom(state), depth + 1), MySQLCastOperation.CastType.getRandom());
            default:
                throw new AssertionError();
        }
    }

    @Override
    public MySQLExpression generateExpression(MySQLDataType type) {
        return generateExpression(type, 0);
    }

    @Override
    public MySQLExpression generateExpression(MySQLDataType type, int depth) {
        if (depth >= state.getOptions().getMaxExpressionDepth()) {
            return generateLeafNode(type);
        }
        switch (type) {
            case BOOLEAN:
                return generateBooleaExpression(depth);
            case INT:
            case DECIMAL:
                return generateIntExpression(depth);
            case VARCHAR:
                return generateTextExpression(depth);
            case FLOAT:
            case DOUBLE:
                return generateLeafNode(type);
            default:
                throw new AssertionError();
        }
    }

    @Override
    protected MySQLDataType getRandomType() {
        if (columns.isEmpty() || Randomly.getBooleanWithRatherLowProbability()) {
            return MySQLDataType.getRandom(state);
        } else {
            return Randomly.fromList(columns).getType();
        }
    }

    @Override
    protected boolean canGenerateColumnOfType(MySQLDataType type) {
        return columns.stream().anyMatch(c -> c.getType() == type);
    }
    

    @Override
    public MySQLExpression generateConstant(MySQLDataType type) {
        if (Randomly.getBooleanWithRatherLowProbability()) {
            return MySQLConstant.createNullConstant();
        }
        switch (type) {
	        case BOOLEAN:
	            return MySQLConstant.createBoolean(Randomly.getBoolean());
            case INT:
            case DECIMAL:
                return MySQLConstant.createIntConstant(state.getRandomly().getInteger());
            case FLOAT:
                return MySQLConstant.createDoubleConstant((float) state.getRandomly().getDouble());
            case DOUBLE:
                double val = state.getRandomly().getDouble();
                if (Math.abs(val) <= 1 && val != 0) {
                    // https://bugs.mysql.com/bug.php?id=99145
                    throw new IgnoreMeException();
                }
                if (Math.abs(val) > 1.0E30) {
                    // https://bugs.mysql.com/bug.php?id=99146
                    throw new IgnoreMeException();
                }
                return new MySQLDoubleConstant(val);
            case VARCHAR:
                /* Replace characters that still trigger open bugs in MySQL */
                String string = state.getRandomly().getString().replace("\\", "").replace("\n", "");
                if (string.startsWith("\n")) {
                    // workaround for https://bugs.mysql.com/bug.php?id=99130
                    throw new IgnoreMeException();
                }
                if (string.startsWith("-0") || string.startsWith("0.") || string.startsWith(".")) {
                    // https://bugs.mysql.com/bug.php?id=99145
                    throw new IgnoreMeException();
                }
                MySQLConstant createStringConstant = MySQLConstant.createStringConstant(string);
                // if (Randomly.getBoolean()) {
                // return new MySQLCollate(createStringConstant,
                // Randomly.fromOptions("ascii_bin", "binary"));
                // }
                if (string.startsWith("1e")) {
                    // https://bugs.mysql.com/bug.php?id=99146
                    throw new IgnoreMeException();
                }
                return createStringConstant;
            default:
                throw new AssertionError(type);
        }
    }

    @Override
    protected MySQLExpression generateColumn(MySQLDataType type) {
        MySQLColumn column = Randomly.fromList(columns.stream().filter(c -> c.getType() == type).collect(Collectors.toList()));
        MySQLConstant val;
        if (rowVal == null) {
            val = null;
        } else {
            val = rowVal.getValues().get(column);
        }
        return MySQLColumnReference.create(column, val);
    }

    private MySQLExpression getComputableFunction(MySQLDataType type, int depth) {
        List<MySQLFunction> functions = Stream.of(MySQLComputableFunction.MySQLFunction.values()).filter(f -> f.supportsReturnType(type)).collect(Collectors.toList());
        
        if (functions.isEmpty()) {
            throw new IgnoreMeException();
        }
        MySQLFunction func = Randomly.fromList(functions);
        int nrArgs = func.getNrArgs();
        if (func.isVariadic()) {
            nrArgs += Randomly.smallNumber();
        }
        MySQLDataType[] argTypes = func.getInputTypesForReturnType(type, nrArgs);
        MySQLExpression[] args = new MySQLExpression[nrArgs];
        for (int i = 0; i < args.length; i++) {
            args[i] = generateExpression(argTypes[i], depth + 1);
        }
        return new MySQLComputableFunction(func, args);
    }

    private MySQLExpression getExists() {
        if (Randomly.getBoolean()) {
            return new MySQLExists(new MySQLStringExpression("SELECT 1", MySQLConstant.createTrue()));
        } else {
            return new MySQLExists(new MySQLStringExpression("SELECT 1 wHERE FALSE", MySQLConstant.createFalse()));
        }
    }

    @Override
    public MySQLExpression generatePredicate() {
        return generateExpression(MySQLDataType.BOOLEAN);
    }

    @Override
    public MySQLExpression negatePredicate(MySQLExpression predicate) {
        return new MySQLUnaryPrefixOperation(predicate, MySQLUnaryPrefixOperator.NOT);
    }

    @Override
    public MySQLExpression isNull(MySQLExpression expr) {
        return new MySQLUnaryPostfixOperation(expr, MySQLUnaryPostfixOperation.UnaryPostfixOperator.IS_NULL, false);
    }

    public MySQLExpression generateHavingClause() {
        MySQLExpression expression = generateExpression(MySQLDataType.BOOLEAN);
        return expression;
    }

    @Override
    public List<MySQLExpression> generateOrderBys() {
        List<MySQLExpression> expressions = super.generateOrderBys();
        List<MySQLExpression> newOrderBys = new ArrayList<>();
        for (MySQLExpression expr : expressions) {
            if (Randomly.getBoolean()) {
                MySQLOrderByTerm newExpr = new MySQLOrderByTerm(expr, MySQLOrder.getRandomOrder());
                newOrderBys.add(newExpr);
            } else {
                newOrderBys.add(expr);
            }
        }
        return newOrderBys;
    }

}
