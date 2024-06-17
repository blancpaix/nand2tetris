package assembler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Parser {
	private BufferedReader reader;
	private String currentLine;
	private String currentCmd;

    public Parser(String file) throws FileNotFoundException {
    	if (file == null) {
    		throw new NullPointerException("No such file!");
    	};
        reader = new BufferedReader(new FileReader(file));
        currentLine = null;
        currentCmd = null;
    }

    // 다음 명령이 있는지 확인
    public boolean hasMoreCommands() throws IOException {
        // 구현
    	String input = reader.readLine();
    	if (input != null) {
    		return true;
    	}
    	
    	currentLine = input.trim();
    	return false;
    }

    // 다음 명령으로 진행 - hasMoreCommands 호출 후 실행
    public void advance() {
    	currentCmd = currentLine;
    }

    // 현재 명령의 유형 반환
    /**
     * @는 a 명령어
     * ()는 L 명령어
     * 나머지 C
     */
    public CommandType commandType() {
    	if (currentCmd.startsWith("@")) {
    		return CommandType.A_COMMAND;
    	} else if (currentCmd.startsWith(")") && currentCmd.endsWith(")")) {
    		return CommandType.L_COMMAND;
    	}
    	
    	return CommandType.A_COMMAND;
    }

    // 현재 명령의 심볼 또는 명령 반환
    // A or L 타입 명령에서만 호출
    public String symbol() {
    	// CommandType currentCmd = commandType();
    	// if (currentCmd != CommandType.C_COMMAND) return;
    	// A // L 인 상황 
    	if (commandType() == CommandType.C_COMMAND) {
    		return currentCmd;
    	};
    	
    	return currentCmd.substring(1);
    };
    
    // Parser 에서는 단순히 호출만 하는건데 나는 다 처리를 해버렷네??..
    // C-com 에서만 호출
    // dest 연상 기호(8개)를 반환, 미리 확인을 하고 호출을 하는건가 아니면 호출에서 걸러주는건가.. 그걸 잘 모르겠다.. 시발거
    public String dest() {
    	 // 들어온대로 처리를 하긴 하는데... 이거 결정을 어떻게 할건데??
    	if (currentCmd.contains("=")) {
        	String[] dField = currentCmd.split("=");
        	switch(dField[0]) {
    	    	case "M" :
    	    		return "001";
    	    	case "D":
    	    		return "010";
    	    	case "MD":
    	    		return "011";
    	    	case "A":
    	    		return "100";
    	    	case "AM":
    	    		return "101";
    	    	case "AD":
    	    		return "110";
    	    	case "AMD" :
    	    		return "111";
    			default : 
    				return "000";
        	}
    	}
    	// D 필드가 없느경우는 그냥 000 이지
    	return "000";
    };
    
    // C-com 에서만 호출
    // 무조건 있는 필드
    public String comp() {
    	String cmd = null;
    	if (currentCmd.contains("=")) {
    		String[] cField = currentCmd.split("=");
    		cmd = cField[1];
    	} else {
    		String[] cField = currentCmd.split(";");
    		cmd = cField[0];
    	}
    		
    	switch(cmd) {
			case "0":
	            return "0101010";
	        case "1":
	            return "0111111";
	        case "-1":
	            return "0111010";
	        case "D":
	            return "0001100";
	        case "A":
	            return "0110000";
	        case "!D":
	            return "0001101";
	        case "!A":
	            return "0110001";
	        case "-D":
	            return "0001111";
	        case "-A":
	            return "0110011";
	        case "D+1":
	            return "0011111";
	        case "A+1":
	            return "0110111";
	        case "D-1":
	            return "0001110";
	        case "A-1":
	            return "0110010";
	        case "D+A":
	            return "0000010";
	        case "D-A":
	            return "0010011";
	        case "A-D":
	            return "0000111";
	        case "D&A":
	            return "0000000";
	        case "D|A":
	            return "0010101";
	        case "M":
	            return "1110000";
	        case "!M":
	            return "1110001";
	        case "-M":
	            return "1110011";
	        case "M+1":
	            return "1110111";
	        case "M-1":
	            return "1110010";
	        case "D+M":
	            return "1000010";
	        case "D-M":
	            return "1010011";
	        case "M-D":
	            return "1000111";
	        case "D&M":
	            return "1000000";
	        case "D|M":
	            return "1010101";
	        default:
	            return "0000000";
		}
    };
    
    public String jump() {
    	if (currentCmd.contains(";")) {
    		String[] jField = currentCmd.split(";");
    		switch(jField[1]) {
	    		case "JGT" : 
	    			return "001";
	    		case "JEQ" : 
	    			return "010";
	    		case "JGE" : 
	    			return "011";
	    		case "JLT" : 
	    			return "100";
	            case "JNE":
	                return "101";
	            case "JLE":
	                return "110";
	            case "JMP":
	                return "111";
	           default : 
	        	   return "000";
    		}
    	} else {
    		return "000";
    	}
    }

}