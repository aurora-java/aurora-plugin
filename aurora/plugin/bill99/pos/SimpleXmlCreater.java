package aurora.plugin.bill99.pos;

public class SimpleXmlCreater {
	public static String xmlHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static String xmlHead2 = "<MasMessage xmlns=\"http://www.99bill.com/mas_cnp_merchant_interface\">";
	public static String xmlEnd = "</MasMessage>";

	public String addFatherNode(String nodeName, boolean isEnd) {
		String str = "";
		if (isEnd) {
			str += "</" + nodeName + ">";
		} else {
			str += "<" + nodeName + ">";
		}
		return str;
	}

	public String addFullNode(String nodeName, String nodeValue) {
		String str = "";
		if (!("".equals(nodeValue) | nodeValue == null)) {
			str += "<" + nodeName + ">";
			str += nodeValue;
			str += "</" + nodeName + ">";
		}

		return str;
	}
}
