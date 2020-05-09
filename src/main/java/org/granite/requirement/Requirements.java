package org.granite.requirement;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This code licensed under Mozilla Public License Version 2.0.
 * See attached text in this jar.
 * Utility for making generalized requirements.
 * A thorough understanding of "lambda"'s is very helpful, but not strictly required.
 * This class is very small, but very versatile.
 * Possible use cases: unit testing, input validation, code assertions.
 */
public final class Requirements {
	private static final Predicate<?> NOT_NULL = nameInt(o -> o != null, () -> "Must not be null");
	private static final Predicate<String> NOT_BLANK = nameInt(o -> o != null && o.length() > 0, () -> "Must not be blank");

	private Requirements() {		
	}
	
	/**
	 * Performs a test on the argument, returning that argument if it succeeds, but throwing
	 * a user supplied exception if it fails.    This is the most general form of "require".
	 * 
	 * Example:
	 *     Require.require(null, Require.notNull(), (o, v) -> new MyException()) # throws MyException
	 *     Require.require("abc", Require.notNull(), (o, v) -> new MyException()) == "abc"
	 *     
	 * @param <T> The type of the object being tested
	 * @param <EX> The type of the exception that could be thrown.
	 * @param obj The object to verify
	 * @param validator The predicate that must succeed.
	 * @param ex The Exception supplier
	 * @return The verified object
	 * @throws EX if the test fails
	 */
	public static <T, EX extends Exception> T require(T obj, Predicate<T> validator, BiFunction<T, Predicate<T>, EX> ex) throws EX {
		return requireInt(obj, requireInt(validator, () -> "validator", notNull()), requireInt(ex, () -> "ex", notNull()));
	}

	/**
	 * Performs a test on the argument, returning that argument if it succeeds, but throwing
	 * an {@link IllegalArgumentException} if it fails.   A specified prefix is prepended to the beginning of the message
	 * in that exception.
	 * 
	 * Example:
	 *     Require.require(null, Require.notNull(), () -> "someobj") # throws IllegalArgumentException("someobj: Must not be null")
	 *     Require.require("abc", Require.notNull(), () -> "someobj") == "abc"
	 *     
	 * @param <T> The type of the object being tested
	 * @param obj The object to verify
	 * @param validator The predicate that must succeed.   Note that "toString" on this
	 * 	predicate will be called.   To make this predicate have a toString, use name(Predicate)
	 * @param msg The supplier of the additional message to prepend.
	 * @return The verified object
	 * @throws IllegalArgumentException if the test fails
	 */
	public static <T> T require(T obj, Predicate<T> validator, Supplier<String> msg) {
		return requireInt(obj, requireInt(msg, () -> "msg", notNull()), requireInt(validator, () -> "validator", notNull()));
	}
	
	/**
	 * Performs a test on the argument, returning that argument if it succeeds, throwing
	 * an {@link IllegalArgumentException} if it fails.
	 * 
	 * Example:
	 *     Require.require(null, Require.notNull()) # throws IllegalArgumentException("Must not be null")
	 *     Require.require("abc", Require.notNull())  == "abc"
	 *     
	 * @param <T> The type of the object being tested
	 * @param obj The object to verify
 	 * @param validator The predicate that must succeed.   Note that "toString" on this
	 * 	predicate will be called.   To make this predicate have a toString, use name(Predicate)
	 * @return The object
	 * @throws IllegalArgumentException if the test fails
	 */
	public static <T> T require(T obj, Predicate<T> validator) {
		return requireInt(obj, requireInt(validator, () -> "validator", notNull()));
	}
	
	/**
	 * Returns a predicate that checks for its argument to be not null.
	 * 
	 * Example:
	 *     Require.require(null, Require.notNull()) # throws IllegalArgumentException("Must not be null")
	 *     Require.require("abc", Require.notNull())  == "abc"
	 * 
	 * @param <T> The type of the object being tested
	 * @return a predicate that checks for its argument to be not null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> notNull() {
		return (Predicate<T>) NOT_NULL;
	}

	/**
	 * Returns a predicate that checks for its string argument to be not blank.
	 * A string must be non-null and have a length > 0 to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.notBlank()) # throws IllegalArgumentException("Must not be blank")
	 *     Require.require("", Require.notBlank()) # throws IllegalArgumentException("Must not be blank")
	 *     Require.require("abc", Require.notBlank())  == "abc"
	 * 
	 * @return a predicate that checks for its string argument to be not blank.
	 */
	public static Predicate<String> notBlank() {
		return NOT_BLANK;
	}

