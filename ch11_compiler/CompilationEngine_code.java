package compiler_code;

import java.io.FileNotFoundException;
import java.io.IOException;

// XML 파일 작성만을 위한 코드 제거
public class CompilationEngine_code {
	private JackTokenizer jt;
	private SymbolTable st;	// 식별자 판단을 위한 table
	private VMWriter vw;	// vm 코드 작성 객체
	
	private String className;	// 클래스 이름
	private String methodName;	// 서브루틴 이름
	private String letVarName;	// 현재 선언된 let 변수 이름
	private String statementType;	// 명령어의 타입
	private int countLocalVar;	// 지역 변수의 개수
	private int countFieldVar;	// field 변수의 개수
	private int countArgs;		// 매개 변수의 개수
	private String subroutineClassName;	// 서부루틴의 클래스 이름
	private boolean isMethod;	// 서브루틴 여부 확인
	private boolean isConstructor;	// 생성자 여부 확인
	
	// vm code 작성을 위한 input, output File 설정 및 초기화
	public CompilationEngine_code(String inputFile, String outputFilePath) throws IOException {
		try {			
            vw = new VMWriter(outputFilePath);
			jt = new JackTokenizer(inputFile);
			st = new SymbolTable();
			
			className = null;
			methodName = null;
			letVarName = null;
			statementType = null;
			countLocalVar = 0;
			countFieldVar = 0;
			countArgs = 0;
			subroutineClassName = null;
			isMethod = false;
			isConstructor = false;
			
			System.out.println("초기화");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	};	
	
	// 클래스 -> 키워드 클래스 -> 식별자 이름 -> 심볼 { }
	// 클래스 컴파일 후 .vm 파일 출력
	public void CompileClass (String ouputFilePath, String vmFilePath) {
		System.out.println("CompileClass 호출");
		try {
			if (jt.hasMoreTokens()) {
				// class keyword
				jt.advance();
				String classToken = jt.getToken();
				if (!"class".equals(classToken)) {
					throw new RuntimeException("\"Class\" keyword not found. -CompileClass");
				};
				
				// class identifier
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("\"Class Identifier\" not found. -CompileClass");
				}
				String identifierName = jt.identifier();
				writeIdentifier(identifierName, "class");	// 클래스
				className = identifierName;
				st.setClassName(identifierName);
				
				// { symbol
				jt.advance();
				if (!"{".equals(jt.getToken())) {
					throw new RuntimeException("\"{\" not found. -CompileClass");
				}
				
				// 재귀
				jt.advance();
				while (!"}".equals(jt.getToken())) {
					String curToken = jt.getToken();
					if ("static".equals(curToken) 
							|| "field".equals(curToken)) {
						// Kind를 굳이 지금 기입할 이유가 있나?? static 인지 field 인지 뭐 그런것 ??
						CompileClassVarDec();
						countFieldVar = countLocalVar;
						countLocalVar = 0;
					} else if ("constructor".equals(curToken) 
							|| "function".equals(curToken)) {
						isMethod = false;
						CompileSubroutine();
					} else if ("method".equals(curToken)){
						isMethod = true;
						CompileSubroutine();
					}  else {
						throw new RuntimeException("Unexpected Token -CompileClass : " + curToken);
					};
					
					jt.advance();
				}
				
				// } symbol
				String tailSymbolName = Character.toString(jt.symbol());
				if (!"}".equals(tailSymbolName)) {
					throw new RuntimeException("\"}\" not found. -CompileClass");
				}
				
				// class 컴파일 이후 VMWriter 종료
				vw.close();
			};
		} catch (IOException e) {
			System.err.println("Fail to load Tokens. -CompileClass");
		} finally {
	        System.out.println("== Done! - Compilation Engine ==");
		}
	}
	
