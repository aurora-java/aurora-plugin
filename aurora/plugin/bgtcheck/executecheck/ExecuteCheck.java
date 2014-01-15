package aurora.plugin.bgtcheck.executecheck;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.AbstractEntry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import uncertain.proc.ProcedureRunner;
import aurora.plugin.bgtcheck.DatabaseTool;
import aurora.service.IServiceFactory;
import aurora.service.ServiceInvoker;
import aurora.service.ServiceThreadLocal;

public class ExecuteCheck extends AbstractEntry {

	private IObjectRegistry mRegistry;

	private ILogger logger;
	private DataBaseActions dbActions;
	private IProcedureManager procedureManager;
	private IServiceFactory serviceFactory;

	private String executeProcPath;
	private String batchIdPath;
	private int batch_id;

	public ExecuteCheck(IObjectRegistry registry) throws IOException {
		this.mRegistry = registry;
		logger = LoggingContext.getLogger(this.getClass().getPackage().getName(), mRegistry);
		procedureManager = (IProcedureManager) mRegistry.getInstanceOfType(IProcedureManager.class);
		if (procedureManager == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, IProcedureManager.class, this.getClass().getName());
		serviceFactory = (IServiceFactory) mRegistry.getInstanceOfType(IServiceFactory.class);
		if (serviceFactory == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(this, IServiceFactory.class, this.getClass().getName());
	}

	@Override
	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		executeProcPath = TextParser.parse(executeProcPath, context);
		batchIdPath = TextParser.parse(batchIdPath, context);
		if(batchIdPath != null && !"".equals(batchIdPath))
			batch_id = Integer.valueOf(batchIdPath);
		if (executeProcPath == null || "".equals(executeProcPath))
			throw BuiltinExceptionFactory.createAttributeMissing(this, "sqlFileRootPath");
		dbActions = new DataBaseActions(mRegistry, logger);
		execute(context);
	}

	private void execute(CompositeMap context) throws Exception {
		CompositeMap hierarchyBatch = getHierarchyBatch();
		List<CompositeMap> batchList = hierarchyBatch.getChilds();
		if (batchList == null)
			throw new IllegalStateException("Can not find batch records!");
		for (CompositeMap batch : batchList) {
			executeCheck(batch, context);
		}
	}

	private void executeCheck(CompositeMap batch, CompositeMap context) throws Exception {
		int batch_id = batch.getInt("batch_id");
		CompositeMap batchTables = dbActions.getBatchTables(batch_id);
		List<CompositeMap> recordList = batchTables.getChilds();
		if (recordList != null) {
			for (CompositeMap record : recordList) {
				String table_name = record.getString("table_name");
				dbActions.deleteTable(table_name);
				dbActions.copyDataToTable(batch_id, table_name);
			}
		}
		List<CompositeMap> subBatchList = batch.getChilds();
		if (subBatchList == null || subBatchList.size() == 0) {
			executeProc(executeProcPath, batch_id);
		} else {
			for (CompositeMap subBatch : subBatchList) {
				executeCheck(subBatch, context);
			}
		}
	}

	protected void executeProc(String procedure_name, int batch_id) {
		logger.log(Level.CONFIG, "load procedure:{0}", new Object[] { procedure_name });
		Procedure proc = procedureManager.loadProcedure(procedure_name);
		executeProc(batch_id, proc);
	}

	protected void executeProc(int batch_id, Procedure proc) {
		if (proc == null)
			throw new IllegalArgumentException("Procedure can not be null!");
		try {
			CompositeMap context = ServiceThreadLocal.getCurrentThreadContext();
			CompositeMap fakeContext = new CompositeMap("context");
			fakeContext.addChild(context.getChild("session"));
			fakeContext.addChild(context.getChild("parameter"));
			String name = "check." + batch_id;
			fakeContext.putObject("/parameter/@batch_id", batch_id, true);
			// ServiceInvoker.invokeProcedureWithTransaction(name, proc,
			// serviceFactory, context);
			Connection connection = DatabaseTool.getContextConnection(mRegistry);
			connection.commit();
			ServiceInvoker.invokeProcedureWithTransaction(name, proc, serviceFactory, fakeContext, connection);
			// connection.commit();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private CompositeMap getHierarchyBatch() throws SQLException {
		CompositeMap hierarchyBatch = new CompositeMap();
		Map<Integer, CompositeMap> batch_map = new HashMap<Integer, CompositeMap>();
		CompositeMap batchRecords = null;
		if(batch_id >0 )
			batchRecords = dbActions.getBatchs(batch_id);
		else
			batchRecords = dbActions.getBatchs();
		if (batchRecords == null)
			throw new IllegalStateException("Can not find batch records!");
		List<CompositeMap> recordList = batchRecords.getChilds();
		if (recordList == null)
			throw new IllegalStateException("Can not find batch records!");
		for (CompositeMap record : recordList) {
			CompositeMap batch = null;
			int batch_id = record.getInt("batch_id");
			batch = batch_map.get(batch_id);
			if(batch == null){
				batch =new CompositeMap("batch");
				batch.put("batch_id", batch_id);
				batch_map.put(batch_id, batch);
			}
			int parent_batch_id = record.getInt("parent_batch_id");
			if (parent_batch_id > 0) {
				CompositeMap parentBatch = batch_map.get(parent_batch_id);
				if (parentBatch == null) {
					parentBatch = new CompositeMap("batch");
					parentBatch.put("batch_id", parent_batch_id);
					batch_map.put(parent_batch_id, parentBatch);
				}
				parentBatch.addChild(batch);
			} else {
				hierarchyBatch.addChild(batch);
			}
		}
		return hierarchyBatch.getRoot();
	}

	public String getExecuteProcPath() {
		return executeProcPath;
	}

	public void setExecuteProcPath(String executeProcPath) {
		this.executeProcPath = executeProcPath;
	}

	public String getBatchIdPath() {
		return batchIdPath;
	}

	public void setBatchIdPath(String batchIdPath) {
		this.batchIdPath = batchIdPath;
	}
}
