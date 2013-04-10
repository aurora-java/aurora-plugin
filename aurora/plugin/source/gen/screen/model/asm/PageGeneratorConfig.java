package aurora.plugin.source.gen.screen.model.asm;

import aurora.plugin.source.gen.screen.model.asm.PageGeneratorConfig;

import aurora.plugin.source.gen.screen.model.asm.PageGeneratorConfig;

public class PageGeneratorConfig {
	private static PageGeneratorConfig instance;
	String pagePath = "custom";// modules/custom
	String pageNamePattern = "custom_page${@custom_page_id}";
	String pageModel = "page.custom_page";
	String pagePartModel = "page.custom_page_structure";
	String formModel = "page.custom_form";
	String gridModel = "page.custom_grid";

	private PageGeneratorConfig() {
		super();
		instance = this;
	}

	public static PageGeneratorConfig getInstance() {
		if (instance == null) {
			new PageGeneratorConfig();
		}
		return instance;
	}

	public String getPagePath() {
		return pagePath;
	}

	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}

	public String getPageNamePattern() {
		return pageNamePattern;
	}

	public void setPageNamePattern(String pageNamePattern) {
		this.pageNamePattern = pageNamePattern;
	}

	public String getPageModel() {
		return pageModel;
	}

	public void setPageModel(String pageModel) {
		this.pageModel = pageModel;
	}

	public String getPagePartModel() {
		return pagePartModel;
	}

	public void setPagePartModel(String pagePartModel) {
		this.pagePartModel = pagePartModel;
	}

	public String getFormModel() {
		return formModel;
	}

	public void setFormModel(String formModel) {
		this.formModel = formModel;
	}

	public String getGridModel() {
		return gridModel;
	}

	public void setGridModel(String gridModel) {
		this.gridModel = gridModel;
	}
	
}
