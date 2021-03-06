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

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;

public interface MODELS {
	public static final String NAMESPACE = "http://enilink.net/vocab/komma/models#";
	public static final URI NAMESPACE_URI = URIs.createURI(NAMESPACE);

	public static final URI CLASS_DIAGNOSTIC = NAMESPACE_URI
			.appendFragment("Diagnostic");

	public static final URI PROPERTY_MODEL = NAMESPACE_URI
			.appendFragment("model");

	public static final URI PROPERTY_MODIFIED = NAMESPACE_URI
			.appendFragment("modified");

	public static final URI PROPERTY_LOADED = NAMESPACE_URI
			.appendFragment("loaded");

	public static final URI PROPERTY_ERROR = NAMESPACE_URI
			.appendFragment("error");

	public static final URI PROPERTY_WARNING = NAMESPACE_URI
			.appendFragment("warning");

	public static final URI PROPERTY_METADATACONTEXT = NAMESPACE_URI
			.appendFragment("metaDataContext");

	public static final URI TYPE_MODELSET = NAMESPACE_URI
			.appendFragment("ModelSet");

	public static final URI TYPE_MODEL = NAMESPACE_URI.appendFragment("Model");
}