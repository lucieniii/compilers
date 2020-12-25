package src.main.java.c0.instruction;

import java.util.Objects;
import src.main.java.c0.util.Encoder;

public class Instruction {
    private int idx;
    private Operation opt;
    private long x;
    public static int stackUse = 0;

    public Instruction(int idx, Operation opt) {
        this.idx = idx;
        this.opt = opt;
        this.x = 0L;
        stackUse += opt.getUseStack(0);
    }

    public Instruction(int idx, Operation opt, long x) {
        this.idx = idx;
        this.opt = opt;
        this.x = x;
        stackUse += opt.getUseStack((int) x);
    }

    public Instruction(int idx, Operation opt, long x, int paramCnt) {
        this.idx = idx;
        this.opt = opt;
        this.x = x;
        stackUse += opt.getUseStack(paramCnt);
    }

    public Instruction(int idx) {
        this.idx = idx;
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

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    @Override
    public String toString() {
        return switch (this.opt) {
            case PUSH, POP_N, LOC_A, ARG_A, GLOB_A, STACK_ALLOC, BR, BR_FALSE, BR_TRUE, CALL, CALL_NAME
                    -> String.format("%s %s", this.opt, this.x);
            default -> String.format("%s", this.opt);
        };
    }

    public byte[] toBytes() {
        byte[] ins;
        switch (this.opt) {
            case PUSH -> {
                byte[] opt = Encoder.toBytes((byte) this.opt.getValue());
                byte[] x = Encoder.toBytes(this.x);
                ins = new byte[opt.length + x.length];
                System.arraycopy(opt, 0, ins, 0, opt.length);
                System.arraycopy(x, 0, ins, opt.length, x.length);
            }
            case POP_N, LOC_A, ARG_A, GLOB_A, STACK_ALLOC, BR, BR_FALSE, BR_TRUE, CALL, CALL_NAME -> {
                byte[] opt = Encoder.toBytes((byte) this.opt.getValue());
                byte[] x = Encoder.toBytes((int)this.x);
                ins = new byte[opt.length + x.length];
                System.arraycopy(opt, 0, ins, 0, opt.length);
                System.arraycopy(x, 0, ins, opt.length, x.length);
            }
            default -> {
                ins = Encoder.toBytes(this.opt.getValue());
            }
        }
        return ins;
    }
}
