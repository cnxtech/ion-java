// Copyright (c) 2010 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion.impl.lite;

import com.amazon.ion.IonBool;
import com.amazon.ion.IonType;
import com.amazon.ion.NullValueException;
import com.amazon.ion.ValueVisitor;

/**
 *
 */
public class IonBoolLite
    extends IonValueLite
    implements IonBool
{
    private static final int HASH_SIGNATURE =
        IonType.BOOL.toString().hashCode();

    /**
     * Optimizes out a function call for a const result
     */
    protected static final int TRUE_HASH
            = IonType.BOOL.toString().hashCode() ^ Boolean.TRUE.hashCode();

    /**
     * Optimizes out a function call for a const result
     */
    protected static final int FALSE_HASH
            = IonType.BOOL.toString().hashCode() ^ Boolean.FALSE.hashCode();

    /**
     * Constructs a null bool value.
     */
    public IonBoolLite(IonContext context, boolean isNull)
    {
        super(context, isNull);
    }

    /**
     * makes a copy of this IonBool including a copy
     * of the Boolean value which is "naturally" immutable.
     * This calls IonValueImpl to copy the annotations and the
     * field name if appropriate.  The symbol table is not
     * copied as the value is fully materialized and the symbol
     * table is unnecessary.
     */
    @Override
    public IonBoolLite clone()
    {
        IonBoolLite clone = new IonBoolLite(this._context.getSystem(), this.isNullValue());

        // this copies the flags member which will
        // copy the is null and is bool true state
        // as a "side effect"
        clone.copyValueContentFrom(this);

        return clone;
    }

    @Override
    public IonType getType()
    {
        return IonType.BOOL;
    }

    /**
     * Calculate bool hash code as Java does
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        int hash = HASH_SIGNATURE;
        if (!isNullValue())  {
            hash ^= booleanValue() ? TRUE_HASH : FALSE_HASH;
        }
        return hash;
    }

    public boolean booleanValue()
        throws NullValueException
    {
        validateThisNotNull();
        return this._isBoolTrue();
    }

    public void setValue(boolean b)
    {
        // the called setValue will check if this is locked
        setValue(Boolean.valueOf(b));
    }

    public void setValue(Boolean b)
    {
        checkForLock();
        if (b == null) {
            _isBoolTrue(false);
            _isNullValue(true);
        }
        else {
            _isBoolTrue(b.booleanValue());
            _isNullValue(false);
        }
    }

    @Override
    public void accept(ValueVisitor visitor)
        throws Exception
    {
        visitor.visit(this);
    }
}
