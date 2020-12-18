package main.java.c0.instruction;

/**
 * navm 虚拟机指令集
 */
public enum Operation {

    NOP,
    PUSH,
    POP,
    POP_N,
    DUP,
    LOC_A,
    ARG_A,
    GLOB_A,
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
    STACK_ALLOC,
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
    BR,
    BR_FALSE,
    BR_TRUE,
    CALL,
    RET,
    CALL_NAME,
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
