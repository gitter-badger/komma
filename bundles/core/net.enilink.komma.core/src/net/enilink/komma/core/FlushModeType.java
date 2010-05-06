/*******************************************************************************
 * Copyright (c) 1998, 2010 Oracle. All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * 
 * Contributors:
 *     dclarke - Java Persistence 2.0 - Proposed Final Draft (March 13, 2009)
 *     		     Specification available from http://jcp.org/en/jsr/detail?id=317
 *
 * Java(TM) Persistence API, Version 2.0 - EARLY ACCESS
 * This is an implementation of an early-draft specification developed under the 
 * Java Community Process (JCP).  The code is untested and presumed not to be a  
 * compatible implementation of JSR 317: Java(TM) Persistence API, Version 2.0.   
 * We encourage you to migrate to an implementation of the Java(TM) Persistence 
 * API, Version 2.0 Specification that has been tested and verified to be compatible 
 * as soon as such an implementation is available, and we encourage you to retain 
 * this notice in any implementation of Java(TM) Persistence API, Version 2.0 
 * Specification that you distribute.
 ******************************************************************************/
package net.enilink.komma.core;

/**
 * Flush mode setting.
 *
 * <p> When queries are executed within a transaction, if 
 * <code>FlushModeType.AUTO</code> is set on the {@link Query} 
 * object, or if the flush mode setting for the persistence context 
 * is <code>AUTO</code> (the default) and a flush mode setting has 
 * not been specified for the {@link Query} object, the persistence 
 * provider is responsible for ensuring that all updates to the state 
 * of all entities in the persistence context which could potentially 
 * affect the result of the query are visible to the processing 
 * of the query. The persistence provider implementation may achieve 
 * this by flushing those entities to the database or by some other 
 * means. If <code>FlushModeType.COMMIT</code> is set, the effect 
 * of updates made to entities in the persistence context upon 
 * queries is unspecified.
 *
 * <p> If there is no transaction active, the persistence provider 
 * must not flush to the database.
 *
 * @since Java Persistence 1.0
 */
public enum FlushModeType {

    /** Flushing must occur only at transaction commit */
    COMMIT,

    /** (Default) Flushing to occur at query execution */
    AUTO
}
