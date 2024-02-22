package esercitazione5;

import esercitazione5.Lexer;
import esercitazione5.parser;
import java_cup.runtime.Symbol;
import nodes.ProgramNode;
import org.w3c.dom.Document;
import visitor.XMLVisitor;

import java.io.FileReader;
import java.io.IOException;

public class Tester {

	public static void main(String[] args) throws IOException {

		String filePath = args[0];
		FileReader fileReader = new FileReader(filePath);
		Lexer lexicalAnalyzer = new Lexer(fileReader);
		parser parser = new parser(lexicalAnalyzer);
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

		try {
			ProgramNode programNode = (ProgramNode) parser.parse().value;
			XMLVisitor xml = new XMLVisitor();
			Document document = (Document) programNode.accept(xml);
			xml.saveDocument(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

