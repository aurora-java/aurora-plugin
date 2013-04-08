package aurora.plugin.entity;

public class EntityGeneratorConfig {
	String entityModel = "entity.entity";
	String entityFieldModel = "entity.entity_field";
	String entityViewFieldModel = "entity.entity_view_field";
	String entityPath = "custom";//WEB-INF/classes/custom
	String entityNamePattern = "custom_entity${@entity_id}";
	String pkRecordNamePattern = "entity${@entity_id}_id";
	String entityColumnNamePattern = "field_${@entity_id}_${@field_id}";

	private static EntityGeneratorConfig instance;

	EntityGeneratorConfig() {
		super();
		instance = this;
	}

	public static EntityGeneratorConfig getInstance() {
		if (instance == null) {
			new EntityGeneratorConfig();
		}
		return instance;
	}

	public String getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(String entityModel) {
		this.entityModel = entityModel;
	}

	public String getEntityFieldModel() {
		return entityFieldModel;
	}

	public void setEntityFieldModel(String entityFieldModel) {
		this.entityFieldModel = entityFieldModel;
	}

	public String getEntityPath() {
		return entityPath;
	}

	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}

	public String getEntityViewFieldModel() {
		return entityViewFieldModel;
	}

	public void setEntityViewFieldModel(String entityViewFieldModel) {
		this.entityViewFieldModel = entityViewFieldModel;
	}

	public String getEntityNamePattern() {
		return entityNamePattern;
	}

	public void setEntityNamePattern(String entityNamePattern) {
		this.entityNamePattern = entityNamePattern;
	}

	public String getPkRecordNamePattern() {
		return pkRecordNamePattern;
	}

	public void setPkRecordNamePattern(String pkRecordNamePattern) {
		this.pkRecordNamePattern = pkRecordNamePattern;
	}

	public String getEntityColumnNamePattern() {
		return entityColumnNamePattern;
	}

	public void setEntityColumnNamePattern(String entityColumnNamePattern) {
		this.entityColumnNamePattern = entityColumnNamePattern;
	}
}
