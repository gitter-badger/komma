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
package net.enilink.komma.core;

import javax.management.Query;

/**
 * Thrown by the persistence provider when {@link Query#getSingleResult
 * getSingleResult()} is executed on a query and there is more than one result
 * from the query. This exception will not cause the current transaction, if one
 * is active, to be marked for roll back.
 * 
 * @see Query#getSingleResult()
 * @see Query#getTypedSingleResult()
 * 
 */
public class NonUniqueResultException extends KommaException {

	private static final long serialVersionUID = 1986014608546876860L;

	/**
	 * Constructs a new <code>NonUniqueResultException</code> exception with
	 * <code>null</code> as its detail message.
	 */
	public NonUniqueResultException() {
		super();
	}

	/**
	 * Constructs a new <code>NonUniqueResultException</code> exception with the
	 * specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public NonUniqueResultException(String message) {
		super(message);
	}
}
