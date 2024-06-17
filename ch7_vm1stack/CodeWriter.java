package vm1stack;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/*
[neg]
@SP
A=M-1 중복
M=-M

[not]
~
M=!M

[add]
@SP
AM=M-1
D=M
A=A-1 중복
M=M+D

[sub]
~
M=M-D

[and]
~
M=D&M

[or]
M=D|M

true 아니면 false 로 점프, (TRUE) 레이블을 따로 구현해야 함
[eq]
두 값을 빼서 0 확인
 */

public class CodeWriter {
	private BufferedWriter bw;
	private int jumpLine;
	
	public CodeWriter (String outputFilePath) throws IOException {
		bw = new BufferedWriter(new FileWriter(outputFilePath));
		jumpLine = 0;
	};
	
	public void writeArithmetic(String command) throws IOException  {
		if (command.equals("neg") || command.equals("not")) {
			 bw.write("@SP\nA=M-1\n");
			 
	            if (command.equals("neg")) {
	                bw.write("M=-M\n");
	            } else {
	                bw.write("M=!M\n");
	            }
		} else {
			bw.write("@SP\nAM=M-1\nD=M\nA=A-1\n");
			
            switch (command) {
                case "add":
                    bw.write("M=M+D\n");
                    break;
                case "sub":
                    bw.write("M=M-D\n");
                    break;
                case "and":
                    bw.write("M=M&D\n");
                    break;
                case "or":
                    bw.write("M=M|D\n");
                    break;
                case "eq":
                case "gt":
                case "lt":
                    bw.write("D=M-D\n@FALSE" + jumpLine + "\n");
                    if (command.equals("eq")) {
                        bw.write("D;JNE\n");
                    } else if (command.equals("gt")) {
                        bw.write("D;JLE\n");
                    } else {
                        bw.write("D;JGE\n");
                    }
                    bw.write("@SP\nA=M-1\nM=-1\n@CONTINUE" + jumpLine + "\n0;JMP\n");
                    bw.write("(FALSE" + jumpLine + ")\n@SP\nA=M-1\nM=0\n");
                    bw.write("(CONTINUE" + jumpLine + ")\n");
                    jumpLine++;
                    break;
            }
		}
	};
	
	// 주어진 cmd 를 번역한 어셈블리 코드로 기록
	public void writePushPop(CommandType cmdType, String segment, int idx) throws IOException {
		if (cmdType == CommandType.C_PUSH) {
			if (segment.equals("pointer") || segment.equals("temp")) {
				if (segment.equals("pointer")) {		// pointer 0, 1만 가능하고 서로 this, that 만 가리킨대..
					bw.write("@" + 3 + idx + "\nD=M\n");		// this나 that이라는데??..
				} else {
					bw.write("@" + 5 + idx + "\nD=M\n");
				};
				bw.write("@SP\nA=M\nM=D"
						+ "@SP\nM=M+1");
			} else {
				bw.write("@" + idx + "\nD=A\n");
				switch(segment) {
					case "local":
						bw.write("@LCL\nA=D+M\nD=M\n");
						break;
					case "argument":
						bw.write("@ARG\nA=D+M\nD=M\\n");
						break;
					case "this":
						bw.write("@THIS\nA=D+M\\nD=M\n");
						break;
					case "that":
						bw.write("@THAT\nA=D+M\nD=M\n");
						break;
					case "constant":
						bw.write("@SP\nA=M\nM=D");
						break;
					default :
						break;
				};
				bw.write("@SP\nA=M\nM=D\n@SP\nM=M+1");
			}
		} else if (cmdType == CommandType.C_POP) {
			// local, argument, this, that, pointer, temp, static
			if (segment.equals("constant")) {
				throw new IllegalArgumentException("Readonly, Constant");
			};
			
			if (segment.equals("pointer") || segment.equals("temp")) {
				bw.write("@SP\nAM=M-1\nD=M\n");
				if (segment.equals("pointer")) {
					bw.write("@" + 3 + idx + "\nM=D\n");
				} else {
					bw.write("@" + 5 + idx + "\nM=D\n");					
				}
			} else {
				bw.write("@" + idx + "\nD=A\n");
				switch(segment) {
					case "local":
						bw.write("@LCL\nA=M+D\n");
						break;
					case "argument":
						bw.write("@ARG\nA=M+D\n");
						break;
		            case "this":
		            	bw.write("@THIS\nA=M+D\n");
		            	break;
		            case "that":
		            	bw.write("@THAT\nA=M+D\n");
		            	break;
		            default : 
		            	break;
				};
				
				bw.write("@13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D");
			}		
		}
		
		bw.flush();
	};
	
	// 출력파일 닫기
	public void close() throws IOException {
		bw.close();
	}
}
