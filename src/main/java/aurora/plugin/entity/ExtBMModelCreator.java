package aurora.plugin.entity;

import java.util.List;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.model.BMModel;
import aurora.plugin.entity.model.IEntityConst;
import aurora.plugin.entity.model.Record;
import aurora.plugin.source.gen.screen.model.asm.PageGenerator;

public class ExtBMModelCreator extends AbstractBMModelCreator {

	private CompositeMap viewFields;
	private CompositeMap viewMap;

	public ExtBMModelCreator(DatabaseServiceFactory svcFactory,
			CompositeMap context) {
		super(svcFactory, context);
	}

	@Override
	public BMModel create(CompositeMap viewMap) throws Exception {
		this.viewMap = viewMap;
		CompositeMap parentEntity = getEntity(viewMap.get("parent_entity"));
		BMModel bmm = new BMModelCreator(getDatabaseServiceFactory(),
				getContext()).create(parentEntity);
		String name = getExtEntityName(viewMap);
		// bmm.setName(name);
		viewMap.put("name", name);
		viewMap.put("status", "GEN");
		this.viewFields = getViewFields(viewMap.get("entity_id"));
		@SuppressWarnings("unchecked")
		List<CompositeMap> list = viewFields.getChildsNotNull();
		for (CompositeMap m : list) {
			Record r = bmm.getRecord("field_id", m.getInt("field_id"));
			if (r == null)
				continue;
			r.setForDisplay(m.getBoolean(
					IEntityConst.FOR_DISPLAY.toLowerCase(), false));
			r.setForQuery(m.getBoolean(IEntityConst.FOR_QUERY.toLowerCase(),
					false));
			r.setForLov(r.isForDisplay() || r.isForQuery());
			int dw = m.getInt(IEntityConst.DISPLAY_WIDTH.toLowerCase(), -1);
			int qw = m.getInt(IEntityConst.QUERY_WIDTH.toLowerCase(), -1);
			if (qw != -1)
				r.put(IEntityConst.QUERY_WIDTH, qw);
			if (dw != -1)
				r.put(IEntityConst.DISPLAY_WIDTH, dw);
		}
		bmm.getPkRecord().setForLov(true);
		return bmm;
	}

	@Override
	public void updateBack() throws Exception {
		updateEntity(viewMap);
	}

	private CompositeMap getViewFields(Object viewId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("entity_id", viewId);
		return PageGenerator.query(getDatabaseServiceFactory(), getContext(),
				config.entityViewFieldModel, para);
	}

}
