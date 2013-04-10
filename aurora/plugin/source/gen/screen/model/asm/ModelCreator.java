package aurora.plugin.source.gen.screen.model.asm;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Button;
import aurora.plugin.source.gen.screen.model.HBox;
import aurora.plugin.source.gen.screen.model.ScreenBody;

public class ModelCreator extends AbstractModelCreator {

	public ModelCreator(DatabaseServiceFactory svcFactory, CompositeMap context) {
		super(svcFactory, context);
	}

	public AuroraComponent create(CompositeMap pageMap) throws Exception {
		ScreenBody model = new ScreenBody();
		CompositeMap pagePartsMap;
		pagePartsMap = getPagePartsMap(pageMap.get("custom_page_id"));
		@SuppressWarnings("unchecked")
		List<CompositeMap> partList = pagePartsMap.getChildsNotNull();

		for (CompositeMap m : partList) {
			String type = m.getString("part_type");

		}

		createButtons(model);
		return model;
	}

	@Override
	public void decorateComponent(AuroraComponent com, CompositeMap mainPartMap)
			throws Exception {

	}

	private CompositeMap getPagePartsMap(Object pageId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_page_id", pageId);
		return PageGenerator.query(getDatabaseServiceFactory(), getContext(),
				pgConfig.pagePartModel, para);
	}

	private void createButtons(ScreenBody body) {
		HBox hbox = new HBox();
		body.addChild(hbox);
		Button b = createButton("保存");
		hbox.addChild(b);
		b = createButton("提交");
		hbox.addChild(b);
	}

	
}
