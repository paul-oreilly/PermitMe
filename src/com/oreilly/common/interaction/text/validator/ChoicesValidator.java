package com.oreilly.common.interaction.text.validator;

import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import com.oreilly.common.interaction.text.InteractionPage;
import com.oreilly.common.interaction.text.interfaces.Choices;
import com.oreilly.common.interaction.text.validator.error.InterfaceDependencyError;
import com.oreilly.common.interaction.text.validator.error.ValidationFailedError;


public class ChoicesValidator extends Validator {
	
	@Override
	protected void validate( Object object, InteractionPage page ) throws ValidationFailedError,
			InterfaceDependencyError {
		if ( page instanceof Choices ) {
			HashSet< String > choiceList = ( (Choices)page ).getChoices();
			if ( choiceList.contains( object ) )
				return;
			throw new ValidationFailedError( this, "Input must be one of: " + StringUtils.join( choiceList, ", " ) );
		}
		else
			throw new InterfaceDependencyError( Choices.class.toString() );
	}
	
}
