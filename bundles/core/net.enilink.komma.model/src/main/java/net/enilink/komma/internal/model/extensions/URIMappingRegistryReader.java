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

import net.enilink.komma.model.ModelPlugin;
import net.enilink.komma.model.base.IURIMapRuleSet;
import net.enilink.komma.model.base.RegExURIMapRule;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * A plugin extension reader that populates the global mapping registry. Clients
 * are not expected to use this class directly.
 */
public class URIMappingRegistryReader extends KommaRegistryReader {
	static final String TAG_MAPPING = "rule";
	static final String ATT_PRIORITY = "priority";
	static final String ATT_PATTERN = "pattern";
	static final String ATT_REPLACEMENT = "replacement";
	static final String ATT_IFPATTERN = "ifPattern";
	static final String ATT_UNLESSPATTERN = "unlessPattern";

	private IURIMapRuleSet uriMap;

	public URIMappingRegistryReader(IURIMapRuleSet uriMap) {
		super(ModelPlugin.PLUGIN_ID, "uriMapRules");
		this.uriMap = uriMap;
	}

	@Override
	protected boolean readElement(IConfigurationElement element, boolean add) {
		if (element.getName().equals(TAG_MAPPING)) {
			String pattern = element.getAttribute(ATT_PATTERN);
			if (pattern == null) {
				logMissingAttribute(element, ATT_PATTERN);
			} else {
				String replacement = element.getAttribute(ATT_REPLACEMENT);
				if (replacement == null) {
					logMissingAttribute(element, ATT_REPLACEMENT);
				} else {
					String priority = element.getAttribute(ATT_PRIORITY);
					if (priority == null) {
						logMissingAttribute(element, ATT_PRIORITY);
					} else {
						RegExURIMapRule rule = new RegExURIMapRule(pattern,
								replacement, Integer.parseInt(priority));

						String ifPattern = element.getAttribute(ATT_IFPATTERN);
						if (ifPattern != null) {
							rule.setIfPattern(ifPattern);
						}

						String unlessPattern = element
								.getAttribute(ATT_UNLESSPATTERN);
						if (unlessPattern != null) {
							rule.setUnlessPattern(unlessPattern);
						}

						if (add) {
							uriMap.addRule(rule);
						} else {
							uriMap.removeRule(rule);
						}
						return true;
					}
				}
			}
		}

		return false;
	}
}
