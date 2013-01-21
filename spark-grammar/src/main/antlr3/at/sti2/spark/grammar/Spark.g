/*
 * Copyright (c) 2010, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 * @author Michael Rogger   (michael.rogger)
 * @author Srdjan Komazec (srdjan.komazec)
 * @version $Id$
 */

grammar Spark;

options{
output = AST;
}

tokens{
PROLOGUE;
PREFIX;
QUERY;
SELECT_CLAUSE;
WHERE_CLAUSE;
VAR;
IRI;
PREFIX_NAME;
GROUP_GRAPH_PATTERN;
AND_GRAPH;
BEFORE_GRAPH;
TRIPLE;
SUBJECT;
PREDICATE;
OBJECT;
RDFLITERAL;
CONSTRUCT_TRIPLES;
HANDLERS;
HANDLER;
HANDLER_GROUP;
HANDLER_CLASS;
KEYVALUE_PAIR;
KEY;
VALUE;
FILTER;
BRACKETTED_EXPRESSION;
LOGIC_BRACKETTED_EXPRESSION;
NUMERIC_LITERAL;
DECIMAL_LITERAL;
INTEGER_LITERAL;
EPSILON_ONTOLOGY;
STATIC_INSTANCES;
}

@header {
package at.sti2.spark.grammar;

import at.sti2.spark.grammar.IErrorReporter;
}

@lexer::header {
package at.sti2.spark.grammar;

import at.sti2.spark.grammar.IErrorReporter;
}

@parser::members {
  // lexer members
  private IErrorReporter errorReporter = null;
  public void setErrorReporter(IErrorReporter errorReporter) {
  	this.errorReporter = errorReporter;
  }
 
  public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errorReporter.reportError(tokenNames, e, hdr, msg);
  }
}

@lexer::members {
  // lexer members
  private IErrorReporter errorReporter = null;
  public void setErrorReporter(IErrorReporter errorReporter) {
  	this.errorReporter = errorReporter;
  }
 
  public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        String hdr = getErrorHeader(e);
        String msg = getErrorMessage(e, tokenNames);
        errorReporter.reportError(tokenNames, e, hdr, msg);
  }
}

// $<Parser

query
    : prologue (selectQuery | constructQuery) EOF -> ^(QUERY prologue selectQuery* constructQuery*)
    ;

prologue
    : prefixes epsilonOntologyClause staticInstances handlersClause -> ^(PROLOGUE prefixes epsilonOntologyClause staticInstances handlersClause)
    ;
    
prefixes
    : (prefixDecl)* -> ^(PREFIX prefixDecl*)
    ;
    
prefixDecl
    : PREFIX PNAME_NS IRI_REF -> ^(PNAME_NS IRI_REF)
    ;
    
epsilonOntologyClause
    : EPSILON_ONTOLOGY EQUAL string ->^(EPSILON_ONTOLOGY string)
    ;
    
 staticInstances
    : STATIC_INSTANCES EQUAL string -> ^(STATIC_INSTANCES string)
    ;

handlersClause
    : HANDLERS OPEN_CURLY_BRACE handlerClause* CLOSE_CURLY_BRACE -> ^(HANDLERS handlerClause*)
    ;
   
handlerClause
    : HANDLER handlerGroup -> ^(HANDLER handlerGroup)
    ;
    
handlerGroup
    : OPEN_CURLY_BRACE keyValuePair* CLOSE_CURLY_BRACE -> ^(HANDLER_GROUP keyValuePair* )
    ;
    
keyValuePair
    : string EQUAL string -> ^(KEYVALUE_PAIR ^(KEY string) ^(VALUE string))
    ;
    
selectQuery
    : selectClause whereClause -> ^(SELECT selectClause whereClause)
    ;
    
selectClause
    : SELECT ASTERISK -> ^(SELECT_CLAUSE ASTERISK)
    ;
    
constructQuery
    : CONSTRUCT constructTemplate whereClause  -> ^(CONSTRUCT constructTemplate whereClause* )
    //| CONSTRUCT datasetClause* WHERE OPEN_CURLY_BRACE triplesTemplate? CLOSE_CURLY_BRACE solutionModifier -> ^(CONSTRUCT datasetClause* ^(WHERE_CLAUSE triplesTemplate*) solutionModifier*)
    ;
    
constructTemplate
    : OPEN_CURLY_BRACE constructTriples? CLOSE_CURLY_BRACE -> ^(CONSTRUCT_TRIPLES constructTriples?)
    ;
    
