/*
 * generated by Xtext
 */
package it.xsemantics.example.fj;

import it.xsemantics.example.fj.typing.FjStringRepresentation;
import it.xsemantics.runtime.StringRepresentation;


/**
 * Use this class to register components to be used within the IDE.
 */
public class FJRuntimeModule extends it.xsemantics.example.fj.AbstractFJRuntimeModule {

	/*
	@Override
	public Class<? extends ILinkingService> bindILinkingService() {
		return FJLinkingService.class;
	}
	*/

//	@Override
//	public Class<? extends XtextResource> bindXtextResource() {
//		return FJLinkingResource.class;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.xtext.service.DefaultRuntimeModule#bindIResourceFactory()
//	 */
//	public Class<? extends XtextResourceFactory> bindXtextResourceFactory() {
//		  return FJResourceFactory.class;
//	}

	/*
	@Override
	public Class<? extends ITransientValueService> bindITransientValueService() {
		// TODO Auto-generated method stub
		return FJTransientValueService.class;
	}
	*/
	
	public Class<? extends StringRepresentation> bindStringRepresentation() {
		return FjStringRepresentation.class;
	}
}
