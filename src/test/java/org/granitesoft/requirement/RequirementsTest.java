package org.granitesoft.requirement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.granitesoft.requirement.Requirements;
import org.junit.Test;

/**
 * This test will hits all lines in the Requirements class.
 */
public class RequirementsTest {

	@Test
	public void testMinLength() {
		assertThrows(IllegalArgumentException.class, () -> Requirements.minLength(-1));
		Predicate<String> pred = Requirements.minLength(3);
		assertEquals(pred.toString(),"Must have a length of at least 3");
		assertFalse(pred.test(null));
		assertFalse(pred.test(""));
		assertFalse(pred.test("a"));
		assertTrue(pred.test("abc"));
		assertTrue(pred.test("abcd"));
	}
	@Test
	public void testMaxLength() {
		assertThrows(IllegalArgumentException.class, () -> Requirements.maxLength(-1));
		Predicate<String> pred = Requirements.maxLength(3);
		assertEquals(pred.toString(),"Must have a length of at most 3");
		assertFalse(pred.test(null));
		assertTrue(pred.test(""));
		assertTrue(pred.test("a"));
		assertTrue(pred.test("abc"));
		assertFalse(pred.test("abcd"));
	}
	@Test
	public void testMinSize() {
		assertThrows(IllegalArgumentException.class, () -> Requirements.minSize(-1));
		Predicate<Collection<Integer>> pred = Requirements.minSize(3);
		assertEquals(pred.toString(),"Must have a size of at least 3");
		assertFalse(pred.test(null));
		assertFalse(pred.test(Arrays.asList()));
		assertFalse(pred.test(Arrays.asList(1)));
		assertTrue(pred.test(Arrays.asList(1,2,3)));
		assertTrue(pred.test(Arrays.asList(1,2,3,4)));
	}
	@Test
	public void testMaxSzie() {
		assertThrows(IllegalArgumentException.class, () -> Requirements.maxSize(-1));
		Predicate<Collection<Integer>> pred = Requirements.maxSize(3);
		assertEquals(pred.toString(),"Must have a size of at most 3");
		assertFalse(pred.test(null));
		assertTrue(pred.test(Arrays.asList()));
		assertTrue(pred.test(Arrays.asList(1)));
		assertTrue(pred.test(Arrays.asList(1,2,3)));
		assertFalse(pred.test(Arrays.asList(1,2,3,4)));
	}
	@Test
	public void testNotNull() {
		Predicate<Object> pred = Requirements.notNull();
		assertEquals(pred.toString(),"Must not be null");
		assertFalse(pred.test(null));
		assertTrue(pred.test(""));
		assertTrue(pred.test("a"));
		assertTrue(pred.test("abc"));
	}
	@Test
	public void testNotBlank() {
		Predicate<String> pred = Requirements.notBlank();
		assertEquals(pred.toString(),"Must not be blank");
		assertFalse(pred.test(null));
		assertFalse(pred.test(""));
		assertTrue(pred.test("a"));
		assertTrue(pred.test("abc"));
	}
	@Test
	public void testMatches() {
		Predicate<String> pred = Requirements.matches(Pattern.compile("\\d+"));
		assertEquals(pred.toString(),"Must match \\d+");
		assertFalse(pred.test(null));
		assertFalse(pred.test(""));
		assertFalse(pred.test("a"));
		assertTrue(pred.test("1"));
		assertTrue(pred.test("12"));
	}
	@Test
	public void testMatches2() {
		Predicate<String> pred = Requirements.matches("\\d+");
		assertEquals(pred.toString(),"Must match \\d+");
		assertFalse(pred.test(null));
		assertFalse(pred.test(""));
		assertFalse(pred.test("a"));
		assertTrue(pred.test("1"));
		assertTrue(pred.test("12"));
	}
	@Test
	public void testChain() {
		Predicate<String> pred = Requirements.chain(String::length,Requirements.gt(2));
		assertFalse(pred.test(""));
		assertFalse(pred.test(" "));
		assertFalse(pred.test("  "));
		assertTrue(pred.test("   "));
	}
	@Test
	public void testNameF() {
		Predicate<String> pred = Requirements.chain(Requirements.nameF(String::length,() -> "length"),Requirements.gt(2));
		assertEquals(pred.toString(),"length: Must be greater than 2");
		assertFalse(pred.test(""));
		assertTrue(pred.test("   "));
		assertThrows(IllegalArgumentException.class, () -> Requirements.nameF(String::length,null));
		assertThrows(IllegalArgumentException.class, () -> Requirements.nameF(null,() -> "length"));
	}
	@Test
	public void testName() {
		Predicate<String> pred = Requirements.chain(Requirements.nameF(String::length,() -> "length"),Requirements.name(Requirements.gt(2), () -> "gt2"));
		assertFalse(pred.test(""));
		assertTrue(pred.test("   "));
		assertThrows(IllegalArgumentException.class, () -> Requirements.name(Requirements.gt(2),null));
		assertThrows(IllegalArgumentException.class, () -> Requirements.name(null,() -> "length"));
	}
	@Test
	public void testLt() {
		Predicate<Integer> pred = Requirements.lt(2);
		assertEquals(pred.toString(),"Must be less than 2");
		assertTrue(pred.test(null));
		assertTrue(pred.test(-1));
		assertTrue(pred.test(0));
		assertTrue(pred.test(1));
		assertFalse(pred.test(2));
		assertFalse(pred.test(3));
	}
	@Test
	public void testLe() {
		Predicate<Integer> pred = Requirements.le(2);
		assertEquals(pred.toString(),"Must be less than or equal to 2");
		assertTrue(pred.test(null));
		assertTrue(pred.test(-1));
		assertTrue(pred.test(0));
		assertTrue(pred.test(1));
		assertTrue(pred.test(2));
		assertFalse(pred.test(3));
	}
	@Test
	public void testGt() {
		Predicate<Integer> pred = Requirements.gt(2);
		assertEquals(pred.toString(),"Must be greater than 2");
		assertFalse(pred.test(null));
		assertFalse(pred.test(-1));
		assertFalse(pred.test(0));
		assertFalse(pred.test(1));
		assertFalse(pred.test(2));
		assertTrue(pred.test(3));
	}
	@Test
	public void testGe() {
		Predicate<Integer> pred = Requirements.ge(2);
		assertEquals(pred.toString(),"Must be greater than or equal to 2");
		assertFalse(pred.test(null));
		assertFalse(pred.test(-1));
		assertFalse(pred.test(0));
		assertFalse(pred.test(1));
		assertTrue(pred.test(2));
		assertTrue(pred.test(3));
	}
	@Test
	public void testEq() {
		assertTrue(Requirements.eq((String)null).test((String)null));
		assertFalse(Requirements.eq((String)null).test(""));
		Predicate<Integer> pred = Requirements.eq(2);
		assertEquals(pred.toString(),"Must be equal to 2");
		assertFalse(pred.test(null));
		assertFalse(pred.test(-1));
		assertFalse(pred.test(0));
		assertFalse(pred.test(1));
		assertTrue(pred.test(2));
		assertFalse(pred.test(3));
	}
	@Test
	public void testNe() {
		Predicate<Integer> pred = Requirements.ne(2);
		assertEquals(pred.toString(),"Must be not equal to 2");
		assertTrue(pred.test(null));
		assertTrue(pred.test(-1));
		assertTrue(pred.test(0));
		assertTrue(pred.test(1));
		assertFalse(pred.test(2));
		assertTrue(pred.test(3));
	}
	@Test
	public void testEqualTo() {
		assertTrue(Requirements.equalTo((String)null).test((String)null));
		assertFalse(Requirements.equalTo((String)null).test(""));
		Predicate<Integer> pred = Requirements.equalTo(2);
		assertEquals(pred.toString(),"Must be equal to 2");
		assertFalse(pred.test(null));
		assertFalse(pred.test(-1));
		assertFalse(pred.test(0));
		assertFalse(pred.test(1));
		assertTrue(pred.test(2));
		assertFalse(pred.test(3));
	}
	static class Foo {
		@Override
		public int hashCode() {
			return 0;
		}
		@Override
		public boolean equals(Object obj) {
			return true;
		}
	}
	@Test
	public void testSame() {
		Predicate<String> pred = Requirements.same((String)null);
		assertEquals(pred.toString(),"Must be same object as null");
		assertTrue(pred.test((String)null));
		assertFalse(pred.test(""));
		Foo f1 = new Foo();
		assertTrue(Requirements.same(f1).test(f1));
		assertFalse(Requirements.same(f1).test(new Foo()));
	}
	@Test
	public void testAnd() {
		Predicate<Integer> pred = Requirements.and(Requirements.ge(2),Requirements.le(5));
		assertEquals(pred.toString(),"(Must be greater than or equal to 2) and (Must be less than or equal to 5)");
		assertFalse(pred.test(null));
		assertFalse(pred.test(1));
		assertTrue(pred.test(2));
		assertTrue(pred.test(3));
		assertTrue(pred.test(4));
		assertTrue(pred.test(5));
		assertFalse(pred.test(6));
		assertThrows(IllegalArgumentException.class, () -> Requirements.and(null, Requirements.le(2)));
		assertThrows(IllegalArgumentException.class, () -> Requirements.and(Requirements.le(2), null));
	}
	@Test
	public void testOr() {
		Predicate<Integer> pred = Requirements.or(Requirements.le(2),Requirements.ge(5));
		assertEquals(pred.toString(),"(Must be less than or equal to 2) or (Must be greater than or equal to 5)");
		assertTrue(pred.test(null));
		assertTrue(pred.test(1));
		assertTrue(pred.test(2));
		assertFalse(pred.test(3));
		assertFalse(pred.test(4));
		assertTrue(pred.test(5));
		assertTrue(pred.test(6));
		assertThrows(IllegalArgumentException.class, () -> Requirements.or(null, Requirements.le(2)));
		assertThrows(IllegalArgumentException.class, () -> Requirements.or(Requirements.le(2), null));
	}
	@Test
	public void testNegate() {
		Predicate<Integer> pred = Requirements.negate(Requirements.equalTo(2));
		assertEquals(pred.toString(),"not (Must be equal to 2)");
		assertTrue(pred.test(1));
		assertFalse(pred.test(2));
		assertTrue(pred.test(null));
		assertFalse(Requirements.negate(pred).test(1));
		assertTrue(Requirements.negate(pred).test(2));
		assertFalse(Requirements.negate(pred).test(null));
	}
	@Test
	public void testContains() {
		Predicate<Collection<? extends Integer>> pred = Requirements.contains(2);
		assertEquals(pred.toString(),"Must contain 2");
		assertFalse(pred.test(null));
		assertFalse(pred.test(Collections.emptyList()));
		assertFalse(Requirements.contains(null).test(Arrays.asList(5, 7)));
		assertTrue(Requirements.contains(null).test(Arrays.asList(null, 7)));
		assertFalse(pred.test(Arrays.asList(5, 7)));
		assertTrue(pred.test(Arrays.asList(2, 7)));
		assertTrue(pred.test(Arrays.asList(5, 2)));
		assertFalse(pred.test(Arrays.asList(0, 1)));
	}
	@Test
	public void testContainsKey() {
		Predicate<Map<Integer, Integer>> pred = Requirements.containsKey(2);
		assertEquals(pred.toString(),"Must contain key 2");
		assertFalse(pred.test(null));
		assertFalse(Requirements.containsKey(null).test(Collections.singletonMap(1, 2)));
		assertTrue(Requirements.containsKey(null).test(Collections.singletonMap(null, 2)));
		assertFalse(pred.test(Collections.singletonMap(null, 2)));
		assertFalse(pred.test(Collections.singletonMap(1, 2)));
		assertFalse(pred.test(Collections.emptyMap()));
		assertTrue(pred.test(Collections.singletonMap(2, 2)));
	}
	@Test
	public void testMapNotEmpty() {
		Predicate<Map<Integer, Integer>> pred = Requirements.mapNotEmpty();
		assertEquals(pred.toString(),"Must not be empty");
		assertFalse(pred.test(null));
		assertTrue(Requirements.mapNotEmpty().test(Collections.singletonMap(1, 2)));
		assertTrue(pred.test(Collections.singletonMap(null, 2)));
		assertFalse(pred.test(Collections.emptyMap()));
	}
	@Test
	public void testNotEmpty() {
		Predicate<Collection<? extends Object>> pred = Requirements.notEmpty();
		assertEquals(pred.toString(),"Must not be empty");
		assertTrue(pred.test(Arrays.asList(5, 7)));
		assertFalse(pred.test(Arrays.asList()));
		assertFalse(pred.test(null));
	}
	@Test
	public void testMemberOf() {
		Predicate<Integer> pred = Requirements.memberOf(Arrays.asList(5, 7));
		assertEquals(pred.toString(),"Must be a member of [5, 7]");
		assertFalse(pred.test(null));
		assertTrue(Requirements.memberOf(Arrays.asList(5, null)).test(null));
		assertFalse(pred.test(2));
		assertTrue(pred.test(5));
		assertTrue(pred.test(7));
		assertFalse(pred.test(8));
	}
	@Test
	public void testMemberOf2() {
		Predicate<Integer> pred = Requirements.memberOf(5, 7);
		assertEquals(pred.toString(),"Must be a member of [5, 7]");
		assertFalse(pred.test(null));
		assertTrue(Requirements.memberOf(5, null).test(null));
		assertFalse(pred.test(2));
		assertTrue(pred.test(5));
		assertTrue(pred.test(7));
		assertFalse(pred.test(8));
	}
	@Test
	public void testSuperSet() {
		Predicate<Collection<? extends Integer>> pred = Requirements.superSetOf(Arrays.asList(5, 7));
		assertEquals(pred.toString(),"Must be a superset of [5, 7]");
		assertTrue(pred.test(Arrays.asList(5, 7, 8)));
		assertTrue(pred.test(Arrays.asList(5, 7)));
		assertFalse(pred.test(Arrays.asList(5)));
		assertFalse(pred.test(Collections.emptyList()));
		assertFalse(pred.test(null));
	}
	@Test
	public void testSuperSet2() {
		Predicate<Collection<? extends Integer>> pred = Requirements.superSetOf(5, 7);
		assertEquals(pred.toString(),"Must be a superset of [5, 7]");
		assertTrue(pred.test(Arrays.asList(5, 7, 8)));
		assertTrue(pred.test(Arrays.asList(5, 7)));
		assertFalse(pred.test(Arrays.asList(5)));
		assertFalse(pred.test(Collections.emptyList()));
		assertFalse(pred.test(null));
	}
	@Test
	public void testFunctions() {
		Object o = new Object();
		assertTrue(Requirements.hash().apply(o).equals(o.hashCode()));
		assertEquals(Requirements.hash().toString(), "hash");
		
		assertTrue(Requirements.length().apply("abc").equals(3));
		assertEquals(Requirements.length().toString(), "length");
		
		assertTrue(Requirements.keySet().apply(Collections.singletonMap(1, 7)).equals(Collections.singleton(1)));
		assertEquals(Requirements.keySet().toString(), "keys");
		
		assertTrue(Requirements.values().apply(Collections.singletonMap(1, 7)).equals(Collections.singleton(7)));
		assertEquals(Requirements.values().toString(), "values");
		
		assertTrue(Requirements.mapSize().apply(Collections.singletonMap(1, 7)).equals(1));
		assertEquals(Requirements.mapSize().toString(), "size");
		
		assertTrue(Requirements.size().apply(Collections.singleton(7)).equals(1));
		assertEquals(Requirements.size().toString(), "size");
		
		assertTrue(Requirements.stringify().apply(Arrays.asList(1, 3)).equals("[1, 3]"));
		assertEquals(Requirements.stringify().toString(), "toString");
	}
	@Test
	public void testSubSet() {
		Predicate<Collection<? extends Integer>> pred = Requirements.subSetOf(Arrays.asList(5, 7));
		assertEquals(pred.toString(),"Must be a subset of [5, 7]");
		assertFalse(pred.test(null));
		assertTrue(pred.test(Collections.emptyList()));
		assertTrue(pred.test(Arrays.asList(5)));
		assertTrue(pred.test(Arrays.asList(5, 7)));
		assertFalse(pred.test(Arrays.asList(5, 7, 8)));
	}
	@Test
	public void testSubSet2() {
		Predicate<Collection<? extends Integer>> pred = Requirements.subSetOf(5, 7);
		assertEquals(pred.toString(),"Must be a subset of [5, 7]");
		assertFalse(pred.test(null));
		assertTrue(pred.test(Collections.emptyList()));
		assertTrue(pred.test(Arrays.asList(5)));
		assertTrue(pred.test(Arrays.asList(5, 7)));
		assertFalse(pred.test(Arrays.asList(5, 7, 8)));
	}

