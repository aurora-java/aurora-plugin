/*
 * Created on 2015年6月26日 下午1:50:36
 * $Id$
 */
package aurora.plugin.redis.pipe;

import uncertain.pipe.base.IProcessor;

public class DataPrinter implements IProcessor {
    
    public DataPrinter(){
    }

    public Object process(Object source){
        System.out.println("result from pipe:"+source);
        return source;
    };

}
