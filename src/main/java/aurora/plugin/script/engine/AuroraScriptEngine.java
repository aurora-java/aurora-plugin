package aurora.plugin.script.engine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uncertain.cache.INamedCacheFactory;
import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import aurora.javascript.Context;
import aurora.javascript.ContextFactory;
import aurora.javascript.Function;
import aurora.javascript.ImporterTopLevel;
import aurora.javascript.RhinoException;
import aurora.javascript.Script;
import aurora.javascript.Scriptable;
import aurora.javascript.ScriptableObject;
import aurora.javascript.TopLevel;
import aurora.javascript.Undefined;
import aurora.javascript.Wrapper;
import aurora.plugin.script.scriptobject.ActionEntryObject;
import aurora.plugin.script.scriptobject.CompositeMapBuilder;
import aurora.plugin.script.scriptobject.CompositeMapObject;
import aurora.plugin.script.scriptobject.CookieObject;
import aurora.plugin.script.scriptobject.ModelServiceObject;
import aurora.plugin.script.scriptobject.ScriptShareObject;
import aurora.plugin.script.scriptobject.ScriptUtil;
import aurora.plugin.script.scriptobject.SessionObject;
import aurora.service.ServiceInstance;
import aurora.service.exception.ExceptionDescriptorConfig;
import aurora.service.exception.IExceptionDescriptor;

public class AuroraScriptEngine {
	public static final String aurora_core_js = "aurora-core.js";
	public static final String KEY_SERVICE_CONTEXT = "service_context";
	public static final String KEY_SSO = "sso";
	private static String js = ScriptUtil.loadAuroraCore();
	protected static TopLevel topLevel = null;
	private Scriptable scope = null;
	static {
		RhinoException.useMozillaStackStyle(false);
		initTopLevel(Context.enter());
		Context.exit();
		ContextFactory.initGlobal(new ContextFactory() {
			protected Context makeContext() {
				Context cx = super.makeContext();
				cx.setLanguageVersion(Context.VERSION_1_8);
				cx.setOptimizationLevel(-1);
				cx.setClassShutter(RhinoClassShutter.getInstance());
				cx.setWrapFactory(RhinoWrapFactory.getInstance());
				return cx;
			}
		});
	}

	private static Set<String> executedInTopLevel = new HashSet<String>();

	protected CompositeMap service_context;
	private int optimizeLevel = -1;

	public AuroraScriptEngine(CompositeMap context) {
		super();
		if (context == null)
			throw new NullPointerException("init context for '"
					+ getClass().getSimpleName() + "' can not be null.");
		this.service_context = context;
		IObjectRegistry or = ((ScriptShareObject) service_context.get(KEY_SSO))
				.getObjectRegistry();
		UncertainEngine engine = (UncertainEngine) or
				.getInstanceOfType(UncertainEngine.class);
		CompiledScriptCache.createInstanceNE(engine);
		ScriptUtil.registerExceptionHandle(or);
	}

	protected void preDefine(Context cx, Scriptable scope) {
		Scriptable ctx = cx.newObject(scope, CompositeMapObject.CLASS_NAME,
				new Object[] { service_context });
		ScriptableObject.defineProperty(scope, "$ctx", ctx,
				ScriptableObject.EMPTY);
		// define property for $ctx
		definePropertyForCtx((CompositeMapObject) ctx, cx, service_context);

		Scriptable ses = cx.newObject(scope, SessionObject.CLASS_NAME,
				new Object[] { service_context });
		ScriptableObject.defineProperty(scope, "$session", ses,
				ScriptableObject.READONLY);
		Scriptable cok = cx.newObject(scope, CookieObject.CLASS_NAME);
		ScriptableObject.defineProperty(scope, "$cookie", cok,
				ScriptableObject.READONLY);
	}

	private void definePropertyForCtx(CompositeMapObject ctx, Context cx,
			CompositeMap service_context) {
		String[] names = { "parameter", "session", "cookie", "model" };
		for (String s : names) {
			Object p = service_context.getChild(s);
			if (p == null)
				p = service_context.createChild(s);
			ctx.definePrivateProperty(s, cx.newObject(ctx,
					CompositeMapObject.CLASS_NAME, new Object[] { p }));
		}
	}

