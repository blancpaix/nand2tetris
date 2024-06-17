package compiler_code;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;

// XML 문서 구현을 위해 부모-Element를 parameter로 입력받음
public class CompilationEngine_Syntax {
	private DocumentBuilderFactory documentFactory;
	private DocumentBuilder documentBuilder;
	private Document document;
	private JackTokenizer jt;
	private SymbolTable st;
	
	// XML 파일 출력을 위한 input, output 파일 설정
	public CompilationEngine_Syntax(String inputFile, String outputFilePath) throws ParserConfigurationException, IOException {
		try {
			documentFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            
			jt = new JackTokenizer(inputFile);
			st = new SymbolTable();
			
			System.out.println("초기화");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	};	
	
	// 클래스 컴파일 후 XML 파일 출력
	// 재귀함수로 트리형태의 Syntax 처리
	public void CompileClass (String ouputFilePath, String vmFilePath) throws TransformerException {
		// System.out.println("CompileClass 호출");
		try {
			if (jt.hasMoreTokens()) {
				// class
				jt.advance();
				String classToken = jt.getToken();
				if (!"class".equals(classToken)) {
					throw new RuntimeException("\"Class\" keyword not found. -CompileClass");
				};
				// 최상단 class wrapper
				Element classWrapper = document.createElement(classToken);
				document.appendChild(classWrapper);
				
				// class keyword
				writeKeyword(classWrapper, classToken);
				
				// class identifier
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("\"Class Identifier\" not found. -CompileClass");
				}
				String identifierName = jt.identifier();
				writeIdentifier(classWrapper, identifierName, "class");	// 클래스
				
				// { symbol
				jt.advance();
				if (!"{".equals(jt.getToken())) {
					throw new RuntimeException("\"{\" not found. -CompileClass");
				}
				String headSymbolName = Character.toString(jt.symbol());
				writeSymbol(classWrapper, headSymbolName);
				
				// 재귀
				jt.advance();
				while (!"}".equals(jt.getToken())) {
					String curToken = jt.getToken();
					if ("static".equals(curToken) 
							|| "field".equals(curToken)) {
						CompileClassVarDec(classWrapper);
					} else if ("constructor".equals(curToken) 
							|| "function".equals(curToken) 
							|| "method".equals(curToken)) {
						CompileSubroutine(classWrapper);
					} else {
						throw new RuntimeException("Unexpected Token -CompileClass : " + curToken);
					};
					
					jt.advance();
				}
				
				// } symbol
				String tailSymbolName = Character.toString(jt.symbol());
				if (!"}".equals(tailSymbolName)) {
					throw new RuntimeException("\"}\" not found. -CompileClass");
				}
				writeSymbol(classWrapper, tailSymbolName);
			};
		} catch (IOException e) {
			System.err.println("Fail to load Tokens. -CompileClass");
		} finally {
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer;
			transformer = transformerFactory.newTransformer();
	        DOMSource domSource = new DOMSource(document);
	        StreamResult streamResult = new StreamResult(new File(ouputFilePath));

	        transformer.transform(domSource, streamResult);
	       
	        System.out.println("== Done creating XML File ==");
		}
	}
	
