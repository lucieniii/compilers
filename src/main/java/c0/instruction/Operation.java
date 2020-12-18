package main.java.c0.instruction;

/**
 * navm 虚拟机指令集
 */
public enum Operation {

    NOP, /* Need no arg. */
    PUSH, /* Need no arg. */
    POP,
    POP_N, /* Need no arg. */
    DUP,
    LOC_A, /* Need no arg. */
    ARG_A, /* Need no arg. */
    GLOB_A, /* Need no arg. */
    LOAD_8,
    LOAD_16,
    LOAD_32,
    LOAD_64,
    STORE_8,
    STORE_16,
    STORE_32,
    STORE_64,
    ALLOC,
    FREE,
    STACK_ALLOC, /* Need no arg. */
    ADD_I,
    SUB_I,
    MUL_I,
    DIV_I,
    ADD_F,
    SUB_F,
    MUL_F,
    DIV_F,
    DIV_U,
    SHL,
    SHR,
    AND,
    OR,
    XOR,
    NOT,
    CMP_I,
    CMP_U,
    CMP_F,
    NEG_I,
    NEG_F,
    ITOF,
    FTOI,
    SHR_L,
    SET_LT,
    SET_GT,
    BR, /* Need no arg. */
    BR_FALSE, /* Need no arg. */
    BR_TRUE, /* Need no arg. */
    CALL, /* Need no arg. */
    RET,
    CALL_NAME, /* Need no arg. */
    SCAN_I,
    SCAN_C,
    SCAN_F,
    PRINT_I,
    PRINT_C,
    PRINT_F,
    PRINT_S,
    PRINT_LN,
    PANIC;
}
