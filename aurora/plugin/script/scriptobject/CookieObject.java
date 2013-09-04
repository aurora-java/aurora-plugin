package aurora.plugin.script.scriptobject;

import uncertain.composite.CompositeMap;
import uncertain.proc.ProcedureRunner;
import aurora.application.action.AuroraCookie;
import aurora.javascript.Callable;
import aurora.javascript.Scriptable;
import aurora.javascript.ScriptableObject;

public class CookieObject extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3648414099810680339L;
	public static final String CLASS_NAME = "Cookie";
	private CompositeMap context;
	private ProcedureRunner runner;
	private AuroraCookie auroraCookie;

	public CookieObject() {
		super();
		context = ScriptUtil.getContext();
	}

	private void init() {
		if (runner == null)
			runner = new ProcedureRunner();
		if (auroraCookie == null)
			auroraCookie = new AuroraCookie();
	}

	public void jsFunction_put(String name, String value, Integer maxAge) {
		if (maxAge == null || maxAge == 0)
			maxAge = -1;
		init();
		runner.setContext(context);
		auroraCookie.setName(name);
		auroraCookie.setValue(value);
		auroraCookie.setMaxAge(maxAge);
		try {
			auroraCookie.run(runner);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		if (!(value instanceof Callable)) {
			jsFunction_put(name, value == null ? null : value.toString(), -1);
		}
		if (!isSealed())
			super.put(name, start, value);
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
