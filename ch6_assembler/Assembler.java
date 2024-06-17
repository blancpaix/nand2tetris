package assembler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Assembler {
	private Parser parser;
	private SymbolTable symbolTable;
	
	public Assembler(String inputFile, String outputFile) throws IOException {
		parser = new Parser(inputFile);
		symbolTable = new SymbolTable();
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
			while (parser.hasMoreCommands()) {
				parser.advance();
				String binLine = "";
				
				if (parser.commandType() == CommandType.A_COMMAND) {
					String symbol = parser.symbol();
					int addr; 
					
					
					// 숫자 regex
					if (symbol.matches("\\d+")) {		// 숫자로만 이뤄진 A 커맨드
						addr = Integer.parseInt(symbol);
					} else {
						if (!symbolTable.contains(symbol)) {
							symbolTable.addEntry(symbol, symbolTable.nextVariableAddr());
						}
						
						addr = symbolTable.GetAddress(symbol);
					}
					binLine = String.format("%16s", Integer.toBinaryString(addr)).replace(" ", "0");
				} else if (parser.commandType() == CommandType.C_COMMAND) {
					String comp = parser.comp();
					String dest = parser.dest();
					String jump = parser.jump();
					
					binLine = "111" + comp + dest + jump;
				}
				
				writer.write(binLine);
				writer.newLine();
				
			}		
		}
	};
	
    public static void main(String[] args) throws IOException {
        new Assembler(args[0], args[1]);
    }
	 
}
