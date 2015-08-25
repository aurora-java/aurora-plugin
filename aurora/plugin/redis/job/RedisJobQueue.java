package aurora.plugin.redis.job;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import redis.clients.jedis.Jedis;

public class RedisJobQueue {
	
	ConcurrentLinkedQueue<RedisJob> 		queue;
	List<RedisJobThread>					threads;

	public RedisJobQueue() {
		 queue = new ConcurrentLinkedQueue<RedisJob>();
		 threads = new LinkedList<RedisJobThread>();
	}
	
	public void submit(RedisJob job){
		queue.add(job);
	}
	
	public void addThread( RedisJobThread thread ){
		thread.setJobQueue(queue);
		threads.add(thread);
	}
	
	public void start(){
		for(RedisJobThread thread:threads)
			thread.start();
	}
	
	public void interrupt(){
		for(RedisJobThread thread:threads){
			try{
				thread.interrupt();
			}catch(Throwable thr){
				thr.printStackTrace();
			}
		}
	}
	
	public void shutdown(){
		for(RedisJobThread thread:threads){
			Jedis jedis = thread.getJedis();
			if(jedis!=null&&jedis.isConnected())
				try{
					jedis.close();
				}catch(Throwable thr){
					thr.printStackTrace();
				}
		}
	}

}
