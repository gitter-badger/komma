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

public class PrefixDecl {
	protected String prefix;
	protected IriRef Iri;

	public PrefixDecl(String prefix, IriRef Iri) {
		this.prefix = prefix;
		this.Iri = Iri;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public IriRef getIri() {
		return Iri;
	}
}
