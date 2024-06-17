package vm1stack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
	private BufferedReader br;
	private String curLine;
	private String curCmd;
	
	
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
        } else {
            return CommandType.C_ARITHMETIC;
        }
	};
	
	public String arg1() {
		if (commandType() == CommandType.C_ARITHMETIC) {
			return curCmd;
		} else {
			return curCmd.split(" ")[1];
		}
	};
	 
	// C_PUSH, POP, FUNCTION, CALL 만 호출 가능
	public int args2() {
		if (commandType() == CommandType.C_PUSH 
				|| commandType() == CommandType.C_POP 
				|| commandType() == CommandType.C_FUNCTION 
				|| commandType() == CommandType.C_CALL) {
			return Integer.parseInt((curCmd).split(" ")[2]);
		};
		
		return -1;
	};
	
	
	
}
