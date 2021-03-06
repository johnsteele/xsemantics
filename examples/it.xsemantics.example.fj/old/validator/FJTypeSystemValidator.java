package it.xsemantics.example.fj.typesystem.fj.validator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.Check;

import com.google.inject.Inject;

import it.xtypes.runtime.AbstractTypeSystemDeclarativeValidator;
import it.xtypes.runtime.TypingJudgmentEnvironment;
import it.xtypes.typesystem.ExpressionType;

import it.xsemantics.example.fj.fj.FjPackage;

import it.xsemantics.example.fj.typesystem.fj.FJTypeSystemDefinition;

/**
 * Automatically generated by XTypeS, http://xtypes.sourceforge.net
 * Do not modify it.
 * This class should be instantiated with Google Guide injector.
 */
public class FJTypeSystemValidator
		extends
			AbstractTypeSystemDeclarativeValidator {
	@Inject
	protected FJTypeSystemDefinition typeSystem;

	protected FjPackage basicPackage = FjPackage.eINSTANCE;

	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(it.xsemantics.example.fj.fj.FjPackage.eINSTANCE);
		return result;
	}

	protected ExpressionType createBasicType(String basic) {
		return typeSystem.createBasicType(basic);
	}

	protected ExpressionType createEClassifierType(EClassifier eClassifier) {
		return typeSystem.createEClassifierType(eClassifier);
	}

	protected ExpressionType createCollectionType(ExpressionType expressionType) {
		return typeSystem.createCollectionType(expressionType);
	}

	public static final String CHECK_TYPE_FAILED_METHOD = "CheckTypeFailedMethod";

	@Check
	public void checkMethodType(it.xsemantics.example.fj.fj.Method object) {
		generateErrors(typeSystem.tryToApply(typeEnvironmentFor(object), "|-",
				":", createEClassifierType(basicPackage.getMethod()),
				createBasicType("String"), object), object,
				CHECK_TYPE_FAILED_METHOD);
	}

	protected TypingJudgmentEnvironment typeEnvironmentFor(
			it.xsemantics.example.fj.fj.Method object) {
		return getDefaultTypingJudgmentEnvironmentFor(object);
	}

	public static final String CHECK_TYPE_FAILED_FIELD = "CheckTypeFailedField";

	@Check
	public void checkFieldType(it.xsemantics.example.fj.fj.Field object) {
		generateErrors(typeSystem.tryToApply(typeEnvironmentFor(object), "|-",
				":", createEClassifierType(basicPackage.getField()),
				createBasicType("String"), object), object,
				CHECK_TYPE_FAILED_FIELD);
	}

	protected TypingJudgmentEnvironment typeEnvironmentFor(
			it.xsemantics.example.fj.fj.Field object) {
		return getDefaultTypingJudgmentEnvironmentFor(object);
	}

	public static final String CHECK_TYPE_FAILED_CLASS = "CheckTypeFailedClass";

	@Check
	public void checkClassType(it.xsemantics.example.fj.fj.Class object) {
		generateErrors(typeSystem.tryToApply(typeEnvironmentFor(object), "|-",
				":", createEClassifierType(basicPackage.getClass_()),
				createBasicType("String"), object), object,
				CHECK_TYPE_FAILED_CLASS);
	}

	protected TypingJudgmentEnvironment typeEnvironmentFor(
			it.xsemantics.example.fj.fj.Class object) {
		return getDefaultTypingJudgmentEnvironmentFor(object);
	}

	public static final String CHECK_TYPE_FAILED_PROGRAM = "CheckTypeFailedProgram";

	@Check
	public void checkProgramType(it.xsemantics.example.fj.fj.Program object) {
		generateErrors(typeSystem.tryToApply(typeEnvironmentFor(object), "|-",
				":", createEClassifierType(basicPackage.getProgram()),
				createBasicType("String"), object), object,
				CHECK_TYPE_FAILED_PROGRAM);
	}

	protected TypingJudgmentEnvironment typeEnvironmentFor(
			it.xsemantics.example.fj.fj.Program object) {
		return getDefaultTypingJudgmentEnvironmentFor(object);
	}

}
