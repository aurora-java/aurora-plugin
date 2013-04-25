package aurora.plugin.source.gen.screen.model.asm;

import java.util.Arrays;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.EntityGeneratorConfig;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Button;
import aurora.plugin.source.gen.screen.model.CheckBox;
import aurora.plugin.source.gen.screen.model.Combox;
import aurora.plugin.source.gen.screen.model.DatePicker;
import aurora.plugin.source.gen.screen.model.DateTimePicker;
import aurora.plugin.source.gen.screen.model.Input;
import aurora.plugin.source.gen.screen.model.LOV;
import aurora.plugin.source.gen.screen.model.NumberField;
import aurora.plugin.source.gen.screen.model.Renderer;
import aurora.plugin.source.gen.screen.model.TextArea;
import aurora.plugin.source.gen.screen.model.TextField;

public abstract class AbstractModelCreator {
	private DatabaseServiceFactory svcFactory;
	private CompositeMap context;
	protected PageGeneratorConfig pgConfig = PageGeneratorConfig.getInstance();
	protected EntityGeneratorConfig egConfig = EntityGeneratorConfig
			.getInstance();
	private String template_type;

	public AbstractModelCreator(DatabaseServiceFactory svcFactory,
			CompositeMap context) {
		super();
		this.svcFactory = svcFactory;
		this.context = context;
	}

	public CompositeMap getContext() {
		return context;
	}
	
	public void setTemplateType(String type) {
		this.template_type=type;
	}
	
	public String getTemplateType() {
		return template_type;
	}
	
	public boolean isViewPage() {
		return getTemplateType().endsWith("-view");
	}

	public DatabaseServiceFactory getDatabaseServiceFactory() {
		return svcFactory;
	}

	public abstract AuroraComponent create(CompositeMap mainParaMap)
			throws Exception;

	public abstract void decorateComponent(AuroraComponent com,
			CompositeMap mainPartMap) throws Exception;
	
	protected Renderer getRenderer(String editor) {
		Renderer r = new Renderer();
		r.setRendererType(Renderer.INNER_FUNCTION);
		if (Input.DATE_PICKER.endsWith(editor))
			r.setFunctionName("Aurora.formatDate");
		else if (Input.DATETIMEPICKER.equals(editor))
			r.setFunctionName("Aurora.formatDateTime");
		return r;
	}

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
		}else if("datepicker".equals(type)) {
			input=new DatePicker();
		}else if("datetimepicker".equals(type)) {
			input=new DateTimePicker();
		}else if("textarea".equalsIgnoreCase(type)) {
			input=new TextArea();
		}
		return input;
	}

	public static String getNormalComponentType(String type) {
		for (String s : Input.INPUT_TYPES)
			if (s.equalsIgnoreCase(type))
				return s;
		return type;
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
	
	public static boolean in(Object obj,Object...list){
		return Arrays.asList(list).contains(obj);
	}
	
	public static boolean eq(Object o1,Object o2){
		if(o1==null)
			return o2==null;
		return o1.equals(o2);
	}
}
