tree grammar SparkT;

options {
tokenVocab=Spark; // reuse token types
ASTLabelType=CommonTree; // $label will have type CommonTree
output=AST; //template;
}

@header{
    package at.sti2.spark.grammar;
}

// $<Parser

query
    : ^(QUERY prologue selectQuery?)
    ;


prologue
    : ^(PROLOGUE prefixDecl*)
    ;
    
prefixDecl
    : ^(PREFIX PNAME_NS IRI_REF)
    ;

selectQuery
    : ^(SELECT selectClause whereClause*)
    ;
    
selectClause
    : ^(SELECT_CLAUSE ASTERISK)
    ;
    
whereClause
    : ^(WHERE_CLAUSE groupGraphPattern?)
    ;

groupGraphPattern
    : ^(GROUP_GRAPH_PATTERN triplesBlock)
    ;

triplesBlock
    : triples+
    ;
    
triples
    : ^(TRIPLE ^(SUBJECT varOrTerm) ^(PREDICATE varOrTerm) ^(OBJECT varOrTerm))
    ;
    
varOrTerm
    : var 
    | graphTerm
    ;

var
    : VAR1
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
    : IRI_REF
    | prefixedName
    ;
    
prefixedName
    : PNAME_LN
    | PNAME_NS
    ;

// $>