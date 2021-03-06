/*
 * Copyright (c) 2007, 2010, James Leigh All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution. 
 * - Neither the name of the openrdf.org nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.enilink.composition.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks recorded roles and maps them to their subject type.
 * 
 */
public class SimpleRoleMapper<T> implements Cloneable {
	/** http://www.w3.org/2000/01/rdf-schema#Resource */
	private static final String BASE_TYPE = "http://www.w3.org/2000/01/rdf-schema#Resource";

	private final Logger logger = LoggerFactory
			.getLogger(SimpleRoleMapper.class);

	private T baseType;

	private boolean empty = true;

	private Map<T, List<Class<?>>> roles = new ConcurrentHashMap<T, List<Class<?>>>(
			256);

	private Map<T, Boolean> unregisteredTypes = new ConcurrentHashMap<T, Boolean>();

	public SimpleRoleMapper<T> clone() {
		try {
			@SuppressWarnings("unchecked")
			SimpleRoleMapper<T> cloned = (SimpleRoleMapper<T>) super.clone();
			cloned.roles = clone(roles);
			return cloned;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	private <K, V> Map<K, List<V>> clone(Map<K, List<V>> map) {
		Map<K, List<V>> cloned = new HashMap<K, List<V>>(map);
		for (Map.Entry<K, List<V>> e : cloned.entrySet()) {
			e.setValue(new ArrayList<V>(e.getValue()));
		}
		return cloned;
	}

	public void setTypeFactory(TypeFactory<T> typeFactory) {
		baseType = typeFactory.createType(BASE_TYPE);
		List<Class<?>> list = Collections.emptyList();
		roles.put(baseType, list);
	}

	public T getBaseType() {
		return baseType;
	}

	public Collection<Class<?>> findAllRoles() {
		List<Class<?>> list = new ArrayList<Class<?>>(roles.size());
		for (List<Class<?>> v : roles.values()) {
			list.addAll(v);
		}
		return list;
	}

	public void findRoles(T type, Collection<Class<?>> classes) {
		List<Class<?>> rolesForType = roles.get(type);
		if (rolesForType == null) {
			unregistered(type);
			classes.addAll(findBaseRoles());
		} else {
			classes.addAll(rolesForType);
		}
	}

	public void findRoles(Collection<T> types, Collection<Class<?>> classes) {
		boolean found = false;
		for (T type : types) {
			List<Class<?>> rolesForType = roles.get(type);
			if (rolesForType == null) {
				unregistered(type);
			} else {
				found = true;
				classes.addAll(rolesForType);
			}
		}
		if (!found) {
			classes.addAll(findBaseRoles());
		}
	}

	public boolean isTypeRecorded(T type) {
		return roles.containsKey(type);
	}

	public synchronized Set<Class<?>> recordRoles(Set<Class<?>> newRoles, T uri) {
		List<Class<?>> existingRoles = roles.get(uri);
		Set<Class<?>> changed = new HashSet<Class<?>>();
		if (existingRoles == null) {
			changed.addAll(findBaseRoles());
		} else {
			changed.addAll(existingRoles);
		}
		changed.addAll(newRoles);

		if (existingRoles == null || changed.size() != existingRoles.size()) {
			empty &= uri.equals(baseType);
			roles.put(uri, Arrays.asList(changed.toArray(new Class<?>[changed
					.size()])));
		}
		return changed;
	}

	public synchronized void recordBaseRole(Class<?> role) {
		for (Map.Entry<T, List<Class<?>>> e : roles.entrySet()) {
			List<Class<?>> set = e.getValue();
			boolean contains = false;
			for (Class<?> c : set) {
				if (role.equals(c)) {
					contains = true;
					break;
				}
			}
			if (contains)
				continue;
			List<Class<?>> ar = new ArrayList<Class<?>>(set.size() + 1);
			ar.addAll(set);
			ar.add(role);
			e.setValue(ar);
		}
	}

	private Collection<Class<?>> findBaseRoles() {
		Collection<Class<?>> baseRoles = roles.get(baseType);
		return baseRoles == null ? Collections.<Class<?>> emptySet()
				: baseRoles;
	}

	private void unregistered(T type) {
		if (!unregisteredTypes.containsKey(type)) {
			unregisteredTypes.put(type, Boolean.TRUE);
			logger.info("Unregistered type {}", type);
		}
	}
}