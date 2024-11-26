package com.jdttst.utils;

//
// Taken from http://github.com/fpinjava/fpinjava.  No copyright in original code.
//

/** This is a simple interface representing a function that takes no parameters and supplies
 *  a value.  It takes advantage of the Single Abstract Method (SAM) feature of Java to allow
 *  the use of a lambda to be used as an implementation of this interface.
 * <p>
 * An example use of this is in the Result class' "getOrElse" method.  If the Result object
 * is of subtype Success then the function passed to the method is not called.
 * Instead the successValue is returned.  If the subtype is Failure or Empty then the
 * function is called and the value it provides is returned.
 *
 * <pre>{@code
 *   // Create a Result.
 *   Result<Integer> r1 = success(84);
 *   ...
 *   Integer v1 = r1.getOrElse(() -> 42);
 *   // In this case the following will print out '84'
 *   System.out.println(v1);
 *   ...
 *
 *   Result<Integer> r2 = failure("Bad thing happened")
 *   ...
 *   Integer v2 = r2.getOrElse(() -> 42);
 *   // In this case the following will print out '42'
 *   System.out.println(v2);
 * }</pre>
 * */
public interface Supplier<T> {
    T get();
 }
