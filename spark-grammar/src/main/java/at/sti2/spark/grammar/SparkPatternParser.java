package at.sti2.spark.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
import at.sti2.spark.core.triple.RDFLiteral;
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
		
		Tree rootTree = (Tree)nodes.nextElement();
		parseQuery( new TreeWrapper(rootTree) , triplePatternGraph);
		
		
		return triplePatternGraph;
	}
	
	/**
	 * Parse Query
	 */
	private void parseQuery(TreeWrapper treeNode,TriplePatternGraph patternGraph){
		
		if(treeNode!=null){
			
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("PROLOGUE")){
    				parsePrologue(child,patternGraph);
    			}
				else if(childToken.equals("SELECT")){
    				parseSelect(child,patternGraph);
    			}else if(childToken.equals("CONSTRUCT")){
    				parseConstruct(child,patternGraph);
    			}
			}
		}
	}
	
	/**
	 * Parse Prologue
	 */
	private void parsePrologue(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		
		if(treeNode!=null){
			
			for(TreeWrapper child : treeNode) {
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
	private List<Prefix> parsePrefix(TreeWrapper treeNode){
		
		List<Prefix> prefixes = new ArrayList<Prefix>();
		
		for(TreeWrapper child : treeNode) {
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
	 * Parse SELECT
	 */
	private void parseSelect(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
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
	
	private void parseSelectClause(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			TreeWrapper child = treeNode.getChild(0);
			logger.debug(child.toString());
		}
	}
	
	/**
	 * Parse CONSTRUCT
	 * @param treeNode
	 * @param patternGraph
	 */
	private void parseConstruct(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("CONSTRUCT_TRIPLES")){
					parseConstructTriples(child,patternGraph);
				}
				else if(childToken.equals("WHERE_CLAUSE")){
					parseWhereClause(child,patternGraph);
				}
			}
		}
	}
	
	/**
	 * Parse CONSTRUCT_TRIPLES
	 * @param treeNode
	 */
	private void parseConstructTriples(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("TRIPLE")){
					TripleCondition triple = parseTriple(child,patternGraph);
					patternGraph.addConstructTripleCondition(triple);
				}
			}
		}
	}
	
	/**
	 * Parse WHERE_CLAUSE
	 * @param treeNode
	 */
	private void parseWhereClause(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
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
	private void parseGroupGraphPattern(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		if(treeNode!=null){

			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("TRIPLE")){
					TripleCondition triple = parseTriple(child,patternGraph);
					patternGraph.addSelectTripleCondition(triple);
				}else if(childToken.equals("TIMEWINDOW")){
					int timewindow = parseTimewindow(child);
					patternGraph.setTimeWindowLength(timewindow);
				}
			}
		}
	}
	
	
	/**
	 * Parse TIMEWINDOW
	 * @param treeNode
	 */
	private int parseTimewindow(TreeWrapper treeNode){
		
		int timewindow = 0;
		if(treeNode!=null){
			TreeWrapper timeWindowValue = treeNode.getChild(0);
			
			if(timeWindowValue!=null){
				timewindow = Integer.parseInt(timeWindowValue.toString());				
			}
		}
		
		logger.info(timewindow);
		
		return timewindow;
	}
	
	/**
	 * Parse TRIPLE
	 * @param treeNode
	 */
	private TripleCondition parseTriple(TreeWrapper treeNode, TriplePatternGraph patternGraph) {
		
		TripleCondition tripleCondition = new TripleCondition();
		RDFTriple triple = new RDFTriple();
		tripleCondition.setConditionTriple(triple);
		
		if (treeNode != null) {
			int size = treeNode.getSize();
			if (size == 3) {
				
				// subject
				TreeWrapper childSubject = treeNode.getChild(0);
				TreeWrapper childSubjectType = childSubject.getChild(0);
				RDFValue subjectRDFValue = parseRDFValue(childSubjectType,patternGraph);
				triple.setSubject(subjectRDFValue);

				// subject constant test
				if( !(subjectRDFValue instanceof RDFVariable) ){
					TripleConstantTest test = new TripleConstantTest(subjectRDFValue.toString(), RDFTriple.Field.SUBJECT);
					tripleCondition.addConstantTest(test);
				}
				
				// predicate
				TreeWrapper childPredicate = treeNode.getChild(1);
				TreeWrapper childPredicateType = childPredicate.getChild(0);
				RDFValue predicateRDFValue = parseRDFValue(childPredicateType,patternGraph);
				triple.setPredicate(predicateRDFValue);
				
				// predicate constant test
				if(!(predicateRDFValue instanceof RDFVariable)){
					TripleConstantTest test = new TripleConstantTest(predicateRDFValue.toString(), RDFTriple.Field.PREDICATE);
					tripleCondition.addConstantTest(test);
				}
				
				// object
				TreeWrapper childObject = treeNode.getChild(2);
				TreeWrapper childObjectType = childObject.getChild(0);
				RDFValue objectRDFValue = parseRDFValue(childObjectType,patternGraph);
				triple.setObject(objectRDFValue);
				
				// object constant test
				if(!(objectRDFValue instanceof RDFVariable)){
					TripleConstantTest test = new TripleConstantTest(objectRDFValue.toString(), RDFTriple.Field.OBJECT);
					tripleCondition.addConstantTest(test);
				}

			}
		}
		
		logger.info(triple);
		
		return tripleCondition;
	}

	/**
	 * Parse VAR, IRI, PREFIX_NAME
	 * @param treeNode
	 * @return
	 */
	private RDFValue parseRDFValue(TreeWrapper treeNode, TriplePatternGraph patternGraph){
		
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
				
			}else if(childToken.equals("RDFLITERAL")){
				TreeWrapper literalValue = treeNode.getChild(0);
				String value = literalValue.toString().replaceAll("\"", "");
				TreeWrapper iri = treeNode.getChild(1);
				RDFURIReference rdfuriReference = parseRDFURIReference(iri);
				//TODO language TAG
				rdfValue = new RDFLiteral(value, rdfuriReference, null);
			}
		}
			
		return rdfValue;
	}
	
	private RDFURIReference parseRDFURIReference(TreeWrapper treeNode){
		RDFURIReference uri = null;
		if(treeNode!=null){
			TreeWrapper iriValue = treeNode.getChild(0);
			uri = new RDFURIReference(parseIRI(iriValue.toString()));
		}
		
		return uri;
	}
	
	private String parseIRI(String iri){
		return iri.replaceAll("<|>","");
	}
	
}
