package org.granitesoft.requirement;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Utility for making generalized requirements.
 * A thorough understanding of "lambda"'s is very helpful, but not strictly required.
 * See https://github.com/ericgoffster/requirement.
 * <p>
 * This code licensed under Mozilla Public License Version 2.0.
 * <p>
 * The primary place to start is here: {@link Requirements#require(Object, Predicate)}
 */
public final class Requirements {
	private static final Predicate<?> NOT_NULL = nameInt(o -> o != null, () -> "Must not be null");
	private static final Predicate<String> NOT_BLANK = nameInt(o -> o != null && o.length() > 0, () -> "Must not be blank");

	private Requirements() {		
	}
	
	/**
	 * Performs a test on the argument, returning that argument if it succeeds, but throwing a user supplied exception if it fails.
	 * This is the most general form of "require".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,  Requirements.notNull(), (o, v) -&gt; new Exception()) # throws MyException
	 *     Requirements.require("abc", Requirements.notNull(), (o, v) -&gt; new Exception()) == "abc"
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param <EX> The type of the exception that could be thrown.
	 * @param obj The object to verify - Can be null
	 * @param validator The predicate that must succeed - Can't be null
	 * @param ex The Exception supplier - Can't be null
	 * @return The verified object
	 * @throws EX if the test fails
	 */
	public static <T, EX extends Exception> T require(T obj, Predicate<T> validator, BiFunction<T, Predicate<T>, EX> ex) throws EX {
		return requireInt(obj, requireInt(validator, () -> "validator", notNull()), requireInt(ex, () -> "ex", notNull()));
	}

	/**
	 * Performs a test on the argument, returning that argument if it succeeds, but throwing an {@link IllegalArgumentException} if it fails.
	 * A specified prefix is prepended to the beginning of the message
	 * in that exception.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,  Requirements.notNull(), () -&gt; "someobj") # throws IllegalArgumentException("someobj: Must not be null")
	 *     Requirements.require("abc", Requirements.notNull(), () -&gt; "someobj") == "abc"
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param obj The object to verify - Can be null
	 * @param validator The predicate that must succeed - Can't be null - Note that "toString" on this
	 * 	predicate will be called.   To make this predicate have a toString, use name(Predicate)
	 * @param msg The supplier of the additional message to prepend - Can't be null
	 * @return The verified object
	 * @throws IllegalArgumentException if the test fails
	 */
	public static <T> T require(T obj, Predicate<T> validator, Supplier<String> msg) {
		return requireInt(obj, requireInt(msg, () -> "msg", notNull()), requireInt(validator, () -> "validator", notNull()));
	}
	
	/**
	 * Performs a test on the argument, returning that argument if it succeeds, throwing an {@link IllegalArgumentException} if it fails.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,  Requirements.notNull()) # throws IllegalArgumentException("Must not be null")
	 *     Requirements.require("abc", Requirements.notNull())  == "abc"
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param obj The object to verify - Can be null
 	 * @param validator The predicate that must succeed - Can't be null - Note that "toString" on this
	 * 	predicate will be called.   To make this predicate have a toString, use name(Predicate)
	 * @return The verified object
	 * @throws IllegalArgumentException if the test fails
	 */
	public static <T> T require(T obj, Predicate<T> validator) {
		return requireInt(obj, requireInt(validator, () -> "validator", notNull()));
	}
	
	/**
	 * Returns a predicate that checks for its argument to be not null.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,  Requirements.notNull()) # throws IllegalArgumentException("Must not be null")
	 *     Requirements.require("abc", Requirements.notNull())  == "abc"
	 * </pre>
	 * 
	 * @param <T> The type of the object being tested
	 * @return A predicate that checks for its argument to be not null
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> notNull() {
		return (Predicate<T>) NOT_NULL;
	}

	/**
	 * Returns a predicate that checks for its string argument to be not blank.
	 * A string must be non-null and have a length &gt; 0 to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,  Requirements.notBlank()) # throws IllegalArgumentException("Must not be blank")
	 *     Requirements.require("",    Requirements.notBlank()) # throws IllegalArgumentException("Must not be blank")
	 *     Requirements.require("abc", Requirements.notBlank())  == "abc"
	 * </pre>
	 * 
	 * @return A predicate that checks for its string argument to be not blank
	 */
	public static Predicate<String> notBlank() {
		return NOT_BLANK;
	}

	/**
	 * Returns a predicate that checks for its string argument to have a length <code>&gt;= n</code>.
	 * A string must be non-null and have a length of at least "n" to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,  Requirements.minLength(2)) # throws IllegalArgumentException("Must have a length of at least 2")
	 *     Requirements.require("",    Requirements.minLength(2)) # throws IllegalArgumentException("Must have a length of at least 2")
	 *     Requirements.require("ab",  Requirements.minLength(2))  == "ab"
	 *     Requirements.require("abc", Requirements.minLength(2))  == "abc"
	 * </pre>
	 * 
	 * @param n The minimum length - Must be &gt;= 0
	 * @return A predicate that checks for its string argument to have a length <code>&gt;= n</code>
	 */
	public static Predicate<String> minLength(int n) {
		requireInt(n, ge(0));
		return nameInt(s -> s != null && s.length() >= n, () -> String.format("Must have a length of at least %d", n));
	}

	/**
	 * Returns a predicate that checks for its string argument to have a length <code>&lt;= n</code>.
	 * A string must be non-null and have a length of at most "n" to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,  Requirements.maxLength(2)) # throws IllegalArgumentException("Must have a length of at most 2")
	 *     Requirements.require("",    Requirements.maxLength(2)) == ""
	 *     Requirements.require("ab",  Requirements.maxLength(2))  == "ab"
	 *     Requirements.require("abc", Requirements.maxLength(2)) # throws IllegalArgumentException("Must have a length of at most 2")
	 * </pre>
	 * 
	 * @param n The maximum length - Must be &gt;= 0
	 * @return A predicate that checks for its string argument to have a length <code>&lt;= n</code>
	 */
	public static Predicate<String> maxLength(int n) {
		requireInt(n, ge(0));
		return nameInt(s -> s != null && s.length() <= n, () -> String.format("Must have a length of at most %d", n));
	}

	/**
	 * Returns a predicate that checks for its string argument to match <code>p</code>.
	 * A string must be non-null and match the pattern to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,     Requirements.matches(Pattern.compile("^\\d+$"))) # throws IllegalArgumentException("Must match ^\d+$")
	 *     Requirements.require("",       Requirements.matches(Pattern.compile("^\\d+$"))) # throws IllegalArgumentException("Must match ^\d+$")
	 *     Requirements.require("1",      Requirements.matches(Pattern.compile("^\\d+$")))  == "1"
	 *     Requirements.require("123",    Requirements.matches(Pattern.compile("^\\d+$")))  == "123"
	 *     Requirements.require("abc123", Requirements.matches(Pattern.compile("^\\d+$"))) # throws IllegalArgumentException("Must match ^\d+$")
	 * </pre>
	 *     
	 * @param p The pattern - Can't be null
	 * @return A predicate that checks for its string argument to match <code>p</code>
	 */
	public static Predicate<String> matches(Pattern p) {
		requireInt(p, notNull());
		return nameInt(s -> s != null && p.asPredicate().test(s), () -> String.format("Must match %s", p));
	}
	
	/**
	 * Returns a predicate that checks for its string argument to match <code>p</code>.
	 * A string must be non-null and match the pattern to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,     Requirements.matches("\\d+")) # throws IllegalArgumentException("Must match \d+")
	 *     Requirements.require("",       Requirements.matches("\\d+")) # throws IllegalArgumentException("Must match \d+")
	 *     Requirements.require("1",      Requirements.matches("\\d+"))  == "1"
	 *     Requirements.require("123",    Requirements.matches("\\d+"))  == "123"
	 *     Requirements.require("abc123", Requirements.matches("\\d+")) == "abc123"
	 * </pre>
	 *     
	 * @param p The pattern - Can't be null
	 * @return A predicate that checks for its string argument to match <code>p</code>
	 */
	public static Predicate<String> matches(String p) {
		requireInt(p, notNull());
		Pattern q = Pattern.compile(p);
		return nameInt(s -> s != null && q.asPredicate().test(s), () -> String.format("Must match %s", p));
	}
	
	/**
	 * Returns a predicate that checks for its collection based argument to be not empty.
	 * A collection must be non-null and have a size &gt; 0 to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,   Requirements.notEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Requirements.require([],     Requirements.notEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Requirements.require([1, 2], Requirements.notEmpty()) == [1, 2]
	 * </pre>
	 *     
	 * @param <E> The type of the elements of the collection
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for its collection based argument to be not empty
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> notEmpty() {
		return nameInt(collection -> collection != null && !collection.isEmpty(), () -> "Must not be empty");
	}

	/**
	 * Returns a predicate that checks for its collection to contain <code>val</code>.
	 * A collection must be non-null and contain the given object to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,   Requirements.contains(1)) # throws IllegalArgumentException("Must contain 1")
	 *     Requirements.require([],     Requirements.contains(1)) # throws IllegalArgumentException("Must contain 1")
	 *     Requirements.require([2, 3], Requirements.contains(1)) # throws IllegalArgumentException("Must contain 1")
	 *     Requirements.require([1, 2], Requirements.contains(1)) == [1]
	 * </pre>
	 *     
	 * @param val The object the collection must contain - Can be null
	 * @param <E> The type of the elements of the collection.
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for its collection to contain <code>val</code>
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> contains(E val) {
		return nameInt(collection -> collection != null && collection.contains(val), () -> String.format("Must contain %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its map to contain <code>key</code>.
	 * A map must be non-null and contain the given key to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,             Requirements.containsKey(1)) # throws IllegalArgumentException("Must contain key 1")
	 *     Requirements.require({},               Requirements.containsKey(1)) # throws IllegalArgumentException("Must contain key 1")
	 *     Requirements.require({a=1, b=2}, Requirements.containsKey("c")) # throws IllegalArgumentException("Must contain key c")
	 *     Requirements.require({a=1, b=2}, Requirements.containsKey("a")) == {a=1, b=2}
	 * </pre>
	 *     
	 * @param key The key the map must contain - Can be null
	 * @param <K> The type of the keys of the map.
	 * @param <V> The type of the values of the map.
	 * @param <T> The Map type of the object being tested
	 * @return A predicate that checks for its map to contain <code>key</code>
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Predicate<T> containsKey(K key) {
		return nameInt(map -> map != null && map.containsKey(key), () -> String.format("Must contain key %s", key));
	}

	/**
	 * Returns a predicate that checks for its map to be not empty.
	 * A map must be non-null and be not empty to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,             Requirements.mapNotEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Requirements.require({},               Requirements.mapNotEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Requirements.require({a=1, b=2}, Requirements.mapNotEmpty()) == {a=1, b=2}
	 * </pre>
	 *     
	 * @param <K> The type of the keys of the map
	 * @param <V> The type of the values of the map
	 * @param <T> The Map type of the object being tested
	 * @return A predicate that checks for its map to be not empty
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Predicate<T> mapNotEmpty() {
		return nameInt(map -> map != null && !map.isEmpty(), () -> String.format("Must not be empty"));
	}

	/**
	 * Returns a predicate that checks for its collection to have a size <code>&gt;= n</code>.
	 * A collection must be non-null and have a size of at least "n" to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.minSize(2)) # throws IllegalArgumentException("Must have a size of at least 2")
	 *     Requirements.require([],        Requirements.minSize(2)) # throws IllegalArgumentException("Must have a size of at least 2")
	 *     Requirements.require([1, 2],    Requirements.minSize(2)) == [1, 2]
	 *     Requirements.require([1, 2, 3], Requirements.minSize(2))  == [1, 2, 3]
	 * </pre>
	 * 
	 * @param <E> The element type of the collection
	 * @param <T> The type of the collection
	 * @param n The minimum size - Must be &gt;= 0
	 * @return A predicate that checks for its collection to have a size <code>&gt;= n</code>
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> minSize(int n) {
		requireInt(n, ge(0));
		return nameInt(s -> s != null && s.size() >= n, () -> String.format("Must have a size of at least %d", n));
	}

	/**
	 * Returns a predicate that checks for its collection to have a size <code>&lt;= n</code>.
	 * A collection must be non-null and have a size of at most "n" to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.maxSize(2)) # throws IllegalArgumentException("Must have a size of at most 2")
	 *     Requirements.require([],        Requirements.maxSize(2)) == []
	 *     Requirements.require([1, 2],    Requirements.maxSize(2)) == [1, 2]
	 *     Requirements.require([1, 2, 3], Requirements.maxSize(2)) # throws IllegalArgumentException("Must have a size of at most 2")
	 * </pre>
	 * 
	 * @param <E> The element type of the collection
	 * @param <T> The type of the collection
	 * @param n The maximum size - Must be &gt;= 0
	 * @return A predicate that checks for its collection to have a size  <code>&lt;= n</code>
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> maxSize(int n) {
		requireInt(n, ge(0));
		return nameInt(s -> s != null && s.size() <= n, () -> String.format("Must have a size of at most %d", n));
	}

	/**
	 * Returns a predicate that checks for its collection to be a superset of <code>coll</code>.
	 * A collection must be non-null and be a superset of the given collection to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.superSetOf([1, 2])) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Requirements.require([],        Requirements.superSetOf([1, 2])) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Requirements.require([1],       Requirements.superSetOf([1, 2])) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Requirements.require([1, 2],    Requirements.superSetOf([1, 2])) == [1, 2]
	 *     Requirements.require([1, 2, 3], Requirements.superSetOf([1, 2])) == [1, 2, 3]
	 * </pre>
	 *  
	 * @param coll The collection to test against - Can't be null
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for its collection to be a superset of <code>coll</code>
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> superSetOf(Collection<? extends E> coll) {
		requireInt(coll, notNull());
		return nameInt(collection -> collection != null && collection.containsAll(coll), () -> String.format("Must be a superset of %s", coll));
	}
	
	/**
	 * Returns a predicate that checks for its collection to be a superset of <code>arr</code>.
	 * A collection must be non-null and be a superset of the given collection to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.superSetOf(1, 2)) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Requirements.require([],        Requirements.superSetOf(1, 2)) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Requirements.require([1],       Requirements.superSetOf(1, 2)) # throws IllegalArgumentException("Must be superset of [1, ]}")
	 *     Requirements.require([1, 2],    Requirements.superSetOf(1, 2)) == [1, 2]
	 *     Requirements.require([1, 2, 3], Requirements.superSetOf(1, 2)) == [1, 2, 3]
	 * </pre>
	 *  
	 * @param arr The collection to test against - Can't be null
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for its collection to be a superset of <code>arr</code>
	 */
	@SafeVarargs
	public static <E, T extends Collection<? extends E>> Predicate<T> superSetOf(E ... arr) {
		Set<E> setOther = new LinkedHashSet<>();
		for(E e: arr) {
			setOther.add(e);
		}
		return nameInt(collection -> collection != null && collection.containsAll(setOther), () -> String.format("Must be a superset of %s", setOther));
	}

	/**
	 * Returns a predicate that checks for its collection to be a subset of <code>coll</code>.
	 * A collection must be non-null and be a subset of the given collection to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.subSetOf([1, 2])) # throws IllegalArgumentException("Must be subset of [1, 2]")
	 *     Requirements.require([],        Requirements.subSetOf([1, 2])) == []
	 *     Requirements.require([1],       Requirements.subSetOf([1, 2])) == [1]
	 *     Requirements.require([1, 2],    Requirements.subSetOf([1, 2])) == [1, 2]
	 *     Requirements.require([1, 2, 3], Requirements.subSetOf([1, 2])) # throws IllegalArgumentException("Must be subset of [1, 2]")
	 * </pre>
	 *     
	 * @param coll The collection to test against - Can't be null
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for its collection to be a subset of <code>coll</code>
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> subSetOf(Collection<? extends E> coll) {
		requireInt(coll, notNull());
		return nameInt(collection -> collection != null && coll.containsAll(collection), () -> String.format("Must be a subset of %s", coll));
	}

	/**
	 * Returns a predicate that checks for its collection to be a subset of <code>arr</code>.
	 * A collection must be non-null and be a subset of the given collection to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.subSetOf(1, 2)) # throws IllegalArgumentException("Must be subset of [1, 2]")
	 *     Requirements.require([],        Requirements.subSetOf(1, 2)) == []
	 *     Requirements.require([1],       Requirements.subSetOf(1, 2)) == [1]
	 *     Requirements.require([1, 2],    Requirements.subSetOf(1, 2)) == [1, 2]
	 *     Requirements.require([1, 2, 3], Requirements.subSetOf(1, 2)) # throws IllegalArgumentException("Must be subset of [1, 2]")
	 * </pre>
	 *     
	 * @param arr The collection to test against - Can't be null
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for its collection to be a subset of <code>arr</code>
	 */
	@SafeVarargs
	public static <E, T extends Collection<? extends E>> Predicate<T> subSetOf(E ... arr) {
		Set<E> setOther = new LinkedHashSet<>();
		for(E e: arr) {
			setOther.add(e);
		}
		return nameInt(collection -> collection != null && setOther.containsAll(collection), () -> String.format("Must be a subset of %s", setOther));
	}

	/**
	 * Returns a predicate that checks for all its members to match <code>memberTest</code>.
	 * A collection must be non-null and all of its members to match <code>memberTest</code>  to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.allMembers(Requirements.notNull()) # throws IllegalArgumentException("All members: Must not be null")
	 *     Requirements.require([],        Requirements.allMembers(Requirements.notNull()) == []
	 *     Requirements.require([1],       Requirements.allMembers(Requirements.notNull()) == [1]
	 *     Requirements.require([1, null], Requirements.allMembers(Requirements.notNull()) # throws IllegalArgumentException("All members: Must not be null")
	 * </pre>
	 *     
	 * @param memberTest The predicate used to test members.
	 * @param <E> The type of the elements in the collection.
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for its collection to to match <code>memberTest</code>
	 */
	public static <E, T extends Iterable<? extends E>> Predicate<T> allMembers(Predicate<E> memberTest) {
		requireInt(memberTest, notNull());
		return nameInt( collection -> collection != null && allMembers(collection, memberTest), () -> String.format("All members: %s", memberTest));
	}

	/**
	 * Returns a predicate that checks for at least one its members to match <code>memberTest</code>.
	 * A collection must be non-null and at least one its members matched <code>memberTest</code>  to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,       Requirements.anyMember(Requirements.notNull()) # throws IllegalArgumentException("At least one member: Must not be null")
	 *     Requirements.require([],         Requirements.anyMember(Requirements.notNull()) # throws IllegalArgumentException("At least one member: Must not be null")
	 *     Requirements.require([1],        Requirements.anyMember(Requirements.notNull()) == [1]
	 *     Requirements.require([1, null],  Requirements.anyMember(Requirements.notNull()) == [1, null]
	 *     Requirements.require([null],     Requirements.anyMember(Requirements.notNull()) # throws IllegalArgumentException("At least one member: Must not be null")
	 * </pre>
	 *     
	 * @param memberTest The predicate used to test members.
	 * @param <E> The type of the elements in the collection.
	 * @param <T> The Collection type of the object being tested
	 * @return A predicate that checks for at least one its members to match <code>memberTest</code>
	 */
	public static <E, T extends Iterable<? extends E>> Predicate<T> anyMember(Predicate<E> memberTest) {
		requireInt(memberTest, notNull());
		return nameInt( collection -> collection != null && anyMember(collection, memberTest), () -> String.format("At least one member: %s", memberTest));
	}
	
	/**
	 * Returns a predicate that checks for its object to be a member of <code>arr</code>.
	 * An object must be a member of the given collection to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.memberOf([])    ) # throws IllegalArgumentException("Must be a member of []")
	 *     Requirements.require(null, Requirements.memberOf([null])) == null
	 *     Requirements.require(null, Requirements.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of [1, 2]")
	 *     Requirements.require(1,    Requirements.memberOf([1, 2])) == 1
	 *     Requirements.require(3,    Requirements.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of [1, 2]")
	 * </pre>
	 *     
	 * @param arr The collection that must contain the object - Can't be null
	 * @param <T> The type of the object being tested
	 * @return A predicate that checks for its object to be a member of <code>arr</code>
	 */
	@SafeVarargs
	public static <T> Predicate<T> memberOf(T ... arr) {
		Set<T> setColl = new LinkedHashSet<>();
		for(T e: arr) {
			setColl.add(e);
		}
		return nameInt(setColl::contains, () -> String.format("Must be a member of %s", setColl));
	}
	
	/**
	 * Returns a predicate that checks for its object to be a member of <code>coll</code>.
	 * An object must be a member of the given collection to satisfy the condition.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.memberOf([])    ) # throws IllegalArgumentException("Must be a member of []")
	 *     Requirements.require(null, Requirements.memberOf([null])) == null
	 *     Requirements.require(null, Requirements.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of [1, 2]")
	 *     Requirements.require(1,    Requirements.memberOf([1, 2])) == 1
	 *     Requirements.require(3,    Requirements.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of [1, 2]")
	 * </pre>
	 *     
	 * @param coll The collection that must contain the object - Can't be null
	 * @param <T> The type of the object being tested
	 * @return A predicate that checks for its object to be a member of <code>coll</code>
	 */
	public static <T> Predicate<T> memberOf(Iterable<? extends T> coll) {
		requireInt(coll, notNull());
		Set<T> setColl = new LinkedHashSet<>();
		for(T e: coll) {
			setColl.add(e);
		}
		return nameInt(setColl::contains, () -> String.format("Must be a member of %s", setColl));
	}
	
	/**
	 * Returns a predicate that checks for its object to be <code>&lt; val</code>.
	 * An object must be less than another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.lt(null)) # throws IllegalArgumentException("Must be less than null")
	 *     Requirements.require(null, Requirements.lt(2)   ) == null
	 *     Requirements.require(1,    Requirements.lt(2)   ) == 1
	 *     Requirements.require(2,    Requirements.lt(2)   ) # throws IllegalArgumentException("Must be less than 2")
	 *     Requirements.require(3,    Requirements.lt(2)   ) # throws IllegalArgumentException("Must be less than 2")
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>&lt; val</code>
	 */
	public static <T extends Comparable<T>> Predicate<T> lt(T val) {
		return nameInt(o -> compare(o, val) < 0, () -> String.format("Must be less than %s", val));
	}

	/**
	 * Returns a predicate that checks for its object to be <code>&gt; val</code>.
	 * An object must be greater than another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.gt(null)) # throws IllegalArgumentException("Must be greater than null")
	 *     Requirements.require(null, Requirements.gt(2)   ) # throws IllegalArgumentException("Must be greater than 2")
	 *     Requirements.require(1,    Requirements.gt(2)   ) # throws IllegalArgumentException("Must be greater than 2")
	 *     Requirements.require(2,    Requirements.gt(2)   ) # throws IllegalArgumentException("Must be greater than 2")
	 *     Requirements.require(3,    Requirements.gt(2)   ) == 3
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>&gt; val</code>.
	 */
	public static <T extends Comparable<T>> Predicate<T> gt(T val) {
		return nameInt(o -> compare(o, val) > 0, () -> String.format("Must be greater than %s", val));
	}

	/**
	 * Returns a predicate that checks for its object to be <code>&lt;= val</code>.
	 * An object must be less than or equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.le(null)) == null
	 *     Requirements.require(null, Requirements.le(2)   ) == null
	 *     Requirements.require(1,    Requirements.le(2)   ) == 1
	 *     Requirements.require(2,    Requirements.le(2)   ) == 2
	 *     Requirements.require(3,    Requirements.le(2)   ) # throws IllegalArgumentException("Must be less than or equal to 2")
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>&lt;= val</code>
	 */
	public static <T extends Comparable<T>> Predicate<T> le(T val) {
		return nameInt(o -> compare(o, val) <= 0, () -> String.format("Must be less than or equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its object to be <code>&gt;= val</code>.
	 * An object must be greater than or equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.ge(null)) == null
	 *     Requirements.require(null, Requirements.ge(2)   ) # throws IllegalArgumentException("Must be greater than or equal to 2")
	 *     Requirements.require(1,    Requirements.ge(2)   ) # throws IllegalArgumentException("Must be greater than or equal to 2")
	 *     Requirements.require(2,    Requirements.ge(2)   ) == 2
	 *     Requirements.require(3,    Requirements.ge(2)   ) == 3
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>&gt;= val</code>
	 */
	public static <T extends Comparable<T>> Predicate<T> ge(T val) {
		return nameInt(o -> compare(o, val) >= 0, () -> String.format("Must be greater than or equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its object to be <code>== val</code> (via numerical sense).
	 * An object must be equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.eq(null)) == null
	 *     Requirements.require(null, Requirements.eq(2)   ) # throws IllegalArgumentException("Must be equal to 2")
	 *     Requirements.require(1,    Requirements.eq(2)   ) # throws IllegalArgumentException("Must be equal to 2")
	 *     Requirements.require(2,    Requirements.eq(2)   ) == 2
	 *     Requirements.require(3,    Requirements.eq(2)   ) # throws IllegalArgumentException("Must be equal to 2")
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== val</code>
	 */
	public static <T extends Comparable<T>> Predicate<T> eq(T val) {
		return nameInt(o -> compare(o, val) == 0, () -> String.format("Must be equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its object to be <code>!= val</code> (via numerical sense).
	 * An object must not be equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.ne(null)) # throws IllegalArgumentException("Must not be equal to null")
	 *     Requirements.require(null, Requirements.ne(2)   ) == null
	 *     Requirements.require(1,    Requirements.ne(2)   ) == 1
	 *     Requirements.require(2,    Requirements.ne(2)   ) # throws IllegalArgumentException("Must not be equal to 2")
	 *     Requirements.require(3,    Requirements.ne(2)   ) == 3
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>!= val</code>
	 */
	public static <T extends Comparable<T>> Predicate<T> ne(T val) {
		return nameInt(o -> compare(o, val) != 0, () -> String.format("Must not be equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its byte[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.equalTo(null)  ) == null
	 *     Requirements.require([],        Requirements.equalTo(null)  ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],        Requirements.equalTo([])    ) == []
	 *     Requirements.require([1],       Requirements.equalTo([1, 2])) # throws IllegalArgumentException("Must be equal to [1, 2]")
	 *     Requirements.require([1, 2],    Requirements.equalTo([1, 2])) == [1, 2]
	 *     Requirements.require([2, 1],    Requirements.equalTo([1, 2])) # throws IllegalArgumentException("Must be equal to [1, 2]")
	 *     Requirements.require([1, 2, 3], Requirements.equalTo([1, 2])) # throws IllegalArgumentException("Must be equal to [1, 2]")
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<byte[]> equalTo(byte[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}

	/**
	 * Returns a predicate that checks for its short[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.equalTo(null)  ) == null
	 *     Requirements.require([],        Requirements.equalTo(null)  ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],        Requirements.equalTo([])    ) == []
	 *     Requirements.require([1],       Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Requirements.require([1, 2],    Requirements.equalTo([1, 2])) == [1, 2]
	 *     Requirements.require([2, 1],    Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Requirements.require([1, 2, 3], Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<short[]> equalTo(short[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}

	/**
	 * Returns a predicate that checks for its int[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.equalTo(null)  ) == null
	 *     Requirements.require([],        Requirements.equalTo(null)  ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],        Requirements.equalTo([])    ) == []
	 *     Requirements.require([1],       Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Requirements.require([1, 2],    Requirements.equalTo([1, 2])) == [1, 2]
	 *     Requirements.require([2, 1],    Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Requirements.require([1, 2, 3], Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<int[]> equalTo(int[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}

	/**
	 * Returns a predicate that checks for its long[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,      Requirements.equalTo(null)  ) == null
	 *     Requirements.require([],        Requirements.equalTo(null)  ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],        Requirements.equalTo([])    ) == []
	 *     Requirements.require([1],       Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Requirements.require([1, 2],    Requirements.equalTo([1, 2])) == [1, 2]
	 *     Requirements.require([2, 1],    Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Requirements.require([1, 2, 3], Requirements.equalTo([1, 2])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<long[]> equalTo(long[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}
	
	/**
	 * Returns a predicate that checks for its char[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,            Requirements.equalTo(null)      ) == null
	 *     Requirements.require([],              Requirements.equalTo(null)      ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],              Requirements.equalTo([])        ) == []
	 *     Requirements.require(['1'],           Requirements.equalTo(['1', '2'])) # throws IllegalArgumentException
	 *     Requirements.require(['1', '2'],      Requirements.equalTo(['1', '2'])) == ['1', '2']
	 *     Requirements.require(['2', '1'],      Requirements.equalTo(['1', '2'])) # throws IllegalArgumentException
	 *     Requirements.require(['1', '2', '3'], Requirements.equalTo(['1', '2'])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<char[]> equalTo(char[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}

	/**
	 * Returns a predicate that checks for its boolean[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,                 Requirements.equalTo(null)         ) == null
	 *     Requirements.require([],                   Requirements.equalTo(null)         ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],                   Requirements.equalTo([])           ) == []
	 *     Requirements.require([true],               Requirements.equalTo([true, false])) # throws IllegalArgumentException
	 *     Requirements.require([true, false],        Requirements.equalTo([true, false])) == [true, false]
	 *     Requirements.require([false, true],        Requirements.equalTo([true, false])) # throws IllegalArgumentException
	 *     Requirements.require([true, false, false], Requirements.equalTo([true, false])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<boolean[]> equalTo(boolean[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}

	/**
	 * Returns a predicate that checks for its float[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,            Requirements.equalTo(null)      ) == null
	 *     Requirements.require([],              Requirements.equalTo(null)      ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],              Requirements.equalTo([])        ) == []
	 *     Requirements.require([1.0],           Requirements.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Requirements.require([1.0, 2.0],      Requirements.equalTo([1.0, 2.0])) == [1.0, 2.0]
	 *     Requirements.require([2.0, 1.0],      Requirements.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Requirements.require([1.0, 2.0, 3.0], Requirements.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<float[]> equalTo(float[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}

	/**
	 * Returns a predicate that checks for its double[] object to be <code>== arr</code> (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,            Requirements.equalTo(null)      ) == null
	 *     Requirements.require([],              Requirements.equalTo(null)      ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],              Requirements.equalTo([])        ) == []
	 *     Requirements.require([1.0],           Requirements.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Requirements.require([1.0, 2.0],      Requirements.equalTo([1.0, 2.0])) == [1.0, 2.0]
	 *     Requirements.require([2.0, 1.0],      Requirements.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Requirements.require([1.0, 2.0, 3.0], Requirements.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static Predicate<double[]> equalTo(double[] arr) {
		return nameInt(t -> Arrays.equals(t, arr), () -> String.format("Must be equal to %s",Arrays.toString(arr)));
	}

	/**
	 * Returns a predicate that checks for its Object[] object to be <code>== arr</code> (using Arrays.deepEquals).
	 * An object must not be equal to another object using Arrays.deepEquals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null,            Requirements.equalTo(null)      ) == null
	 *     Requirements.require([],              Requirements.equalTo(null)      ) # throws IllegalArgumentException("Must be equal to null")
	 *     Requirements.require([],              Requirements.equalTo([])        ) == []
	 *     Requirements.require(["1"],           Requirements.equalTo(["1", "2"])) # throws IllegalArgumentException
	 *     Requirements.require(["1", "2"],      Requirements.equalTo(["1", "2"])) == ["1", "2"]
	 *     Requirements.require(["2", "1"],      Requirements.equalTo(["1", "2"])) # throws IllegalArgumentException
	 *     Requirements.require(["1", "2", "3"], Requirements.equalTo(["1", "2"])) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param <E> the element type of the array being tests
	 * @param arr The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static <E> Predicate<E[]> equalTo(E[] arr) {
		return nameInt(t -> Arrays.deepEquals(t, arr), () -> String.format("Must be equal to %s",Arrays.deepToString(arr)));
	}

	/**
	 * Returns a predicate that checks for its Object object to be <code>== arr</code> (using equals).
	 * An object must be equal to another object using equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.equalTo("1") ) # throws IllegalArgumentException
	 *     Requirements.require(null, Requirements.equalTo(null)) == null
	 *     Requirements.require("",   Requirements.equalTo(null)) # throws IllegalArgumentException
	 *     Requirements.require("1",  Requirements.equalTo("1") ) == "1"
	 *     Requirements.require("2",  Requirements.equalTo("1") ) # throws IllegalArgumentException
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== arr</code>
	 */
	public static <T> Predicate<T> equalTo(T val) {
		return nameInt(t -> Objects.equals(t, val), () -> String.format("Must be equal to %s", val));
	}

	/**
	 * Returns a predicate that checks for its object to be <code>== val</code> (using pointer equality).
	 * An object must be equal to another object using equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(null, Requirements.same("1") ) # throws IllegalArgumentException("Must be same object as 1")
	 *     Requirements.require(null, Requirements.same(null)) == null
	 *     Requirements.require("",   Requirements.same(null)) # throws IllegalArgumentException("Must be same object as null")
	 *     Requirements.require("1",  Requirements.same("1") ) == "1"
	 *     Requirements.require("2",  Requirements.same("1") ) # throws IllegalArgumentException("Must be same object as 1")
	 *     Foo foo = new Foo();
	 *     Foo bar = new Foo();
	 *     Requirements.require(foo, Requirements.same(bar) # throws IllegalArgumentException("Must be same object as ...")
	 * </pre>
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to - Can be null
	 * @return A predicate that checks for its object to be <code>== val</code>
	 */
	public static <T> Predicate<T> same(T val) {
		return nameInt(t -> t == val, () -> String.format("Must be same object as %s", val));
	}

	/**
	 * Returns a predicate that is the "and" of <code>p1</code> and <code>p2</code>.
	 * An object must match 2 predicates.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(1, Requirements.and(Requirements.gt(0), Requirements.lt(2))) == 1
	 *     Requirements.require(1, Requirements.and(Requirements.gt(1), Requirements.lt(2))) # throws IllegalArgumentException("Must (Be greater than 1) and (Be less than 2)")
	 *     Requirements.require(1, Requirements.and(Requirements.gt(0), Requirements.lt(0))) # throws IllegalArgumentException("(Must (Be greater than 0) and (Be less than 0)")
	 * </pre>
	 *     
	 * @param <T> the type of the object being tested
	 * @param p1 The first predicate  - Can't be null - always evaluated
	 * @param p2 The second predicate - Can't be null - conditionally evaluated
	 * @return A predicate that is the "and" of <code>p1</code> and <code>p2</code>
	 */
	public static <T> Predicate<T> and(Predicate<T> p1, Predicate<T> p2) {
		requireInt(p1, () -> "p1", notNull());
		requireInt(p2, () -> "p2", notNull());
		return nameInt(p1.and(p2), () -> String.format("Must (%s) and (%s)",
				removeMust(p1),
				removeMust(p2)));
	}

	/**
	 * Returns a predicate that implements if-then-else constructs.  If <code>ifClause</code> is satisfied,
	 *    then <code>thenClause</code> must be satisfied, else <code>elseClause</code> must be satisfied.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require( 5, Requirements.ifThenElse(Requirements.gt(0), Requirements.gt(3), Requirements.lt(-2)) == 5
	 *     Requirements.require( 2, Requirements.ifThenElse(Requirements.gt(0), Requirements.gt(3), Requirements.lt(-2)) # throws IllegalArgumentException("if (Be greater than 0) then (Must be greater than 3) else (Be less than -2)")
	 *     Requirements.require(-4, Requirements.ifThenElse(Requirements.gt(0), Requirements.gt(3), Requirements.lt(-2)) == -4
	 *     Requirements.require(-1, Requirements.ifThenElse(Requirements.gt(0), Requirements.gt(3), Requirements.lt(-2)) # throws IllegalArgumentException("if (Be greater than 0) then (Must be greater than 3) else (Be less than -2)")
	 * </pre>
	 *     
	 * @param <T> the type of the object being tested
	 * @param ifClause The first predicate  - Can't be null
	 * @param thenClause The second predicate - Can't be null
	 * @param elseClause The second predicate - Can't be null
	 * @return A predicate that implements if-then-else constructs
	 */
	public static <T> Predicate<T> ifThenElse(Predicate<T> ifClause, Predicate<T> thenClause, Predicate<T> elseClause) {
		requireInt(ifClause, () -> "ifClause", notNull());
		requireInt(thenClause, () -> "thenClause", notNull());
		requireInt(elseClause, () -> "elseClause", notNull());
		return nameInt(obj -> ifClause.test(obj) ? thenClause.test(obj) : elseClause.test(obj),
				() -> String.format("if (%s) then (%s) else (%s)", removeMust(ifClause), thenClause, elseClause));
	}
	
	/**
	 * Returns a predicate that implements if-then constructs.  If <code>ifClause</code> is satisfied,
	 *    then <code>thenClause</code> must be satisfied.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require( 5, Requirements.ifThen(Requirements.gt(0), Requirements.gt(3)) == 5
	 *     Requirements.require( 2, Requirements.ifThen(Requirements.gt(0), Requirements.gt(3)) # throws IllegalArgumentException("if (Be greater than 0) then (Must be greater than 3)")
	 *     Requirements.require(-1, Requirements.ifThen(Requirements.gt(0), Requirements.gt(3)) == -1
	 * </pre>
	 *     
	 * @param <T> the type of the object being tested
	 * @param ifClause The first predicate  - Can't be null
	 * @param thenClause The second predicate - Can't be null 
	 * @return A predicate that implements if-then-else constructs
	 */
	public static <T> Predicate<T> ifThen(Predicate<T> ifClause, Predicate<T> thenClause) {
		requireInt(ifClause, () -> "ifClause", notNull());
		requireInt(thenClause, () -> "thenClause", notNull());
		return nameInt(obj -> ifClause.test(obj) ? thenClause.test(obj) : true,
				() -> String.format("if (%s) then (%s)", removeMust(ifClause), thenClause));
	}
	
	/**
	 * Returns a predicate that ensures an exception is not thrown by a body of code.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require("abc", Requirements.doesNotThrowException(Requirements.nameC(
	 *         x -&gt; {throw new Exception();}, () -&gt; "foo")) # throws IllegalArgumentException("foo: Must not throw exception")
	 *     Requirements.require("abc", Requirements.doesNotThrowException(Requirements.nameC(x -&gt; {}, () -&gt; "foo")) == "abc"
	 * </pre>
	 * 
	 * @param <T> The type of the object being tested
	 * @param <E> The exception type, so that checked exceptions can be used.
	 * @param consumer The consumer of the object.
	 * @return true, if no exception was thrown, false otherwise.
	 */
	public static <T, E extends Throwable> Predicate<T> doesNotThrowException(ThrowingConsumer<T, E> consumer) {
		return nameInt(obj -> {
			try {
				consumer.accept(obj);
			} catch(Throwable t) {
				return false;
			}
			return true;
		}, () -> String.format("%s: Must not throw an exception", consumer)) ;
	}

	/**
	 * Returns a predicate that is the "or" of <code>p1</code> and <code>p2</code>.
	 * An object must match one of two predicates.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(1, Requirements.or(Requirements.gt(0), Requirements.lt(2))) == 1
	 *     Requirements.require(1, Requirements.or(Requirements.gt(1), Requirements.lt(2))) == 1
	 *     Requirements.require(1, Requirements.or(Requirements.gt(0), Requirements.lt(0))) == 1
	 *     Requirements.require(1, Requirements.or(Requirements.gt(1), Requirements.gt(3))) # throws IllegalArgumentException("Must (Be greater than 1) or (Be less than 3)")
	 * </pre>
	 *     
	 * @param <T> the type of the object being tested
	 * @param p1 The first predicate - Can't be null - always evaluated
	 * @param p2 The second predicate - Can't be null - conditionally evaluated
	 * @return A predicate that is the "or" of <code>p1</code> and <code>p2</code>
	 */
	public static <T> Predicate<T> or(Predicate<T> p1, Predicate<T> p2) {
		requireInt(p1, () -> "p1", notNull());
		requireInt(p2, () -> "p2", notNull());
		return nameInt(p1.or(p2), () -> String.format("Must (%s) or (%s)",
				removeMust(p1),
				removeMust(p2)));
	}

	/**
	 * Returns a predicate that is the "not" of <code>p</code>.
	 * An object must match not match a sub predicate.
	 * NULL's are allowed.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require(1, Requirements.negate(Requirements.gt(0))) # throws IllegalArgumentException("Must not (Be greater than 0)")
	 *     Requirements.require(1, Requirements.negate(Requirements.gt(1))) == 1
	 * </pre>
	 *     
	 * @param <T> the type of the object being tested
	 * @param p The predicate to negate - Can't be null 
	 * @return A predicate that is the "not" of <code>p</code>.
	 */
	public static <T> Predicate<T> negate(Predicate<T> p) {
		requireInt(p, notNull());
		return nameInt(p.negate(), () -> String.format("Must not (%s)", removeMust(p)));
	}
	
	/**
	 * Add a toString to <code>pred</code>.
	 * This is the mechanism by which we can associate strings
	 * with predicates, giving nice messages when they fail.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.name(someobj -&gt; true, () -&gt; "True").toString() == "True"
	 * </pre>
	 * 
	 * @param <T> The type of the object being tested
	 * @param pred The predicate to decorate with a message - Can't be null 
	 * @param msg The supplier of the message - Can't be null 
	 * @return A predicate whose toString will return the msg
	 */
	public static <T> Predicate<T> name(Predicate<T> pred, Supplier<String> msg) {
		return nameInt(requireInt(pred, () -> "pred", notNull()), requireInt(msg, () -> "msg", notNull()));
	}
	
	/**
	 * Returns the Map::values function named "values".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.values().apply({a =&gt; 1, b =&gt; 2}) == [1, 2]
	 *     Requirements.values().toString() == "values"
	 * </pre>
	 *     
	 * @param <K> The Map key type
	 * @param <V> The Map value type
	 * @param <T> The Map type
	 * @return A named function representing Map::values
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Function<T, Collection<? extends V>> values() {
		return nameFInt(m -> m.values(), () -> "values");
	}

	/**
	 * Returns the Map::keySet function named "keys".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.values().apply({a =&gt; 1, b =&gt;2}) == [1, 2]
	 *     Requirements.values().toString() == "keys"
	 * </pre>
	 *     
	 * @param <K> The Map key type
	 * @param <V> The Map value type
	 * @param <T> The Map type
	 * @return A named function representing Map::values
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Function<T, Set<? extends K>> keySet() {
		return nameFInt(m -> m.keySet(), () -> "keys");
	}

	/**
	 * Returns the Map::size function named "size".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.mapSize().apply({a =&gt; 1, b =&gt; 2}) == 2
	 *     Requirements.mapSize().toString() == "size"
	 * </pre>
	 *     
	 * @param <K> The Map key type
	 * @param <V> The Map value type
	 * @param <T> The Map type
	 * @return A named function representing Map::size
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Function<T, Integer> mapSize() {
		return nameFInt(m -> m.size(), () -> "size");
	}

	/**
	 * Returns the Object::toString function named "toString".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.stringify().apply(3) == "3"
	 *     Requirements.stringify().toString() == "toString"
	 * </pre>
	 *
	 * @param <T> the type of the object being operated on
	 * @return A named function representing Object::toString
	 */
	public static <T> Function<T, String> stringify() {
		return nameFInt(Object::toString, () -> "toString");
	}

	/**
	 * Returns the Object::hashCode function named "hash".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.hash().apply(o) == o.hashCode()
	 *     Requirements.hash().toString() == "hash"
	 * </pre>
	 *     
	 * @param <T> the type of the object being operated on
	 * @return A named function representing Object::hash
	 */
	public static <T> Function<T, Integer> hash() {
		return nameFInt(Object::hashCode, () -> "hash");
	}

	/**
	 * Returns the String::length function named "length".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.length().apply("abc") ==length3
	 *     Requirements.length().toString() == "hash"
	 * </pre>
	 *     
	 * @return A named function representing Object::hash
	 */
	public static Function<String, Integer> length() {
		return nameFInt(String::length, () -> "length");
	}

	/**
	 * Returns the Collection::size function named "size".
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.size().apply([4,5]) == 2
	 *     Requirements.size().toString() == "size"
	 * </pre>
	 *     
	 * @param <E> the type for the element of the collection
	 * @param <T> the type of the object being operated on
	 * @return A named function representing Collection::size
	 */
	public static <E, T extends Collection<? extends E>> Function<T, Integer> size() {
		return nameFInt(c -> c.size(), () -> "size");
	}

	/**
	 * Adds a toString to a predicate.   This is the mechanism by which we can associate strings
	 * with predicates, giving nice messages when they fail.   Function are primarily used
	 * with {@link #chain(Function, Predicate)}
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.nameF(String::length, () -&gt; "Length").toString() == "Length"
	 * </pre>
	 *     
	 * @param <T> The type being operated on by the function
	 * @param <U> The return type of the function
	 * @param function The function to decorate with a message - Can't be null 
	 * @param msg The supplier of the message - Can't be null 
	 * @return A function whose toString will return the msg.
	 */
	public static <T, U> Function<T, U> nameF(Function<T, U> function, Supplier<String> msg) {
		return nameFInt(requireInt(function, () -> "f", notNull()), requireInt(msg, () -> "msg", notNull()));
	}

	/**
	 * Adds a toString to a consumer.   This is the mechanism by which we can associate strings
	 * with consumers, giving nice messages when they fail.   Consumers are primarily used
	 * with {@link #doesNotThrowException(ThrowingConsumer)}
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.nameC(x -&gt; {}, () -&gt; "Empty").toString() == "Empty"
	 * </pre>
	 * 
	 * @param <T> The type being operated on by the consumer
	 * @param <E> The exception type
	 * @param consumer The consumer to decorate with a message - Can't be null 
	 * @param msg The supplier of the message - Can't be null 
	 * @return A consumer whose toString will return the msg.
	 */
	public static <T, E extends Throwable> ThrowingConsumer<T, E>  nameC(ThrowingConsumer<T, E> consumer, Supplier<String> msg) {
		return nameCInt(requireInt(consumer, () -> "consumer", notNull()), requireInt(msg, () -> "msg", notNull()));
	}

	/**
	 * Returns a predicate for the test of <code>f(o)</code>.
	 * A function of an object must match match a sub predicate.
	 * NULL's are allowed, but only if the function allows null, so care needs to
	 * be taken here.
	 * 
	 * <pre>
	 * Example:
	 *     Requirements.require("abc", Requirements.chain(Requirements.length(), Requirements.gt(2)) == "abc"
	 *     Requirements.require("ab",  Requirements.chain(Requirements.length(), Requirements.gt(2)) # throws IllegalArgumentException("length: Must be greater than 0")
	 * </pre>
	 *     
	 * If f throws an exception, it will be passed on to the caller.
	 * @param <T> The type of the object being tested
	 * @param <U> The type of the object returned by f
	 * @param f The function - Can't be null 
	 * @param p The predicate of the return value - Can't be null 
	 * @return A predicate for the test of <code>f(o)</code>
	 */
	public static <T, U> Predicate<T> chain(Function<T,U> f, Predicate<U> p) {
		return nameInt(o -> p.test(f.apply(o)), () -> f + ": " + p) ;
	}

	static <E> boolean allMembers(Iterable<? extends E> collection, Predicate<E> memberTest) {
		for(E member: collection) {
			if (!memberTest.test(member)) {
				return false;
			}
		}
		return true;
	}

	static <E> boolean anyMember(Iterable<? extends E> collection, Predicate<E> memberTest) {
		for(E member: collection) {
			if (memberTest.test(member)) {
				return true;
			}
		}
		return false;
	}

	static <T> String removeMust(Predicate<T> p1) {
		return removeMust(p1.toString());
	}

	static String removeMust(String msg) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i + 5 < msg.length()) {
			if (msg.charAt(i) == 'M' && msg.charAt(i + 1) == 'u'
					&& msg.charAt(i + 2) == 's' && msg.charAt(i + 3) == 't'
					&& msg.charAt(i + 4) == ' ') {
				sb.append(Character.toUpperCase(msg.charAt(i + 5)));
				i += 6;
			} else {
				sb.append(msg.charAt(i++));
			}
		}
		return sb + msg.substring(i);
	}

	private static <T, U> Function<T, U> nameFInt(Function<T, U> f, Supplier<String> msg) {
		return new Function<T, U>() {
			@Override
			public U apply(T t) {
				return f.apply(t);
			}
			
			@Override
			public String toString() {
				return msg.get();
			}
		};
	}

	/**
	 * Compares two objects, allowing for null.
	 * @param <T> The Comparable-based type
	 * @param o1 Object 1 - Can be null 
	 * @param o2 Object 2 - Can be null 
	 * @return < 0 if o1 < o2, 0 if o1 = o2 > 0, if o1 > o2
	 */
	static <T extends Comparable<T>> int compare(T o1, T o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			}
			return -1;
		}
		if (o2 == null) {
			return 1;
		}
		return o1.compareTo(o2);
	}

	private static <T, E extends Exception> T requireInt(T obj, Predicate<T> validator, BiFunction<T, Predicate<T>, E> ex) throws E {
		if (!validator.test(obj)) {
			throw ex.apply(obj, validator);
		}
		return obj;
	}

	private static <T> T requireInt(T obj, Predicate<T> validator) {
		return requireInt(obj, validator, (o, v) -> new IllegalArgumentException(validator.toString()));
	}

	private static <T> T requireInt(T obj, Supplier<String> msg, Predicate<T> validator) {
		return requireInt(obj, validator, (o, v) -> new IllegalArgumentException(String.format("%s: %s", msg.get(), validator)));
	}

	private static <T> Predicate<T> nameInt(Predicate<T> pred, Supplier<String> msg) {
		return new Predicate<T>() {
	
			@Override
			public boolean test(T t) {
				return pred.test(t);
			}
			
			@Override
			public String toString() {
				return msg.get();
			}
		};
	}

	private static <T, E extends Throwable> ThrowingConsumer<T, E> nameCInt(ThrowingConsumer<T, E> cons, Supplier<String> msg) {
		return new ThrowingConsumer<T, E>() {

			@Override
			public void accept(T t) throws E {
				cons.accept(t);				
			}
			@Override
			public String toString() {
				return msg.get();
			}
		};
	}
}
