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
package net.enilink.commons.ui.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackage()
			.getName()
			+ ".messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String FormDialog_defaultTitle;
	public static String FormText_copy;
	public static String Form_tooltip_minimize;
	public static String Form_tooltip_restore;
	/*
	 * Message manager
	 */
	public static String MessageManager_sMessageSummary;
	public static String MessageManager_sWarningSummary;
	public static String MessageManager_sErrorSummary;
	public static String MessageManager_pMessageSummary;
	public static String MessageManager_pWarningSummary;
	public static String MessageManager_pErrorSummary;
	public static String ToggleHyperlink_accessibleColumn;
	public static String ToggleHyperlink_accessibleName;
}
