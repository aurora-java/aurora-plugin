package aurora.plugin.entity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.List;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.application.sourcecode.SourceCodeUtil;
import aurora.database.service.BusinessModelService;
import aurora.database.service.DatabaseServiceFactory;
import aurora.datasource.INamedDataSourceProvider;
import aurora.plugin.entity.gen.BaseBmGenerator;
import aurora.plugin.entity.gen.ExtendBmGenerator;
import aurora.plugin.entity.gen.SqlGenerator;
import aurora.plugin.entity.model.BMModel;
import aurora.plugin.entity.model.IEntityConst;

public class EntityGenerator extends AbstractEntry {
	private EntityGeneratorConfig config = EntityGeneratorConfig.getInstance();
	private DatabaseServiceFactory svcFactory;
	private IObjectRegistry registry;
	private File bmPath;
	private CompositeMap context;
	String entityId;
	String override = "false";

	public EntityGenerator(IObjectRegistry registry,
			DatabaseServiceFactory svcFactory) {
		super();
		this.registry = registry;
		this.svcFactory = svcFactory;
		File webHome = SourceCodeUtil.getWebHome(registry);
		bmPath = new File(webHome, "WEB-INF/classes/" + config.entityPath);
	}

	public CompositeMap gen() throws Exception {
		CompositeMap entityMap = getEntity();
		CompositeMap parentEntityMap = getParentEntity(entityMap, 2);
		if (entityMap != null) {
			CompositeMap entityFieldMap_ = getEntityFields();
			if (entityFieldMap_ != null) {
				BMModel model = new BMModelCreator(config)
						.createFromCompositeMap(parentEntityMap, entityMap,
								entityFieldMap_);
				// create base bm
				CompositeMap modelMap = new BaseBmGenerator(model).gen();
				writeBmFile(modelMap, entityMap.getString("name") + ".bm");
				// #writeback#
				updateEntity(entityMap);
				updateEntityFields(entityFieldMap_);
				// create extention bm
				CompositeMap viewsMap = getEntityViews();
				@SuppressWarnings("unchecked")
				List<CompositeMap> viewsList = viewsMap.getChildsNotNull();
				for (CompositeMap vm : viewsList) {
					CompositeMap viewFieldsMap = getEntityViewField(vm
							.getLong("entity_id"));
					// create ext model
					BMModel extModel = new ExtBMModelCreator(config)
							.createFromBase(model, entityMap, vm, viewFieldsMap);
					CompositeMap extMap = new ExtendBmGenerator(extModel).gen();
					// create ext bm
					writeBmFile(extMap, vm.getString("name") + ".bm");
					// writeback
					updateEntity(vm);
				}
				// create table
				String[] sqls = new SqlGenerator(model, model.getName()).gen();
				createTable(sqls, model.getName());
			}
		}
		return entityMap;
	}

	private void createTable(String[] sqls, String tableName) throws Exception {
		Connection conn = svcFactory.getDataSource().getConnection();
		java.sql.Statement stmt = conn.createStatement();
		String seq = tableName + "_s";
		try {
			stmt.executeUpdate("create sequence " + seq);
		} catch (Exception e) {
			if (e.getMessage().indexOf("ORA-00955") != -1 && isOverride()) {
				stmt.executeUpdate("drop sequence " + seq);
				stmt.executeUpdate("create sequence " + seq);
			} else
				throw e;
		}
		try {
			for (String s : sqls) {
				stmt.executeUpdate(s);
			}
		} catch (Exception e) {
			if (e.getMessage().indexOf("ORA-00955") != -1 && isOverride()) {
				stmt.executeUpdate("drop table " + tableName);
				for (String s : sqls) {
					stmt.executeUpdate(s);
				}
			} else
				throw e;
		}
	}

	private void writeBmFile(CompositeMap data, String fileName)
			throws Exception {
		File f = new File(bmPath, fileName);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(data.toXML());
		} catch (Exception e) {
			throw e;
		} finally {
			if (bw != null)
				bw.close();
		}
	}

	public String getEntityId() {
		return entityId;
	}

	public boolean isOverride() {
		return Boolean.parseBoolean(override);
	}

	public String getOverride() {
		return override;
	}

	public void setOverride(String override) {
		this.override = override;
	}

	private CompositeMap getEntity() throws Exception {
		BusinessModelService service = svcFactory.getModelService(
				config.entityModel, context);
		CompositeMap para = new CompositeMap();
		para.put("entity_id", entityId);
		CompositeMap res = service.queryAsMap(para);
		if (res == null)
			return null;
		if (res.getChilds() == null || res.getChilds().size() == 0)
			return null;
		return (CompositeMap) res.getChilds().get(0);
	}

	private CompositeMap getParentEntity(CompositeMap curEntity, int level)
			throws Exception {
		if (level == 0)
			return curEntity;
		long parentId = curEntity.getLong("parent", -1);
		if (parentId == -1)
			return null;
		BusinessModelService service = svcFactory.getModelService(
				config.entityModel, context);
		CompositeMap para = new CompositeMap();
		para.put("entity_id", parentId);
		CompositeMap res = service.queryAsMap(para);
		if (res == null)
			return null;
		if (res.getChilds() == null || res.getChilds().size() == 0)
			return null;
		return getParentEntity((CompositeMap) res.getChilds().get(0), --level);
	}

	public void updateEntity(CompositeMap entityMap) throws Exception {
		BusinessModelService service = svcFactory.getModelService(
				config.entityModel, context);
		service.updateByPK(entityMap);
	}

	private CompositeMap getEntityFields() throws Exception {
		BusinessModelService service = svcFactory.getModelService(
				config.entityFieldModel, context);
		CompositeMap para = new CompositeMap();
		para.put("entity_id", entityId);
		return service.queryAsMap(para);
	}

	public void updateEntityFields(CompositeMap entityFieldsMap)
			throws Exception {
		BusinessModelService service = svcFactory.getModelService(
				config.entityFieldModel, context);
		@SuppressWarnings("unchecked")
		List<CompositeMap> list = entityFieldsMap.getChildsNotNull();
		for (CompositeMap m : list) {
			service.updateByPK(m);
		}
	}

	private CompositeMap getEntityViews() throws Exception {
		BusinessModelService service = svcFactory.getModelService(
				config.entityModel, context);
		CompositeMap para = new CompositeMap();
		para.put("parent", entityId);
		CompositeMap res = service.queryAsMap(para);
		if (res == null)
			return null;
		if (res.getChilds() == null || res.getChilds().size() == 0)
			return null;
		CompositeMap views_v = (CompositeMap) res.getChilds().get(0);
		para.put("parent", views_v.get("entity_id"));
		para.put("type", IEntityConst.VIEW);
		return service.queryAsMap(para);
	}

	private CompositeMap getEntityViewField(long viewId) throws Exception {
		BusinessModelService service = svcFactory.getModelService(
				config.entityViewFieldModel, context);
		CompositeMap para = new CompositeMap();
		para.put("entity_id", viewId);
		return service.queryAsMap(para);
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		this.context = runner.getContext();
		entityId = TextParser.parse(entityId, context);
		override = TextParser.parse(override, context);
		CompositeMap res = gen();
		context.putObject("/parameter/@res", res);
	}

}
