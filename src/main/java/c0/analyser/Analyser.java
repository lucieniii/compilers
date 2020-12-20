package main.java.c0.analyser;

import main.java.c0.tokenizer.Token;
import main.java.c0.tokenizer.Tokenizer;
import main.java.c0.tokenizer.TokenType;
import main.java.c0.error.*;
import main.java.c0.instruction.*;
import main.java.c0.util.Pos;

import java.util.*;

public class Analyser {

    //todo: instructions.add(new Instruction(Operation.SUB));
    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    int paramCount = 0;
    Stack<SymbolEntry> symbolStack = new Stack<>();
    Stack<SymbolEntry> globalSymbolStack = new Stack<>();

    /** 下一个变量的栈偏移 */
    int nextParamOffset = 0;
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

    private int getNextParamOffset() {
        return this.nextParamOffset++;
    }

    /**
     * 获取下一个全局变量的栈偏移
     *
     * @return
     */
    private int getNextGlobalVariableOffset() {
        return this.nextGlobalOffset++;
    }

    private void duplicateSymbolCheck (Stack<SymbolEntry> stack, Token symbolIdent, int level) throws AnalyzeError {
        for (SymbolEntry exist: stack)
            if (!exist.isString && exist.level == level && exist.ident.getValueString().equals(symbolIdent.getValueString()))
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
    private SymbolEntry addVariableSymbol(Token ident, Token type, int level, boolean isFunctionParam, boolean isConstant, boolean isInitialized) throws AnalyzeError {
        duplicateSymbolCheck(level == 0 ? globalSymbolStack : symbolStack, ident, level);
        int stackOffset = level == 0 ? getNextGlobalVariableOffset() : (isFunctionParam ? getNextParamOffset() : getNextVariableOffset());
        SymbolEntry symbol = new SymbolEntry(ident, type, level, isFunctionParam, isConstant, isInitialized, stackOffset);
        symbolStack.push(symbol);
        return symbol;
    }

    /**
     * 添加一个函数符号
     *
     * @param ident         变量标识符
     * @param type          变量类型
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private SymbolEntry addFunctionSymbol (Token ident, Token type) throws AnalyzeError {
        duplicateSymbolCheck(globalSymbolStack, ident, 0);
        SymbolEntry func = new SymbolEntry(ident, type, getNextGlobalVariableOffset());
        this.instructions = func.getInstructions();
        globalSymbolStack.push(func);
        return func;
    }

    /**
     * 添加一个函数符号, 默认全局
     *
     * @param ident         字符串ident
     * @throws AnalyzeError 如果重复定义了则抛异常
     * @return 字符串在全局变量中的编号
     */
    private int addStringSymbol (Token ident, String strValue) throws AnalyzeError {
        int offset = getNextGlobalVariableOffset();
        for (SymbolEntry exist: globalSymbolStack)
            if (exist.isString && exist.ident.getValueString().equals(ident.getValueString()))
                return exist.stackOffset;
        SymbolEntry str = new SymbolEntry(ident, strValue, offset);
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
        for (SymbolEntry exist : symbolStack) {
            if (exist.ident.getValueString().equals(ident.getValueString()))
                find = exist;
        }
        if (find == null) {
            for (SymbolEntry exist : globalSymbolStack) {
                if (!exist.isString && exist.ident.getValueString().equals(ident.getValueString()))
                    find = exist;
            }
        }
        if (find == null) {
            switch (ident.getValueString()) {
                case "putchar", "putint", "putdouble", "putln", "putstr"
                        -> {
                    addFunctionSymbol(ident, new Token(TokenType.VOID, "void", ident.getStartPos(), ident.getEndPos()));
                    find = getSymbol(ident);
                }
                case "getchar", "getint" -> {
                    addFunctionSymbol(ident, new Token(TokenType.INT, "int", ident.getStartPos(), ident.getEndPos()));
                    find = getSymbol(ident);
                }
                case "getdouble" -> {
                    addFunctionSymbol(ident, new Token(TokenType.DOUBLE, "double", ident.getStartPos(), ident.getEndPos()));
                    find = getSymbol(ident);
                }
                default -> throw new AnalyzeError(ErrorCode.NotDeclared, ident.getStartPos());
            }
        }
        return find;
    }

    /**
     * 将上一块符号栈弹出
     * @param level     当前块的阶级
     */
    private void evictSymbolBlock(int level) {
        while (!symbolStack.empty() && symbolStack.peek().level == level) {
            int __ = symbolStack.pop().isFunctionParam ? this.paramCount-- : this.nextOffset--;
        }
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
                    analyseDeclareStatement(new Token(TokenType.IDENT, "_GLOBAL", start, start), 0);
                }
                //<function>
                case FN_KW -> {
                    paramCount = 0;
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
     * @param fnToken
     * @param level
     */
    private void analyseDeclareStatement(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        switch (peek().getTokenType()) {
            case LET_KW -> {
                analyseLetDeclareStatement(fnToken, level);
            }
            case CONST_KW -> {
                analyseConstDeclareStatement(fnToken, level);
            }
            default -> throw new AnalyzeError(ErrorCode.InvalidDeclaration, start);
        }
    }

    /**
     * <let declare statement> ::= <LET> <IDENT> <COLON> <TYPE> (<ASSIGN> <expression>)? <SEMICOLON>
     * @throws CompileError
     * @param fnToken
     * @param level
     */
    private void analyseLetDeclareStatement(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.LET_KW);
        Token ident = expect(TokenType.IDENT);
        //todo: handle symbol
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidVariableType, start);
        //todo: handle variable type
        Token type = next();
        SymbolEntry s = addVariableSymbol(ident, type, level, false, false, false);
        if (nextIf(TokenType.ASSIGN) != null) {
            start = peek().getStartPos();
            instructions.add(new Instruction(instructions.size() - 1, Operation.ARG_A, s.stackOffset));
            if (analyseExpression() != type.getTokenType()) //todo: 参数、返回值未知
                throw new AnalyzeError(ErrorCode.ConflictType, start);
        }
        expect(TokenType.SEMICOLON);
    }

