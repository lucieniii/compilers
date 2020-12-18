package main.java.c0.tokenizer;

import main.java.c0.error.TokenizeError;
import main.java.c0.error.ErrorCode;
import main.java.c0.util.Pos;

public class Tokenizer {
    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        } else if (peek == '_' || Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        } else if (peek == '"') {
            return lexString();
        } else if (peek == '\'') {
            return lexChar();
        } else {
            Token t = lexOperatorOrCommentOrUnknown();
            if (t.getTokenType() == TokenType.COMMENT)
                return nextToken();
            else return t;
        }
    }

    private String nextCharOrEscape() throws TokenizeError {
        // include ' "
        Pos start = it.currentPos();
        char c = it.nextChar();
        if (c == 0)
            throw new TokenizeError(ErrorCode.IncompleteStringOrChar, start);
        if (c == '\\') {
            char peek = it.peekChar();
            if (peek == '\\' || peek == '"' || peek == '\'' || peek == 'n' || peek == 'r' || peek == 't')
                return String.valueOf(c) + it.nextChar();
            else
                throw new TokenizeError (ErrorCode.InvalidEscapeSeq, start);
        }
        return String.valueOf(c);
    }

    private Token lexUIntOrDouble() throws TokenizeError {

        Pos start = it.currentPos();
        StringBuilder strNum = new StringBuilder();
        boolean isInt = true;
        char c = it.peekChar();
        while (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
            c = it.nextChar();
            if (c == '.')
                isInt = false;
            strNum.append(c);
            c = it.peekChar();
        }
        Pos end = it.currentPos();
        if (isInt) {
            long val;
            try {
                val = Long.parseLong(strNum.toString());
            } catch (NumberFormatException e) {
                throw new TokenizeError(ErrorCode.IntegerInvalidOrOverflow, start);
            }
            return new Token(TokenType.UINT_LITERAL, val, start, end);
        } else {
            double val;
            try {
                val = Double.parseDouble(strNum.toString());
            } catch (NumberFormatException e) {
                throw new TokenizeError(ErrorCode.DoubleInvalidOrOverflow, start);
            }
            return new Token(TokenType.DOUBLE_LITERAL, val, start, end);
        }
    }

    private Token lexString() throws TokenizeError {
        Pos start = it.currentPos();
        it.nextChar(); //"
        String ce;
        StringBuilder s = new StringBuilder();
        while (true) {
            ce = nextCharOrEscape();
            if ("\"".equals(ce)) {
                return new Token(TokenType.STRING_LITERAL, s.toString(), start, it.currentPos());
            } else {
                s.append(ce);
            }
        }
    }

    private Token lexChar() throws TokenizeError {
        Pos start = it.currentPos();
        it.nextChar(); //'
        String ce = nextCharOrEscape();
        if ("'".equals(ce)) {
            throw new TokenizeError(ErrorCode.EmptyChar, start);
        } else {
            if ("'".equals(nextCharOrEscape()))
                return new Token(TokenType.CHAR_LITERAL, ce.charAt(0), start, it.currentPos());
            else
                throw new TokenizeError(ErrorCode.InvalidChar, start);
        }
    }

    private Token lexIdentOrKeyword() throws TokenizeError {

        Pos start = it.currentPos();
        StringBuilder tmp = new StringBuilder();
        char c;

        //也许可以不要
        /*if (it.peekChar() == '_' || Character.isLetter(it.peekChar())) {
            c = it.nextChar();
            tmp.append(c);
        } else {
            throw new TokenizeError(ErrorCode.InvalidIdentifier, start);
        }*/
        while (it.peekChar() == '_' || Character.isDigit(it.peekChar()) || Character.isLetter(it.peekChar())) {
            c = it.nextChar();
            tmp.append(c);
        }
        Pos end = it.currentPos();
        String res = tmp.toString();
        return switch (res) {
            case "fn" -> new Token(TokenType.FN_KW, res, start, end);
            case "let" -> new Token(TokenType.LET_KW, res, start, end);
            case "const" -> new Token(TokenType.CONST_KW, res, start, end);
            case "as" -> new Token(TokenType.AS_KW, res, start, end);
            case "while" -> new Token(TokenType.WHILE_KW, res, start, end);
            case "if" -> new Token(TokenType.IF_KW, res, start, end);
            case "else" -> new Token(TokenType.ELSE_KW, res, start, end);
            case "return" -> new Token(TokenType.RETURN_KW, res, start, end);
            case "break" -> new Token(TokenType.BREAK_KW, res, start, end);
            case "continue" -> new Token(TokenType.CONTINUE_KW, res, start, end);
            case "int" -> new Token(TokenType.INT, res, start, end);
            case "double" -> new Token(TokenType.DOUBLE, res, start, end);
            case "void" -> new Token(TokenType.VOID, res, start, end);
            /*case "getint" -> new Token(TokenType.GET_INT, res, start, end);
            case "getdouble" -> new Token(TokenType.GET_DOUBLE, res, start, end);
            case "getchar" -> new Token(TokenType.GET_CHAR, res, start, end);
            case "putint" -> new Token(TokenType.PUT_INT, res, start, end);
            case "putdouble" -> new Token(TokenType.PUT_DOUBLE, res, start, end);
            case "putchar" -> new Token(TokenType.PUT_CHAR, res, start, end);
            case "putstr" -> new Token(TokenType.PUT_STR, res, start, end);
            case "putln" -> new Token(TokenType.PUT_LN, res, start, end);*/
            default -> new Token(TokenType.IDENT, res, start, end);
        };
    }

    private Token lexOperatorOrCommentOrUnknown() throws TokenizeError {

        Pos start = it.currentPos();
        return switch (it.nextChar()) {
            case '+' -> new Token(TokenType.PLUS, '+', start, it.currentPos());
            case '-' -> {
                if (it.peekChar() == '>') {
                    it.nextChar();
                    yield new Token(TokenType.ARROW, "->", start, it.currentPos());
                } else {
                    yield new Token(TokenType.MINUS, "-", start, it.currentPos());
                }
            }
            case '*' -> new Token(TokenType.MUL, '*', start, it.currentPos());
            case '/' -> {
                if (it.peekChar() == '/') {
                    it.nextChar();
                    StringBuilder cmt = new StringBuilder();
                    char c;
                    while (it.peekChar() != '\n') {
                        c = it.nextChar();
                        cmt.append(c);
                    }
                    yield new Token(TokenType.COMMENT, cmt.toString(), start, it.currentPos());
                } else {
                    yield new Token(TokenType.DIV, '/', start, it.currentPos());
                }
            }
            case '=' -> {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    yield new Token(TokenType.EQ, "==", start, it.currentPos());
                } else {
                    yield new Token(TokenType.ASSIGN, "=", start, it.currentPos());
                }
            }
            case ';' -> new Token(TokenType.SEMICOLON, ';', start, it.currentPos());
            case '<' -> {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    yield new Token(TokenType.LE, "<=", start, it.currentPos());
                } else {
                    yield new Token(TokenType.LT, "<", start, it.currentPos());
                }
            }
            case '>' -> {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    yield new Token(TokenType.GE, ">=", start, it.currentPos());
                } else {
                    yield new Token(TokenType.GT, ">", start, it.currentPos());
                }
            }
            case '!' -> {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    yield new Token(TokenType.NEQ, "!=", start, it.currentPos());
                } else {
                    throw new TokenizeError(ErrorCode.InvalidInput, start);
                }
            }
            case '(' -> new Token(TokenType.L_PAREN, '(', start, it.currentPos());
            case ')' -> new Token(TokenType.R_PAREN, ')', start, it.currentPos());
            case '{' -> new Token(TokenType.L_BRACE, '{', start, it.currentPos());
            case '}' -> new Token(TokenType.R_BRACE, '}', start, it.currentPos());
            case ',' -> new Token(TokenType.COMMA, ',', start, it.currentPos());
            case ':' -> new Token(TokenType.COLON, ':', start, it.currentPos());
            default -> throw new TokenizeError(ErrorCode.UnknownToken, start);
        };
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
