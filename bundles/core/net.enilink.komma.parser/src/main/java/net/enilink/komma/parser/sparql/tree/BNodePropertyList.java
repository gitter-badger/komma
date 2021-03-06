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

import net.enilink.komma.parser.sparql.tree.visitor.Visitor;

public class BNodePropertyList extends BNode {
	protected PropertyList bNodePropertyList;

	@Override
	public void setPropertyList(PropertyList propertyList) {
		if (bNodePropertyList == null) {
			this.bNodePropertyList = propertyList;
		}
		super.setPropertyList(propertyList);
	}

	public PropertyList getBNodePropertyList() {
		return bNodePropertyList;
	}

	public <R, T> R accept(Visitor<R, T> visitor, T data) {
		return visitor.bNodePropertyList(this, data);
	}
}
