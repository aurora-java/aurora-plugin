package aurora.plugin.export.word;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import uncertain.core.IGlobalInstance;
import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;

public class WordTemplateProvider implements IGlobalInstance{
	
	public static final String DEFAULT_ENCODING = "UTF-8";
	
	private Configuration freemarkerConfiguration;
	
	private String defaultEncoding;
	
	private UncertainEngine uncertainEngine;
	private IObjectRegistry registry;
	
	public WordTemplateProvider(IObjectRegistry rgt,UncertainEngine uncertainEngine) {
		this.registry = rgt;
		this.uncertainEngine = uncertainEngine;
	}
	
	public Configuration getFreeMarkerConfiguration(){
		return freemarkerConfiguration;
	}
	
	
	public void onInitialize() throws Exception {
		registry.registerInstance(WordTemplateProvider.class, this);
		freemarkerConfiguration = new Configuration();
		freemarkerConfiguration.setDefaultEncoding(getDefaultEncoding());
		freemarkerConfiguration.setOutputEncoding(getDefaultEncoding());
		freemarkerConfiguration.setNumberFormat("#");
		freemarkerConfiguration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
		freemarkerConfiguration.setDirectoryForTemplateLoading(uncertainEngine.getConfigDirectory());
	}


	public String getDefaultEncoding() {
		return defaultEncoding == null ? DEFAULT_ENCODING : defaultEncoding;
	}


	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}
}
