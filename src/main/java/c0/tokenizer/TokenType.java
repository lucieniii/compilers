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
    COMMENT;
}
