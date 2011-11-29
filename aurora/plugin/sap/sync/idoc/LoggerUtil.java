package aurora.plugin.sap.sync.idoc;

import uncertain.logging.ILogger;

public class LoggerUtil {
	private static ILogger logger;
	public static void setLogger(ILogger logger){
		LoggerUtil.logger = logger;
	}
	public static ILogger getLogger(){
		return logger;
	}
}
