package aurora.plugin.source.gen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.IterationHandle;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import uncertain.pkg.IPackageManager;
import uncertain.pkg.PackageManager;
import aurora.plugin.source.gen.builders.ISourceBuilder;
import aurora.plugin.source.gen.test.Test;

public class SourceGenManager {

	private IObjectRegistry registry;
	private Map<String, String> builders;
	private ISourceTemplateProvider sourceTemplateProvider;
	private IDGenerator idg = new IDGenerator();
	public SourceGenManager() {
	}

	public SourceGenManager(IObjectRegistry registry) {
		this.registry = registry;
	}

	public void buildTestScreen() {
		try {
			CompositeMap buildScreen = buildScreen(Test.loadCompositeMap());
			System.out.println(buildScreen.toXML());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

	public CompositeMap buildScreen(CompositeMap modelMap) throws IOException,
			SAXException {
		// load package
		loadBuilders();
		BuilderSession session = new BuilderSession(this);
		CompositeMap screenModel = createScreenModel(modelMap);
		session.setModel(screenModel);

		String buildComponent = buildComponent(session, screenModel);
		CompositeLoader parser = new CompositeLoader();
		CompositeMap loadFromString = parser.loadFromString(buildComponent,
				"utf-8");
		return loadFromString;
	}

	public CompositeMap buildScreen(CompositeMap modelMap,BuilderSession session) throws IOException,
			SAXException {
		// load package
		loadBuilders();
		CompositeMap screenModel = createScreenModel(modelMap);
		session.setModel(screenModel);

		String buildComponent = buildComponent(session, screenModel);
		CompositeLoader parser = new CompositeLoader();
		CompositeMap loadFromString = parser.loadFromString(buildComponent,
				"utf-8");
		return loadFromString;
	}

//	public CompositeMap buildBeOpenedScreen(CompositeMap modelMap,
//			String para_name, String para_value) throws IOException,
//			SAXException {
//		// load package
//		loadBuilders();
//		BuilderSession session = new BuilderSession(this);
//		session.addConfig("be_opened_from_another", true);
//		session.addConfig("para_name", para_name);
//		session.addConfig("para_value", para_value);
//		CompositeMap screenModel = createScreenModel(modelMap);
//		session.setModel(screenModel);
//		String buildComponent = buildComponent(session, screenModel);
//		CompositeLoader parser = new CompositeLoader();
//		CompositeMap loadFromString = parser.loadFromString(buildComponent,
//				"utf-8");
//		return loadFromString;
//	}

	public String buildComponent(BuilderSession session, CompositeMap modelMap) {
		buildContext(session, modelMap);
		return bindTemplate(session);
	}

	public String bindTemplate(BuilderSession session) {
		ISourceTemplateProvider tmplConfig = getTemplateProvider();
		return tmplConfig.bindTemplate(session);
	}

	public ISourceTemplateProvider getTemplateProvider() {
		return this.sourceTemplateProvider;
	}

	private void buildContext(BuilderSession session, CompositeMap modelMap) {
		// builder
		ISourceBuilder builder = getBuilder(modelMap);
		if (builder == null) {
			builder = getDefaultBuilder();
		}
		session.setCurrentModel(modelMap);
		builder.buildContext(session);
	}

	private ISourceBuilder getDefaultBuilder() {
		return createNewInstance("aurora.plugin.source.gen.builders.DefaultSourceBuilder");
	}

	private ISourceBuilder getBuilder(CompositeMap modelMap) {
		String component_type = modelMap.getString("component_type", "");
		String b = getBuilders().get(component_type.toLowerCase());
		if (b == null || "".equals(b)) {
			return null;
		}
		return createNewInstance(b);
	}

	public ISourceBuilder createNewInstance(String className) {
		ISourceBuilder newInstance = null;
		try {
			newInstance = (ISourceBuilder) Class.forName(className)
					.newInstance();
		} catch (InstantiationException e) {
			// log
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// log
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// log
			// e.printStackTrace();
		}
		return newInstance;
	}

	protected void loadBuilders() {
		if (getBuilders() != null) {
			return;
		}
		setBuilders(new HashMap<String, String>());
		File component_file;
		UncertainEngine engine = (UncertainEngine) registry
				.getInstanceOfType(UncertainEngine.class);
		File configDirectory = engine.getConfigDirectory();
		File f = new File(configDirectory, "aurora.plugin.source.gen");
		File config = new File(f, "config");
		component_file = new File(config, "components.xml");
		CompositeLoader loader = getCompositeLoader();
		try {
			CompositeMap components = loader.loadByFullFilePath(component_file
					.getPath());
			components.iterate(new IterationHandle() {
				public int process(CompositeMap map) {
					String component_type = map.getString("component_type", "");
					String builder = map.getString("builder", "");
					if ("".equals(component_type) == false) {
						getBuilders()
								.put(component_type.toLowerCase(), builder);
					}
					return IterationHandle.IT_CONTINUE;
				}
			}, false);
		} catch (Exception ex) {
			// load builders false
			throw new RuntimeException(ex);
		}
	}

	private CompositeLoader getCompositeLoader() {
		return getPackageManager().getCompositeLoader();
	}

	private PackageManager getPackageManager() {
		return (PackageManager) registry
				.getInstanceOfType(IPackageManager.class);
	}

	public void buildBM(CompositeMap modelMap) {

	}

	public void setTemplateProvider(
			ISourceTemplateProvider sourceTemplateProvider) {
		this.sourceTemplateProvider = sourceTemplateProvider;
		if (sourceTemplateProvider instanceof SourceTemplateProvider) {
			((SourceTemplateProvider) sourceTemplateProvider)
					.setSourceGenManager(this);
		}
	}

	private CompositeMap createScreenModel(CompositeMap bodyMap) {
		CompositeMap screen = new CompositeMap("screen");
		screen.setNameSpace(ISourceBuilder.Default_prefix,
				ISourceBuilder.Default_Namespace);
		screen.put("component_type", "screen");
		screen.put("markid", "screen3310");
		CompositeMap view = screen.createChild(ISourceBuilder.Default_prefix,
				ISourceBuilder.Default_Namespace, "view");
		view.put("component_type", "view");
		view.put("markid", "view3310");
		view.addChild(bodyMap);
		screen.put("template_type",
				bodyMap.getString("diagram_bind_template", ""));
		return screen;
	}

	public Map<String, String> getBuilders() {
		return builders;
	}

	public void setBuilders(Map<String, String> builders) {
		this.builders = builders;
	}

	public ModelMapParser createModelMapParser(CompositeMap model) {
		ModelMapParser mmp = new ModelMapParser(registry, model);
		return mmp;
	}
	public IDGenerator getIDGenerator() {
		return idg;
	}

	public void setIdg(IDGenerator idg) {
		this.idg = idg;
	}

}
