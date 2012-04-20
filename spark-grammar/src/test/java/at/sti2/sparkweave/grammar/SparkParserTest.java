package at.sti2.sparkweave.grammar;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

import at.sti2.sparkweave.grammar.SparkParser.prologue_return;

public class SparkParserTest extends TestCase {
	
	public void testParser() throws Exception{
		CharStream input = new ANTLRFileStream("target/classes/pattern-PT2-TW100.tpg");
	    
		SparkLexer lexer = new SparkLexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		SparkParser parser = new SparkParser(tokenStream);
//		prefixDecl_return prefixDecl = parser.prefixDecl();
		prologue_return prologue = parser.prologue();
		
		System.out.println( ((Tree)prologue.tree).toStringTree());
		
		if(parser.getNumberOfSyntaxErrors() >0){
			return;
		}
		
		
		
	}

}
