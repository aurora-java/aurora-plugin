package aurora.plugin.entity;

import java.util.List;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import aurora.database.service.BusinessModelService;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.model.BMModel;
import aurora.plugin.entity.model.DataType;
import aurora.plugin.entity.model.EditorType;
import aurora.plugin.entity.model.IEntityConst;
import aurora.plugin.entity.model.ModelUtil;
import aurora.plugin.entity.model.PkRecord;
import aurora.plugin.entity.model.Record;
import aurora.plugin.source.gen.screen.model.asm.PageGenerator;

public class BMModelCreator extends AbstractBMModelCreator {

	private BMModel model;
	private CompositeMap parentEntityMap;
	private CompositeMap fieldsMap;
	private CompositeMap entityMap;

	public BMModelCreator(DatabaseServiceFactory svcFactory,
			CompositeMap context) {
		super(svcFactory, context);
	}

	@Override
	public BMModel create(CompositeMap entityMap) throws Exception {
		this.entityMap = entityMap;
		this.fieldsMap = getEntityFields(entityMap.get("entity_id"));
		this.parentEntityMap = getEntity(entityMap.get("parent_entity"));
		model = new BMModel();
		@SuppressWarnings("unchecked")
		List<CompositeMap> list = fieldsMap.getChilds();
		if (list == null)
			list = java.util.Collections.emptyList();
		PkRecord pkr = new PkRecord();
		String pk_name = getParsedPattern(config.getPkRecordNamePattern(),
				entityMap);
		pkr.setName(pk_name);
		model.setPkRecord(pkr);
		String entity_name = getParsedPattern(config.getEntityNamePattern(),
				entityMap);
		entityMap.put("name", entity_name);
		entityMap.put("status", IEntityConst.STATUS_GEN);// write back
		model.setName(entity_name);
		boolean findFirstTextField = false;
		for (CompositeMap m : list) {
			Record r = createRecordFromMap(m);
			model.add(r);
			if (!findFirstTextField) {
				if (r.getType().equals(DataType.TEXT.getDisplayType())) {
					findFirstTextField = true;
					model.setDefaultDisplay(r.getPrompt());
				}
			}
		}
		return model;
	}

	public void updateBack() throws Exception {
		updateEntity(entityMap);
		//
		BusinessModelService service = getDatabaseServiceFactory()
				.getModelService(config.entityFieldModel, getContext());
		@SuppressWarnings("unchecked")
		List<CompositeMap> list = fieldsMap.getChildsNotNull();
		for (CompositeMap m : list) {
			service.updateByPK(m);
		}
	}

	private Record createRecordFromMap(CompositeMap map) {
		Record r = new Record();
		// prompt
		r.setPrompt(map.getString(IEntityConst.COLUMN_PROMPT));
		// type
		String type = map.getString(IEntityConst.COLUMN_TYPE);
		DataType dt = DataType.fromString(type);
		if (dt == null)
			dt = DataType.TEXT;
		r.setType(dt.getDisplayType());
		// name
		String pattern = config.getEntityColumnNamePattern();
		CompositeMap para = map;
		if (r.getType().equals(IEntityConst.REFERENCE)) {
			CompositeMap m_ = new CompositeMap();
			m_.put("entity_id", map.get("source"));
			pattern = config.getPkRecordNamePattern();
			para = m_;
			if (ModelUtil.eq(map.get("source"),
					parentEntityMap.get("entity_id"))) {
				// reference to master table
				r.setInsertExpression("${../../@"
						+ getParsedPattern(pattern, para) + "}");
				r.put(IEntityConst.COLUMN_QUERYFIELD, true);
				r.put(IEntityConst.COLUMN_QUERY_OP, IEntityConst.OP_EQ);
				r.setForUpdate(false);
			} else {

			}
		}
		r.setName(getParsedPattern(pattern, para));
		map.put("name", r.getName());// write back
		// editor
		r.setEditor(EditorType.toNormalCase(map
				.getString(IEntityConst.COLUMN_EDITOR)));
		// query operator
		String query_op = map.getString(IEntityConst.COLUMN_QUERY_OP);
		if (query_op != null) {
			query_op = query_op.toLowerCase();
			r.put(IEntityConst.COLUMN_QUERYFIELD, true);
			r.put(IEntityConst.COLUMN_QUERY_OP, query_op);
		}
		// options
		Object options = map.get("source");
		if (r.getType().equals(IEntityConst.REFERENCE)) {
			r.put(IEntityConst.COLUMN_OPTIONS, config.getEntityPath() + "."
					+ getEntityName(options));
		} else {
			r.put(IEntityConst.COLUMN_OPTIONS, options);
		}
		// additions....
		r.put("field_id", map.getInt("field_id"));
		return r;
	}

	private String getEntityName(Object entityId) {
		try {
			CompositeMap para = new CompositeMap();
			para.put("entity_id", entityId);
			CompositeMap map = PageGenerator.queryFirst(
					getDatabaseServiceFactory(), getContext(),
					config.getEntityModel(), para);
			return getParsedPattern(config.getExtEntityNamePattern(), map);
		} catch (Exception e) {

		}
		return "_exception_";
	}

	private String getParsedPattern(String pattern, CompositeMap para) {
		return TextParser.parse(pattern, para);
	}

	private CompositeMap getEntityFields(Object entityId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("entity_id", entityId);
		return PageGenerator.query(getDatabaseServiceFactory(), getContext(),
				config.entityFieldModel, para);
	}

}
