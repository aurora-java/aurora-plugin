package aurora.plugin.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

public class RedisUtil {

	static RedisUtil DEFAULT_INSTANCE = new RedisUtil();

	public static RedisUtil getInstance() {
		return DEFAULT_INSTANCE;
	}

	ObjectMapper mapper;

	public RedisUtil(ObjectMapper mapper) {
		super();
		this.mapper = mapper;
	}

	public RedisUtil() {
		mapper = new ObjectMapper();
	}

	public void save(Jedis conn, String key, Object obj) {
		if (obj instanceof Map) {
			conn.hmset(key, (Map) obj);
		} else {
			Map result = (Map) mapper.convertValue(obj, Map.class);
			Map<String, String> to_save = new HashMap<String, String>();
			for (Object o : result.entrySet()) {
				Map.Entry entry = (Map.Entry) o;
				if (entry.getKey() != null)
					to_save.put(entry.getKey().toString(),
							entry.getValue() == null ? null : entry.getValue().toString());
			}
			conn.hmset(key, to_save);
		}

	}

	public Object load(Jedis conn, String key, Class type) {
		Map map = conn.hgetAll(key);
		if (map == null)
			return null;
		return mapper.convertValue(map, type);
	}

	public void lpushJson(Jedis conn, String key, List list) {
		saveListAsJson(conn, key, list, true);
	}

	public void pushObjectAsJson(Jedis conn, String key, Object obj, boolean from_left) {
		try {
			String json = mapper.writeValueAsString(obj);
			if (from_left)
				conn.lpush(key, json);
			else
				conn.rpush(key, json);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void lpushObjectAsJson(Jedis conn, String key, Object obj) {
		pushObjectAsJson(conn, key, obj, true);
	}

	public void rpushObjectAsJson(Jedis conn, String key, Object obj) {
		pushObjectAsJson(conn, key, obj, false);
	}

	public void rpushJson(Jedis conn, String key, List list) {
		saveListAsJson(conn, key, list, false);
	}

	public void saveListAsJson(Jedis conn, String key, List list, boolean from_left) {
		try {
			String[] array = new String[list.size()];
			int id = 0;
			for (Iterator it = list.iterator(); it.hasNext();) {
				Object obj = it.next();
				array[id] = mapper.writeValueAsString(obj);
				id++;
			}
			if (from_left)
				conn.lpush(key, array);
			else
				conn.rpush(key, array);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public List getObjectListFromJson(Jedis conn, String key, Class<?> object_type, int start, int end) {
		try {
			List<String> result = conn.lrange(key, start, end);
			if (result == null || result.size() == 0)
				return null;
			List rlst = new ArrayList(result.size());
			int id = 0;
			for (String str : result) {
				Object value = mapper.readValue(str, object_type);
				rlst.add(id, value);
				id++;
			}
			return rlst;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

	}
	
	public List getObjectListFromJsonAll(Jedis conn, String key, Class<?> object_type) {
		return getObjectListFromJson(conn,key,object_type,0,-1);
	}
	

}