	// 클래스 내부 변수 정의 컴파일
	public void CompileClassVarDec () {
		// System.out.println("CompileClassVarDec 호출");
		try {
			// static | field syntax
			if (!"static".equals(jt.getToken()) && !"field".equals(jt.getToken()) ) {
				throw new RuntimeException("STATIC OR FIELD not found -CompileClassVarDec");
			};
			String staticFieldIdentifier = jt.getToken();
			
			// type : class | int | char | boolean	syntax
			jt.advance();
			if (jt.tokenType() != TokenType.KEYWORD && jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("TYPE OR CUSTOM TYPE not found. -CompileClassVarDec");
			};
			String voidTypeName = jt.getToken();
			
			if (!voidTypeName.equals("void") 
					&& !voidTypeName.equals("int") 
					&& !voidTypeName.equals("char") 
					&& !voidTypeName.equals("boolean") 
					) {
				writeIdentifier(voidTypeName, staticFieldIdentifier);	// static | field	
			}
			
			// varName identifier
			jt.advance();
			if (jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("VAR NAME not found. -CompileClassVarDec");
			};
			String varName = jt.getToken();
			writeIdentifier(varName, staticFieldIdentifier);	// static | field
			countLocalVar++;
			
			// 처리한 토큰이 '..., varName' 에서 멈추도록 설정
			if (jt.peekNextToken().equals(",")) {
				jt.advance();	
			}			
			while (",".equals(jt.getToken())) {
				
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("VAR NAME not found. -CompileClassVarDec");
				};
				String subVarName = jt.getToken();
				writeIdentifier(subVarName, staticFieldIdentifier);	// static | field
				countLocalVar++;
			};
			
			// ; symbol
			jt.advance();
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -CompileClassVarDec");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// 함수 또는 생성자를 컴파일
	public void CompileSubroutine () {
		// System.out.println("CompileSubroutine 호출");
		try {
			if (jt.hasMoreTokens()) {
				// SymbolTable을 초기화 - 함수 scope를 유지하기 위함
				st.startSubroutine();
				
				// subroutineDec syntax
				
				// void | type [int|char|boolean|className]  syntax
				jt.advance();
				// className 으로 인해 Identifier로 추가
				if (jt.tokenType() != TokenType.KEYWORD && jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("KEYWORD OR IDENTIFIER TYPE not found. -CompileSubroutine");
				};
				String voidTypeName = jt.getToken();
				
				if (!voidTypeName.equals("void") 
						&& !voidTypeName.equals("int") 
						&& !voidTypeName.equals("char") 
						&& !voidTypeName.equals("boolean") 
						) {
					writeIdentifier(voidTypeName, "class");
				}
				
				// subroutineName syntax
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("SUB ROUTINE NAME not found. -CompileSubroutine");
				};
				String subRoutineName = jt.getToken();
				writeIdentifier(subRoutineName, "subroutine");
				if (subRoutineName.equals("new")) {
					isConstructor = true;
				}
				methodName = subRoutineName;
				
				// ( symbol
				jt.advance();
				if (!"(".equals(jt.getToken())) {
					throw new RuntimeException("\"(\" not found. -CompileSubroutine");
				};
				jt.advance();
				
				// parameter List
				// , ..., symbol 까지 처리
				
				while (!")".equals(jt.getToken())) {
					writeIdentifier(jt.getToken(), "argument");	// 서브루틴 인자
					jt.advance();
				};
				
				// ) symbol
				if (!")".equals(jt.getToken())) {
					throw new RuntimeException("\")\" not found. -CompileSubroutine");
				}
				
				// subroutineBody syntax
				compileSubroutineBody();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// 파라미터 목록 처리는 CompileSubroutine에서 처리함 (저자의 요구사항과 다르게 구성)
	// private void compileParameterList () {	};
	
	// 변수 선언 컴파일
	public void compileVarDec() {
		// System.out.println("compileVarDec 호출" + parent);
		try {
			// var syntax
			if (!"var".equals(jt.getToken())) {
				throw new RuntimeException("VAR keyword not found. -compileVarDec");
			};

			// type syntax
			jt.advance();
			if (jt.tokenType() != TokenType.KEYWORD && jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("TYPE OR CUSTOM TYPE not found. -compileVarDec");
			};
			String typeName = jt.getToken();
			writeIdentifier(typeName, "class");	// type이 클래스 일수도있잖혀
			
			// varName syntax
			jt.advance();
			if (jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("VAR NAME not found. -compileVarDec");
			};
			String varName = jt.getToken();
			writeIdentifier(varName, "var");	// 지역변수
			countLocalVar++;
			
			// 2개 이상의 varName 처리
			jt.advance();
			while (",".equals(jt.getToken())) {
				// , symbol
				
				// varName syntax
				jt.advance();
				writeIdentifier(jt.getToken(), "var");
				countLocalVar++;
				
				jt.advance();
			};
			
			// ; symbol
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileVarDec");
			}
			jt.advance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	// 함수 본문 컴파일
	public void compileSubroutineBody() {
		// System.out.println("compileSubroutineBody 호출");
		try {
			// 서부루틴 지역변수 개수 초기화
			countLocalVar = 0;	

			// { symbol
			jt.advance();
			if (!"{".equals(jt.getToken())) {
				throw new RuntimeException("\"{\" not found. -compileSubroutineBody");
			};
			jt.advance();
			
			// 한개 이상의 varDec syntax
			while ("var".equals(jt.getToken())) {
				compileVarDec();
			};
			
			// vm 함수 작성
			vw.writeFunction(className + "." + methodName, countLocalVar);
			if (isConstructor) {			// 생성자인 경우 메모리 할당 및 세그먼트 값 설정
				vw.writePop(Segment.CONST, countFieldVar);
				vw.writeCall("Memory.alloc", 1);
				vw.writePop(Segment.POINTER, 0);
			} else if (isMethod) {			// 일반 함수일 경우 변수 세그먼트 값 설정
				vw.writePush(Segment.ARG, 0);
				vw.writePop(Segment.POINTER, 0);
			}
			
			// statements 컴파일
			compileStatements();
			
			// } symbol
			if (!"}".equals(jt.getToken())) {
				throw new RuntimeException("\"}\" not found. -compileSubroutineBody");
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// 식별자 구분 및 세그먼트 별 index 부여
	private void writeIdentifier(String input, String identifierType) {
		if (!st.isContainKey(input) ) {
			if (identifierType.equals("static")) {
				st.Define(input, identifierType, Kind.STATIC);
			} else if (identifierType.equals("field")) {
				st.Define(input, identifierType, Kind.FIELD);
			} else if (identifierType.equals("arg")) {
				st.Define(input, identifierType, Kind.ARG);
			} else if (identifierType.equals("var")) {
				st.Define(input, identifierType, Kind.VAR);
			}	
		}
	};

	// 명령문 컴파일 - let, if, while, do, return
	public void compileStatements() {
		try {
			// 명령어 타입 저장
			statementType = jt.getToken();
			
			while (jt.hasMoreTokens()) {
				switch(jt.getToken()) {
					case "let":
						compileLet();
						jt.advance();
						break;
					case "if":
						compileIf();
						jt.advance();
						break;
					case "while":
						compileWhile();
						jt.advance();
						break;
					case "do":
						compileDo();
						jt.advance();
						break;
					case "return":
						compileReturn();
						break;
					default :
						return;
				};
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// let 구문 컴파일
	private void compileLet() {
		// System.out.println("compilLet - " + jt.getToken());
		try {
			// let header
			if (!"let".equals(jt.getToken())) {
				throw new RuntimeException("\"LET\" not found. -compileLet");
			}

			// varName identifier
			jt.advance();
			if (jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("VAR NAME not found.  -compileLet");
			}
			writeIdentifier(jt.getToken(), "var");
			letVarName = jt.getToken();
			
			// 표현식이 존재 할 경우 '[ 표현식 ]' 으로 구성
			jt.advance();
			if ("[".equals(jt.getToken())) {

				// 표현식 syntax
				jt.advance();
				CompileExpression();
				
				// ] symbol
				jt.advance();
				
				if (!"]".equals(jt.getToken())) {
					throw new RuntimeException("] not found.  -compileLet");
				};
				
				// 변수의 세그먼트를 추출하여 vm 코드 작성
				Kind kind = st.KindOf(letVarName);
				Segment letVarSegment = null;
				switch (kind) {
					case STATIC :
						letVarSegment = Segment.STATIC;
						break;
					case FIELD :
						letVarSegment = Segment.THIS;
						break;
					case ARG :
						letVarSegment = Segment.ARG;
						break;
					case VAR :
						letVarSegment = Segment.LOCAL;
						break;
					default :
						break;
				};
				vw.writePush(letVarSegment, st.IndexOf(letVarName));
				vw.WriteArithmetic("add");
				vw.writePop(Segment.POINTER, 1);
				
				jt.advance();
			};
			
			// = symbol
			if (!"=".equals(jt.getToken())) {
				throw new RuntimeException("= not found. -compileLet");
			};
			
			// expression syntax
			jt.advance();
			CompileExpression();
			
			// 변수의 세그먼트를 추출하여 vm 코드 작성
			Kind kind = st.KindOf(statementType);
			Segment popValue = null;
			switch (kind) {
				case STATIC :
					popValue = Segment.STATIC;
					break;
				case FIELD :
					popValue = Segment.THIS;
					break;
				case ARG :
					popValue = Segment.ARG;
					break;
				case VAR :
					popValue = Segment.LOCAL;
					break;
				default :
					break;
			};
			vw.writePop(popValue, st.IndexOf(statementType));
			
			// ; symbol
			jt.advance();
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileLet");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};	
	
	// if 구문 컴파일
	private void compileIf() {
		System.out.println("compileIf");
		try {
			// if header 
			if (!"if".equals(jt.getToken())) {
				throw new RuntimeException("\"IF\" not found -compileIf");
			}
			
			// ( symbol
			jt.advance();
			if (!"(".equals(jt.getToken())) {
				throw new RuntimeException("\"(\" not found. -compileIf");
			};
			
			// expression syntax
			jt.advance();
			CompileExpression();

			// ) symbol
			jt.advance();
			if (!")".equals(jt.getToken())) {
				throw new RuntimeException("\"(\" not found. -compileIf");
			};
			
			// { symbol
			jt.advance();
			if (!"{".equals(jt.getToken())) {
				throw new RuntimeException("\"{\" not found. -compileIf");
			};
		
			// statements syntax
			jt.advance();
			compileStatements();
			
			// } symbol
			if (!"}".equals(jt.getToken())) {
				throw new RuntimeException("\"}\" not found. -compileIf");
			};
			
			if ("else".equals(jt.getToken())) {
				// else keyword
				
				// { symbol
				jt.advance();
				if (!"{".equals(jt.getToken())) {
					throw new RuntimeException("\"{\" not found. -compileIf");
				};
				
				// statements syntax
				jt.advance();
				compileStatements();
				
				// } symbol
				if (!"}".equals(jt.getToken())) {
					throw new RuntimeException("\"}\" not found. -compileIf");
				};
			};
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// while 구문 컴파일
	private void compileWhile() {
		// System.out.println("compileWhile");
		try {
			// while syntax
			if (!"while".equals(jt.getToken())) {
				throw new RuntimeException("\"WHILE\" not found -compileWhile");
			}
			
			// ( symbol
			jt.advance();
			if (!"(".equals(jt.getToken())) {
				throw new RuntimeException("\"(\" not found. -compileWhile");
			};
			
			// expression syntax
			jt.advance();
			CompileExpression();
	
			// ) symbol
			jt.advance();
			if (!")".equals(jt.getToken())) {
				throw new RuntimeException("\")\" not found. -compileWhile");
			};
			
			// { symbol
			jt.advance();
			if (!"{".equals(jt.getToken())) {
				throw new RuntimeException("\"{\" not found. -compileWhile");
			};
		
			// statements
			jt.advance();
			compileStatements(); // advance 수행 후 반환
			
			// } symbol
			if (!"}".equals(jt.getToken())) {
				throw new RuntimeException("\"}\" not found. -compileWhile");
			};		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// do 구문 컴파일
	private void compileDo() {
		// 함수 호출로 함수 스코프 변수 초기화
		st.startSubroutine();
		// System.out.println("compileDo");
		try {
			// do header
			if (!"do".equals(jt.getToken())) {
				throw new RuntimeException("\"DO\" not found. -compileDo");
			}
			
			// subroutine syntax
			// subroutine - 여러개의 토큰을 처리할 가능성이 있음. JackTokenizer 을 직접 사용하도록 해!
			jt.advance();
			compileSubroutineCall(jt.getToken());
			
			// ; symbol
			jt.advance();
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileDo");
			}
			
			vw.writePop(Segment.TEMP, 0);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// return 구문 컴파일
	private void compileReturn() {
		 // System.out.println("compileReturn");
		try {
			// return syntax
			if (!"return".equals(jt.getToken())) {
				throw new RuntimeException("\"RETURN\" not found. -compileReturn");
			}
			
			// expression syntax 처리, expression이 없으면 ';' 이 현재 토큰값
			jt.advance();
			if (!";".equals(jt.getToken())) {
				CompileExpression();
				jt.advance();
			}
			
			// ; symbol
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileReturn");
			}
			
			vw.writeReturn();
			
			jt.advance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// 표현식 컴파일
	public void CompileExpression() throws IOException {
		// System.out.println("CompileExpression - value : " + jt.getToken());
		// term syntax
		CompileTerm();
		
		// (op term)이 없을 경우 해당하지 않는 토큰이 pop 되는 현상 방지
		String op = jt.peekNextToken();
		
		// (op term)*
		while (jt.isOp(op)) {
			// op symbol
			jt.advance();
			String opSymbol = Character.toString(jt.symbol());
			
			// term
			jt.advance();
			CompileTerm();
			
			// 각 연산자 별 VM 출력
			switch (opSymbol) {
				case "+" :
					vw.WriteArithmetic("add");
					break;
				case "-":
					vw.WriteArithmetic("sub");
					break;
				case"*" :
					vw.writeCall("Math.multiply", 2);
					break;
				case "/" : 
					vw.writeCall("Math.divide", 2);
					break;
				case "&":
					vw.WriteArithmetic("and");
					break;
				case "|":
					vw.WriteArithmetic("or");
					break;
				case "<":
					vw.WriteArithmetic("lt");
					break;
				case ">" :
					vw.WriteArithmetic("gt");
					break;
				case "=" :
					vw.WriteArithmetic("eq");
					break;
				default :
					break;
			}
			
			op = jt.peekNextToken();
		};
	};
	
	/**
	 * (expression (',' expression)* )?
	 */
	// 표현식 목록 컴파일
	private void compileExpressionList() {
		// System.out.println("CompileExpressionList - " + jt.getToken());
		try {
			// 하나라도 값이 있는 경우
			if (!")".equals(jt.getToken())) {
				CompileExpression();
				countArgs++;
			};
			
			// 2개 이상의 표현식 syntax 처리
			while (",".equals(jt.getToken())) {
				jt.advance();
				if (!",".equals(jt.getToken())) {
					throw new RuntimeException("\",\" not found. -compileExpressionList");
				};
				
				// expression syntax
				jt.advance();
				CompileExpression();
				countArgs++;
			};
			
			// 현재 identifier 값까지만 출력하고 큐에 대기
		}	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// term 컴파일
	public void CompileTerm() {
		// System.out.println("CompileTerm - " + jt.getToken());
		try {
			if (jt.tokenType() == TokenType.INT_CONST) {
				// 상수 처리
				vw.writePush(Segment.CONST, Integer.parseInt(jt.getToken()));
			} else if (jt.tokenType() == TokenType.STRING_CONST) {
				// 문자열 처리
				vw.writePush(Segment.CONST, jt.getToken().length());
				vw.writeCall("String.new", 1);
				for (int i = 0; i < jt.getToken().length(); i++) {
					vw.writePop(Segment.CONST, (int)jt.getToken().charAt(i));
					vw.writeCall("String.appendChar", 2);
				}
			} else if ("true".equals(jt.getToken())
					|| "false".equals(jt.getToken())
					|| "null".equals(jt.getToken()) 
					|| "this".equals(jt.getToken())) {				
				if ("true".equals(jt.getToken())) {
					vw.writePop(Segment.CONST, 0);
					vw.WriteArithmetic("not");
				} else if ("false".equals(jt.getToken())) {
					vw.writePush(Segment.CONST, 0);
				} else if ("this".equals(jt.getToken())) {
					vw.writePop(Segment.POINTER, 0);
				}
			} else if (jt.tokenType() == TokenType.IDENTIFIER) {	// case : Keyboard
				String identifierName = jt.identifier();
				
				// routineName(exp) | identifier.routineName
				// 큐에 대기중인 값으로 유형 판별
				String nextToken = jt.peekNextToken();

				if ("[".equals(nextToken)) {
					jt.advance();
					// var[10]과 같은 배열 처리
					writeIdentifier(identifierName, "var");	// 지역 변수
					
					jt.advance();
					CompileExpression();
					vw.WriteArithmetic("add");
					vw.writePop(Segment.POINTER, 1);
					
					jt.advance();
					if (!"]".equals(jt.getToken())) {
						throw new RuntimeException("\"]\" not found. 없슈 -CompileTerm " + jt.getToken() );
					}
				} else if ("(".equals(nextToken) || ".".equals(nextToken))  {
		            // 현재 IDENTIFIER[] or Identifier.METHOD 처럼 되어있는 상태
					subroutineClassName = identifierName;
		            compileSubroutineCall(identifierName);
				} else {
					// 단순히 출력만 수행
					writeIdentifier(identifierName, "var");
				}
			} else if ("(".equals(jt.getToken())) {
				// ( symbol
				
				jt.advance();
				CompileExpression();
				
				// ) symbol
			} else if ("[".equals(jt.getToken())) {
				// [ symbol
				
				// expression syntax
				jt.advance();
				compileExpressionList();
				
				// ]
				jt.advance();
				if (!"]".equals(jt.getToken())) {
					throw new RuntimeException("\"]\" not found. -CompileTerm " + jt.getToken() );
				};
			} else if ("-".equals(jt.getToken()) || "~".equals(jt.getToken())) {
				if ("-".equals(jt.getToken())) {
					vw.WriteArithmetic("neg");
				} else if ("~".equals(jt.getToken())) {
					vw.WriteArithmetic("not");
				};
				CompileTerm();
			} else {
				throw new RuntimeException("Unexpected Token -CompileTerm : "+ jt.getToken());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	/**
	 * subRoutineCall : 
	 * 	( subroutineName '(' expressionList ')' )
	 * 	| ( (className | varName ) '.' subroutineName '(' expressionList ')' )
	 * 
	 * @param identifier : subroutineName 식별자
	 */
	// 서브루틴 호출 컴파일
	private void compileSubroutineCall(String identifier) {
		// System.out.println("compileSubroutineCall " + identifier + " " + jt.getToken());
		try {			
			// subroutineName or ClassName | varName syntax
			String subRoutineName = identifier;
			
			jt.advance();
			if ("(".equals(jt.getToken())) {
				writeIdentifier(subRoutineName, "subroutine");	// 서브루틴
				// ( subroutineName '(' expressionList ')' )
				// ( symbol

				countArgs = 0;
				// Expression Lists syntax
				jt.advance();
				compileExpressionList();
				
				// ) symbol
				if (!")".equals(jt.getToken())) {
					throw new RuntimeException("\")\" not found.");
				};
				// 함수 호출 vm 코드 작성
				vw.writeCall(subroutineClassName + "." + subRoutineName, countArgs);
			} else if (".".equals(jt.getToken())) {
				// ( (className | varName ) '.' subroutineName '(' expressionList ')' )
				// SymbolTalbe에 존재하면 var 이고 없으면 classType
				if (st.isContainKey(subRoutineName)) {
					writeIdentifier(subRoutineName, "var");	// 지역변수	
				} else {
					writeIdentifier(subRoutineName, "class");	// 클래스
				}
				
				// . symbol
				
				// subroutineName identifier
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("SUBROUTINE NAME IDENTIFIER not found. -compileSubroutineCall");
				};
				writeIdentifier(jt.getToken(), "subroutine");	// 서브루틴
				
				// ( symbol
				jt.advance();
				if (!"(".equals(jt.getToken())) {
					throw new RuntimeException("\"(\" not found. -compileSubroutineCall");
				};
				
				// ExpressionList syntax
				jt.advance();
				compileExpressionList();
				
				// compileExpressionList 처리 후 현재 토큰이 varName 인 경우
				if (!")".equals(jt.getToken())) {

					jt.advance();
				}
				
				// ) symbol
				if (!")".equals(jt.getToken())) {
					throw new RuntimeException("\")\" not found. -compileSubroutineCall");
				};
			} else {
				throw new RuntimeException("'(' or '.' not found. -compileSubroutineCall");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
