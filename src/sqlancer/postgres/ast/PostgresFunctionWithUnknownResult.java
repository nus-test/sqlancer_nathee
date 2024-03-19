package sqlancer.postgres.ast;

import java.util.ArrayList;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.postgres.PostgresSchema.PostgresDataType;
import sqlancer.postgres.gen.PostgresExpressionGenerator;

public enum PostgresFunctionWithUnknownResult {

//    ABBREV("abbrev", PostgresDataType.TEXT, PostgresDataType.INET), // E05
//    BROADCAST("broadcast", PostgresDataType.INET, PostgresDataType.INET), // E05
//    FAMILY("family", PostgresDataType.INT, PostgresDataType.INET), // E05
//    HOSTMASK("hostmask", PostgresDataType.INET, PostgresDataType.INET), // E05
//    MASKLEN("masklen", PostgresDataType.INT, PostgresDataType.INET), // E05
//    NETMASK("netmask", PostgresDataType.INET, PostgresDataType.INET), // E05
//    SET_MASKLEN("set_masklen", PostgresDataType.INET, PostgresDataType.INET, PostgresDataType.INT), // E05
//    TEXT("text", PostgresDataType.TEXT, PostgresDataType.INET), // E05
//    INET_SAME_FAMILY("inet_same_family", PostgresDataType.BOOLEAN, PostgresDataType.INET, PostgresDataType.INET), // E05

    // https://www.postgresql.org/docs/devel/functions-admin.html#FUNCTIONS-ADMIN-SIGNAL-TABLE
    // PG_RELOAD_CONF("pg_reload_conf", PostgresDataType.BOOLEAN), // too much output
    // PG_ROTATE_LOGFILE("pg_rotate_logfile", PostgresDataType.BOOLEAN), prints warning

    // https://www.postgresql.org/docs/devel/functions-info.html#FUNCTIONS-INFO-SESSION-TABLE
    // CURRENT_DATABASE("current_database", PostgresDataType.TEXT), // name // E06
    // CURRENT_QUERY("current_query", PostgresDataType.TEXT), // can generate false positives
    // CURRENT_SCHEMA("current_schema", PostgresDataType.TEXT), // name // E06
    // CURRENT_SCHEMAS("current_schemas", PostgresDataType.TEXT, PostgresDataType.BOOLEAN),
    // INET_CLIENT_PORT("inet_client_port", PostgresDataType.INT), // E06
    // INET_SERVER_PORT("inet_server_port", PostgresDataType.INT),
    // PG_BACKEND_PID("pg_backend_pid", PostgresDataType.INT), // E06
    // PG_CURRENT_LOGFILE("pg_current_logfile", PostgresDataType.TEXT), // E06
    // PG_IS_OTHER_TEMP_SCHEMA("pg_is_other_temp_schema", PostgresDataType.BOOLEAN), // E06
    // PG_JIT_AVAILABLE("pg_jit_available", PostgresDataType.BOOLEAN), // E06
    // PG_NOTIFICATION_QUEUE_USAGE("pg_notification_queue_usage", PostgresDataType.REAL), // E06
    // PG_TRIGGER_DEPTH("pg_trigger_depth", PostgresDataType.INT), VERSION("version", PostgresDataType.TEXT), // E06

    //
    // TO_CHAR("to_char", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.TEXT) { // E14
    //     @Override
    //     public PostgresExpression[] getArguments(PostgresDataType returnType, PostgresExpressionGenerator gen,
    //             int depth) {
    //         PostgresExpression[] args = super.getArguments(returnType, gen, depth);
    //         args[0] = gen.generateExpression(PostgresDataType.getRandomType());
    //         return args;
    //     }
    // },

