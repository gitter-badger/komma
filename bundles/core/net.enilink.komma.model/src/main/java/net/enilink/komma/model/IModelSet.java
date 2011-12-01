/**
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 */
package net.enilink.komma.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.enilink.composition.annotations.Iri;

import com.google.inject.Injector;
import com.google.inject.Module;

import net.enilink.komma.common.adapter.IAdapterSet;
import net.enilink.komma.common.notify.INotification;
import net.enilink.komma.common.notify.INotificationListener;
import net.enilink.komma.common.notify.INotifier;
import net.enilink.komma.common.util.WrappedException;
import net.enilink.komma.dm.IDataManagerFactory;
import net.enilink.komma.dm.change.IDataChangeSupport;
import net.enilink.komma.dm.change.IDataChangeTracker;
import net.enilink.komma.model.base.AbstractModelSetSupport;
import net.enilink.komma.core.IEntityManager;
import net.enilink.komma.core.IEntityManagerFactory;
import net.enilink.komma.core.IReference;
import net.enilink.komma.core.IUnitOfWork;
import net.enilink.komma.core.KommaModule;
import net.enilink.komma.core.URI;

@Iri(MODELS.NAMESPACE + "ModelSet")
public interface IModelSet extends INotifier<INotification> {
	interface Internal extends IModelSet {
		/**
		 * Collects {@link Module} that are used to construct an
		 * {@link Injector} instance for models.
		 */
		void collectInjectionModules(Collection<Module> modules);

		Injector getInjector();

		IDataManagerFactory getDataManagerFactory();

		IEntityManagerFactory getEntityManagerFactory();

		/**
		 * Allow specific model sets to set their default graph in case they
		 * cannot handle the default null.
		 */
		URI getDefaultGraph();

		/**
		 * Initialize the model set.
		 */
		void init();
	}

	/**
	 * Returns the adapter set which contains dynamic adapters registered with
	 * this model set
	 * 
	 * @return the adapter set
	 */
	IAdapterSet adapters();

	void addMetaDataListener(INotificationListener<INotification> listener);

	/**
	 * Register listener which gets notified on changes of <code>object</code>
	 */
	void addSubjectListener(IReference object,
			INotificationListener<INotification> listener);

	/**
	 * Creates a new model, of the appropriate type, and returns it.
	 * <p>
	 * It delegates to the model factory {@link #getModelFactoryRegistry()
	 * model} to determine the {@link IModel.Factory.Registry#getFactory(URI)
	 * correct} factory, and then it uses that factory to
	 * {@link IModel.Factory#createModel(URI) create} the resource and adds it
	 * to the {@link #getModels() contents}. If there is no registered factory,
	 * <code>null</code> will be returned; when running within Eclipse, a
	 * default RDF/XML factory will be registered, and this will never return
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param uri
	 *            the URI of the model to create.
	 * @return a new model, or <code>null</code> if no factory is registered.
	 */
	IModel createModel(URI uri);

	/**
	 * Creates a new resource, of the appropriate type, and returns it.
	 * <p>
	 * It delegates to the resource factory {@link #getModelFactoryRegistry
	 * registry} to determine the
	 * {@link IModel.Factory.Registry#getFactory(URI, String) correct} factory,
	 * and then it uses that factory to {@link IModel.Factory#createModel(URI)
	 * create} the ontology and adds it to the {@link #getModels() contents}. If
	 * there is no registered factory, <code>null</code> will be returned; when
	 * running within Eclipse, a default RDF/XML factory will be registered, and
	 * this will never return <code>null</code>.
	 * </p>
	 * 
	 * @param uri
	 *            the URI of the resource to create.
	 * @param contentType
	 *            the {@link IContentHandler#CONTENT_TYPE_PROPERTY content type
	 *            identifier} of the URI, or <code>null</code> if no content
	 *            type should be used during lookup.
	 * @return a new ontology, or <code>null</code> if no factory is registered.
	 */
	IModel createModel(URI uri, String contentType);

	/**
	 * Performs necessary cleanup.
	 */
	void dispose();

	/**
	 * Returns change support for changes made to the underlying data
	 * repository.
	 * 
	 * @return the change support
	 */
	IDataChangeSupport getDataChangeSupport();

	/**
	 * Returns tracker for changes made to the underlying data repository.
	 * 
	 * @return the change tracker
	 */
	IDataChangeTracker getDataChangeTracker();

	/**
	 * Returns the options used during demand load.
	 * <p>
	 * Options are handled generically as feature-to-setting entries. They are
	 * passed to the model when it is {@link IModel#load(Map) deserialized} . A
	 * model will ignore options it doesn't recognize. The options could even
	 * include things like an Eclipse progress monitor...
	 * </p>
	 * 
	 * @return the options used during demand load.
	 * @see IModel#load(Map)
	 */
	Map<Object, Object> getLoadOptions();

	IEntityManager getMetaDataManager();

