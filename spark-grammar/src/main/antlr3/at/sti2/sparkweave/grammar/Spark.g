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
QUERY;
SELECT_CLAUSE;
VAR;
}

@header {
    package at.sti2.sparkweave.grammar;
}

@lexer::header {
    package at.sti2.sparkweave.grammar;
}

// $<Parser

query
    : prologue (selectQuery) EOF -> ^(QUERY prologue selectQuery*)
    ;

prologue
    : (prefixDecl)* -> ^(PROLOGUE prefixDecl*)
    ;
    
prefixDecl
    : PREFIX PNAME_NS IRI_REF -> ^(PREFIX PNAME_NS IRI_REF)
    ;
    
selectQuery
    : selectClause whereClause -> ^(SELECT selectClause whereClause)
    ;
    
selectClause
    : SELECT ASTERISK -> ^(SELECT_CLAUSE ASTERISK)
    ;
    
whereClause
    : WHERE groupGraphPattern
    ;
    
groupGraphPattern
    : OPEN_CURLY_BRACE
    ;

triplesBlock
    : triplesSameSubjectPath (DOT triplesSameSubjectPath)* DOT? -> triplesSameSubjectPath+
    ;
    
triplesSameSubjectPath
    : triplesNode propertyListNotEmpty? -> ^(TRIPLES_SAME_SUBJECT  triplesNode propertyListNotEmpty?)
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

INVERSE : '^';

PIPE : '|';

fragment
DIGIT : '0'..'9';

fragment
LESS : '<';

PNAME_NS : p=PN_PREFIX? ':';

fragment
PN_PREFIX : PN_CHARS_BASE ((PN_CHARS|DOT)* PN_CHARS)?;

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