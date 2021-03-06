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
package net.enilink.komma.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import net.enilink.commons.util.extensions.RegistryFactoryHelper;
import net.enilink.commons.util.extensions.RegistryReader;
import net.enilink.komma.common.AbstractKommaPlugin;
import net.enilink.komma.common.util.IResourceLocator;
import net.enilink.komma.core.KommaModule;
import net.enilink.komma.internal.model.ModelModule;
import net.enilink.komma.internal.model.extensions.ContentFactoriesRegistryReader;
import net.enilink.komma.internal.model.extensions.ContentHandlerRegistryReader;
import net.enilink.komma.internal.model.extensions.ExtensionFactoriesRegistryReader;
import net.enilink.komma.internal.model.extensions.ProtocolFactoriesRegistryReader;
import net.enilink.komma.internal.model.extensions.URIMappingRegistryReader;
import net.enilink.komma.model.base.ContentHandlerRegistry;
import net.enilink.komma.model.base.IURIMapRuleSet;
import net.enilink.komma.model.base.ModelFactoryRegistry;
import net.enilink.komma.model.base.ModelSetSupport;
import net.enilink.komma.model.base.ModelSupport;
import net.enilink.komma.model.base.URIMapRuleSet;
import net.enilink.komma.model.validation.Diagnostician;
import net.enilink.komma.model.validation.IValidator;
import net.enilink.komma.model.validation.ValidatorRegistry;

/**
 * The activator class controls the plug-in life cycle
 */
public class ModelPlugin extends AbstractKommaPlugin {
	private IValidator.Registry validatorRegistry = new ValidatorRegistry();

	private IContentHandler.Registry contentHandlerRegistry = new ContentHandlerRegistry();

	private IModel.Factory.Registry modelFactoryRegistry = new ModelFactoryRegistry();

	private IURIMapRuleSet uriMap = new URIMapRuleSet();
	
	/**
	 * Readers for the various extension points (URI mapping rules, content
	 * types, protocols, ...).
	 */
	private List<RegistryReader> extensionReaders = Collections.emptyList();

	// The plug-in ID
	public static final String PLUGIN_ID = "net.enilink.komma.model";

	private static final ModelPlugin INSTANCE = new ModelPlugin();

	static {
		if (!IS_OSGI_RUNNING) {
			getDefault().readExtensions();
		}
	}

