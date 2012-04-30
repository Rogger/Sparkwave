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
TRIPLE;
SUBJECT;
PREDICATE;
OBJECT;
}

@header {
    package at.sti2.spark.grammar;
}

@lexer::header {
    package at.sti2.spark.grammar;
}

// $<Parser

query
    : prologue (selectQuery) EOF -> ^(QUERY prologue selectQuery*)
    ;

prologue
    : prefixes -> ^(PROLOGUE prefixes)
    ;
    
prefixes
    : (prefixDecl)* -> ^(PREFIX prefixDecl*)
    ;
    
prefixDecl
    : PREFIX PNAME_NS IRI_REF -> ^(PNAME_NS IRI_REF)
    ;
    
selectQuery
    : selectClause whereClause -> ^(SELECT selectClause whereClause)
    ;
    
selectClause
    : SELECT ASTERISK -> ^(SELECT_CLAUSE ASTERISK)
    ;
    
whereClause
    : WHERE groupGraphPattern -> ^(WHERE_CLAUSE groupGraphPattern)
    ;
    
groupGraphPattern
    : OPEN_CURLY_BRACE groupGraphPatternSub CLOSE_CURLY_BRACE -> ^(GROUP_GRAPH_PATTERN groupGraphPatternSub)
    ;
    
groupGraphPatternSub
    : triplesBlock groupGraphPatternSubDetail* -> triplesBlock groupGraphPatternSubDetail*
    | groupGraphPatternSubDetail+ -> groupGraphPatternSubDetail+
    ;
    
groupGraphPatternSubDetail
    : graphPatternNotTriples DOT? triplesBlock? -> graphPatternNotTriples triplesBlock?
    ;
    
graphPatternNotTriples
    : timewindow
    ;
    
timewindow
    : TIMEWINDOW OPEN_BRACE TIMEWINDOW_CONSTRAINT CLOSE_BRACE -> ^(TIMEWINDOW TIMEWINDOW_CONSTRAINT)
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
    //| rdfLiteral
    //| numericLiteral
    //| booleanLiteral
    //| blankNode
    //| nil
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

MINUS : '-';

PREFIX : ('P'|'p')('R'|'r')('E'|'e')('F'|'f')('I'|'i')('X'|'x');

SELECT : ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');

WHERE : ('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');

TIMEWINDOW : ('T'|'t')('I'|'i')('M'|'m')('E'|'e')('W'|'w')('I'|'i')('N'|'n')('D'|'d')('O'|'o')('W'|'w');

ASTERISK : '*';

OPEN_CURLY_BRACE : '{';

CLOSE_CURLY_BRACE : '}';

OPEN_BRACE : '(';

CLOSE_BRACE : ')';


VAR1 : '?' VARNAME;

INVERSE : '^';

PIPE : '|';

fragment
DIGIT : '0'..'9';

TIMEWINDOW_CONSTRAINT : DIGIT+;

fragment
LESS : '<';

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

IRI_REF
    :('<' (options{greedy=false;}: IRI_REF_CHARACTERS)* '>') =>  '<' (options{greedy=false;}: IRI_REF_CHARACTERS)* '>'
    | LESS { $type = LESS; }
    ;

fragment
IRI_REF_CHARACTERS
    :  ~('<' | '>' | '"' | OPEN_CURLY_BRACE | CLOSE_CURLY_BRACE | PIPE | INVERSE | '`' | '\\' | '\u0000' | '\u0001'| '\u0002' | '\u0003' | '\u0004'| '\u0005' | '\u0006'| '\u0007' | '\u0008' | '\u0009'| '\u000A' | '\u000B'| '\u000C' | '\u000D' | '\u000E'| '\u000F'| '\u0010' | '\u0011'| '\u0012' | '\u0013' | '\u0014'| '\u0015' | '\u0016'| '\u0017' | '\u0018' | '\u0019'| '\u001A' | '\u001B'| '\u001C' | '\u001D' | '\u001E'| '\u001F' | '\u0020')
    ;
    
fragment
VARNAME : (PN_CHARS_U | DIGIT) (PN_CHARS_U | DIGIT | '\u00B7' | '\u0300'..'\u036F' | '\u203F'..'\u2040')*;
    
// $>