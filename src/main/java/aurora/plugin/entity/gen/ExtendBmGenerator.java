package aurora.plugin.entity.gen;

import uncertain.composite.CompositeMap;
import aurora.plugin.entity.EntityGeneratorConfig;
import aurora.plugin.entity.model.BMModel;
import aurora.plugin.entity.model.IEntityConst;
import aurora.plugin.entity.model.Record;

public class ExtendBmGenerator extends BaseBmGenerator {

	public ExtendBmGenerator(BMModel model) {
		super(model);
	}

	@Override
	protected String getBaseTable() {
		return null;
	}

	@Override
	protected String getExtend() {
		return EntityGeneratorConfig.getInstance().getEntityPath() + "."
				+ getModel().getName();
	}

	@Override
	protected String getExtendMode() {
		return "reference";
	}

	@Override
	protected void setUpModelMap(CompositeMap map) throws DuplicateException {
		// super.setUpModelMap(map);
		map.addChild(genFieldsMap());
		// genQueryFieldsMap(map);
	}

	private CompositeMap genFieldsMap() {
		CompositeMap fieldsMap = newCompositeMap("fields");
		for (Record r : getModel().getRecords(true)) {
			if (r.isForLov()) {
				CompositeMap m = newCompositeMap("field");
				m.put(IEntityConst.COLUMN_NAME, r.getName());
				if (r.isForQuery())
					m.put(IEntityConst.FOR_QUERY, true);
				if (r.isForDisplay())
					m.put(IEntityConst.FOR_DISPLAY, true);
				if (r.get(IEntityConst.DISPLAY_WIDTH) != null) {
					m.put(IEntityConst.DISPLAY_WIDTH,
							r.get(IEntityConst.DISPLAY_WIDTH));
				}
				if (r.get(IEntityConst.QUERY_WIDTH) != null) {
					m.put(IEntityConst.QUERY_WIDTH,
							r.get(IEntityConst.QUERY_WIDTH));
				}
				fieldsMap.addChild(m);
			}
		}
		return fieldsMap;
	}

}