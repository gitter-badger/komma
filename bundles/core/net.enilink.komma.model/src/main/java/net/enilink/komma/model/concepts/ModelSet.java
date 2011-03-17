/*******************************************************************************
 * Copyright (c) 2009, 2010 Fraunhofer IWU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fraunhofer IWU - initial API and implementation
 *******************************************************************************/
package net.enilink.komma.model.concepts;

import net.enilink.composition.annotations.Iri;

import net.enilink.komma.core.IEntity;

/**
 * 
 * @generated
 */
@Iri("http://enilink.net/vocab/komma/models#ModelSet")
public interface ModelSet extends IEntity {
	/**
	 * Returns whether this model set is persistent or not.
	 * 
	 * @generated
	 */
	@Iri("http://enilink.net/vocab/komma/models#persistent")
	boolean isPersistent();

	/**
	 * Sets whether this model set is persistent or not.
	 * 
	 * @generated
	 */
	void setPersistent(boolean persistent);
}