package at.sti2.sparkwave.rest.resources;


//public class SparkwaveKernelProvider implements Injectable<SparkwaveKernel>, InjectableProvider<Context, Type> {
//
//	SparkwaveKernel sparkwaveKernel;
//	
//	public SparkwaveKernelProvider(SparkwaveKernel sparkwaveKernel) {
//		this.sparkwaveKernel = sparkwaveKernel;
//	}
//	
//	@Override
//	public SparkwaveKernel getValue() {
//		return sparkwaveKernel;
//	}
//
//	@Override
//	public ComponentScope getScope() {
//		return ComponentScope.Singleton;
//	}
//
//	@Override
//	public Injectable getInjectable(ComponentContext cc, Context a, Type t) {
//		if(sparkwaveKernel!=null && t instanceof Class){
//			Class c = (Class)t;
//			if(SparkwaveKernel.class.isAssignableFrom(c) && c.isInstance(sparkwaveKernel)){
//				return this;
//			}
//		}
//		return null;
//	}
//
//}