constructTriples
    : triple (DOT triple)* DOT? -> triple+
    ;
    
whereClause
    : WHERE groupGraphPattern -> ^(WHERE_CLAUSE groupGraphPattern)
    ;
    
groupGraphPattern
    : OPEN_CURLY_BRACE groupGraphPatternSub CLOSE_CURLY_BRACE -> ^(GROUP_GRAPH_PATTERN groupGraphPatternSub)
    | OPEN_CURLY_BRACE logicGraphPattern CLOSE_CURLY_BRACE -> logicGraphPattern
    ;
    
groupGraphPatternSub
    : triplesBlock groupGraphPatternSubDetail* -> triplesBlock groupGraphPatternSubDetail*
//    | groupGraphPatternSubDetail+ -> groupGraphPatternSubDetail+
    ;
    
groupGraphPatternSubDetail
    : graphPatternNotTriples DOT? triplesBlock? -> graphPatternNotTriples triplesBlock?
    ;
    
graphPatternNotTriples
    : timewindow
    | filter
    ;
    
timewindow
    : TIMEWINDOW OPEN_BRACE INTEGER CLOSE_BRACE -> ^(TIMEWINDOW INTEGER)
    ;
    
filter
    : FILTER constraint -> ^(FILTER constraint)
    ;
  
constraint
    : brackettedExpression
    ;
    
brackettedExpression
    : OPEN_BRACE expression CLOSE_BRACE -> ^(BRACKETTED_EXPRESSION expression)
    ;
    
expression
    : relationalExpression
    ;
    
relationalExpression
	: (n1=numericExpression -> $n1) ( ( EQUAL n2=numericExpression -> ^(EQUAL $relationalExpression $n2 ))
	| (NOT_EQUAL n3=numericExpression -> ^(NOT_EQUAL $relationalExpression $n3))
	| (LESS n4=numericExpression -> ^(LESS $relationalExpression $n4))
	| (GREATER n5=numericExpression -> ^(GREATER $relationalExpression $n5))
	| (LESS_EQUAL n6=numericExpression -> ^(LESS_EQUAL $relationalExpression $n6))
	| (GREATER_EQUAL n7=numericExpression -> ^(GREATER_EQUAL $relationalExpression $n7)) )?
	;
//    : (n1=numericExpression -> $n1) ((EQUAL n2=numericExpression -> ^(EQUAL $relationalExpression $n2))   
//                                    | (NOT_EQUAL n3=numericExpression -> ^(NOT_EQUAL $relationalExpression $n3)) 
//                                    | (LESS n4=numericExpression -> ^(LESS $relationalExpression $n4)) 
//                                    | (GREATER n5=numericExpression -> ^(GREATER $relationalExpression $n5))
//                                    | (LESS_EQUAL n6=numericExpression -> ^(LESS_EQUAL $relationalExpression $n6))
//                                    | (GREATER_EQUAL n7=numericExpression -> ^(GREATER_EQUAL $relationalExpression $n7))  
//                                    | (IN l2=expressionList -> ^(IN $relationalExpression $l2))
//                                    | (NOT IN l3=expressionList -> ^(NOT IN $relationalExpression $l3)))?
//    ;

numericExpression
    : primaryExpression
    ;
    
primaryExpression
    : brackettedExpression | numericLiteral | var
    ;

 logicGraphPattern
    : (g1=groupGraphPattern -> $g1) ( (AND g2=groupGraphPattern -> ^(AND_GRAPH $logicGraphPattern $g2 )) 
    | (BEFORE binaryBrackettedExpression g3=groupGraphPattern -> ^(BEFORE_GRAPH $logicGraphPattern binaryBrackettedExpression $g3 )) )
    ;
    
binaryBrackettedExpression
    : OPEN_BRACE (n1=numericLiteral) COMMA (n2=numericLiteral) CLOSE_BRACE -> ^(LOGIC_BRACKETTED_EXPRESSION $n1 $n2)
    ;

triplesBlock
    : triple (DOT triple)* DOT? -> triple+
    ;
    
triple
    : varOrTerm varOrTerm varOrTerm -> ^(TRIPLE ^(SUBJECT varOrTerm) ^(PREDICATE varOrTerm) ^(OBJECT varOrTerm))
    ;

varOrTerm
    : var
    | graphTerm
    ;
    
var
    : VAR1 -> ^(VAR VAR1)
    ;

