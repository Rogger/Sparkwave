package at.sti2.spark.core.triple;

import at.sti2.spark.core.constants.XMLSchema;

/**
 * An extension of RDFLiteral that stores a numeric value to avoid parsing. Use
 * <code>RDFLiteral.Factory.createLiteral()</code> to create an instance.
 * 
 * @author michaelrogger
 * 
 */
public class RDFNumericLiteral extends RDFLiteral {

	private static final long serialVersionUID = -1269906711880830311L;
	
	private final Number number;
	
	protected RDFNumericLiteral(Number number, RDFURIReference datatype) {
		super(number.toString(), datatype, null);
		this.number = number;
	}
	
	protected RDFNumericLiteral(int number) {
		this(number, XMLSchema.getXSDInt());
	}
	
	protected RDFNumericLiteral(double number) {
		this(number, XMLSchema.getXSDDouble());
	}
	
	/**
	 * Return value as double
	 * @return
	 */
	public double doubleValue(){
		return number.doubleValue();
	}
}
