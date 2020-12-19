package main.java.c0.analyser;

import main.java.c0.instruction.Instruction;
import main.java.c0.tokenizer.Token;

import java.util.ArrayList;

/**
 * 符号表管理
 */
public class SymbolEntry {

    Token ident;
    Token type;
    int level;
    int stackOffset;

    boolean isFunction;
    ArrayList<Instruction> instructions;

    boolean isConstant;
    boolean isInitialized;

    boolean isString;

    /**
     * Variable
     * @param ident
     * @param type
     * @param level
     * @param isConstant
     * @param isDeclared
     * @param stackOffset
     */
    public SymbolEntry(Token ident, Token type, int level, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.ident = ident;
        this.type = type;
        this.level = level;
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;

        this.isFunction = false;
        this.instructions = null;
        this.isString = false;
    }

    /**
     * Function
     * @param ident
     * @param type
     * @param stackOffset
     */
    public SymbolEntry(Token ident, Token type, int stackOffset) {
        this.ident = ident;
        this.type = type;
        this.stackOffset = stackOffset;

        this.level = 0;
        this.isConstant = false;
        this.isInitialized = false;
        this.isFunction = true;
        this.isString = false;

        this.instructions = new ArrayList<>();
    }

    /**
     * String
     * @param ident
     * @param stackOffset
     */
    public SymbolEntry(Token ident, int stackOffset) {
        this.ident = ident;
        this.type = null;
        this.level = 0;
        this.isConstant = false;
        this.isInitialized = false;
        this.stackOffset = stackOffset;

        this.isFunction = false;
        this.instructions = null;
        this.isString = true;
    }

    /**
     * @return the stackOffset
     */
    public int getStackOffset() {
        return stackOffset;
    }


    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public String getStringValue() {
        return ident.getValueString();
    }

    /**
     * @return the isConstant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * @param stackOffset the stackOffset to set
     */
    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }
}

