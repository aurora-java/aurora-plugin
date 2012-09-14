package aurora.plugin.script.engine;

import java.io.File;
import java.io.IOException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import uncertain.composite.CompositeMap;
import aurora.application.sourcecode.SourceCodeUtil;
import aurora.plugin.script.scriptobject.ScriptShareObject;

public class ScriptImportor {
	private static final String std_path = "WEB-INF/server-script/";

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
		File webHome = SourceCodeUtil.getWebHome(sso.getObjectRegistry());
		String web_inf = new File(webHome, "WEB-INF").getCanonicalPath()
				.toLowerCase();
		File jsFile = null;
		for (String js : jss) {
			jsFile = new File(webHome, std_path + js.trim());
			if (!jsFile.getCanonicalPath().toLowerCase().startsWith(web_inf))
				throw new RuntimeException("imported js must under 'WEB-INF'."
						+ jsFile);
			addImport(cx, scope, jsFile);
		}
	}

	private static void addImport(Context cx, Scriptable scope, File jsFile) {
		Script script = CompiledScriptCache.getInstance().getScript(jsFile, cx);
		if (script != null)
			script.exec(cx, scope);
	}
}
