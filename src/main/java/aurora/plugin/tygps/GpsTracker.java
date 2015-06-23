package aurora.plugin.tygps;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import uncertain.composite.CompositeMap;
import uncertain.composite.JSONAdaptor;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;
import aurora.service.ServiceContext;

@SuppressWarnings("unchecked")
public class GpsTracker extends AbstractEntry {

	private GPSProvider provider;
	private String debugger = "false";
	private String path = "gps";
	private String url;
	protected Parameter[] parameters;

	public GpsTracker(IObjectRegistry registry) {
		provider = (GPSProvider) registry.getInstanceOfType(GPSProvider.class);
	}

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		ServiceContext service = ServiceContext.createServiceContext(context);
		CompositeMap model = service.getModel();
		String url = getUrl();
		if(url!=null){
			DefaultHttpClient httpclient = new DefaultHttpClient();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(GPSProvider.CUSTOMER_ID, provider.getCustomerId()));
			params.add(new BasicNameValuePair(GPSProvider.USER_ACCOUNT, provider.getUserAccount()));
			params.add(new BasicNameValuePair(GPSProvider.USER_PASSWORD, provider.getPassword()));
			
			if(parameters != null){
				for(Parameter parameter:parameters){
					params.add(new BasicNameValuePair(parameter.getName(), uncertain.composite.TextParser.parse(parameter.getValue(), model)));
				}
			}
			URI uri = new URI(url + "?" + URLEncodedUtils.format(params, "utf-8"));
			HttpGet httpget = new HttpGet(uri);
			try {
				if("true".equals(debugger)) {
					LoggingContext.getLogger(context,GpsTracker.class.getCanonicalName()).log(httpget.getURI().toString());
				}
				HttpResponse response = httpclient.execute(httpget); 
				String content = EntityUtils.toString(response.getEntity());
				if(content!=null) {
					JSONObject jobj = new JSONObject(content);
		            CompositeMap root = JSONAdaptor.toMap(jobj);
		            String success= root.getString("success");
		            if("true".equals(success)){
		            	CompositeMap result = root.getChild("result");
		            	if(result!=null){
		            		List records = new ArrayList();
		            		List list = result.getChilds();
		            		if(list!=null){
		            			Iterator it = list.iterator();
		            			while(it.hasNext()){
		            				CompositeMap record = (CompositeMap)it.next();
		            				String error = record.getString("err");
		            				if(error==null){
		            					records.add(record);
		            				}
		            			}
		            		}
		            		
		            		model.addChilds(records);
		            	}
		            	if("true".equals(debugger)) {
							LoggingContext.getLogger(context,GpsTracker.class.getCanonicalName()).log(model.toXML());
						}
		            }else {
		            	throw new Exception("GPS Error: " + root.getString("message"));            	
		            }
		            
		            
				}
			} finally {
		        httpclient.getConnectionManager().shutdown();  
		    }  
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Parameter[] getParameters() {
		return parameters;
	}

	public void setParameters(Parameter[] parameters) {
		this.parameters = parameters;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDebugger() {
		return debugger;
	}

	public void setDebugger(String debugger) {
		this.debugger = debugger;
	}
}