	// 클래스 내부 변수 정의 컴파일
	public void CompileClassVarDec (Element parent) {
		// System.out.println("CompileClassVarDec 호출");
		try {
			// static | field	syntax
			if (!"static".equals(jt.getToken()) && !"field".equals(jt.getToken()) ) {
				throw new RuntimeException("STATIC OR FIELD not found -CompileClassVarDec");
			};
			String staticFieldIdentifier = jt.getToken();
			Element elClassVarDec = document.createElement("classVarDec");
			writeKeyword(elClassVarDec, staticFieldIdentifier);
			parent.appendChild(elClassVarDec);
			
			// type : class | int | char | boolean	syntax
			// classType일 경우 identifier로 처리
			jt.advance();
			if (jt.tokenType() != TokenType.KEYWORD && jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("TYPE OR CUSTOM TYPE not found. -CompileClassVarDec");
			};
			String voidTypeName = jt.getToken();
			
			if (voidTypeName.equals("void") 
					|| voidTypeName.equals("int") 
					|| voidTypeName.equals("char") 
					|| voidTypeName.equals("boolean") 
					) {
				writeKeyword(elClassVarDec, voidTypeName);
			} else {
				writeIdentifier(elClassVarDec, voidTypeName, staticFieldIdentifier);	// static | field	
			}
			
			// varName identifier
			jt.advance();
			if (jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("VAR NAME not found. -CompileClassVarDec");
			};
			String varName = jt.getToken();
			writeIdentifier(elClassVarDec, varName, staticFieldIdentifier);	// static | field
			
			// 처리한 토큰이 '..., varName' 에서 멈추도록 설정
			if (jt.peekNextToken().equals(",")) {
				jt.advance();	
			}
			while (",".equals(jt.getToken())) {
				writeSymbol(elClassVarDec, ",");
				
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("VAR NAME not found. -CompileClassVarDec");
				};
				String subVarName = jt.getToken();
				writeIdentifier(elClassVarDec, subVarName, staticFieldIdentifier);	// static | field
			};
			
			// ; symbol
			jt.advance();
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -CompileClassVarDec");
			}
			String terminatorSymbol = Character.toString(jt.symbol());
			writeSymbol(elClassVarDec, terminatorSymbol);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// 함수 또는 생성자를 컴파일	
	public void CompileSubroutine (Element parent) {
		// System.out.println("CompileSubroutine 호출");
		try {
			if (jt.hasMoreTokens()) {
				// SymbolTable을 초기화 - 함수 scope를 유지하기 위함
				st.startSubroutine();
				
				// subroutineDec	syntax
				Element elSubroutineDec = document.createElement("subroutineDec");
				String curToken = jt.getToken();
				writeKeyword(elSubroutineDec, curToken);
				parent.appendChild(elSubroutineDec);
				
				// void | type[int|char|boolean|className] syntax
				jt.advance();
				// className 으로 인해 Identifier로 추가
				if (jt.tokenType() != TokenType.KEYWORD && jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("KEYWORD OR IDENTIFIER TYPE not found. -CompileSubroutine");
				};
				String voidTypeName = jt.getToken();
				
				if (voidTypeName.equals("void") 
						|| voidTypeName.equals("int") 
						|| voidTypeName.equals("char") 
						|| voidTypeName.equals("boolean") 
						) {
					writeKeyword(elSubroutineDec, voidTypeName);
				} else {
					writeIdentifier(elSubroutineDec, voidTypeName, "class");
				}

				// subroutineName syntax
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("SUB ROUTINE NAME not found. -CompileSubroutine");
				};
				String subRoutineName = jt.getToken();
				writeIdentifier(elSubroutineDec, subRoutineName, "subroutine");
				
				// ( symbol
				jt.advance();
				if (!"(".equals(jt.getToken())) {
					throw new RuntimeException("\"(\" not found. -CompileSubroutine");
				};
				String headSymbolName = Character.toString(jt.symbol());
				writeSymbol(elSubroutineDec, headSymbolName);
				
				// parameter List
				Element parameterList = document.createElement("parameterList");
				elSubroutineDec.appendChild(parameterList);
				
				// , ...  symbol 까지 처리
				jt.advance();
				while (!")".equals(jt.getToken())) {
					String parameterToken = jt.getToken();
					writeKeyword(parameterList, parameterToken);
					jt.advance();
					writeIdentifier(parameterList, jt.getToken(), "argument");	// 서브루틴 인자
				};
				
				// ) symbol
				if (!")".equals(jt.getToken())) {
					throw new RuntimeException("\")\" not found. -CompileSubroutine");
				}
				String tailSymbolName = Character.toString(jt.symbol());
				writeSymbol(elSubroutineDec, tailSymbolName);
				
				// subroutineBody
				compileSubroutineBody(elSubroutineDec);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// 파라미터 목록 처리는 CompileSubroutine에서 처리함 (저자의 요구사항과 다르게 구성)
	// private void compileParameterList () {	};
	
	// 변수 선언 컴파일
	public void compileVarDec(Element parent) {
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
			writeIdentifier(parent, typeName, "class");
			
			// varName syntax
			jt.advance();
			if (jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("VAR NAME not found. -compileVarDec");
			};
			String varName = jt.getToken();
			writeIdentifier(parent, varName, "var");	// 지역변수
			
			// 2개 이상의 varName 처리
			jt.advance();
			while (",".equals(jt.getToken())) {
				// , symbol
				writeSymbol(parent, ",");
				// varName	syntax
				jt.advance();
				writeIdentifier(parent, jt.getToken(), "var");
				
				jt.advance();
			};
			
			// ; symbol
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileVarDec");
			}
			String terminatorSymbol = Character.toString(jt.symbol());
			writeSymbol(parent, terminatorSymbol);
			
			jt.advance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e);
			e.printStackTrace();
		}
	}
	
	// 함수 본문 컴파일
	public void compileSubroutineBody(Element parent) {
		// System.out.println("compileSubroutineBody 호출");
		try {
			Element elSubroutineBody = document.createElement("subroutineBody");
			String curToken = jt.getToken();
			writeKeyword(elSubroutineBody, curToken);
			parent.appendChild(elSubroutineBody);
			
			// { symbol
			jt.advance();
			if (!"{".equals(jt.getToken())) {
				throw new RuntimeException("\"{\" not found. -compileSubroutineBody");
			};
			String headSymbolName = Character.toString(jt.symbol());
			writeSymbol(elSubroutineBody, headSymbolName);
			jt.advance();
			
			// 한개 이상의 varDec syntax
			while ("var".equals(jt.getToken())) {
				Element elVarDec = document.createElement("varDec");
				writeKeyword(elVarDec, jt.getToken());
				compileVarDec(elVarDec);
				elSubroutineBody.appendChild(elVarDec);
			};
			
			// statements 컴파일
			compileStatements(elSubroutineBody);
			
			// } symbol
			if (!"}".equals(jt.getToken())) {
				throw new RuntimeException("\"}\" not found. -compileSubroutineBody");
			};
			String tailSymbolName = Character.toString(jt.symbol());
			writeSymbol(elSubroutineBody, tailSymbolName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 키워드 XML 입력
	private void writeKeyword(Element parent, String input) {
		// System.out.println("<keyword/> " + input);
		Element keyword = document.createElement("keyword");
		keyword.appendChild(document.createTextNode(input));
		parent.appendChild(keyword);
	};
	
	// 식별자 XML 입력 + 식별자 구분 및 세그먼트 별 index 부여
	private void writeIdentifier(Element parent, String input, String identifierType) {
		int idx = -1;
		Kind identiType = Kind.NONE;
		if (st.isContainKey(input) ) {
			idx = st.IndexOf(input);
			identiType = st.KindOf(input);
		} else {
			if (identifierType.equals("static")) {
				idx = st.Define(input, identifierType, Kind.STATIC);
				identiType = st.KindOf(input);
			} else if (identifierType.equals("field")) {
				idx = st.Define(input, identifierType, Kind.FIELD);
				identiType = st.KindOf(input);
			} else if (identifierType.equals("arg")) {
				idx = st.Define(input, identifierType, Kind.ARG);
				identiType = st.KindOf(input);
			} else if (identifierType.equals("var")) {
				idx = st.Define(input, identifierType, Kind.VAR);
				identiType = st.KindOf(input);
			}	
		}
		
		// System.out.println("<identifier/> " + input + " - " + identifierType + " | " + identiType + " " + idx);
		Element identifier = document.createElement("identifier");
		identifier.appendChild(document.createTextNode(input + " - " + identifierType + " | " + identiType + " " + idx));
		parent.appendChild(identifier);
	};

	// symbol XML 입력
	private void writeSymbol(Element parent, String input) {
		// System.out.println("<symbol/> " + input);
		Element symbol = document.createElement("symbol");
		
		if (input.equals("<")) {
			symbol.appendChild(document.createTextNode("&lt"));
		} else if (input.equals(">")) {
			symbol.appendChild(document.createTextNode("&gt"));
		} else if (input.equals("\"")) {
			symbol.appendChild(document.createTextNode("&quot"));
		} else if (input.equals("&")) {
			symbol.appendChild(document.createTextNode("&amp"));
		} else {
			symbol.appendChild(document.createTextNode(input));	
		}
		parent.appendChild(symbol);
	};
	
	// 명령문 컴파일 - let, if, while, do, return
	public void compileStatements(Element parent) {
		Element elStatements = document.createElement("statements");
		parent.appendChild(elStatements);
		
		// XML 작성 시 {, } 심볼 제외
		try {
			while (jt.hasMoreTokens()) {
				switch(jt.getToken()) {
					case "let":
						compileLet(elStatements);
						jt.advance();
						break;
					case "if":
						compileIf(elStatements);
						jt.advance();
						break;
					case "while":
						compileWhile(elStatements);
						jt.advance();
						break;
					case "do":
						compileDo(elStatements);
						jt.advance();
						break;
					case "return":
						compileReturn(elStatements);
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
	private void compileLet(Element parent) {
		// System.out.println("compilLet - " + jt.getToken());
		try {
			// let header + keyword
			Element elLet = document.createElement("letStatement");
			if (!"let".equals(jt.getToken())) {
				throw new RuntimeException("\"LET\" not found. -compileLet");
			}
			writeKeyword(elLet, jt.getToken());
			parent.appendChild(elLet);

			// varName identifier
			jt.advance();
			if (jt.tokenType() != TokenType.IDENTIFIER) {
				throw new RuntimeException("VAR NAME not found.  -compileLet");
			}
			writeIdentifier(elLet, jt.getToken(), "var");		
			
			// 표현식이 존재 할 경우 '[ 표현식 ]' 으로 구성
			jt.advance();

			if ("[".equals(jt.getToken())) {
				// [ symbol
				writeSymbol(elLet, jt.getToken());
				jt.advance();
				
				// 표현식 syntax
				CompileExpression(elLet);
				jt.advance();
				
				// ] symbol
				if (!"]".equals(jt.getToken())) {
					throw new RuntimeException("] not found.  -compileLet");
				};
				writeSymbol(elLet, jt.getToken());
				jt.advance();
			};
			
			// = symbol
			if (!"=".equals(jt.getToken())) {
				throw new RuntimeException("= not found. -compileLet");
			};
			writeSymbol(elLet, jt.getToken());
			
			// expression syntax
			jt.advance();
			CompileExpression(elLet);
						
			// ; symbol
			jt.advance();
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileLet");
			}
			String terminatorSymbol = Character.toString(jt.symbol());
			writeSymbol(elLet, terminatorSymbol);	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// if 구문 컴파일
	private void compileIf(Element parent) {
		System.out.println("compileIf");
		try {
			// if header + keyword
			Element elIf = document.createElement("ifStatement");
			if (!"if".equals(jt.getToken())) {
				throw new RuntimeException("\"IF\" not found -compileIf");
			}
			writeKeyword(elIf, jt.getToken());
			parent.appendChild(elIf);
			
			// ( symbol
			jt.advance();
			if (!"(".equals(jt.getToken())) {
				throw new RuntimeException("\"(\" not found. -compileIf");
			};
			writeSymbol(elIf, jt.getToken());
			
			// expression syntax
			jt.advance();
			CompileExpression(elIf);

			// ) symbol
			jt.advance();
			if (!")".equals(jt.getToken())) {
				throw new RuntimeException("\"(\" not found. -compileIf");
			};
			writeSymbol(elIf, jt.getToken());
			
			// { symbol
			jt.advance();
			if (!"{".equals(jt.getToken())) {
				throw new RuntimeException("\"{\" not found. -compileIf");
			};
			writeSymbol(elIf, jt.getToken());
		
			// statements syntax
			jt.advance();
			compileStatements(elIf);
			
			// } symbol
			if (!"}".equals(jt.getToken())) {
				throw new RuntimeException("\"}\" not found. -compileIf");
			};
			writeSymbol(elIf, jt.getToken());
			
			if ("else".equals(jt.getToken())) {
				// else keyword
				writeKeyword(elIf, jt.getToken());
				
				// { symbol
				jt.advance();
				if (!"{".equals(jt.getToken())) {
					throw new RuntimeException("\"{\" not found. -compileIf");
				};
				writeSymbol(elIf, jt.getToken());
				
				// statements syntax
				jt.advance();
				compileStatements(elIf);
				
				// } symbol
				if (!"}".equals(jt.getToken())) {
					throw new RuntimeException("\"}\" not found. -compileIf");
				};
				writeSymbol(elIf, jt.getToken());
			};
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// while 구문 컴파일
	private void compileWhile(Element parent) {
		// System.out.println("compileWhile");
		try {
			// while header + keyword
			Element elWhile = document.createElement("whileStatement");
			if (!"while".equals(jt.getToken())) {
				throw new RuntimeException("\"WHILE\" not found -compileWhile");
			}
			writeKeyword(elWhile, jt.getToken());
			parent.appendChild(elWhile);
			
			// ( symbol
			jt.advance();
			if (!"(".equals(jt.getToken())) {
				throw new RuntimeException("\"(\" not found. -compileWhile");
			};
			writeSymbol(elWhile, jt.getToken());
			
			// expression syntax
			jt.advance();
			CompileExpression(elWhile);
	
			// ) symbol
			jt.advance();
			if (!")".equals(jt.getToken())) {
				throw new RuntimeException("\")\" not found. -compileWhile");
			};
			writeSymbol(elWhile, jt.getToken());
			
			// { symbol
			jt.advance();
			if (!"{".equals(jt.getToken())) {
				throw new RuntimeException("\"{\" not found. -compileWhile");
			};
			writeSymbol(elWhile, jt.getToken());
			jt.advance();

			// statements syntax
			compileStatements(elWhile); // jt.advance() 호출 후 반환
			
			// } symbol
			if (!"}".equals(jt.getToken())) {
				throw new RuntimeException("\"}\" not found. -compileWhile");
			};
			writeSymbol(elWhile, jt.getToken());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// do 구문 컴파일
	private void compileDo(Element parent) {
		// System.out.println("compileDo");
		st.startSubroutine();
		try {
			// do header + keyword
			Element elDo = document.createElement("doStatement");
			if (!"do".equals(jt.getToken())) {
				throw new RuntimeException("\"DO\" not found. -compileDo");
			}
			writeKeyword(elDo, jt.getToken());
			
			// subroutine syntax
			// 서브루틴 내부에서 여러개의 토큰을 처리할 가능성이 있음. JackTokenizer 을 직접 사용하도록 해!
			jt.advance();
			compileSubroutineCall(elDo, jt.getToken());
			parent.appendChild(elDo);
			
			// ; symbol
			jt.advance();
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileDo");
			}
			String terminatorSymbol = Character.toString(jt.symbol());
			writeSymbol(elDo, terminatorSymbol);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// return 구문 컴파일
	private void compileReturn(Element parent) {
		 // System.out.println("compileReturn");
		try {
			// return header + keyword
			Element elReturn = document.createElement("returnStatement");
			if (!"return".equals(jt.getToken())) {
				throw new RuntimeException("\"RETURN\" not found. -compileReturn");
			}
			writeKeyword(elReturn, jt.getToken());
			parent.appendChild(elReturn);
			jt.advance();
			
			// expression syntax 처리, expression이 없으면 ';' 이 현재 토큰값
			if (!";".equals(jt.getToken())) {
				CompileExpression(elReturn);
				jt.advance();
			}
			
			// ; symbol
			if (!";".equals(jt.getToken())) {
				throw new RuntimeException("\";\" not found. -compileReturn");
			}
			String terminatorSymbol = Character.toString(jt.symbol());
			writeSymbol(elReturn, terminatorSymbol);

			jt.advance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// 표현식 컴파일
	public void CompileExpression(Element parent) throws IOException {
		// System.out.println("CompileExpression - value : " + jt.getToken());
		// Expression header
		Element elExpression = document.createElement("expression");
		parent.appendChild(elExpression);

		// term syntax
		CompileTerm(elExpression);
		
		// (op term)이 없을 경우 해당하지 않는 토큰이 pop 되는 현상 방지
		String op = jt.peekNextToken();
		
		// (op term)*
		while (jt.isOp(op)) {
			// op symbol
			jt.advance();
			writeSymbol(elExpression, Character.toString(jt.symbol()));	
			
			// term syntax
			jt.advance();
			CompileTerm(elExpression);
						
			op = jt.peekNextToken();
		};
	};
	
	/**
	 * (expression (',' expression)* )?
	 */
	// 표현식 목록 컴파일
	private void compileExpressionList(Element parent) {
		// System.out.println("CompileExpressionList - " + jt.getToken());
		try {
			Element elExpressionList = document.createElement("expressionList");
			parent.appendChild(elExpressionList);
			// 하나라도 값이 있는 경우
			if (!")".equals(jt.getToken())) {
				CompileExpression(elExpressionList);
			};
			
			// 2개 이상의 표현식 syntax 처리
			while (",".equals(jt.getToken())) {
				// , symbol
				jt.advance();
				if (!",".equals(jt.getToken())) {
					throw new RuntimeException("\",\" not found. -compileExpressionList");
				};
				writeSymbol(elExpressionList, jt.getToken());
				
				// expression syntax
				jt.advance();
				CompileExpression(elExpressionList);
			};
		}	catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	// term 컴파일
	public void CompileTerm(Element parent) {
		// System.out.println("CompileTerm - " + jt.getToken());
		try {
			Element elTerm = document.createElement("term");
			parent.appendChild(elTerm);
			
			if (jt.tokenType() == TokenType.INT_CONST) {
				writeIntConst(elTerm);
			} else if (jt.tokenType() == TokenType.STRING_CONST) {
				writeStringCosnt(elTerm);
			} else if ("true".equals(jt.getToken())
					|| "false".equals(jt.getToken())
					|| "null".equals(jt.getToken()) 
					|| "this".equals(jt.getToken())) {
				writeKeyword(elTerm, jt.getToken());
			} else if (jt.tokenType() == TokenType.IDENTIFIER) {	// case1 : Keyboard
				// var [expression] | subroutineCall (expression)
				String identifierName = jt.identifier();

				// 큐에 대기중인 값으로 유형 판별
				String nextToken = jt.peekNextToken();

				if ("[".equals(nextToken)) {
					// var [expression]
					// [ symbol
					jt.advance();
					writeIdentifier(elTerm, identifierName, "var");	// 지역 변수
					writeSymbol(elTerm, "[");
					
					// expression syntax
					jt.advance();
					CompileExpression(elTerm);
					
					// ] symbol
					jt.advance();
					if (!"]".equals(jt.getToken())) {
						throw new RuntimeException("\"]\" not found. 없슈 -CompileTerm " + jt.getToken() );
					}
					writeSymbol(elTerm, "]");
				} else if ("(".equals(nextToken) || ".".equals(nextToken))  {
					// subroutineCall (expression) - 저자가 제공한 파일에는 subroutine 대신 term 으로 출력됨
					// subroutine syntax
		            compileSubroutineCall(elTerm, identifierName);
				} else {
					// 단순히 출력만 수행
					writeIdentifier(elTerm, identifierName, "var");
				}
			} else if ("(".equals(jt.getToken())) {
				// ( symbol
				writeSymbol(elTerm, jt.getToken());
				
				// expression syntax
				jt.advance();
				CompileExpression(elTerm);
				
				// ) symbol
				writeSymbol(elTerm, jt.getToken());
			} else if ("[".equals(jt.getToken())) {
				// [ symbol
				writeSymbol(elTerm, jt.getToken());
				
				// expression syntax
				jt.advance();
				compileExpressionList(elTerm);
				
				jt.advance();
				// ] symbol
				if (!"]".equals(jt.getToken())) {
					throw new RuntimeException("\"]\" not found. -CompileTerm " + jt.getToken() );
				};
				writeSymbol(elTerm, jt.getToken());				
			} else if ("-".equals(jt.getToken()) || "~".equals(jt.getToken())) {
				writeSymbol(elTerm, jt.getToken());
				CompileTerm(elTerm);
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
	private void compileSubroutineCall(Element parent, String identifier) {
		// System.out.println("compileSubroutineCall " + identifier + " " + jt.getToken());
		try {
			Element elSubroutineCall = document.createElement("subroutineCall");
			parent.appendChild(elSubroutineCall);
			
			// subroutineName or ClassName | varName syntax
			String subRoutineName = identifier;
			// writeIdentifier(elSubroutineCall, subRoutineName, "subroutine");	// 서브루틴 이거나 클래스인데... 뭐여
			
			jt.advance();

			if ("(".equals(jt.getToken())) {
				writeIdentifier(elSubroutineCall, subRoutineName, "subroutine");	// 서브루틴
				// ( subroutineName '(' expressionList ')' )
				// ( symbol
				writeSymbol(elSubroutineCall, jt.getToken());

				// Expression Lists
				jt.advance();
				compileExpressionList(elSubroutineCall);
				
				// ) symbol
				if (!")".equals(jt.getToken())) {
					throw new RuntimeException("\")\" not found.");
				};
				writeSymbol(elSubroutineCall, jt.getToken());
			} else if (".".equals(jt.getToken())) {
				// ( (className | varName ) '.' subroutineName '(' expressionList ')' )
				// SymbolTalbe에 존재하면 var 이고 없으면 classType
				if (st.isContainKey(subRoutineName)) {
					writeIdentifier(elSubroutineCall, subRoutineName, "var");	// 지역변수	
				} else {
					writeIdentifier(elSubroutineCall, subRoutineName, "class");	// 클래스
				}
				
				// . symbol
				writeSymbol(elSubroutineCall, jt.getToken());
				
				// subroutineName identifier
				jt.advance();
				if (jt.tokenType() != TokenType.IDENTIFIER) {
					throw new RuntimeException("SUBROUTINE NAME IDENTIFIER not found. -compileSubroutineCall");
				};
				writeIdentifier(elSubroutineCall, jt.getToken(), "subroutine");	// 서브루틴
				
				// ( symbol
				jt.advance();
				if (!"(".equals(jt.getToken())) {
					throw new RuntimeException("\"(\" not found. -compileSubroutineCall");
				};
				writeSymbol(elSubroutineCall, jt.getToken());
				
				// ExpressionList
				jt.advance();
				compileExpressionList(elSubroutineCall);
				
				// compileExpressionList 처리 후 현재 토큰이 varName 인 경우
				if (!")".equals(jt.getToken())) {
					jt.advance();
				}
				
				// ) symbol
				if (!")".equals(jt.getToken())) {
					throw new RuntimeException("\")\" not found. -compileSubroutineCall");
				};
				writeSymbol(elSubroutineCall, jt.getToken());
			} else {
				throw new RuntimeException("'(' or '.' not found. -compileSubroutineCall");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// XML 문서에 상수를 기록
	private void writeIntConst(Element parent) {
		Element intConst = document.createElement("integerConstant");
		intConst.appendChild(document.createTextNode(jt.getToken().toString()));
		parent.appendChild(intConst);
	};
	
	// XML 문서에 문자열을 기록
	private void writeStringCosnt(Element parent) {
		Element stringConst = document.createElement("stringConstant");

		String curToken = jt.getToken();
		if (curToken.contains("<")) {
			curToken.replaceAll("<","&lt");
		} else if (curToken.contains(">")) {
			curToken.replaceAll(">","&gt");
		} else if (curToken.contains("\"")) {
			curToken.replaceAll("\"","&qout");
		} else if (curToken.contains("&")) {
			curToken.replaceAll("&","&amp");
		}
		
		stringConst.appendChild(document.createTextNode(curToken));
		parent.appendChild(stringConst);
	}
}
