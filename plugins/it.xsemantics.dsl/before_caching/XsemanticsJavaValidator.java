package it.xsemantics.dsl.validation;

import it.xsemantics.dsl.typing.TupleType;
import it.xsemantics.dsl.typing.XsemanticsTypeSystem;
import it.xsemantics.dsl.util.XsemanticsNodeModelUtils;
import it.xsemantics.dsl.util.XsemanticsUtils;
import it.xsemantics.dsl.util.XsemanticsXExpressionHelper;
import it.xsemantics.dsl.xsemantics.AuxiliaryDescription;
import it.xsemantics.dsl.xsemantics.AuxiliaryFunction;
import it.xsemantics.dsl.xsemantics.CheckRule;
import it.xsemantics.dsl.xsemantics.Injected;
import it.xsemantics.dsl.xsemantics.InputParameter;
import it.xsemantics.dsl.xsemantics.JudgmentDescription;
import it.xsemantics.dsl.xsemantics.JudgmentParameter;
import it.xsemantics.dsl.xsemantics.Rule;
import it.xsemantics.dsl.xsemantics.RuleConclusion;
import it.xsemantics.dsl.xsemantics.RuleConclusionElement;
import it.xsemantics.dsl.xsemantics.RuleInvocation;
import it.xsemantics.dsl.xsemantics.RuleParameter;
import it.xsemantics.dsl.xsemantics.XsemanticsPackage;
import it.xsemantics.dsl.xsemantics.XsemanticsSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XbasePackage.Literals;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import com.google.inject.Inject;

public class XsemanticsJavaValidator extends AbstractXsemanticsValidator {

	@Inject
	protected XsemanticsTypeSystem typeSystem;

	@Inject
	protected XsemanticsUtils xsemanticsUtils;

	@Inject
	protected XsemanticsJavaValidatorHelper helper;

	@Inject
	protected XsemanticsXExpressionHelper xExpressionHelper;

	@Inject
	protected XsemanticsNodeModelUtils nodeModelUtils;

	public final static int maxOfOutputParams = 3;

	protected boolean enableWarnings = true;

	@Override
	@Check
	public void checkAssignment(XAssignment assignment) {
		// we allow assignment to output parameters
		JvmIdentifiableElement assignmentFeature = assignment.getFeature();
		if (assignmentFeature instanceof JvmFormalParameter) {
			if (xsemanticsUtils
					.isInputParam((JvmFormalParameter) assignmentFeature)) {
				error("Assignment to input parameter",
						Literals.XASSIGNMENT__ASSIGNABLE,
						ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
						IssueCodes.ASSIGNMENT_TO_INPUT_PARAM);
			}
			return;
		}
		super.checkAssignment(assignment);
	}

	@Override
	@Check
	public void checkReturn(XReturnExpression expr) {
		error("Return statements are not allowed here", expr, null,
				IssueCodes.RETURN_NOT_ALLOWED);
	}

//	@Override
//	protected boolean supportsCheckedExceptions() {
//		// we generate Java code which already handles exceptions
//		return false;
//	}

	@Override
	protected boolean isImplicitReturn(XExpression expr) {
		if (isContainedInAuxiliaryFunction(expr)) {
			return super.isImplicitReturn(expr);
		}

		// we will deal with this during generation
		return false;
	}

	protected boolean isContainedInAuxiliaryFunction(XExpression expr) {
		return EcoreUtil2.getContainerOfType(expr, AuxiliaryFunction.class) != null;
	}

	@Check
	public void checkThrow(XThrowExpression expr) {
		error("Throw statements are not allowed here", expr, null,
				IssueCodes.THROW_NOT_ALLOWED);
	}

