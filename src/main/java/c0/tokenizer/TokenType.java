package main.java.c0.tokenizer;

public enum TokenType {

    /* ---------关键字--------- */

    /* 函数 */
    FN_KW,

    /* 变量声明 */
    LET_KW,

    /* 常量声明 */
    CONST_KW,

    /* 类型转换 */
    AS_KW,

    /* 循环 */
    WHILE_KW,

    /* 分支if */
    IF_KW,

    /* 分支else */
    ELSE_KW,

    /* 返回 */
    RETURN_KW,

    /* 循环跳出 */
    BREAK_KW,

    /* 循环下一个 */
    CONTINUE_KW,

    /* ---------字面量--------- */

    /* 无符号整型 */
    UINT_LITERAL,

    /* 字符串常量 */
    STRING_LITERAL,

    /* 浮点数 */
    DOUBLE_LITERAL,

    /* 字符常量 */
    CHAR_LITERAL,

    /* ---------标识符--------- */

    /* 标识符 */
    IDENT,

    /* ---------运算符--------- */

    /* + */
    PLUS,

    /* - */
    MINUS,

    /* * */
    MUL,

    /* / */
    DIV,

    /* = */
    ASSIGN,

    /* == */
    EQ,

    /* != */
    NEQ,

    /* < */
    LT,

    /* > */
    GT,

    /* <= */
    LE,

    /* >= */
    GE,

    /* ( */
    L_PAREN,

    /* ) */
    R_PAREN,

    /* { */
    L_BRACE,

    /* } */
    R_BRACE,

    /* -> */
    ARROW,

    /* , */
    COMMA,

    /* ; */
    COLON,

    /* ; */
    SEMICOLON,

    /* ---------注释--------- */

    /* 注释 */
    COMMENT,

    /* ---------类型标识--------- */

    /* 有符号整型标识 */
    INT,

    /* 空类型标识 */
    VOID,

    /* 浮点数标识 */
    DOUBLE,

    /* ---------库函数--------- */

    /* 读入一个有符号整数 */
    GET_INT,

    /* 读入一个浮点数 */
    GET_DOUBLE,

    /* 读入一个字符 */
    GET_CHAR,

    /* 输出一个整数 */
    PUT_INT,

    /* 输出一个浮点数 */
    PUT_DOUBLE,

    /* 输出一个字符 */
    PUT_CHAR,

    /* 将编号为这个整数的全局常量看作字符串输出 */
    PUT_STR,

    /* 输出一个换行 */
    PUT_LN,

    /* ---------自定义--------- */

    /* 文件末尾 */
    EOF,

    /* 表达式末尾 */
    EOE;

    @Override
    public String toString() {
        return switch (this) {
            case EQ -> "EQ";
            case GE -> "GE";
            case GT -> "GT";
            case LE -> "LE";
            case LT -> "LT";
            case DIV -> "DIV";
            case EOE -> "EOE";
            case EOF -> "EOF";
            case INT -> "INT";
            case MUL -> "MUL";
            case NEQ -> "NEQ";
            case PLUS -> "PLUS";
            case VOID -> "VOID";
            case ARROW -> "ARROW";
            case AS_KW -> "AS_KW";
            case COLON -> "COLON";
            case COMMA -> "COMMA";
            case FN_KW -> "FN_KW";
            case IDENT -> "IDENT";
            case IF_KW -> "IF_KW";
            case MINUS -> "MINUS";
            case ASSIGN -> "ASSIGN";
            case DOUBLE -> "DOUBLE";
            case LET_KW -> "LET_KW";
            case PUT_LN -> "PUT_LN";
            case COMMENT -> "COMMENT";
            case ELSE_KW -> "ELSE_KW";
            case GET_INT -> "GET_INT";
            case L_BRACE -> "L_BRACE";
            case L_PAREN -> "L_PAREN";
            case PUT_INT -> "PUT_INT";
            case PUT_STR -> "PUT_STR";
            case R_BRACE -> "R_BRACE";
            case R_PAREN -> "R_PAREN";
            case BREAK_KW -> "BREAK_KW";
            case CONST_KW -> "CONST_KW";
            case GET_CHAR -> "GET_CHAR";
            case PUT_CHAR -> "PUT_CHAR";
            case WHILE_KW -> "WHILE_KW";
            case RETURN_KW -> "RETURN_KW";
            case SEMICOLON -> "SEMICOLON";
            case GET_DOUBLE -> "GET_DOUBLE";
            case PUT_DOUBLE -> "PUT_DOUBLE";
            case CONTINUE_KW -> "CONTINUE_KW";
            case CHAR_LITERAL -> "CHAR_LITERAL";
            case UINT_LITERAL -> "UINT_LITERAL";
            case DOUBLE_LITERAL -> "DOUBLE_LITERAL";
            case STRING_LITERAL -> "STRING_LITERAL";
        };
    }
}
