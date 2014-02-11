package aurora.plugin.export;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import sun.misc.BASE64Decoder;
import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

@SuppressWarnings("unchecked")
public class Base64ImgExport extends AbstractEntry {
	
	private String codePath = null;
	
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		String code = (String)model.getObject(getCodePath());
		if(code!=null && !"".equals(code)) {
			OutputStream os = null;
			try {
				BASE64Decoder decoder = new BASE64Decoder();				
				HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance.getInstance(context);
				HttpServletResponse response = serviceInstance.getResponse();
				os = response.getOutputStream();
				byte[] decoderBytes = decoder.decodeBuffer(code);
				os.write(decoderBytes);
				ProcedureRunner preRunner=runner;
				while(preRunner.getCaller()!=null){
					preRunner=preRunner.getCaller();
					preRunner.stop();
				}	
			} finally {
				if(os!=null) os.close();
			}
		}
	}

	public String getCodePath() {
		return codePath;
	}

	public void setCodePath(String codePath) {
		this.codePath = codePath;
	}

}