	@Test
	public void testArrays() {
		Predicate<byte[]> predByteArr = Requirements.equalTo(new byte[] {1, 8});
		assertTrue(predByteArr.test(new byte[] {1, 8}));
		assertFalse(predByteArr.test(new byte[] {8, 1}));
		assertFalse(predByteArr.test(null));
		assertEquals(predByteArr.toString(),"Must be equal to [1, 8]");
		
		Predicate<short[]> predShortArr = Requirements.equalTo(new short[] {1, 8});
		assertTrue(predShortArr.test(new short[] {1, 8}));
		assertFalse(predShortArr.test(new short[] {8, 1}));
		assertFalse(predShortArr.test(null));
		assertEquals(predShortArr.toString(),"Must be equal to [1, 8]");
		
		Predicate<int[]> predIntArray = Requirements.equalTo(new int[] {1, 8});
		assertTrue(predIntArray.test(new int[] {1, 8}));
		assertFalse(predIntArray.test(new int[] {8, 1}));
		assertFalse(predIntArray.test(null));
		assertEquals(predIntArray.toString(),"Must be equal to [1, 8]");
		
		Predicate<long[]> predLongArr = Requirements.equalTo(new long[] {1, 8});
		assertTrue(predLongArr.test(new long[] {1, 8}));
		assertFalse(predLongArr.test(new long[] {8, 1}));
		assertFalse(predLongArr.test(null));
		assertEquals(predLongArr.toString(),"Must be equal to [1, 8]");
		
		Predicate<float[]> predFloatArr = Requirements.equalTo(new float[] {1, 8});
		assertTrue(predFloatArr.test(new float[] {1, 8}));
		assertFalse(predFloatArr.test(new float[] {8, 1}));
		assertFalse(predFloatArr.test(null));
		assertEquals(predFloatArr.toString(),"Must be equal to [1.0, 8.0]");
		
		Predicate<double[]> predDoubleArr = Requirements.equalTo(new double[] {1, 8});
		assertTrue(predDoubleArr.test(new double[] {1, 8}));
		assertFalse(predDoubleArr.test(new double[] {8, 1}));
		assertFalse(predDoubleArr.test(null));
		assertEquals(predDoubleArr.toString(),"Must be equal to [1.0, 8.0]");
		
		Predicate<char[]> predCharArr = Requirements.equalTo(new char[] {'1', '8'});
		assertTrue(predCharArr.test(new char[] {'1', '8'}));
		assertFalse(predCharArr.test(new char[] {'8', '1'}));
		assertFalse(predCharArr.test(null));
		assertEquals(predCharArr.toString(),"Must be equal to [1, 8]");
		
		Predicate<boolean[]> predBooleanArr = Requirements.equalTo(new boolean[] {true, false});
		assertTrue(predBooleanArr.test(new boolean[] {true, false}));
		assertFalse(predBooleanArr.test(new boolean[] {false, true}));
		assertFalse(predBooleanArr.test(null));
		assertEquals(predBooleanArr.toString(),"Must be equal to [true, false]");
		
		Predicate<String[]> predStringArr = Requirements.equalTo(new String[] {"1" ,"8"});
		assertTrue(predStringArr.test(new String[] {"1" ,"8"}));
		assertFalse(predStringArr.test(new String[] {"8," ,"1"}));
		assertFalse(predStringArr.test(null));
		assertEquals(predStringArr.toString(),"Must be equal to [1, 8]");
	}
	
