package aurora.plugin.touch;

import javax.servlet.http.HttpServletRequest;

import uncertain.composite.CompositeMap;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class DeviceTypeInit extends AbstractEntry {
	
	private static final String[] MOBILE_SPECIFIC_SUBSTRING = {"hrms",
		"iPhone", "Android", "MIDP", "Opera Mobi", "Opera Mini",
		"BlackBerry", "HP iPAQ", "IEMobile", "MSIEMobile", "Windows Phone",
		"HTC", "LG", "MOT", "Nokia", "Symbian", "Fennec", "Maemo", "Tear",
		"Midori", "armv", "Windows CE", "WindowsCE", "Smartphone",
		"240x320", "176x220", "320x320", "160x160", "webOS", "Palm",
		"Sagem", "Samsung", "SGH", "SIE", "SonyEricsson", "MMP", "UCWEB" };
	
	private static final String[] PAD_SPECIFIC_SUBSTRING = {"iPad"};
	
	
	
	public DeviceTypeInit() {
		super();
	}

	public void run(ProcedureRunner runner) {
		CompositeMap context_map = runner.getContext();
		HttpServiceInstance svc = (HttpServiceInstance)ServiceInstance.getInstance(context_map);
		HttpServletRequest request = svc.getRequest();
		String agent = request.getHeader("User-Agent");
		if(agent != null){
			if(isPad(agent)){
				context_map.putObject("/request/@device_type", "PAD",true);
			}else if(isPhone(agent)){
				context_map.putObject("/request/@device_type", "PHONE",true);
			}
		}
	}
	
	private boolean isPad(String userAgent){

		String userAgentUpper = userAgent.toUpperCase();
		
		for(String pads : PAD_SPECIFIC_SUBSTRING){
			if(userAgentUpper.contains(pads.toUpperCase())){
				return true;
			}
		}
		return false;
	}
	
	private boolean isPhone(String userAgent){

		String userAgentUpper = userAgent.toUpperCase();
		
		for(String phones : MOBILE_SPECIFIC_SUBSTRING){
			if(userAgentUpper.contains(phones.toUpperCase())){
				return true;
			}
		}
		return false;
	}
}
