/*******************************************************************************
 * Copyright (c) 2009 Fraunhofer IWU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Fraunhofer IWU - initial API and implementation
 *******************************************************************************/
package net.enilink.commons.util.extensions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;

import net.enilink.commons.util.CommonsUtilPlugin;

/**
 * Template implementation of a registry reader that creates objects
 * representing registry contents. Typically, an extension contains one element,
 * but this reader handles multiple elements per extension.
 * 
 * To start reading the extensions from the registry for an extension point,
 * call the method <code>readRegistry</code>.
 * 
 * To read children of an IConfigurationElement, call the method
 * <code>readElementChildren</code> from your implementation of the method
 * <code>readElement</code>, as it will not be done by default.
 */
public abstract class RegistryReader implements IRegistryChangeListener {
	protected static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

	private boolean isInitialized;
	private final String extensionPointId;
	private final String pluginId;
	private final IExtensionRegistry registry;

	/**
	 * The constructor.
	 */
	protected RegistryReader(IExtensionRegistry registry, String pluginId,
			String extensionPoint) {
		this.registry = registry;
		this.pluginId = pluginId;
		this.extensionPointId = extensionPoint;
	}

	/**
	 * The constructor.
	 */
	protected RegistryReader(String pluginId, String extensionPoint) {
		this.registry = RegistryFactoryHelper.getRegistry();
		this.pluginId = pluginId;
		this.extensionPointId = extensionPoint;
	}

	/**
	 * This method extracts description as a subelement of the given element.
	 * 
	 * @return description string if defined, or empty string if not.
	 */
	protected String getDescription(IConfigurationElement config) {
		IConfigurationElement[] children = config.getChildren(TAG_DESCRIPTION);
		if (children.length >= 1) {
			return children[0].getValue();
		}
		return "";//$NON-NLS-1$
	}

	/**
	 * Logs the error in the workbench log using the provided text and the
	 * information in the configuration element.
	 */
	protected static void logError(IConfigurationElement element, String text) {
		IExtension extension = element.getDeclaringExtension();
		StringBuffer buf = new StringBuffer();
		buf
				.append("Plugin " + extension.getNamespaceIdentifier() + ", extension " + extension.getExtensionPointUniqueIdentifier());//$NON-NLS-2$//$NON-NLS-1$
		buf.append("\n" + text);//$NON-NLS-1$

		CommonsUtilPlugin.logErrorMessage(buf.toString());
	}

	/**
	 * Logs a very common registry error when a required attribute is missing.
	 */
	protected static void logMissingAttribute(IConfigurationElement element,
			String attributeName) {
		logError(element,
				"Required attribute '" + attributeName + "' not defined");//$NON-NLS-2$//$NON-NLS-1$
	}

	/**
	 * Logs a very common registry error when a required child is missing.
	 */
	protected static void logMissingElement(IConfigurationElement element,
			String elementName) {
		logError(element,
				"Required sub element '" + elementName + "' not defined");//$NON-NLS-2$//$NON-NLS-1$
	}

	/**
	 * Logs a registry error when the configuration element is unknown.
	 */
	protected static void logUnknownElement(IConfigurationElement element) {
		logError(element, "Unknown extension tag found: " + element.getName());//$NON-NLS-1$
	}

	/**
	 * Apply a reproducable order to the list of extensions provided, such that
	 * the order will not change as extensions are added or removed.
	 */
	protected IExtension[] orderExtensions(IExtension[] extensions) {
		// By default, the order is based on plugin id sorted
		// in ascending order. The order for a plugin providing
		// more than one extension for an extension point is
		// dependent in the order listed in the XML file.
		IExtension[] sortedExtension = new IExtension[extensions.length];
		System.arraycopy(extensions, 0, sortedExtension, 0, extensions.length);
		Comparator<Object> comparer = new Comparator<Object>() {
			public int compare(Object a, Object b) {
				String s1 = ((IExtension) a).getNamespaceIdentifier();
				String s2 = ((IExtension) b).getNamespaceIdentifier();
				return s1.compareToIgnoreCase(s2);
			}
		};
		Collections.sort(Arrays.asList(sortedExtension), comparer);
		return sortedExtension;
	}

	/**
	 * Implement this method to read element's attributes. If children should
	 * also be read, then implementor is responsible for calling
	 * <code>readElementChildren</code>. Implementor is also responsible for
	 * logging missing attributes.
	 * 
	 * @return true if element was recognized, false if not.
	 */
	protected boolean readElement(IConfigurationElement element) {
		return false;
	}

	/**
	 * Implement this method to read element attributes for the purpose of
	 * adding or removing their registrations. If this element has child
	 * elements, the reader will recursively cycle through them and will call
	 * this method, so don't do it here.
	 */
	protected boolean readElement(IConfigurationElement element, boolean add) {
		return add && readElement(element);
	}

	/**
	 * Read the element's children. This is called by the subclass' readElement
	 * method when it wants to read the children of the element.
	 */
	protected void readElementChildren(IConfigurationElement element,
			boolean add) {
		readElements(element.getChildren(), add);
	}

	/**
	 * Read each element one at a time by calling the subclass implementation of
	 * <code>readElement</code>.
	 * 
	 * Logs an error if the element was not recognized.
	 */
	protected void readElements(IConfigurationElement[] elements, boolean add) {
		for (int i = 0; i < elements.length; i++) {
			if (!readElement(elements[i], add)) {
				logUnknownElement(elements[i]);
			}
		}
	}

	/**
	 * Read one extension by looping through its configuration elements.
	 */
	protected void readExtension(IExtension extension, boolean add) {
		readElements(extension.getConfigurationElements(), add);
	}

	/**
	 * Start the registry reading process using the supplied plugin ID and
	 * extension point.
	 */
	public void readRegistry() {
		if (isInitialized) {
			return;
		}
		synchronized (this) {
			if (!isInitialized) {
				IExtensionPoint point = registry.getExtensionPoint(pluginId,
						extensionPointId);
				if (point == null) {
					return;
				}
				IExtension[] extensions = point.getExtensions();
				extensions = orderExtensions(extensions);
				for (int i = 0; i < extensions.length; i++) {
					readExtension(extensions[i], true);
				}
				isInitialized = true;
			}

		}
	}

	/**
	 * Registers change listener to extension registry
	 */
	public void registerListener() {
		registry.addRegistryChangeListener(this);
	}

	/**
	 * Unregisters change listener from extension registry
	 */
	public void unregisterListener() {
		registry.removeRegistryChangeListener(this);
	}

	public void registryChanged(IRegistryChangeEvent event) {
		String qualifiedExtensionPointId = pluginId + "." + extensionPointId;

		IExtensionDelta[] deltas = event.getExtensionDeltas();
		for (int i = 0; i < deltas.length; ++i) {
			IExtensionDelta delta = deltas[i];
			if (delta.getExtensionPoint().getUniqueIdentifier().equals(
					qualifiedExtensionPointId)) {
				boolean add = delta.getKind() == IExtensionDelta.ADDED;
				readExtension(delta.getExtension(), add);
			}
		}
	}

}
