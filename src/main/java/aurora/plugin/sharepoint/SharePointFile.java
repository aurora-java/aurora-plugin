package aurora.plugin.sharepoint;

public class SharePointFile {

	private SharePointConfig spConfig;
	
	private String fileFullPath;
	private String folderPath;
	private String listName;
	private String listUrl;
	private String fileName;
	
	public SharePointFile(SharePointConfig spConfig,String fileFullPath){
		this.spConfig = spConfig;
		this.fileFullPath = fileFullPath;
		init(fileFullPath);
	}
	
	public String getFileFullPath() {
		return fileFullPath;
	}
	
	public void setFileFullPath(String fileFullPath) {
		this.fileFullPath = fileFullPath;
	}
	
	public String getFolderPath() {
		return folderPath;
	}
	
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	
	public String getListName() {
		return listName;
	}
	
	public void setListName(String listName) {
		this.listName = listName;
	}
	
	public String getListUrl() {
		return listUrl;
	}
	
	public void setListUrl(String listUrl) {
		this.listUrl = listUrl;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/*
	 * fileFullPath:http://moss15/sites/Doc/PRO_DOC/c1/c2/test.txt
	 * appLocation :http://moss15/sites/Doc
	 * listUrl     :PROC_DOC
	 * listName    :unknown,use map;
	 * folderPath  :c1/c2
	 * fileName    :test.txt
	 */
	private void init(String fileFullPath){
		String appLocation = spConfig.getAppLocation();
		int appLocationIndex = fileFullPath.indexOf(appLocation) + appLocation.length() + 1;// +1 remove /
		if(appLocationIndex<=appLocation.length())
			return;
		int folderPathIndex = fileFullPath.indexOf("/", appLocationIndex);
		listUrl = fileFullPath.substring(appLocationIndex, folderPathIndex);
		listName = spConfig.getListName(listUrl);
		if(listName == null)
			listName = listUrl;
		int endIndex = fileFullPath.lastIndexOf("/");
		folderPath = fileFullPath.substring(folderPathIndex+1, endIndex);
		fileName = fileFullPath.substring(endIndex+1);
		return;
	}
	
}
