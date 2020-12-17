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
    DOUBLE;

    @Override
    public String toString() {
        return switch (this) {
            case EQ -> "EQ";
            case GE -> "GE";
            case GT -> "GT";
            case LE -> "LE";
            case LT -> "LT";
            case DIV -> "DIV";
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
            case COMMENT -> "COMMENT";
            case ELSE_KW -> "ELSE_KW";
            case L_BRACE -> "L_BRACE";
            case L_PAREN -> "L_PAREN";
            case R_BRACE -> "R_BRACE";
            case R_PAREN -> "R_PAREN";
            case BREAK_KW -> "BREAK_KW";
            case CONST_KW -> "CONST_KW";
            case WHILE_KW -> "WHILE_KW";
            case RETURN_KW -> "RETURN_KW";
            case SEMICOLON -> "SEMICOLON";
            case CONTINUE_KW -> "CONTINUE_KW";
            case CHAR_LITERAL -> "CHAR_LITERAL";
            case UINT_LITERAL -> "UINT_LITERAL";
            case DOUBLE_LITERAL -> "DOUBLE_LITERAL";
            case STRING_LITERAL -> "STRING_LITERAL";
        };
    }
}
