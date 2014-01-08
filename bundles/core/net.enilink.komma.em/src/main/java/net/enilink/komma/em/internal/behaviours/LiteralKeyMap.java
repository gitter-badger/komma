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
package net.enilink.komma.em.internal.behaviours;

import net.enilink.composition.annotations.Iri;
import net.enilink.komma.core.URI;
import net.enilink.vocab.komma.KOMMA;

@Iri(KOMMA.NAMESPACE + "LiteralKeyMap")
public abstract class LiteralKeyMap extends AbstractRDFMap {
	@Override
	protected URI getUri4Key() {
		return KOMMA.PROPERTY_KEYDATA;
	}

	@Override
	protected URI getUri4Value() {
		return KOMMA.PROPERTY_VALUE;
	}

}
