/*
 * generated by Xtext
 */
package it.xsemantics.dsl.ui.labeling;

import it.xsemantics.dsl.util.XsemanticsUtils;
import it.xsemantics.dsl.xsemantics.AuxiliaryDescription;
import it.xsemantics.dsl.xsemantics.AuxiliaryFunction;
import it.xsemantics.dsl.xsemantics.Axiom;
import it.xsemantics.dsl.xsemantics.CheckRule;
import it.xsemantics.dsl.xsemantics.Injected;
import it.xsemantics.dsl.xsemantics.JudgmentDescription;
import it.xsemantics.dsl.xsemantics.Rule;
import it.xsemantics.dsl.xsemantics.RuleWithPremises;
import it.xsemantics.dsl.xsemantics.XsemanticsSystem;

import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;
import org.eclipse.xtext.util.Strings;

import com.google.inject.Inject;

/**
 * Provides labels for a EObjects.
 * 
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#labelProvider
 */
public class XsemanticsLabelProvider extends DefaultEObjectLabelProvider {

	@Inject
	protected XsemanticsUtils utils;

	@Inject
	public XsemanticsLabelProvider(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	public String text(Rule rule) {
		JudgmentDescription judgmentDescription = utils.getJudgmentDescription(rule);
		return rule.getName()
				+ (judgmentDescription != null ? " (" + Strings.emptyIfNull(judgmentDescription.getName()) + ")" : "");
	}

	public String text(CheckRule rule) {
		return rule.getName();
	}

	public String text(AuxiliaryFunction aux) {
		AuxiliaryDescription desc = utils.getAuxiliaryDescription(aux);
		return aux.getName() + (desc != null ? " (" + Strings.emptyIfNull(desc.getName()) + ")" : "");
	}

	public String image(Axiom axiom) {
		return "axiom.gif";
	}

	public String image(RuleWithPremises rule) {
		return "rule.gif";
	}

	public String image(CheckRule rule) {
		return "checkrule.gif";
	}

	public String image(XsemanticsSystem ts) {
		return "system.gif";
	}

	public String image(JudgmentDescription desc) {
		return "judgment.gif";
	}

	public String image(AuxiliaryFunction aux) {
		return "auxfun.gif";
	}

	public String image(AuxiliaryDescription aux) {
		return "auxdesc.gif";
	}

	public String image(Injected inj) {
		return "field_public_obj.gif";
	}
}
