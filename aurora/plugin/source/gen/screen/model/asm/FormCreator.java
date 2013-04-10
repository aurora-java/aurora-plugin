package aurora.plugin.source.gen.screen.model.asm;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Form;
import aurora.plugin.source.gen.screen.model.Input;
import aurora.plugin.source.gen.screen.model.ResultDataSet;

public class FormCreator extends AbstractModelCreator {

	public FormCreator(DatabaseServiceFactory svcFactory, CompositeMap context) {
		super(svcFactory, context);
	}

	@Override
	public void decorateComponent(AuroraComponent com, CompositeMap formPart)
			throws Exception {
		Form form = (Form) com;
		try {
			CompositeMap formMap = getFormMap(formPart.get("part_id"));
			form.setTitle("编辑 - " + formMap.get("entity_name"));
			ResultDataSet rds = new ResultDataSet();
			rds.setModel(getEntityModelPath(formMap.get("entity_id")));
			form.setDataset(rds);
			form.setCol(formMap.getInt("col", form.getCol()));
			CompositeMap itemsMap = getFormItems(formPart.get("part_id"));
			if (itemsMap != null) {
				@SuppressWarnings("unchecked")
				List<CompositeMap> itemList = itemsMap.getChildsNotNull();
				for (CompositeMap m : itemList) {
					String editor = m.getString("editor");
					Input input = createInput(editor);
					if (input != null) {
						input.setName(m.getString("name"));
						input.setPrompt(m.getString("prompt"));
						form.addChild(input);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public AuroraComponent create(CompositeMap formPart) throws Exception {
		Form form = new Form();
		decorateComponent(form, formPart);
		return form;
	}

	private CompositeMap getFormMap(Object formId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_form_id", formId);
		return PageGenerator.queryFirst(getDatabaseServiceFactory(),
				getContext(), "page.custom_form_for_query", para);

	}

	private CompositeMap getFormItems(Object formId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_form_id", formId);
		return PageGenerator.query(getDatabaseServiceFactory(), getContext(),
				"page.custom_form_item_for_query", para);
	}

}