	private static void initTopLevel(Context cx) {
		topLevel = new ImporterTopLevel(cx);
		try {
			ScriptableObject.defineClass(topLevel, CompositeMapObject.class);
			ScriptableObject.defineClass(topLevel, SessionObject.class);
			ScriptableObject.defineClass(topLevel, CookieObject.class);
			ScriptableObject.defineClass(topLevel, ModelServiceObject.class);
			ScriptableObject.defineClass(topLevel, ActionEntryObject.class);
			topLevel.defineFunctionProperties(new String[] { "print",
					"println", "raise_app_error", "$instance", "$cache",
					"$config", "$bm", "$define", "$import", "$logger" },
					AuroraScriptEngine.class, ScriptableObject.DONTENUM);
			// cx.evaluateString(topLevel, js, aurora_core_js, 1, null);
			// --define useful method
			ScriptableObject cmBuilder = (ScriptableObject) cx
					.newObject(topLevel);
			ScriptableObject.defineProperty(topLevel, "CompositeMapBuilder",
					cmBuilder, ScriptableObject.DONTENUM);
			Method[] ms = CompositeMapBuilder.class.getDeclaredMethods();
			ArrayList<String> als = new ArrayList<String>();
			int mod = Modifier.PUBLIC | Modifier.STATIC;
			for (Method m : ms) {
				if ((m.getModifiers() & mod) == mod) {
					als.add(m.getName());
				}
			}
			String[] names = als.toArray(new String[als.size()]);
			cmBuilder.defineFunctionProperties(names,
					CompositeMapBuilder.class, ScriptableObject.DONTENUM);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public Object eval(String source) throws Exception {
		Object ret = null;
		Context cx = Context.enter();
		try {
			cx.putThreadLocal(KEY_SERVICE_CONTEXT, service_context);
			cx.setOptimizationLevel(optimizeLevel);
			if (scope == null) {
				scope = cx.newObject(topLevel);
				scope.setParentScope(null);
				scope.setPrototype(topLevel);
			}
			preDefine(cx, scope);
			ScriptImportor.organizeUserImport(cx, scope, service_context);
			Script scr = CompiledScriptCache.getInstance()
					.getScript(source, cx);
			ret = scr == null ? null : scr.exec(cx, scope);
		} catch (RhinoException re) {
			if (re.getCause() instanceof InterruptException)
				throw (InterruptException) re.getCause();
			throw re;
		} finally {
			Context.exit();
		}

		if (ret instanceof Wrapper) {
			ret = ((Wrapper) ret).unwrap();
		} else if (ret instanceof Undefined)
			ret = null;
		return ret;
	}

	public static void print(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		for (int i = 0; i < args.length; i++) {
			if (i > 0)
				System.out.print(" ");
			// Convert the arbitrary JavaScript value into a string form.
			String s = Context.toString(args[i]);
			System.out.print(s);
		}
	}

	public static void println(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		print(cx, thisObj, args, funObj);
		System.out.println();
	}

	public static void raise_app_error(String err_code)
			throws InterruptException {
		throw new InterruptException(err_code);
	}

	public static Object $instance(String className) {
		return ScriptUtil.getInstanceOfType(className);
	}

	public static Object $cache(String cacheName) {
		CompositeMap ctx = ScriptUtil.getContext();
		IObjectRegistry reg = ScriptUtil.getObjectRegistry(ctx);
		INamedCacheFactory cf = (INamedCacheFactory) reg
				.getInstanceOfType(INamedCacheFactory.class);
		return cf.getNamedCache(cacheName);
	}

	public static Object $config(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		ServiceInstance si = ServiceInstance.getInstance(ScriptUtil
				.getContext());
		Script scr = CompiledScriptCache.getInstance().getScript(
				"importClass(Packages.uncertain.composite.CompositeUtil)", cx,
				"<Import CompositeUtil>");
		if (scr != null)
			scr.exec(cx, thisObj);
		return si.getServiceConfigData();
	}

	public static Object $bm(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		ModelServiceObject bm = (ModelServiceObject) cx.newObject(thisObj,
				ModelServiceObject.CLASS_NAME, args);
		if (args.length > 1)
			bm.jsSet_option(args[1]);
		return bm;
	}

	public static Object $logger(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		String topic = "server-script";
		if (args.length > 0)
			topic = "" + args[0];
		CompositeMap context = (CompositeMap) cx
				.getThreadLocal(KEY_SERVICE_CONTEXT);
		return uncertain.logging.LoggingContext.getLogger(context, topic);
	}

	public static void $define(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws IOException {
		if (args.length == 0 || !(args[0] instanceof String))
			return;
		String jspath = (String) args[0];
		String jspath_low = jspath.toLowerCase();
		CompositeMap context = (CompositeMap) cx
				.getThreadLocal(KEY_SERVICE_CONTEXT);
		if (!executedInTopLevel.contains(jspath_low)) {
			ScriptImportor.defineExternScript(cx, topLevel, context, jspath);
			executedInTopLevel.add(jspath_low);
			return;
		}
		ScriptShareObject sso = (ScriptShareObject) context
				.get(AuroraScriptEngine.KEY_SSO);
		if (sso == null)
			return;
		File jsFile = ScriptImportor.getJsFile(sso, jspath);
		if (CompiledScriptCache.getInstance().isChanged(jsFile, cx)) {
			Script script = CompiledScriptCache.getInstance().getScript(jsFile,
					cx);
			if (script != null)
				script.exec(cx, topLevel);
		}
	}

	public static void $import(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws IOException {
		if (args.length == 0 || !(args[0] instanceof String))
			return;
		String jspath = (String) args[0];
		CompositeMap context = (CompositeMap) cx
				.getThreadLocal(KEY_SERVICE_CONTEXT);
		ScriptImportor.defineExternScript(cx, thisObj, context, jspath);
	}

	public void setOptimizeLevel(int level) {
		optimizeLevel = level;
	}
}
