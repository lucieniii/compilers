package main.java.c0.analyser;

import main.java.c0.tokenizer.Token;
import main.java.c0.tokenizer.Tokenizer;
import main.java.c0.tokenizer.TokenType;
import main.java.c0.error.*;
import main.java.c0.instruction.*;
import main.java.c0.util.Pos;

import java.util.*;

public class Analyser {

    //todo: 赋值表达式的值是void
    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    Stack<SymbolEntry> symbolStack = new Stack<>();
    Stack<SymbolEntry> globalSymbolStack = new Stack<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;
    int nextGlobalOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public void analyse() throws CompileError {
        analyseProgram();
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个局部变量的栈偏移
     *
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 获取下一个全局变量的栈偏移
     *
     * @return
     */
    private int getNextGlobalVariableOffset() {
        return this.nextGlobalOffset++;
    }

    private void duplicateSymbolCheck (Stack<SymbolEntry> stack, Token symbolIdent) throws AnalyzeError {
        for (SymbolEntry exist: stack)
            if (!exist.isString && exist.ident.getValueString().equals(symbolIdent.getValueString()))
                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, symbolIdent.getStartPos());
    }

    /**
     * 添加一个变量符号
     *
     * @param ident         变量标识符
     * @param type          变量类型
     * @param level         变量所在块的阶级 0为全局变量
     * @param isConstant    是否是常量
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addVariableSymbol(Token ident, Token type, int level, boolean isConstant, boolean isDeclared) throws AnalyzeError {
        duplicateSymbolCheck(level == 0 ? globalSymbolStack : symbolStack, ident);
        SymbolEntry symbol = new SymbolEntry(ident, type, level, isConstant, isDeclared, level == 0 ? getNextGlobalVariableOffset() : getNextVariableOffset());
        symbolStack.push(symbol);
    }

    /**
     * 添加一个函数符号
     *
     * @param ident         变量标识符
     * @param type          变量类型
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addFunctionSymbol (Token ident, Token type, Pos curPos) throws AnalyzeError {
        duplicateSymbolCheck(globalSymbolStack, ident);
        SymbolEntry func = new SymbolEntry(ident, type, getNextGlobalVariableOffset());
        this.instructions = func.getInstructions();
        globalSymbolStack.push(func);
    }

    /**
     * 添加一个函数符号, 默认全局
     *
     * @param ident         字符串ident
     * @throws AnalyzeError 如果重复定义了则抛异常
     * @return 字符串在全局变量中的编号
     */
    private int addStringSymbol (Token ident) throws AnalyzeError {
        int offset = getNextGlobalVariableOffset();
        for (SymbolEntry exist: globalSymbolStack)
            if (exist.isString && exist.ident.getValueString().equals(ident.getValueString()))
                return exist.stackOffset;
        SymbolEntry str = new SymbolEntry(ident, offset);
        globalSymbolStack.push(str);
        return offset;
    }

    /**
     * 尝试从符号栈中找到变量
     * @param ident     符号标识
     * @return          符号
     * @throws AnalyzeError 没有找到符号
     */
    private SymbolEntry getSymbol(Token ident) throws AnalyzeError {
        SymbolEntry find = null;
        for (SymbolEntry exist: symbolStack) {
            if (exist.ident.getValueString().equals(ident.getValueString()))
                find = exist;
        }
        if (find == null) {
            for (SymbolEntry exist: globalSymbolStack) {
                if (!exist.isString && exist.ident.getValueString().equals(ident.getValueString()))
                    find = exist;
            }
        }
        if (find == null)
            throw new AnalyzeError(ErrorCode.NotDeclared, ident.getStartPos());
        return find;
    }

    /**
     * <program> ::= (<declare statement>|<function>)*
     * @throws CompileError
     */
    private void analyseProgram() throws CompileError {
        Pos start = peek().getStartPos();

        while (!check(TokenType.EOF)) {
            //<declare statement>|<function>
            switch (peek().getTokenType()) {
                //<declare statement>
                case LET_KW, CONST_KW -> {
                    analyseDeclareStatement("_GLOBAL", 0);
                }
                //<function>
                case FN_KW -> {
                    analyseFunction();
                }
                //ERROR
                default -> throw new AnalyzeError(ErrorCode.InvalidGlobalDeclaration, start);
            }
        }
    }

