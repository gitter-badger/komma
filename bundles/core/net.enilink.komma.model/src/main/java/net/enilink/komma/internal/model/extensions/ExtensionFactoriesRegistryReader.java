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
package net.enilink.komma.internal.model.extensions;

import net.enilink.komma.model.IModel;
import net.enilink.komma.model.ModelPlugin;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A plugin extension reader that populates the
 * {@link org.eclipse.ModelSetFactory.ecore.resource.Resource.Factory.Registry#INSTANCE
 * global} resource factory's
 * {@link org.eclipse.ModelSetFactory.ecore.resource.Resource.Factory.Registry#getExtensionToFactoryMap()
 * extension} map. Clients are not expected to use this class directly.
 */
public class ExtensionFactoriesRegistryReader extends KommaRegistryReader {
	static final String TAG_FACTORY = "factory";
	static final String ATT_TYPE = "type";
	static final String ATT_CLASS = "class";

	private IModel.Factory.Registry modelFactoryRegistry;

	public ExtensionFactoriesRegistryReader(
			IModel.Factory.Registry ontologyFactoryRegistry) {
		super(ModelPlugin.PLUGIN_ID, "extensionFactories");
		this.modelFactoryRegistry = ontologyFactoryRegistry;
	}

	@Override
	protected boolean readElement(IConfigurationElement element, boolean add) {
		if (element.getName().equals(TAG_FACTORY)) {
			String type = element.getAttribute(ATT_TYPE);
			if (type == null) {
				logMissingAttribute(element, ATT_TYPE);
			} else if (element.getAttribute(ATT_CLASS) == null) {
				logMissingAttribute(element, ATT_CLASS);
			} else if (add) {
				Object previous = modelFactoryRegistry
						.getExtensionToFactoryMap().put(type,
								new ModelFactoryDescriptor(element, ATT_CLASS));
				if (previous instanceof ModelFactoryDescriptor) {
					ModelFactoryDescriptor descriptor = (ModelFactoryDescriptor) previous;
					ModelPlugin.logErrorMessage("Both '"
							+ descriptor.element.getContributor().getName()
							+ "' and '" + element.getContributor().getName()
							+ "' register an extension parser for '" + type
							+ "'");
				}
				return true;
			} else {
				modelFactoryRegistry.getExtensionToFactoryMap().remove(type);
				return true;
			}
		}
		return false;
	}
}
