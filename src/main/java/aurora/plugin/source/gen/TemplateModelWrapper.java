package aurora.plugin.source.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import aurora.plugin.source.gen.screen.model.properties.IProperties;

import uncertain.composite.CompositeMap;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.SimpleObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TemplateModelWrapper implements TemplateHashModel {

	public static final String COMPONENT_CHILDREN = "component_children";

	private static final String INIT_PROCEDURE = "initprocedure";

	private static final String IS_BOX = "isbox";

	private static final String IS_LAYOUT = "islayout";

	private static final String HAS_CHILD = "haschild";
//	private static final String HAS_COMPONENT_CHILDREN = "haschildren";

	private static final String NAME = "name";

	private static final String RAWNAME = "rawname";

	private static final String COMPONENTS = "components";

	private static final String CDATA = "cdata";

	protected SimpleObjectWrapper sow = new SimpleObjectWrapper();

	protected DefaultObjectWrapper dow = new DefaultObjectWrapper();

	private CompositeMap cm;
	private String name;

	private static final String[] INNER_KEYS = { CDATA, COMPONENTS, RAWNAME,
			IS_LAYOUT, IS_BOX, HAS_CHILD, INIT_PROCEDURE, COMPONENT_CHILDREN
			 };

	public TemplateModelWrapper(CompositeMap cm) {
		this(cm.getName(), cm);
	}

	public TemplateModelWrapper(String name, CompositeMap cm) {
		super();
		this.cm = cm;
		this.name = name;
	}

	public Set<?> keys() {
		return cm.keySet();
	}

	public Collection<?> values() {
		return cm.values();
	}

	public TemplateModel get(String key) throws TemplateModelException {
		if (isInnerKey(key)) {
			return getInnerValue(key);
		}

//		if("formBody".equals(key)){
//			System.out.println();
//		}
		String compositeValue = getCompositeValue(key, cm);
		if (compositeValue != null) {
			return dow.wrap(compositeValue);
		}

		@SuppressWarnings("rawtypes")
		List childsNotNull = cm.getChildsNotNull();
		for (Object object : childsNotNull) {
			if (object instanceof CompositeMap) {
				if (key.equalsIgnoreCase(((CompositeMap) object).getName())
						|| key.equalsIgnoreCase(((CompositeMap) object)
								.getString("component_type", ""))) {
					return new TemplateModelWrapper(
							((CompositeMap) object).getName(),
							(CompositeMap) object);
				}
			}
		}
		if ("toolbar".equals(key)) {
			return null;
		}
		if ("mappings".equals(key)) {
			return null;
		}
		if ("formBody".equals(key)) {
			return null;
		}
		return dow.wrap("");
	}

	private TemplateModel getInnerValue(String key)
			throws TemplateModelException {
		if (CDATA.equalsIgnoreCase(key)) {
			String text = cm.getText();
			return dow.wrap(text == null ? "" : text);
		}
		if (COMPONENT_CHILDREN.equalsIgnoreCase(key)) {
			CompositeMap childrenMap = cm.getChildByAttrib(
					IProperties.PROPERTYE_ID, COMPONENT_CHILDREN);
			List<TemplateModel> models = new ArrayList<TemplateModel>();
			if (childrenMap == null) {
				return dow.wrap(models);
			}
			@SuppressWarnings("rawtypes")
			List childsNotNull = childrenMap.getChildsNotNull();
			for (Object object : childsNotNull) {
				models.add(new TemplateModelWrapper(((CompositeMap) object)
						.getName(), (CompositeMap) object));
			}
			return dow.wrap(models);
		}
		if (COMPONENTS.equalsIgnoreCase(key)) {
			@SuppressWarnings("rawtypes")
			List childsNotNull = cm.getChildsNotNull();
			List<TemplateModel> models = new ArrayList<TemplateModel>();
			for (Object object : childsNotNull) {
				models.add(new TemplateModelWrapper(((CompositeMap) object)
						.getName(), (CompositeMap) object));
			}
			return dow.wrap(models);
		}
		if (RAWNAME.equalsIgnoreCase(key)) {
			String text = cm.getRawName();
			return dow.wrap(text == null ? "" : text);
		}
		// if (NAME.equalsIgnoreCase(key)) {
		// String text = cm.getName();
		// return dow.wrap(text == null ? "" : text);
		// }
		if (IS_LAYOUT.equalsIgnoreCase(key)) {
			List childs = cm.getChildsNotNull();
			return dow.wrap(childs.size() > 0);
		}
		if (HAS_CHILD.equalsIgnoreCase(key)) {
			List childs = cm.getChildsNotNull();
			return dow.wrap(childs.size() > 0);
		}
//		if (HAS_COMPONENT_CHILDREN.equalsIgnoreCase(key)) {
//			CompositeMap childrenMap = cm.getChildByAttrib(
//					IProperties.PROPERTYE_ID, COMPONENT_CHILDREN);
//			return dow.wrap(""+(childrenMap == null ? false : childrenMap
//					.getChildsNotNull().size() > 0));
//		}

		if (IS_BOX.equalsIgnoreCase(key)) {
			return dow.wrap(false);
		}
		return null;
	}

	private boolean isInnerKey(String key) {
		for (String k : INNER_KEYS) {
			if (k.equalsIgnoreCase(key))
				return true;
		}
		return false;
		// return Arrays.asList(INNER_KEYS).contains(key.toLowerCase());
	}

	public boolean isEmpty() throws TemplateModelException {
		return false;
	}

	public CompositeMap getCompositeMap() {
		return cm;
	}

	public void setCompositeMap(CompositeMap cm) {
		this.cm = cm;
	}

	public String getName() {
		return name;
	}

	private String getCompositeValue(String key, CompositeMap map) {
		Set keySet = map.keySet();
		for (Object object : keySet) {
			if (key.equalsIgnoreCase(object.toString())) {
				return map.getString(object);
			}
		}
		return null;
	}
}
