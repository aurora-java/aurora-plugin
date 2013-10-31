package aurora.plugin.source.gen.screen.model.properties;

public interface ComponentInnerProperties {
	String COMPONENT_TYPE = "component_type";
	String CHILDREN = "component_children";
	String COMPONENT_CHILDREN = CHILDREN;
	String LOCATION_X = "location_x";
	String LOCATION_Y = "location_y";
	String LOCATION = "location";
	String DATASET_OWNER = "dataset_owner";

	String OPEN_PATH = "openpath";
	String DATASET_QUERY_CONTAINER = "dataset_query_container";
	String BUTTON_CLICK_TARGET_COMPONENT = "button_click_target_component";
	String BUTTON_CLICK_ACTIONID = "button_click_actionid";
	// String BUTTON_CLICK_OPENPATH = "button_click_openpath";
	String BUTTON_CLICK_OPENPATH = OPEN_PATH;
	String BUTTON_CLICK_CLOSEWINDOWID = "button_click_closewindowid";
	String BUTTON_CLICK_FUNCTION = "button_click_function";
	String BUTTON_CLICK_PARAMETERS = "button_click_parameters";
	String BUTTON_CLICKER = "button_clicker";
	String GRIDCOLUMN_ROWHIGHT = "gridcolumn_rowhight";
	String GRID_COLUMNS = "grid_columns";
	String FOOT_RENDERER_TYPE = "foot_renderer_type";
	String FOOT_RENDERER_FUNCTION = "foot_renderer_funcrion";

	String PARAMETER_NAME = "parameter_name";
	String PARAMETER_VALUE = "parameter_value";

	// String RENDERER_OPEN_PATH = "renderer_open_path";
	String RENDERER_OPEN_PATH = OPEN_PATH;
	String RENDERER_LABELTEXT = "renderer_labeltext";
	String RENDERER_FUNCTION_NAME = "renderer_function_name";
	String RENDERER_FUNCTION = "renderer_function";
	String RENDERER_TYPE = "renderer_type";
	String RENDERER_PARAMETERS = "renderer_parameters";

	String GRID_COLUMN_RENDERER = "grid_column_renderer";
	String GRID_COLUMN_FOOTRENDERER = "grid_column_footrenderer";
	String GRID_SELECTION_MODE = "grid_selection_mode";
	String GRID_NAVBAR = "grid_navbar";
	String GRID_TOOLBAR = "grid_toolbar";

	String TABBODY_VISIBLE = "tabbody_visible";

	String DIAGRAM_BIND_TEMPLATE = "diagram_bind_template";
	String DIAGRAM_BIND_TEMPLATE_TYPE = "diagram_bind_template_type";

	String COMPONENT_MARKER_ID = "component_marker_id";

	String CONTAINER_SECTION_TYPE = "container_section_type";
	String DATASET_DELEGATE = "i_dataset_delegate";
	String DATASET_FIELD_DELEGATE = "i_dataset_field_delegate";
	String TAB_ITEM_CURRENT = "tab_item_current";
	//
	String BOUNDS = "component_bounds";
	String SIZE = "size";
	String TAB_SCREEN_REF = "tab_screen_ref";
	String TOOLBAR = "toolbar";
	public static final String INNER_EDITOR_UN_BIND_MODELS = "inner_editor_un_bind_models";
	public static final String INNER_LOV_SERVICE = "inner_lov_service";
	String DATASET_QUERY_CONTAINER_HOLDER = "dataset_query_container_holder";
	String PAGE_REDIRECT = "PAGE_REDIRECT";
	String LINK_ID = "link_id";
	String INNER_BUTTONCLICKER = "inner_buttonclicker";
	String LINK_BASE_PATH = "link_base_path";
	String LINK_FILE_PATH = "link_file_path";
	String LOVSERVICE_OPTIONS = "lovservice_options";
	String FIELD_TYPE = "field_type";
	String FIELD_NAME = "field_name";
	String QUERY_DS = "query_ds";
	String I_DATASET_DELEGATE = "i_dataset_delegate";
	String MARKID = "markid";
	String PROPERTYE_ID = "propertye_id";
	String DS_ID = "ds_id";
	String CUSTOM = "custom";
	String BINDED_NAME = "binded_name";
	String LINE_MODEL = "line_model";
	String NEED_SUBMIT_URL = "need_submit_url";
	String IS_WORKFLOW_HEAD_DS = "is_workflow_head_ds";
	String WORKFLOW_HEAD_DS_ID = "workflow_head_ds_id";
	String WORKFLOW_HEAD_MODEL_PK = "workflow_head_model_pk";
	String COMPONENT_TABS = "component_tabs";
	String FUNCTION_NAME = "functionName";
	String SAVE = "save";
	String OPEN = "open";
	String RESET = "reset";
	String QUERY = "query";
	String CLOSE = "close";
	String INNER_FUNCTION = "INNER_FUNCTION";
	String CLOSE_WINDOW_ID = "closeWindowID";
	String USER_FUNCTION = "USER_FUNCTION";
	String FUNCTION = "function";
	String INNER_PARAMERTER = "inner_paramerter";
	String QUERY_FORM_TOOLBAR_CHILDREN = "query_form_toolbar_children";
	String QUERY_FORM_TOOLBAR = "query_form_toolbar";
	String RESULT_TARGET_CONTAINER = "resultTargetContainer";
	String EDITOR_TYPE = "editor_type";
	String IS_HEAD_DS = "is_head_ds";
	String NEED_MASTER_DETAIL_SUBMIT_URL = "need_master_detail_submit_url";
	String BE_OPENED_FROM_ANOTHER = "be_opened_from_another";
	String NEED_AUTO_QUERY_URL = "need_auto_query_url";
	String DS_TYPE = "ds_type";
	String INNER_DATSET_FIELD_MAPPINGS = "inner_datset_field_mappings";
	String LOVSERVICE_FOR_DISPLAY = "lovservice_for_display";
	String LOVSERVICE_FOR_RETURN = "lovservice_for_return";
	String FOR_DISPLAY_FIELD = "for_display_field";
	String FOR_RETURN_FIELD = "for_return_field";
	String MAPPINGS = "mappings";
	String FILE_NAME = "file_name";
	String INPUT_SIMPLE_DATA = "input_simple_data";
	String GRID_COLUMN_SIMPLE_DATA = "grid_column_simple_data_";
	String ICON_BYTES_DATA = "icon_bytes_data";
	static final String IMAGE_WIDTH = "image_width";
	static final String IMAGE_HEIGHT = "image_height";
	String TEXT_STYLE = "_text_style";
	String GRID_COLUMN_SORTABLE = "grid_column_sortable";
	String ICON_BYTES_DATA_DEO = ComponentInnerProperties.ICON_BYTES_DATA
			+ "_deo";
}
