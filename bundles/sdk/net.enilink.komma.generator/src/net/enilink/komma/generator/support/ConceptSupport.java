/*
 * Copyright (c) 2008, 2010, James Leigh All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution. 
 * - Neither the name of the openrdf.org nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.enilink.komma.generator.support;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import net.enilink.composition.traits.Behaviour;

import net.enilink.vocab.rdf.Property;
import net.enilink.vocab.rdfs.Datatype;
import net.enilink.komma.generator.JavaNameResolver;
import net.enilink.komma.generator.builder.JavaCodeBuilder;
import net.enilink.komma.generator.concepts.CodeClass;
import net.enilink.komma.generator.source.JavaClassBuilder;

public abstract class ConceptSupport implements CodeClass, Behaviour<CodeClass> {
	public String generateSourceCode(JavaNameResolver resolver)
			throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		JavaClassBuilder jcb = new JavaClassBuilder(new PrintWriter(baos));
		JavaCodeBuilder builder = new JavaCodeBuilder(jcb, resolver);
		if (getBehaviourDelegate() instanceof Datatype) {
			builder.classHeader(getBehaviourDelegate());
			builder.stringConstructor(getBehaviourDelegate());
		} else {
			builder.interfaceHeader(getBehaviourDelegate());
			builder.constants(getBehaviourDelegate());
			for (Property prop : getDeclaredProperties()) {
				try {
					builder.property(getBehaviourDelegate(), prop);
				} catch (Exception e) {
					throw new Exception("Failed to generate property " + prop
							+ " of class " + getBehaviourDelegate(), e);
				}
			}
		}
		builder.close();
		return baos.toString();
	}
}
