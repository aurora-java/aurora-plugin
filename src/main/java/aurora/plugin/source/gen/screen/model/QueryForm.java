package aurora.plugin.source.gen.screen.model;

import java.util.List;

import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;
import aurora.plugin.source.gen.screen.model.properties.ComponentProperties;

//import org.eclipse.draw2d.geometry.Dimension;
//import org.eclipse.ui.views.properties.IPropertyDescriptor;
//
//import aurora.ide.meta.gef.editors.property.ContainerHolderEditDialog;
//import aurora.ide.meta.gef.editors.property.DialogPropertyDescriptor;
//import aurora.ide.meta.gef.editors.property.StringPropertyDescriptor;

public class QueryForm extends BOX {
	public static final String QUERY_FORM = "queryForm";
	/**
	 * 
	 */
	public static String DEFAULT_QUERY_FIELD_KEY = "defaultQueryField";
	public static String DEFAULT_QUERY_HINT_KEY = "defaultQueryHint";
	public static String RESULT_TARGET_CONTAINER_HOLDER_KEY = "resultTargetHolderContainer";
	public static String QUERY_HOOK_KEY = "queryHook";
	private QueryFormToolBar toolBar = new QueryFormToolBar();
	// private QueryFormBody body = new QueryFormBody();
	private String defaultQueryField = "";
	private String defaultQueryHint = "";
//	private String queryHook = "";
	private ContainerHolder resultTargetContainer = null;

	public QueryForm() {
		this.setComponentType(QUERY_FORM);
		Dataset ds = new Dataset();
		ds.setComponentType(Dataset.QUERYDATASET);
		this.setDataset(ds);
		this.setSectionType(BOX.SECTION_TYPE_QUERY);
		setCol(1);
		toolBar.setDataset(getDataset());
		addChild(toolBar);
		this.addPropertyChangeListener(toolBar);
		// addChild(body);
		resultTargetContainer = new ContainerHolder();
		resultTargetContainer.setOwner(this);
		resultTargetContainer.addContainerType(BOX.SECTION_TYPE_RESULT);
		setSize(600, 400);
		// addPropertyChangeListener(this);
	}

	public int getHeadHight() {
		return 0;
	}

	public QueryFormToolBar getToolBar() {
		for (AuroraComponent ac : getChildren()) {
			if (ac instanceof QueryFormToolBar)
				return (QueryFormToolBar) ac;
		}
		return null;
	}

	public QueryFormBody getBody() {
		for (AuroraComponent ac : getChildren()) {
			if (ac instanceof QueryFormBody)
				return (QueryFormBody) ac;
		}
		return null;
	}

	public void setDataset(Dataset ds) {
		super.setDataset(ds);
		if (toolBar != null)
			toolBar.setDataset(ds);
		QueryFormBody body = getBody();
		if (body != null)
			body.setDataset(ds);
	}

	// @Override
	// public IPropertyDescriptor[] getPropertyDescriptors() {
	// return pds;
	// }

	@Override
	public Object getPropertyValue(String propName) {
		if (DEFAULT_QUERY_FIELD_KEY.equals(propName))
			return getDefaultQueryField();
		else if (DEFAULT_QUERY_HINT_KEY.equals(propName))
			return getDefaultQueryHint();
		else if (RESULT_TARGET_CONTAINER_HOLDER_KEY.equals(propName))
			return getResultTargetContainer();
		else if ("resultTargetContainer".equals(propName))
			return getResultTargetContainer().getTarget();
		else if ("queryFormBody".equals(propName))
			return this.getBody();
		else if (ComponentInnerProperties.QUERY_FORM_TOOLBAR_CHILDREN
				.equals(propName))
			return this.toolBar.getHBox().getPropertyValue(
					ComponentInnerProperties.CHILDREN);
		else if (ComponentInnerProperties.QUERY_FORM_TOOLBAR.equals(propName)) {
			return this.toolBar;
		} else if (ComponentProperties.labelWidth.equals(propName)) {
			if (toolBar != null) {
				return this.toolBar.getLabelWidth();
			}
		}
		return super.getPropertyValue(propName);
	}

	@Override
	public void setPropertyValue(String propName, Object val) {
		if (DEFAULT_QUERY_FIELD_KEY.equals(propName))
			setDefaultQueryField((String) val);
		else if (DEFAULT_QUERY_HINT_KEY.equals(propName))
			setDefaultQueryHint((String) val);
		else if (RESULT_TARGET_CONTAINER_HOLDER_KEY.equals(propName))
			setResultTargetContainer((ContainerHolder) val);
		else if ("resultTargetContainer".equals(propName)
				&& val instanceof Container)
			this.getResultTargetContainer().setTarget((Container) val);
		else if ("queryFormBody".equals(propName)
				&& val instanceof QueryFormBody) {
			this.addChild((AuroraComponent) val);
		} else if (ComponentInnerProperties.QUERY_FORM_TOOLBAR_CHILDREN
				.equals(propName) && val instanceof List) {
			this.toolBar.getHBox().setPropertyValue(
					ComponentInnerProperties.CHILDREN, val);
			// for (AuroraComponent c : (List<AuroraComponent>) val) {
			// this.addChild(c);
			// }
			// return this.toolBar.getHBox().getChildren();
		} else if (ComponentProperties.labelWidth.equals(propName)) {
			if (toolBar != null) {
				toolBar.setLabelWidth(val);
			}
		} else
			super.setPropertyValue(propName, val);
	}

	public String getDefaultQueryField() {
		return defaultQueryField;
	}

	public void setDefaultQueryField(String defaultQueryField) {
		this.defaultQueryField = defaultQueryField;
	}

	public String getDefaultQueryHint() {
		return defaultQueryHint;
	}

	public void setDefaultQueryHint(String defaultQueryHint) {
		String old = this.defaultQueryField;
		this.defaultQueryHint = defaultQueryHint;
		this.firePropertyChange(DEFAULT_QUERY_HINT_KEY, old, defaultQueryHint);
		// toolBar.propertyChange(DEFAULT_QUERY_HINT_KEY, old,
		// defaultQueryHint);
	}

	public ContainerHolder getResultTargetContainer() {
		return resultTargetContainer;
	}

	public void setResultTargetContainer(ContainerHolder resultTargetContainer) {
		this.resultTargetContainer = resultTargetContainer;
		resultTargetContainer.setOwner(this);
	}

	@Override
	public boolean isResponsibleChild(AuroraComponent component) {
		if (component instanceof QueryFormToolBar)
			return getToolBar() == null;
		if (component instanceof QueryFormBody)
			return getBody() == null;
		return false;
	}

	// public void propertyChange(PropertyChangeEvent evt) {
	// if (evt.getPropertyName().equals(ComponentInnerProperties.CHILDREN)) {
	// Object newVal = evt.getNewValue();
	// if (newVal instanceof QueryFormBody) {
	// toolBar.setHasMore(getChildren().contains(newVal));
	// }
	// }
	// }
}