	/**
	 * Returns a predicate that checks for its string argument to have a minimum length
	 * A string must be non-null and have a length of at least "n" to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.minLength(2)) # throws IllegalArgumentException("Must have a length of at least 2")
	 *     Require.require("", Require.minLength(2)) # throws IllegalArgumentException("Must have a length of at least 2")
	 *     Require.require("ab", Require.minLength(2))  == "ab"
	 *     Require.require("abc", Require.minLength(2))  == "abc"
	 * 
	 * @param n The minimum length
	 * @return a predicate that checks for its string argument to have a minimum length
	 */
	public static Predicate<String> minLength(int n) {
		requireInt(n, ge(0));
		return nameInt(s -> s != null && s.length() >= n, () -> String.format("Must have a length of at least %d", n));
	}

	/**
	 * Returns a predicate that checks for its string argument to have a maximum length
	 * A string must be non-null and have a length of at most "n" to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.maxLength(2)) # throws IllegalArgumentException("Must have a length of at most 2")
	 *     Require.require("", Require.maxLength(2)) == ""
	 *     Require.require("ab", Require.maxLength(2))  == "ab"
	 *     Require.require("abc", Require.maxLength(2)) # throws IllegalArgumentException("Must have a length of at most 2")
	 * 
	 * @param n The maximum length
	 * @return a predicate that checks for its string argument to have a maximum length
	 */
	public static Predicate<String> maxLength(int n) {
		requireInt(n, ge(0));
		return nameInt(s -> s != null && s.length() <= n, () -> String.format("Must have a length of at most %d", n));
	}

	/**
	 * Returns a predicate that checks for its string argument to match a pattern.
	 * A string must be non-null and match the pattern to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.matches(Pattern.compile("\\d+"))) # throws IllegalArgumentException("Must match \d+")
	 *     Require.require("", Require.matches(Pattern.compile("\\d+"))) # throws IllegalArgumentException("Must match \d+")
	 *     Require.require("1", Require.matches(Pattern.compile("\\d+")))  == "1"
	 *     Require.require("123", Require.matches(Pattern.compile("\\d+")))  == "123"
	 *     Require.require("abc123", Require.matches(Pattern.compile("\\d+"))) # throws IllegalArgumentException("Must match \d+")
	 *     
	 * @param p The pattern.
	 * @return a predicate that checks for its string argument to match a pattern.
	 */
	public static Predicate<String> matches(Pattern p) {
		requireInt(p, notNull());
		return nameInt(s -> s != null && p.asPredicate().test(s), () -> String.format("Must match %s", p));
	}
	
	/**
	 * Returns a predicate that checks for its string argument to match a pattern.
	 * A string must be non-null and match the pattern to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.matches("\\d+")) # throws IllegalArgumentException("Must match \d+")
	 *     Require.require("", Require.matches("\\d+")) # throws IllegalArgumentException("Must match \d+")
	 *     Require.require("1", Require.matches("\\d+"))  == "1"
	 *     Require.require("123", Require.matches("\\d+"))  == "123"
	 *     Require.require("abc123", Require.matches("\\d+")) # throws IllegalArgumentException("Must match \d+")
	 *     
	 * @param p The pattern.
	 * @return a predicate that checks for its string argument to match a pattern.
	 */
	public static Predicate<String> matches(String p) {
		requireInt(p, notNull());
		Pattern q = Pattern.compile(p);
		return nameInt(s -> s != null && q.asPredicate().test(s), () -> String.format("Must match %s", p));
	}
	
