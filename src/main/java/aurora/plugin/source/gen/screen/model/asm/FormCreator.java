package aurora.plugin.source.gen.screen.model.asm;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.model.DataType;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Combox;
import aurora.plugin.source.gen.screen.model.Dataset;
import aurora.plugin.source.gen.screen.model.Form;
import aurora.plugin.source.gen.screen.model.Input;
import aurora.plugin.source.gen.screen.model.Label;
import aurora.plugin.source.gen.screen.model.NumberField;
import aurora.plugin.source.gen.screen.model.TextArea;

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
			form.setTitle(getFormTitle(formMap.getString("entity_name")));
			Dataset ds = form.getDataset();
			ds.setModel(getEntityModelPath(formMap.get("entity_id")));
			form.setCol(formMap.getInt("col", form.getCol()));
			form.setSize(form.getCol() * 240, form.getSize().y);
			CompositeMap itemsMap = getFormItems(formPart.get("part_id"));
			if (itemsMap != null) {
				@SuppressWarnings("unchecked")
				List<CompositeMap> itemList = itemsMap.getChildsNotNull();
				for (CompositeMap m : itemList) {
					String editor = getNormalComponentType(m
							.getString("editor"));
					if (isViewPage() || "label".equals(editor)) {
						if (in(editor, Input.LOV, Input.Combo)) {
							Combox cb = new Combox();
							cb.setName(m.getString("name"));
							cb.setPrompt(m.getString("prompt"));
							form.addChild(cb);
							continue;
						}
						// 查看类页面,非必读的编辑器,保持原样(生成的页面仍然可以继续输入数据)
						if (eq(m.getString("readonly", "false"), "false")) {
							Input input = createInputExt(m);
							if (input != null) {
								form.addChild(input);
							}
							continue;
						}
						Label l = createLabel(m);
						form.addChild(l);
					} else {
						Input input = createInputExt(m);
						if (input != null) {
							form.addChild(input);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Input createInputExt(CompositeMap data) throws Exception {
		Input input = createInput(data.getString("editor"));
		if (input == null)
			return null;
		input.setName(data.getString("name"));
		input.setPrompt(data.getString("prompt"));
		input.setSize(data.getInt("width", input.getSize().x),
				input.getSize().y);
		input.setReadOnly(Boolean.parseBoolean(data.getString("readonly")));
		input.setRequired(Boolean.parseBoolean(data.getString("required")));
		if (input instanceof NumberField) {
			NumberField nf = (NumberField) input;
			CompositeMap entity_field = getEntityField(data.get("field_id"));
			if (entity_field != null) {
				DataType dt = DataType.fromString(entity_field
						.getString("type"));
				if (dt == DataType.INTEGER) {
					nf.setAllowDecimals(false);
				}
			}
		}
		if (input instanceof TextArea) {
			TextArea ta = (TextArea) input;
			ta.setSize(ta.getSize().x, data.getInt("height", ta.getSize().y));
		}
		return input;
	}

	private Label createLabel(CompositeMap data) {
		Label l = new Label();
		l.setName(data.getString("name"));
		l.setPrompt(data.getString("prompt"));
		String editor = getNormalComponentType(data.getString("editor"));
		if (Input.DATE_PICKER.equals(editor)) {
			l.setRenderer("Aurora.formatDate");
		} else if (Input.DATETIMEPICKER.equals(editor)) {
			l.setRenderer("Aurora.formatDateTime");
		}
		return l;
	}

	private String getFormTitle(String entity_name) {
		return (isViewPage() ? "查看" : "编辑") + " - " + entity_name;
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
