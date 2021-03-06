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
package net.enilink.komma.em.internal;

import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import net.enilink.komma.core.IEntity;
import net.enilink.komma.core.IEntityDecorator;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IReferenceable;
import net.enilink.komma.core.URI;

public class EagerCachingEntityManager extends DecoratingEntityManager {
	@Inject
	private Map<IReference, Object> entityCache;

	@Inject
	public EagerCachingEntityManager(Set<IEntityDecorator> decorators) {
		super(decorators);
	}

	@Override
	protected IEntity createBeanForClass(IReference resource, Class<?> type) {
		Object bean = entityCache.get(resource);
		if (bean == null || !bean.getClass().equals(type)) {
			Object oldBean = bean;
			bean = super.createBeanForClass(resource, type);

			entityCache.put(resource, bean);
		}

		return (IEntity) bean;
	}

	public <T> T rename(T bean, URI uri) {
		entityCache.remove(((IReferenceable) bean).getReference());
		T newBean = super.rename(bean, uri);
		entityCache.put(((IReferenceable) newBean).getReference(), bean);
		return newBean;
	}
}
