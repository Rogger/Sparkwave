package at.sti2.spark.core.constants;

import at.sti2.spark.core.triple.RDFURIReference;

public class XMLSchema {

	private static final String NS = "http://www.w3.org/2001/XMLSchema#";
	
	// INT
	private static final RDFURIReference xsdInt = RDFURIReference.Factory.createURIReference(NS+"integer");
	// DOUBLE
	private static final RDFURIReference xsdDouble = RDFURIReference.Factory.createURIReference(NS+"double");
	
	private XMLSchema() {
		// private constructor
	}
	
	public static RDFURIReference getXSDInt(){
		return xsdInt;
	}
	
	public static RDFURIReference getXSDDouble(){
		return xsdDouble;
	}
}
