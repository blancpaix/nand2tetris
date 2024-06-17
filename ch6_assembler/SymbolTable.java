package assembler;

import java.util.HashMap;

public class SymbolTable {
	private HashMap<String, Integer> table;
	private int varAddr;
	
	public SymbolTable() {
		table = new HashMap<String, Integer>();
		table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);
        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
        
        varAddr = 16;
	};
	
	public void addEntry(String symbol, int address) {
		table.put(symbol, address);
	};
	
	public boolean contains(String symbol) {
		return table.containsKey(symbol);
	};
	
	public int GetAddress(String symbol) {
		
		return table.get(symbol);
	};
	
	public int nextVariableAddr() {
        return varAddr++;
    }
}