	/**
	 * Returns the model resolved by the URI.
	 * <p>
	 * A model set is expected to implement the following strategy in order to
	 * resolve the given URI to a model. First it uses it's
	 * {@link #getURIConverter URI converter} to {@link IURIConverter#normalize
	 * normalize} the URI and then to compare it with the normalized URI of each
	 * resource; if it finds a match, that resource becomes the result. Failing
	 * that, it {@link AbstractModelSetSupport#delegatedGetModel delegates} to
	 * allow the URI to be resolved elsewhere. The important point is that an
	 * arbitrary implementation may resolve the URI to any model, not
	 * necessarily to one contained by this particular model set. If the
	 * delegation step fails to provide a result, and if
	 * <code>loadOnDemand</code> is <code>true</code>, a model is
	 * {@link AbstractModelSetSupport#demandCreateModel created} and that model
	 * becomes the result. If <code>loadOnDemand</code> is <code>true</code> and
	 * the result model is not {@link IModel#isLoaded loaded}, it will be
	 * {@link AbstractModelSetSupport#demandLoad loaded} before it is returned.
	 * </p>
	 * 
	 * @param uri
	 *            the URI to resolve.
	 * @param loadOnDemand
	 *            whether to create and load the model, if it doesn't already
	 *            exists.
	 * @return the model resolved by the URI, or <code>null</code> if there
	 *         isn't one and it's not being demand loaded.
	 * @throws RuntimeException
	 *             if a model can't be demand created.
	 * @throws WrappedException
	 *             if a problem occurs during demand load.
	 */
	IModel getModel(URI uri, boolean loadOnDemand);

	/**
	 * Returns the registry used for creating a model of the appropriate type.
	 * <p>
	 * An implementation will typically provide a registry that delegates to the
	 * global model factory registry. As a result, registrations made in this
	 * registry are <em>local</em> to this model set, i.e., they augment or
	 * override those of the global registry.
	 * </p>
	 * 
	 * @return the registry used for creating a model of the appropriate type.
	 */
	IModel.Factory.Registry getModelFactoryRegistry();

	/**
	 * Returns the direct {@link IModel}s being managed.
	 * <p>
	 * A model added to this set will be {@link IModel#getModelSet contained}
	 * by this model set. If it was previously contained by a model set, it will
	 * have been removed.
	 * </p>
	 * 
	 * @return the model models.
	 * @see IModel#getModelSet
	 */
	@Iri("http://enilink.net/vocab/komma/models#model")
	Set<IModel> getModels();

	/**
	 * Returns an {@link KommaModule} which may be used for creating an
	 * {@link IEntityManager} or inclusion into other modules
	 * 
	 * @return the module instance
	 */
	KommaModule getModule();

	/**
	 * Returns the object resolved by the URI.
	 * <p>
	 * Every object {@link IObject#getModel() contained} by a model has a
	 * corresponding URI that resolves to the object. So for any object
	 * contained by a resource, the following is <code>true</code>.
	 * 
	 * <pre>
	 * eObject == eObject.eResource().getResourceSet()
	 * 		.getEObject(EcoreUtil.getURI(eObject), false)
	 * </pre>
	 * 
	 * </p>
	 * <p>
	 * The URI {@link URI#trimFragment without} the fragment, is used to
	 * {@link #getResource resolve} a resource. If the resource resolves, the
	 * {@link URI#fragment fragment} is used to {@link IModel#getObject resolve}
	 * the object.
	 * </p>
	 * 
	 * @param uri
	 *            the URI to resolve.
	 * @param loadOnDemand
	 *            whether to create and load the resource, if it doesn't already
	 *            exists.
	 * @return the object resolved by the URI, or <code>null</code> if there
	 *         isn't one.
	 * @see IModel#getObject(String)
	 * @see #getModel(URI, boolean)
	 * @throws RuntimeException
	 *             if a model can't be demand created.
	 * @throws WrappedException
	 *             if a problem occurs during demand load.
	 */
	IObject getObject(URI uri, boolean loadOnDemand);

	IUnitOfWork getUnitOfWork();

	/**
	 * Returns the converter used to normalize URIs and to open streams.
	 * 
	 * @return the URI converter.
	 * @see IURIConverter
	 * @see URI
	 */
	IURIConverter getURIConverter();

	/**
	 * Returns {@code true} if this model set is persistent else {@code false}
	 * 
	 * @return the state of persistence for this model set
	 */
	boolean isPersistent();

	void removeMetaDataListener(INotificationListener<INotification> listener);

	/**
	 * Remove listener which gets notified on changes of <code>object</code>
	 */
	void removeSubjectListener(IReference object,
			INotificationListener<INotification> listener);

	/**
	 * Sets the registry used for creating models of the appropriate type.
	 * 
	 * @param modelFactoryRegistry
	 *            the new registry.
	 */
	void setModelFactoryRegistry(IModel.Factory.Registry modelFactoryRegistry);

	void setModels(Set<IModel> models);

	/**
	 * Sets the converter used to normalize URIs and to open streams.
	 * 
	 * @param converter
	 *            the new converter.
	 * @see URIConverter
	 * @see URI
	 */
	void setURIConverter(IURIConverter converter);
}
