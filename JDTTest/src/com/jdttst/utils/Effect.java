package com.jdttst.utils;

//
// Taken from http://github.com/fpinjava/fpinjava.  No copyright in original code.
//

/**
 * This interface represents a 'side-effecting' function.  It is provided a value and it
 * does something with that value but doesn't return any value.  It takes advantage of
 * the Single Abstract Method (SAM) feature of Java to allow the use of a lambda to be
 * used as an implementation of this interface.
 * <p>
 * An example use of this is in the Result class' "forEach" method.  If the Result object
 * is of subtype <code>Failure</code> or <code>Empty</code> then the function is not called.  
 * <p>
 * If the subtype is <code>Success then the function is called with the contained value.
 *
 * <pre>{@code
 *   // Create a Result.
 *   Result<Integer> r1 = success(42);
 *   ...
 *   // In this case the following will print out '42'
 *   r1.forEach((i) -> System.out.println(i));
 *   ...
 *
 *   Result<Integer> r2 = failure(new Exception("Bad thing happened"))
 *   ...
 *   // In this case the following will not print out anything.
 *   r2.forEach((i) -> System.out.println(i));
 * }</pre>
 */
public interface Effect<T> {
    /**
     * Actually execute the Effect.
     * @param t an instance of the type this interface is instantiated on.
     */
	void apply(T t);
}
