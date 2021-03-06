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
package net.enilink.komma.em.tests;

import org.junit.After;
import org.junit.Before;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.IUnitOfWork;
import net.enilink.komma.core.KommaModule;
import net.enilink.komma.dm.IDataManager;
import net.enilink.komma.dm.IDataManagerFactory;
import net.enilink.komma.em.DecoratingEntityManagerModule;
import net.enilink.komma.em.EntityManagerFactoryModule;
import net.enilink.komma.em.util.UnitOfWork;

public abstract class EntityManagerTest {
	protected Injector injector;
	protected IEntityManagerFactory factory;
	protected IEntityManager manager;
	protected UnitOfWork uow;

	// defines the default module for configuring the storage backend
	private static final String DEFAULT_STORAGE_MODULE = "net.enilink.komma.rdf4j.RDF4JMemoryStoreModule";

	private Module createStorageModule() {
		try {
			return (Module) getClass().getClassLoader()
					.loadClass(DEFAULT_STORAGE_MODULE).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Unable to instantiate storage module: "
					+ DEFAULT_STORAGE_MODULE, e);
		}
	}

	@Before
	public void beforeTest() throws Exception {
		injector = Guice.createInjector(
				createStorageModule(),
				new EntityManagerFactoryModule(createModule(), null,
						new DecoratingEntityManagerModule()),
				new AbstractModule() {
					@Override
					protected void configure() {
						UnitOfWork uow = new UnitOfWork();
						uow.begin();

						bind(UnitOfWork.class).toInstance(uow);
						bind(IUnitOfWork.class).toInstance(uow);
					}

					@Provides
					protected IDataManager provideDataManager(
							IDataManagerFactory dmFactory) {
						return dmFactory.get();
					}
				});
		factory = injector.getInstance(IEntityManagerFactory.class);
		manager = factory.get();
	}

	protected KommaModule createModule() throws Exception {
		return new KommaModule(getClass().getClassLoader());
	}

	@After
	public void afterTest() throws Exception {
		try {
			factory.getUnitOfWork().end();
			factory.close();
		} catch (Exception e) {
		}
	}
}