package vm2programControl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import vm1stack.CommandType;

public class CodeWriter {
	private BufferedWriter bw;
	private int jumpLine;
	
	public CodeWriter(String outputFile) throws IOException {
		bw = new BufferedWriter(new FileWriter(outputFile));
		jumpLine = 0;
	};
	
	public void writerInit() throws IOException {
		// VM초기화 수행하는 어셈블리 코드인 부트스트랩 코드를 출력
		// 출력 파일 맨 앞에 있어야 함
		bw.write("@256\nD=A\n");
		bw.write("@SP\nM=D\n");
		
		bw.flush();
		
		writeCall("SYs.init", 0);		
		return;
	}
	
	public void writeArithmetic(String command) throws IOException  {
		bw.write("// " + command + "\n");
		
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
		};
		
		bw.flush();
	};
	
	
	public void writePushPop(vm2programControl.CommandType cmdType, String segment, int idx) throws IOException {
		if (cmdType.equals(CommandType.C_PUSH)) {
			if (segment.equals("pointer") || segment.equals("temp")) {
				if (segment.equals("pointer")) {
					// pointer argu : 0, 1 허용, this, that 만 가리킨대..
					bw.write("@" + 3 + idx + "\nD=M\n");
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
		} else if (cmdType.equals(CommandType.C_POP)) {
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
	
    public void writeLabel(String label) throws IOException {
    	bw.write("(" + label + ")\n");
    	bw.flush();
    }

    public void writeGoto(String label) throws IOException {
    	bw.write("@" + label + "\n0;JMP\n");
    	bw.flush();
    }

    public void writeIf(String label) throws IOException {
    	bw.write("@SP\nAM=M-1\nD=M\n");
    	bw.write("@" + label + "\n;D;JNE");		// 0이 아닌경우 해당주소로 점프
    	bw.flush();
    }
    // p.181 참조
    public void writeCall(String functionName, int numArgs) throws IOException {
        bw.write("@RETURN-LABEL\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        bw.write("@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        bw.write("@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        bw.write("@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        bw.write("@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        bw.write("@SP\nD=M\n@5\nD=D-A\n");
        bw.write("@" + numArgs + "\nD=D-A\n@ARG\nM=D\n");
        bw.write("@SP\nD=M\n@LCL\nM=D\n");
        bw.write("@" + functionName + "\n0;JMP;\n");
        bw.write("(RETURN-LABEL)\n");
        
        bw.flush();
    }

    public void writeReturn() throws IOException {
    	// 반환값은 스택 최상단에 위치하고 있어 *ARG = pop()을 통해 반환값 위치 재설정됨
    	bw.write("@LCL\nD=M\n@FRAME\nM=D\n");
    	bw.write("@5\nA=D-A\nD=M\n@RET\nM=D\n");
    	bw.write("@SP\nAM=M-1\nD=M\n@ARG\nA=M\nM=D\n");
    	bw.write("@ARG\nD=M+1\n@SP\nM=D\n");
    	
    	bw.write("@FRAME\nAM=M-1\nD=M\n@THAT\nM=D\n");
    	bw.write("@FRAME\nAM=M-1\nD=M\n@THIS\nM=D\n");
    	bw.write("@FRAME\nAM=M-1\nD=M\n@ARG\nM=D\n");
    	bw.write("@FRAME\nAM=M-1\nD=M\n@LCL\nM=D\n");
    	
    	bw.write("@RET\nA=M\n0;JMP\n");
    	
    	bw.flush();
    }

    public void writeFunction(String functionName, int numLocals) throws IOException {
    	bw.write("@" + functionName + "\n0;JMP\n");
    	bw.write("@START\nD:JLE\n");
    	bw.write("(LOOP)\n@SP\nA=M\nM=0\n@SP\nM=M+1\n");
    	bw.write("@" + numLocals + "\nMD=M-1\n");
    	bw.write("@LOOP\nD;JGT\n(START)");

    	bw.flush();
    }

    public void close() throws IOException {
        this.bw.close();
    }
}
