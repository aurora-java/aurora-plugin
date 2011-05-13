/*
 * Created on 2007-6-27
 */
package aurora.plugin.quartz;

import org.quartz.JobDetail;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IConfigurable;

public class AuroraJobDetail extends JobDetail implements IConfigurable {
    
    Class           targetJobClass;
    String          method;
    CompositeMap    config;
    
    public AuroraJobDetail(){
        super();
    }
    
    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }
    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }
    /**
     * @return the targetJobClass
     */
    public Class getTargetJobClass() {
        return targetJobClass;
    }
    /**
     * @param targetJobClass the targetJobClass to set
     */
    public void setTargetJobClass(Class targetJobClass) {
        this.targetJobClass = targetJobClass;
    }
    /* (non-Javadoc)
     * @see org.quartz.JobDetail#setJobClass(java.lang.Class)
     */
    public void setJobClass(Class jobClass) {        
        super.setJobClass(AuroraJobRunner.class);
        targetJobClass = jobClass;
    }
    
    public CompositeMap getConfig(){
        return config;
    }


    public void beginConfigure(CompositeMap config) {
        this.config = config;
    }

    public void endConfigure() {
    }

}
