/*
 * Created on 2006-11-25
 */
package aurora.plugin.quartz;

public class JobInstance {
    
    String  jobName;
    String  jobGroup;
    String  triggerName;
    String  triggerGroup;
    /**
     * @return the jobName
     */
    public String getJobName() {
        return jobName;
    }
    /**
     * @param jobName the jobName to set
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    /**
     * @return the triggerName
     */
    public String getTriggerName() {
        return triggerName;
    }
    /**
     * @param triggerName the triggerName to set
     */
    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }
    /**
     * @return the jobGroup
     */
    public String getJobGroup() {
        return jobGroup;
    }
    /**
     * @param jobGroup the jobGroup to set
     */
    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }
    /**
     * @return the triggerGroup
     */
    public String getTriggerGroup() {
        return triggerGroup;
    }
    /**
     * @param triggerGroup the triggerGroup to set
     */
    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }
    

}
