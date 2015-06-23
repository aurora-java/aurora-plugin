package aurora.plugin.jms;

import java.util.Enumeration;
import java.util.Properties;

import javax.jms.TextMessage;

import uncertain.composite.CompositeMap;

import aurora.application.features.msg.IMessage;
public class JMSMessage implements IMessage{
	TextMessage textMessage;
	public JMSMessage(TextMessage textMessage){
		this.textMessage = textMessage;
	}
	public String getText() throws Exception{
		return textMessage.getText();
	}
	public CompositeMap getProperties() throws Exception{
		if(textMessage.getPropertyNames()!= null){
			CompositeMap cm = new CompositeMap();
			String propertyName;
			for(Enumeration e = textMessage.getPropertyNames();e.hasMoreElements();){
				propertyName = e.nextElement().toString();
				Object obj = textMessage.getObjectProperty(propertyName);
				cm.put(propertyName,obj);
			}
			return cm;
		}
		return null;
	}
	public TextMessage getTextMessage(){
		return textMessage;
	}
}