	/**
	 * Returns a predicate that checks for its collection based argument to be not empty.
	 * A collection must be non-null and have a size > 0 to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.notEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Require.require([], Require.notEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Require.require([1, 2], Require.notEmpty()) == [1, 2]
	 *     
	 * @param <E> The type of the elements of the collection.
	 * @param <T> The Collection type of the object being tested
	 * @return a predicate that checks for its collection based argument to be not empty.
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> notEmpty() {
		return nameInt(collection -> collection != null && !collection.isEmpty(), () -> "Must not be empty");
	}

	/**
	 * Returns a predicate that checks for its collection to contain an object
	 * A collection must be non-null and contain the given object to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.contains(1)) # throws IllegalArgumentException("Must contain 1")
	 *     Require.require([], Require.contains(1)) # throws IllegalArgumentException("Must contain 1")
	 *     Require.require([2, 3], Require.contains(1)) # throws IllegalArgumentException("Must contain 1")
	 *     Require.require([1, 2], Require.contains(1)) == [1]
	 *     
	 * @param object The object the collection must contain.
	 * @param <E> The type of the elements of the collection.
	 * @param <T> The Collection type of the object being tested
	 * @return a predicate that checks for its collection to contain an object
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> contains(E object) {
		return nameInt(collection -> collection != null && collection.contains(object), () -> String.format("Must contain %s", object));
	}
	
	/**
	 * Returns a predicate that checks for its map to contain a key
	 * A map must be non-null and contain the given key to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.containKey(1)) # throws IllegalArgumentException("Must contain key 1")
	 *     Require.require({}, Require.containKey(1)) # throws IllegalArgumentException("Must contain key 1")
	 *     Require.require({a => 1, b => 2}, Require.containKey(c)) # throws IllegalArgumentException("Must contain 1")
	 *     Require.require({a => 1, b => 2}, Require.containKey(a)) == {a => 1, b => 2}
	 *     
	 * @param object The object the collection must contain.
	 * @param <K> The type of the keys of the map.
	 * @param <V> The type of the values of the map.
	 * @param <T> The Map type of the object being tested
	 * @return a predicate that checks for its map to contain a key
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Predicate<T> containsKey(K key) {
		return nameInt(map -> map != null && map.containsKey(key), () -> String.format("Must contain key %s", key));
	}

	/**
	 * Returns a predicate that checks for its map to be not empty.
	 * A map must be non-null and be not empty to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.mapNotEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Require.require({}, Require.mapNotEmpty()) # throws IllegalArgumentException("Must not be empty")
	 *     Require.require({a => 1, b => 2}, Require.mapNotEmpty()) == {a => 1, b => 2}
	 *     
	 * @param <K> The type of the keys of the map.
	 * @param <V> The type of the values of the map.
	 * @param <T> The Map type of the object being tested
	 * @return a predicate that checks for its map to be not empty
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Predicate<T> mapNotEmpty() {
		return nameInt(map -> map != null && !map.isEmpty(), () -> String.format("Must not be empty"));
	}

	/**
	 * Returns a predicate that checks for its Collection argument to have a minimum size
	 * A collection must be non-null and have a size of at least "n" to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.minSize(2)) # throws IllegalArgumentException("Must have a size of at least 2")
	 *     Require.require([], Require.minSize(2)) # throws IllegalArgumentException("Must have a size of at least 2")
	 *     Require.require([1, 2], Require.minSize(2)) == [1, 2]
	 *     Require.require([1, 2, 3], Require.minSize(2))  == [1, 2, 3]
	 * 
	 * @param <E> The element type of the collection
	 * @param <T> The type of the collection
	 * @param n The minimum size
	 * @return a predicate that checks for its Collection argument to have a minimum size
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> minSize(int n) {
		requireInt(n, ge(0));
		return nameInt(s -> s != null && s.size() >= n, () -> String.format("Must have a size of at least %d", n));
	}

	/**
	 * Returns a predicate that checks for its Collection argument to have a maximum size
	 * A collection must be non-null and have a size of at most "n" to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.maxSize(2)) # throws IllegalArgumentException("Must have a size of at most 2")
	 *     Require.require([], Require.maxSize(2)) == []
	 *     Require.require([1, 2], Require.maxSize(2)) == [1, 2]
	 *     Require.require([1, 2, 3], Require.maxSize(2)) # throws IllegalArgumentException("Must have a size of at most 2")
	 * 
	 * @param <E> The element type of the collection
	 * @param <T> The type of the collection
	 * @param maxSize The maximum size
	 * @return a predicate that checks for its Collection argument to have a maximum size
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> maxSize(int maxSize) {
		requireInt(maxSize, ge(0));
		return nameInt(s -> s != null && s.size() <= maxSize, () -> String.format("Must have a size of at most %d", maxSize));
	}

	/**
	 * Returns a predicate that checks for its collection to be a superset of another collection
	 * A collection must be non-null and be a superset of the given collection to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.superSetOf([1, 2])) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Require.require([], Require.superSetOf([1, 2])) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Require.require([1], Require.superSetOf([1, 2])) # throws IllegalArgumentException("Must be superset of [1, 2]")
	 *     Require.require([1, 2], Require.superSetOf([1, 2])) == [1, 2]
	 *     Require.require([1, 2, 3], Require.superSetOf([1, 2])) == [1, 2, 3]
	 *  
	 * @param other The collection to test against. (not null)
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return a predicate that checks for its collection to be a superset of another collection
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> superSetOf(Collection<? extends E> other) {
		requireInt(other, notNull());
		return nameInt(collection -> collection != null && collection.containsAll(other), () -> String.format("Must be a superset of %s", other));
	}
	
	/**
	 * Returns a predicate that checks for its collection to be a superset of another collection
	 * A collection must be non-null and be a superset of the given collection to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.superSetOf(1, 2)) # throws IllegalArgumentException("Must be superset of {1, 2}")
	 *     Require.require([], Require.superSetOf(1, 2)) # throws IllegalArgumentException("Must be superset of {1, 2}")
	 *     Require.require([1], Require.superSetOf(1, 2)) # throws IllegalArgumentException("Must be superset of {1, 2}")
	 *     Require.require([1, 2], Require.superSetOf(1, 2)) == [1, 2]
	 *     Require.require([1, 2, 3], Require.superSetOf(1, 2)) == [1, 2, 3]
	 *  
	 * @param other The collection to test against. (not null)
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return a predicate that checks for its collection to be a superset of another collection
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> superSetOf(@SuppressWarnings("unchecked") E ... other) {
		Set<E> setOther = new LinkedHashSet<>(Arrays.asList(requireInt(other, notNull())));
		return nameInt(collection -> collection != null && collection.containsAll(setOther), () -> String.format("Must be a superset of %s", setOther));
	}

	/**
	 * Returns a predicate that checks for its collection to be a subset another collection
	 * A collection must be non-null and be a subset of the given collection to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.subSetOf([1, 2])) # throws IllegalArgumentException("Must be subset of [1, 2]")
	 *     Require.require([], Require.subSetOf([1, 2])) == []
	 *     Require.require([1], Require.subSetOf([1, 2])) == [1]
	 *     Require.require([1, 2], Require.subSetOf([1, 2])) == [1, 2]
	 *     Require.require([1, 2, 3], Require.subSetOf([1, 2])) # throws IllegalArgumentException("Must be subset of [1, 2]")
	 *     
	 * @param other The collection to test against.
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return a predicate that checks for its collection to be a subset another collection
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> subSetOf(Collection<? extends E> other) {
		requireInt(other, notNull());
		return nameInt(collection -> collection != null && other.containsAll(collection), () -> String.format("Must be a subset of %s", other));
	}

	/**
	 * Returns a predicate that checks for its collection to be a subset another collection
	 * A collection must be non-null and be a subset of the given collection to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.subSetOf(1, 2)) # throws IllegalArgumentException("Must be subset of {1, 2}")
	 *     Require.require([], Require.subSetOf(1, 2)) == []
	 *     Require.require([1], Require.subSetOf(1, 2)) == [1]
	 *     Require.require([1, 2], Require.subSetOf(1, 2)) == [1, 2]
	 *     Require.require([1, 2, 3], Require.subSetOf(1, 2)) # throws IllegalArgumentException("Must be subset of {1, 2}")
	 *     
	 * @param other The collection to test against.
	 * @param <E> The type of the elements of either collection.
	 * @param <T> The Collection type of the object being tested
	 * @return a predicate that checks for its collection to be a subset another collection
	 */
	public static <E, T extends Collection<? extends E>> Predicate<T> subSetOf(@SuppressWarnings("unchecked") E ... other) {
		Set<E> setOther = new LinkedHashSet<>(Arrays.asList(requireInt(other, notNull())));
		return nameInt(collection -> collection != null && setOther.containsAll(collection), () -> String.format("Must be a subset of %s", setOther));
	}
	
