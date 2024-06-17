package compiler_code;
import java.util.Hashtable;

enum Kind {
	STATIC, FIELD, ARG, VAR, NONE
};

class SymbolInfo {
	private String type;	// int | char | boolean | void...
	private Kind kind;		// STATIC | FIELD | ARG | VAR | NONE	- Segment와는 도 다름
	private int idx;

	SymbolInfo(String type, Kind kind, int idx) {
		this.type = type;
		this.kind = kind;
		this.idx = idx;
	};
	
	public String getType() { return this.type; };
	public Kind getKind() { return this.kind; };
	public int getIdx() { return this.idx; };
};

public class SymbolTable {
	private Hashtable<String, SymbolInfo> classTable;
	private int classTableIdx;
	private Hashtable<String, SymbolInfo> subRoutineTable;
	private int subRoutineTableIdx;
	private int staticIdx;
	private int fieldIdx;
	private int argIdx;
	private int varIdx;
	private String className;
	
	public SymbolTable() {
		// scope로 구분 된 class, subroutine Table 구성 
		classTable = new Hashtable<String, SymbolInfo>();
		classTableIdx = 0;
		staticIdx = 0;
		fieldIdx = 0;
		argIdx = 0;
		varIdx = 0;
		subRoutineTable = new Hashtable<String, SymbolInfo>();
		className = null;
	};
	
	public void setClassName(String className) {
		this.className = className;
	};
	
	public String getClassName() {
		return this.className;
	}
	
	// 새로운 함수 호출 시 subroutineTable 초기화
	public void startSubroutine () {
		this.subRoutineTable.clear();
		this.subRoutineTableIdx = 0;
		this.argIdx = 0;
		this.varIdx = 0;
		
	};
	
	public Kind kindParser(String kind) {
		if (kind.equals("static")) {
			return Kind.STATIC;
		} else if (kind.equals("field")) {
			return Kind.FIELD;
		} else if (kind.equals("argument")) {
			return Kind.ARG;
		} else if (kind.equals("var")) {
			return Kind.VAR;
		};
		
		return Kind.NONE;
	}
	
	// 변수 및 식별자 저장
	public int Define(String name, String type, Kind kind) {
		if (kind.equals(Kind.STATIC) || kind.equals(Kind.FIELD)) {
			// class
			classTable.put(name, new SymbolInfo(type, kind, classTableIdx++));
			if (kind.equals(Kind.STATIC)) {
				return staticIdx++;
			} else {
				return fieldIdx++;
			}
		} else {
			// subroutineScope
			subRoutineTable.put(name, new SymbolInfo(type, kind, subRoutineTableIdx++));
			if (kind.equals(Kind.ARG)) { 
				return argIdx++;
			} else {
				return varIdx++;
			}
		}
	};
	
	// 특정 종류의 생성된 식별자 수 반환
	public int VarCount(Kind kind) {
		if (kind.equals(Kind.STATIC)) {
			return this.staticIdx;
		} else if (kind.equals(Kind.FIELD)){
			return this.fieldIdx;
		} else if (kind.equals(Kind.ARG)) {
			return this.argIdx;
		} else {	// VAR
			return this.varIdx;
		}
	};
	
	// Kind : STATIC, FIELD, ARG, VAR 반환
	public Kind KindOf(String name) {
		if (subRoutineTable.containsKey(name)) {
			return subRoutineTable.get(name).getKind();
		} else if (classTable.containsKey(name)) {
			return classTable.get(name).getKind();
		}

		return Kind.NONE;
	};
	
	// tpye : int | char | boolean | void... 반환
	public String TypeOf(String name) {
		if (subRoutineTable.containsKey(name)) {
			return subRoutineTable.get(name).getType();
		} else if (classTable.containsKey(name)) {
			return classTable.get(name).getType();	
		};
		
		return null;
	};
	
	// 해당 값에 지정된 Segment index 반환
	public int IndexOf(String name) {
		if (subRoutineTable.containsKey(name)) {
			return subRoutineTable.get(name).getIdx();
		} else if (classTable.containsKey(name)) {
			return classTable.get(name).getIdx();
		};
		
		return -1;
	};
	
	// 변수 및 식별자 존재 확인
	public boolean isContainKey(String name) {
		if (subRoutineTable.containsKey(name)) {
			return true;
		} else if (classTable.containsKey(name)) {
			return true;
		};
		
		return false;
	}
};


