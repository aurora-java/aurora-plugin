package aurora.plugin.source.gen.screen.model.asm;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.database.service.BusinessModelService;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.source.gen.screen.model.ScreenBody;
import aurora.plugin.source.gen.screen.model.asm.ModelCreator;
import aurora.plugin.source.gen.screen.model.asm.PageGeneratorConfig;
import aurora.plugin.source.gen.screen.model.asm.ModelCreator;
import aurora.plugin.source.gen.screen.model.asm.PageGeneratorConfig;
import aurora.plugin.source.gen.screen.model.io.Object2CompositeMap;

public class PageGenerator extends AbstractEntry {

	String pageId;
	private DatabaseServiceFactory svcFactory;
	private PageGeneratorConfig config = PageGeneratorConfig.getInstance();
	private CompositeMap context;

	public PageGenerator(IObjectRegistry registry,
			DatabaseServiceFactory svcFactory) {
		super();
		this.svcFactory = svcFactory;
	}

	public void gen() throws Exception {
		CompositeMap pageMap = getPageMap();
		if (pageMap != null) {
			ModelCreator creator = new ModelCreator(svcFactory, context);
			ScreenBody model = (ScreenBody) creator.create(pageMap);
			Object2CompositeMap o2c = new Object2CompositeMap();
			String xml = o2c.createXML(model);
			System.out.println(xml);
		}
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
