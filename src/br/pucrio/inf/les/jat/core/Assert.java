package br.pucrio.inf.les.jat.core;

import br.pucrio.inf.les.jat.core.exception.AssertionContentFailed;
import br.pucrio.inf.les.jat.core.exception.ComparisonContentFailure;

/**
 * A set of assert methods. Messages are only displayed when an assert fails.
 */

public class Assert {
	/**
	 * Protect constructor since it is a static only class
	 */
	public Assert() {
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * AssertionFailedError with the given message.
	 */
	public void assertTrue(String message, boolean condition) throws AssertionContentFailed {
		if (!condition)
			fail(message);
	}

	/**
	 * Asserts that a condition is true. If it isn't it throws an
	 * AssertionFailedError.
	 */
	public void assertTrue(boolean condition) throws AssertionContentFailed {
		assertTrue(null, condition);
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * AssertionFailedError with the given message.
	 */
	public void assertFalse(String message, boolean condition) throws AssertionContentFailed {
		assertTrue(message, !condition);
	}

	/**
	 * Asserts that a condition is false. If it isn't it throws an
	 * AssertionFailedError.
	 */
	public void assertFalse(boolean condition) throws AssertionContentFailed {
		assertFalse(null, condition);
	}

	/**
	 * Fails a test with the given message.
	 */
	public void fail(String message) throws AssertionContentFailed {
		throw new AssertionContentFailed(message);
	}

	/**
	 * Fails a test with no message.
	 */
	public void fail() throws AssertionContentFailed {
		fail(null);
	}

	/**
	 * Asserts that two objects are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	public void assertEquals(String message, Object expected, Object actual) throws AssertionContentFailed {
		if (expected == null && actual == null)
			return;
		if (expected != null && expected.equals(actual))
			return;
		failNotEquals(message, expected, actual);
	}

	/**
	 * Asserts that two objects are equal. If they are not an AssertionFailedError
	 * is thrown.
	 */
	public void assertEquals(Object expected, Object actual) throws AssertionContentFailed {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two Strings are equal.
	 */
	public void assertEquals(String message, String expected, String actual) throws ComparisonContentFailure {
		if (expected == null && actual == null)
			return;
		if (expected != null && expected.equals(actual))
			return;
		throw new ComparisonContentFailure(message, expected, actual);
	}

	/**
	 * Asserts that two Strings are equal.
	 */
	public void assertEquals(String expected, String actual) throws ComparisonContentFailure {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two doubles are equal concerning a delta. If they are not an
	 * AssertionFailedError is thrown with the given message. If the expected value
	 * is infinity then the delta value is ignored.
	 */
	public void assertEquals(String message, double expected, double actual, double delta)
			throws AssertionContentFailed {
		// handle infinity specially since subtracting to infinite values gives NaN and
		// the
		// the following test fails
		if (Double.isInfinite(expected)) {
			if (!(expected == actual))
				failNotEquals(message, new Double(expected), new Double(actual));
		} else if (!(Math.abs(expected - actual) <= delta)) // Because comparison with NaN always returns false
			failNotEquals(message, new Double(expected), new Double(actual));
	}

	/**
	 * Asserts that two doubles are equal concerning a delta. If the expected value
	 * is infinity then the delta value is ignored.
	 */
	public void assertEquals(double expected, double actual, double delta) throws AssertionContentFailed {
		assertEquals(null, expected, actual, delta);
	}

	/**
	 * Asserts that two floats are equal concerning a delta. If they are not an
	 * AssertionFailedError is thrown with the given message. If the expected value
	 * is infinity then the delta value is ignored.
	 */
	public void assertEquals(String message, float expected, float actual, float delta) throws AssertionContentFailed {
		// handle infinity specially since subtracting to infinite values gives NaN and
		// the
		// the following test fails
		if (Float.isInfinite(expected)) {
			if (!(expected == actual))
				failNotEquals(message, new Float(expected), new Float(actual));
		} else if (!(Math.abs(expected - actual) <= delta))
			failNotEquals(message, new Float(expected), new Float(actual));
	}

	/**
	 * Asserts that two floats are equal concerning a delta. If the expected value
	 * is infinity then the delta value is ignored.
	 */
	public void assertEquals(float expected, float actual, float delta) throws AssertionContentFailed {
		assertEquals(null, expected, actual, delta);
	}

	/**
	 * Asserts that two longs are equal. If they are not an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertEquals(String message, long expected, long actual) throws AssertionContentFailed {
		assertEquals(message, new Long(expected), new Long(actual));
	}

	/**
	 * Asserts that two longs are equal.
	 */
	public void assertEquals(long expected, long actual) throws AssertionContentFailed {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two booleans are equal. If they are not an AssertionFailedError
	 * is thrown with the given message.
	 */
	public void assertEquals(String message, boolean expected, boolean actual) throws AssertionContentFailed {
		assertEquals(message, new Boolean(expected), new Boolean(actual));
	}

	/**
	 * Asserts that two booleans are equal.
	 */
	public void assertEquals(boolean expected, boolean actual) throws AssertionContentFailed {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two bytes are equal. If they are not an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertEquals(String message, byte expected, byte actual) throws AssertionContentFailed {
		assertEquals(message, new Byte(expected), new Byte(actual));
	}

	/**
	 * Asserts that two bytes are equal.
	 */
	public void assertEquals(byte expected, byte actual) throws AssertionContentFailed {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two chars are equal. If they are not an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertEquals(String message, char expected, char actual) throws AssertionContentFailed {
		assertEquals(message, new Character(expected), new Character(actual));
	}

	/**
	 * Asserts that two chars are equal.
	 */
	public void assertEquals(char expected, char actual) throws AssertionContentFailed {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two shorts are equal. If they are not an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertEquals(String message, short expected, short actual) throws AssertionContentFailed {
		assertEquals(message, new Short(expected), new Short(actual));
	}

	/**
	 * Asserts that two shorts are equal.
	 */
	public void assertEquals(short expected, short actual) throws AssertionContentFailed {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that two ints are equal. If they are not an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertEquals(String message, int expected, int actual) throws AssertionContentFailed {
		assertEquals(message, new Integer(expected), new Integer(actual));
	}

	/**
	 * Asserts that two ints are equal.
	 */
	public void assertEquals(int expected, int actual) throws AssertionContentFailed {
		assertEquals(null, expected, actual);
	}

	/**
	 * Asserts that an object isn't null.
	 */
	public void assertNotNull(Object object) throws AssertionContentFailed {
		assertNotNull(null, object);
	}

	/**
	 * Asserts that an object isn't null. If it is an AssertionFailedError is thrown
	 * with the given message.
	 */
	public void assertNotNull(String message, Object object) throws AssertionContentFailed {
		assertTrue(message, object != null);
	}

	/**
	 * Asserts that an object is null.
	 */
	public void assertNull(Object object) throws AssertionContentFailed {
		assertNull(null, object);
	}

	/**
	 * Asserts that an object is null. If it is not an AssertionFailedError is
	 * thrown with the given message.
	 */
	public void assertNull(String message, Object object) throws AssertionContentFailed {
		assertTrue(message, object == null);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertSame(String message, Object expected, Object actual) throws AssertionContentFailed {
		if (expected == actual)
			return;
		failNotSame(message, expected, actual);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not the same
	 * an AssertionFailedError is thrown.
	 */
	public void assertSame(Object expected, Object actual) throws AssertionContentFailed {
		assertSame(null, expected, actual);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not an
	 * AssertionFailedError is thrown with the given message.
	 */
	public void assertNotSame(String message, Object expected, Object actual) throws AssertionContentFailed {
		if (expected == actual)
			failSame(message);
	}

	/**
	 * Asserts that two objects refer to the same object. If they are not the same
	 * an AssertionFailedError is thrown.
	 */
	public void assertNotSame(Object expected, Object actual) throws AssertionContentFailed {
		assertNotSame(null, expected, actual);
	}

	private void failSame(String message) throws AssertionContentFailed {
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		fail(formatted + "expected not same");
	}

	private void failNotSame(String message, Object expected, Object actual) throws AssertionContentFailed {
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
	}

	private void failNotEquals(String message, Object expected, Object actual) throws AssertionContentFailed {
		fail(format(message, expected, actual));
	}

	public String format(String message, Object expected, Object actual) {
		String formatted = "";
		if (message != null)
			formatted = message + " ";
		return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
	}
}