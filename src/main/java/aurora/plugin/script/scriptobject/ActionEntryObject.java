package aurora.plugin.script.scriptobject;

import uncertain.composite.CompositeMap;
import uncertain.ocm.ClassRegistry;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.OCManager;
import uncertain.ocm.ObjectRegistryImpl;
import uncertain.ocm.ReflectionMapper;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.javascript.Context;
import aurora.javascript.Function;
import aurora.javascript.NativeObject;
import aurora.javascript.ScriptableObject;

public class ActionEntryObject extends ScriptableObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2709907538259764391L;
	public static final String CLASS_NAME = "ActionEntry";
	private String uri;
	private String name;

	public ActionEntryObject() {
		super();
	}

	public ActionEntryObject(String uri, String name) {
		this();
		this.uri = uri;
		this.name = name;
	}

	public static ActionEntryObject jsConstructor(Context cx, Object[] args,
			Function ctorObj, boolean inNewExpr) {
		if (args.length == 0 || args[0] == Context.getUndefinedValue())
			return new ActionEntryObject();
		if (args.length == 1) {
			return new ActionEntryObject("uncertain.proc", (String) args[0]);
		}
		if (args.length == 2)
			return new ActionEntryObject((String) args[0], (String) args[1]);
		return new ActionEntryObject();
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	public void jsFunction_run(Object param) {
		IObjectRegistry ior = ScriptUtil.getObjectRegistry(ScriptUtil
				.getContext());
		ClassRegistry cr = (ClassRegistry) ior
				.getInstanceOfType(ClassRegistry.class);
		String className = cr.getClassName(new CompositeMap("", uri, name));
		if (className == null)
			throw new RuntimeException(String.format(
					"Can not find class for '%s' in '%s'.", name, uri));
		try {
			AbstractEntry entry = (AbstractEntry) ((ObjectRegistryImpl) ior)
					.createInstanceSilently(Class.forName(className));
			CompositeMap map = toCompositeMap((NativeObject) param);
			if (entry instanceof IConfigurable) {
				((IConfigurable) entry).beginConfigure(map);
			}
			OCManager ocm = (OCManager) ior.getInstanceOfType(OCManager.class);
			ReflectionMapper rm = new ReflectionMapper(ocm);
			rm.toObject(map, entry);
			ProcedureRunner runner = ScriptUtil.getProcedureRunner();
			entry.run(runner);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private CompositeMap toCompositeMap(NativeObject nObj) {
		CompositeMap map = new CompositeMap("");
		for (Object k : nObj.keySet()) {
			if (k instanceof String) {
				String key = k.toString();
				Object value = nObj.get(k);
				if (value != null) {
					map.put(key, value.toString());
					map.put(key.toLowerCase(), value.toString());
				}
			}
		}
		return map;
	}
}