	/**
	 * Returns a predicate that checks for its object to be a member of a collection
	 * An object must be a member of the given collection to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.memberOf([])) # throws IllegalArgumentException("Must be a member of {}")
	 *     Require.require(null, Require.memberOf([null])) == null
	 *     Require.require(null, Require.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of {1, 2}")
	 *     Require.require(1, Require.memberOf([1, 2])) == 1
	 *     Require.require(3, Require.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of {1, 2}")
	 *     
	 * @param coll The collection that must contain the object.
	 * @param <T> The type of the object being tested
	 * @return a predicate that checks for its object to be a member of a collection
	 */
	public static <T> Predicate<T> memberOf(@SuppressWarnings("unchecked") T ... coll) {
		Set<T> setColl = new LinkedHashSet<>(Arrays.asList(requireInt(coll, notNull())));
		return nameInt(setColl::contains, () -> String.format("Must be a member of %s", setColl));
	}
	
	/**
	 * Returns a predicate that checks for its object to be a member of a collection
	 * An object must be a member of the given collection to satisfy the condition.
	 * 
	 * Example:
	 *     Require.require(null, Require.memberOf([])) # throws IllegalArgumentException("Must be a member of []")
	 *     Require.require(null, Require.memberOf([null])) == null
	 *     Require.require(null, Require.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of [1, 2]")
	 *     Require.require(1, Require.memberOf([1, 2])) == 1
	 *     Require.require(3, Require.memberOf([1, 2])) # throws IllegalArgumentException("Must be a member of [1, 2]")
	 *     
	 * @param coll The collection that must contain the object.
	 * @param <T> The type of the object being tested
	 * @return a predicate that checks for its object to be a member of a collection
	 */
	public static <T> Predicate<T> memberOf(Collection<? extends T> coll) {
		requireInt(coll, notNull());
		return nameInt(coll::contains, () -> String.format("Must be a member of %s", coll));
	}
	
