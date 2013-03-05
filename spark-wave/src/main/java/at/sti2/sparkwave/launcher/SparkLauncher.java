package at.sti2.sparkwave.launcher;

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
			jcl.invokeMain("at.sti2.sparkwave.SparkwaveKernel", args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
