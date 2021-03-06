/*******************************************************************************
 * Copyright (c) 2010 Fraunhofer IWU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Fraunhofer IWU - initial API and implementation
 *******************************************************************************/
package net.enilink.komma.parser.sparql.tree;

import java.util.Arrays;

public abstract class QueryWithSolutionModifier extends Query {
	protected LimitModifier limitModifier;
	protected OffsetModifier offsetModifier;
	protected OrderModifier orderModifier;

	protected SolutionModifier[] modifiers;

	public QueryWithSolutionModifier(Dataset dataset, Graph graph,
			SolutionModifier... modifiers) {
		super(dataset, graph);
		this.modifiers = modifiers;

		for (SolutionModifier modifier : modifiers) {
			if (modifier instanceof LimitModifier) {
				assertNull(LimitModifier.class, limitModifier);
				limitModifier = (LimitModifier) modifier;
			} else if (modifier instanceof OffsetModifier) {
				assertNull(OffsetModifier.class, offsetModifier);
				offsetModifier = (OffsetModifier) modifier;
			} else if (modifier instanceof OrderModifier) {
				assertNull(OrderModifier.class, orderModifier);
				orderModifier = (OrderModifier) modifier;
			}
		}
	}

	public LimitModifier getLimitModifier() {
		return limitModifier;
	}

	public OffsetModifier getOffsetModifier() {
		return offsetModifier;
	}

	public OrderModifier getOrderModifier() {
		return orderModifier;
	}

	public java.util.Collection<? extends SolutionModifier> getModifiers() {
		return Arrays.asList(modifiers);
	}

	protected <T> void assertNull(Class<T> type, T value) {
		if (value != null) {
			throw new IllegalArgumentException("Modifier of type " + type
					+ " may only be assigned once.");
		}
	}
}