	/**
	 * Returns a predicate that checks for its object to be less than another.
	 * An object must be less than another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * Example:
	 *     Require.require(null, Require.lt(null)) # throws IllegalArgumentException("Must be less than null")
	 *     Require.require(null, Require.lt(2)) == null
	 *     Require.require(1, Require.lt(2)) == 1
	 *     Require.require(2, Require.lt(2)) # throws IllegalArgumentException("Must be less than 2")
	 *     Require.require(3, Require.lt(2)) # throws IllegalArgumentException("Must be less than 2")
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to.
	 * @return a predicate that checks for its object to be less than another.
	 */
	public static <T extends Comparable<T>> Predicate<T> lt(T val) {
		return nameInt(o -> compare(o, val) < 0, () -> String.format("Must be less than %s", val));
	}

	/**
	 * Returns a predicate that checks for its object to be greater than another.
	 * An object must be greater than another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * Example:
	 *     Require.require(null, Require.gt(null)) # throws IllegalArgumentException("Must be greater than null")
	 *     Require.require(null, Require.gt(2)) # throws IllegalArgumentException("Must be greater than 2")
	 *     Require.require(1, Require.gt(2)) # throws IllegalArgumentException("Must be greater than 2")
	 *     Require.require(2, Require.gt(2)) # throws IllegalArgumentException("Must be greater than 2")
	 *     Require.require(3, Require.gt(2)) == 3
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to.
	 * @return a predicate that checks for its object to be greater than another.
	 */
	public static <T extends Comparable<T>> Predicate<T> gt(T val) {
		return nameInt(o -> compare(o, val) > 0, () -> String.format("Must be greater than %s", val));
	}

