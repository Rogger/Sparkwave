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

import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.core.condition.TripleConstantTest;
import at.sti2.spark.core.condition.TriplePatternGraph;
import at.sti2.spark.core.prefix.Prefix;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.variable.RDFVariable;


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
					parseSelectClause(child,patternGraph);
				}
				else if(childToken.equals("WHERE_CLAUSE")){
					parseWhereClause(child,patternGraph);
				}
			}
		}
	}
	
	private void parseSelectClause(Tree treeNode, TriplePatternGraph patternGraph){
		//TODO
	}
	
	/**
	 * Parse WHERE_CLAUSE
	 * @param treeNode
	 */
	private void parseWhereClause(Tree treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			int size = treeNode.getChildCount();

			for(int i = 0 ; i < size ; i++) {
				Tree child = treeNode.getChild(i);
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("GROUP_GRAPH_PATTERN")){
					parseGroupGraphPattern(child,patternGraph);
				}
			}
		}
	}
	
	/**
	 * Parse GROUP_GRAPH_PATTERN
	 * @param treeNode
	 */
	private void parseGroupGraphPattern(Tree treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			int size = treeNode.getChildCount();

			for(int i = 0 ; i < size ; i++) {
				Tree child = treeNode.getChild(i);
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("TRIPLE")){
					TripleCondition triple = parseTriple(child,patternGraph);
					patternGraph.addSelectTripleCondition(triple);
				}
			}
		}
	}
	
	/**
	 * Parse TRIPLE
	 * @param treeNode
	 */
	private TripleCondition parseTriple(Tree treeNode, TriplePatternGraph patternGraph) {
		
		TripleCondition tripleCondition = new TripleCondition();
		RDFTriple triple = new RDFTriple();
		tripleCondition.setConditionTriple(triple);
		
		if (treeNode != null) {
			int size = treeNode.getChildCount();
			if (size == 3) {
				
				// subject
				Tree childSubject = treeNode.getChild(0);
				Tree childSubjectType = childSubject.getChild(0);
				RDFValue subjectRDFValue = parseRDFValue(childSubjectType,patternGraph);
				triple.setSubject(subjectRDFValue);

				// subject constant test
				if( !(subjectRDFValue instanceof RDFVariable) ){
					TripleConstantTest test = new TripleConstantTest(subjectRDFValue.toString(), RDFTriple.Field.SUBJECT);
					tripleCondition.addConstantTest(test);
				}
				
				// predicate
				Tree childPredicate = treeNode.getChild(1);
				Tree childPredicateType = childPredicate.getChild(0);
				RDFValue predicateRDFValue = parseRDFValue(childPredicateType,patternGraph);
				
				// predicate constant test
				if(!(predicateRDFValue instanceof RDFVariable)){
					TripleConstantTest test = new TripleConstantTest(predicateRDFValue.toString(), RDFTriple.Field.PREDICATE);
					tripleCondition.addConstantTest(test);
				}
				
				// object
				Tree childObject = treeNode.getChild(2);
				Tree childObjectType = childObject.getChild(0);
				RDFValue objectRDFValue = parseRDFValue(childObjectType,patternGraph);
				
				// object constant test
				if(!(objectRDFValue instanceof RDFVariable)){
					TripleConstantTest test = new TripleConstantTest(objectRDFValue.toString(), RDFTriple.Field.OBJECT);
					tripleCondition.addConstantTest(test);
				}

			}
		}
		
		return tripleCondition;
	}

	/**
	 * Parse VAR, IRI, PREFIX_NAME
	 * @param treeNode
	 * @return
	 */
	private RDFValue parseRDFValue(Tree treeNode, TriplePatternGraph patternGraph){
		
		RDFValue rdfValue = null;
		
		if( treeNode != null){
			String childToken = treeNode.toString();
			
			if(childToken.equals("VAR")){
				String varName = treeNode.getChild(0).toString();
				varName = varName.replaceAll("\\?", "");
				rdfValue = new RDFVariable(varName);
			}else if(childToken.equals("IRI")){
				//TODO
			}else if(childToken.equals("PREFIX_NAME")){
				String prefixName = treeNode.getChild(0).toString();
				String[] prefixNameSplit = prefixName.split(":");
				String namespace = patternGraph.getNamespaceByLabel(prefixNameSplit[0]);
				
				rdfValue = new RDFURIReference(namespace,prefixNameSplit[1]);
			}
			
		}
		
		return rdfValue;
	}
	
	private String parseIRI(String iri){
		return iri.replaceAll("<|>","");
	}
}