    /**
     * <declare statement> ::= <let declare statement>|<const declare statement>
     * @throws CompileError
     * @param name
     * @param level
     */
    private void analyseDeclareStatement(String name, int level) throws CompileError {
        Pos start = peek().getStartPos();

        switch (peek().getTokenType()) {
            case LET_KW -> {
                analyseLetDeclareStatement(name, level);
            }
            case CONST_KW -> {
                analyseConstDeclareStatement(name, level);
            }
            default -> throw new AnalyzeError(ErrorCode.InvalidDeclaration, start);
        }
    }

    /**
     * <let declare statement> ::= <LET> <IDENT> <COLON> <TYPE> (<ASSIGN> <expression>)? <SEMICOLON>
     * @throws CompileError
     * @param fnName
     * @param level
     */
    private void analyseLetDeclareStatement(String fnName, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.LET_KW);
        String name = expect(TokenType.IDENT).getValueString();
        //todo: handle symbol
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidVariableType, start);
        //todo: handle variable type
        Token type = next();
        if (nextIf(TokenType.ASSIGN) != null)
            analyseExpression(); //todo: 参数、返回值未知
        expect(TokenType.SEMICOLON);
    }

    /**
     * <let declare statement> ::= <CONST> <IDENT> <COLON> <TYPE> <ASSIGN> <expression> <SEMICOLON>
     * @throws CompileError
     * @param fnName
     * @param level
     */
    private void analyseConstDeclareStatement(String fnName, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.CONST_KW);
        String name = expect(TokenType.IDENT).getValueString();
        //todo: handle symbol
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidVariableType, start);
        Token type = next(); //todo: handle type
        //todo: handle variable type
        expect(TokenType.ASSIGN);
        analyseExpression(); //todo: 参数、返回值未知
        expect(TokenType.SEMICOLON);
    }

    private void analyseStatement(String fnName, int level) throws CompileError {
        Pos start = peek().getStartPos();

        switch (peek().getTokenType()) {
            case LET_KW, CONST_KW -> analyseDeclareStatement(fnName, level);
            case L_BRACE -> analyseBlockStatement(fnName, level);
            case IF_KW -> analyseIfStatement(fnName, level);
            case WHILE_KW -> analyseWhileStatement(fnName, level);
            //analyseContinueStatement
            case CONTINUE_KW -> {
                expect(TokenType.CONTINUE_KW);
                expect(TokenType.SEMICOLON);
            }
            //analyseBreakStatement
            case BREAK_KW -> {
                expect(TokenType.BREAK_KW);
                expect(TokenType.SEMICOLON);
            }
            //analyseReturnStatement
            case RETURN_KW -> {
                expect(TokenType.RETURN_KW);
                if (!check(TokenType.SEMICOLON)) {
                    analyseExpression();
                } //todo: handle return value
                expect(TokenType.SEMICOLON);
            }
            //analyseEmptyStatement
            case SEMICOLON -> {
                //todo: handle no op
            }
            //analyseExpressionStatement
            default -> {
                analyseExpression();
                expect(TokenType.SEMICOLON);
            }
        }
    }

    private void analyseBlockStatement(String fnName, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.L_BRACE);
        while (!check(TokenType.R_BRACE))
            analyseStatement(fnName, level + 1);
        expect(TokenType.R_BRACE);
    }

    private void analyseIfStatement(String fnName, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.IF_KW);
        analyseExpression(); //todo: handle boolean
        analyseBlockStatement(fnName, level + 1);
        while (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)) {
                expect(TokenType.IF_KW);
                analyseExpression(); //todo: handle boolean
            }
            analyseBlockStatement(fnName, level + 1);
        }
    }

    private void analyseWhileStatement(String fnName, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.WHILE_KW);
        analyseExpression(); //todo: handle boolean
        analyseBlockStatement(fnName, level + 1);
    }

    private void analyseExpression() throws CompileError {
        Pos start = peek().getStartPos();

        switch (peek().getTokenType()) {
            //analyseNegateExpression
            case MINUS -> {
                expect(TokenType.MINUS);
                analyseExpression();
                //todo: 转换为相反数
            }
            case IDENT -> {
                String name = expect(TokenType.IDENT).getValueString();
                //todo: handle symbol
                switch (peek().getTokenType()) {
                    //analyseAssignExpression
                    case ASSIGN -> {
                        expect(TokenType.ASSIGN);
                        analyseExpression();
                        //todo: handle assign
                    }
                    //analyseCallExpression
                    case L_PAREN -> {
                        expect(TokenType.L_PAREN);
                        //todo: handle call
                        analyseCallParamList(name);
                        expect(TokenType.R_PAREN);
                    }
                    //analyseIdentExpression
                    default -> {
                        //todo: handle symbol?

                    }
                }
            }
            //analyseLiteralExpression
            case UINT_LITERAL, DOUBLE_LITERAL, CHAR_LITERAL -> {
                long value = next().getValueLong();
                //todo: handle value
            }
            //analyseLiteralExpression
            case STRING_LITERAL -> {
                String value = next().getValueString();
                //todo: handle value
            }
            //analyseGroupExpression
            case L_PAREN -> {
                expect(TokenType.L_PAREN);
                analyseExpression();
                expect(TokenType.R_PAREN);
            }
            default -> throw new AnalyzeError(ErrorCode.InvalidExpression, start);
        }
        analyseAdvanceExpression();
    }

    private void analyseAdvanceExpression() throws CompileError {
        Pos start = peek().getStartPos();

        switch (peek().getTokenType()) {
            //analyseOperatorExpression
            case PLUS, MINUS, MUL, DIV, EQ, NEQ, LT, GT, LE, GE -> {
                Token op = next(); //todo: do calc
                analyseExpression();
                analyseAdvanceExpression();
            }
            //analyseAsExpression
            case AS_KW -> {
                expect(TokenType.AS_KW);
                if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
                    throw new AnalyzeError(ErrorCode.InvalidType, start);
                Token type = next(); //todo: handle type
                analyseAdvanceExpression();
            }
        }
    }

    /**
     * <call param list> ::= <expression> (<COMMA> <expression>)*
     * @param fnName
     * @throws CompileError
     */
    private void analyseCallParamList(String fnName) throws CompileError {
        Pos start = peek().getStartPos();

        if (check(TokenType.R_PAREN))
            return;
        analyseExpression();
        while (nextIf(TokenType.COMMA) != null)
            analyseExpression();
    }

    /**
     * <function> ::= <FN_KW> <IDENT> <LPAREN> <function param list> <RPAREN> <ARROW> <TYPE> <block statement>
     * @throws CompileError
     */
    private void analyseFunction() throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.FN_KW);
        String name = expect(TokenType.IDENT).getValueString();
        //todo: handle symbol
        expect(TokenType.L_PAREN);
        analyseParamList(name);
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE) && !check(TokenType.VOID))
            throw new AnalyzeError(ErrorCode.InvalidFunctionReturnType, start);
        Token type = next();
        //todo: handle function type
        analyseBlockStatement(name, 1);
    }

    /**
     * <function param list> ::= <function param> (<COMMA> <function param>)*
     * @param fnName
     * @throws CompileError
     */
    private void analyseParamList(String fnName) throws CompileError {
        Pos start = peek().getStartPos();

        if (check(TokenType.R_PAREN))
            return;
        analyseParam(fnName);
        while (nextIf(TokenType.COMMA) != null)
            analyseParam(fnName);
    }

    /**
     * <function param> ::= <CONST>? <IDENT> <COLON> <TYPE>
     * @param fnName
     * @throws CompileError
     */
    private void analyseParam(String fnName) throws CompileError {
        Pos start = peek().getStartPos();

        //check if the param is a constant
        boolean isConst = false;
        if (nextIf(TokenType.CONST_KW) != null)
            isConst = true;
        String name = expect(TokenType.IDENT).getValueString();
        //todo: handle symbol
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidParamType, start);
        Token type = next();
        //todo: handle function param type
    }
}
