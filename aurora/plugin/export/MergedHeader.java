package aurora.plugin.export;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uncertain.composite.CompositeMap;

public class MergedHeader {
	final static String KEY_LEVEL="level";
	final static String KEY_COUNT="count";
	public CompositeMap conifg;
	public MergedHeader(CompositeMap headerConfig){
		this.conifg=headerConfig;
		parseColumn(headerConfig);
	}
	
	static Map<String,Integer> parseColumn(CompositeMap config){
		int count=0;
		int level=0;
		Map<String,Integer> returnMap=new HashMap<String,Integer>();  
		List list=config.getChilds();				
		if(list!=null){			
			int length=list.size();
			for(int i=0;i<length;i++){
				Map<String,Integer> map=null;
				CompositeMap child = (CompositeMap)list.get(i);
				Iterator childIterator=child.getChildIterator();
				if(childIterator!=null){					
					while(childIterator.hasNext()){
						CompositeMap column=(CompositeMap)childIterator.next();
						map=parseColumn(column);
						level=map.get(KEY_LEVEL);
					}
					level++;
					for(int l=0;l<=i;l++){
						((CompositeMap)list.get(l)).putLong("_level", level);
					}					
				}else{					
					child.putLong("_level", level);
				}
				if(map!=null)				
					count+=map.get(KEY_COUNT);
				else
					count++;				
			}
		}
		config.putLong("_count", count);
		returnMap.put(KEY_LEVEL, level);
		returnMap.put(KEY_COUNT, count);
		return returnMap;
	}
}
