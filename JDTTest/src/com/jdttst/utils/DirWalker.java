package com.jdttst.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.jdt.core.compiler.IProblem;

public class DirWalker extends SimpleFileVisitor<Path> {

	///////////////////// Filters ////////////////////////////////////
	@SuppressWarnings("boxing")
	public static Function<Path, Boolean> defaultFilter = path -> true;
	@SuppressWarnings("boxing")
	public static Function<Path, Boolean> javaOnly = path -> {
		if (path.toString().endsWith(".java")) { return true; }
		return false;
	};
	
	////////////////////// Convenience Actions ///////////////////////////////////
	/**
	 * Don't perform any post-visit action 
	 */
	public static BiFunction<Path, List<IProblem>, Exception> noPostAction = (p, s) -> null;
	
	/**
	 * Print the contents of the provided <code>String</code> (presumably the contents
	 * of the file after processing) to System.err after a visit.
	 */
	public static BiFunction<Path, List<IProblem>, Exception> printProblemsAction = (p, s) -> {
		System.err.println("-------------------------");
		System.err.println(p.toString());
		s.stream().forEach(prob -> System.err.println("Error: " + prob.getMessage()));
		return null;
	};
	
	/////////////////////////// Static entry points //////////////////////
	/**
	 * Walk a directory tree with the given root and perform the specified action
	 * on each file found.
	 * @param startingPath the root of the tree to process
	 * @param fileVisitAction the action to perform on each file.  This has the type
	 *    <code>Function&gt;Path, String&lt;.  It is passed the path of the file to visit
	 *    and returns a string with the results of processing the file.
	 * @return a <code>Result&gt;Path&lt;</code> with the starting path in the successful
	 *    case and an <code>Exception</code> specifying the nature of any failure otherwise.
	 */
	public static Result<Path> walkDirs(Path startingPath, 
			Function<Path, List<IProblem>> fileVisitAction) {
		return walkDirs(startingPath, defaultFilter, fileVisitAction, printProblemsAction);
	}

	/**
	 * Walk a directory tree with the given root and perform the specified actions
	 * on each file found.
	 * @param startingPath the root of the tree to process
	 * @param visitFilter a <code>Function&gt;Path, Boolean&lt;</code> specifying whether
	 *     to visit a file with the given path.
	 * @param fileVisitAction the action to perform on each file.  This has the type
	 *    <code>Function&gt;Path, String&lt;.  It is passed the path of the file to visit
	 *    and returns a string with the results of processing the file.
	 * @param postVisitAction the action to take after each file is visited.  This has the type
	 *    <code>BiFunction&gt;Path, String, Exception&lt;.  It is passed the path of the file 
	 *    visited and the resulting string and returns an <code>Exception</code> or null if
	 *    no exception occurred.
	 * @return a <code>Result&gt;Path&lt;</code> with the starting path in the successful
	 *    case and an <code>Exception</code> specifying the nature of any failure otherwise.
	 * @implNote there are two filters provided for convenience: {@link #defaultFilter} and
	 *    {@link #javaOnly}. There are also two <code>postVisitAction</code> functions provided
	 *    for convenience {@link #noPostAction} and {@link #printFileAction}.  
	 */
	public static Result<Path> walkDirs(Path startingPath, 
			Function<Path, Boolean> visitFilter,
			Function<Path, List<IProblem>> fileVisitAction,
			BiFunction<Path, List<IProblem>, Exception> postVisitAction) {
		DirWalker walker = new DirWalker(visitFilter, fileVisitAction, postVisitAction);
		try {
			Path p = Files.walkFileTree(startingPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 
								Integer.MAX_VALUE, walker);
			return Result.success(p);
		} catch (IOException e) {
			return Result.failure(e);
		}
	}

	///////////////////////   Constructor  /////////////////////////////
	/**
	 * Construct a <code>DirWalker</code> with the actions to take during and after
	 * visiting a file, plus a filter (if necessary) to specify the files to visit and,
	 * just as importantly, not visit.
	 * @param visitFilter a <code>Function&gt;Path, Boolean&lt;</code> specifying whether
	 *     to visit a file with the given path.
	 * @param fileVisitAction the action to take for each file visited.  This has the type
	 *    <code>Function&gt;Path, String&lt;.  It is passed the path of the file to visit
	 *    and returns a string with the results of processing the file.
	 * @param postVisitAction the action to take after each file is visited.  This has the type
	 *    <code>BiFunction&gt;Path, String, Exception&lt;.  It is passed the path of the file 
	 *    visited and the resulting string and returns an <code>Exception</code> or null if
	 *    no exception occurred.
	 * @implNote there are two filters provided for convenience: {@link #defaultFilter} and
	 *    {@link #javaOnly}. There are also two <code>postVisitAction</code> functions provided
	 *    for convenience {@link #noPostAction} and {@link #printFileAction}.  
	 */
	public DirWalker(Function<Path, Boolean> visitFilter,
			         Function<Path, List<IProblem>> fileVisitAction, 
                     BiFunction<Path, List<IProblem>, Exception> postVisitAction) {
		this.fileVisitAction = fileVisitAction;
		this.visitFilter = visitFilter;
        this.postVisitAction = postVisitAction;
	}
	
	
	//////////////// SimpleFileVisitor<Path> Overrides //////////////////////////////////

	// Apply the provided function to each java file we visit.
    @SuppressWarnings("boxing")
	@Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr) {
    	if (!visitFilter.apply(file)) {
    		return FileVisitResult.CONTINUE;
    	}
    	List<IProblem> res = fileVisitAction.apply(file);
    	Exception e = postVisitAction.apply(file, res);
    	if (e != null) {
    		System.err.println("\n*******************************************************");
    		System.err.println(e.getLocalizedMessage());
    		System.err.println("*******************************************************\n");
    	}
        return FileVisitResult.CONTINUE;
    }

    // If there is some error accessing the file, let the user know.
    // If you don't override this method and an error occurs, an IOException 
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
    	System.err.println("Error transforming file " + file.toString() + "\n" + 
                           exc.getLocalizedMessage());
        return FileVisitResult.CONTINUE;
    }

    //////////////////////////// Members /////////////////////////////////
	Function<Path, Boolean> visitFilter;
	Function<Path, List<IProblem>> fileVisitAction;
	BiFunction<Path, List<IProblem>, Exception> postVisitAction;
}
