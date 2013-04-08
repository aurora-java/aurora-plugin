package aurora.plugin.entity;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.plugin.entity.model.BMModel;
import aurora.plugin.entity.model.IEntityConst;
import aurora.plugin.entity.model.Record;

public class ExtBMModelCreator {
	private EntityGeneratorConfig config;

	public ExtBMModelCreator(EntityGeneratorConfig config) {
		super();
		this.config = config;
	}

	public BMModel createFromBase(BMModel model, CompositeMap entityMap,
			CompositeMap viewMap, CompositeMap viewFields) {
		BMModel bmm = getCopy(model);
		viewMap.put("name",
				entityMap.get("name") + "_ext" + viewMap.get("entity_id"));
		viewMap.put("status", "GEN");
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

	private BMModel getCopy(BMModel model) {
		return model;
	}
}
