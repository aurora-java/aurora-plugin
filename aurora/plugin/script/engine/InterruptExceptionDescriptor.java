package aurora.plugin.script.engine;

import uncertain.composite.CompositeMap;
import aurora.service.ServiceContext;
import aurora.service.exception.BaseExceptionDescriptor;
import aurora.service.exception.IExceptionDescriptor;
import aurora.service.validation.ErrorMessage;

public class InterruptExceptionDescriptor implements IExceptionDescriptor {

	@Override
	public CompositeMap process(ServiceContext context, Throwable exception) {
		String code = exception.getMessage();
		ErrorMessage msg = new ErrorMessage(code,
				BaseExceptionDescriptor.getTranslatedMessage(code, context),
				null);
		CompositeMap map = msg.getObjectContext();
		return map;
	}

}
