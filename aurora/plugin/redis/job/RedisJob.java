package aurora.plugin.redis.job;

import java.lang.reflect.Method;

public class RedisJob {

	Object		instance;
	Method		method;
	Object[]	args;

	public RedisJob(Object instance, Method method, Object[] args) {
		super();
		this.instance = instance;
		this.method = method;
		this.args = args;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	
}