graphTerm
    : iriRef
    | rdfLiteral
    //| numericLiteral
    //| booleanLiteral
    //| blankNode
    //| nil
    ;
    
rdfLiteral
    : string (LANGTAG | (REFERENCE iriRef))? -> ^(RDFLITERAL string LANGTAG* iriRef*)
    ;
    
numericLiteral
    : numericLiteralUnsigned -> ^(NUMERIC_LITERAL numericLiteralUnsigned)
//    | numericLiteralPositive
//    | numericLiteralNegative
    ;
    
numericLiteralUnsigned
    : INTEGER -> ^(INTEGER_LITERAL INTEGER)
    | DECIMAL -> ^(DECIMAL_LITERAL DECIMAL)
//    | DOUBLE
    ;
    
string
    : STRING_LITERAL1
    | STRING_LITERAL2
    //| STRING_LITERAL_LONG1
    //| STRING_LITERAL_LONG2
    ;
    
iriRef
    : IRI_REF -> ^(IRI IRI_REF)
    | prefixedName -> ^(PREFIX_NAME prefixedName)
    ;
    
prefixedName
    : PNAME_LN
    | PNAME_NS
    ;
    
// $>


// $<LEXER

DOT : '.';

PLUS : '+';

COLUMN : ':';

MINUS : '-';

PREFIX : ('P'|'p')('R'|'r')('E'|'e')('F'|'f')('I'|'i')('X'|'x');

SELECT : ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');

EPSILON_ONTOLOGY : ('E'|'e')('P'|'p')('S'|'s')('I'|'i')('L'|'l')('O'|'o')('N'|'n')('_')('O'|'o')('N'|'n')('T'|'t')('O'|'o')('L'|'l')('O'|'o')('G'|'g')('Y'|'y') ;

STATIC_INSTANCES : ('S'|'s')('T'|'t')('A'|'a')('T'|'t')('I'|'i')('C'|'c')('_')('I'|'i')('N'|'n')('S'|'s')('T'|'t')('A'|'a')('N'|'n')('C'|'c')('E'|'e')('S'|'s');

HANDLER : ('H'|'h')('A'|'a')('N'|'n')('D'|'d')('L'|'l')('E'|'e')('R'|'r');

HANDLERS : ('H'|'h')('A'|'a')('N'|'n')('D'|'d')('L'|'l')('E'|'e')('R'|'r')('S'|'s');

CONSTRUCT : ('C'|'c')('O'|'o')('N'|'n')('S'|'s')('T'|'t')('R'|'r')('U'|'u')('C'|'c')('T'|'t');

WHERE : ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

AND :	('A'|'a')('N'|'n')('D'|'d');

BEFORE : ('B'|'b')('E'|'e')('F'|'f')('O'|'o')('R'|'r')('E'|'e');

TIMEWINDOW : ('T'|'t')('I'|'i')('M'|'m')('E'|'e')('W'|'w')('I'|'i')('N'|'n')('D'|'d')('O'|'o')('W'|'w');

FILTER : ('F'|'f')('I'|'i')('L'|'l')('T'|'t')('E'|'e')('R'|'r');

TRUE : ('T'|'t')('R'|'r')('U'|'u')('E'|'e');

FALSE : ('F'|'f')('A'|'a')('L'|'l')('S'|'s')('E'|'e');

ASTERISK : '*';

COMMA : ',';

EQUAL : '=';

NOT_EQUAL : '!=';

LESS_EQUAL : '<=';

GREATER_EQUAL : '>=';

OPEN_CURLY_BRACE : '{';

CLOSE_CURLY_BRACE : '}';

OPEN_BRACE : '(';

CLOSE_BRACE : ')';

VAR1 : '?' VARNAME;

LANGTAG : '@' ('A'..'Z'|'a'..'z')+ (MINUS ('A'..'Z'|'a'..'z'|DIGIT)+)*;

INTEGER : DIGIT+;

DECIMAL
    : DIGIT+ DOT DIGIT*
//    | DOT DIGIT+
    ;

STRING_LITERAL1 : '\'' (options {greedy=false;} : ~('\'' | '\\' | EOL) | ECHAR)* '\'';

STRING_LITERAL2 : '"' (options {greedy=false;} : ~('"' | '\\' | EOL) | ECHAR)* '"';

fragment
ECHAR : '\\' ('t' | 'b' | 'n' | 'r' | 'f' | '\\' | '"' | '\'');

