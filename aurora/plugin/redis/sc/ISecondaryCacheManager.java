package aurora.plugin.redis.sc;

import java.util.Collection;

public interface ISecondaryCacheManager {

	ISecondaryCache getCache(String name);

	void addCache(ISecondaryCache cache);

	void addCaches(Collection<ISecondaryCache> cache_list);

	void fullLoad();

	void insert(String data_name, String primary_key, Object inserted_data);

	void update(String data_name, String primary_key, Object updated_data);

	void delete(String data_name, String primary_key, Object deleted_data);

	String getMode();

}