package compiler_code;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class VMWriter {
	private BufferedWriter bw;

	// 새 파일을 생성하고 쓰기를 준비	
	public VMWriter(String outputFile) throws IOException {
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
	};
	
	// Segment 별 push 구문 작성
	public void writePush(Segment segment, int idx) throws IOException {
		// push SEGMENT INDEX
		String seg = "";
		switch (segment) {
			case CONST :
				seg = "constant";
				break;
			case LOCAL:
				seg = "local";
				break;
			case ARG:
				seg = "argument";
				break;
			case THIS:
				seg = "this";
				break;
			case THAT:
				seg = "that";
				break;
			case POINTER:
				seg = "pointer";
				break;
			case TEMP:
				seg = "temp";
				break;
			default :
				break;
		};
		bw.write("push " + seg + " " + idx + "\n");
		bw.flush();
	};
	
	// Segment 별 pop 구문 작성
	public void writePop(Segment segment, int idx) throws IOException {
		// local, argument, this, that, pointer, temp, static
		// pop SEGMENT INDEX
		String seg = "";
		switch (segment) {
			case CONST :
				seg = "constant";
				break;
			case LOCAL:
				seg = "local";
				break;
			case ARG:
				seg = "argument";
				break;
			case THIS:
				seg = "this";
				break;
			case THAT:
				seg = "that";
				break;
			case POINTER:
				seg = "pointer";
				break;
			case TEMP:
				seg = "temp";
				break;
			default :
				break;
		};
		bw.write("pop " + seg + " " + idx + "\n");
		bw.flush();
	};
	
	/**
	 * @param cmd : add | sub | and | or | not | neg | eq | gt | lt
	 */
	// 기초 연산자 처리를 위한 구문 작성
	public void WriteArithmetic(String cmd) throws IOException {
		switch (cmd) {
		case "add":
			bw.write("add\n");
			break;
		case "sub":
			bw.write("sub\n");
			break;
		case "and":
			bw.write("and\n");
			break;
		case "or":
			bw.write("or\n");
			break;
		case "not":
			bw.write("not\n");
			break;
		case "neg":
			bw.write("neg\n");
			break;
		case "eq":
			bw.write("eq\n");
			break;
		case "gt":
			bw.write("gt\n");
			break;
		case "lt":
			bw.write("lt\n");
			break;
		default :
			break;
		};
		
		bw.flush();
	};
	
	// 레이블 작성
	public void WriteLabel(String label) throws IOException {
		bw.write("label " + label + "\n");
		bw.flush();
	};
	
	// goto 구문 작성
	public void WriteGoto(String label) throws IOException {
		bw.write("goto " + label + "\n");
    	bw.flush();
	};
	
	// if 구문 작성
	public void WriteIf(String label) throws IOException  {
		// 스택의 현재값이 0이 아닌 경우 해당 레이블로 점프
		bw.write("if-goto " + label + "\n");
		
		bw.flush();
	};
	
	// p.181 참조
	// 함수 호출 구문 작성
    public void writeCall(String functionName, int numArgs) throws IOException {
    	bw.write("call " + functionName + " " + numArgs + "\n") ;
        
        bw.flush();
    }
    
    // 함수 return 구문 작성
    public void writeReturn() throws IOException {
    	// 반환값은 스택 최상단에 위치하고 있어 *ARG = pop()을 통해 반환값 위치 재설정됨
    	bw.write("return\n");
    	// 반환될 값이 스택의 가장 위에 있어야 하기 때문에
    	
    	bw.flush();
    }

    // 함수 정의 구문 작성
    public void writeFunction(String functionName, int numLocals) throws IOException {
    	bw.write("call " + functionName + " " + numLocals + "\n");

    	bw.flush();
    }
	
    // vm 코드 작성 후 bufferedWriter 종료
	public void close() throws IOException {
		bw.close();
		
		System.out.println("== Done create .vm code ==");
	}
	
	
}
