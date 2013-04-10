package aurora.plugin.source.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import uncertain.composite.CompositeMap;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import uncertain.pkg.IPackageManager;
import uncertain.pkg.PackageManager;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class SourceTemplateProvider implements ISourceTemplateProvider {
	private String template = "default";
	private String packageName;
	private Configuration freemarkerConfiguration;
	private Map<String, Template> cache = new HashMap<String, Template>();
	private IObjectRegistry registry;
	private File theme;
	private Template defaultTemplate;
	private SourceGenManager sourceGenManager;
	private boolean debug;

	public SourceTemplateProvider() {
		debug = true;
	}

	public SourceTemplateProvider(IObjectRegistry registry) {
		this.registry = registry;
//		setSourceGenManager((SourceGenManager) registry
//				.getInstanceOfType(SourceGenManager.class));
//		registry.registerInstance(this);
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public void initialize() {
		freemarkerConfiguration = new Configuration();
		freemarkerConfiguration.setDefaultEncoding(getDefaultEncoding());
		freemarkerConfiguration.setOutputEncoding(getDefaultEncoding());
		// freemarkerConfiguration.setNumberFormat("#");
		freemarkerConfiguration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
		// freemarkerConfiguration.setDirectoryForTemplateLoading(null);

	}

	private String getDefaultEncoding() {
		return "UTF-8";
	}

	private Template getFMTemplate(CompositeMap currentMap) {
		String component_type = currentMap.getString("component_type", "");
		Template template = cache.get(component_type);
		if (template == null) {
			template = loadTemplate(currentMap);
			if (template != null)
				cache.put(component_type, template);
			else {
				template = getDefaultTempalte();
			}
		}
		return template;
	}

	private Template getDefaultTempalte() {
		if (defaultTemplate == null) {
			defaultTemplate = Template.getPlainTextTemplate("default",
					"<a:screen id='${model}'\\>", freemarkerConfiguration);
		}
		return defaultTemplate;
	}

	private Template loadTemplate(CompositeMap currentMap) {
		String component_type = currentMap.getString("component_type", "");
		File theme = getTemplateTheme();
		File f = new File(theme, component_type + ".ftl");
		if (f.exists() && f.isFile() && f.getName().endsWith("ftl")) {
		} else {
			f = new File(theme, "default" + ".ftl");
		}

		try {
			FileReader fr = new FileReader(f);
			Template t = new Template(component_type, fr,
					freemarkerConfiguration);
			return t;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private File getTemplateTheme() {
		if (debug) {
			return new File(
					"/Users/shiliyan/Desktop/work/aurora/workspace/aurora_runtime/hap/WebContent/WEB-INF/aurora.plugin.source.gen/template/workflow");
		}
		if (theme != null)
			return theme;
		UncertainEngine engine = (UncertainEngine) registry
				.getInstanceOfType(UncertainEngine.class);
		File configDirectory = engine.getConfigDirectory();
		File f = new File(configDirectory, "aurora.plugin.source.gen");
		// f.exists() TODO

		File tFolder = new File(f,
				"template");
		theme = new File(tFolder, this.getTemplate());
		return theme;
	}

	private PackageManager getPackageManager() {
		return (PackageManager) registry
				.getInstanceOfType(IPackageManager.class);
	}

	public Map<String, String> defineConfig(BuilderSession session) {
		String property = System.getProperty("user.name");

		String format = DateFormat.getDateInstance().format(
				new java.util.Date());
		Map<String, String> config = new HashMap<String, String>();
		config.put("encoding", "UTF-8");
		config.put("date", format);
		config.put("author", property);
		config.put("revision", "1.0");
		config.put("copyright", "add by aurora_ide team");
		config.put("template_type",session.getModel().getString("template_type", ""));
		return config;
	}

	public String bindTemplate(CompositeMap context, BuilderSession session) {
		// gettemplate
		Template tplt = getFMTemplate(context);
		// process
		try {
			StringWriter sw = new StringWriter();
			Map<String, Object> p = new HashMap<String, Object>();
			p.put("context", new TemplateModelWrapper(context));
			p.put("config", defineConfig(session));
			p.put("action", new ActionMethod(session));
			p.put("properties", new PropertiesMethod(session));
			tplt.process(p, sw);
			sw.flush();
			return sw.toString();
		} catch (TemplateException e) {
			// log
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			// log
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		// out.write(sw.toString());

		// getcomponents builder
		// builders.run action

//		return "";
	}

	public String buildBM(CompositeMap modelMap) {
		return "";
	}

	public String getPackageName() {
		return packageName == null ? "aurora.plugin.source.gen" : packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String bindTemplate(BuilderSession session) {
		CompositeMap context = session.getCurrentContext();
		return this.bindTemplate(context, session);
	}

	public SourceGenManager getSourceGenManager() {
		return sourceGenManager;
	}

	public void setSourceGenManager(SourceGenManager sourceGenManager) {
		this.sourceGenManager = sourceGenManager;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