	/**
	 * The constructor
	 */
	public ModelPlugin() {
		super(new IResourceLocator[] {});
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ModelPlugin getDefault() {
		return INSTANCE;
	}
	

	public static void logErrorMessage(String message) {
		getDefault().log(new Status(IStatus.ERROR, PLUGIN_ID, IModelStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi = new MultiStatus(PLUGIN_ID, IModelStatusConstants.INTERNAL_ERROR, message, null);
		multi.add(status);
		getDefault().log(multi);
	}

	public static void log(Throwable e) {
		getDefault().log(new Status(IStatus.ERROR, PLUGIN_ID, IModelStatusConstants.INTERNAL_ERROR,
				"Internal error", e));
	}

	@Override
	public IResourceLocator getBundleResourceLocator() {
		return plugin;
	}

	public IContentHandler.Registry getContentHandlerRegistry() {
		return contentHandlerRegistry;
	}

	public IModel.Factory.Registry getModelFactoryRegistry() {
		return modelFactoryRegistry;
	}

	public IURIMapRuleSet getURIMap() {
		return uriMap;
	}

	public IValidator.Registry getValidatorRegistry() {
		return validatorRegistry;
	}

	private Diagnostician diagnosticion;

	public IValidator getDefaultValidator() {
		if (diagnosticion == null) {
			diagnosticion = new Diagnostician(getValidatorRegistry());
		}
		return diagnosticion;
	}
	
	public static IContentTypeManager getContentTypeManager() {
		return plugin == null ? null : plugin.getContentTypeManager();
	}

	/**
	 * The plugin singleton
	 */
	private static Implementation plugin;

	/**
	 * A plugin implementation that handles Ecore plugin registration.
	 * 
	 * @see #startup()
	 */
	static public class Implementation extends EclipsePlugin {
		private ServiceTracker<IContentTypeManager, IContentTypeManager> contentTypeManagerTracker = null;
		
		/**
		 * Creates the singleton instance.
		 */
		public Implementation() {
			plugin = this;
		}

		/**
		 * Starts up this plugin by reading some extensions and populating the
		 * relevant registries.
		 * <p>
		 * The {@link org.eclipse.emf.ecore.EPackage.Registry#INSTANCE global}
		 * package registry is populated by plugin registration of the form:
		 * 
		 * <pre>
		 *  &lt;extension point=&quot;org.eclipse.emf.ecore.generated_package&quot; &gt;
		 *      &lt;package uri=&quot;http://www.example.org/abc/Abc.ecore&quot; class=&quot;org.example.abc.AbcPackage&quot;/&gt;
		 *  &lt;extension&gt;
		 * </pre>
		 * 
		 * </p>
		 * The URI is arbitrary but an absolute URI is recommended. Provision
		 * for access to the serialized model via <code>"http:"</code> is
		 * encouraged.
		 * <p>
		 * The
		 * {@link org.eclipse.ModelSetFactory.ecore.resource.Resource.Factory.Registry#INSTANCE
		 * global} resource factory registry's
		 * {@link org.eclipse.ModelSetFactory.ecore.resource.Resource.Factory.Registry#getExtensionToFactoryMap()
		 * extension} map is populated by plugin registration of the form:
		 * 
		 * <pre>
		 *  &lt;extension point=&quot;org.eclipse.emf.ecore.extension_parser&quot;&gt;
		 *      &lt;parser type=&quot;abc&quot; class=&quot;org.example.abc.util.AbcResourceFactoryImpl&quot;/&gt;
		 *  &lt;extension&gt;
		 * </pre>
		 * 
		 * </p>
		 * <p>
		 * The
		 * {@link org.eclipse.ModelSetFactory.ecore.resource.Resource.Factory.Registry#INSTANCE
		 * global} resource factory registry's
		 * {@link org.eclipse.ModelSetFactory.ecore.resource.Resource.Factory.Registry#getProtocolToFactoryMap()
		 * protocol} map is populated by plugin registration of the form:
		 * 
		 * <pre>
		 *  &lt;extension point=&quot;org.eclipse.emf.ecore.protocol_parser&quot; &gt;
		 *      &lt;parser protocolName=&quot;abc&quot; class=&quot;org.example.abc.util.AbcResourceFactoryImpl&quot;/&gt;
		 *  &lt;extension&gt;
		 * </pre>
		 * 
		 * </p>
		 * <p>
		 * The {@link org.eclipse.emf.ecore.resource.URIConverter#URI_MAP
		 * global} URI map is populated by plugin registration of the form:
		 * 
		 * <pre>
		 *  &lt;extension point=&quot;org.eclipse.emf.ecore.uri_mapping&quot; &gt;
		 *      &lt;mapping source=&quot;//special/&quot; target=&quot;special/&quot;/&gt;
		 *  &lt;extension&gt;
		 * </pre>
		 * 
		 * If the target is relative, it is resolved against the plugin's
		 * installed location, resulting in a URI of the form:
		 * 
		 * <pre>
		 *  platform:/plugin/plugin-name_1.2.3/...
		 * </pre>
		 * 
		 * The above registration would map
		 * 
		 * <pre>
		 * // special/a/b.c
		 * </pre>
		 * 
		 * to
		 * 
		 * <pre>
		 *  platform:/plugin/plugin-name_1.2.3/special/a/b.c
		 * </pre>
		 * 
		 * </p>
		 * 
		 * @throws Exception
		 *             if there is a show stopping problem.
		 */
		@Override
		public void start(BundleContext context) throws Exception {
			super.start(context);
			contentTypeManagerTracker = new ServiceTracker<>(context, IContentTypeManager.class, null);
			contentTypeManagerTracker.open();
			getDefault().readExtensions();
		}

		@Override
		public void stop(BundleContext ctx) throws Exception {
			// remove extension registry listeners
			getDefault().extensionReaders.forEach(reader -> reader.unregisterListener());
			if (contentTypeManagerTracker != null) {
				contentTypeManagerTracker.close();
				contentTypeManagerTracker = null;
			}
			super.stop(ctx);
		}
		
		/**
		 * Lazy initialize ContentTypeManager - it can only be used after the registry is up and running
		 */
		public IContentTypeManager getContentTypeManager() {
			return contentTypeManagerTracker == null ? null : (IContentTypeManager) contentTypeManagerTracker.getService();
		}
	}

	/**
	 * Initialize registered extensions.
	 */
	private void readExtensions() {
		IModel.Factory.Registry modelFactoryRegistry = getDefault().getModelFactoryRegistry();
		// keep track of extension registry readers, used as listeners
		extensionReaders = Arrays.asList( //
				new ExtensionFactoriesRegistryReader(modelFactoryRegistry),
				new ProtocolFactoriesRegistryReader(modelFactoryRegistry),
				new ContentFactoriesRegistryReader(modelFactoryRegistry),
				new ContentHandlerRegistryReader(getDefault().getContentHandlerRegistry()),
				new URIMappingRegistryReader(getDefault().getURIMap()));

		extensionReaders.forEach(reader -> {
			// read already existing elements from the extension registry
			reader.readRegistry();
			// listen to future updates
			reader.registerListener();
		});
	}

	/**
	 * Returns models which are registered as individual ones
	 * 
	 * @return base models
	 */
	public static Collection<ModelDescription> getBaseModels() {
		List<ModelDescription> descriptions = new ArrayList<ModelDescription>();

		IExtensionPoint extensionPoint = RegistryFactoryHelper.getRegistry().getExtensionPoint(PLUGIN_ID, "models");
		if (extensionPoint != null) {
			// Loop through the config elements.
			for (IConfigurationElement configElement : extensionPoint.getConfigurationElements()) {
				ModelDescription modelDescription = new ModelDescription(configElement);

				if (modelDescription.getNamespace() == null) {
					logErrorMessage("Attribute namespace required for modelDescription: " + configElement);
					continue;
				}

				descriptions.add(modelDescription);
			}
		}

		return descriptions;
	}

	public static KommaModule createModelSetModule(ClassLoader classLoader) {
		KommaModule module = new KommaModule(classLoader);

		module.addConcept(ModelSupport.class, MODELS.TYPE_MODEL.toString());
		module.addConcept(ModelSetSupport.class, MODELS.TYPE_MODELSET.toString());
		module.addConcept(IModel.IDiagnostic.class, MODELS.CLASS_DIAGNOSTIC.toString());

		for (KommaModule modelModule : getModelModules()) {
			module.includeModule(modelModule);
		}
		return module;
	}

	/**
	 * Returns KommaModules with concepts and behaviours for ModelSets and
	 * Models
	 * 
	 * @return model modules
	 */
	public static Collection<? extends KommaModule> getModelModules() {
		List<KommaModule> modules = new ArrayList<KommaModule>();
		if (RegistryFactoryHelper.getRegistry() != null) {
			IExtensionPoint extensionPoint = RegistryFactoryHelper.getRegistry().getExtensionPoint(PLUGIN_ID,
					"modelModules");
			if (extensionPoint != null) {
				// Loop through the config elements.
				for (IConfigurationElement configElement : extensionPoint.getConfigurationElements()) {
					String className = configElement.getAttribute("class");

					if (className == null) {
						logErrorMessage("Attribute class required for module: " + configElement);
						continue;
					}

					try {
						modules.add((KommaModule) configElement.createExecutableExtension("class"));
					} catch (CoreException e) {
						log(e);
					}
				}
			}
		}
		// add at least a basic module with concepts and behaviours if no extensions were found
		if (modules.isEmpty()) {
			modules.add(new ModelModule());
		}
		return modules;
	}
}
