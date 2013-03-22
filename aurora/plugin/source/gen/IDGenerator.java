package aurora.plugin.source.gen;

import java.util.ArrayList;
import java.util.List;

import uncertain.composite.CompositeMap;

public class IDGenerator {
	private List<String> ids;

	private static final String QS = "_query_ds";

	private static final String RS = "_result_ds";

	public IDGenerator() {
		ids = new ArrayList<String>();
	}

	public String genEditorID(String editorType) {
		return genID(editorType, 0);
	}

	public String genLinkID(String fileName) {
		if (fileName == null)
			return null;
		return genID(fileName + "_link", 0);
	}

	public String genDatasetID(CompositeMap dataset) {
		String[] split = dataset.getString("model", "").split("\\.");
		String name = split[split.length - 1];
		name = "resultdataset".equalsIgnoreCase(dataset.getString(
				"component_type", "")) ? name + RS : name + QS;
		return genID(name, 0);
	}

	public String genID(String id, int i) {
		String oldID = id;
		if (i > 0) {
			id = id + "_" + i;
		}
		if (ids.contains(id)) {
			i++;
			return genID(oldID, i);
		} else {
			ids.add(id);
			return id;
		}
	}

	public String genWindowID(String linkId) {
		if (linkId == null)
			linkId = "";
		return genID(linkId + "_window", 0);
	}
}
