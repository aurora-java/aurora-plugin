package aurora.plugin.source.gen;

import java.util.Collection;

import aurora.plugin.source.gen.builders.ISourceBuilder;
import uncertain.composite.CompositeMap;
import uncertain.composite.IterationHandle;

public class BuilderSession {
	private SourceGenManager sourceGenManager;

	private StringBuilder eventResult = new StringBuilder();
	private IDGenerator idg = new IDGenerator();

	public BuilderSession(SourceGenManager sourceGenManager) {
		super();
		this.sourceGenManager = sourceGenManager;
	}

	private CompositeMap model;

	private CompositeMap context;

	private CompositeMap current_context;

	private CompositeMap current_model;

	public SourceGenManager getSourceGenManager() {
		return sourceGenManager;
	}

	public void setSourceGenManager(SourceGenManager sourceGenManager) {
		this.sourceGenManager = sourceGenManager;
	}

	public CompositeMap getContext() {
		return context;
	}

	public void setContext(CompositeMap context) {
		this.context = context;
	}

	public CompositeMap getModel() {
		return model;
	}

	public void setModel(CompositeMap model) {
		this.model = model;
	}

	public CompositeMap getCurrentModel() {
		return current_model;
	}

	public void setCurrentModel(CompositeMap current_model) {
		this.current_model = current_model;
	}

	public void appendContext(CompositeMap context) {
		CompositeMap parent = this.getCurrentModel().getParent();
		if (parent == null) {
			this.context = context;
		} else {
			String modelId = parent.getString("markid", "");
			if ("".equals(modelId) == false) {
				CompositeMap parentContext = lookUpParentContext(modelId);
				if (parentContext != null) {
					parentContext.addChild(context);
				}
			}
		}
		this.setCurrentContext(context);
	}

	private CompositeMap lookUpParentContext(final String modelId) {
		if (this.current_context != null) {
			String c_id = current_context.getString("markid", "");
			if (modelId.equals(c_id)) {
				return current_context;
			}
		}
		final CompositeMap[] maps = new CompositeMap[1];
		this.context.iterate(new IterationHandle() {
			@Override
			public int process(CompositeMap map) {
				String c_id = map.getString("markid", "");
				if (modelId.equals(c_id)) {
					maps[0] = map;
					return IterationHandle.IT_BREAK;
				}
				return IterationHandle.IT_CONTINUE;
			}
		}, true);
		return maps[0];
	}

	public CompositeMap getCurrentContext() {
		return current_context;
	}

	public void setCurrentContext(CompositeMap current_context) {
		this.current_context = current_context;
	}

	public String buildComponent(CompositeMap model) {
		return this.sourceGenManager.buildComponent(this,model);
	}

	public void appendResult(String result) {
		getEventResult().append(result);
		getEventResult().append("\n");
	}

	public void clearEventResult() {
		setEventResult(new StringBuilder());
	}

	public StringBuilder getEventResult() {
		return eventResult;
	}

	public void setEventResult(StringBuilder eventResult) {
		this.eventResult = eventResult;
	}

	public BuilderSession getCopy() {
		BuilderSession bs = new BuilderSession(this.sourceGenManager);
		bs.setContext(this.getContext());
		bs.setCurrentContext(this.getCurrentContext());
		bs.setCurrentModel(this.getCurrentModel());
		bs.setModel(this.getModel());
		bs.setIdg(idg);
		return bs;
	}

	public IDGenerator getIDGenerator() {
		return idg;
	}

	public void setIdg(IDGenerator idg) {
		this.idg = idg;
	}
	
	public Object execActionEvent(String event, CompositeMap context) {
		StringBuilder sb = new StringBuilder();
		Collection<String> values =sourceGenManager.getBuilders().values();
		for (String clazz : values) {
			ISourceBuilder newInstance = sourceGenManager.createNewInstance(clazz);
			if (newInstance != null) {
				BuilderSession bs = this.getCopy();
				((ISourceBuilder) newInstance).actionEvent(event, bs);
				sb.append(bs.getEventResult());
			}
		}
		return sb.toString();
	}
}
