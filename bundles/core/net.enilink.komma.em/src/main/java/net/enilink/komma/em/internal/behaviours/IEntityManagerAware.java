/*******************************************************************************
 * Copyright (c) 2009, 2010 Fraunhofer IWU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Fraunhofer IWU - initial API and implementation
 *******************************************************************************/
package net.enilink.komma.em.internal.behaviours;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IReference;

/**
 * Internal interface implemented by {@link EntitySupport}.
 */
public interface IEntityManagerAware {
	void initEntityManager(IEntityManager manager);

	void initReference(IReference reference);
}
