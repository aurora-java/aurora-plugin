package aurora.plugin.entity.model;

public class PkRecord extends Record {

	public PkRecord() {
		super();
		setName("default_pk_name");// this name should be reset
		setPrompt("primary-key");
		setType(DataType.BIGNIT.getDisplayType());
		put(COLUMN_QUERYFIELD, true);
		put(COLUMN_QUERY_OP, OP_EQ);
		setForInsert(true);
		setForUpdate(false);
		// lov
		setForDisplay(false);
		setForQuery(false);
		setForLov(true);
	}
}
