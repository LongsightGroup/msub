package org.theospi.portfolio.presentation.support;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.theospi.portfolio.presentation.model.Presentation;

public class UpdatePresentationValidator implements Validator {

	public boolean supports(Class clazz) {
		return (clazz.equals(Presentation.class));
	}

	public void validate(Object obj, Errors errors) {
		Presentation presentation = (Presentation) obj;
		if (presentation.getId() == null || "".equals(presentation.getId().getValue()))
			errors.rejectValue("id", "error.required", "Portfolio ID required");
		
		if (presentation.getName() != null && "".equals(presentation.getName()))
			errors.rejectValue("name", "error.required", "Portfolio Name required");
		
		if (presentation.getDescription() != null && presentation.getDescription().length() > 255)
			errors.rejectValue("description", "error.lengthExceeded", new Object[]{"255"}, "Description must be less than {0} characters");
	}

}
