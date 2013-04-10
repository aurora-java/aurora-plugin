package aurora.plugin.source.gen.screen.model.asm;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.EntityGeneratorConfig;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Button;
import aurora.plugin.source.gen.screen.model.CheckBox;
import aurora.plugin.source.gen.screen.model.Combox;
import aurora.plugin.source.gen.screen.model.Input;
import aurora.plugin.source.gen.screen.model.LOV;
import aurora.plugin.source.gen.screen.model.NumberField;
import aurora.plugin.source.gen.screen.model.TextField;

public abstract class AbstractModelCreator {
	private DatabaseServiceFactory svcFactory;
	private CompositeMap context;
	protected PageGeneratorConfig pgConfig=PageGeneratorConfig.getInstance();
	protected EntityGeneratorConfig egConfig = EntityGeneratorConfig.getInstance();

	public AbstractModelCreator(DatabaseServiceFactory svcFactory,
			CompositeMap context) {
		super();
		this.svcFactory = svcFactory;
		this.context = context;
	}
	public CompositeMap getContext() {
		return context;
	}

	public DatabaseServiceFactory getDatabaseServiceFactory() {
		return svcFactory;
	}

	public abstract AuroraComponent create(CompositeMap mainParaMap) throws Exception;
	public abstract void decorateComponent(AuroraComponent com,CompositeMap mainPartMap) throws Exception;

	public static Input createInput(String type) {
		Input input = null;
		if (type != null)
			type = type.toLowerCase();
		if ("textfield".equals(type)) {
			input = new TextField();
		} else if ("numberfield".equals(type)) {
			input = new NumberField();
		} else if ("combobox".equals(type)) {
			Combox combox = new Combox();
			input = combox;
		} else if ("lov".equals(type)) {
			input = new LOV();
		} else if ("checkbox".equals(type)) {
			input = new CheckBox();
		}
		return input;
	}

	public static Button createButton(String text) {
		Button b = new Button();
		b.setText(text);
		return b;
	}

	protected String getEntityModelPath(Object entityId) {
		CompositeMap entityMap = null;
		try {
			entityMap = getEntity(entityId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return egConfig.getEntityPath() + "." + entityMap.get("name");
	}

	protected CompositeMap getEntity(Object entityId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("entity_id", entityId);
		return PageGenerator.queryFirst(svcFactory, context,
				egConfig.getEntityModel(), para);
	}

	protected CompositeMap getEntityField(Object fieldId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("field_id", fieldId);
		return PageGenerator.queryFirst(svcFactory, context,
				egConfig.getEntityFieldModel(), para);
	}
}
