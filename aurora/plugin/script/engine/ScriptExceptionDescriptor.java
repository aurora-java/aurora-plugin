package aurora.plugin.script.engine;

import javax.script.ScriptException;

import uncertain.composite.CompositeMap;
import aurora.service.ServiceContext;
import aurora.service.exception.IExceptionDescriptor;
import aurora.service.validation.ErrorMessage;

public class ScriptExceptionDescriptor implements IExceptionDescriptor {

	@Override
	public CompositeMap process(ServiceContext context, Throwable exception) {
		ScriptException se = (ScriptException) exception;
		String msg = exception.getMessage();
		ErrorMessage em = new ErrorMessage("SCRIPT ERROR", msg, null);
		CompositeMap map = em.getObjectContext();
		map.put("fileName", se.getFileName());
		map.put("lineno", se.getLineNumber());
		return map;
	}

}
