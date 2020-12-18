package main.java.c0.instruction;

import java.util.Objects;

public class Instruction {
    private Operation opt;
    Long x;

    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0L;
    }

    public Instruction(Operation opt, Long x) {
        this.opt = opt;
        this.x = x;
    }

    public Instruction() {
        this.opt = Operation.NOP;
        this.x = 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }


    @Override
    public String toString() {
        return switch (this.opt) {
            case PUSH, POP_N, LOC_A, ARG_A, GLOB_A, STACK_ALLOC, BR, BR_FALSE, BR_TRUE, CALL, CALL_NAME
                    -> String.format("%s %s", this.opt, this.x);
            default -> String.format("%s", this.opt);
        };
    }
}
