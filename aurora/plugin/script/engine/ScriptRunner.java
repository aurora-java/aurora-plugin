package aurora.plugin.script.engine;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.ProcedureRunner;
import aurora.plugin.script.scriptobject.ScriptShareObject;

public class ScriptRunner {
	private String exp;

	private CompositeMap context = null;
	private ScriptShareObject sso;

	private String optimizeLevel;

	public ScriptRunner(String script) {
		this.exp = script;

	}

	public ScriptRunner(String script, CompositeMap context) {
		this(script);
		this.context = context;
		sso = (ScriptShareObject) context.get(AuroraScriptEngine.KEY_SSO);
		if (sso == null) {
			sso = new ScriptShareObject();
			context.put(AuroraScriptEngine.KEY_SSO, sso);
		}
	}

	public ScriptRunner(String script, CompositeMap context,
			IObjectRegistry registry) {
		this(script, context);
		sso.put(registry);
	}

	public String getOriginalScript() {
		return exp;
	}

	public void setImport(String import1) {
		sso.put(ScriptShareObject.KEY_IMPORT, import1);
	}

	public String getImport() {
		return sso.get(ScriptShareObject.KEY_IMPORT);
	}

	@Deprecated
	public String getParsedScript() {
		if (context == null)
			return exp;
		return TextParser.parse(exp, context);
	}

	public Object run() throws Exception {
		AuroraScriptEngine engine = sso.getEngine();
		if (engine == null) {
			engine = new AuroraScriptEngine(context);
			sso.put(engine);
		}
		// DON'T parse xpath tag in script
		String str = exp;// getParsedScript();
		if (optimizeLevel != null && optimizeLevel.length() > 0)
			engine.setOptimizeLevel(Integer.parseInt(optimizeLevel));
		return engine.eval(str);
	}

	public void setProcedureRunner(ProcedureRunner runner) {
		sso.put(ScriptShareObject.KEY_RUNNER, runner);
	}

	public void setOptimizeLevel(String optimizeLevel) {
		this.optimizeLevel = optimizeLevel;
	}
}
