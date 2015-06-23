package aurora.plugin.source.gen.screen.model.asm;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Button;
import aurora.plugin.source.gen.screen.model.Grid;
import aurora.plugin.source.gen.screen.model.GridColumn;
import aurora.plugin.source.gen.screen.model.Input;
import aurora.plugin.source.gen.screen.model.Renderer;
import aurora.plugin.source.gen.screen.model.Toolbar;

public class GridCreator extends AbstractModelCreator {

	public GridCreator(DatabaseServiceFactory svcFactory, CompositeMap context) {
		super(svcFactory, context);
	}

	@Override
	public void decorateComponent(AuroraComponent com, CompositeMap gridPart)
			throws Exception {
		Grid grid = (Grid) com;
		CompositeMap gridMap;
		try {
			gridMap = getGridMap(gridPart.get("part_id"));
			grid.getDataset().setModel(
					getEntityModelPath(gridMap.get("entity_id")));
			grid.setSize(gridMap.getInt("width", grid.getSize().x),
					gridMap.getInt("height", grid.getSize().y));
			CompositeMap columnMap = getGridColumns(gridPart.get("part_id"));
			@SuppressWarnings("unchecked")
			List<CompositeMap> columndList = columnMap.getChildsNotNull();
			for (CompositeMap m : columndList) {
				GridColumn gc = new GridColumn();
				String editor = getNormalComponentType(m.getString("editor"));
				if (isViewPage()
						&& (Input.Combo.equals(editor) || Input.LOV
								.equals(editor))) {
					gc.setEditor(Input.Combo);
				} else if (!isViewPage())
					gc.setEditor(editor);
				if (Input.DATE_PICKER.equals(editor)||Input.DATETIMEPICKER.equals(editor)) {
					Renderer r = getRenderer(editor);
					gc.setRenderer(r);
				}
				gc.setSize(m.getInt("width"), gc.getSize().y);
				gc.setPrompt(m.getString("prompt"));
				gc.setName(m.getString("name"));
				grid.addChild(gc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		grid.setNavbarType(Grid.NAVBAR_COMPLEX);
	}

	@Override
	public AuroraComponent create(CompositeMap gridPart) throws Exception {
		Grid grid = new Grid();
		decorateComponent(grid, gridPart);
		return grid;
	}

	private CompositeMap getGridMap(Object gridId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_grid_id", gridId);
		return PageGenerator.queryFirst(getDatabaseServiceFactory(),
				getContext(), "page.custom_grid_for_query", para);

	}

	private CompositeMap getGridColumns(Object gridId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_grid_id", gridId);
		return PageGenerator.query(getDatabaseServiceFactory(), getContext(),
				"page.custom_grid_column_for_query", para);
	}

}
