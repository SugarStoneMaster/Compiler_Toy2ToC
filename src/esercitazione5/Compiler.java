package esercitazione5;

import nodes.ProgramNode;
import org.w3c.dom.Document;
import visitor.*;

import java.io.*;

public class Compiler {



	public static void main(String[] args) throws IOException {
		String filePath = args[0];
		FileReader fileReader = new FileReader(filePath);
		Lexer lexicalAnalyzer = new Lexer(fileReader);
		parser parser = new parser(lexicalAnalyzer);
		try {
			ProgramNode programNode = (ProgramNode) parser.parse().value;

			XMLVisitor xml = new XMLVisitor();
			Document document = (Document) programNode.accept(xml);
			xml.saveDocument(document);

			ScopingVisitor scoping = new ScopingVisitor();
			Environment top = (Environment) programNode.accept(scoping);

			SemanticAnalysisVisitor semantic = new SemanticAnalysisVisitor(top);
			programNode.accept(semantic);

			CodeGeneratorVisitor codeVisitor = new CodeGeneratorVisitor(top);
			programNode.accept(codeVisitor);
			String code = codeVisitor.saveCFile(filePath);

			runProgramInC(filePath);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void runProgramInC(String filePath) {
		String[] filePathSplit = filePath.split("/");
		String fileNameWithExt = filePathSplit[filePathSplit.length - 1];
		String fileName = fileNameWithExt.split("\\.")[0];

		String os = System.getProperty("os.name").toLowerCase();

		try {
			Process p = Runtime.getRuntime().exec("gcc -v");
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
		} catch (Exception e) {
			System.out.println("GCC is not installed on this system.");
			e.printStackTrace();
		}

		if (os.contains("win"))
		{

			try {
				Runtime rt = Runtime.getRuntime();
				Process processGCC = rt.exec("gcc test_files" + File.separator + "c_out" + File.separator + fileName + ".c" + " -o ." + File.separator + "test_files" + File.separator+ "c_out" + File.separator + fileName + ".exe");
				processGCC.waitFor();
				Process processEXE = Runtime.getRuntime().exec("cmd /k start cmd.exe @cmd /k " + "test_files" + File.separator+ "c_out" + File.separator + fileName + ".exe");
				processEXE.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (os.contains("mac"))
		{
			try {
				String gccCommand = "gcc test_files" + File.separator + "c_out" + File.separator + fileName + ".c" + " -o ." + File.separator + "test_files" + File.separator+ "c_out" + File.separator + fileName;
				Process p = Runtime.getRuntime().exec(gccCommand);
				p.waitFor();

				String terminalCommand = "open -a Terminal ";
				Runtime.getRuntime().exec(terminalCommand + "test_files" + File.separator + "c_out" + File.separator + fileName);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

/*if (lexicalAnalyzer != null)
		{
			Symbol token;
			try {
				while ((token = lexicalAnalyzer.next_token()) != null && token.sym != sym.EOF) {
					// Use token.sym to index into terminalNames to get the token name
					String tokenName = sym.terminalNames[token.sym];
					System.out.println("<" + token.sym + ", " + tokenName + ", " + (token.value != null ? token.value : " ") + ">");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
			System.out.println("File not found!!");*/

