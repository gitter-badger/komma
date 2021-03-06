/*
 * (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Hewlett-Packard
 * Development Company, LP All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.enilink.commons.iterator;

import java.util.Iterator;

/**
 * An iterator that consumes an underlying iterator and maps its results before
 * delivering them; supports remove if the underlying iterator does.
 */
public class MappedIterator<A, B> extends NiceIterator<B> {
	private Iterator<? extends A> base;
	private IMap<? super A, ? extends B> map;

	/**
	 * Construct a list of the converted.
	 * 
	 * @param m
	 *            The conversion to apply.
	 * @param it
	 *            the iterator of elements to convert
	 */
	public MappedIterator(IMap<? super A, ? extends B> m,
			Iterator<? extends A> it) {
		map = m;
		base = it;
	}

	public B next() {
		return map.map(base.next());
	}

	/** hasNext: defer to the base iterator */
	public boolean hasNext() {
		return base.hasNext();
	}

	/**
	 * if .remove() is allowed, delegate to the base iterator's .remove;
	 * otherwise, throw an UnsupportedOperationException.
	 */
	public void remove() {
		base.remove();
	}

	/** close: defer to the base, iff it is closable */
	public void close() {
		WrappedIterator.close(base);
	}
}
