package aurora.plugin.export;

import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import uncertain.ocm.IObjectRegistry;

public class ExcelExport {
	IObjectRegistry mObjectRegistry;
	ModelExport modelExport;
	ModelOutput modelOutput;
	boolean exportFlag = false;

	public ExcelExport(IObjectRegistry registry) {
		mObjectRegistry = registry;
	}

	public int preInvokeService(ServiceContext context) throws Exception {
		ServiceInstance svc = ServiceInstance.getInstance(context.getObjectContext());
		if (svc.getName().indexOf(".svc") != -1) {
			exportFlag = true;
			modelExport = new ModelExport(mObjectRegistry);

			return modelExport.preInvokeService(context);
		} else {
			exportFlag = false;
			modelOutput = new ModelOutput(mObjectRegistry);

			return modelOutput.preInvokeService(context);
		}
	}

	public int preCreateSuccessResponse(ServiceContext context) throws Exception {
		if (exportFlag) {
			return modelExport.preCreateSuccessResponse(context);
		} else {
			return modelOutput.preCreateSuccessResponse(context);
		}
	}
}
