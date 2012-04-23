package at.sti2.sparkweave.grammar;

import junit.framework.TestCase;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;

import at.sti2.sparkweave.grammar.SparkParser.prologue_return;
import at.sti2.sparkweave.grammar.SparkParser.query_return;

public class SparkLexerParserTest extends TestCase {
	
	static protected Logger logger = Logger.getLogger(SparkLexerParserTest.class);
	
	public void testLexerParser() throws Exception{
		
		// open file
		CharStream input = new ANTLRFileStream("target/classes/pattern-PT2-TW100.tpg");
		
		// Lexer
		SparkLexer lexer = new SparkLexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		
		// Parser
		SparkParser parser = new SparkParser(tokenStream);
		SparkParser.query_return query = parser.query();
		
		logger.info( ((Tree)query.tree).toStringTree());
	
		// Test: no error wile parsing
		assertTrue(parser.getNumberOfSyntaxErrors() == 0);
		
		CommonTreeNodeStream nodes = new CommonTreeNodeStream((Tree)query.tree);
		nodes.setTokenStream(tokenStream);
		
		
		
		
		logger.info("List of CommonTreeNodes");

        CommonTree n = null;
        while ((n = ((CommonTree) nodes.nextElement())) != null) {
        	logger.info(" "+n.toString());
            if ((n.toString()).compareTo("EOF") == 0) {
                break;
            }
        }
        logger.info("\n-------------------------------");
        
        
        
        nodes.reset();

        logger.info("CommonTreeNodes tree=" + ((Tree) nodes.getTreeSource()).toStringTree());
        logger.info("-------------------------------");
		
	}

}
