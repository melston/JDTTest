package com.jdttst.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class StreamUtils {
	
	///////////////////////////////////////////////////////////////////////////////
	// zip functions
	///////////////////////////////////////////////////////////////////////////////

	/**
	 * Basic support for zipping two <code>Stream</code>s.
	 * <p>
	 * Zipping means taking elements from each stream, in turn, and combining them
	 * using a provided function and returning the resulting value into the resulting
	 * <code>Stream</code>.
	 * <p>
	 * This combining operation continues until one <code>Stream</code> or the other
	 * has no more elements.
	 * 
	 * @param <ARG1> the type of the first <code>Stream</code>.
	 * @param <ARG2> the type of the second <code>Stream</code>.
	 * @param <RESULT> the type of the resulting <code>Stream</code>.
	 * @param s1 the first <code>Stream</code> 
	 * @param s2 the second <code>Stream</code>
	 * @param combiner a <code>BiFunction&lt;ARG1, ARG2, RESULT&gt;</code> that takes an element from
	 *      the first <code>Stream</code> and an element from the second <code>Stream</code> and 
	 *      combines them into a <code>RESULT</code> type.
	 * @return a <code>Stream</code> of <code>RESULT</code> types.
	 */
	public static <ARG1, ARG2, RESULT> Stream<RESULT> zip(
            Stream<ARG1> s1,
            Stream<ARG2> s2,
            BiFunction<ARG1, ARG2, RESULT> combiner) {
        final var i2 = s2.iterator();
        return s1.map(x1 -> i2.hasNext() ? combiner.apply(x1, i2.next()) : null)
                .takeWhile(Objects::nonNull);
    }

	/**
	 * This is a convenience function for zipping two lists.  It is implemented in terms of
	 * {@link StreamUtils#zip(Stream, Stream, BiFunction)}.
	 * 
	 * @param <ARG1> the type of the first <code>List</code>.
	 * @param <ARG2> the type of the second <code>List</code>.
	 * @param <RESULT> the type of the resulting <code>Stream</code>.
	 * @param s1 the first <code>List</code> 
	 * @param s2 the second <code>List</code>
	 * @param combiner a <code>BiFunction&lt;ARG1, ARG2, RESULT&gt;</code> that takes an element from
	 *      the first <code>List</code> and an element from the second <code>List</code> and 
	 *      combines them into a <code>RESULT</code> type.
	 * @return a <code>Stream</code> of <code>RESULT</code> types.
	 */
    public static <ARG1, ARG2, RESULT> Stream<RESULT> zip(
            List<ARG1> s1,
            List<ARG2> s2,
            BiFunction<ARG1, ARG2, RESULT> combiner) {
        final var i2 = s2.iterator();
        return s1.stream().map(x1 -> i2.hasNext() ? combiner.apply(x1, i2.next()) : null)
                .takeWhile(Objects::nonNull);
    }

	/**
	 * This is a convenience function for zipping two arrays.  It is implemented in terms of
	 * {@link StreamUtils#zip(Stream, Stream, BiFunction)}.
	 * 
	 * @param <ARG1> the type of the first <code>array</code>.
	 * @param <ARG2> the type of the second <code>array</code>.
	 * @param <RESULT> the type of the resulting <code>Stream</code>.
	 * @param s1 the first <code>array</code> 
	 * @param s2 the second <code>array</code>
	 * @param combiner a <code>BiFunction&lt;ARG1, ARG2, RESULT&gt;</code> that takes an element from
	 *      the first <code>array</code> and an element from the second <code>array</code> and 
	 *      combines them into a <code>RESULT</code> type.
	 * @return a <code>Stream</code> of <code>RESULT</code> types.
	 */
    public static <ARG1, ARG2, RESULT> Stream<RESULT> zip(
            ARG1[] s1,
            ARG2[] s2,
            BiFunction<ARG1, ARG2, RESULT> combiner) {
    	return zip(Arrays.stream(s1), Arrays.stream(s2), combiner);
    }
    
    /**
     * Zip a <code>Stream</code> with the indices of the elements of the <code>Stream</code>.
     * @param <ARG1> The type of the input <code>Stream</code>
     * @param <RESULT> the type of the resulting <code>Stream</code>
     * @param startingIndex the starting index to use
     * @param s1 the input <code>Stream</code>
	 * @param combiner a <code>BiFunction&lt;Integer, ARG1, RESULT&gt;</code> that takes an index 
	 *      element and an element from the input <code>Stream</code> and 
	 *      combines them into a <code>RESULT</code> type.
     * @return  a <code>Stream</code> of <code>RESULT</code> types.
     */
    public static<ARG1, RESULT> Stream<RESULT> zipWithIndex(
    		Integer startingIndex,
    		Stream<ARG1> s1,
    		BiFunction<Integer, ARG1, RESULT> combiner) {
    	Stream<Integer> ints = Stream.iterate(startingIndex, n -> n + 1);
    	return zip(ints, s1, combiner);
    }
    
    /**
     * Zip a <code>Stream</code> with the indices of the elements of the <code>Stream</code>.
     * Use a default value of 0 for the starting index.
     * @param <ARG1> The type of the input <code>Stream</code>
     * @param <RESULT> the type of the resulting <code>Stream</code>
     * @param s1 the input <code>Stream</code>
	 * @param combiner a <code>BiFunction&lt;Integer, ARG1, RESULT&gt;</code> that takes an index 
	 *      element and an element from the input <code>Stream</code> and 
	 *      combines them into a <code>RESULT</code> type.
     * @return  a <code>Stream</code> of <code>RESULT</code> types.
     */
    public static<ARG1, RESULT> Stream<RESULT> zipWithIndex(
    		Stream<ARG1> s1,
    		BiFunction<Integer, ARG1, RESULT> combiner) {
    	return zipWithIndex(0, s1, combiner);
    }
}
