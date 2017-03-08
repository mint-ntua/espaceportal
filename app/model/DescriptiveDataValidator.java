package model;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import model.basicDataTypes.Language;
import model.basicDataTypes.MultiLiteral;

public class DescriptiveDataValidator implements ConstraintValidator<DescriptiveDataAnnotation, DescriptiveData>{

	@Override
	public void initialize(DescriptiveDataAnnotation arg0) {
	}

	@Override
	public boolean isValid(DescriptiveData dd, ConstraintValidatorContext arg1) {
		MultiLiteral label = dd.getLabel();
        MultiLiteral description = dd.getDescription();
        if (label == null || label.isEmpty() || label.get(Language.DEFAULT) == null || 
        		label.get(Language.DEFAULT).isEmpty() || label.get(Language.DEFAULT).get(0).isEmpty()) 
        	if (description == null || description.isEmpty() || description.get(Language.DEFAULT) == null ||
        	  description.get(Language.DEFAULT).isEmpty() || description.get(Language.DEFAULT).get(0).isEmpty())
        		return false;
        return true;
	}
}
