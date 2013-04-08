package aurora.plugin.entity.model;

public interface IEntityConst {
	String EXTENSION = "bmq";
	// modelviewer column properties
	String COLUMN_NUM = "no.";
	String COLUMN_PROMPT = "prompt";
	String COLUMN_TYPE = "type";
	String COLUMN_NAME = "name";
	String COLUMN_EDITOR = "editor";
	String COLUMN_QUERYFIELD = "queryfield";
	String COLUMN_ISFOREIGN = "foreign";
	String COLUMN_QUERY_OP = "query_op";
	String COLUMN_OPTIONS = "options";

	String[] TABLE_COLUMN_PROPERTIES = { "", COLUMN_NUM, COLUMN_PROMPT,
			COLUMN_TYPE, COLUMN_NAME, COLUMN_EDITOR, COLUMN_QUERYFIELD,
			COLUMN_OPTIONS };
	// relationviewer column properties
	String COLUMN_RELNAME = "rel_name";
	String COLUMN_REFMODEL = "ref_model";
	String COLUMN_LOCFIELD = "loc_field";
	String COLUMN_SRCFIELD = "src_field";
	String COLUMN_JOINTYPE = "join_type";
	String[] COLUMN_PROPERTIES = { "", COLUMN_NUM, COLUMN_RELNAME,
			COLUMN_REFMODEL, COLUMN_LOCFIELD, COLUMN_SRCFIELD, COLUMN_JOINTYPE,
			Relation.REF_PROMPTS };

	String[] JOIN_TYPES = new String[] { "LEFT OUTER", "RIGHT OUTER",
			"FULL OUTER", "INNER", "CROSS" };

	// query operator
	String OP_EQ = "=";
	String OP_GT = ">";
	String OP_GE = ">=";
	String OP_LT = "<";
	String OP_LE = "<=";
	String OP_LIKE = "like";
	String OP_PRE_MATCH = "pre_match%";
	String OP_END_MATCH = "%end_match";
	String OP_ANY_MATCH = "%any_match%";
	String OP_INTERVAL = "[from,to]";
	String[] OPERATORS = { OP_EQ, OP_GT, OP_GE, OP_LT, OP_LE, OP_LIKE,
			OP_PRE_MATCH, OP_END_MATCH, OP_ANY_MATCH, OP_INTERVAL };
	// data type
	String TEXT = "text";
	String LONG_TEXT = "long text";
	String INTEGER = "integer";
	String BIGINT = "big int";
	String FLOAT = "float";
	String DATE = "date";
	String DATE_TIME = "dateTime";
	String LOOKUPCODE = "lookupCode";
	String REFERENCE = "reference";
	String[] data_types = { TEXT, LONG_TEXT, INTEGER, BIGINT, FLOAT, DATE,
			DATE_TIME, LOOKUPCODE, REFERENCE };
	// auto extend types
	String AE_LOV = "lov";
	String AE_QUERY = "query";
	String AE_UPDATE = "update";
	String AE_MAINTAIN = "maintain";
	String[] AE_TYPES = { AE_QUERY, AE_LOV, AE_MAINTAIN };
	//
	String FOR_UPDATE = "forUpdate";
	String FOR_INSERT = "forInsert";
	String FOR_DISPLAY = "forDisplay";
	String FOR_QUERY = "forQuery";
	String INSERT_EXPRESSION = "insertExpression";
	String UPDATE_EXPRESSION = "updateExpression";
	String FOR_LOV = "forLov";
	String QUERY_WIDTH="queryWidth";
	String DISPLAY_WIDTH="displayWidth";
	// /////////entity
	String ENTITY = "ENTITY";
	String VIEW = "VIEW";
	String VIEWS = "VIEWS";
	String DETAIl = "DETAIL";
	String DETAILS = "DETAILS";
	String STATUS_NEW="NEW";
	String STATUS_GEN="GEN";
}
