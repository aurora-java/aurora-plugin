package aurora.plugin.redis.sc;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import uncertain.ocm.OCManager;

public class CollectionData extends MapData {

	/*
	public CollectionData(OCManager mapper) throws Exception {
		super(mapper);
		this.collectionType = Collection.class;
	}
	*/
	
	public CollectionData(){
		super();
	}

	@Override
	public Object getProcessedData() {
		if(Collection.class.equals(collectionType))
			return super.dataMap.values();
		else{
			try{
				List lst = (List)collectionType.newInstance();
				lst.addAll(dataMap.values());
				return lst;
			}catch(Exception ex){
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public Class getBaseDataType() {
		return Collection.class;
	}

	@Override
	public void start() {
		try{
			dataMap = new HashMap<String, Object>();
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
	
	
	

}
