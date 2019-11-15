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
package net.enilink.komma.em.internal.query;

import java.util.List;

public class ResultInfo {
	public boolean typeRestricted = false;
	public List<Class<?>> types;

	public ResultInfo(boolean typeRestricted, List<Class<?>> types) {
		this.typeRestricted = typeRestricted;
		this.types = types;
	}
}