package at.sti2.spark.rete.alpha;

public abstract class ValueTestAlphaNode extends AlphaNode {

	protected String testValue = null;
	
	public ValueTestAlphaNode(String testValue) {
		super();
		this.testValue = testValue;
	}

	public String getTestValue() {
		return testValue;
	}

	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}
	
	public String toString(){
		return "Value test node : " + testValue;
	}
}
