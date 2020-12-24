package main.java.c0.tokenizer;

import main.java.c0.error.TokenizeError;
import main.java.c0.util.Pos;

import java.util.Objects;

public class Token {
    private TokenType tokenType;
    private Object value;
    private Pos startPos;
    private Pos endPos;

    public Token(TokenType tokenType, Object value, Pos startPos, Pos endPos) {
        this.tokenType = tokenType;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public Token(Token token) {
        this.tokenType = token.tokenType;
        this.value = token.value;
        this.startPos = token.startPos;
        this.endPos = token.endPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Token token = (Token) o;
        return tokenType == token.tokenType && Objects.equals(value, token.value)
                && Objects.equals(startPos, token.startPos) && Objects.equals(endPos, token.endPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenType, value, startPos, endPos);
    }

    public String getValueString() {
        if (value instanceof Long || value instanceof String) {
            return value.toString();
        } else if (value instanceof Double) {
            return String.valueOf(Double.doubleToRawLongBits((Double) value));
        } else if (value instanceof Character) {
            if (tokenType == TokenType.CHAR_LITERAL)
                return String.valueOf((int) (char)value);
            else return value.toString();
        }
        throw new Error("No suitable cast for token value.");
    }

    public Long getValueLong() {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Double) {
            return Double.doubleToRawLongBits((Double) value);
        } else if (value instanceof Character) {
            return (long)(char)value;
        } else throw new Error("Can not get long value of a non-number token.");
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Pos getStartPos() {
        return startPos;
    }

    public void setStartPos(Pos startPos) {
        this.startPos = startPos;
    }

    public Pos getEndPos() {
        return endPos;
    }

    public void setEndPos(Pos endPos) {
        this.endPos = endPos;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        //sb.append("Line: ").append(this.startPos.row).append(' ');
        //sb.append("Column: ").append(this.startPos.col).append(' ');
        //sb.append("Type: ").append(this.tokenType).append(' ');
        //sb.append("Value: ").append(this.value);
        sb.append("Value: ").append(getValueString());
        return sb.toString();
    }

    public String toHexString(boolean isFunction) {
        StringBuilder hex = new StringBuilder();
        switch (this.getValueString()) {
            case "putchar", "putint", "putdouble", "putln", "putstr" ,"getchar", "getint", "getdouble"
                    -> {
                for (char c : this.getValueString().toCharArray()) {
                    hex.append(Integer.toHexString(c));
                    hex.append(" ");
                }
            }
            default -> {
                if (tokenType == TokenType.STRING_LITERAL || isFunction) {
                    for (char c : this.getValueString().toCharArray()) {
                        hex.append(Integer.toHexString(c));
                        hex.append(" ");
                    }
                } else {
                    hex.append("00 00 00 00 00 00 00 00");
                }
            }
        }
        return hex.toString();
    }

    public String toStringAlt() {
        return new StringBuilder().append("Token(").append(this.tokenType).append(", value: ").append(value)
                .append("at: ").append(this.startPos).toString();
    }
}
