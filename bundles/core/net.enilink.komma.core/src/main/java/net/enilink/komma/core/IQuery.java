/*
 * Copyright (c) 2007, 2010, James Leigh All rights reserved.
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
package net.enilink.komma.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.enilink.commons.iterator.IExtendedIterator;

/**
 * Interface used to bind and evaluate a query.
 * 
 */
public interface IQuery<R> extends IQueryBase<IQuery<R>>, AutoCloseable {
	/**
	 * Closes any open results from this query.
	 */
	void close();

	/**
	 * Evaluates the query and returns an iterator over the result.
	 * 
	 * @return Iterator over the result of the query.
	 */
	IExtendedIterator<R> evaluate();

	/**
	 * Evaluates the query and returns an iterator over the result.
	 * 
	 * @return Iterator over the result of the query.
	 */
	<T> IExtendedIterator<T> evaluate(Class<T> resultType,
			Class<?>... resultTypes);

	/**
	 * Evaluates the query and returns an iterator over the result.
	 * 
	 * @return Iterator over the result of the query.
	 */
	<T> IExtendedIterator<T> evaluateRestricted(Class<T> resultType,
			Class<?>... resultTypes);

	/**
	 * Asserts the given <code>resultTypes</code> for the result values. This
	 * ensures that the values have at least the <code>resultTypes</code>.
	 * 
	 * @param resultTypes
	 *            The types for the result values
	 * @return The query object
	 */
	<T> IQuery<T> bindResultType(Class<T> resultType, Class<?>... resultTypes);

	/**
	 * Restricts the types of the result values to the given
	 * <code>resultTypes</code>. This ensures that no additional queries are
	 * executed to retrieve the actual types of the values.
	 * 
	 * @param resultTypes
	 *            The types for the result values
	 * @return The query object
	 */
	<T> IQuery<T> restrictResultType(Class<T> resultType,
			Class<?>... resultTypes);

	/**
	 * Evaluates the query and returns the first result as <code>boolean</code>
	 * value.
	 * 
	 * @return The first result from the query as <code>boolean</code> value.
	 */
	boolean getBooleanResult();

	/**
	 * Get the properties and associated values that are in effect for the query
	 * instance.
	 * 
	 * @return query properties
	 */
	Map<String, Object> getProperties();

	/**
	 * Get the current lock mode for the query.
	 * 
	 * @return lock mode
	 * @throws IllegalStateException
	 *             if the query is found not to be a Java Persistence query
	 *             language SELECT query or a Criteria API query
	 */
	LockModeType getLockMode();

	/**
	 * Evaluates the query and returns the results disconnected from the query.
	 * 
	 * @return The results from the query.
	 */
	List<R> getResultList();

	/**
	 * Evaluates the query and returns the first result.
	 * 
	 * @return The first result from the query.
	 */
	R getSingleResult();

	/**
	 * Evaluates the query and returns the first result.
	 * 
	 * @return The first result from the query.
	 */
	<T> T getSingleResult(Class<T> resultType);

	/**
	 * Get the names of the properties that are supported for query objects.
	 * These include all standard query properties as well as vendor-specific
	 * properties supported by the provider. These properties may or may not
	 * currently be in effect.
	 * 
	 * @return properties
	 */
	Set<String> getSupportedProperties();

	/**
	 * Set a query property. If a vendor-specific property is not recognized, it
	 * is silently ignored. Depending on the database in use and the locking
	 * mechanisms used by the provider, the property may or may not be observed.
	 * 
	 * @param propertyName
	 * @param value
	 * @return the same query instance
	 * @throws IllegalArgumentException
	 *             if the second argument is not valid for the implementation
	 */
	IQuery<R> setProperty(String propertyName, Object value);

	/**
	 * Set the lock mode type to be used for the query execution.
	 * 
	 * @param lockMode
	 * @throws IllegalStateException
	 *             if the query is found not to be a Java Persistence query
	 *             language SELECT query or a Criteria API query
	 */
	IQuery<R> setLockMode(LockModeType lockMode);

	/**
	 * Assigns a concept to the given name.
	 * 
	 * @param name
	 *            Name of the variable to bind to.
	 * @param concept
	 *            Registered concept.
	 */
	IQuery<R> setTypeParameter(String name, Class<?> concept);
}
