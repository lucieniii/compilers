package main.java.c0.instruction;

import main.java.c0.util.Encoder;

/**
 * navm 虚拟机指令集
 */
public enum Operation {

    NOP,
    PUSH, /* Need 64 bits arg. */
    POP,
    POP_N, /* Need 32 bits arg. */
    DUP,
    LOC_A, /* Need 32 bits arg. */
    ARG_A, /* Need 32 bits arg. */
    GLOB_A, /* Need 32 bits arg. */
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
    STACK_ALLOC, /* Need 32 bits arg. */
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
    BR, /* Need 32 bits arg. */
    BR_FALSE, /* Need 32 bits arg. */
    BR_TRUE, /* Need 32 bits arg. */
    CALL, /* Need 32 bits arg. */
    RET,
    CALL_NAME, /* Need 32 bits arg. */
    SCAN_I,
    SCAN_C,
    SCAN_F,
    PRINT_I,
    PRINT_C,
    PRINT_F,
    PRINT_S,
    PRINT_LN,
    PANIC;

    public int getUseStack(int n) {
        return switch (this) {
            case POP_N, CALL, CALL_NAME -> -n;
            case STORE_8, STORE_16, STORE_32, STORE_64 -> -2;
            case POP, FREE, ADD_I, SUB_I, MUL_I, DIV_I, ADD_F, SUB_F, MUL_F, DIV_F, DIV_U, SHL, SHR, AND, OR, XOR,
                   CMP_I, CMP_U, CMP_F, SHR_L, BR_FALSE, BR_TRUE, PRINT_I, PRINT_C, PRINT_F, PRINT_S -> -1;
            case NOP, LOAD_8, LOAD_16, LOAD_32, LOAD_64, ALLOC, NEG_I, NEG_F, ITOF, FTOI, SET_LT, SET_GT, BR, NOT,
                    RET, PRINT_LN, PANIC -> 0;
            case PUSH, DUP, LOC_A, ARG_A, GLOB_A, SCAN_I, SCAN_C, SCAN_F -> 1;
            case STACK_ALLOC -> n;
        };
    }

    public int getValue() {
        return switch (this) {
            case NOP -> 0x00;
            case PUSH -> 0x01; /* Need 64 bits arg. */
            case POP -> 0x02;
            case POP_N -> 0x03; /* Need 32 bits arg. */
            case DUP -> 0x04;
            case LOC_A -> 0x0a; /* Need 32 bits arg. */
            case ARG_A -> 0x0b; /* Need 32 bits arg. */
            case GLOB_A -> 0x0c; /* Need 32 bits arg. */
            case LOAD_8 -> 0x10;
            case LOAD_16 -> 0x11;
            case LOAD_32 -> 0x12;
            case LOAD_64 -> 0x13;
            case STORE_8 -> 0x14;
            case STORE_16 -> 0x15;
            case STORE_32 -> 0x16;
            case STORE_64 -> 0x17;
            case ALLOC -> 0x18;
            case FREE -> 0x19;
            case STACK_ALLOC -> 0x1a; /* Need 32 bits arg. */
            case ADD_I -> 0x20;
            case SUB_I -> 0x21;
            case MUL_I -> 0x22;
            case DIV_I -> 0x23;
            case ADD_F -> 0x24;
            case SUB_F -> 0x25;
            case MUL_F -> 0x26;
            case DIV_F -> 0x27;
            case DIV_U -> 0x28;
            case SHL -> 0x29;
            case SHR -> 0x2a;
            case AND -> 0x2b;
            case OR -> 0x2c;
            case XOR -> 0x2d;
            case NOT -> 0x2e;
            case CMP_I -> 0x30;
            case CMP_U -> 0x31;
            case CMP_F -> 0x32;
            case NEG_I -> 0x34;
            case NEG_F -> 0x35;
            case ITOF -> 0x36;
            case FTOI -> 0x37;
            case SHR_L -> 0x38;
            case SET_LT -> 0x39;
            case SET_GT -> 0x3a;
            case BR -> 0x41; /* Need 32 bits arg. */
            case BR_FALSE -> 0x42; /* Need 32 bits arg. */
            case BR_TRUE -> 0x43; /* Need 32 bits arg. */
            case CALL -> 0x48; /* Need 32 bits arg. */
            case RET -> 0x49;
            case CALL_NAME -> 0x4a; /* Need 32 bits arg. */
            case SCAN_I -> 0x50;
            case SCAN_C -> 0x51;
            case SCAN_F -> 0x52;
            case PRINT_I -> 0x54;
            case PRINT_C -> 0x55;
            case PRINT_F -> 0x56;
            case PRINT_S -> 0x57;
            case PRINT_LN -> 0x58;
            case PANIC -> 0xfe;
        };
    }

    public static void main(String[] args) {
        System.out.println(Encoder.EncodeToString((byte)CALL.getValue()));
    }
}
