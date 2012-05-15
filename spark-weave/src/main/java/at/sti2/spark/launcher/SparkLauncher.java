package at.sti2.spark.launcher;

/**
 * This launcher is the main entry point, Jar class loader will take care of
 * loading dependencies on demand
 * 
 * @author michaelrogger
 * 
 */
public class SparkLauncher {

	public static void main(String[] args) {
		JarClassLoader jcl = new JarClassLoader();
		try {
			jcl.invokeMain("at.sti2.spark.network.SparkWeaveNetwork", args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
