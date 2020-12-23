package main.java.c0.analyser;

import main.java.c0.tokenizer.Token;
import main.java.c0.tokenizer.Tokenizer;
import main.java.c0.tokenizer.TokenType;
import main.java.c0.error.*;
import main.java.c0.instruction.*;
import main.java.c0.util.Pos;

import java.io.PrintStream;
import java.util.*;

public class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    Stack<SymbolEntry> symbolStack = new Stack<>();
    Stack<SymbolEntry> globalSymbolStack = new Stack<>();

    /** 下一个变量的栈偏移 */
    int nextFuncOffset = 0;
    int nextParamOffset = 0;
    int nextOffset = 0;
    int nextGlobalOffset = 0;

    /** While 层数 **/
    int whileBlock = 0;
    Stack<Instruction> bcStack = new Stack<>();

    /** 是否输出指令 **/
    boolean silent = false;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public ArrayList<Instruction> analyse(PrintStream output) throws CompileError {
        analyseProgram();
        ArrayList<Instruction> allInstructions = new ArrayList<>();
        for (SymbolEntry globalSymbol : globalSymbolStack) {
            switch (globalSymbol.ident.getValueString()) {
                case "putchar", "putint", "putdouble", "putln", "putstr" ,"getchar", "getint", "getdouble"
                        -> output.println(globalSymbol.ident.toString());
                default -> {
                    if (!globalSymbol.isFunction)
                        output.println(globalSymbol.ident.toString());
                }
            }
        }
        for (SymbolEntry globalSymbol : globalSymbolStack) {
            if (globalSymbol.isFunction)
                output.println(globalSymbol.ident.toString());
        }
        output.println();
        for (SymbolEntry globalSymbol : globalSymbolStack) {
            if (globalSymbol.isFunction)
                for (Instruction i : globalSymbol.instructions)
                    output.println(i.toString());
            output.println();
        }
        return allInstructions;
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

    private int getNextFuncOffset() {
        return this.nextFuncOffset++;
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
        var __ = level == 0 ? globalSymbolStack.push(symbol) : symbolStack.push(symbol);
        return symbol;
    }

    /**
     * 添加一个库函数符号
     *
     * @param ident         变量标识符
     * @param type          变量类型
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private SymbolEntry addBaseFunctionSymbol (Token ident, Token type) throws AnalyzeError {
        duplicateSymbolCheck(globalSymbolStack, ident, 0);
        SymbolEntry func = new SymbolEntry(ident, type, getNextGlobalVariableOffset());
        globalSymbolStack.push(func);
        return func;
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
        SymbolEntry func = new SymbolEntry(ident, type, getNextFuncOffset());
        this.instructions = func.getInstructions();
        globalSymbolStack.push(func);
        return func;
    }

    /**
     * 添加一个字符串, 默认全局
     *
     * @param ident         字符串ident
     * @throws AnalyzeError 如果重复定义了则抛异常
     * @return 字符串在全局变量中的编号
     */
    private int addStringSymbol (Token ident, String strValue) throws AnalyzeError {
        for (SymbolEntry exist: globalSymbolStack)
            if (exist.isString && exist.ident.getValueString().equals(ident.getValueString()))
                return exist.stackOffset;
        int offset = getNextGlobalVariableOffset();
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
                    SymbolEntry fnSymbol = addBaseFunctionSymbol(ident, new Token(TokenType.VOID, "void", ident.getStartPos(), ident.getEndPos()));
                    switch (ident.getValueString()) {
                        case "putchar", "putint" -> fnSymbol.paramList.add(TokenType.INT);
                        case "putdouble" -> fnSymbol.paramList.add(TokenType.DOUBLE);
                        case "putstr" -> fnSymbol.paramList.add(TokenType.STRING_LITERAL);
                    }
                    find = getSymbol(ident);
                }
                case "getchar", "getint" -> {
                    addBaseFunctionSymbol(ident, new Token(TokenType.INT, "int", ident.getStartPos(), ident.getEndPos()));
                    find = getSymbol(ident);
                }
                case "getdouble" -> {
                    addBaseFunctionSymbol(ident, new Token(TokenType.DOUBLE, "double", ident.getStartPos(), ident.getEndPos()));
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
            int __ = symbolStack.pop().isFunctionParam ? this.nextParamOffset-- : this.nextOffset--;
        }
    }

    private void addInstruction(Instruction instruction) {
        if (!silent)
            instructions.add(instruction);
    }

    /**
     * <program> ::= (<declare statement>|<function>)*
     * @throws CompileError
     */
    private void analyseProgram() throws CompileError {
        Pos start = peek().getStartPos();
        SymbolEntry _start = addFunctionSymbol(new Token(TokenType.IDENT, "_GLOBAL", start, start), new Token(TokenType.VOID, "_GLOBAL", start, start));

        while (!check(TokenType.EOF)) {
            //<declare statement>|<function>
            switch (peek().getTokenType()) {
                //<declare statement>
                case LET_KW, CONST_KW -> {
                    analyseDeclareStatement(_start, 0);
                }
                //<function>
                case FN_KW -> {
                    //Instruction.stackUse = 0;
                    analyseFunction();
                }
                //ERROR
                default -> throw new AnalyzeError(ErrorCode.InvalidGlobalDeclaration, start);
            }
        }
        int mainId = -1;
        for (SymbolEntry func : globalSymbolStack)
            if (func.isFunction && func.ident.getValueString().equals("main"))
                mainId = func.stackOffset;
        if (mainId == -1)
            throw new AnalyzeError(ErrorCode.NoMainFunction, start);
        _start.instructions.add(new Instruction(_start.instructions.size() - 1, Operation.CALL, mainId, 0));
    }

    /**
     * <declare statement> ::= <let declare statement>|<const declare statement>
     * @throws CompileError
     * @param fnSymbol
     * @param level
     */
    private void analyseDeclareStatement(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        switch (peek().getTokenType()) {
            case LET_KW -> {
                analyseLetDeclareStatement(fnSymbol, level);
            }
            case CONST_KW -> {
                analyseConstDeclareStatement(fnSymbol, level);
            }
            default -> throw new AnalyzeError(ErrorCode.InvalidDeclaration, start);
        }
    }

    /**
     * <let declare statement> ::= <LET> <IDENT> <COLON> <TYPE> (<ASSIGN> <expression>)? <SEMICOLON>
     * @throws CompileError
     * @param fnSymbol
     * @param level
     */
    private void analyseLetDeclareStatement(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.LET_KW);
        Token ident = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidVariableType, start);
        Token type = next();
        SymbolEntry s = addVariableSymbol(ident, type, level, false, false, false);
        if (nextIf(TokenType.ASSIGN) != null) {
            start = peek().getStartPos();
            if (s.level == 0) {
                addInstruction(new Instruction(instructions.size() - 1, Operation.GLOB_A, s.stackOffset));
            } else {
                addInstruction(new Instruction(instructions.size() - 1, Operation.LOC_A, s.stackOffset));
            }
            if (analyseExpression() != type.getTokenType())
                throw new AnalyzeError(ErrorCode.ConflictType, start);
            addInstruction(new Instruction(instructions.size() - 1, Operation.STORE_64));
            s.setInitialized(true);
        }

        expect(TokenType.SEMICOLON);
    }

    /**
     * <let declare statement> ::= <CONST> <IDENT> <COLON> <TYPE> <ASSIGN> <expression> <SEMICOLON>
     * @throws CompileError
     * @param fnSymbol
     * @param level
     */
    private void analyseConstDeclareStatement(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.CONST_KW);
        Token ident = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidVariableType, start);
        Token type = next();
        expect(TokenType.ASSIGN);
        start = peek().getStartPos();
        SymbolEntry s = addVariableSymbol(ident, type, level, false, true, true);
        if (s.level == 0) {
            addInstruction(new Instruction(instructions.size() - 1, Operation.GLOB_A, s.stackOffset));
        } else {
            addInstruction(new Instruction(instructions.size() - 1, Operation.LOC_A, s.stackOffset));
        }
        if (analyseExpression() != type.getTokenType())
            throw new AnalyzeError(ErrorCode.ConflictType, start);
        addInstruction(new Instruction(instructions.size() - 1, Operation.STORE_64));
        expect(TokenType.SEMICOLON);
    }

    private boolean analyseStatement(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        boolean ret = false;
        switch (peek().getTokenType()) {
            case LET_KW, CONST_KW -> analyseDeclareStatement(fnSymbol, level);
            case L_BRACE -> ret = analyseBlockStatement(fnSymbol, level + 1);
            case IF_KW -> ret = analyseIfStatement(fnSymbol, level);
            case WHILE_KW -> ret = analyseWhileStatement(fnSymbol, level);
            //analyseContinueStatement
            case CONTINUE_KW -> {
                if (whileBlock == 0)
                    throw new AnalyzeError(ErrorCode.NotInWhile, start);
                expect(TokenType.CONTINUE_KW);
                addInstruction(new Instruction(instructions.size() - 1, Operation.BR, -whileBlock));
                bcStack.push(instructions.get(instructions.size() - 1));
                expect(TokenType.SEMICOLON);
            }
            //analyseBreakStatement
            case BREAK_KW -> {
                if (whileBlock == 0)
                    throw new AnalyzeError(ErrorCode.NotInWhile, start);
                expect(TokenType.BREAK_KW);
                addInstruction(new Instruction(instructions.size() - 1, Operation.BR, whileBlock));
                bcStack.push(instructions.get(instructions.size() - 1));
                expect(TokenType.SEMICOLON);
            }
            //analyseReturnStatement
            case RETURN_KW -> {
                expect(TokenType.RETURN_KW);
                start = peek().getStartPos();
                TokenType retType = TokenType.VOID;
                addInstruction(new Instruction(instructions.size() - 1, Operation.ARG_A, 0));
                if (!check(TokenType.SEMICOLON)) {
                    retType = analyseExpression();
                }
                if (retType != fnSymbol.type.getTokenType())
                    throw new AnalyzeError(ErrorCode.ConflictFunctionReturnType, start);
                addInstruction(new Instruction(instructions.size() - 1, Operation.STORE_64));
                addInstruction(new Instruction(instructions.size() - 1, Operation.RET));
                expect(TokenType.SEMICOLON);
                ret = true;
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
        if (Instruction.stackUse != 0)
            addInstruction(new Instruction(instructions.size(), Operation.POP_N, Instruction.stackUse));
        return ret;
    }

    private boolean analyseBlockStatement(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.L_BRACE);
        int bcCount = bcStack.size(); //todo: break&continue后的代码省略
        boolean ret = false;
        while (!check(TokenType.R_BRACE)) {
            ret = analyseStatement(fnSymbol, level) || ret;
            if (bcCount < bcStack.size())
                silent = true;
        }
        silent = false;
        expect(TokenType.R_BRACE);
        evictSymbolBlock(level);

        return ret;
    }

    private boolean analyseIfStatement(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        boolean haveElse = false;
        expect(TokenType.IF_KW);
        analyseExpression();
        addInstruction(new Instruction(instructions.size() - 1, Operation.BR_TRUE, 1));
        addInstruction(new Instruction(instructions.size() - 1, Operation.BR, -1));
        int codeStart = instructions.size();
        boolean ret = analyseBlockStatement(fnSymbol, level + 1);
        int codeEnd = instructions.size();
        instructions.get(codeStart - 1).setX(codeEnd - codeStart + 1);

        addInstruction(new Instruction(instructions.size() - 1, Operation.BR, -1));
        codeStart = instructions.size();
        if (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            start = peek().getStartPos();
            if (check(TokenType.L_BRACE)) {
                haveElse = true;
                ret = analyseBlockStatement(fnSymbol, level + 1) && ret;
            } else if (check(TokenType.IF_KW)) {
                ret = analyseIfStatement(fnSymbol, level) && ret;
            } else throw new AnalyzeError(ErrorCode.MissingBlockOrIfAfterElse, start);
        }
        codeEnd = instructions.size();
        instructions.get(codeStart - 1).setX(codeEnd - codeStart);

        return ret && haveElse;
    }

    private boolean analyseWhileStatement(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.WHILE_KW);
        int booleanStart = instructions.size();
        analyseExpression();
        addInstruction(new Instruction(instructions.size() - 1, Operation.BR_TRUE, 1));
        addInstruction(new Instruction(instructions.size() - 1, Operation.BR, -1));
        int codeStart = instructions.size();
        whileBlock++;
        boolean ret = analyseBlockStatement(fnSymbol, level + 1);
        int codeEnd = instructions.size();
        instructions.get(codeStart - 1).setX(codeEnd - codeStart + 1);
        addInstruction(new Instruction(instructions.size() - 1, Operation.BR, booleanStart - codeEnd));
        if (!bcStack.isEmpty()) {
            int x = (int) (bcStack.peek().getX());
            x = x > 0 ? x : -x;
            while (x == whileBlock) {
                Instruction bc = bcStack.pop();
                if (bc.getX() < 0) { //continue
                    bc.setX(booleanStart - bc.getIdx() - 1);
                } else { //break
                    bc.setX(codeEnd - bc.getIdx());
                }
                if (bcStack.isEmpty())
                    break;
                x = (int) (bcStack.peek().getX());
                x = x > 0 ? x : -x;
            }
        }
        whileBlock--;

        return ret;
    }

    /**
     * <expression> ::= <calculate expression> (<compare operator><calculate expression>)
     * @return
     * @throws CompileError
     */
    private TokenType analyseExpression() throws CompileError {
        Pos start = peek().getStartPos();

        TokenType type = analyseCalculateExpression();
        while (true) {
            switch (peek().getTokenType()) {
                case GT, LT, GE, LE, EQ, NEQ -> {
                    Token op = next();
                    start = peek().getStartPos();
                    if (type != analyseCalculateExpression())
                        throw new AnalyzeError(ErrorCode.ConflictType, start);
                    switch (type) {
                        case INT -> addInstruction(new Instruction(instructions.size() - 1, Operation.CMP_I));
                        case DOUBLE -> addInstruction(new Instruction(instructions.size() - 1, Operation.CMP_F));
                    }
                    switch (op.getTokenType()) {
                        case GT -> addInstruction(new Instruction(instructions.size() - 1, Operation.SET_GT));
                        case LT -> addInstruction(new Instruction(instructions.size() - 1, Operation.SET_LT));
                        case EQ -> addInstruction(new Instruction(instructions.size() - 1, Operation.NOT));
                        case GE -> {
                            addInstruction(new Instruction(instructions.size() - 1, Operation.SET_LT));
                            addInstruction(new Instruction(instructions.size() - 1, Operation.NOT));
                        }
                        case LE -> {
                            addInstruction(new Instruction(instructions.size() - 1, Operation.SET_GT));
                            addInstruction(new Instruction(instructions.size() - 1, Operation.NOT));
                        }
                    }
                }
                default -> {
                    return type;
                }
            }
        }
    }

    /**
     * <calculate expression> ::= <term> (<add operator><term>)
     * @return
     * @throws CompileError
     */
    private TokenType analyseCalculateExpression() throws CompileError {
        Pos start = peek().getStartPos();

        TokenType type = analyseTerm();
        while (true) {
            switch (peek().getTokenType()) {
                case PLUS, MINUS -> {
                    Token op = next();
                    start = peek().getStartPos();
                    if (type != analyseTerm())
                        throw new AnalyzeError(ErrorCode.ConflictType, start);
                    switch (op.getTokenType()) {
                        case PLUS -> addInstruction(new Instruction(instructions.size() - 1, type == TokenType.INT ? Operation.ADD_I : Operation.ADD_F));
                        case MINUS -> addInstruction(new Instruction(instructions.size() - 1, type == TokenType.INT ? Operation.SUB_I : Operation.SUB_F));
                    }
                }
                default -> {
                    return type;
                }
            }
        }
    }

    /**
     * <term> ::= <factor> (<multiply operator><factor>)
     * @return
     * @throws CompileError
     */
    private TokenType analyseTerm() throws CompileError {
        Pos start = peek().getStartPos();

        TokenType type = analyseFactor();
        while (true) {
            switch (peek().getTokenType()) {
                case MUL, DIV -> {
                    Token op = next();
                    start = peek().getStartPos();
                    if (type != analyseFactor())
                        throw new AnalyzeError(ErrorCode.ConflictType, start);
                    switch (op.getTokenType()) {
                        case MUL -> addInstruction(new Instruction(instructions.size() - 1, type == TokenType.INT ? Operation.MUL_I : Operation.MUL_F));
                        case DIV -> addInstruction(new Instruction(instructions.size() - 1, type == TokenType.INT ? Operation.DIV_I : Operation.DIV_F));
                    }
                }
                default -> {
                    return type;
                }
            }
        }
    }

    /**
     * <factor> ::= <MINUS>? (<group expression>|<call expression>|<IDENT>|<literal expression>|<assign expression>) (<AS> <TYPE>)?
     * @return
     * @throws CompileError
     */
    private TokenType analyseFactor() throws CompileError {
        Pos start = peek().getStartPos();

        boolean opposite = false;
        while (nextIf(TokenType.MINUS) != null)
            opposite = !opposite;
        TokenType type;
        switch (peek().getTokenType()) {
            case IDENT -> {
                Token ident = expect(TokenType.IDENT);
                SymbolEntry symbol = getSymbol(ident);
                switch (peek().getTokenType()) {
                    //analyseAssignExpression
                    case ASSIGN -> {
                        if (symbol.isConstant || symbol.isFunction)
                            throw new AnalyzeError(ErrorCode.AssignToConstant, ident.getStartPos());
                        if (symbol.isFunctionParam)
                            addInstruction(new Instruction(instructions.size() - 1, Operation.ARG_A, symbol.stackOffset));
                        else if (symbol.level == 0) {
                            addInstruction(new Instruction(instructions.size() - 1, Operation.GLOB_A, symbol.stackOffset));
                        } else {
                            addInstruction(new Instruction(instructions.size() - 1, Operation.LOC_A, symbol.stackOffset));
                        }
                        expect(TokenType.ASSIGN);
                        //todo: 字符串赋值
                        if (symbol.type.getTokenType() != analyseExpression())
                            throw new AnalyzeError(ErrorCode.ConflictType, ident.getStartPos());
                        addInstruction(new Instruction(instructions.size() - 1, Operation.STORE_64));
                        type = TokenType.VOID;
                    }
                    //analyseCallExpression
                    case L_PAREN -> {
                        if (!symbol.isFunction)
                            throw new AnalyzeError(ErrorCode.NotAFunction, start);
                        if (symbol.type.getTokenType() != TokenType.VOID)
                            addInstruction(new Instruction(instructions.size() - 1, Operation.STACK_ALLOC, 1));
                        expect(TokenType.L_PAREN);
                        analyseCallParamList(symbol);
                        expect(TokenType.R_PAREN);
                        switch (symbol.ident.getValueString()) {
                            case "putchar", "putint", "putdouble", "putln", "putstr" ,"getchar", "getint", "getdouble"
                                    -> addInstruction(new Instruction(instructions.size() - 1, Operation.CALL_NAME, symbol.stackOffset, symbol.paramList.size()));
                            default -> addInstruction(new Instruction(instructions.size() - 1, Operation.CALL, symbol.stackOffset, symbol.paramList.size()));
                        }
                        type = symbol.type.getTokenType();
                    }
                    //analyseIdentExpression
                    default -> {
                        type = symbol.type.getTokenType();
                        if (symbol.isFunctionParam)
                            addInstruction(new Instruction(instructions.size() - 1, Operation.ARG_A, symbol.stackOffset));
                        else if (symbol.level == 0) {
                            addInstruction(new Instruction(instructions.size() - 1, Operation.GLOB_A, symbol.stackOffset));
                        } else {
                            addInstruction(new Instruction(instructions.size() - 1, Operation.LOC_A, symbol.stackOffset));
                        }
                        addInstruction(new Instruction(instructions.size() - 1, Operation.LOAD_64));
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
                addInstruction(new Instruction(instructions.size() - 1, Operation.PUSH, value));
            }
            //analyseLiteralExpression
            case STRING_LITERAL -> {
                Token strIdent = next();
                addInstruction(new Instruction(instructions.size() - 1, Operation.PUSH, addStringSymbol(strIdent, strIdent.getValueString())));
                type = TokenType.STRING_LITERAL;//todo: 究竟是什么类型？
            }
            //analyseGroupExpression
            case L_PAREN -> {
                expect(TokenType.L_PAREN);
                type = analyseExpression();
                expect(TokenType.R_PAREN);
            }
            default -> throw new AnalyzeError(ErrorCode.InvalidExpression, start);
        }
        if (opposite) {
            start = peek().getStartPos();
            if (type == TokenType.DOUBLE)
                addInstruction(new Instruction(instructions.size() - 1, Operation.NEG_F));
            else if (type == TokenType.INT)
                addInstruction(new Instruction(instructions.size() - 1, Operation.NEG_I));
            else throw new AnalyzeError(ErrorCode.InvalidNegative, start);
        }

        TokenType asType;
        while (nextIf(TokenType.AS_KW) != null) {
            start = peek().getStartPos();
            if (type != TokenType.INT && type != TokenType.DOUBLE)
                throw new AnalyzeError(ErrorCode.ConflictType, start);
            if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
                throw new AnalyzeError(ErrorCode.ConflictType, start);
            asType = next().getTokenType();
            if (asType != type) {
                addInstruction(new Instruction(instructions.size() - 1, asType == TokenType.INT ? Operation.FTOI : Operation.ITOF));
                type = asType;
            }
        }

        return type;
    }

    /**
     * <call param list> ::= <expression> (<COMMA> <expression>)*
     * @param fnSymbol
     * @throws CompileError
     */
    private void analyseCallParamList(SymbolEntry fnSymbol) throws CompileError {
        Pos start = peek().getStartPos();

        if (check(TokenType.R_PAREN))
            return;
        int curParam = 0;
        if (analyseExpression() == fnSymbol.paramList.get(curParam))
            curParam++;
        else
            throw new AnalyzeError(ErrorCode.InvalidFunctionParam, start);
        while (nextIf(TokenType.COMMA) != null) {
            start = peek().getStartPos();
            if (analyseExpression() == fnSymbol.paramList.get(curParam))
                curParam++;
            else
                throw new AnalyzeError(ErrorCode.InvalidFunctionParam, start);
        }
        start = peek().getStartPos();
        if (curParam != fnSymbol.paramList.size())
            throw new AnalyzeError(ErrorCode.InvalidFunctionParam, start);
    }

    /**
     * <function> ::= <FN_KW> <IDENT> <LPAREN> <function param list> <RPAREN> <ARROW> <TYPE> <block statement>
     * @throws CompileError
     */
    private void analyseFunction() throws CompileError {
        Pos start = peek().getStartPos();

        expect(TokenType.FN_KW);
        Token ident = expect(TokenType.IDENT);
        SymbolEntry fnSymbol = addFunctionSymbol(ident, null);
        expect(TokenType.L_PAREN);
        analyseParamList(fnSymbol, 1);
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE) && !check(TokenType.VOID))
            throw new AnalyzeError(ErrorCode.InvalidFunctionReturnType, start);
        Token retType = next();
        fnSymbol.setType(retType);
        if (retType.getTokenType() != TokenType.VOID)
            for (SymbolEntry param : symbolStack)
                param.stackOffset++;
        start = peek().getStartPos();
        boolean ret = analyseBlockStatement(fnSymbol, 1);
        if (!ret)
            if (retType.getTokenType() == TokenType.VOID)
                addInstruction(new Instruction(instructions.size() - 1, Operation.RET));
            else throw new AnalyzeError(ErrorCode.NoReturn, start);
    }

    /**
     * <function param list> ::= <function param> (<COMMA> <function param>)*
     * @param fnSymbol
     * @param level
     * @throws CompileError
     */
    private void analyseParamList(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        if (check(TokenType.R_PAREN))
            return;
        analyseParam(fnSymbol, level);
        while (nextIf(TokenType.COMMA) != null)
            analyseParam(fnSymbol, level);
    }

    /**
     * <function param> ::= <CONST>? <IDENT> <COLON> <TYPE>
     * @param fnSymbol
     * @param level
     * @throws CompileError
     */
    private void analyseParam(SymbolEntry fnSymbol, int level) throws CompileError {
        Pos start = peek().getStartPos();

        //check if the param is a constant
        boolean isConst = false;
        if (nextIf(TokenType.CONST_KW) != null)
            isConst = true;
        Token ident = expect(TokenType.IDENT);
        expect(TokenType.COLON);
        if (!check(TokenType.INT) && !check(TokenType.DOUBLE))
            throw new AnalyzeError(ErrorCode.InvalidParamType, start);
        Token type = next();
        fnSymbol.paramList.add(type.getTokenType());
        addVariableSymbol(ident, type, level,true, isConst, true);
    }
}
