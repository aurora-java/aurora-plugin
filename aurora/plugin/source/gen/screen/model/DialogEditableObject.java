package aurora.plugin.source.gen.screen.model;


public class DialogEditableObject implements IDialogEditableObject {

	private Object data;
	private String desc;
	private Object propertyId;
	private Object context;

	public void setDescripition(String desc) {
		this.desc = desc;
	}

	public String getDescripition() {
		return desc;
	}

	public Object getContextInfo() {
		return context;
	}

	public IDialogEditableObject clone() {
		DialogEditableObject dialogEditableObject = new DialogEditableObject();
		dialogEditableObject.setPropertyId(propertyId);
		dialogEditableObject.setData(data);
		dialogEditableObject.setDescripition(desc);
		dialogEditableObject.setContentInfo(context);
		return dialogEditableObject;
	}

	public void setContentInfo(Object context) {
		this.context = context;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Object getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Object propertyId) {
		this.propertyId = propertyId;
	}
}
