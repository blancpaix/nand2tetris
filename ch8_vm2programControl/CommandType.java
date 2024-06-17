package vm2programControl;

public enum CommandType {
	C_ARITHMETIC,		// 연산 지시
	C_PUSH,
	C_POP,
	
	C_LABEL,
	C_GOTO,
	C_IF,
	C_FUNCTION,
	C_RETURN,
	C_CALL
};
