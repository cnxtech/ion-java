// Copyright (c) 2007-2009 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;




public class StringTest
    extends TextTestCase
{
    private static final boolean _debug_output = false;
    public static void testDummy() {
        int u =  0x10400; // 0x10FFFE; // 0xD800 + 2;  // 'a'; //

        char[] c = Character.toChars(u);

        CharBuffer cb = CharBuffer.wrap(c);

        byte[] b = null;
        try {
            b = Charset.forName("UTF-8").newEncoder().encode(cb).array();
        } catch (CharacterCodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int ii=0; ii<c.length; ii++) {
            if (_debug_output) {
                System.out.println("char "+ii+" = "+Integer.toHexString(((c[ii]))));
            }
        }
        for (int ii=0; ii<b.length; ii++) {
            if (_debug_output) {
                System.out.println("byte "+ii+" = "+Integer.toHexString(((b[ii]) & 0xff)));
            }
        }
        return;
    }
    public static void checkNullString(IonString value)
    {
        assertSame(IonType.STRING, value.getType());
        assertTrue("isNullValue() is false",   value.isNullValue());
        assertNull("stringValue() isn't null", value.stringValue());
    }

    public void modifyString(IonString value)
    {
        String modValue = "sweet!";
        assertFalse(modValue.equals(value.stringValue()));

        value.setValue(modValue);
        assertEquals(modValue, value.stringValue());
        assertFalse(value.isNullValue());

        String modValue1 = "dude!";
        value.setValue(modValue1);
        assertEquals(modValue1, value.stringValue());

        value.setValue(null);
        checkNullString(value);
    }


    @Override
    protected String wrap(String ionText)
    {
        return "\"" + ionText + "\"";
    }


    //=========================================================================
    // Test cases


    public void testFactoryString()
    {
        IonString value = system().newNullString();
        checkNullString(value);
        modifyString(value);
    }

    public void testTextNullString()
    {
        IonString value = (IonString) oneValue("null.string");
        checkNullString(value);
        modifyString(value);
    }


    public void testEmptyStrings()
    {
        IonString value = (IonString) oneValue("\"\"");
        assertSame(IonType.STRING, value.getType());
        checkString("", value);
        value = (IonString) oneValue("''''''");
        checkString("", value);
    }


    public void testStringBasics()
    {
        IonString value = (IonString) oneValue("\"hello\"");
        assertSame(IonType.STRING, value.getType());
        assertFalse(value.isNullValue());
        assertEquals("hello", value.stringValue());

        //TODO implement
    }


    public void testTruncatedStrings()
    {
        // Short string
        try
        {
            oneValue(" \"1234");
            fail("Expected UnexpectedEofException");
        }
        catch (UnexpectedEofException e) { }


        // Medium string
        try
        {
            oneValue(" \"1234556789ABCDEF");
            fail("Expected UnexpectedEofException");
        }
        catch (UnexpectedEofException e) { }


        // Long string
        try
        {
            oneValue(" '''1234556789ABCDEF");
            fail("Expected UnexpectedEofException");
        }
        catch (UnexpectedEofException e) { }
    }


    public void testBackslashEof()
    {
        try
        {
            // EOF right after backslash
            oneValue(" \"\\");
            fail("Expected UnexpectedEofException");
        }
        catch (UnexpectedEofException e) { }
    }


    public void testStringEscapes()
    {
        IonString value = (IonString) oneValue(" \"\\\n\"");
        String valString = value.stringValue();
        assertEquals("", valString);

        // Test again with a longer string due to short-string parsing magic.
        value = (IonString) oneValue("\"1234556789ABCDEF\\\nGHI\"");
        valString = value.stringValue();
        assertEquals("1234556789ABCDEFGHI", valString);

        // JSON escapes. See http://www.ietf.org/rfc/rfc4627.txt
        assertEscape('\u0022', '\"');  // quotation mark
        assertEscape('\\',     '\\');  // reverse solidus  u005C
        assertEscape('\u002F', '/');   // solidus
        assertEscape('\u0008', 'b');   // backspace
        assertEscape('\u000C', 'f');   // form feed
        assertEscape('\n',     'n');   // line feed        u000A
        assertEscape('\r',     'r');   // carriage return  u000D
        assertEscape('\u0009', 't');   // tab

        // Non-JSON escapes.

        assertEscape('\u0000', '0');   // nul
        assertEscape('\u0007', 'a');   // bell
        assertEscape('\u000B', 'v');   // vertical tab
        assertEscape('\u003F', '?');   // question mark; thank you C++
        assertEscape('\'',     '\'');  // single quote
    }


    /**
     * No octal, but we do have \0.
     */
    public void testOctal000()
    {
        IonString value = (IonString) oneValue("\"0\\0000\"");
        String str = value.stringValue();
        assertEquals(5, str.length());
        assertEquals('0', str.charAt(0));
        assertEquals( 0 , str.charAt(1));
        assertEquals('0', str.charAt(2));
        assertEquals('0', str.charAt(3));
        assertEquals('0', str.charAt(4));
    }

    public void testUnicodeCharacters()
    {
        String expected;
        IonString value;

        expected = "\u0123 \u1234 \uceed"; // was \ufeed but that's an illegal unicode scalar
        value = (IonString) oneValue('"'+expected+'"');
        checkString(expected, value);

        expected = "\u0000 \u0007 \u0012 \u0123 \u1234 \uceed"; // was \ufeed but that's an illegal unicode scalar
        value = (IonString) oneValue('"'+expected+'"');
        checkString(expected, value);
    }

    public void testTrickyEscapes()
    {
        // Plowing them into a long string changes the parsing/encoding flow.
        // This caught a fairly nasty bug so leave it in.
        IonString value = (IonString) oneValue("\"\\0 \\a \\b \\t \\n \\f \\r \\v \\\" \\' \\? \\\\\\\\\\/\"");
        String expected = "\u0000 \u0007 \b \t \n \f \r \u000B \" \' ? \\\\/";
        checkString(expected, value);

        value = (IonString) oneValue("'''\\0 \\a \\b \\t \\n \\f \\r \\v \\\" \\' \\? \\\\\\\\\\/'''");
        checkString(expected, value);
    }

    public void testReadStringsFromSuite()
        throws Exception
    {
        Iterable<IonValue> values = loadTestFile("good/strings.ion");
        // File is a sequence of many string values.

        for (IonValue value : values)
        {
            assertTrue(value instanceof IonString);
        }
    }

    public void testNewlineInString()
    {
        badValue(" \"123\n456\" ");
        // Get beyond the 13-char threshold.
        badValue(" \"123456789ABCDEF\nGHI\" ");
    }

    public void testTopLevelConcatenation()
    {
        IonValue value = oneValue("  '''a''' '''b'''  ");
        checkString("ab", value);
    }

    public void testQuotesOnMediumStringBoundary()
    {
        // Double-quote falls on the boundary.
        checkString("KIM 12\" X 12\"", oneValue("\"KIM 12\\\" X 12\\\"\""));
        // Try again with long-string syntax.
        checkString("KIM 12\" X 12\"", oneValue("'''KIM 12\\\" X 12\\\"'''"));
    }


    public void testStringClone()
        throws Exception
    {
        testSimpleClone("null.string");
        testSimpleClone("\"\"");
        testSimpleClone("\"root\"");
    }
}
