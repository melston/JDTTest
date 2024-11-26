package com.jdttst.utils;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * This class is a monad implementing the context of the result of an operation that can fail.
 * A successful result is encapsulated in the <code>Success</code> subclass while a 
 * failure is encapsulated in the <code>Failure</code> subclass.  
 * There is a third subclass (<code>Empty</code>) that is used to represent a successful
 * operation that has no meaningful data.
 *
 * The advantage of this class over a class like <code>Optional</code> is that a failure carries with it a
 * meaningful error in the form of a <code>RuntimeException</code> from which we can extract a message.  This
 * allows us to provide the end user with an error when something goes wrong instead of just
 * throwing up our hands and saying "Oops".
 *
 * This class is adapted from the code provided in the book "Functional Programming in Java"
 * written by Pierre-Yves Saument and published by Manning in 2017.  The code from the book is
 * available on github (http://github.com/fpinjava/fpinjava).
 *
 * An alternative to this class would be to include the Vavr library which has its own version of
 * the Result class (called 'Try').  The reason for not using Vavr (https://github.com/vavr-io/vavr)
 * is that it includes much more than we currently need.  If we decide to make more extensive use
 * of FP techniques then it may be beneficial to switch to Vavr which has been available in one
 * form or another since 2014 and is distributed with the Apache 2.0 license.
 * 
 * @param <T> the type parameter for the result.  This is the type of the data that is contained
 *            in a successful result.
 */
public abstract class Result<T> {

    private Result() {}

    /// ------- Factory methods ------- ///
    
    /**
     * Transform a <code>Failure</code> to the correct type (with the right type parameter).
     * This works by re-wrapping the exception in a Failure of the right type.
     * @param <T> The type parameter for the result
     * @param <U> The type parameter for the original failure
     * @param failure the failure to transform
     * @return the transformed failure
     */
    public static <T, U> Result<T> failure(Failure<U> failure) {
    	return new Failure<>(failure.exception);
    }

    /**
     * Create a <code>Failure</code> with the given message.
     * @param <T> the type parameter for the result
     * @param message the message to be used in the encapsulated exception
     * @return a <code>Failure</code> with the given message.
     */
    public static <T> Result<T> failure(String message) {
    	return new Failure<>(message);
    }

    /**
     * Create a <code>Failure</code> with the given message and exception.
     * The exception is wrapped in an <code>IllegalStateException</code>.
     * @param <T> the type parameter for the result
     * @param message the message to be used in the encapsulated exception
     * @param e the exception to be used in the encapsulated exception
     * @return a <code>Failure</code> with the given message and exception.
     */
    public static <T> Result<T> failure(String message, Exception e) {
    	return new Failure<>(new IllegalStateException(message, e));
    }

    /**
     * Create a <code>Failure</code> with the given exception.
     * @param <V> the type parameter for the result
     * @param e the exception to be used in the <code>Failure</code>
     * @return a <code>Failure</code> with the given exception.
     */
    public static <V> Result<V> failure(Exception e) {
    	return new Failure<>(e);
    }

    /**
     * Create a <code>Success</code> with the given value.
     * @param <T> the type parameter for the result
     * @param value the value to be used in the <code>Success</code>
     * @return a <code>Success</code> with the given value.
     */
    public static <T> Result<T> success(T value) {
    	return new Success<>(value);
    }

    /**
     * Create an <code>Empty</code> <code>Result</code>.
     * @param <T> the type parameter for the result
     * @return an <code>Empty</code> <code>Result</code>.
     */
    public static <T> Result<T> empty() {
    	return new Empty<>();
    }

    /**
     * Create a <code>Result</code> from a <code>Callable</code>.
     * This calls {@link #of(Callable, String)} with a message of "Null value".
     * @param <T> the type parameter for the result
     * @param callable the <code>Callable</code> to call
     * @return a <code>Result</code> from calling the <code>Callable</code>.
     */
    public static <T> Result<T> of(final Callable<T> callable) {
      return of(callable, "Null value");
    }

    /**
     * Create a <code>Result</code> from a <code>Callable</code>.
     * If the <code>Callable</code> throws an <code>Exception</code>, an
     * <code>Failure</code> with the <code>Exception</code> is returned.
     * If the <code>Callable</code> returns <code>null</code>, an
     * <code>Result</code> with the provided <code>message</code> is returned.
     * @param <T> the type parameter for the result
     * @param callable the <code>Callable</code> to call
     * @param message the message to use if the <code>Callable</code> returns <code>null</code>
     * @return a <code>Result</code> from calling the <code>Callable</code>.
     */
    public static <T> Result<T> of(final Callable<T> callable,
                                   final String message) {
      try {
        T value = callable.call();
        return value == null
            ? Result.failure(message)
            : Result.success(value);
      } catch (Exception e) {
        return Result.failure(e.getMessage(), e);
      }
    }

    /**
     * Create a <code>Result</code> from the given <code>predicate</code>, passing it
     * the <code>value</code>.
     * If the <code>predicate</code> throws an <code>Exception</code>, an
     * <code>Failure</code> with the <code>Exception</code> is returned.
     * If the <code>predicate</code> returns <code>false</code>, an
     * <code>Failure</code> with the provided <code>message</code> is returned.
     * Otherwise, an <code>Success</code> with the <code>value</code> is returned.
     * @param <T> the type parameter for the result
     * @param predicate the <code>predicate</code> to call.  This is a 
     *                 <code>Function</code> that takes the <code>value</code> and returns
     *                 <code>true</code> or <code>false</code>.
     * @param value the value to pass to the <code>predicate</code>
     * @param message the message to use if the <code>predicate</code> returns <code>false</code>
     * @return a <code>Result</code> from calling the <code>predicate</code>.
     */
    @SuppressWarnings("boxing")
	public static <T> Result<T> of(final Function<T, Boolean> predicate,
                                   final T value,
                                   final String message) {
      try {
        return predicate.apply(value)
            ? Result.success(value)
            : Result.failure(String.format(message, value));
      } catch (Exception e) {
        String errMessage = String.format("Exception while evaluating predicate: %s", String.format(message, value));
        return Result.failure(errMessage, e);
      }
    }

    /**
     * Create a <code>Result</code> from the given <code>value</code>.
     * If the <code>value</code> is <code>null</code>, an 
     * <code>Failure</code> with the message <code>"Null value"</code> is returned.
     * Otherwise, an <code>Success</code> with the <code>value</code> is returned.
     * @param <T>
     * @param value
     * @return
     */
    public static <T> Result<T> of(final T value) {
      return value != null
          ? success(value)
          : Result.failure("Null value");
    }

    /**
     * Create a <code>Result</code> from the given <code>value</code>.
     * If the <code>value</code> is <code>null</code>, an
     * <code>Failure</code> with the <code>message</code> is returned.
     * Otherwise, an <code>Success</code> with the <code>value</code> is returned.
     * @param <T> the type parameter for the result
     * @param value the value to use if the <code>value</code> is not <code>null</code>
     * @param message the message to use if the <code>value</code> is <code>null</code>
     * @return a <code>Result</code> from comparing the <code>value</code> to <code>null</code>.
     */
    public static <T> Result<T> of(final T value, final String message) {
      return value != null
          ? Result.success(value)
          : Result.failure(message);
    }

    /// ------- Transformation Methods ------- ///
    /**
     * Transfor a <code>Result&ltT&gt;</code> into a <code>Result&ltU&gt;</code>.
     * This is accomplished by applying the function <code>f</code> to the value
     * of the <code>Result</code> and returning the result.  This is what happens
     * if the original <code>Result</code> is a <code>Success</code>.
     * If the original <code>Result</code> is a <code>Failure</code>, then the 
     * <code>Result</code> is returned as is (possibly tranforming the type parameter
     * of the result).
     * @param <U> the type parameter for the result
     * @param f the function to apply
     * @return a <code>Result&ltU&gt;</code>
     */
    public abstract <U> Result<U> map(Function<T, U> f);
    
    /**
     * If <code>this</code> is a <code>Failure</code>, return a new <code>Failure</code>
     * by calling {@link #failure(String, Exception)} with the <code>String</code> and
     * <code>Exception</code> provided.  Otherwise, return <code>this</code>.
     * <p>
     * This allows us to transform <code>Failure</code>s as necessary at a later time.
     * @param s the <code>String</code> to use if <code>this</code> is a <code>Failure</code>
     * @param e the <code>Exception</code> to use if <code>this</code> is a <code>Failure</code>
     * @return a <code>Result</code>
     */
    public abstract Result<T> mapFailure(String s, Exception e);

    /**
     * If <code>this</code> is a <code>Failure</code>, return a new <code>Failure</code>
     * by calling {@link #failure(String)} with the <code>String</code> provided.  
     * Otherwise, return <code>this</code>.
     * <p>
     * This allows us to transform <code>Failure</code>s as necessary at a later time.
     * @param s the <code>String</code> to use if <code>this</code> is a <code>Failure</code>
     * @return a <code>Result</code>
     */
    public abstract Result<T> mapFailure(String s);
    
    /**
     * If <code>this</code> is a <code>Failure</code>, return a new <code>Failure</code>
     * by calling {@link #failure(Exception)} with the <code>Exception</code> provided.  
     * Otherwise, return <code>this</code>.
     * <p>
     * This allows us to transform <code>Failure</code>s as necessary at a later time.
     * @param e the <code>Exception</code> to use if <code>this</code> is a <code>Failure</code>
     * @return a <code>Result</code>
     */
    public abstract Result<T> mapFailure(Exception e);

    /**
     * If <code>this</code> is a <code>Failure</code>, return the <code>Result</code> provided.  
     * Otherwise, return <code>this</code>.
     * <p>
     * This allows us to transform <code>Failure</code>s as necessary at a later time.
     * @param v the <code>Failure</code> to use if <code>this</code> is a <code>Failure</code>
     * @return a <code>Result</code>
     */
    public abstract Result<T> mapFailure(Result<T> v);
    
    /**
     * Similar to <code>map</code> except that the function <code>f</code> returns
     * a <code>Result&ltU&gt;</code>.  Instead of wrapping the returned value in a
     * new <code>Result</code>, the new <code>Result</code> is returned, as is.
     * @param <U> the type parameter for the returned object.
     * @param f the function to apply
     * @return a <code>Result&ltU&gt;</code>
     */
    public abstract <U> Result<U> flatMap(Function<T, Result<U>> f);

    /**
     * Take a <code>Result&ltResult&ltT&gt&gt</code> and flatten it.  This returns
     * a <code>Result&ltT&gt</code> object.
     * @param <T> the type parameter for the returned object
     * @param result the <code>Result&ltResult&ltT&gt&gt</code> to flatten.
     * @return a <code>Result&ltT&gt</code> object.
     */
    public static <T> Result<T> flatten(Result<Result<T>> result) {
        return result.flatMap(x -> x);
    }

    public abstract <V> V foldLeft(final V identity, Function<V, Function<T, V>> f);
    public abstract <V> V foldRight(final V identity, Function<T, Function<V, V>> f);

    // ------- Query Methods ------- ///
    
    /**
     * Return <code>true</code> if <code>this</code> is a <code>Success</code>.
     * Otherwise, return <code>false</code>.
     * @return a <code>Boolean</code>
     */
    public abstract boolean isSuccess();
    
    /**
     * Return <code>true</code> if <code>this</code> is a <code>Failure</code>.
     * Otherwise, return <code>false</code>.
     * @return  a <code>Boolean</code>
     */
    public abstract boolean isFailure();
    
    /**
     * Return <code>true</code> if <code>this</code> is an <code>Empty</code>.
     * Otherwise, return <code>false</code>.
     * @return a <code>Boolean</code>
     */
    public abstract boolean isEmpty();
    
    /**
     * If <code>this</code> is a <code>Success</code>, then apply the function <code>f</code>
     * to the contained value and return the result.  Otherwise, return <code>false</code>.
     * @param f the function to apply (a predicate).
     * @return a <code>Boolean</code> result from applying <code>f</code> to the contained value.
     */
    public abstract Boolean exists(Function<T, Boolean> f);

    /// ------- Value Access Methods ------- ///
    
    /**
     * Return the contained value if <code>this</code> is a <code>Success</code>.
     * <p>
     * Caution: This will throw an IllegalStateException if <code>this</code> is a 
     * not a <code>Success</code>.
     * @return
     */
    public abstract T successValue();

    /**
     * Return the contained exception if <code>this</code> is a <code>Failure</code>.
     * <p>
     * Caution: This will throw an IllegalStateException if <code>this</code> is a 
     * not a <code>failure</code>.
     * @return
     */
    public abstract Exception failureValue();

    /**
     * Get the contained value if <code>this</code> is a <code>Success</code>.  If this is
     * not a <code>Success</code>, then return the provided default value.
     * @param defaultValue the default value to return if this is not a <code>Success</code>.
     * @return the contained value or the default value.
     */
    public abstract T getOrElse(final T defaultValue);
    
    /**
     * Get the contained value if <code>this</code> is a <code>Success</code>.  If this is
     * not a <code>Success</code>, then return the result of calling the provided function.
     * @param defaultValue the function to call if this is not a <code>Success</code>.
     * @return the contained value or the result of calling the provided function.
     */
    public abstract T getOrElse(final Supplier<T> defaultValue);

    /**
     * Similar to {@link #getOrElse(Supplier)} except that the <code>Supplier</code> function
     * returns a <code>Result&ltT&gt;</code>.  Using this method is similar to using
     * (@link flatMap(Function)).
     * @param defaultValue the function to call if this is not a <code>Success</code>.
     * @return a <code>Result&ltT&gt;</code>
     */
    public Result<T> orElse(Supplier<Result<T>> defaultValue) {
        return map(x -> this).getOrElse(defaultValue);
    }
    
    /// ------- Functional Methods ------- ///
    
    /**
     * If <code>this</code> is a <code>Success</code>, apply the provided <code>Effect</code> to
     * the contained value.  If this is a <code>Failure</code> or <code>Empty</code>, do nothing.
     * @param c the <code>Effect</code> to apply.
     */
    public abstract void forEach(Effect<T> c);
    
    /**
     * If <code>this</code> is a <code>Success</code>, apply the provided <code>Effect</code> to
     * the contained value.  If this is a <code>Failure</code>, throw the contained exception.
     * @param c the <code>Effect</code> to apply.
     */
    public abstract void forEachOrThrow(Effect<T> c);

    /**
     * If <code>this</code> is a <code>Success</code>, apply the provided <code>Effect</code> 
     * to the contained value and return <code>empty</code>.   
     * If this is a <code>Failure</code>, return the message from the contained exception,
     * wrapped in a <code>Success</code>.
     * Otherwise, return <code>empty</code>.
     * <p>
     * The advantage here is that this allows you to transform a <code>Failure</code> into 
     * a <code>Success</code> with the message in the <code>Failure</code>, which can
     * be further processed or output to the console.
     * <p>
     * For example:
     * <pre>{@code
     * Result<Integer> res = someOperationThatCanFail();
     * res.forEachOrFail(i -> {}) // no-op Effect.  
     *                            // Just transforms a Failure into a Success
     *    .forEach(s -> System.out.println("Error doing some operation: " + s)); 
     * }</pre>
     * <p>
     * In the above code, there will be no printout for any <code>Success</code> results.
     * @param e the effect to apply
     * @return an empty <code>Result</code> unless <code>this</code> is a <code>Failure</code>.
     */
    public abstract Result<String> forEachOrFail(Effect<T> e);
    
    /**
     * If <code>this</code> is a <code>Success</code>, apply the provided <code>Effect</code>
     * to the contained value and return <code>empty</code>.  If this is a <code>Failure</code>,
     * return the exception from the contained exception, wrapped in a <code>Success</code>.
     * <p>
     * This can be used in a similar fashion to {@link #forEachOrFail(Effect)}.
     * @param e the effect to apply
     * @return an empty <code>Result</code> unless <code>this</code> is a <code>Failure</code>.
     */
    public abstract Result<RuntimeException> forEachOrException(Effect<T> e);
    
    /**
     * If <code>this</code> is a <code>Success</code>, apply the provided <code>Function</code>
     * to the contained value. If the funtion returns <code>true</code>, return <code>this</code>.
     * If it returns <code>false</code>, return a <code>Failure</code> with a message indicating
     * the failure..
     * <p>
     * If <code>this</code> is a <code>Failure</code>, return <code>this</code>.
     * <p>
     * If <code>this</code> is an <code>Empty</code>, return <code>this</code>.
     * 
     * @param f the function to apply
     * @return this if the function returns <code>true</code>, else a <code>Failure</code>
     */
    public abstract Result<T> filter(Function<T, Boolean> f);
    
    /**
     * If <code>this</code> is a <code>Success</code>, apply the provided <code>Function</code>
     * to the contained value. If the funtion returns <code>true</code>, return <code>this</code>.
     * If it returns <code>false</code>, return a <code>Failure</code> with the message provided.
     * <p>
     * If <code>this</code> is a <code>Failure</code>, return <code>this</code>.
     * <p>
     * If <code>this</code> is an <code>Empty</code>, return <code>this</code>.
     * 
     * @param p the function to apply
     * @param message the message to return if the function returns <code>false</code>
     * @return this if the function returns <code>true</code>, else a <code>Failure</code>
     */
    public abstract Result<T> filter(Function<T, Boolean> p, String message);

    /**
     * Lift a function into the <code>Result</code> monad.  That is, take a 
     * <code>Function&ltA, B&gt;</code> and return a 
     * <code>Function&ltResult&ltA&gt, Result&ltB&gt&gt;</code>.
     * @param <A> the type parameter for the argument to the function.
     * @param <B> the type parameter for the result of the function.
     * @param f the function to lift.
     * @return a <code>Function&ltResult&ltA&gt, Result&ltB&gt&gt;</code>
     */
    public static <A, B> Function<Result<A>, Result<B>> lift(final Function<A, B> f) {
      return x -> x.map(f);
    }

    /**
     * Like {@link #lift(Function)} except that the function <code>f</code> takes two
     * arguments. 
     * @param <A> the type parameter for the first argument to the function.
     * @param <B> the type parameter for the second argument to the function.
     * @param <C> the type parameter for the result of the function.
     * @param f the function to lift.
     * @return a <code>Function&ltResult&ltA&gt, Function&ltResult&ltB&gt, Result&ltC&gt&gt&gt&gt;</code>
     */
    public static <A, B, C> Function<Result<A>, Function<Result<B>, Result<C>>> lift2(final Function<A, Function<B, C>> f) {
      return a -> b -> a.map(f).flatMap(b::map);
    }

    /**
     * Like {@link #lift(Function)} except that the function <code>f</code> takes three
     * arguments.
     * @param <A> the type parameter for the first argument
     * @param <B> the type parameter for the second argument
     * @param <C> the type parameter for the third argument
     * @param <D> the type parameter for the result
     * @param f the function to lift
     * @return a <code>Function&ltResult&ltA&gt, Function&ltResult&ltB&gt, Function&ltResult&ltC&gt, Result&ltD&gt&gt&gt&gt&gt&gt;</code>
     */
    public static <A, B, C, D> Function<Result<A>, Function<Result<B>, Function<Result<C>, Result<D>>>> lift3(final Function<A, Function<B, Function<C, D>>> f) {
      return a -> b -> c -> a.map(f).flatMap(b::map).flatMap(c::map);
    }

    /**
     * Similar to {@link #map(Function)} except that the function <code>f</code> takes two
     * arguments.  The arguments for the function are wrapped in <code>Result</code> objects.
     * If either of the arguments are a <code>Failure</code>, then the <code>Result</code>
     * that is returnd is also a <code>Failure</code>.
     * <p>
     * This is the same as {@link #map2(Result, Result, Function)} except that the 
     * implementation is more explicit and doesn't use <code>lift2</code>.  In most cases
     * {@link #map2(Result, Result, Function)} should be used instead.
     * @param <A> the type parameter for the first argument
     * @param <B> the type parameter for the second argument
     * @param <C> the type parameter for the result of applying <code>f</code>.
     * @param a the first argument (wrapped in a <code>Result</code>)
     * @param b the second argument (wrapped in a <code>Result</code>)
     * @param f the function to apply
     * @return a <code>Result&ltC&gt;</code> object.
     */
    @SuppressWarnings("unchecked")
    public static <A, B, C> Result<C> map2_(final Result<A> a,
                                           final Result<B> b,
                                           final Function<A, Function<B, C>> f) {
      return a.isSuccess()
          ? b.isSuccess()
              ? Result.of(() -> f.apply(a.successValue()).apply(b.successValue()))
              : Result.failure((Failure<C>) b)
          : b.isSuccess()
              ? Result.failure((Failure<C>) a)
              : Result.failure(String.format("%s, %s", a.failureValue(), b.failureValue()));
    }

    /**
     * Similar to {@link #map(Function)} except that the function <code>f</code> takes two
     * arguments.  The arguments for the function are wrapped in <code>Result</code> objects.
     * If either of the arguments are a <code>Failure</code>, then the <code>Result</code>
     * that is returnd is also a <code>Failure</code>.
     * @param <A> the type parameter for the first argument
     * @param <B> the type parameter for the second argument
     * @param <C> the type parameter for the result of applying <code>f</code>.
     * @param a the first argument (wrapped in a <code>Result</code>)
     * @param b the second argument (wrapped in a <code>Result</code>)
     * @param f the function to apply
     * @return a <code>Result&ltC&gt;</code> object.
     */
    public static <A, B, C> Result<C> map2(final Result<A> a,
                                           final Result<B> b,
                                           final Function<A, Function<B, C>> f) {
    	return lift2(f).apply(a).apply(b);
    }

    /// ------- Implementation Classes ------- ///
   
    private static class Failure<T> extends Empty<T> {

      private final RuntimeException exception;

      private Failure(String message) {
        super();
        this.exception = new IllegalStateException(message);
      }

      private Failure(RuntimeException e) {
        super();
        this.exception = e;
      }

      private Failure(Exception e) {
        super();
        this.exception = new IllegalStateException(e);
      }

      @Override
      public boolean isSuccess() {
        return false;
      }

      @Override
      public boolean isFailure() {
        return true;
      }

      @Override
      public T getOrElse(final T defaultValue) {
        return defaultValue;
      }

      @Override
      public T successValue() {
        throw new IllegalStateException("Method successValue() called on a Failure instance");
      }

      @Override
      public RuntimeException failureValue() {
        return this.exception;
      }

      @Override
      public void forEach(Effect<T> c) {
        /* Empty. Do nothing. */
      }

      @Override
      public void forEachOrThrow(Effect<T> c) {
        throw exception;
      }

      @Override
      public Result<RuntimeException> forEachOrException(Effect<T> c) {
        return success(exception);
      }

      @Override
      public Result<String> forEachOrFail(Effect<T> c) {
        return success(exception.getLocalizedMessage());
      }

      @Override
      public Result<T> filter(Function<T, Boolean> f) {
        return failure(this);
      }

      @Override
      public Result<T> filter(Function<T, Boolean> p, String message) {
        return failure(this);
      }

      @Override
      public <U> Result<U> map(Function<T, U> f) {
        return failure(this);
      }

      @Override
      public Result<T> mapFailure(String s, Exception e) {
        return failure(s, e);
      }

      @Override
      public Result<T> mapFailure(String s) {
        return failure(s, exception);
      }

      @Override
      public Result<T> mapFailure(Exception e) {
        return failure(e.getMessage(), e);
      }

      @Override
      public Result<T> mapFailure(Result<T> v) {
        return v;
      }

      @Override
      public <U> Result<U> flatMap(Function<T, Result<U>> f) {
        return failure(exception.getMessage(), exception);
      }

      @Override
      public String toString() {
        return String.format("Failure(%s)", failureValue());
      }

      @SuppressWarnings("boxing")
	@Override
      public Boolean exists(Function<T, Boolean> f) {
        return false;
      }

      @Override
      public T getOrElse(Supplier<T> defaultValue) {
        return defaultValue.get();
      }
    }

    private static class Empty<T> extends Result<T> {

      public Empty() {
        super();
      }

      @Override
      public boolean isSuccess() {
        return false;
      }

      @Override
      public boolean isFailure() {
        return false;
      }

      @Override
      public boolean isEmpty() {
        return true;
      }

      @Override
      public T getOrElse(final T defaultValue) {
        return defaultValue;
      }

      @Override
      public T successValue() {
        throw new IllegalStateException("Method successValue() called on a Empty instance");
      }

      @Override
      public RuntimeException failureValue() {
        throw new IllegalStateException("Method failureMessage() called on a Empty instance");
      }

      @Override
      public void forEach(Effect<T> c) {
        /* Empty. Do nothing. */
      }

      @Override
      public void forEachOrThrow(Effect<T> c) {
        /* Do nothing */
      }

      @Override
      public Result<String> forEachOrFail(Effect<T> c) {
        return empty();
      }

      @Override
      public Result<RuntimeException> forEachOrException(Effect<T> c) {
        return empty();
      }

      @Override
      public Result<T> filter(Function<T, Boolean> f) {
        return empty();
      }

      @Override
      public Result<T> filter(Function<T, Boolean> p, String message) {
        return empty();
      }

      @Override
      public <U> Result<U> map(Function<T, U> f) {
        return empty();
      }

      @Override
      public Result<T> mapFailure(String s, Exception e) {
        return failure(s, e);
      }

      @Override
      public Result<T> mapFailure(String s) {
        return failure(s);
      }

      @Override
      public Result<T> mapFailure(Exception e) {
        return failure(e.getMessage(), e);
      }

      @Override
      public Result<T> mapFailure(Result<T> v) {
        return v;
      }

      @Override
      public <U> Result<U> flatMap(Function<T, Result<U>> f) {
        return empty();
      }

      @Override
      public String toString() {
        return "Empty()";
      }

      @SuppressWarnings("boxing")
	  @Override
      public Boolean exists(Function<T, Boolean> f) {
        return false;
      }

      @Override
      public T getOrElse(Supplier<T> defaultValue) {
        return defaultValue.get();
      }

      @Override
      public <V> V foldLeft(V identity, Function<V, Function<T, V>> f) {
        return identity;
      }

      @Override
      public <V> V foldRight(V identity, Function<T, Function<V, V>> f) {
        return identity;
      }
    }

    private static class Success<T> extends Result<T> {

      private final T value;

      public Success(T value) {
        super();
        this.value = value;
      }

      @Override
      public boolean isSuccess() {
        return true;
      }

      @Override
      public boolean isFailure() {
        return false;
      }

      @Override
      public boolean isEmpty() {
        return false;
      }

      @Override
      public T getOrElse(final T defaultValue) {
        return successValue();
      }

      @Override
      public T successValue() {
        return this.value;
      }

      @Override
      public RuntimeException failureValue() {
        throw new IllegalStateException("Method failureValue() called on a Success instance");
      }

      @Override
      public void forEach(Effect<T> e) {
        e.apply(this.value);
      }

      @Override
      public void forEachOrThrow(Effect<T> e) {
        e.apply(this.value);
      }

      @Override
      public Result<String> forEachOrFail(Effect<T> e) {
        e.apply(this.value);
        return empty();
      }

      @Override
      public Result<RuntimeException> forEachOrException(Effect<T> e) {
        e.apply(this.value);
        return empty();
      }

      @Override
      public Result<T> filter(Function<T, Boolean> p) {
        return filter(p, "Unmatched predicate with no error message provided.");
      }

      @SuppressWarnings("boxing")
	  @Override
      public Result<T> filter(Function<T, Boolean> p, String message) {
        try {
          return p.apply(successValue())
              ? this
              : failure(message);
        } catch (Exception e) {
          return failure(e.getMessage(), e);
        }
      }

      @Override
      public <U> Result<U> map(Function<T, U> f) {
        try {
          return success(f.apply(successValue()));
        } catch (Exception e) {
          return failure(e.getMessage(), e);
        }
      }

      @Override
      public Result<T> mapFailure(String f, Exception e) {
        return this;
      }

      @Override
      public Result<T> mapFailure(String s) {
        return this;
      }

      @Override
      public Result<T> mapFailure(Exception e) {
        return this;
      }

      @Override
      public Result<T> mapFailure(Result<T> v) {
        return this;
      }

      @Override
      public <U> Result<U> flatMap(Function<T, Result<U>> f) {
        try {
          return f.apply(successValue());
        } catch (Exception e) {
          return failure(e.getMessage());
        }
      }

      @Override
      public String toString() {
        return String.format("Success(%s)", successValue().toString());
      }

      @Override
      public Boolean exists(Function<T, Boolean> f) {
        return f.apply(successValue());
      }

      @Override
      public T getOrElse(Supplier<T> defaultValue) {
        return successValue();
      }

      @Override
      public <V> V foldLeft(V identity, Function<V, Function<T, V>> f) {
        return f.apply(identity).apply(successValue());
      }

      @Override
      public <V> V foldRight(V identity, Function<T, Function<V, V>> f) {
        return f.apply(successValue()).apply(identity);
      }

    }
}