    /**
     * <let declare statement> ::= <CONST> <IDENT> <COLON> <TYPE> <ASSIGN> <expression> <SEMICOLON>
     * @throws CompileError
     * @param fnToken
     * @param level
     */
    private void analyseConstDeclareStatement(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.CONST_KW);
        Token ident = expect(TokenType.IDENT);
        //todo: handle symbol
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidVariableType, start);
        Token type = next();
        //todo: handle type
        expect(TokenType.ASSIGN);
        start = peek().getStartPos();
        if (analyseExpression() != type.getTokenType()) //todo: 参数、返回值未知
            throw new AnalyzeError(ErrorCode.ConflictType, start);
        addVariableSymbol(ident, type, level, false, true, true);
        expect(TokenType.SEMICOLON);
    }

    private void analyseStatement(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        SymbolEntry fnSymbol = getSymbol(fnToken);
        switch (peek().getTokenType()) {
            case LET_KW, CONST_KW -> analyseDeclareStatement(fnToken, level);
            case L_BRACE -> analyseBlockStatement(fnToken, level + 1);
            case IF_KW -> analyseIfStatement(fnToken, level);
            case WHILE_KW -> analyseWhileStatement(fnToken, level);
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
                start = peek().getStartPos();
                TokenType retType = TokenType.VOID;
                if (!check(TokenType.SEMICOLON)) {
                    retType = analyseExpression();
                } //todo: handle return value
                if (retType != fnSymbol.type.getTokenType())
                    throw new AnalyzeError(ErrorCode.ConflictType, start);
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

    private void analyseBlockStatement(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.L_BRACE);
        while (!check(TokenType.R_BRACE))
            analyseStatement(fnToken, level);
        expect(TokenType.R_BRACE);
        evictSymbolBlock(level);
    }

    private void analyseIfStatement(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.IF_KW);
        analyseExpression(); //todo: handle boolean
        analyseBlockStatement(fnToken, level + 1);
        while (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)) {
                expect(TokenType.IF_KW);
                analyseExpression(); //todo: handle boolean
                analyseBlockStatement(fnToken, level + 1);
            } else {
                analyseBlockStatement(fnToken, level + 1);
                return;
            }
        }
    }

    private void analyseWhileStatement(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.WHILE_KW);
        analyseExpression(); //todo: handle boolean
        analyseBlockStatement(fnToken, level + 1);
    }

    private TokenType analyseExpression() throws CompileError { //todo: instruction分析到这
        Pos start = peek().getStartPos();

        TokenType type;
        switch (peek().getTokenType()) {
            //analyseNegateExpression
            case MINUS -> {
                expect(TokenType.MINUS);
                start = peek().getStartPos();
                type = analyseExpression();
                //todo: 转换为相反数
                if (type == TokenType.DOUBLE)
                    instructions.add(new Instruction(instructions.size() - 1, Operation.NEG_F));
                else if (type == TokenType.INT)
                    instructions.add(new Instruction(instructions.size() - 1, Operation.NEG_I));
                else throw new AnalyzeError(ErrorCode.InvalidNegative, start);
            }
            case IDENT -> {
                Token ident = expect(TokenType.IDENT);
                //todo: handle symbol
                SymbolEntry symbol = getSymbol(ident);
                switch (peek().getTokenType()) {
                    //analyseAssignExpression
                    case ASSIGN -> {
                        expect(TokenType.ASSIGN);
                        if (symbol.isConstant)
                            throw new AnalyzeError(ErrorCode.AssignToConstant, ident.getStartPos());
                        //todo: handle assign
                        if (symbol.type.getTokenType() != analyseExpression())
                            throw new AnalyzeError(ErrorCode.ConflictType, ident.getStartPos());
                        type = TokenType.VOID;
                    }
                    //analyseCallExpression
                    case L_PAREN -> {
                        expect(TokenType.L_PAREN);
                        //todo: handle call
                        analyseCallParamList(ident);
                        expect(TokenType.R_PAREN);
                        type = symbol.type.getTokenType();
                    }
                    //analyseIdentExpression
                    default -> {
                        //todo: handle symbol?
                        type = symbol.type.getTokenType();
                    }
                }
            }
            //analyseLiteralExpression
            case UINT_LITERAL, DOUBLE_LITERAL, CHAR_LITERAL -> {
                switch (peek().getTokenType()) {
                    case UINT_LITERAL, CHAR_LITERAL -> type = TokenType.INT;
                    default -> type = TokenType.DOUBLE;
                }
                long value = next().getValueLong();
                //todo: handle value
            }
            //analyseLiteralExpression
            case STRING_LITERAL -> {
                Token strIdent = next();
                //todo: handle string value
                addStringSymbol(strIdent, strIdent.getValueString());
                type = TokenType.STRING_LITERAL;
            }
            //analyseGroupExpression
            case L_PAREN -> {
                expect(TokenType.L_PAREN);
                type = analyseExpression();
                expect(TokenType.R_PAREN);
            }
            default -> throw new AnalyzeError(ErrorCode.InvalidExpression, start);
        }
        TokenType advType = analyseAdvanceExpression(type);
        if (advType == TokenType.EOE)
            return type;
        else
            return advType;
    }

    private TokenType analyseAdvanceExpression(TokenType type) throws CompileError {
        Pos start = peek().getStartPos();

        switch (peek().getTokenType()) {
            //analyseOperatorExpression
            case PLUS, MINUS, MUL, DIV, EQ, NEQ, LT, GT, LE, GE -> {
                Token op = next(); //todo: do calc
                start = peek().getStartPos();
                if (type != analyseExpression())
                    throw new AnalyzeError(ErrorCode.ConflictType, start);
                TokenType advType = analyseAdvanceExpression(type);
                if (advType == TokenType.EOE)
                    return type;
                if (type != analyseAdvanceExpression(type))
                    throw new AnalyzeError(ErrorCode.ConflictType, start);
                return type;
            }
            //analyseAsExpression
            case AS_KW -> {
                expect(TokenType.AS_KW);
                if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
                    throw new AnalyzeError(ErrorCode.InvalidType, start);
                Token asType = next(); //todo: handle type
                boolean isNextAs = peek().getTokenType() == TokenType.AS_KW;
                TokenType advType = analyseAdvanceExpression(asType.getTokenType());
                if (isNextAs)
                    return advType;
                else
                    return asType.getTokenType();
            }
        }
        return TokenType.EOE;
    }

    /**
     * <call param list> ::= <expression> (<COMMA> <expression>)*
     * @param fnToken
     * @throws CompileError
     */
    private void analyseCallParamList(Token fnToken) throws CompileError {
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
        Token ident = expect(TokenType.IDENT);
        //todo: handle symbol
        expect(TokenType.L_PAREN);
        analyseParamList(ident, 1);
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE) && !check(TokenType.VOID))
            throw new AnalyzeError(ErrorCode.InvalidFunctionReturnType, start);
        Token type = next();
        addFunctionSymbol(ident, type);
        //todo: handle function type
        analyseBlockStatement(ident, 1);
    }

    /**
     * <function param list> ::= <function param> (<COMMA> <function param>)*
     * @param fnToken
     * @param level
     * @throws CompileError
     */
    private void analyseParamList(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        if (check(TokenType.R_PAREN))
            return;
        analyseParam(fnToken, level);
        while (nextIf(TokenType.COMMA) != null)
            analyseParam(fnToken, level);
    }

    /**
     * <function param> ::= <CONST>? <IDENT> <COLON> <TYPE>
     * @param fnToken
     * @param level
     * @throws CompileError
     */
    private void analyseParam(Token fnToken, int level) throws CompileError {
        Pos start = peek().getStartPos();

        //check if the param is a constant
        boolean isConst = false;
        if (nextIf(TokenType.CONST_KW) != null)
            isConst = true;
        Token ident = expect(TokenType.IDENT);
        //todo: handle symbol
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidParamType, start);
        Token type = next();
        //todo: handle function param type
        addVariableSymbol(ident, type, level,true, isConst, true);
        this.paramCount++;
    }
}
