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
	/**
	 * A filter to use in walking directories that allows us to process all files.
	 */
	@SuppressWarnings("boxing")
	public static Function<Path, Boolean> defaultFilter = path -> true;
	
	/**
	 * A filter to use in walking directories that only allows us to process Java files.
	 */
	@SuppressWarnings("boxing")
	public static Function<Path, Boolean> javaOnly = path -> {
		if (path.toString().endsWith(".java")) { return true; }
		return false;
	};
	
	/**
	 * A filter to use in walking directories that only allows us to process Java files
	 * with the exception of those introduced by project Jigsaw (module-info.java and
	 * project-info.java).
	 */
	@SuppressWarnings("boxing")
	public static Function<Path, Boolean> javaOnlyNoInfo = path -> {
		String pathStr = path.toString();
		if (pathStr == "module-info.java" || pathStr == "package-info.java") return false;
		if (pathStr.endsWith(".java")) { return true; }
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
		if (s.size() <= 0) return null;
		System.err.println("-------------------------");
		System.err.println(p.toString());
		s.stream().forEach(prob -> System.err.println("Error: " + prob.getMessage()));
		return null;
	};
	
	/////////////////////////// Static entry points //////////////////////
	/**
	 * Walk a directory tree with the given root and perform the specified action
	 * on each file found.  This uses a default filter of {@link #javaOnlyNoInfo} to
	 * excluding any non-java files plus any project Jigsaw files.
	 * @param startingPath the root of the tree to process
	 * @param fileVisitAction the action to perform on each file.  This has the type
	 *    <code>Function&lt;Path, String&gt;</code>.  It is passed the path of the file to visit
	 *    and returns a string with the results of processing the file.
	 * @return a <code>Result&lt;Path&gt;</code> with the starting path in the successful
	 *    case and an <code>Exception</code> specifying the nature of any failure otherwise.
	 */
	public static Result<Path> walkDirs(Path startingPath, 
			Function<Path, List<IProblem>> fileVisitAction) {
		return walkDirs(startingPath, javaOnlyNoInfo, fileVisitAction, printProblemsAction);
	}

	/**
	 * Walk a directory tree with the given root and perform the specified actions
	 * on each file found.
	 * @param startingPath the root of the tree to process
	 * @param visitFilter a <code>Function&lt;Path, Boolean&gt;</code> specifying whether
	 *     to visit a file with the given path.
	 * @param fileVisitAction the action to perform on each file.  This has the type
	 *    <code>Function&lt;Path, String&gt;</code>.  It is passed the path of the file to visit
	 *    and returns a string with the results of processing the file.
	 * @param postVisitAction the action to take after each file is visited.  This has the type
	 *    <code>BiFunction&lt;Path, String, Exception&gt;</code>.  It is passed the path of the file 
	 *    visited and the resulting string and returns an <code>Exception</code> or null if
	 *    no exception occurred.
	 * @return a <code>Result&lt;Pathglt;</code> with the starting path in the successful
	 *    case and an <code>Exception</code> specifying the nature of any failure otherwise.
	 * @implNote there are two filters provided for convenience: {@link #defaultFilter} and
	 *    {@link #javaOnly}. There are also two <code>postVisitAction</code> functions provided
	 *    for convenience {@link #noPostAction} and {@link #printProblemsAction}.  
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
	 * @param visitFilter a <code>Function&lt;Path, Boolean&gt;</code> specifying whether
	 *     to visit a file with the given path.
	 * @param fileVisitAction the action to take for each file visited.  This has the type
	 *    <code>Function&lt;Path, String&gt;</code>.  It is passed the path of the file to visit
	 *    and returns a string with the results of processing the file.
	 * @param postVisitAction the action to take after each file is visited.  This has the type
	 *    <code>BiFunction&lt;Path, String, Exception&gt;</code>.  It is passed the path of the file 
	 *    visited and the resulting string and returns an <code>Exception</code> or null if
	 *    no exception occurred.
	 * @implNote there are two filters provided for convenience: {@link #defaultFilter} and
	 *    {@link #javaOnly}. There are also two <code>postVisitAction</code> functions provided
	 *    for convenience {@link #noPostAction} and {@link #printProblemsAction}.  
	 */
	public DirWalker(Function<Path, Boolean> visitFilter,
			         Function<Path, List<IProblem>> fileVisitAction, 
                     BiFunction<Path, List<IProblem>, Exception> postVisitAction) {
		this.fileVisitAction = fileVisitAction;
		this.visitFilter = visitFilter;
        this.postVisitAction = postVisitAction;
	}
	
	
	//////////////// SimpleFileVisitor<Path> Overrides //////////////////////////////////
	/**
	 * Apply the provided function to each java file we visit.
	 */
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

    /**
     * If there is some error accessing the file, let the user know.
     * <p>
     * If you don't override this method and an error occurs, an IOException is thrown.
     */
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