IRI_REF
    :('<' (options{greedy=false;}: IRI_REF_CHARACTERS)* '>') =>  '<' (options{greedy=false;}: IRI_REF_CHARACTERS)* '>'
    | LESS { $type = LESS; }
    ;

fragment
IRI_REF_CHARACTERS
    :  ~('<' | '>' | '"' | OPEN_CURLY_BRACE | CLOSE_CURLY_BRACE | PIPE | INVERSE | '`' | '\\' | '\u0000' | '\u0001'| '\u0002' | '\u0003' | '\u0004'| '\u0005' | '\u0006'| '\u0007' | '\u0008' | '\u0009'| '\u000A' | '\u000B'| '\u000C' | '\u000D' | '\u000E'| '\u000F'| '\u0010' | '\u0011'| '\u0012' | '\u0013' | '\u0014'| '\u0015' | '\u0016'| '\u0017' | '\u0018' | '\u0019'| '\u001A' | '\u001B'| '\u001C' | '\u001D' | '\u001E'| '\u001F' | '\u0020')
    ;

fragment
DIGIT : '0'..'9';

INVERSE : '^';

fragment
EOL : '\n' | '\r';

REFERENCE : '^^';

fragment
LESS : '<';

GREATER : '>';

PIPE : '|';

PNAME_NS : p=PN_PREFIX? ':';

PNAME_LN : PNAME_NS PN_LOCAL;

fragment
PN_PREFIX : PN_CHARS_BASE ((PN_CHARS|DOT)* PN_CHARS)?;

fragment
PN_LOCAL : (PN_CHARS_U|DIGIT|PLX)  ((PN_CHARS|{    
                    	                       if (input.LA(1)=='.') {
                    	                          int LA2 = input.LA(2);
                    	       	                  if (!((LA2>='-' && LA2<='.')||(LA2>='0' && LA2<='9')||(LA2>='A' && LA2<='Z')||LA2=='_'||(LA2>='a' && LA2<='z')||LA2=='\u00B7'||(LA2>='\u00C0' && LA2<='\u00D6')||(LA2>='\u00D8' && LA2<='\u00F6')||(LA2>='\u00F8' && LA2<='\u037D')||(LA2>='\u037F' && LA2<='\u1FFF')||(LA2>='\u200C' && LA2<='\u200D')||(LA2>='\u203F' && LA2<='\u2040')||(LA2>='\u2070' && LA2<='\u218F')||(LA2>='\u2C00' && LA2<='\u2FEF')||(LA2>='\u3001' && LA2<='\uD7FF')||(LA2>='\uF900' && LA2<='\uFDCF')||(LA2>='\uFDF0' && LA2<='\uFFFD'))) {
                    	       	                     return;
                    	       	                  }
                    	                       }
                                           } DOT| PLX)* (PN_CHARS|PLX))?;

fragment
PN_CHARS_U : PN_CHARS_BASE | '_';

fragment
PN_CHARS_BASE
    : 'A'..'Z'
    | 'a'..'z'
    | '\u00C0'..'\u00D6'
    | '\u00D8'..'\u00F6'
    | '\u00F8'..'\u02FF'
    | '\u0370'..'\u037D'
    | '\u037F'..'\u1FFF'
    | '\u200C'..'\u200D'
    | '\u2070'..'\u218F'
    | '\u2C00'..'\u2FEF'
    | '\u3001'..'\uD7FF'
    | '\uF900'..'\uFDCF'
    | '\uFDF0'..'\uFFFD'
    ;
    
fragment
PLX : PERCENT | PN_LOCAL_ESC;

fragment
PERCENT : '%' HEX HEX;

fragment
HEX : DIGIT | 'A'..'F' | 'a'..'z';

fragment
PN_LOCAL_ESC : '\\' ( '_' | '~' | '.' | '-' | '!' | '$' | '&' | '\'' | '(' | ')' | '*' | '+' | ',' | ';' | '=' | ':' | '/' | '?' | '#' | '@' | '%' );    	

    
fragment
PN_CHARS
    : PN_CHARS_U
    | MINUS
    | DIGIT
    | '\u00B7' 
    | '\u0300'..'\u036F'
    | '\u203F'..'\u2040'
    ;
    
fragment
VARNAME : (PN_CHARS_U | DIGIT) (PN_CHARS_U | DIGIT | '\u00B7' | '\u0300'..'\u036F' | '\u203F'..'\u2040')*;
    
// $>