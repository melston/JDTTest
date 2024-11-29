package com.jdttst.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ParserOps {

	public static ASTNode parseFile(
								String pathToFile,
								int astLevel,
								List<String> rootDirs,
								boolean resolveBindings,
								boolean statementsRecovery,
								boolean bindingsRecovery,
								String unitName) {
		String [] roots = rootDirs.stream().toArray(String[]::new);
		String contents = getFileContents(pathToFile);
		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(contents.toCharArray());
		parser.setEnvironment(null, // classpath entries
				roots,  // sourcePath entries
				null,   // Default encoding
				true);  // Include binding resolution
		parser.setResolveBindings(resolveBindings);
		parser.setStatementsRecovery(statementsRecovery);
		parser.setBindingsRecovery(bindingsRecovery);
		parser.setCompilerOptions(getCompilerOptions());
		parser.setUnitName(unitName);
		return parser.createAST(null);
	}
	
	static String getFileContents(String path) {
	    String source;
	    try {
	        source = readFile(path);
	        return source;
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    return "";
	}

	private static String readFile(String pathToFile) throws IOException {
	    byte[] ba = Files.readAllBytes(Paths.get(pathToFile));
	    return new String(ba);
	}

	private static Map<String, String> getCompilerOptions() {
		Map<String, String> options = new CompilerOptions().getMap();
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
		return options;
	}

}
