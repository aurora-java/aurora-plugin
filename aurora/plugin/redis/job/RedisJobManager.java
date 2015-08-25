package aurora.plugin.redis.job;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import aurora.plugin.redis.IRedisConnectionFactory;
import redis.clients.jedis.Jedis;

public class RedisJobManager {
	
	String name;
	IRedisConnectionFactory 			connFactory;
	Map<String,RedisJobQueue>			taskQueueMap;
	ThreadGroup	threadGroup;

	public RedisJobManager(String name, IRedisConnectionFactory connFactory) {
		this.connFactory = connFactory;
		this.name = name;
		taskQueueMap = new ConcurrentHashMap<String,RedisJobQueue>();
		threadGroup = new ThreadGroup(name);
	}
	
	public RedisJobQueue createQueue(String queue_name, int thread_count, String conn_name ){
		if(taskQueueMap.containsKey(queue_name))
			throw new IllegalArgumentException(queue_name+" exists");
		RedisJobQueue queue = new RedisJobQueue();
		for(int i=0; i<thread_count; i++){
			String name = String.format("TaskQueue[%s].worker.%d", queue_name,i);
			RedisJobThread thread = new RedisJobThread(threadGroup,name);
			Jedis conn = connFactory.getConnection(conn_name);
			thread.setJedis(conn);
			queue.addThread(thread);
		}
		taskQueueMap.put(queue_name, queue);
		queue.start();
		System.out.println("Redis Job queue "+name+"["+queue_name+"] started");
		return queue;
	}
	
	public RedisJobQueue getQueue(String name){
		return taskQueueMap.get(name);
	}
	
	public void removeQueue( String queue_name ){
		taskQueueMap.remove(queue_name);
	}
	
	public void submit( String queue, Object instance, Method method, Object[] args){
		RedisJobQueue jobQueue = getQueue(queue);
		if(jobQueue==null)
			throw new IllegalArgumentException("RedisJobQueue "+queue+" does not exist");
		RedisJob job = new RedisJob(instance,method,args);
		jobQueue.submit(job);
	}
	
	public void shutdown(){
		for(RedisJobQueue queue:taskQueueMap.values()){
			try{
				queue.shutdown();
			}catch(Throwable thr){
				thr.printStackTrace();
			}
		}
	}

}
