package vm1stack;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("input : Java Main <inputFile> <outputFile>");
			return;
		};
		
		String inputFile = args[0];
		String outputFile = args[1];
		
		try {
			Parser parser = new Parser(inputFile);
			CodeWriter codeWriter = new CodeWriter(outputFile);
			
			while(parser.hasMoreCommands()) {
				parser.advance();
				CommandType cmdType = parser.commandType();
				
				if (cmdType == CommandType.C_ARITHMETIC) {
					codeWriter.writeArithmetic(parser.arg1());
				} else if (cmdType == CommandType.C_PUSH || cmdType == CommandType.C_POP) {
                    codeWriter.writePushPop(cmdType, parser.arg1(), Integer.parseInt(parser.arg1()));
                }
			}
		} catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
	}
}
