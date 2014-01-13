package aurora.plugin.source.gen.screen.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import aurora.plugin.source.gen.screen.model.properties.ComponentInnerProperties;

public class QueryFormToolBar extends HBox implements PropertyChangeListener {

	public static final String FORM_TOOLBAR = "formToolbar";
	private HBox hBox = new HBox();
	private Button btnQuery = new Button();
	private Button btnMore = new Button();

	public QueryFormToolBar() {
		super();
		setComponentType(FORM_TOOLBAR);
		addChild(hBox);
		btnQuery.setText("查询");
		btnMore.setText("更多");
		addChild(btnQuery);
		// addChild(btnMore);
	}

	public HBox getHBox() {
		return hBox;
	}

	@Override
	public void setDataset(Dataset ds) {
		if (hBox != null)
			hBox.setDataset(ds);
	}

	public void setHasMore(boolean more) {
		if (more && !getChildren().contains(btnMore))
			addChild(btnMore);
		if (!more && getChildren().contains(btnMore))
			removeChild(btnMore);
	}

	public boolean hasMore() {
		return getChildren().contains(btnMore);
	}

	@Override
	public int getLabelWidth() {
		if (hBox != null)
			return hBox.getLabelWidth();
		return 80;
	}

	@Override
	public void setLabelWidth(int lw) {
		if (hBox != null)
			hBox.setLabelWidth(lw);
	}

	public void setLabelWidth(Object lw) {
		if (hBox != null)
			hBox.setLabelWidth(lw);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(QueryForm.DEFAULT_QUERY_HINT_KEY)) {
			this.firePropertyChange(QueryForm.DEFAULT_QUERY_HINT_KEY,
					evt.getOldValue(), evt.getNewValue());
		}

		if (evt.getPropertyName().equals(ComponentInnerProperties.CHILDREN)) {
			Object newVal = evt.getNewValue();
			if (newVal instanceof QueryFormBody) {
				this.setHasMore(this.getParent().getChildren().contains(newVal));
			}
		}

	}
}