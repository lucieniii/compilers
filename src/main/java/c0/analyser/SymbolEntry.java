package main.java.c0.analyser;

import main.java.c0.instruction.Instruction;
import main.java.c0.tokenizer.Token;
import main.java.c0.tokenizer.TokenType;

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
    ArrayList<TokenType> paramList;
    ArrayList<Instruction> instructions;
    int stackSize;

    boolean isFunctionParam;
    boolean isConstant;
    boolean isInitialized;

    boolean isString;
    String strValue;

    /**
     * Variable
     * @param ident
     * @param type
     * @param level
     * @param isConstant
     * @param isDeclared
     * @param stackOffset
     */
    public SymbolEntry(Token ident, Token type, int level, boolean isFunctionParam, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.ident = ident;
        this.type = type;
        this.level = level;
        this.isFunctionParam = isFunctionParam;
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;

        this.isFunction = false;
        this.stackSize = 0;
        this.paramList = null;
        this.instructions = null;
        this.isString = false;
        this.strValue = null;
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
        this.isFunctionParam = false;
        this.isConstant = true;
        this.isInitialized = false;
        this.isFunction = true;
        this.isString = false;
        this.strValue = null;

        this.instructions = new ArrayList<>();
        this.paramList = new ArrayList<>();
        this.stackSize = 0;
    }

    /**
     * String
     * @param ident
     * @param stackOffset
     */
    public SymbolEntry(Token ident, String strValue, int stackOffset) {
        this.ident = ident;
        this.type = null;
        this.level = 0;
        this.isFunctionParam = false;
        this.isConstant = true;
        this.isInitialized = false;
        this.stackOffset = stackOffset;

        this.isFunction = false;
        this.paramList = null;
        this.instructions = null;
        this.stackSize = 0;
        this.isString = true;
        this.strValue = strValue;
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

    public void setType(Token type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "SymbolEntry{" +
                "ident=" + ident +
                ", type=" + type +
                ", level=" + level +
                ", stackOffset=" + stackOffset +
                ", isFunction=" + isFunction +
                ", isConstant=" + isConstant +
                ", isInitialized=" + isInitialized +
                ", isString=" + isString +
                '}';
    }
}

