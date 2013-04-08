package aurora.plugin.entity.model;

public enum DataType {
	TEXT("varchar2(50)", "VARCHAR2", "java.lang.String", IEntityConst.TEXT,
			IEntityConst.OP_EQ, "textField") {
	},
	LONG_TEXT("clob", "VARCHAR2", "java.lang.String", IEntityConst.LONG_TEXT,
			IEntityConst.OP_ANY_MATCH, "textArea") {
	},
	INTEGER("number", "NUMBER", "java.lang.Long", IEntityConst.INTEGER,
			IEntityConst.OP_EQ, "numberField") {
	},
	BIGNIT("number", "BIGINT", "java.lang.Long", IEntityConst.BIGINT,
			IEntityConst.OP_EQ, "numberField") {
	},
	FLOAT("number(20,2)", "NUMBER", "java.lang.Double", IEntityConst.FLOAT,
			IEntityConst.OP_INTERVAL, "numberField") {
	},
	DATE("date", "DATE", "java.sql.Date", IEntityConst.DATE,
			IEntityConst.OP_INTERVAL, "datePicker") {
	},
	DATE_TIME("date", "TIMESTAMP", "java.sql.Date", IEntityConst.DATE_TIME,
			IEntityConst.OP_INTERVAL, "dateTimePicker") {
	},
	LOOPUPCODE("varchar2(50)", "VARCHAR2", "java.lang.String",
			IEntityConst.LOOKUPCODE, IEntityConst.OP_EQ, "comboBox") {
	},
	REFERENCE("number", "NUMBER", "java.lang.Long",
			IEntityConst.REFERENCE, IEntityConst.OP_EQ, "lov") {
	};

	private String sqlType;// use in generate sql
	private String dbType;// use in bm 'databaseType'
	private String javaType;// use in bm 'dataType'
	private String displayType;// use for display in editor
	private String defaultOperator;
	private String defaultEditor;

	private DataType(String sqlType, String dbType, String javaType,
			String displayType, String defaultOp, String defaultEditor) {
		this.sqlType = sqlType;
		this.dbType = dbType;
		this.javaType = javaType;
		this.displayType = displayType;
		this.defaultOperator = defaultOp;
		this.defaultEditor = defaultEditor;
	}

	public String getSqlType() {
		return sqlType;
	}

	public String getDbType() {
		return dbType;
	}

	public String getJavaType() {
		return javaType;
	}

	public String getDisplayType() {
		return displayType;
	}

	public String getDefaultOperator() {
		return defaultOperator;
	}

	public String getDefaultEditor() {
		return defaultEditor;
	}

	/**
	 * find the datatype by display type ,Ignore Case
	 * 
	 * @param str
	 * @return
	 */
	public static DataType fromString(String str) {
		for (DataType dt : values()) {
			if (dt.getDisplayType().equalsIgnoreCase(str))
				return dt;
		}
		return null;
	}
}
