package aurora.plugin.mail;

import java.io.File;

import uncertain.ocm.AbstractLocatableObject;

public class Attachment extends AbstractLocatableObject{
	
	public String path;
	public String name;
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getName() {
		if(name != null)
			return name;
		 File file = new File(path);
		 return file.getName();
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
