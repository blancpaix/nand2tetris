package compiler_code;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JackTokenizer {
	// 잭 언어에서는 2중 symbol을 사용하지 않습니다
	String RGX_SYMBOL = "(\".*?\")|(\\w+)|([{}()\\[\\].,;+\\-*/&|<>=~])";	// 각 토큰 구분
	String REX_IDENTIFIER = "^[a-zA-Z_][a-zA-Z0-9_]*$";	// 문자 또는 _ 로 시작하는 식별자 구분
	
	private BufferedReader br;
	private String curLine;
	private String curToken;
	private Queue<String> tokenList;	// 각 줄을 읽어 토큰들을 큐에 넣어서 구문 분석을 수행
	private boolean inBlockedComment;	// 블록 주석 처리 여부확인
	private int lineNumber;	// Console에서 처리한 줄 수 확인
	
	private static final Map<String, TokenType> TOKEN_TYPES = new HashMap<>();
	private static final Map<String, KeywordType> KEYWORD_TYPES = new HashMap<>();
	
	static {
		// Token Type : Keyword 정의
		TOKEN_TYPES.put("class", TokenType.KEYWORD);
		TOKEN_TYPES.put("constructor", TokenType.KEYWORD);
		TOKEN_TYPES.put("function", TokenType.KEYWORD);
		TOKEN_TYPES.put("method", TokenType.KEYWORD);
		TOKEN_TYPES.put("field", TokenType.KEYWORD);
		TOKEN_TYPES.put("static", TokenType.KEYWORD);
		TOKEN_TYPES.put("var", TokenType.KEYWORD);
		TOKEN_TYPES.put("int", TokenType.KEYWORD);
		TOKEN_TYPES.put("char", TokenType.KEYWORD);
		TOKEN_TYPES.put("boolean", TokenType.KEYWORD);
		TOKEN_TYPES.put("void", TokenType.KEYWORD);
		TOKEN_TYPES.put("true", TokenType.KEYWORD);
		TOKEN_TYPES.put("false", TokenType.KEYWORD);
		TOKEN_TYPES.put("null", TokenType.KEYWORD);
		TOKEN_TYPES.put("this", TokenType.KEYWORD);
		TOKEN_TYPES.put("let", TokenType.KEYWORD);
		TOKEN_TYPES.put("do", TokenType.KEYWORD);
		TOKEN_TYPES.put("if", TokenType.KEYWORD);
		TOKEN_TYPES.put("else", TokenType.KEYWORD);
		TOKEN_TYPES.put("while", TokenType.KEYWORD);
		TOKEN_TYPES.put("return", TokenType.KEYWORD);
		
		// Token Type : Symbol 정의
		TOKEN_TYPES.put("{", TokenType.SYMBOL);
		TOKEN_TYPES.put("}", TokenType.SYMBOL);
		TOKEN_TYPES.put("(", TokenType.SYMBOL);
		TOKEN_TYPES.put(")", TokenType.SYMBOL);
		TOKEN_TYPES.put("[", TokenType.SYMBOL);
		TOKEN_TYPES.put("]", TokenType.SYMBOL);
		TOKEN_TYPES.put(".", TokenType.SYMBOL);
		TOKEN_TYPES.put(",", TokenType.SYMBOL);
		TOKEN_TYPES.put(";", TokenType.SYMBOL);
		TOKEN_TYPES.put("+", TokenType.SYMBOL);
		TOKEN_TYPES.put("-", TokenType.SYMBOL);
		TOKEN_TYPES.put("*", TokenType.SYMBOL);
		TOKEN_TYPES.put("/", TokenType.SYMBOL);
		TOKEN_TYPES.put("&", TokenType.SYMBOL);
		TOKEN_TYPES.put("|", TokenType.SYMBOL);
		TOKEN_TYPES.put("<", TokenType.SYMBOL);
		TOKEN_TYPES.put(">", TokenType.SYMBOL);
		TOKEN_TYPES.put("=", TokenType.SYMBOL);
		TOKEN_TYPES.put("~", TokenType.SYMBOL);
		
		// Keyword Type 정의
		KEYWORD_TYPES.put("class", KeywordType.CLASS);
		KEYWORD_TYPES.put("constructor", KeywordType.CONSTRUCTOR);
		KEYWORD_TYPES.put("function", KeywordType.FUNCITON);
		KEYWORD_TYPES.put("method", KeywordType.METHOD);
		KEYWORD_TYPES.put("field", KeywordType.FIELD);
		KEYWORD_TYPES.put("static", KeywordType.STATIC);
		KEYWORD_TYPES.put("var", KeywordType.VAR);
		KEYWORD_TYPES.put("int", KeywordType.INT);
		KEYWORD_TYPES.put("char", KeywordType.CHAR);
		KEYWORD_TYPES.put("boolean", KeywordType.BOOLEAN);
		KEYWORD_TYPES.put("void", KeywordType.VOID);
		KEYWORD_TYPES.put("true", KeywordType.TRUE);
		KEYWORD_TYPES.put("false", KeywordType.FALSE);
		KEYWORD_TYPES.put("null", KeywordType.NULL);
		KEYWORD_TYPES.put("this", KeywordType.THIS);
		KEYWORD_TYPES.put("let", KeywordType.LET);
		KEYWORD_TYPES.put("do", KeywordType.DO);
		KEYWORD_TYPES.put("if", KeywordType.IF);
		KEYWORD_TYPES.put("else", KeywordType.ELSE);
		KEYWORD_TYPES.put("while", KeywordType.WHILE);
		KEYWORD_TYPES.put("return", KeywordType.RETURN);
	};
	
	// 구문 분석을 위해 input 파일 확인 및 초기화
	public JackTokenizer(String inputFile) throws FileNotFoundException {
		if (inputFile == null || inputFile == "") {
			throw new NullPointerException("File not exists.");
		}
		br = new BufferedReader(new FileReader(inputFile));
		curLine = null;
		curToken = null;
		tokenList = new LinkedList<String>();
		inBlockedComment = false;
		lineNumber = 0;
	};
	
	// 다음 줄이 있을 경우 큐에 처리된 토큰 삽입
	public boolean hasMoreLine() throws IOException {
		curLine = br.readLine();
		
		while (curLine != null) {
			// 주석 제거
			int commentLocation = curLine.indexOf("//");
			if (commentLocation > 0) {
				curLine = curLine.substring(commentLocation);	
			}
			
			if (inBlockedComment) {
				if (curLine.endsWith("*/")) {
					inBlockedComment = false;
					break;
				};
			} else if ( curLine.startsWith("/**") && curLine.endsWith("*/")) {
				inBlockedComment = false;
				curLine = br.readLine();
			} else if (curLine.startsWith("/*") || curLine.startsWith("/**")) {
				inBlockedComment = true;
				curLine = br.readLine();
				continue;
			} else if (curLine.startsWith("//") || curLine.trim().equals("")) {
				curLine = br.readLine();
				continue;
			} else {
				break;
			}
		};
		
		System.out.println("[" + lineNumber++ + "] " + curLine);
		if (curLine != null) {
			// regex와 일치하는 토큰들만 큐에 삽입
			Pattern pattern = Pattern.compile(RGX_SYMBOL);
			Matcher matcher = pattern.matcher(curLine);
			while (matcher.find()) {
				tokenList.add(matcher.group().trim());
			};
			return true;
		} else {
			return false;
		}
	}
	
	// 빈 큐 확인
	public boolean hasMoreTokens() throws IOException {
		if (!tokenList.isEmpty()) {
			return true;
		};
		
		// 저장된 토큰이 없으면 불러오기
		if (hasMoreLine()) {
			return true;
		}
		
		return false;
	};
	
	// 다음 토큰으로 진행
	public void advance() throws IOException {
		curToken = tokenList.poll();
		if (tokenList.isEmpty()) {
			hasMoreLine();
		};
	};
	
	// 토큰의 타입 반환 TokenType.KEYWORD | IDENTIFIER | INT_CONST | STRING_CONST
	// 1순위- Keyword, 2순위 IDENTIFIER, 
	public TokenType tokenType() {
		if (curToken == null || curToken == "") {
			return null;
		};

		TokenType type = TOKEN_TYPES.get(curToken);
		
		if (Pattern.matches(REX_IDENTIFIER,  curToken)) {
			return TokenType.IDENTIFIER;
		} else if (isInt(curToken)) {
			return TokenType.INT_CONST;
		} else if (isString(curToken)) {
			return TokenType.STRING_CONST;
		}
		
		return type;
	}
	
	// 0 이상의 정수 여부 판단
	private boolean isInt(String token) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(token);
		return matcher.matches();
	};
	
	// 문자열 여부 판단
	private boolean isString(String token) {
		return token.startsWith("\"") && token.endsWith("\"");
	};
	
	// 현재 토큰의 Keyword Type 을 반환 - Keyword Type 일때만 호출
	public KeywordType keyword() {
		return KEYWORD_TYPES.get(curToken);
	};
	
	// 현재 토큰의 symbol 반환
	public char symbol() {
		return curToken.charAt(0);
	};
	
	// 현재 토큰의 identifier 반환
	public String identifier () {
		return curToken;
	};
	
	// 현재 토큰의 int 값 반환
	public int intVal() {
		return Integer.parseInt(curToken);
	};
	
	// 현재 토큰의 String 값 반환
	public String stringVal() {
		return curToken.substring(1, curToken.length() - 1);
	};
	
	// 현재 토큰 값만 반환
	public String getToken() {
		return this.curToken.trim();
	};
	
	// 연산자 여부 판단
	public boolean isOp() {
		switch (curToken) {
		case "+":
		case "-":
		case "*":
		case "/":
		case "&":
		case "|":
		case "<":
		case ">":
		case "=":
			return true;
		default :
			return false;
		}
	};
	
	// 현재 토큰으로 판단을 하지 않는 경우 overload
	public boolean isOp(String op) {
		switch (op) {
		case "+":
		case "-":
		case "*":
		case "/":
		case "&":
		case "|":
		case "<":
		case ">":
		case "=":
			return true;
		default :
			return false;
		}
	};
	
	// 현재 토큰을 pop 하지 않고 값만 확인
	public String peekNextToken() {
		return tokenList.peek();
	}
	
}
