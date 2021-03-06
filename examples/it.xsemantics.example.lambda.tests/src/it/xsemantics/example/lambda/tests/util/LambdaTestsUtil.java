/**
 * 
 */
package it.xsemantics.example.lambda.tests.util;

import it.xsemantics.example.lambda.lambda.Abstraction;
import it.xsemantics.example.lambda.lambda.Application;
import it.xsemantics.example.lambda.lambda.ArrowType;
import it.xsemantics.example.lambda.lambda.Term;
import it.xsemantics.example.lambda.lambda.Type;
import it.xsemantics.example.lambda.lambda.TypeVariable;
import it.xsemantics.example.lambda.lambda.Variable;
import it.xsemantics.example.lambda.xsemantics.LambdaTypeBeautifier;

/**
 * @author Lorenzo Bettini
 * 
 */
public class LambdaTestsUtil {
	public static Abstraction getAbstraction(Term term) {
		return (Abstraction) term;
	}

	public static Application getApplication(Term term) {
		return (Application) term;
	}

	public static TypeVariable getTypeVariable(Type type) {
		return (TypeVariable) type;
	}

	public static ArrowType getArrowType(Type type) {
		return (ArrowType) type;
	}

	public static Variable getVariable(Term term) {
		return (Variable) term;
	}

	public static String rep(Type type) {
		return new LambdaTypeBeautifier().getBeautifiedString(type);
	}

	public static void repOut(Type type) {
		System.out.println(rep(type));
	}

}
