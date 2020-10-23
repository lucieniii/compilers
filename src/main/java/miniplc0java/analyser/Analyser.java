package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
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
     * 获取下一个变量的栈偏移
     *
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 添加一个符号
     *
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        if (this.symbolTable.get(name) != null) {
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            this.symbolTable.put(name, new SymbolEntry(isConstant, isInitialized, getNextVariableOffset()));
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    /**
     * <程序> ::= 'begin'<主过程>'end'
     */
    private void analyseProgram() throws CompileError {
        // 示例函数，示例如何调用子程序
        // 'begin'
        expect(TokenType.Begin);

        analyseMain();

        // 'end'
        expect(TokenType.End);
        expect(TokenType.EOF);
    }

    private void analyseMain() throws CompileError {
        analyseConstantDeclaration();
        analyseVariableDeclaration();
        analyseStatementSequence();
    }

    private void analyseConstantDeclaration() throws CompileError {
        // 示例函数，示例如何解析常量声明
        // 如果下一个 token 是 const 就继续
        while (nextIf(TokenType.Const) != null) {
            // 变量名
            var nameToken = expect(TokenType.Ident);

            // 等于号
            expect(TokenType.Equal);

            // 常表达式
            analyseConstantExpression();
            addSymbol(nameToken.getValueString(), true, true, nameToken.getStartPos());
            // 分号
            expect(TokenType.Semicolon);
        }
    }

    private void analyseVariableDeclaration() throws CompileError {
        while (nextIf(TokenType.Var) != null) {
            var nameToken = expect(TokenType.Ident);

            if (nextIf(TokenType.Equal) != null) {
                analyseExpression();
                addSymbol(nameToken.getValueString(), true, false, nameToken.getStartPos());
            } else {
                addSymbol(nameToken.getValueString(), false, false, nameToken.getStartPos());
            }

            expect(TokenType.Semicolon);
        }
    }

    private void analyseStatementSequence() throws CompileError {
        while (check(TokenType.Ident) || check(TokenType.Print) || check(TokenType.Semicolon))
            analyseStatement();
    }

    private void analyseStatement() throws CompileError {
        if (check(TokenType.Ident))
            analyseAssignmentStatement();
        else if (check((TokenType.Print)))
            analyseOutputStatement();
        else
            next();
    }

    private void analyseConstantExpression() throws CompileError {
        boolean negate = false;
        var t1 = next();
        if (t1.getTokenType().equals(TokenType.Plus)) {
            t1 = next();
        }
        else if (t1.getTokenType().equals(TokenType.Minus)) {
            negate = true;
            instructions.add(new Instruction(Operation.LIT, 0));
            t1 = next();
        }
        if (t1.getTokenType().equals(TokenType.Uint)) {
            instructions.add(new Instruction(Operation.LIT, (Integer) t1.getValue()));
            if (negate)
                instructions.add(new Instruction(Operation.SUB));
        } else throw new AnalyzeError(ErrorCode.ConstantNeedValue, t1.getStartPos());
    }

    private void analyseExpression() throws CompileError {
        analyseItem();
        while (check(TokenType.Plus) || check(TokenType.Minus)) {
            var t = next();
            analyseItem();
            if (t.getTokenType().equals(TokenType.Plus))
                instructions.add(new Instruction(Operation.ADD));
            else
                instructions.add(new Instruction(Operation.SUB));
        }
    }

    private void analyseAssignmentStatement() throws CompileError {
        var t1 = expect(TokenType.Ident);
        expect(TokenType.Equal);
        analyseExpression();
        instructions.add(new Instruction(Operation.STO, symbolTable.get(t1.getValueString()).stackOffset));
    }

    private void analyseOutputStatement() throws CompileError {
        expect(TokenType.Print);
        expect(TokenType.LParen);
        analyseExpression();
        expect(TokenType.RParen);
        expect(TokenType.Semicolon);
        instructions.add(new Instruction(Operation.WRT));
    }

    private void analyseItem() throws CompileError {
        analyseFactor();
        while (check(TokenType.Mult) || check(TokenType.Div)) {
            var t = next();
            analyseFactor();
            if (t.getTokenType().equals(TokenType.Mult))
                instructions.add(new Instruction(Operation.MUL));
            else
                instructions.add(new Instruction(Operation.DIV));
        }
    }

    private void analyseFactor() throws CompileError {
        boolean negate;
        if (nextIf(TokenType.Minus) != null) {
            negate = true;
            // 计算结果需要被 0 减
            instructions.add(new Instruction(Operation.LIT, 0));
        } else {
            nextIf(TokenType.Plus);
            negate = false;
        }

        if (check(TokenType.Ident)) {
            var t1 = next();
            instructions.add(new Instruction(Operation.LOD, symbolTable.get(t1.getValueString()).stackOffset));
        } else if (check(TokenType.Uint)) {
            var t1 = next();
            instructions.add(new Instruction(Operation.LIT, (Integer) t1.getValue()));
        } else if (check(TokenType.LParen)) {
            expect(TokenType.LParen);
            analyseExpression();
            expect(TokenType.RParen);
        } else {
            // 都不是，摸了
            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
        }

        if (negate) {
            instructions.add(new Instruction(Operation.SUB));
        }
    }
}
