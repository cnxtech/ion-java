/*
 * Copyright (c) 2007 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion.impl;

import com.amazon.ion.ContainedValueException;
import com.amazon.ion.IonList;
import com.amazon.ion.IonType;
import com.amazon.ion.IonValue;
import com.amazon.ion.ValueVisitor;
import java.util.Collection;

/**
 * Implements the Ion <code>list</code> type.
 */
public final class IonListImpl
    extends IonSequenceImpl
    implements IonList
{
    private static final int NULL_LIST_TYPEDESC =
        IonConstants.makeTypeDescriptor(IonConstants.tidList,
                                        IonConstants.lnIsNullSequence);


    /**
     * Constructs a null list value.
     */
    public IonListImpl()
    {
        this(true);
    }

    /**
     * Constructs a null or empty list.
     *
     * @param makeNull indicates whether this should be <code>null.list</code>
     * (if <code>true</code>) or an empty sequence (if <code>false</code>).
     */
    public IonListImpl(boolean makeNull)
    {
        super(NULL_LIST_TYPEDESC, makeNull);
        assert pos_getType() == IonConstants.tidList;
    }

    /**
     * Constructs a list value <em>not</em> backed by binary.
     *
     * @param elements
     *   the initial set of child elements.  If <code>null</code>, then the new
     *   instance will have <code>{@link #isNullValue()} == true</code>.
     *
     * @throws ContainedValueException if any value in <code>elements</code>
     * has <code>{@link IonValue#getContainer()} != null</code>.
     */
    public IonListImpl(Collection<? extends IonValue> elements)
        throws ContainedValueException
    {
        super(NULL_LIST_TYPEDESC, elements);
    }


    /**
     * Constructs a non-materialized list backed by a binary buffer.
     *
     * @param typeDesc
     */
    public IonListImpl(int typeDesc)
    {
        super(typeDesc);
        assert pos_getType() == IonConstants.tidList;
    }

    /**
     * creates a copy of this IonListImpl.  Most of the work
     * is actually done by IonContainerImpl.copyFrom() and
     * IonValueImpl.copyFrom().
     */
    public IonListImpl clone() throws CloneNotSupportedException
    {
    	IonListImpl clone = new IonListImpl();
    	
    	clone.copyFrom(this);
    	
    	return clone;
    }


    public IonType getType()
    {
        return IonType.LIST;
    }


    public void accept(ValueVisitor visitor)
        throws Exception
    {
        makeReady();
        visitor.visit(this);
    }
}
