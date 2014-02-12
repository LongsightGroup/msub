/**
 * 
 */
package org.theospi.portfolio.presentation.support;

public class CreatePresentationCommand {
	private String presentationType;
	private String templateId;
	
	public CreatePresentationCommand() {}
	
	public String getPresentationType() {
		return presentationType;
	}
	public void setPresentationType(String presentationType) {
		this.presentationType = presentationType;
	}
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
}