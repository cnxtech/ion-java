// Copyright (c) 2010 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion.impl.lite;

import com.amazon.ion.IonNull;
import com.amazon.ion.IonType;
import com.amazon.ion.ValueVisitor;

/**
 *
 */
public class IonNullLite
    extends IonValueLite
    implements IonNull
{
    private static final int HASH_SIGNATURE =
        IonType.NULL.toString().hashCode();

    /**
     * @param context
     */
    protected IonNullLite(IonContext context)
    {
        super(context, true);
    }

    @Override
    public void accept(final ValueVisitor visitor) throws Exception
    {
        visitor.visit(this);
    }

    /**
     * makes a copy of this IonNull which, as it has no
     * value (as such), is immutable.
     * This calls IonValueImpl to copy the annotations and the
     * field name if appropriate.  The symbol table is not
     * copied as the value is fully materialized and the symbol
     * table is unnecessary.
     */
    @Override
    public IonNullLite clone()
    {
        IonNullLite clone = new IonNullLite(_context.getSystem());

        clone.copyValueContentFrom(this);

        return clone;
    }

    @Override
    public IonType getType()
    {
        return IonType.NULL;
    }

    /**
     * Implements {@link Object#hashCode()} consistent with equals.
     *
     * @return  An int, consistent with the contracts for
     *          {@link Object#hashCode()} and {@link Object#equals(Object)}.
     */
    @Override
    public int hashCode() {
        return HASH_SIGNATURE;
    }

}
