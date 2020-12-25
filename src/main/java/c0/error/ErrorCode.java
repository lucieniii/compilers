package src.main.java.c0.error;

public enum ErrorCode {
    NoError, // Should be only used internally.
    StreamError, EOF, InvalidInput, InvalidIdentifier, IntegerInvalidOrOverflow, // int32_t overflow.
    NoBegin, NoEnd, NeedIdentifier, ConstantNeedValue, NoSemicolon, InvalidVariableDeclaration, IncompleteExpression,
    NotDeclared, AssignToConstant, DuplicateDeclaration, NotInitialized, InvalidAssignment, InvalidPrint, ExpectedToken,
    InvalidEscapeSeq, EmptyChar, UnknownToken, IncompleteStringOrChar, InvalidChar, DoubleInvalidOrOverflow,
    InvalidGlobalDeclaration, InvalidFunctionReturnType, InvalidDeclaration, InvalidParamType, InvalidVariableType,
    InvalidExpression, InvalidType, ConflictType, ConflictFunctionReturnType, InvalidNegative, InvalidFunctionParam,
    NotAFunction, MissingBlockOrIfAfterElse, NotInWhile, NoReturn, NoMainFunction
}
