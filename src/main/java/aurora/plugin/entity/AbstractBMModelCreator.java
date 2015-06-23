package aurora.plugin.entity;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import aurora.database.service.BusinessModelService;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.entity.model.BMModel;
import aurora.plugin.source.gen.screen.model.asm.PageGenerator;

public abstract class AbstractBMModelCreator {
	private DatabaseServiceFactory svcFactory;
	private CompositeMap context;
	protected EntityGeneratorConfig config = EntityGeneratorConfig
			.getInstance();

	public AbstractBMModelCreator(DatabaseServiceFactory svcFactory,
			CompositeMap context) {
		super();
		this.svcFactory = svcFactory;
		this.context = context;
	}

	public abstract BMModel create(CompositeMap entityMap) throws Exception;

	public abstract void updateBack() throws Exception;

	protected void updateEntity(CompositeMap entityMap) throws Exception {
		BusinessModelService service = getDatabaseServiceFactory()
				.getModelService(config.entityModel, getContext());
		service.updateByPK(entityMap);
	}

	public CompositeMap getContext() {
		return context;
	}

	public DatabaseServiceFactory getDatabaseServiceFactory() {
		return svcFactory;
	}

	protected CompositeMap getEntity(Object entityId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("entity_id", entityId);
		return PageGenerator.queryFirst(getDatabaseServiceFactory(),
				getContext(), config.entityModel, para);

	}

	protected String getEntityName(CompositeMap entityMap) {
		return TextParser.parse(config.getEntityNamePattern(), entityMap);
	}

	protected String getExtEntityName(CompositeMap entityMap) {
		return TextParser.parse(config.getExtEntityNamePattern(), entityMap);
	}
}
