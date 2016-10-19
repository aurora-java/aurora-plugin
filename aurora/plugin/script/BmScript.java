/*
 * Created on 2010-5-13 下午03:45:34
 * $Id: StandardWho.java 4533 2011-12-22 02:20:44Z bobbie.zou@gmail.com $
 */
package aurora.plugin.script;

import java.util.logging.Level;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IConfigurable;
import uncertain.ocm.IObjectRegistry;
import aurora.bm.BusinessModel;
import aurora.database.profile.IDatabaseFactory;
import aurora.plugin.script.engine.AuroraScriptEngine;
import aurora.plugin.script.engine.ScriptRunner;
import aurora.plugin.script.scriptobject.ScriptShareObject;
import aurora.service.ServiceThreadLocal;

public class BmScript implements IConfigurable {

	IDatabaseFactory mFactory;
	CompositeMap mContext;
	ScriptRunner mScriptRunner;
	String mScript;
	String mImport;
	private IObjectRegistry mObjectRegistry;

	public String getImport() {
		return mImport;
	}

	public void setImport(String mImport) {
		this.mImport = mImport;
	}

	String getGlobalOption(String key, String default_value) {
		String value = (String) mFactory.getProperty(key);
		return value == null ? default_value : value;
	}

	public BmScript(IDatabaseFactory fact, IObjectRegistry ior) {
		mFactory = fact;
		mObjectRegistry = ior;
		mContext = ServiceThreadLocal.getCurrentThreadContext();
	}

	public void onPrepareBusinessModel(BusinessModel model) {
		String string = "var $this=$ctx.sso.get('BusinessModel');" + mScript;
		mScriptRunner = new ScriptRunner(string, mContext, mObjectRegistry);
		try {
			ScriptShareObject sso = (ScriptShareObject) mContext
					.get(AuroraScriptEngine.KEY_SSO);
			sso.put("BusinessModel", model);

			// if (mImport != null)
			// mScriptRunner.setImport(mImport);
			mScriptRunner.run();
		} catch (Exception e) {
			e.printStackTrace();
			UncertainEngine engine = (UncertainEngine) mObjectRegistry
					.getInstanceOfType(UncertainEngine.class);
			engine.getLogger(getClass().getSimpleName()).log(Level.SEVERE,
					e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			model.makeReady();
		}

	}

	public void endConfigure() {

	}

	public void beginConfigure(CompositeMap config) {
		mScript = config.getText();
		if (mScript == null)
			mScript = "";
	}

}
