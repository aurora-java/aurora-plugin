package aurora.plugin.sap;
import java.util.logging.Logger;

import uncertain.core.IGlobalInstance;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
public class SapInstance implements IGlobalInstance {
    
    public static final String LOGGING_TOPIC = "aurora.plugin.sap";
    
    public String SID;
    public String USERID;
    public String PASSWORD;
    public String SERVER_IP;
    public String DEFAULT_LANG;
    public int    MAX_CONN;
    public String SAP_CLIENT;
    public String SYSTEM_NUMBER;
    
    Logger        logger;
    
    IRepository     repository;
    
    private boolean inited = false;
    
    public SapInstance(){
        
    }
    
    public SapInstance(Logger logger){
        this.logger = logger;
    }
    
    public void prepare(){
        if(!inited){
            JCO.addClientPool(
                    SID,          // Alias for this pool
                    MAX_CONN,     // Max. number of connections
                    SAP_CLIENT,   // SAP client
                    USERID,       // userid
                    PASSWORD,     // password
                    DEFAULT_LANG,                     // language
                    SERVER_IP,    // host name
                   SYSTEM_NUMBER );
            repository = JCO.createRepository("MYRepository", SID);            
            if(logger!=null) logger.info("SAP connection pool "+SID+" created");
            inited = true;
        }
    }
    
    public IRepository getRepository(){
        if(!inited)
            prepare();
        if(repository==null) throw new RuntimeException("SAP connection pool can't be created");
        return repository;
    }
    
    public void release(){
        JCO.removeClientPool(SID);
        if(logger!=null) logger.info("SAP connection pool "+SID+" released");
        inited = false;
    }
    
    //private boolean inited = false;
    public JCO.Client getClient() {
        if(!inited)
            prepare();
        JCO.Client client = JCO.getClientPoolManager().getClient(SID);        
        return client;
    }
    
    public void onShutdown(){
        release();
    }

}

