package aurora.plugin.source.gen.screen.model.asm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.application.sourcecode.SourceCodeUtil;
import aurora.database.service.BusinessModelService;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.source.gen.SourceGenManager;
import aurora.plugin.source.gen.SourceTemplateProvider;
import aurora.plugin.source.gen.screen.model.ScreenBody;
import aurora.plugin.source.gen.screen.model.io.Object2CompositeMap;

public class PageGenerator extends AbstractEntry {

	private DatabaseServiceFactory svcFactory;
	private PageGeneratorConfig config = PageGeneratorConfig.getInstance();
	private CompositeMap context;
	String pageId;
	private IObjectRegistry registry;
	private File webHome;

	public PageGenerator(IObjectRegistry registry,
			DatabaseServiceFactory svcFactory) {
		super();
		this.registry = registry;
		this.svcFactory = svcFactory;
		webHome = SourceCodeUtil.getWebHome(registry);
	}

	public CompositeMap gen() throws Exception {
		CompositeMap pageMap = getContext().getChild("parameter");
		if (pageMap != null) {
			ModelCreator creator = new ModelCreator(svcFactory, context);
			creator.setTemplateType(pageMap.getString("page_tpl"));
			ScreenBody model = (ScreenBody) creator.create(pageMap);
			Object2CompositeMap o2c = new Object2CompositeMap();
			// String xml = o2c.createXML(model);
			// System.out.println(xml);
			pageMap.put("status", "GEN");
			String page_path = getPageFullPath(pageMap);
			pageMap.put("page_path", page_path);
			// create screen
			createScreen(o2c.createCompositeMap(model), page_path);
			// write back
			updatePageMap(pageMap);
		}
		return pageMap;
	}

	private String getPageFullPath(CompositeMap pageMap) {
		return String.format("modules/%s/%s", config.pagePath,
				TextParser.parse(config.pageNamePattern, pageMap));
	}

	private void createScreen(CompositeMap modelMap, String page_path)
			throws Exception {
		SourceGenManager sgm = new SourceGenManager(registry);
		SourceTemplateProvider stp = new SourceTemplateProvider(registry);
		sgm.setTemplateProvider(stp);
		stp.initialize();
		stp.setTemplate("workflow");
		CompositeMap screen = sgm.buildScreen(modelMap);
		writeScreenFile(screen, page_path);
	}

	private void writeScreenFile(CompositeMap data, String page_path)
			throws Exception {
		File f = new File(webHome, page_path);
		f.getParentFile().mkdirs();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			fos.write(data.toXML().getBytes("UTF-8"));
		} catch (Exception e) {
			throw e;
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	private void updatePageMap(CompositeMap pageMap) throws Exception {
		BusinessModelService bms = svcFactory.getModelService(config.pageModel,
				getContext());
		bms.updateByPK(pageMap);
	}

	public void setContext(CompositeMap context) {
		this.context = context;
	}

	public CompositeMap getContext() {
		return context;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		context = runner.getContext();
	}

	public static CompositeMap query(DatabaseServiceFactory svcFactory,
			CompositeMap context, String bm, CompositeMap para)
			throws Exception {
		BusinessModelService bms = svcFactory.getModelService(bm, context);
		return bms.queryAsMap(para);
	}

	public static CompositeMap queryFirst(DatabaseServiceFactory svcFactory,
			CompositeMap context, String bm, CompositeMap para)
			throws Exception {
		CompositeMap res = query(svcFactory, context, bm, para);
		if (res != null && res.getChilds() != null
				&& res.getChilds().size() > 0)
			return (CompositeMap) res.getChilds().get(0);
		return null;
	}

	public CompositeMap getPageMap() throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_page_id", pageId);
		return queryFirst(svcFactory, context, config.pageModel, para);
	}

}
