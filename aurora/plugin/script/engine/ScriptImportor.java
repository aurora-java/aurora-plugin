package aurora.plugin.script.engine;

import java.io.File;
import java.io.IOException;


import uncertain.composite.CompositeMap;
import aurora.application.sourcecode.SourceCodeUtil;
import aurora.javascript.Context;
import aurora.javascript.Script;
import aurora.javascript.Scriptable;
import aurora.plugin.script.scriptobject.ScriptShareObject;

public class ScriptImportor {
	public static final String std_path = "WEB-INF/server-script/";

	public static void organizeUserImport(Context cx, Scriptable scope,
			CompositeMap context) throws IOException {
		ScriptShareObject sso = (ScriptShareObject) context
				.get(AuroraScriptEngine.KEY_SSO);
		if (sso == null)
			return;
		String str = (String) sso.get(ScriptShareObject.KEY_IMPORT);
		if (str == null || str.trim().length() == 0)
			return;
		String[] jss = str.split(";");
		if (jss.length == 0)
			return;
		File jsFile = null;
		for (String js : jss) {
			jsFile = getJsFile(sso, js);
			addImport(cx, scope, jsFile);
		}
	}

	public static void defineExternScript(Context cx, Scriptable scope,
			CompositeMap context, String jspath) throws IOException {
		ScriptShareObject sso = (ScriptShareObject) context
				.get(AuroraScriptEngine.KEY_SSO);
		if (sso == null)
			return;
		File jsFile = getJsFile(sso, jspath);
		addImport(cx, scope, jsFile);
	}

	public static File getJsFile(ScriptShareObject sso, String jsPath)
			throws IOException {
		File webHome = SourceCodeUtil.getWebHome(sso.getObjectRegistry());
		String web_inf = new File(webHome, "WEB-INF").getCanonicalPath()
				.toLowerCase();
		File jsFile = new File(webHome, std_path + jsPath);
		checkJsPath(jsFile, web_inf);
		return jsFile;
	}

	private static void checkJsPath(File jsFile, String web_inf)
			throws IOException {
		if (!jsFile.getCanonicalPath().toLowerCase().startsWith(web_inf))
			throw new RuntimeException("imported js must under 'WEB-INF'."
					+ jsFile);
	}

	private static void addImport(Context cx, Scriptable scope, File jsFile) {
		Script script = CompiledScriptCache.getInstance().getScript(jsFile, cx);
		if (script != null)
			script.exec(cx, scope);
	}
}