    // String functions
    // ASCII("ascii", PostgresDataType.INT, PostgresDataType.TEXT), // E07
    // BTRIM("btrim", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // CHR("chr", PostgresDataType.TEXT, PostgresDataType.INT), // E07
    // CONVERT_FROM("convert_from", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.TEXT) { // E07
    //     @Override
    //     public PostgresExpression[] getArguments(PostgresDataType returnType, PostgresExpressionGenerator gen,
    //             int depth) {
    //         PostgresExpression[] args = super.getArguments(returnType, gen, depth);
    //         args[1] = PostgresConstant.createTextConstant(Randomly.fromOptions("UTF8", "LATIN1"));
    //         return args;
    //     }
    // },
    // concat
    // segfault
    // BIT_LENGTH("bit_length", PostgresDataType.INT, PostgresDataType.TEXT),
    // INITCAP("initcap", PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // LEFT("left", PostgresDataType.TEXT, PostgresDataType.INT, PostgresDataType.TEXT), // E07
    LOWER("lower", PostgresDataType.TEXT, PostgresDataType.TEXT),
    // MD5("md5", PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    UPPER("upper", PostgresDataType.TEXT, PostgresDataType.TEXT),
    // PG_CLIENT_ENCODING("pg_client_encoding", PostgresDataType.TEXT),
    // QUOTE_LITERAL("quote_literal", PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // QUOTE_IDENT("quote_ident", PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // REGEX_REPLACE("regex_replace", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // REPEAT("repeat", PostgresDataType.TEXT, PostgresDataType.TEXT,
    // PostgresDataType.INT),
    REPLACE("replace", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.TEXT),
    // REVERSE("reverse", PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // RIGHT("right", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.INT), // E07
    // RPAD("rpad", PostgresDataType.TEXT, PostgresDataType.INT, PostgresDataType.TEXT), // E07
    RTRIM("rtrim", PostgresDataType.TEXT, PostgresDataType.TEXT),
    // SPLIT_PART("split_part", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.INT), // E07
    // STRPOS("strpos", PostgresDataType.INT, PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // SUBSTR("substr", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.INT, PostgresDataType.INT), // E07
    // TO_ASCII("to_ascii", PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // TO_HEX("to_hex", PostgresDataType.INT, PostgresDataType.TEXT), // E07
    // TRANSLATE("translate", PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.TEXT, PostgresDataType.TEXT), // E07
    // mathematical functions
    // https://www.postgresql.org/docs/9.5/functions-math.html
    ABS("abs", PostgresDataType.REAL, PostgresDataType.REAL),
    // CBRT("cbrt", PostgresDataType.REAL, PostgresDataType.REAL), CEILING("ceiling", PostgresDataType.REAL), // // E08
    DEGREES("degrees", PostgresDataType.REAL), EXP("exp", PostgresDataType.REAL), LN("ln", PostgresDataType.REAL),
    LOG("log", PostgresDataType.REAL), LOG2("log", PostgresDataType.REAL, PostgresDataType.REAL),
    PI("pi", PostgresDataType.REAL), POWER("power", PostgresDataType.REAL, PostgresDataType.REAL),
    TRUNC("trunc", PostgresDataType.REAL, PostgresDataType.INT),
    TRUNC2("trunc", PostgresDataType.REAL, PostgresDataType.INT, PostgresDataType.REAL),
    FLOOR("floor", PostgresDataType.REAL),

    // trigonometric functions - complete
    // https://www.postgresql.org/docs/12/functions-math.html#FUNCTIONS-MATH-TRIG-TABLE
    ACOS("acos", PostgresDataType.REAL), //
    // ACOSD("acosd", PostgresDataType.REAL), // // E08
    ASIN("asin", PostgresDataType.REAL), //
    // ASIND("asind", PostgresDataType.REAL), // // E08
    ATAN("atan", PostgresDataType.REAL), //
    // ATAND("atand", PostgresDataType.REAL), // // E08
    ATAN2("atan2", PostgresDataType.REAL, PostgresDataType.REAL), //
    // ATAN2D("atan2d", PostgresDataType.REAL, PostgresDataType.REAL), // // E08
    COS("cos", PostgresDataType.REAL), //
    // COSD("cosd", PostgresDataType.REAL), // // E08
    COT("cot", PostgresDataType.REAL), //
    // COTD("cotd", PostgresDataType.REAL), // // E08
    SIN("sin", PostgresDataType.REAL), //
    // SIND("sind", PostgresDataType.REAL), // // E08
    TAN("tan", PostgresDataType.REAL); //
    // TAND("tand", PostgresDataType.REAL), // // E08

