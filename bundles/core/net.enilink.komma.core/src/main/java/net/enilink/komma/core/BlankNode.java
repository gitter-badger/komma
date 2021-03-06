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
package net.enilink.komma.core;

/**
 * This class represents a generic blank node.
 * 
 * Blank nodes with the same nominal value are considered equal.
 * 
 */
public class BlankNode implements IReference {
	/**
	 * ID for bnode prefixes to prevent blank node clashes (unique per
	 * classloaded instance of this class)
	 */
	private static long lastPrefixId = 0;
	private static String idPrefix = nextIdPrefix();
	private static int nextId;

	private static synchronized String nextIdPrefix() {
		lastPrefixId = Math.max(System.currentTimeMillis(), lastPrefixId + 1);
		return Long.toString(lastPrefixId, 32) + "x";
	}

	public static String generateId() {
		return generateId(null);
	}

	public static synchronized String generateId(String prefix) {
		int id = nextId++;
		String idStr = new StringBuilder("_:")
				.append(prefix == null ? "komma-" : prefix).append(idPrefix)
				.append(id).toString();
		if (id == Integer.MAX_VALUE) {
			idPrefix = nextIdPrefix();
		}
		return idStr;
	}

	private String id;

	public BlankNode() {
		this(generateId());
	}

	public BlankNode(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlankNode other = (BlankNode) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * Returns <code>null</code>, since a blank node does not have an
	 * {@link URI}
	 */
	@Override
	public URI getURI() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return id;
	}
}
