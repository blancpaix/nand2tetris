package vm2programControl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
	BufferedReader br;
	String curLine;
	String curCmd;
	
	
	// 입력 파일을 열고 분석할 준비
	public Parser(String inputFile) throws FileNotFoundException{
		if (inputFile == null || inputFile == "") {
			throw new NullPointerException(" NO! ");
		};
		br = new BufferedReader(new FileReader(inputFile));
		curLine = null;
		curCmd = null;
	};
	
	public boolean hasMoreCommands () throws IOException {
		curLine = br.readLine();
		if (curLine == "" || curLine == null) {
			curLine = null;
			curCmd = null;
			return false;
		};
		
		curCmd = curLine; // 원래는 이게 advance 에서 해야 하는건데
		return true;
	};
	
	public void advance () {
		curCmd = curCmd.replaceAll("\\s", "") ;		// 공백 제거
		
		if (curCmd.contains("\\")) {
			curCmd = curCmd.substring(0, curCmd.indexOf("//")).trim();
		}
		
	};
	
	// vm 명령의 타입을 반환함
	public CommandType commandType () {
        if (curCmd.startsWith("push")) {
            return CommandType.C_PUSH;
        } else if (curCmd.startsWith("pop")) {
            return CommandType.C_POP;
        } else if (curCmd.startsWith("(") && curCmd.endsWith(")")) {
        	return CommandType.C_LABEL;
        } else if (curCmd.startsWith("goto")) {
        	return CommandType.C_GOTO;
        } else if (curCmd.startsWith("if-goto")) {
        	return CommandType.C_IF;
        } else if (curCmd.startsWith("function")) {
        	return CommandType.C_FUNCTION;
        } else if (curCmd.equals("return")) {
        	return CommandType.C_RETURN;
        } else if (curCmd.startsWith("call")) {
        	return CommandType.C_CALL;
        } else {
            return CommandType.C_ARITHMETIC;
        }
	};
	
	public String arg1() {
		if (commandType() == CommandType.C_ARITHMETIC) {
			return curCmd;
		} else if (commandType() == CommandType.C_LABEL) {
			return curCmd.substring(1, curCmd.length() - 1);
		} else if (commandType() == CommandType.C_GOTO) {
			return curCmd.split(" ")[1];
		} else if (commandType() == CommandType.C_IF) {
			return curCmd.split(" ")[1];
		} else if (commandType() == CommandType.C_FUNCTION) {
			return curCmd.split(" ")[1];
		} else if (commandType() == CommandType.C_CALL) {
			return curCmd.split(" ")[1];
		} else {
			// ARITHMETIC
			return curCmd.split(" ")[1];
		}
	};
	 
	public int arg2() {
		if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP) {
            return Integer.parseInt(curCmd.split(" ")[2]);
		} else if (commandType() == CommandType.C_FUNCTION) {
        	return Integer.parseInt(curCmd.split(" ")[2]);
		} else if (commandType() == CommandType.C_CALL) {
			return Integer.parseInt(curCmd.split(" ")[2]);
		} else {
            return -1;
        }
	}
	
	
}
