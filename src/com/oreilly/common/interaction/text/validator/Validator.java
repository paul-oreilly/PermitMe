package com.oreilly.common.interaction.text.validator;

import com.oreilly.common.interaction.text.InteractionPage;
import com.oreilly.common.interaction.text.validator.error.InterfaceDependencyError;
import com.oreilly.common.interaction.text.validator.error.ValidationFailedError;


abstract public class Validator {
	
	public Validator nextInChain = null;
	
	
	// chained init functions...
	
	public Validator chain( Validator validator ) {
		if ( nextInChain == null )
			this.nextInChain = validator;
		else
			nextInChain.chain( validator );
		return this;
	}
	
	
	public void startValidation( Object object, InteractionPage page ) throws ValidationFailedError,
			InterfaceDependencyError {
		validate( object, page );
		if ( nextInChain != null )
			nextInChain.startValidation( object, page );
	}
	
	
	abstract protected void validate( Object object, InteractionPage page ) throws ValidationFailedError,
			InterfaceDependencyError;
	
}
