package aurora.plugin.source.gen;



public class Util {
	static public String getNewLinkFilePath(String openpath, String fileName) {
		if (openpath == null)
			openpath = "";
		Path newPath = new Path(openpath);
		if (!"uip".equalsIgnoreCase(newPath.getFileExtension())) {
			return openpath;
		}
		String linkName = newPath.removeFileExtension().lastSegment();
		newPath = newPath.removeLastSegments(1);
		String newName = fileName + "_" + linkName;
		if (newName.length() > 50) {
			newName = newName.substring(0, 49);
		}
		newPath = newPath.append(newName).addFileExtension("screen");
		return newPath.toString();
	}
}