    // hyperbolic functions - complete
    // https://www.postgresql.org/docs/12/functions-math.html#FUNCTIONS-MATH-HYP-TABLE
    // SINH("sinh", PostgresDataType.REAL), // // E08
    // COSH("cosh", PostgresDataType.REAL), // // E08
    // TANH("tanh", PostgresDataType.REAL), // // E08
    // ASINH("asinh", PostgresDataType.REAL), // // E08
    // ACOSH("acosh", PostgresDataType.REAL), // // E08
    // ATANH("atanh", PostgresDataType.REAL), // // E08

    // https://www.postgresql.org/docs/devel/functions-binarystring.html
    // GET_BIT("get_bit", PostgresDataType.INT, PostgresDataType.TEXT, PostgresDataType.INT), // E14
    // GET_BYTE("get_byte", PostgresDataType.INT, PostgresDataType.TEXT, PostgresDataType.INT), // E14

    // range functions
    // https://www.postgresql.org/docs/devel/functions-range.html#RANGE-FUNCTIONS-TABLE
//    RANGE_LOWER("lower", PostgresDataType.INT, PostgresDataType.RANGE), // // E09
//    RANGE_UPPER("upper", PostgresDataType.INT, PostgresDataType.RANGE), // // E09
//    RANGE_ISEMPTY("isempty", PostgresDataType.BOOLEAN, PostgresDataType.RANGE), // // E09
//    RANGE_LOWER_INC("lower_inc", PostgresDataType.BOOLEAN, PostgresDataType.RANGE), // // E09
//    RANGE_UPPER_INC("upper_inc", PostgresDataType.BOOLEAN, PostgresDataType.RANGE), // // E09
//    RANGE_LOWER_INF("lower_inf", PostgresDataType.BOOLEAN, PostgresDataType.RANGE), // // E09
//    RANGE_UPPER_INF("upper_inf", PostgresDataType.BOOLEAN, PostgresDataType.RANGE), // // E09
//    RANGE_MERGE("range_merge", PostgresDataType.RANGE, PostgresDataType.RANGE, PostgresDataType.RANGE), // // E09

    // https://www.postgresql.org/docs/devel/functions-admin.html#FUNCTIONS-ADMIN-DBSIZE
    // GET_COLUMN_SIZE("get_column_size", PostgresDataType.INT, PostgresDataType.TEXT); // E14
    // PG_DATABASE_SIZE("pg_database_size", PostgresDataType.INT, PostgresDataType.INT);
    // PG_SIZE_BYTES("pg_size_bytes", PostgresDataType.INT, PostgresDataType.TEXT);

    private String functionName;
    private PostgresDataType returnType;
    private PostgresDataType[] argTypes;

    PostgresFunctionWithUnknownResult(String functionName, PostgresDataType returnType, PostgresDataType... indexType) {
        this.functionName = functionName;
        this.returnType = returnType;
        this.argTypes = indexType.clone();

    }

    public boolean isCompatibleWithReturnType(PostgresDataType t) {
        return t == returnType;
    }

    public PostgresExpression[] getArguments(PostgresDataType returnType, PostgresExpressionGenerator gen, int depth) {
        PostgresExpression[] args = new PostgresExpression[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = gen.generateExpression(depth, argTypes[i]);
        }
        return args;

    }

    public String getName() {
        return functionName;
    }

    public static List<PostgresFunctionWithUnknownResult> getSupportedFunctions(PostgresDataType type) {
        List<PostgresFunctionWithUnknownResult> functions = new ArrayList<>();
        for (PostgresFunctionWithUnknownResult func : values()) {
            if (func.isCompatibleWithReturnType(type)) {
                functions.add(func);
            }
        }
        return functions;
    }

}
