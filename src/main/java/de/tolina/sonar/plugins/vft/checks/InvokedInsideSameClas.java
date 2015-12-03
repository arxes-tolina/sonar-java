/*
 *  (c) tolina GmbH, 2015
 */
package de.tolina.sonar.plugins.vft.checks;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;

final class InvokedInsideSameClas implements Predicate<MethodInvocationTree> {

	private final Function<MethodInvocationTree, Symbol> getCallingMethod = new GetInvokingSymbol();


	@Override
	public boolean test(final MethodInvocationTree invokedMethod) {
		final Symbol invokedMethodOwnerClass = invokedMethod.symbol().owner();
		final Symbol invokedMethodOwnerPackage = invokedMethodOwnerClass.owner();


		final Symbol invokingMethod = getCallingMethod.apply(invokedMethod);
		final Symbol invokingMethodOwnerClass = invokingMethod.owner();
		final Symbol invokingMethodOwnerPackage = invokingMethodOwnerClass.owner();

		boolean sameClass = Objects.equals(invokingMethodOwnerClass, invokedMethodOwnerClass);
		boolean samePackage = Objects.equals(invokingMethodOwnerPackage, invokedMethodOwnerPackage);
		return sameClass && samePackage;
	}
}