package compiler_code;

public class Main {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("input : Java Main <INPUT_FILE>");
		};
		
		String inputFile = args[0];
		// 저장 경로 설정
		String outputFile = "C:\\Users\\user\\dev\\java\\result.xml";
		String vmFilePath = "C:\\Users\\user\\dev\\java\\resultVM.vm";
		
		try {
			CompilationEngine ce = new CompilationEngine(inputFile, vmFilePath);
			// 컴파일 시작 및 파일 출력
			ce.CompileClass(outputFile, vmFilePath);
			
		} catch (Exception e) {
			System.err.println("Error! " + e);
		} finally {
			System.out.println("Done!");
		}
	}

}