	/**
	 * Returns a predicate that checks for its object to be less than or equal to another.
	 * An object must be less than or equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * Example:
	 *     Require.require(null, Require.le(null)) == null
	 *     Require.require(null, Require.le(2)) == null
	 *     Require.require(1, Require.le(2)) == 1
	 *     Require.require(2, Require.le(2)) == 2
	 *     Require.require(3, Require.le(2)) # throws IllegalArgumentException("Must be less than or equal to 2")
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be less than or equal to another.
	 */
	public static <T extends Comparable<T>> Predicate<T> le(T val) {
		return nameInt(o -> compare(o, val) <= 0, () -> String.format("Must be less than or equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its object to be greater than or equal to another.
	 * An object must be greater than or equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * Example:
	 *     Require.require(null, Require.ge(null)) == null
	 *     Require.require(null, Require.ge(2)) # throws IllegalArgumentException("Must be greater than or equal to 2")
	 *     Require.require(1, Require.ge(2)) # throws IllegalArgumentException("Must be greater than or equal to 2")
	 *     Require.require(2, Require.ge(2)) == 2
	 *     Require.require(3, Require.ge(2)) == 3
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be greater than or equal to another.
	 */
	public static <T extends Comparable<T>> Predicate<T> ge(T val) {
		return nameInt(o -> compare(o, val) >= 0, () -> String.format("Must be greater than or equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its object to be equal to another (via numerical sense).
	 * An object must be equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * Example:
	 *     Require.require(null, Require.eq(null)) == null
	 *     Require.require(null, Require.eq(2)) # throws IllegalArgumentException("Must be equal to 2")
	 *     Require.require(1, Require.eq(2)) # throws IllegalArgumentException("Must be equal to 2")
	 *     Require.require(2, Require.eq(2)) == 2
	 *     Require.require(3, Require.eq(2)) # throws IllegalArgumentException("Must be equal to 2")
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be greater than or equal to another.
	 */
	public static <T extends Comparable<T>> Predicate<T> eq(T val) {
		return nameInt(o -> compare(o, val) == 0, () -> String.format("Must be equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its object to be not equal to another (via numerical sense).
	 * An object must not be equal to another object using compareTo to satisfy the condition.
	 * NULL comparison follows the rule null always less than non-null.
	 * 
	 * Example:
	 *     Require.require(null, Require.ne(null)) # throws IllegalArgumentException("Must be not equal to null")
	 *     Require.require(null, Require.ne(2)) == null
	 *     Require.require(1, Require.ne(2)) == 1
	 *     Require.require(2, Require.ne(2)) # throws IllegalArgumentException("Must be not equal to 2")
	 *     Require.require(3, Require.ne(2)) == 3
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be greater than or equal to another.
	 */
	public static <T extends Comparable<T>> Predicate<T> ne(T val) {
		return nameInt(o -> compare(o, val) != 0, () -> String.format("Must be not equal to %s", val));
	}
	
	/**
	 * Returns a predicate that checks for its byte[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require([1], Require.equalTo([1, 2])) # throws IllegalArgumentException("Must be equal to [1, 2]")
	 *     Require.require([1, 2], Require.equalTo([1, 2])) == [1, 2]
	 *     Require.require([2, 1], Require.equalTo([1, 2])) # throws IllegalArgumentException("Must be equal to [1, 2]")
	 *     Require.require([1, 2, 3], Require.equalTo([1, 2])) # throws IllegalArgumentException("Must be equal to [1, 2]")
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<byte[]> equalTo(byte[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}

	/**
	 * Returns a predicate that checks for its short[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require([1], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Require.require([1, 2], Require.equalTo([1, 2])) == [1, 2]
	 *     Require.require([2, 1], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Require.require([1, 2, 3], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<short[]> equalTo(short[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}

	/**
	 * Returns a predicate that checks for its int[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require([1], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Require.require([1, 2], Require.equalTo([1, 2])) == [1, 2]
	 *     Require.require([2, 1], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Require.require([1, 2, 3], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<int[]> equalTo(int[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}

	/**
	 * Returns a predicate that checks for its long[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require([1], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Require.require([1, 2], Require.equalTo([1, 2])) == [1, 2]
	 *     Require.require([2, 1], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     Require.require([1, 2, 3], Require.equalTo([1, 2])) # throws IllegalArgumentException
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<long[]> equalTo(long[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}
	
	/**
	 * Returns a predicate that checks for its char[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require(['1'], Require.equalTo(['1', '2'])) # throws IllegalArgumentException
	 *     Require.require(['1', '2'], Require.equalTo(['1', '2'])) == ['1', '2']
	 *     Require.require(['2', '1'], Require.equalTo(['1', '2'])) # throws IllegalArgumentException
	 *     Require.require(['1', '2', '3'], Require.equalTo(['1', '2'])) # throws IllegalArgumentException
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<char[]> equalTo(char[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}

	/**
	 * Returns a predicate that checks for its boolean[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require([true], Require.equalTo([true, false])) # throws IllegalArgumentException
	 *     Require.require([true, false], Require.equalTo([true, false])) == [true, false]
	 *     Require.require([false, true], Require.equalTo([true, false])) # throws IllegalArgumentException
	 *     Require.require([true, false, false], Require.equalTo([true, false])) # throws IllegalArgumentException
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<boolean[]> equalTo(boolean[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}

	/**
	 * Returns a predicate that checks for its float[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require([1.0], Require.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Require.require([1.0, 2.0], Require.equalTo([1.0, 2.0])) == [1.0, 2.0]
	 *     Require.require([2.0, 1.0], Require.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Require.require([1.0, 2.0, 3.0], Require.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<float[]> equalTo(float[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}

	/**
	 * Returns a predicate that checks for its double[] object to be equal to another (using Arrays.equals).
	 * An object must not be equal to another object using Arrays.equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require([1.0], Require.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Require.require([1.0, 2.0], Require.equalTo([1.0, 2.0])) == [1.0, 2.0]
	 *     Require.require([2.0, 1.0], Require.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     Require.require([1.0, 2.0, 3.0], Require.equalTo([1.0, 2.0])) # throws IllegalArgumentException
	 *     
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static Predicate<double[]> equalTo(double[] val) {
		return nameInt(t -> Arrays.equals(t, val), () -> String.format("Must be equal to %s",Arrays.toString(val)));
	}

	/**
	 * Returns a predicate that checks for its Object[] object to be equal to another (using Arrays.deepEquals).
	 * An object must not be equal to another object using Arrays.deepEquals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require([], Require.equalTo(null)) # throws IllegalArgumentException("Must be equal to null")
	 *     Require.require([], Require.equalTo([])) == []
	 *     Require.require(["1"], Require.equalTo(["1", "2"])) # throws IllegalArgumentException
	 *     Require.require(["1", "2"], Require.equalTo(["1", "2"])) == ["1", "2"]
	 *     Require.require(["2", "1"], Require.equalTo(["1", "2"])) # throws IllegalArgumentException
	 *     Require.require(["1", "2", "3"], Require.equalTo(["1", "2"])) # throws IllegalArgumentException
	 *     
	 * @param <E> the element type of the array being tests
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static <E> Predicate<E[]> equalTo(E[] val) {
		return nameInt(t -> Arrays.deepEquals(t, val), () -> String.format("Must be equal to %s",Arrays.deepToString(val)));
	}

	/**
	 * Returns a predicate that checks for its Object object to be equal to another (using equals).
	 * An object must be equal to another object using equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.equalTo("1")) # throws IllegalArgumentException
	 *     Require.require(null, Require.equalTo(null)) == null
	 *     Require.require("", Require.equalTo(null)) # throws IllegalArgumentException
	 *     Require.require("1", Require.equalTo("1")) == ""
	 *     Require.require("2", Require.equalTo("1") # throws IllegalArgumentException
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to
	 * @return a predicate that checks for its object to be equal to another
	 */
	public static <T> Predicate<T> equalTo(T val) {
		return nameInt(t -> {
			if (t == null) {
				return val == null;
			}
			if (val == null) {
				return false;
			}
			return t.equals(val);
		}, () -> String.format("Must be equal to %s", val));
	}

	/**
	 * Returns a predicate that checks for its object == another (using pointer equality).
	 * An object must be equal to another object using equals to satisfy the condition.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(null, Require.same("1")) # throws IllegalArgumentException("Must be same object as 1")
	 *     Require.require(null, Require.same(null)) == null
	 *     Require.require("", Require.same(null)) # throws IllegalArgumentException("Must be same object as null")
	 *     Require.require("1", Require.same("1")) == ""
	 *     Require.require("2", Require.same("1") # throws IllegalArgumentException("Must be same object as 1")
	 *     Foo foo = new Foo();
	 *     Foo bar = new Foo();
	 *     Require.require(foo, Require.same(bar) # throws IllegalArgumentException("Must be same object as ...")
	 *     
	 * @param <T> The type of the object being tested
	 * @param val The object to compare to
	 * @return a predicate that checks for its object == another
	 */
	public static <T> Predicate<T> same(T val) {
		return nameInt(t -> t == val, () -> String.format("Must be same object as %s", val));
	}

	/**
	 * Returns a predicate that is the "and" of its sub-predicates.
	 * An object must match 2 predicates.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(1, Require.and(Equal.gt(0), Equal.lt(2)) == 1
	 *     Require.require(1, Require.and(Equal.gt(1), Equal.lt(2)) # throws IllegalArgumentException("(Must be greater than 1) and (Must be less than 2)")
	 *     Require.require(1, Require.and(Equal.gt(0), Equal.lt(0)) # throws IllegalArgumentException("(Must be greater than 0) and (Must be less than 0)")
	 *     
	 * @param <T> the type of the object being tested
	 * @param p1 The first predicate always evaluated)
	 * @param p2 The second predicate conditionally evaluated
	 * @return a predicate that is the "and" of its sub-predicates
	 */
	public static <T> Predicate<T> and(Predicate<T> p1, Predicate<T> p2) {
		requireInt(p1, () -> "p1", notNull());
		requireInt(p2, () -> "p2", notNull());
		return nameInt(p1.and(p2), () -> "(" + p1 + ") and (" + p2 + ")");
	}

	/**
	 * Returns a predicate that is the "or" of its sub-predicates.
	 * An object must match one of two predicates.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(1, Require.or(Equal.gt(0), Equal.lt(2)) == 1
	 *     Require.require(1, Require.or(Equal.gt(1), Equal.lt(2)) == 1
	 *     Require.require(1, Require.or(Equal.gt(0), Equal.lt(0)) == 1
	 *     Require.require(1, Require.or(Equal.gt(1), Equal.gt(3)) # throws IllegalArgumentException("(Must be greater than 1) or (Must be less than 3)")
	 *     
	 * @param <T> the type of the object being tested
	 * @param p1 The first predicate always evaluated)
	 * @param p2 The second predicate conditionally evaluated
	 * @return a predicate that is the "or" of its sub-predicates
	 */
	public static <T> Predicate<T> or(Predicate<T> p1, Predicate<T> p2) {
		requireInt(p1, () -> "p1", notNull());
		requireInt(p2, () -> "p2", notNull());
		return nameInt(p1.or(p2), () -> "(" + p1 + ") or (" + p2 + ")");
	}

	/**
	 * Returns a predicate that is the "not" of its sub-predicate.
	 * An object must match not match a sub predicate.
	 * NULL's are allowed.
	 * 
	 * Example:
	 *     Require.require(1, Require.not(Equal.gt(0)) # throws IllegalArgumentException("not (Must be greater than 0)")
	 *     Require.require(1, Require.not(Equal.gt(1)) == 1
	 *     
	 * @param <T> the type of the object being tested
	 * @param p The predicate to negate.
	 * @return a predicate that is the "not" of its sub-predicate.
	 */
	public static <T> Predicate<T> negate(Predicate<T> p) {
		requireInt(p, notNull());
		return nameInt(p.negate(), () -> "not ("+p+")");
	}
	
	/**
	 * Add a toString to a predicate.   This is the mechanism by which we can associate strings
	 * with predicates, giving nice messages when they fail.
	 * 
	 * Example:
	 *     Require.name(someobj -> true, () -> "True").toString() == "True"
	 * 
	 * @param <T> The type of the object being tested
	 * @param pred The predicate to decorate with a message
	 * @param msg The supplier of the message
	 * @return A predicate whose toString will return the msg.
	 */
	public static <T> Predicate<T> name(Predicate<T> pred, Supplier<String> msg) {
		return nameInt(requireInt(pred, () -> "pred", notNull()), requireInt(msg, () -> "msg", notNull()));
	}
	
	/**
	 * Pairs a string with the Map::values function.
	 * 
	 * Example:
	 *     Require.values().apply({a => 1, b =>2}) == [1, 2]
	 *     Require.values().toString() == "values"
	 *     
	 * @param <K> The Map key type
	 * @param <V> The Map value type
	 * @param <T> The Map type
	 * @return a named function representing Map::values
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Function<T, Collection<? extends V>> values() {
		return nameFInt(Map::values, () -> "values");
	}

	/**
	 * Pairs a string with the Map::keySet function.
	 * 
	 * Example:
	 *     Require.values().apply({a => 1, b =>2}) == [1, 2]
	 *     Require.values().toString() == "keys"
	 *     
	 * @param <K> The Map key type
	 * @param <V> The Map value type
	 * @param <T> The Map type
	 * @return a named function representing Map::values
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Function<T, Set<? extends K>> keySet() {
		return nameFInt(Map::keySet, () -> "keys");
	}

	/**
	 * Pairs a string with the Map::size function.
	 * 
	 * Example:
	 *     Require.mapSize().apply({a => 1, b =>2}) == 2
	 *     Require.mapSize().toString() == "size"
	 *     
	 * @param <K> The Map key type
	 * @param <V> The Map value type
	 * @param <T> The Map type
	 * @return a named function representing Map::size
	 */
	public static <K, V, T extends Map<? extends K,? extends V>> Function<T, Integer> mapSize() {
		return nameFInt(Map::size, () -> "size");
	}

	/**
	 * Pairs a string with the Object::toString function.
	 * 
	 * Example:
	 *     Require.stringify().apply(3) == "3"
	 *     Require.stringify().toString() == "toString"
	 *     
	 * @return a named function representing Object::toString
	 */
	public static <T> Function<T, String> stringify() {
		return nameFInt(Object::toString, () -> "toString");
	}

	/**
	 * Pairs a string with the Object::hashCode function.
	 * 
	 * Example:
	 *     Require.hash().apply(o) == o.hashCode()
	 *     Require.hash().toString() == "hash"
	 *     
	 * @return a named function representing Object::hash
	 */
	public static <T> Function<T, Integer> hash() {
		return nameFInt(Object::hashCode, () -> "hash");
	}

	/**
	 * Pairs a string with the String::length function.
	 * 
	 * Example:
	 *     Require.length().apply("abc") ==length3
	 *     Require.length().toString() == "hash"
	 *     
	 * @return a named function representing Object::hash
	 */
	public static Function<String, Integer> length() {
		return nameFInt(String::length, () -> "length");
	}

	/**
	 * Pairs a string with the Collection::size function.
	 * 
	 * Example:
	 *     Require.size().apply([4,5]) == 2
	 *     Require.size().toString() == "size"
	 *     
	 * @return a named function representing Collection::size
	 */
	public static <E, T extends Collection<? extends E>> Function<T, Integer> size() {
		return nameFInt(Collection::size, () -> "size");
	}

	/**
	 * Add a toString to a predicate.   This is the mechanism by which we can associate strings
	 * with predicates, giving nice messages when they fail.
	 * 
	 * Example:
	 *     Require.nameF(String::length, () -> "Length").toString() == "Length"
	 *     
	 * @param <T> The type being operated on by the function
	 * @param <U> The return type of the function
	 * @param f The function to decorate with a message
	 * @param msg The supplier of the message
	 * @return A function whose toString will return the msg.
	 */
	public static <T, U> Function<T, U> nameF(Function<T, U> f, Supplier<String> msg) {
		return nameFInt(requireInt(f, () -> "f", notNull()), requireInt(msg, () -> "msg", notNull()));
	}

	/**
	 * Returns a predicate for the test of f(o).
	 * A function of an object must match match a sub predicate.
	 * NULL's are allowed, but only if the function allows null, so care needs to
	 * be taken here.
	 * 
	 * Example:
	 *     Require.require("abc", Require.chain(Require.length(), Equal.gt(2)) == "abc"
	 *     Require.require("ab", Require.chain(Require.length(), Equal.gt(2)) # throws IllegalArgumentException("length: Must be greater than 0")
	 *     
	 * If f throws an exception, it will be passed on to the caller.
	 * @param <T> The type of the object being tested
	 * @param <U> The type of the object returned by f
	 * @param f The function 
	 * @param p The predicate of the return value.
	 * @return a predicate for the test of f(o).
	 */
	public static <T, U> Predicate<T> chain(Function<T,U> f, Predicate<U> p) {
		return nameInt(o -> p.test(f.apply(o)), () -> f + ": " + p) ;
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
	 * @param o1 Object 1
	 * @param o2 Object 2
	 * @return < 0 if o1 < o2, 0 if o1 = o2 > 0, if o1 > o2
	 */
	private static <T extends Comparable<T>> int compare(T o1, T o2) {
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
}
