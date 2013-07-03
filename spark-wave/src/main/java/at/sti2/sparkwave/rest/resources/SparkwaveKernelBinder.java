package at.sti2.sparkwave.rest.resources;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import at.sti2.sparkwave.SparkwaveKernel;

public class SparkwaveKernelBinder extends AbstractBinder {

	private final SparkwaveKernel sparkwaveKernel;
	
	public SparkwaveKernelBinder(SparkwaveKernel sparkwaveKernel) {
		this.sparkwaveKernel = sparkwaveKernel;
	}
	
	@Override
	protected void configure() {
		bind(sparkwaveKernel).to(SparkwaveKernel.class);
	}

}
