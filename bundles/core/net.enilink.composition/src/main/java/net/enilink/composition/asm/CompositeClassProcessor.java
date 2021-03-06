/*******************************************************************************
 * Copyright (c) 2010 Fraunhofer IWU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Fraunhofer IWU - initial API and implementation
 *******************************************************************************/
package net.enilink.composition.asm;

/**
 * Interface for processors that implement a transformation
 * of a {@link CompositeClassNode}.
 */
public interface CompositeClassProcessor {
	/**
	 * Applies a transformation to <code>classNode</code>.
	 * 
	 * @param classNode The class to process.
	 * @throws Exception If an error occured during the transformation process.
	 */
	void process(CompositeClassNode classNode) throws Exception;
}