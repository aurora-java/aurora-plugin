package aurora.plugin.source.gen.screen.model;

import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;
import aurora.plugin.source.gen.screen.model.properties.ComponentProperties;

public class Dataset extends AuroraComponent {
	// model a.b.c
	// public static final String AUTO_QUERY = "autoQuery";
	// public static final String MODEL = "model";
	private AuroraComponent owner;
	public static final String QUERYDATASET = "querydataset";
	public static final String DATASET = "dataset";

	// public Dataset(AuroraComponent owner) {
	// this();
	// this.owner = owner;
	// }

	public Dataset() {
		// this.setComponentType("dataSet");
		this.setPageSize(DEFAULT_PAGE_SIZE);
		// this.setComponentType(RESULTDATASET);
		this.setSelectable(false);
		this.setSelectionMode(SELECT_NONE);
	}

	public String getModel() {
		return "" + this.getPropertyValue(ComponentProperties.model);
		// return model;
	}

	public void setModel(String model) {
		// this.model = model;
		this.setPropertyValue(ComponentProperties.model, model);
	}

	public AuroraComponent getOwner() {
		return owner;
	}

	public void setOwner(AuroraComponent owner) {
		this.owner = owner;
	}

	public static final String RESULTDATASET = "resultdataset";
	/**
	 * 
	 */
	public static final String SELECT_NONE = "";
	public static final String SELECT_MULTI = "multiple";
	public static final String SELECT_SINGLE = "single";
	public static final String SELECTION_MODE = "selectionModel";
	private static final String[] selectionModes = { SELECT_NONE, SELECT_MULTI,
			SELECT_SINGLE };
	// private static final IPropertyDescriptor PD_SELECTION_MODE = new
	// ComboPropertyDescriptor(
	// SELECTION_MODE, "*SelectionModel", selectionModes);

	// private ContainerHolder queryContainer = new ContainerHolder();
	public static final String QUERY_CONTAINER = "queryContainer";

	public static final int DEFAULT_PAGE_SIZE = 10;
	// private int pageSize;
	// private boolean selectable = false;

	// private String selectionModel="multiple"/"single" ;
	// private String selectionMode = SELECT_NONE;

	public static final String PAGE_SIZE = "pageSize";
	public static final String SELECTABLE = "selectable";
	public static final String QUERY_DATASET = "queryDataSet";

	// private AuroraComponent owner = null;

	// private static final IPropertyDescriptor[] pds = new
	// IPropertyDescriptor[] {
	// PD_SELECTION_MODE,
	// new IntegerPropertyDescriptor(PAGE_SIZE, "*pageSize"),
	// // new BooleanPropertyDescriptor(SELECTABLE, "selectable"),
	// // new DialogPropertyDescriptor(QUERY_CONTAINER, "*queryDataSet",
	// // QueryContainerEditDialog.class)
	// };

	// public ResultDataSet() {
	// // this.setUse4Query(false);
	// super();
	// this.setPageSize(DEFAULT_PAGE_SIZE);
	// this.setComponentType(RESULTDATASET);
	// this.setSelectable(false);
	// this.setSelectionMode(SELECT_NONE);
	// }

	public int getPageSize() {
		return this.getIntegerPropertyValue(ComponentProperties.pageSize);
		// return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.setPropertyValue(ComponentProperties.pageSize, pageSize);
		// this.pageSize = pageSize;
	}

	public boolean isSelectable() {
		// return selectable;
		return this.getBooleanPropertyValue(ComponentProperties.selectable);
	}

	public void setSelectable(boolean selectable) {
		// this.selectable = selectable;
		this.setPropertyValue(ComponentProperties.selectable, selectable);
	}

	public String getSelectionMode() {
		return this.getStringPropertyValue(ComponentProperties.selectionModel);
		// return selectionMode;
	}

	public void setSelectionMode(String selectionMode) {
		this.setPropertyValue(ComponentProperties.selectionModel, selectionMode);
		// this.selectionMode = selectionMode;
		setSelectable(!selectionMode.equals(SELECT_NONE));
	}

	public Container getQueryContainer() {
		AuroraComponent ac = this
				.getAuroraComponentPropertyValue(ComponentInnerProperties.DATASET_QUERY_CONTAINER);
		return ac instanceof Container ? (Container) ac : null;
	}

	public void setQueryContainer(Container container) {
		this.setPropertyValue(ComponentInnerProperties.DATASET_QUERY_CONTAINER,
				container);
		// queryContainer.setTarget(container);
	}

	public void setPropertyValue(String propName, Object val) {
		if (ComponentProperties.selectionModel.equals(propName)) {
			setSelectable(!val.equals(SELECT_NONE));
		}
		if (ComponentInnerProperties.DATASET_QUERY_CONTAINER_HOLDER
				.equals(propName)) {
			if(val instanceof ContainerHolder){
				this.setQueryContainer(((ContainerHolder) val).getTarget());
			}
		}
		super.setPropertyValue(propName, val);
	}

	public Object getPropertyValue(String propName) {
		if (ComponentInnerProperties.DATASET_QUERY_CONTAINER_HOLDER
				.equals(propName)) {
			ContainerHolder holder = new ContainerHolder();
			holder.setOwner(this.owner);
			holder.setTarget(this.getQueryContainer());
			holder.addContainerType(Container.SECTION_TYPE_QUERY);
			holder.addContainerType(Container.SECTION_TYPE_RESULT);
			return holder;
		}
		return super.getPropertyValue(propName);
	}

}
