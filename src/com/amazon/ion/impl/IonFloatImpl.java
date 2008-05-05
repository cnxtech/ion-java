/*
 * Copyright (c) 2007 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion.impl;

import com.amazon.ion.IonException;
import com.amazon.ion.IonFloat;
import com.amazon.ion.IonType;
import com.amazon.ion.NullValueException;
import com.amazon.ion.ValueVisitor;
import java.io.IOException;
import java.math.BigDecimal;


/**
 * Implements the Ion <code>float</code> type.
 */
public final class IonFloatImpl
    extends IonValueImpl
    implements IonFloat
{
    static final int NULL_FLOAT_TYPEDESC =
        IonConstants.makeTypeDescriptor(IonConstants.tidFloat,
                                        IonConstants.lnIsNullAtom);

    static private final Double ZERO_DOUBLE = new Double(0);

    // not needed: static private final int SIZE_OF_IEEE_754_64_BITS = 8;


    private Double _float_value;

    /**
     * Constructs a <code>null.float</code> element.
     */
    public IonFloatImpl()
    {
        super(NULL_FLOAT_TYPEDESC);
    }

    /**
     * Constructs a binary-backed element.
     */
    public IonFloatImpl(int typeDesc)
    {
        super(typeDesc);
        assert pos_getType() == IonConstants.tidFloat;
//        assert pos_getLowNibble() == IonConstants.lnIsNullAtom
//            || pos_getLowNibble() == SIZE_OF_IEEE_754_64_BITS;
    }


    public IonType getType()
    {
        return IonType.FLOAT;
    }


    public float floatValue()
        throws NullValueException
    {
        makeReady();
        if (_float_value == null) throw new NullValueException();
        return _float_value.floatValue();
    }

    public double doubleValue()
        throws NullValueException
    {
        makeReady();
        if (_float_value == null) throw new NullValueException();
        return _float_value.doubleValue();
    }

    public BigDecimal toBigDecimal()
        throws NullValueException
    {
        makeReady();
        if (_float_value == null) return null;
        return new BigDecimal(_float_value.doubleValue());
    }

    public void setValue(float value)
    {
        setValue(new Double(value));
    }

    public void setValue(double value)
    {
        setValue(new Double(value));
    }

    public void setValue(BigDecimal value)
    {
        if (value == null)
        {
            _float_value = null;
            _hasNativeValue = true;
            setDirty();
        }
        else
        {
            setValue(value.doubleValue());
        }
    }

    public void setValue(Double d)
    {
        _float_value = d;
        _hasNativeValue = true;
        setDirty();
    }

    @Override
    public synchronized boolean isNullValue()
    {
        if (!_hasNativeValue) return super.isNullValue();
        return (_float_value == null);
    }

    @Override
    protected int getNativeValueLength()
    {
        assert _hasNativeValue == true;
        return IonBinary.lenIonFloat(_float_value);
    }

    @Override
    protected int computeLowNibble(int valuelen)
    {
        assert _hasNativeValue == true;

        int ln = 0;
        if (_float_value == null) {
            ln = IonConstants.lnIsNullAtom;
        }
        else if (_float_value.equals(0)) {
            ln = IonConstants.lnNumericZero;
        }
        else {
            ln = getNativeValueLength();
            if (ln > IonConstants.lnIsVarLen) {
                ln = IonConstants.lnIsVarLen;
            }
        }
        return ln;
    }


    @Override
    protected void doMaterializeValue(IonBinary.Reader reader) throws IOException
    {
        assert this._isPositionLoaded == true && this._buffer != null;

        // a native value trumps a buffered value
        if (_hasNativeValue) return;

        // the reader will have been positioned for us
        assert reader.position() == this.pos_getOffsetAtValueTD();

        // we need to skip over the td to get to the good stuff
        int td = reader.read();
        assert (byte)(0xff & td) == this.pos_getTypeDescriptorByte();

        int type = this.pos_getType();
        if (type != IonConstants.tidFloat) {
            throw new IonException("invalid type desc encountered for float");
        }
        int ln = this.pos_getLowNibble();
        switch ((0xf & ln)) {
        case IonConstants.lnIsNullAtom:
            _float_value = null;
            break;
        case 0:
            _float_value = ZERO_DOUBLE;
            break;
        case IonConstants.lnIsVarLen:
            ln = reader.readVarUInt7IntValue();
            // fall through to default:
        default:
            _float_value = new Double(reader.readFloatValue(ln));
            break;
        }
        _hasNativeValue = true;
    }

    @Override
    protected void doWriteNakedValue(IonBinary.Writer writer, int valueLen) throws IOException
    {
        assert valueLen == this.getNakedValueLength();
        assert valueLen > 0;

        int wlen = writer.writeFloatValue(_float_value);
        assert wlen == valueLen;
    }


    public void accept(ValueVisitor visitor) throws Exception
    {
        makeReady();
        visitor.visit(this);
    }
}
