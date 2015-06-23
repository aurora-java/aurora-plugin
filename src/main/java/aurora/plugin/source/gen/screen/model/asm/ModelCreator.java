package aurora.plugin.source.gen.screen.model.asm;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Container;
import aurora.plugin.source.gen.screen.model.ScreenBody;

public class ModelCreator extends AbstractModelCreator {
	private static String PAGE_ID = "custom_page_id";
	private static String PAGE_TPL = "page_tpl";
	private static String PART_TYPE = "part_type";
	private static String PAGE_PATH = "page_path";

	public ModelCreator(DatabaseServiceFactory svcFactory, CompositeMap context) {
		super(svcFactory, context);
	}

	public AuroraComponent create(CompositeMap pageMap) throws Exception {
		CompositeMap pagePartsMap = getPagePartsMap(pageMap.get(PAGE_ID));
		String template = pageMap.getString(PAGE_TPL);
		if (template == null)
			throw new Exception(String.format("page(id=%d) has no template.",
					pageMap.get(PAGE_ID)));
		@SuppressWarnings("unchecked")
		List<CompositeMap> partList = pagePartsMap.getChildsNotNull();
		ScreenBody model = TemplateDescriptor.getModelFromTemplate(template);
		for (CompositeMap m : partList) {
			String type = m.getString(PART_TYPE);
			AuroraComponent ac = findComponent(model, type);
			if (ac == null)
				throw new Exception(
						String.format(
								"Can not find component in template '%s' with type '%s'.",
								template, type));
			AbstractModelCreator pCreator = null;
			if ("form".equals(type)) {
				pCreator = new FormCreator(getDatabaseServiceFactory(),
						getContext());
			} else if ("grid".equals(type)) {
				pCreator = new GridCreator(getDatabaseServiceFactory(),
						getContext());
			}
			if (pCreator != null) {
				pCreator.setTemplateType(getTemplateType());
				pCreator.decorateComponent(ac, m);
			}
		}
		return model;
	}

	private AuroraComponent findComponent(Container container,
			String component_type) {
		for (AuroraComponent c : container.getChildren()) {
			if (component_type.equalsIgnoreCase(c.getComponentType())) {
				return c;
			} else if (c instanceof Container) {
				AuroraComponent ac = findComponent((Container) c,
						component_type);
				if (ac != null)
					return ac;
			}
		}
		return null;
	}

	@Override
	public void decorateComponent(AuroraComponent com, CompositeMap mainPartMap)
			throws Exception {

	}

	private CompositeMap getPagePartsMap(Object pageId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put(PAGE_ID, pageId);
		return PageGenerator.query(getDatabaseServiceFactory(), getContext(),
				pgConfig.pagePartModel, para);
	}
}
