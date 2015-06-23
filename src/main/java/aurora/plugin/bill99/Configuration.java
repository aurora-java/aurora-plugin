package aurora.plugin.bill99;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**

 *
 */
public class Configuration {

	private static Object lock = new Object();
	// private static Configuration config = null;
	// private static ResourceBundle rb = null;
	public static final String DEFAULT_CONFIG_FILE = "bill99";

	final private static Map<String, ResourceBundle> bundleMap = new HashMap<String, ResourceBundle>();

	public Configuration() {
		// rb = ResourceBundle.getBundle(CONFIG_FILE);
	}

	// public static Configuration getInstance() {
	// synchronized (lock) {
	// if (null == config) {
	// config = new Configuration();
	// }
	// }
	// return (config);
	// }

	static public String getValue(String fileName, String key) {
		fileName = fileName == null || "".equals(fileName) ? Configuration.DEFAULT_CONFIG_FILE
				: fileName;
		ResourceBundle rb = getResourceBundle(fileName);
		return (rb.getString(key));
	}

	static private ResourceBundle getResourceBundle(String fileName) {
		ResourceBundle resourceBundle = bundleMap.get(fileName);
		synchronized (lock) {
			if (null == resourceBundle) {
				resourceBundle = ResourceBundle.getBundle(fileName);
				bundleMap.put(fileName, resourceBundle);
			}
		}
		return resourceBundle;
	}
}
