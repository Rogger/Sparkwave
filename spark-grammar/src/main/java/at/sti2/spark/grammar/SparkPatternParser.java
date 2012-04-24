package at.sti2.spark.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;

import at.sti2.spark.core.prefix.Prefix;


public class SparkPatternParser {
	
	protected static Logger logger = Logger.getLogger(SparkParser.class);
	
	private String patternFilePath = null;
	
	public SparkPatternParser(String patternFilePath) {
		this.patternFilePath = patternFilePath;
	}
	
	public TriplePatternGraph parse() throws IOException{
		
		TriplePatternGraph triplePatternGraph = new TriplePatternGraph();
		
		// open file
		CharStream input = new ANTLRFileStream(patternFilePath);
		
		// Lexer
		SparkLexer lexer = new SparkLexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		
		// Parser
		SparkParser parser = new SparkParser(tokenStream);
		SparkParser.query_return query = null;
		try {
			query = parser.query();
			logger.debug( ((Tree)query.tree).toStringTree());
		} catch (RecognitionException e) {
			logger.error(e);
		}
		
		CommonTreeNodeStream nodes = new CommonTreeNodeStream((Tree)query.tree);
		nodes.setTokenStream(tokenStream);
		
		
		parseQuery( (CommonTree)nodes.nextElement(), triplePatternGraph);
		
		
		return triplePatternGraph;
	}
	
	/**
	 * Parse Query
	 */
	private void parseQuery(CommonTree queryNode,TriplePatternGraph patternGraph){
		
		if(queryNode!=null){
			int size = queryNode.getChildCount();
			
			for(int i = 0 ; i < size ; i++) {
				Tree child = queryNode.getChild(i);
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("PROLOGUE")){
    				parsePrologue(child,patternGraph);
    			}
				else if(childToken.equals("SELECT")){
    				parseSelect(child,patternGraph);
    			}
			}
		}
	}
	
	/**
	 * Parse Prologue
	 */
	private void parsePrologue(Tree prologueNode, TriplePatternGraph patternGraph){
		
		if(prologueNode!=null){
			int size = prologueNode.getChildCount();

			for(int i = 0 ; i < size ; i++) {
				Tree child = prologueNode.getChild(i);
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("PREFIX")){
					List<Prefix> prefixes = parsePrefix(child);
					patternGraph.setPrefixes(prefixes);
				}
			}
		}
	}
	
	/**
	 * Parse prefix
	 */
	private List<Prefix> parsePrefix(Tree prefixNode){
		
		List<Prefix> prefixes = new ArrayList<Prefix>();
		int size = prefixNode.getChildCount();

		for(int i = 0 ; i < size ; i++) {
			Tree child = prefixNode.getChild(i);
			String childToken = child.toString();

			String prefixLabel = childToken;
			prefixLabel = prefixLabel.replaceAll(":", "");
			String namespace = parseIRI(child.getChild(0).toString());

			logger.debug(prefixLabel);
			logger.debug(namespace);
			
			Prefix prefix = new Prefix();
			prefix.setLabel(prefixLabel);
			prefix.setNamespace(namespace);
			prefixes.add(prefix);
			
		}
		
		return prefixes;
	}
	
	/**
	 * Parse select
	 */
	private void parseSelect(Tree treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			int size = treeNode.getChildCount();

			for(int i = 0 ; i < size ; i++) {
				Tree child = treeNode.getChild(i);
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("SELECT_CLAUSE")){
					parseSelectClause(child);
				}
				else if(childToken.equals("WHERE_CLAUSE")){
					parseWhereClause(child);
				}
			}
		}
	}
	
	private void parseSelectClause(Tree treeNode){
		//TODO
	}
	
	private void parseWhereClause(Tree treeNode){
		
	}
	
	//TODO
	private void parseTriple(Tree treeNode) {
		if (treeNode != null) {
			int size = treeNode.getChildCount();
			if (size == 3) {
				Tree childSubject = treeNode.getChild(0);
				Tree childPredicate = treeNode.getChild(1);
				Tree childObject = treeNode.getChild(2);

			}
		}
	}
	
	private String parseIRI(String iri){
		return iri.replaceAll("<|>","");
	}
}
