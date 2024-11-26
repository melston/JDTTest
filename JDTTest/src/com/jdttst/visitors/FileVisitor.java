package com.jdttst.visitors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import com.jdttst.types.TypeDB;
import com.jdttst.utils.DirWalker;
import com.jdttst.utils.ParserOps;
import com.jdttst.utils.Result;

public class FileVisitor {

	private static final int AST_JLS_LATEST = AST.getJLSLatest();
	private static boolean resolveBindings = true;
	private static boolean statementsRecovery = true;
	private static boolean bindingsRecovery = true;
    
    public static Stream<String> processDirectoryTree(Path root) {
    	ArrayList<String> msgs = new ArrayList<>();
		Result<Path> res = DirWalker.walkDirs(root,
				DirWalker.javaOnly,
				doProcessFile.apply(root),
				DirWalker.noPostAction);
		res.forEachOrFail(p -> {/* nothing to do */})
			.forEach(s -> msgs.add("Error: " + s));
		return msgs.stream();
	}
    
    static Function<Path, Function<Path, List<IProblem>>> doProcessFile = 
		(Path root) -> (Path p) -> {
			Path relativePath = root.relativize(p);
	    	String strPath = relativePath.toString();
	    	ASTNode n = ParserOps.parseFile(p.toString(), AST_JLS_LATEST, 
	    			List.of(root.toString()),
	    			resolveBindings, statementsRecovery, bindingsRecovery, strPath);

			return processCompilationUnit(n);
		};

    @SuppressWarnings("unchecked")
	private static List<IProblem> processCompilationUnit(ASTNode node) {
		CompilationUnit unit = (CompilationUnit) node;
		List<ImportDeclaration> imports = unit.imports();
		List<AbstractTypeDeclaration> types = unit.types();
		types.stream()
			.forEach(t -> TypeDB.addType(t, imports));
		return Arrays.asList(unit.getProblems());
    }
}
