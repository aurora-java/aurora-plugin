package aurora.plugin.script;

import java.io.File;

import javax.script.ScriptException;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.OCManager;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.application.action.ActionUtil;
import aurora.javascript.EcmaError;
import aurora.javascript.EvaluatorException;
import aurora.javascript.JavaScriptException;
import aurora.javascript.RhinoException;
import aurora.javascript.WrappedException;
import aurora.plugin.script.engine.InterruptException;
import aurora.plugin.script.engine.ScriptRunner;

public class ServerScript extends AbstractEntry {
	String jsimport;
	String exp = null;
	String resultpath = null;
	String cdata = null;
	String optimizeLevel = null;

	int lineno = -1;
	private IObjectRegistry registry;

	public ServerScript(OCManager oc_manager, IObjectRegistry registry) {
		this.registry = registry;
	}

	public String getResultpath() {
		return resultpath;
	}

	public void setResultpath(String resultpath) {
		this.resultpath = resultpath;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public String getImport() {
		return jsimport;
	}

	public void setImport(String import1) {
		jsimport = import1;
	}

	public String getOptimizeLevel() {
		return optimizeLevel;
	}

	public void setOptimizeLevel(String optimizeLevel) {
		this.optimizeLevel = optimizeLevel;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		long t = System.nanoTime();
		CompositeMap context = runner.getContext();
		if (exp == null)
			exp = cdata;
		try {
			ScriptRunner sr = new ScriptRunner(exp, context, registry);
			sr.setImport(jsimport);
			sr.setProcedureRunner(runner);
			sr.setOptimizeLevel(optimizeLevel);
			Object res = sr.run();
			if (resultpath != null)
				context.putObject(resultpath, res, true);
		} catch (InterruptException ie) {
			// ActionUtil.raiseApplicationError(runner, registry, ie.getMessage());
			throw ie;// InterruptExceptionDescriptor will handle this exception
		} catch (RhinoException re) {
			Class<?> clz = re.getClass();
			if (clz == EcmaError.class || clz == JavaScriptException.class
					|| clz == EvaluatorException.class) {
				String fileName = new File(source).getName();
				String srcName = re.sourceName();
				String source_ = fileName;
				int line = re.lineNumber();
				boolean isInLine = "<Unknown source>".equals(srcName);
				if (isInLine) {
					line += lineno - 1;
				} else {
					source_ = source_ + " --> " + srcName;
				}
				StringBuilder sb = new StringBuilder(500);
				sb.append("source  : " + source_ + "\n");
				sb.append("lineno  : " + line + "\n");
				sb.append("line src: " + re.lineSource() + "\n");
				Throwable ee = getRootCause(re);
				sb.append("message : " + ee.getMessage());
				System.err.println(sb.toString());
				ScriptException se = new ScriptException(ee.getMessage(),
						source_, line);
				ee.printStackTrace();
				throw se;
			} else {
				re.printStackTrace();
				throw (Exception) getRootCause(re);
			}
		} finally {
			// System.out.println("server-script:" + (System.nanoTime() - t)
			// / 1000000 + "ms");
		}
	}

	private Throwable getRootCause(Throwable thr) {
		while (thr.getCause() != null)
			thr = thr.getCause();
		return thr;
	}

	@Override
	public void beginConfigure(CompositeMap config) {
		super.beginConfigure(config);
		lineno = config.getLocationNotNull().getStartLine();
		cdata = config.getText();
		if (cdata == null)
			cdata = "";
	}
}
