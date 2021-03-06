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

import org.objectweb.asm.Type;
import net.enilink.composition.asm.meta.ClassInfo;

/**
 * Represents the mutable structure of a behaviour class.
 */
public class BehaviourClassNode extends ExtendedClassNode {
	public BehaviourClassNode(Type type, Class<?> parentClass,
			ClassInfo parentClassInfo) {
		super(type, parentClass, parentClassInfo);
	}
}