	private static class MyException extends Exception {
		private static final long serialVersionUID = 1L;		
	}

	@Test
	public void testRequire() throws MyException {
		String x = "abc";
		assertThrows(IllegalArgumentException.class, () -> Requirements.require(null, Requirements.notNull()));
		assertThrows(MyException.class, () -> Requirements.require (null, Requirements.notNull(), (o, v) -> new MyException()));
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Requirements.require (null, Requirements.notNull(), () -> "foo"));
		assertTrue(ex.getMessage().startsWith("foo:"));
		assertTrue(x == Requirements.require(x, Requirements.notNull()));
		assertTrue(x == Requirements.require (x, Requirements.notNull(), (o, v) -> new MyException()));
		assertTrue(x == Requirements.require (x, Requirements.notNull(), () -> "foo"));
		assertThrows(IllegalArgumentException.class, () -> Requirements.require(null, null));
		assertThrows(IllegalArgumentException.class, () -> Requirements.<String,Exception>require(null, Requirements.notNull(), (BiFunction<String,Predicate<String>,Exception>)null));
		assertThrows(IllegalArgumentException.class, () -> Requirements.require(null, null, () -> "msg"));
		assertThrows(IllegalArgumentException.class, () -> Requirements.require(null, null, (o, v) -> new RuntimeException()));
		assertThrows(IllegalArgumentException.class, () -> Requirements.<String>require(null, Requirements.notNull(), (Supplier<String>)null));
	}
}
