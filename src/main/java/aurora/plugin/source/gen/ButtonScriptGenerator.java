package aurora.plugin.source.gen;


public class ButtonScriptGenerator {
	

	private BuilderSession session;

	public ButtonScriptGenerator(BuilderSession session){
		this.session = session;
	}
	

	public String hrefScript(String functionName, String labelText,
			String newWindowName, String parameter) {
		String s = "function #functionName#(value,record, name){return '<a href=\"javascript:#newWindowName#(record)\">#LabelText#</a>';}";
		s = s.replace("#functionName#", functionName);
		s = s.replace("#newWindowName#", newWindowName);
		s = s.replace("#LabelText#", labelText);
		return s;
	}

	public String searchScript(String functionName, String datasetId) {
		String s = "function #functionName#(){$('#datasetId#').query();}";
		s = s.replace("#functionName#", functionName);
		s = s.replace("#datasetId#", datasetId);
		return s;
	}

	public String resetScript(String functionName, String datasetId) {
		String s = " function #functionName#(){$('#datasetId#').reset();}";
		s = s.replace("#functionName#", functionName);
		s = s.replace("#datasetId#", datasetId);
		return s;
	}

	public String saveScript(String functionName, String datasetId) {
		String s = " function #functionName#(){$('#datasetId#').submit();}";
		s = s.replace("#functionName#", functionName);
		s = s.replace("#datasetId#", datasetId);
		return s;
	}

	public String openScript(String functionName, String linkId) {
		String s = " function #functionName#() {var linkUrl = $('#linkId#');  new Aurora.Window({id: '#windowId#',url:linkUrl.getUrl(),title: 'Title',height: 635,width: 720});}";
		s = s.replace("#functionName#", functionName);
		String windowID = session.getIDGenerator().genWindowID(linkId);
		s = s.replaceAll("#windowId#", windowID);
		s = s.replaceAll("#linkId#", linkId);

		return s;
	}

	public String closeScript(String functionName, String windowId) {
		String s = "function #functionName#(){$('#windowId#').close();}";
		s = s.replace("#functionName#", functionName);
		s = s.replace("#windowId#", windowId);
		return s;
	}

}
