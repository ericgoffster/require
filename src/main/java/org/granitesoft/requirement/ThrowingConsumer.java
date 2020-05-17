package org.granitesoft.requirement;

/**
 * A Consumer that can throw an exception.
 * This is use primarily with {@link Requirements#doesNotThrowException(ThrowingConsumer)}
 * 
 * This code licensed under Mozilla Public License Version 2.0.
 *
 * @param <T> The type of the object being tested
 * @param <E> The exception type
 */
@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}