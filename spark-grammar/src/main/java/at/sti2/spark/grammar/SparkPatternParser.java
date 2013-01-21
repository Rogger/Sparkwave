package at.sti2.spark.grammar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFNumericLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.RDFVariable;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.core.triple.TripleConstantTest;
import at.sti2.spark.grammar.pattern.GraphPattern;
import at.sti2.spark.grammar.pattern.GroupGraphPattern;
import at.sti2.spark.grammar.pattern.Handler;
import at.sti2.spark.grammar.pattern.LogicAndGraphPattern;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.grammar.pattern.Prefix;
import at.sti2.spark.grammar.pattern.TemporalBeforeGraphPattern;
import at.sti2.spark.grammar.pattern.expression.FilterExpression;
import at.sti2.spark.grammar.pattern.expression.FilterOperator;
import at.sti2.spark.grammar.util.Entry;

/**
 * Sparkwave pattern parser supports: prefixes, handlers, select, where, filter and timewindow.
 * @author michaelrogger
 *
 */
public class SparkPatternParser {
	
	protected static Logger logger = Logger.getLogger(SparkParser.class);
	
	private String patternFilePath = null;
	
	public SparkPatternParser(String patternFilePath) {
		this.patternFilePath = patternFilePath;
	}
	
	/**
	 * Parse the pattern
	 * @return Pattern
	 * @throws IOException I/O problems with the pattern file
	 */
	public Pattern parse() throws IOException{
		
		Pattern triplePatternGraph = new Pattern();
		
		// open file
		CharStream input = new ANTLRFileStream(patternFilePath);
		
		//Lexer Error Reporter
		IErrorReporter lexerErrorReporter = new IErrorReporter() {
			
			protected Logger logger = Logger.getLogger(getClass());
			
			@Override
			public void reportError(String[] tokenNames, RecognitionException e, String hdr, String msg) {
				if(e instanceof NoViableAltException){
					// ignore
				}else{
					logger.warn(e);
				}
			}
		};
		
		// Lexer
		SparkLexer lexer = new SparkLexer(input);
		lexer.setErrorReporter(lexerErrorReporter);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		
		//Parser Error Reporter
		IErrorReporter parserErrorReporter = new IErrorReporter() {
			
			protected Logger logger = Logger.getLogger(getClass());
			
			@Override
			public void reportError(String[] tokenNames, RecognitionException e, String hdr, String msg) {
				logger.warn(hdr+"\t"+msg);
			}
		};
		
		// Parser
		SparkParser parser = new SparkParser(tokenStream);
		parser.setErrorReporter(parserErrorReporter);
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
	private void parseQuery(TreeWrapper treeNode,Pattern patternGraph){
		
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
	private void parsePrologue(TreeWrapper treeNode, Pattern patternGraph){
		
		if(treeNode!=null){
			
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("PREFIX")){
					List<Prefix> prefixes = parsePrefix(child);
					patternGraph.setPrefixes(prefixes);
				}else if(childToken.equals("HANDLERS")){
    				parseHandlers(child,patternGraph);
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
	 * HANDLERS
	 */
	private void parseHandlers(TreeWrapper treeNode, Pattern patternGraph){
		
		if(treeNode!=null){
			for(TreeWrapper child : treeNode){
				String childToken = child.toString();
				logger.debug(childToken);
				if(childToken.equals("HANDLER")){
					Handler invokerProperties = parseHandler(child,patternGraph);
					patternGraph.addHandlerProperties(invokerProperties);					
				}
			}
		}
	}
	
	/**
	 * HANDLE
	 */
	private Handler parseHandler(TreeWrapper treeNode, Pattern patternGraph){
		
		Handler handlerProperties = null;
		
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("HANDLER_GROUP")){
					handlerProperties = parseHandlerGroup(child, patternGraph);
				}
			}
		}
		
		return handlerProperties;
	}
	
	private Handler parseHandlerGroup(TreeWrapper treeNode, Pattern patternGraph){
		Handler handlerProperties = new Handler(patternGraph);
		
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("KEYVALUE_PAIR")){
					Entry<String,String> keyValuePair = parseKeyValuePair(child);
					
					if(keyValuePair.getKey().equalsIgnoreCase("class")){
						handlerProperties.setHandlerClass(keyValuePair.getValue());
					}else{
						handlerProperties.addKeyValue(keyValuePair.getKey(),keyValuePair.getValue());
					}
				}
			}
		}
		
		return handlerProperties;
	}
	
	/**
	 * Parse KEYVALUE_PAIR
	 * @param treeNode
	 * @return 
	 */
	private Entry<String, String> parseKeyValuePair(TreeWrapper treeNode){
		
		Entry<String,String> entry = null;
		
		if(treeNode!=null){
			TreeWrapper key = treeNode.getChild(0).getChild(0);
			String strKey = key.toString().replaceAll("\"", "");
			TreeWrapper value = treeNode.getChild(1).getChild(0);
			String strValue = value.toString().replaceAll("\"", "");
			entry = new Entry<String, String>(strKey, strValue);
		}
		
		return entry;
	}
	
	/**
	 * Parse SELECT
	 */
	private void parseSelect(TreeWrapper treeNode, Pattern patternGraph){
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("SELECT_CLAUSE")){
					parseSelectClause(child,patternGraph);
				}
				else if(childToken.equals("WHERE_CLAUSE")){
					GraphPattern whereClause = parseWhereClause(child,patternGraph);
					patternGraph.setWhereClause(whereClause);
				}
			}
		}
	}
	
	private void parseSelectClause(TreeWrapper treeNode, Pattern patternGraph){
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
	private void parseConstruct(TreeWrapper treeNode, Pattern patternGraph){
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("CONSTRUCT_TRIPLES")){
					parseConstructTriples(child,patternGraph);
				}
				else if(childToken.equals("WHERE_CLAUSE")){
					GraphPattern whereClause = parseWhereClause(child,patternGraph);
					patternGraph.setWhereClause(whereClause);
				}
			}
		}
	}
	
	/**
	 * Parse CONSTRUCT_TRIPLES
	 * @param treeNode
	 */
	private void parseConstructTriples(TreeWrapper treeNode, Pattern patternGraph){
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
	private GraphPattern parseWhereClause(TreeWrapper treeNode, Pattern patternGraph){
		if(treeNode!=null){
			for(TreeWrapper child : treeNode) {
				return parseGraphPattern(child, patternGraph);
			}
		}
		
		return null;
	}
	
	/**
	 * Parse WHERE_CLAUSE continued
	 * @param treeNode
	 */
	private GraphPattern parseGraphPattern(TreeWrapper treeNode, Pattern patternGraph){
		if(treeNode!=null){
			String childToken = treeNode.toString();
			logger.debug(childToken);
				
			if(childToken.equals("GROUP_GRAPH_PATTERN")){
				GroupGraphPattern parseGroupGraphPattern = parseGroupGraphPattern(treeNode,patternGraph);
				return parseGroupGraphPattern;
			}else if(childToken.equals("AND_GRAPH")){
				LogicAndGraphPattern parseAndGraphPattern = parseAndGraphPattern(treeNode, patternGraph);
				return parseAndGraphPattern;
			}else if(childToken.equals("BEFORE_GRAPH")){
				TemporalBeforeGraphPattern parseBeforeGraphPattern = parseBeforeGraphPattern(treeNode, patternGraph);
				return parseBeforeGraphPattern;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Parse GROUP_GRAPH_PATTERN
	 * @param treeNode
	 */
	private GroupGraphPattern parseGroupGraphPattern(TreeWrapper treeNode, Pattern patternGraph){
		GroupGraphPattern groupGraphPattern = new GroupGraphPattern();

		if(treeNode!=null){
		
			for(TreeWrapper child : treeNode) {
				String childToken = child.toString();
				logger.debug(childToken);
				
				if(childToken.equals("TRIPLE")){
					TripleCondition triple = parseTriple(child,patternGraph);
					groupGraphPattern.addWhereCondition(triple);
				}else if(childToken.equals("TIMEWINDOW")){
					int timewindow = parseTimewindow(child,patternGraph);
					groupGraphPattern.setTimeWindowLength(timewindow);
				}else if(childToken.equals("FILTER")){
					List<FilterExpression> expressions = parseFilter(child,patternGraph);
					groupGraphPattern.addFilters(expressions);
				}
			}
		}
		
		return groupGraphPattern;
	}
	
	/**
	 * Parse AND_GRAPH
	 * 
	 */
	private LogicAndGraphPattern parseAndGraphPattern(TreeWrapper treeNode, Pattern patternGraph){

		LogicAndGraphPattern logicAndGraphPattern = new LogicAndGraphPattern();
		
		if(treeNode!=null && treeNode.getSize()==2){
		
			TreeWrapper child1 = treeNode.getChild(0);
			String childToken1= child1.toString();
			logger.debug(childToken1);
			GraphPattern groupPattern1 = parseGraphPattern(child1, patternGraph);
			
			TreeWrapper child2 = treeNode.getChild(1);
			String childToken2 = child2.toString();
			logger.debug(childToken2);
			GraphPattern groupPattern2 = parseGraphPattern(child2, patternGraph);
			
			logicAndGraphPattern.setLeft(groupPattern1);
			logicAndGraphPattern.setRight(groupPattern2);
			
		}
		
		return logicAndGraphPattern;
		
	}
	
	/**
	 * Parse BEFORE_GRAPH
	 * 
	 */
	private TemporalBeforeGraphPattern parseBeforeGraphPattern(TreeWrapper treeNode, Pattern patternGraph){

		TemporalBeforeGraphPattern temporalBeforeGraphPattern = new TemporalBeforeGraphPattern();
		
		if(treeNode!=null && treeNode.getSize()==3){
		
			TreeWrapper child1 = treeNode.getChild(0);
			String childToken1= child1.toString();
			logger.debug(childToken1);
			GraphPattern groupPattern1 = parseGraphPattern(child1, patternGraph);
			
			TreeWrapper child2 = treeNode.getChild(1);
			String childToken2= child2.toString();
			logger.debug(childToken2);
			
			//Parse <TEMPORAL OP>(int,int)
			int[] logicBracketedExpression = parseLogicBracketedExpression(child2, patternGraph);
			
			TreeWrapper child3 = treeNode.getChild(2);
			String childToken3 = child3.toString();
			logger.debug(childToken3);
			GraphPattern groupPattern3 = parseGraphPattern(child3, patternGraph);
			
			temporalBeforeGraphPattern.setLeft(groupPattern1);
			temporalBeforeGraphPattern.setRight(groupPattern3);
			temporalBeforeGraphPattern.setLowerBound(logicBracketedExpression[0]);
			temporalBeforeGraphPattern.setUpperBound(logicBracketedExpression[1]);
			
		}
		
		return temporalBeforeGraphPattern;
		
	}
	
	/**
	 * Parse the bracketed expression for temporal operators
	 * @param treeNode
	 * @param patternGraph
	 * @return
	 */
	private int[] parseLogicBracketedExpression(TreeWrapper treeNode, Pattern patternGraph){
		
		int[] boundaries = new int[2];
		
		if(treeNode!=null && treeNode.getSize()==2){
			
			//NUMERIC_LITERAL -> INTEGER_LITERAL -> INT VALUE
			TreeWrapper lowerBound = treeNode.getChild(0).getChild(0).getChild(0);
			String lowerBoundStr = lowerBound.toString();
			logger.debug(lowerBoundStr);
			boundaries[0] = Integer.parseInt(lowerBoundStr);
			
			//NUMERIC_LITERAL -> INTEGER_LITERAL -> INT VALUE
			TreeWrapper upperBound = treeNode.getChild(1).getChild(0).getChild(0);
			String upperBoundStr = upperBound.toString();
			logger.debug(upperBoundStr);
			boundaries[1] = Integer.parseInt(upperBoundStr);
			
		}
		
		return boundaries;
		
	}
	
	/**
	 * Parse TIMEWINDOW
	 * @param treeNode
	 */
	private int parseTimewindow(TreeWrapper treeNode, Pattern patternGraph){
		
		int timewindow = 0;
		if(treeNode!=null){
			TreeWrapper timeWindowValue = treeNode.getChild(0);
			
			if(timeWindowValue!=null){
				timewindow = Integer.parseInt(timeWindowValue.toString());				
			}
		}
		
		logger.debug(timewindow);
		
		return timewindow;
	}
	
	/**
	 * Parse FILTER
	 */
	private List<FilterExpression> parseFilter(TreeWrapper treeNode, Pattern patternGraph){
		
		ArrayList<FilterExpression> expressions = new ArrayList<FilterExpression>();
		
		if(treeNode!=null){
			TreeWrapper bracketted = treeNode.getChild(0);
			logger.debug(bracketted);
			
			if(bracketted!=null && bracketted.toString().equals("BRACKETTED_EXPRESSION")){
				FilterExpression parseExpression = parseBrackettedExpression(bracketted,patternGraph);
				if(parseExpression!=null)
					expressions.add(parseExpression);
			}
		}
		
		return expressions;
	}
	
	/**
	 * Parse BRACKETTED_EXPRESSION
	 * @param treeNode
	 * @return
	 */
	private FilterExpression parseBrackettedExpression(TreeWrapper treeNode, Pattern patternGraph) {
		FilterExpression expression = null;
		if (treeNode != null) {
			TreeWrapper operation = treeNode.getChild(0);
			
			// operation
			if(operation!=null){
				logger.info(operation);
				String operatorValue = operation.toString();
				
				FilterOperator operator = null;
				
				// equal
				if (operatorValue.equals("=")) {
					operator = FilterOperator.EQUAL;
				}
				// not equal
				else if(operatorValue.equals("!=")){
					operator = FilterOperator.NOT_EQUAL;
				}
				// greater
				else if(operatorValue.equals(">")){
					operator = FilterOperator.GREATER;
				}
				// greater equal
				else if(operatorValue.equals(">=")){
					operator = FilterOperator.GREATER_EQUAL;
				}
				// less
				else if(operatorValue.equals("<")){
					operator = FilterOperator.LESS;
				}
				// less equal
				else if(operatorValue.equals("<=")){
					operator = FilterOperator.LESS_EQUAL;
				}
				
				// left
				RDFValue leftRDFValue = parseRelationalExpression(operation.getChild(0));
				
				//right
				RDFValue rightRDFValue = parseRelationalExpression(operation.getChild(1));
				
				expression = new FilterExpression(leftRDFValue, rightRDFValue, operator);
			}
		}
		return expression;
	}
	
	private RDFValue parseRelationalExpression(TreeWrapper treeNode) {
		// expression
		if (treeNode != null) {
			logger.info(treeNode);
			String expressionValue = treeNode.toString();

			if (expressionValue.equals("VAR")) {
				String varName = treeNode.getChild(0).toString();
				varName = varName.replaceAll("\\?", "");
				RDFVariable expressionVariable = new RDFVariable(varName);
				return expressionVariable;
				
			} else if (expressionValue.equals("NUMERIC_LITERAL")) {
				TreeWrapper child = treeNode.getChild(0);
				RDFNumericLiteral numericLiteral = parseNumericLiteral(child);
				return numericLiteral;
			}
		}
		return null;
	}
	
	private RDFNumericLiteral parseNumericLiteral(TreeWrapper treeNode){
		if(treeNode != null){
			logger.info(treeNode);
			String literalType = treeNode.toString();
			if(literalType.equals("DECIMAL_LITERAL")){
				String numericValue = treeNode.getChild(0).toString();
				RDFNumericLiteral expressionNumericValue = RDFLiteral.Factory.createNumericLiteral(Double.parseDouble(numericValue));
				return expressionNumericValue;
			}else if(literalType.equals("INTEGER_LITERAL")){
				String numericValue = treeNode.getChild(0).toString();
				RDFNumericLiteral expressionNumericValue = RDFLiteral.Factory.createNumericLiteral(Integer.parseInt(numericValue));
				return expressionNumericValue;
			}
		}
		return null;
	}
	
	/**
	 * Parse TRIPLE
	 * @param treeNode
	 */
	private TripleCondition parseTriple(TreeWrapper treeNode, Pattern patternGraph) {
		
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
	private RDFValue parseRDFValue(TreeWrapper treeNode, Pattern patternGraph){
		
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
				rdfValue = RDFLiteral.Factory.createLiteral(value, rdfuriReference, null);
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
