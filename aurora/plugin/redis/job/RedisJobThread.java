package aurora.plugin.redis.job;

import java.lang.reflect.InvocationTargetException;
import java.util.Queue;

import redis.clients.jedis.Jedis;

public class RedisJobThread extends Thread {
	
	Jedis	jedis;
	Queue<RedisJob>	jobQueue;

	public RedisJobThread() {
		super();
	}

	public RedisJobThread(Runnable target) {
		super(target);
		// TODO Auto-generated constructor stub
	}

	public RedisJobThread(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public RedisJobThread(ThreadGroup group, Runnable target) {
		super(group, target);
		// TODO Auto-generated constructor stub
	}

	public RedisJobThread(ThreadGroup group, String name) {
		super(group, name);
		// TODO Auto-generated constructor stub
	}

	public RedisJobThread(Runnable target, String name) {
		super(target, name);
		// TODO Auto-generated constructor stub
	}

	public RedisJobThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
		// TODO Auto-generated constructor stub
	}

	public RedisJobThread(ThreadGroup group, Runnable target, String name, long stackSize) {
		super(group, target, name, stackSize);
		// TODO Auto-generated constructor stub
	}
	
	public void run(){
		while(!interrupted()){
			RedisJob job = jobQueue.poll();
			if(job==null){
				try{
					sleep(100);
				}catch(InterruptedException ex){
					break;
				}
				continue;
			}
			int len = job.args==null?1:job.args.length+1;
			Object[] args = new Object[len];
			args[0]=jedis;
			if(job.args!=null)
				for(int i=0; i<job.args.length; i++)
					args[i+1] = job.args[i];
			//System.out.println("To invoke "+job.method.getName());
			try{
				job.method.invoke(job.instance, args);
			}catch(InvocationTargetException ex){
				ex.printStackTrace();
			}catch(IllegalAccessException ex){
				ex.printStackTrace();
			}
		}
	}

	public Jedis getJedis() {
		return jedis;
	}

	public void setJedis(Jedis jedis) {
		this.jedis = jedis;
	}

	public Queue<RedisJob> getJobQueue() {
		return jobQueue;
	}

	public void setJobQueue(Queue<RedisJob> jobQueue) {
		this.jobQueue = jobQueue;
	}

}
