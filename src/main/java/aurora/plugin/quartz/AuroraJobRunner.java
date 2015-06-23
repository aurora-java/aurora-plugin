package aurora.plugin.quartz;

import java.lang.reflect.Method;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import uncertain.core.UncertainEngine;
import uncertain.ocm.IObjectRegistry;
import uncertain.ocm.OCManager;

public class AuroraJobRunner implements Job {

    public static final String DEFAULT_METHOD_NAME = "run";

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
    	IObjectRegistry os = SchedulerConfig.getObjectRegistry(context.getJobDetail().getJobDataMap());
        AuroraJobDetail detail = (AuroraJobDetail) context.getJobDetail();
        Class cls_to_run = detail.getTargetJobClass();
        String method = detail.getMethod();
        if(method==null) method=DEFAULT_METHOD_NAME;
        try{
            // Get method to run
            Method[] marray = cls_to_run.getMethods();
            Method job_method = null;
            for(int i=0; i<marray.length; i++)
                if(marray[i].getName().equals(method)){
                    job_method=marray[i];
                    break;
                }
            if(job_method==null) 
                throw new JobExecutionException("'method' property not set in job detail config");
            // Try to construct all parameters 
            Class[] types = job_method.getParameterTypes();
            Object[] args = new Object[types.length];
            for(int i=0; i<types.length; i++){
                if(JobExecutionContext.class.equals(types[i]))
                    args[i] = context;
                else    
                    args[i] = os.getInstanceOfType(types[i]);
            }
            UncertainEngine engine = (UncertainEngine)os.getInstanceOfType(UncertainEngine.class);
            Object instance = engine.getObjectCreator().createInstance(cls_to_run);
            OCManager om = engine.getOcManager();
            om.populateObject(detail.getConfig(), instance);
            job_method.invoke(instance, args);
        }catch(Exception ex){
            throw new JobExecutionException(ex);
        }
    }


}
