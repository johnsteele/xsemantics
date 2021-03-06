/*
 * generated by Xtext
 */
package it.xsemantics.dsl.ui.contentassist;

import it.xsemantics.dsl.services.XsemanticsProposalsForDatatypeRules;
import it.xsemantics.dsl.xsemantics.XsemanticsPackage;
import it.xsemantics.runtime.XsemanticsRuntimeSystem;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.Assignment;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.xtext.ui.ITypesProposalProvider;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;

import com.google.inject.Inject;

/**
 * see
 * http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on
 * how to customize content assistant
 */
public class XsemanticsProposalProvider extends
		AbstractXsemanticsProposalProvider {

	@Inject
	private XsemanticsProposalsForDatatypeRules proposalsForDatatypeRules;

	@Inject
	private ITypesProposalProvider typeProposalProvider;

	@Inject
	private IJvmTypeProvider.Factory typeProviderFactory;

	@Override
	public void complete_JudgmentSymbol(EObject model, RuleCall ruleCall,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		for (String symbol : proposalsForDatatypeRules.judgmentSymbols()) {
			acceptor.accept(createCompletionProposal(symbol, context));
		}
	}

	@Override
	public void complete_RelationSymbol(EObject model, RuleCall ruleCall,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		for (String symbol : proposalsForDatatypeRules.relationSymbols()) {
			acceptor.accept(createCompletionProposal(symbol, context));
		}
	}

	@Override
	public void completeXsemanticsSystem_ValidatorExtends(EObject model,
			Assignment assignment, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		// we show only subtypes of AbstractDeclarativeValidator
		showOnlySubtypesOf(model, context, acceptor,
				AbstractDeclarativeValidator.class,
				XsemanticsPackage.Literals.XSEMANTICS_SYSTEM__VALIDATOR_EXTENDS);
	}

	@Override
	public void completeXsemanticsSystem_SuperSystem(EObject model,
			Assignment assignment, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		// we show only subtypes of XsemanticsRuntimeSystem
		showOnlySubtypesOf(model, context, acceptor,
				XsemanticsRuntimeSystem.class,
				XsemanticsPackage.Literals.XSEMANTICS_SYSTEM__SUPER_SYSTEM);
	}

	protected void showOnlySubtypesOf(EObject model,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor,
			Class<?> superType, EReference reference) {
		JvmTypeReference superTypeRef = typesBuilder.newTypeRef(model, superType);
		if (superTypeRef != null) {
			typeProposalProvider.createSubTypeProposals(
				superTypeRef.getType(), this,
				context, reference, acceptor);
		}
	}
}
