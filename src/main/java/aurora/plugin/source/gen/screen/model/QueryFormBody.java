package aurora.plugin.source.gen.screen.model;



public class QueryFormBody extends BOX {

	public static final String FORM_BODY = "formBody";

	/**
	 * 
	 */
//	private IPropertyDescriptor[] pds = { PD_COL, PD_LABELWIDTH };

	public QueryFormBody() {
		super();
		setComponentType(FORM_BODY);
		setCol(1);
	}

//	public IPropertyDescriptor[] getPropertyDescriptors() {
//		return pds;
//	}

	@Override
	public boolean isResponsibleChild(AuroraComponent component) {
		if (component instanceof QueryFormToolBar)
			return false;
		if (component instanceof QueryFormBody)
			return false;
		return true;
	}

}
