package assembler;

import java.io.File;

public class Program {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Plz specify source file.");
			System.exit(1);;
		};
		
		File sourceFile = new File(args[0].trim());
		if (!sourceFile.exists()) {
			System.err.println("그런 파일 없다는데?");
			System.exit(1);
		}
		
		// 파일 경로 설정
		String absPath = sourceFile.getAbsolutePath();
		String fullName = sourceFile.getName();	// file.ext
		int extIdx = fullName.lastIndexOf(".");
		
		String fileName = fullName.substring(0, extIdx);
		String extName = fullName.substring(extIdx);
		
		int dirLastPathIdx = absPath.lastIndexOf("\\");
		String dir = absPath.substring(dirLastPathIdx);
		
		String outputPath = dir + "\\" + fileName + ".hack";
		
		File outputFile = new File(outputPath);
		
		try {
			if (outputFile.exists()) {
				outputFile.delete();
			};
			outputFile.createNewFile();
			
			long startAt = System.currentTimeMillis();
			
			// Assembler를 내가 직접 작성을 해줘야함
			
			long endAt = System.currentTimeMillis();
			long duration = endAt - startAt;
			
			// ...
			
		} catch (Exception e) {
			System.out.println("I/O Error " +  e);
			System.exit(1);
		}
		
		
		
	}

}