	@Override
	protected boolean isLocallyUsed(EObject target, EObject containerToFindUsage) {
		if (containerToFindUsage instanceof RuleInvocation) {
			// we don't want warning when a variable declaration appears as
			// output argument: it is implicitly used for the result
			return true;
		}
		return super.isLocallyUsed(target, containerToFindUsage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.xtext.xbase.validation.XbaseJavaValidator#
	 * isValueExpectedRecursive(org.eclipse.xtext.xbase.XExpression)
	 */
	@Override
	protected boolean isValueExpectedRecursive(XExpression expr) {
		// this is used by Xbase validator to check expressions with
		// side effects, by inspecting expr's container
		// so we must customize it when the container is one of our
		// custom XExpressions
		final boolean valueExpectedRecursive = super
				.isValueExpectedRecursive(expr);
		return valueExpectedRecursive
				|| xExpressionHelper.isXsemanticsXExpression(expr.eContainer());
	}

	@Check
	public void checkJudgmentDescription(JudgmentDescription judgmentDescription) {
		checkNoDuplicateJudgmentDescriptionSymbols(judgmentDescription);
		checkNumOfOutputParams(judgmentDescription);
		checkNumOfInputParams(judgmentDescription);
	}

	@Check
	public void checkJudgmentDescriptionHasRules(
			JudgmentDescription judgmentDescription) {
		if (judgmentDescription.isOverride())
			return;
		if (enableWarnings
				&& xsemanticsUtils.rulesForJudgmentDescription(
						judgmentDescription).isEmpty()) {
			warning("No rule defined for the judgment description",
					XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION
							.getEIDAttribute(),
					IssueCodes.NO_RULE_FOR_JUDGMENT_DESCRIPTION);
		}
	}

	@Check
	public void checkInputParameter(InputParameter param) {
		if (helper.findDuplicateParameter(param)) {
			error("Duplicate parameter '" + param.getParameter().getName()
					+ "'",
					XsemanticsPackage.Literals.INPUT_PARAMETER__PARAMETER,
					IssueCodes.DUPLICATE_PARAM_NAME);
		}
	}

	@Check
	public void checkNoDuplicateJudgmentDescription(
			JudgmentDescription judgmentDescription) {
		JudgmentDescription judgmentDescriptionWithTheSameName = helper
				.judgmentDescriptionWithTheSameName(judgmentDescription);
		if (judgmentDescriptionWithTheSameName != null
				&& !judgmentDescription.isOverride()) {
			error("Duplicate judgment '"
					+ judgmentDescription.getName()
					+ "'"
					+ reportContainingSystemName(judgmentDescriptionWithTheSameName),
					XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION__NAME,
					IssueCodes.DUPLICATE_JUDGMENT_NAME);
		}
	}

	protected void checkNoDuplicateJudgmentDescriptionSymbols(
			JudgmentDescription judgmentDescription) {
		String judgmentSymbol = judgmentDescription.getJudgmentSymbol();
		List<String> relationSymbols = judgmentDescription.getRelationSymbols();
		if (xsemanticsUtils.getJudgmentDescriptions(
				xsemanticsUtils.containingSystem(judgmentDescription),
				judgmentSymbol, relationSymbols).size() > 1) {
			error("Duplicate JudgmentDescription symbols: "
					+ symbolsRepresentation(judgmentSymbol, relationSymbols),
					XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION__JUDGMENT_SYMBOL,
					IssueCodes.DUPLICATE_JUDGMENT_DESCRIPTION_SYMBOLS);
		}
	}

	protected void checkNumOfOutputParams(
			JudgmentDescription judgmentDescription) {
		if (xsemanticsUtils.outputJudgmentParameters(judgmentDescription)
				.size() > maxOfOutputParams) {
			error("No more than " + maxOfOutputParams
					+ " output parameters are handled at the moment",
					XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION__JUDGMENT_PARAMETERS,
					IssueCodes.TOO_MANY_OUTPUT_PARAMS);
		}
	}

	protected void checkNumOfInputParams(JudgmentDescription judgmentDescription) {
		if (xsemanticsUtils.inputParams(judgmentDescription).isEmpty()) {
			error("No input parameter; at least one is needed",
					XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION__JUDGMENT_PARAMETERS,
					IssueCodes.NO_INPUT_PARAM);
		}
	}

	@Check
	public void checkRule(Rule rule) {
		JudgmentDescription judgmentDescription = checkRuleConformantToJudgmentDescription(rule);
		if (judgmentDescription != null) {
			List<JudgmentParameter> judgmentParameters = judgmentDescription
					.getJudgmentParameters();
			List<RuleConclusionElement> conclusionElements = rule
					.getConclusion().getConclusionElements();
			// judgmentParameters.size() == conclusionElements.size())
			// otherwise we could not find a JudgmentDescription for the rule
			Iterator<JudgmentParameter> judgmentParametersIt = judgmentParameters
					.iterator();
			for (RuleConclusionElement ruleConclusionElement : conclusionElements) {
				if (!xsemanticsUtils.isOutputParameter(judgmentParametersIt
						.next())
						&& !(ruleConclusionElement instanceof RuleParameter)) {
					error("Must be a parameter, not an expression",
							ruleConclusionElement,
							XsemanticsPackage.Literals.RULE_CONCLUSION_ELEMENT
									.getEIDAttribute(),
							IssueCodes.NOT_PARAMETER);
				}
			}
		}
	}

	@Check
	public void checkNoCheckRulesWithTheSameName(Rule rule) {
		if (!helper.noRulesWithTheSameName(rule)) {
			error("Duplicate rule '" + rule.getName() + "'", rule,
					XsemanticsPackage.Literals.RULE__NAME,
					IssueCodes.DUPLICATE_RULE_NAME);
		}

		if (!helper.noRulesWithTheSameNameOfCheckRule(rule)) {
			error("Duplicate checkrule with the same name", rule,
					XsemanticsPackage.Literals.RULE__NAME,
					IssueCodes.DUPLICATE_RULE_NAME);
		}
	}

	@Check
	public void checkNoRulesWithTheSameName(CheckRule rule) {
		if (!helper.noCheckRulesWithTheSameName(rule)) {
			error("Duplicate checkrule '" + rule.getName() + "'", rule,
					XsemanticsPackage.Literals.CHECK_RULE__NAME,
					IssueCodes.DUPLICATE_RULE_NAME);
		}

		if (!helper.noCheckRulesWithTheSameNameOfRule(rule)) {
			error("Duplicate rule with the same name", rule,
					XsemanticsPackage.Literals.CHECK_RULE__NAME,
					IssueCodes.DUPLICATE_RULE_NAME);
		}
	}

	@Check
	public void checkValidOverride(Rule rule) {
		XsemanticsSystem system = xsemanticsUtils.containingSystem(rule);
		if (system != null) {
			if (rule.isOverride()) {
				XsemanticsSystem superSystem = xsemanticsUtils
						.superSystemDefinition(system);
				if (superSystem == null) {
					error("Cannot override rule without system 'extends'",
							rule, XsemanticsPackage.Literals.RULE__OVERRIDE,
							IssueCodes.OVERRIDE_WITHOUT_SYSTEM_EXTENDS);
				} else {
					List<Rule> rulesOfTheSameKind = xsemanticsUtils
							.allRulesOfTheSameKind(superSystem, rule);
					TupleType tupleType = typeSystem.getInputTypes(rule);
					Rule ruleToOverride = null;
					for (Rule rule2 : rulesOfTheSameKind) {
						TupleType tupleType2 = typeSystem.getInputTypes(rule2);
						if (typeSystem.equals(tupleType, tupleType2, rule)) {
							ruleToOverride = rule2;
							break;
						}
					}
					if (ruleToOverride == null) {
						error("No rule of the same kind to override: "
								+ tupleTypeRepresentation(tupleType), rule,
								XsemanticsPackage.Literals.RULE__OVERRIDE,
								IssueCodes.NO_RULE_TO_OVERRIDE_OF_THE_SAME_KIND);
					} else if (!ruleToOverride.getName().equals(rule.getName())) {
						error("Must have the same name of the rule to override: "
								+ ruleToOverride.getName(),
								rule,
								XsemanticsPackage.Literals.RULE__OVERRIDE,
								IssueCodes.OVERRIDE_RULE_MUST_HAVE_THE_SAME_NAME);
					}
				}
			}
		}
	}

	@Check
	public void checkValidOverride(CheckRule rule) {
		XsemanticsSystem system = xsemanticsUtils.containingSystem(rule);
		if (system != null) {
			if (rule.isOverride()) {
				XsemanticsSystem superSystem = xsemanticsUtils
						.superSystemDefinition(system);
				if (superSystem == null) {
					error("Cannot override checkrule without system 'extends'",
							rule,
							XsemanticsPackage.Literals.CHECK_RULE__OVERRIDE,
							IssueCodes.OVERRIDE_WITHOUT_SYSTEM_EXTENDS);
				} else {
					ArrayList<CheckRule> inheritedCheckRules = xsemanticsUtils
							.allCheckRules(superSystem);
					CheckRule inheritedRule = null;
					for (CheckRule checkRule2 : inheritedCheckRules) {
						if (typeSystem.equals(rule.getElement().getParameter()
								.getParameterType(), checkRule2.getElement()
								.getParameter().getParameterType(), rule)
								&& rule.getName().equals(checkRule2.getName())) {
							inheritedRule = checkRule2;
						}
					}
					if (inheritedRule == null)
						error("No checkrule to override: " + rule.getName(),
								rule,
								XsemanticsPackage.Literals.CHECK_RULE__OVERRIDE,
								IssueCodes.NO_RULE_TO_OVERRIDE_OF_THE_SAME_KIND);
				}
			}
		}
	}

	@Check
	public void checkValidOverride(JudgmentDescription judgment) {
		XsemanticsSystem system = xsemanticsUtils.containingSystem(judgment);
		if (system != null) {
			if (judgment.isOverride()) {
				XsemanticsSystem superSystem = xsemanticsUtils
						.superSystemDefinition(system);
				if (superSystem == null) {
					error("Cannot override judgment without system 'extends'",
							judgment,
							XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION__OVERRIDE,
							IssueCodes.OVERRIDE_WITHOUT_SYSTEM_EXTENDS);
				} else {
					List<JudgmentDescription> inheritedJudgments = xsemanticsUtils
							.allJudgments(superSystem,
									judgment.getJudgmentSymbol(),
									judgment.getRelationSymbols());
					JudgmentDescription judgmentToOverride = null;
					for (JudgmentDescription judgment2 : inheritedJudgments) {
						if (typeSystem.equals(judgment, judgment2)) {
							judgmentToOverride = judgment2;
							break;
						}
					}
					if (judgmentToOverride == null) {
						error("No judgment of the same kind to override: "
								+ nodeModelUtils.getProgramText(judgment),
								judgment,
								XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION__OVERRIDE,
								IssueCodes.NO_JUDGMENT_TO_OVERRIDE_OF_THE_SAME_KIND);
					} else if (!judgmentToOverride.getName().equals(
							judgment.getName())) {
						error("Must have the same name of the judgment to override: "
								+ judgmentToOverride.getName(),
								judgment,
								XsemanticsPackage.Literals.JUDGMENT_DESCRIPTION__OVERRIDE,
								IssueCodes.OVERRIDE_JUDGMENT_MUST_HAVE_THE_SAME_NAME);
					}
				}
			}
		}
	}

	@Check
	public void checkRuleInvocation(RuleInvocation ruleInvocation) {
		JudgmentDescription judgmentDescription = checkRuleInvocationConformantToJudgmentDescription(ruleInvocation);
		if (judgmentDescription != null) {
			List<JudgmentParameter> judgmentParameters = judgmentDescription
					.getJudgmentParameters();
			List<XExpression> invocationExpressions = ruleInvocation
					.getExpressions();
			// judgmentParamters.size() == conclusionElements.size())
			// otherwise we could not find a JudgmentDescription for the rule
			Iterator<JudgmentParameter> judgmentParametersIt = judgmentParameters
					.iterator();
			for (XExpression ruleInvocationExpression : invocationExpressions) {
				if (xsemanticsUtils.isOutputParameter(judgmentParametersIt
						.next())) {
					if (!xsemanticsUtils
							.validOutputArgExpression(ruleInvocationExpression)) {
						error("Not a valid argument for output parameter: "
								+ nodeModelUtils
										.getProgramText(ruleInvocationExpression),
								ruleInvocationExpression,
								null,
								IssueCodes.NOT_VALID_OUTPUT_ARG);
					}
				} else {
					if (!xsemanticsUtils
							.validInputArgExpression(ruleInvocationExpression)) {
						error("Not a valid argument for input parameter: "
								+ nodeModelUtils
										.getProgramText(ruleInvocationExpression),
								ruleInvocationExpression,
								null,
								IssueCodes.NOT_VALID_INPUT_ARG);
					}
				}

			}
		}
	}

	@Check
	public void checkSystem(XsemanticsSystem system) {
		JvmParameterizedTypeReference validatorExtends = system
				.getValidatorExtends();
		if (validatorExtends != null) {
			if (!typeSystem.isAbstractDeclarativeValidator(validatorExtends,
					system)) {
				error("Not an AbstractDeclarativeValidator: "
						+ getNameOfTypes(validatorExtends),
						XsemanticsPackage.Literals.XSEMANTICS_SYSTEM__VALIDATOR_EXTENDS,
						IssueCodes.NOT_VALIDATOR);
			}
		}
		JvmParameterizedTypeReference superSystem = system.getSuperSystem();
		if (superSystem != null) {
			if (!typeSystem.isValidSuperSystem(superSystem, system)) {
				error("Not an Xsemantics system: "
						+ getNameOfTypes(superSystem),
						XsemanticsPackage.Literals.XSEMANTICS_SYSTEM__SUPER_SYSTEM,
						IssueCodes.NOT_VALID_SUPER_SYSTEM);
			}
			if (validatorExtends != null) {
				error("system 'extends' cannot coexist with 'validatorExtends'",
						XsemanticsPackage.Literals.XSEMANTICS_SYSTEM__SUPER_SYSTEM,
						IssueCodes.EXTENDS_CANNOT_COEXIST_WITH_VALIDATOR_EXTENDS);
				error("system 'extends' cannot coexist with 'validatorExtends'",
						XsemanticsPackage.Literals.XSEMANTICS_SYSTEM__VALIDATOR_EXTENDS,
						IssueCodes.EXTENDS_CANNOT_COEXIST_WITH_VALIDATOR_EXTENDS);
			}
		}
		List<XsemanticsSystem> superSystems = xsemanticsUtils
				.allSuperSystemDefinitions(system);
		if (superSystems.contains(system)) {
			error("Cycle in extends relation",
					XsemanticsPackage.Literals.XSEMANTICS_SYSTEM__SUPER_SYSTEM,
					IssueCodes.CYCLIC_HIERARCHY);
		}
	}

	@Check
	protected void checkNoDuplicateCheckRulesWithSameArguments(CheckRule rule) {
		if (rule.isOverride())
			return;
		XsemanticsSystem system = xsemanticsUtils
				.superSystemDefinition(xsemanticsUtils.containingSystem(rule));
		if (system != null) {
			List<CheckRule> rulesWithTheSameName = xsemanticsUtils
					.allCheckRulesByName(system, rule);
			for (CheckRule checkRule : rulesWithTheSameName) {
				error("Duplicate checkrule with the same name"
						+ reportContainingSystemName(checkRule),
						XsemanticsPackage.Literals.CHECK_RULE__NAME,
						IssueCodes.DUPLICATE_RULE_NAME);
			}
		}
	}

	@Check
	protected void checkNoDuplicateRulesWithSameArguments(Rule rule) {
		List<Rule> rulesOfTheSameKind = xsemanticsUtils
				.allRulesOfTheSameKind(rule);
		if (rulesOfTheSameKind.size() > 1) {
			TupleType tupleType = typeSystem.getInputTypes(rule);
			for (Rule rule2 : rulesOfTheSameKind) {
				if (rule2 != rule && !rule.isOverride()) {
					TupleType tupleType2 = typeSystem.getInputTypes(rule2);
					if (typeSystem.equals(tupleType, tupleType2, rule)) {
						error("Duplicate rule of the same kind with parameters: "
								+ tupleTypeRepresentation(tupleType)
								+ reportContainingSystemName(rule2),
								XsemanticsPackage.Literals.RULE__CONCLUSION,
								IssueCodes.DUPLICATE_RULE_WITH_SAME_ARGUMENTS);
						break;
					}
				}
			}
		}
	}

	@Check
	public void checkAuxiliaryDescription(AuxiliaryDescription aux) {
		if (helper.auxiliaryDescriptionWithTheSameName(aux) != null) {
			error("Duplicate auxiliary description '" + aux.getName() + "'",
					XsemanticsPackage.Literals.AUXILIARY_DESCRIPTION__NAME,
					IssueCodes.DUPLICATE_AUXILIARY_NAME);
		}

		if (helper.auxiliaryDescriptionWithTheSameNameOfJudgment(aux) != null) {
			error("Duplicate judgment with the same name '" + aux.getName()
					+ "'",
					XsemanticsPackage.Literals.AUXILIARY_DESCRIPTION__NAME,
					IssueCodes.DUPLICATE_AUXILIARY_NAME);
		}
	}

	@Check
	public void checkAuxiliaryFunctions(AuxiliaryDescription aux) {
		List<AuxiliaryFunction> functionsForAuxiliaryDescrition = xsemanticsUtils.functionsForAuxiliaryDescrition(aux);
		if (enableWarnings
				&& functionsForAuxiliaryDescrition
						.isEmpty()) {
			warning("No function defined for the auxiliary description",
					XsemanticsPackage.Literals.AUXILIARY_DESCRIPTION
							.getEIDAttribute(),
					IssueCodes.NO_AUXFUN_FOR_AUX_DESCRIPTION);
		}
		
		if (functionsForAuxiliaryDescrition.size() > 1) {
			for (AuxiliaryFunction auxiliaryFunction : functionsForAuxiliaryDescrition) {
				TupleType tupleType = typeSystem.getInputTypes(auxiliaryFunction);
				
				for (AuxiliaryFunction auxiliaryFunction2 : functionsForAuxiliaryDescrition) {
					if (auxiliaryFunction == auxiliaryFunction2)
						continue;
					
					TupleType tupleType2 = typeSystem.getInputTypes(auxiliaryFunction2);
					if (typeSystem.equals(tupleType, tupleType2, auxiliaryFunction)) {
						error("Duplicate auxiliary function of the same kind with parameters: "
								+ tupleTypeRepresentation(tupleType)
								+ reportContainingSystemName(auxiliaryFunction2),
								auxiliaryFunction2,
								XsemanticsPackage.Literals.AUXILIARY_FUNCTION__PARAMETERS,
								IssueCodes.DUPLICATE_AUXFUN_WITH_SAME_ARGUMENTS);
						break;
					}
				}
			}
		}
	}

	@Check
	public void checkAuxiliaryFunctionHasAuxiliaryDescription(
			AuxiliaryFunction aux) {
		AuxiliaryDescription auxiliaryDescription = xsemanticsUtils
				.auxiliaryDescription(aux);
		if (auxiliaryDescription == null)
			error("No auxiliary description for auxiliary function '"
					+ aux.getName() + "'",
					XsemanticsPackage.Literals.AUXILIARY_FUNCTION__NAME,
					IssueCodes.NO_AUXDESC_FOR_AUX_FUNCTION);
		else
			checkConformanceOfAuxiliaryFunction(aux, auxiliaryDescription);
	}

	@Check
	public void checkOutputParamAccessWithinClosure(XFeatureCall featureCall) {
		JvmIdentifiableElement feature = featureCall.getFeature();
		if (feature instanceof JvmFormalParameter) {
			EObject container = feature.eContainer();
			if (container instanceof RuleParameter) {
				if (xsemanticsUtils.isOutputParam((RuleParameter) container)
						&& insideClosure(featureCall)) {
					error("Cannot refer to an output parameter "
							+ feature.getIdentifier()
							+ " from within a closure", featureCall, null,
							IssueCodes.ACCESS_TO_OUTPUT_PARAM_WITHIN_CLOSURE);
				}
			}
			return;
		}
	}

	@Check
	public void checkInjected(Injected i) {
		if (helper.hasDuplicateInjectedField(i)) {
			error("Duplicate injection '" + i.getName() + "'",
				XsemanticsPackage.eINSTANCE.getInjected_Name(),
				IssueCodes.DUPLICATE_INJECTED_FIELD);
		}
	}

	private boolean insideClosure(XFeatureCall featureCall) {
		return EcoreUtil2.getContainerOfType(featureCall,
				XClosure.class) != null;
	}

	protected void checkConformanceOfAuxiliaryFunction(AuxiliaryFunction aux,
			AuxiliaryDescription auxiliaryDescription) {
		List<JvmFormalParameter> funParams = aux.getParameters();
		List<JvmFormalParameter> descParams = auxiliaryDescription
				.getParameters();

		if (funParams.size() != descParams.size())
			error("expected " + descParams.size() + " parameter(s), but was "
					+ funParams.size(),
					aux,
					XsemanticsPackage.Literals.AUXILIARY_FUNCTION__PARAMETERS,
					IssueCodes.PARAMS_SIZE_DONT_MATCH);
		else {
			Iterator<JvmFormalParameter> funParamsIt = funParams.iterator();
			for (JvmFormalParameter jvmFormalParameter : descParams) {
				JvmTypeReference expected = typeSystem
						.getType(jvmFormalParameter);
				JvmFormalParameter funParam = funParamsIt.next();
				JvmTypeReference actual = typeSystem.getType(funParam);
				if (!typeSystem.isConformant(expected, actual, funParam)) {
					error("parameter type "
							+ getNameOfTypes(actual)
							+ " is not subtype of AuxiliaryDescription declared type "
							+ getNameOfTypes(expected),
							funParam,
							TypesPackage.Literals.JVM_FORMAL_PARAMETER__PARAMETER_TYPE,
							IssueCodes.NOT_SUBTYPE);
				}
			}
		}
	}

	protected String reportContainingSystemName(EObject object) {
		return ", in system: "
				+ xsemanticsUtils.containingSystem(object).getName();
	}

	protected JudgmentDescription checkRuleConformantToJudgmentDescription(
			Rule rule) {
		RuleConclusion conclusion = rule.getConclusion();
		return checkConformanceAgainstJudgmentDescription(conclusion,
				conclusion.getJudgmentSymbol(),
				conclusion.getRelationSymbols(),
				conclusion.getConclusionElements(), "Rule conclusion",
				XsemanticsPackage.Literals.RULE__CONCLUSION,
				XsemanticsPackage.Literals.RULE_CONCLUSION_ELEMENT
						.getEIDAttribute());
	}

	protected JudgmentDescription checkRuleInvocationConformantToJudgmentDescription(
			RuleInvocation ruleInvocation) {
		return checkConformanceAgainstJudgmentDescription(
				ruleInvocation,
				ruleInvocation.getJudgmentSymbol(),
				ruleInvocation.getRelationSymbols(),
				ruleInvocation.getExpressions(),
				"Rule invocation",
				XsemanticsPackage.Literals.RULE_INVOCATION.getEIDAttribute(),
				null);
	}

	protected JudgmentDescription checkConformanceAgainstJudgmentDescription(
			EObject element, String judgmentSymbol,
			Iterable<String> relationSymbols,
			Iterable<? extends EObject> elements,
			final String elementDescription, EStructuralFeature elementFeature,
			EStructuralFeature conformanceFeature) {
		JudgmentDescription judgmentDescription = xsemanticsUtils
				.judgmentDescription(element, judgmentSymbol, relationSymbols);
		if (judgmentDescription == null) {
			error("No Judgment description for: "
					+ symbolsRepresentation(judgmentSymbol, relationSymbols),
					elementFeature, IssueCodes.NO_JUDGMENT_DESCRIPTION);
		} else {
			List<JudgmentParameter> judgmentParameters = judgmentDescription
					.getJudgmentParameters();
			Iterator<? extends EObject> elementsIt = elements.iterator();
			for (JudgmentParameter judgmentParameter : judgmentParameters) {
				// the rule might still be incomplete, thus we must check
				// whether there is an element to check against.
				// Recall that the judgment has been searched for using only
				// the symbols, not the rele conclusion elements
				if (elementsIt.hasNext())
					checkConformance(judgmentParameter, elementsIt.next(),
						elementDescription, conformanceFeature);
			}
		}
		return judgmentDescription;
	}

	protected void checkConformance(JudgmentParameter judgmentParameter,
			EObject element, final String elementDescription,
			EStructuralFeature feature) {
		JvmTypeReference expected = typeSystem.getType(judgmentParameter);
		JvmTypeReference actual = typeSystem.getType(element);
		if (!typeSystem.isConformant(expected, actual, element)) {
			error(elementDescription + " type " + getNameOfTypes(actual)
					+ " is not subtype of JudgmentDescription declared type "
					+ getNameOfTypes(expected), element, feature,
					IssueCodes.NOT_SUBTYPE);
		}
	}

	protected String symbolsRepresentation(String judgmentSymbol,
			Iterable<String> relationSymbols) {
		return judgmentSymbol + " "
				+ IterableExtensions.join(relationSymbols, " ");
	}

	protected String tupleTypeRepresentation(TupleType tupleType) {
		StringBuilder builder = new StringBuilder();
		Iterator<JvmTypeReference> it = tupleType.iterator();
		while (it.hasNext()) {
			builder.append(getNameOfTypes(it.next()));
			if (it.hasNext())
				builder.append(", ");
		}
		return builder.toString();
	}

	private Object getNameOfTypes(JvmTypeReference typeRef) {
		return (typeRef == null) ? "<null>" : typeRef.getSimpleName();
	}

	public boolean isEnableWarnings() {
		return enableWarnings;
	}

	public void setEnableWarnings(boolean enableWarnings) {
		this.enableWarnings = enableWarnings;
	}

}
