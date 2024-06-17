Jack language compiler

[규칙]
  - 'xxx' : 홑따옴표 볼드체는 글자 그대로 토큰에 사용 (단말)
  - xxx   : 일반 글꼴은 언어 구조 이름에 사용 (비단말)
  - ()    : 괄호는 언어 구조들을 하나로 묶는 데 사용
  - x|y   : x또는 y 가 나올 수 있음
  - x?    : x가 0번 또는 1번 나탄는 경우
  - x*    : x가 0번 이상 나타나는 경우

[구문 규칙]
1. 어휘요소 : 잭 언어에는 다섯가지 단말 요소가 존재

  keyword : 'class' | 'constructor' | 'function' | 'method' |
    'field' | 'static' | 'var' | 'int' | 'char' | 'boolean' |
    'void' | 'true' | 'false' | 'null' | 'this' | 'let' | 'do' |
    'if' | 'else' | 'while' | 'return'

  symbol : '{' | '}' | '(' | ')' | '[' | ']' |
    '.' | ',' | ';' | '+' | '-' | '*' | '/' |
    '&' | '|' | '<' | '>' | '=' | '~' 

  integerConstant : 0 ~ 32767의 10진수 숫자
  
  StringConstant : '"' 따옴표와 줄 바꿈 문자를 제외한 유니코드 문자열 '"'

  identifier : 숫자로 시작하지 않는, 영문자, 숫자, 밑줄(_)로 이뤄진 문자열


2. 프로그램 구조 : 잭 프로그램은 클래스로 이뤄져 있으며, 클래스들은 각각 다른 파일에 있다.
  컴파일 단위는 클래스 하나다. 클래스는 다음 문맥 자유 구문을 따라 구조화 된, 토큰의 연속열이 된다.

  class : 'class' className '{' classVarDec* subroutineDec* '}'

  classVarDec : ('static' | 'field' ) type varName (',' varName)* ';'
  
  type : 'int' | 'char' | 'boolean' | className

  subroutineDec : ('constructor' | 'function' | 'method' )
    ('void' | type ) subroutineName '(' parameterList ')' subroutineBody

  parameterList : ( (type varName) (',' type varName)* )?

  subroutineBody : '{' varDec* statements '}'

  varDec : 'var' type varName (',' varName)* ';'

  className : identifier

  subroutineName : identifier

  varName : identifier


3. 명령문
  
  statements : statement*
  
  statement : letStatement | ifStatement | whileStatement | doStatement | returnStatement

  letStatement : 'let' varName ( '[' expression ']' )? '=' expression ';'

  ifStatement : 'if' '(' expression')' '{' statements '}'
    ('else' '{' statements '}' )?
  
  whileStatement : 'while' '(' expression ')' '{' statements '}'

  doStatement : 'do' subroutineCall ';'

  returnStatement : 'return' expression? ';'


4. 표현식
  
  expression : term (op term)*

  term : integerConstant | stringConstant | keywordConstant |
    varName | varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term

  subroutineCall : subroutineName '(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'

  expressionList : (expression (',' expression)* )?

  op : '+' | '-' | '*' | '/' | '&' | '|' | '<' | '>' | '=' | '-' | '~' 

  unaryOp : '-' | '~' 

  keywordConstant : 'true' | 'false' | 'null' | 'this' 
