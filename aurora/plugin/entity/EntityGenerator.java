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
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.gen.BaseBmGenerator;
import aurora.plugin.entity.gen.ExtendBmGenerator;
import aurora.plugin.entity.gen.SqlGenerator;
import aurora.plugin.entity.model.BMModel;
import aurora.plugin.entity.model.IEntityConst;
import aurora.plugin.source.gen.screen.model.asm.PageGenerator;

public class EntityGenerator extends AbstractEntry {
	private EntityGeneratorConfig config = EntityGeneratorConfig.getInstance();
	private DatabaseServiceFactory svcFactory;
	private File bmPath;
	private CompositeMap context;
	String entityId;
	String override = "false";

	public EntityGenerator(IObjectRegistry registry,
			DatabaseServiceFactory svcFactory) {
		super();
		this.svcFactory = svcFactory;
		File webHome = SourceCodeUtil.getWebHome(registry);
		bmPath = new File(webHome, "WEB-INF/classes/" + config.entityPath);
	}

	public CompositeMap gen() throws Exception {
		CompositeMap entityMap = getEntity();
		if (entityMap != null) {
			AbstractBMModelCreator creator = new BMModelCreator(svcFactory,
					context);
			BMModel model = creator.create(entityMap);
			// create base bm
			CompositeMap modelMap = new BaseBmGenerator(model).gen();
			writeBmFile(modelMap, model.getName() + ".bm");
			// #writeback#
			creator.updateBack();
			// create extention bm
			CompositeMap viewsMap = getEntityViews();
			@SuppressWarnings("unchecked")
			List<CompositeMap> viewsList = viewsMap.getChildsNotNull();
			for (CompositeMap vm : viewsList) {
				// create ext model
				AbstractBMModelCreator extCreator = new ExtBMModelCreator(
						svcFactory, context);
				BMModel extModel = extCreator.create(vm);
				CompositeMap extMap = new ExtendBmGenerator(extModel).gen();
				// create ext bm
				writeBmFile(extMap, vm.getString("name") + ".bm");
				// writeback
				extCreator.updateBack();
			}
			// create table
			String[] sqls = new SqlGenerator(model, model.getName()).gen();
			createTable(sqls, model.getName());
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
		f.getParentFile().mkdirs();
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
		CompositeMap para = new CompositeMap();
		para.put("entity_id", entityId);
		return PageGenerator.queryFirst(svcFactory, context,
				config.entityModel, para);
	}

	private CompositeMap getEntityViews() throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("parent_entity", entityId);
		para.put("type", IEntityConst.VIEW);
		return PageGenerator.query(svcFactory, context, config.entityModel,
				para);
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
