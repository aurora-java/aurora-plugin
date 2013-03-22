package aurora.plugin.source.gen.builders;

import aurora.plugin.source.gen.BuilderSession;

public interface ISourceBuilder {
	String Default_Namespace = "http://www.aurora-framework.org/application";
	String Default_prefix = "a";

	void buildContext(BuilderSession session);

	void actionEvent(String event, BuilderSession session);
}
