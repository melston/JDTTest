package com.jdttst.sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class SampleBindingResolve {

	private static final int AST_JLS_LATEST = AST.getJLSLatest();
	
	public static ASTNode createAST(
								int astLevel,
								String source,
								boolean resolveBindings,
								boolean statementsRecovery,
								boolean bindingsRecovery,
								String unitName) {
	
		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(source.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(resolveBindings);
		parser.setStatementsRecovery(statementsRecovery);
		parser.setBindingsRecovery(bindingsRecovery);
		parser.setCompilerOptions(getCompilerOptions());
		parser.setUnitName(unitName);
		return parser.createAST(null);
	}
	
	private static Map<String, String> getCompilerOptions() {
		Map<String, String> options = new CompilerOptions().getMap();
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
		return options;
	}
	
	protected File createFile(File dir, String fileName, String contents) throws IOException {
		File file = new File(dir, fileName);
		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(contents);
		}
	
		return file;
	}
	
	protected static String getName(Object t) {
		if (t instanceof AbstractTypeDeclaration a)
			return a.getName().toString();
		return t.toString();
	}
	
	public static void main(String [] args) {
		
		boolean resolveBindings = true;
		boolean statementsRecovery = true;
		boolean bindingsRecovery = true;
		String unitName = "p/X.java";

		String contents =
			"package p;\n" +
			"public class X {\n" +
			" public int i;\n" +
			" public static void main(String[] args) {\n" +
			"  int length = args.length;\n" +
			"  System.out.println(length);\n" +
			" }\n" +
			" public void doSomething() {}" +
			"}\n\n" +
			"class Y extends X {\n" +
			" public int i;\n" +
			" public static void main(String[] args) {\n" +
			"  int length = args.length;\n" +
			"  System.out.println(length);\n" +
			" }\n" +
			" void doSomethingElse() {}" +
			"}";
		
		ASTNode node = createAST(AST_JLS_LATEST, contents, resolveBindings, statementsRecovery, bindingsRecovery, unitName);
		
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		
		CompilationUnit unit = (CompilationUnit) node;
		List<?> types = unit.types();
		System.out.println("Types: ");
		types.stream()
			.forEach((t) -> System.out.println("  " + getName(t)));
		TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
		ITypeBinding binding = typeDeclaration.resolveBinding();
		
		assertNotNull("No binding", binding);
		assertNull("Got a java element", binding.getJavaElement());
		assertEquals("Wrong name", "p.X", binding.getQualifiedName());
		
		MethodDeclaration methodDeclaration = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(1);
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		
		assertNotNull("No binding", methodBinding);
		
		ITypeBinding returnType = methodBinding.getReturnType();
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		
		assertNull("Got a java element", methodBinding.getJavaElement());
		
		Block body = methodDeclaration.getBody();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) body.statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		
		assertNotNull("No binding", variableBinding);
		assertNull("Got a java element", variableBinding.getJavaElement());
		
		ExpressionStatement statement2 = (ExpressionStatement) body.statements().get(1);
		Expression expression = statement2.getExpression();
		MethodInvocation invocation = (MethodInvocation) expression;
		Expression expression2 = invocation.getExpression();
		
		assertNotNull("No binding", expression2.resolveTypeBinding());
		
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typeDeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment2 = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		IVariableBinding variableBinding2 = fragment2.resolveBinding();
		
		assertNotNull("No binding", variableBinding2);
		assertNull("Got a java element", variableBinding2.getJavaElement());
		
		System.out.println("OK");
	}
	
	private static void assertEquals(String string, Object m, Object n) {
		assertTrue(string, m.equals(n));
	}
	
	private static void assertNotNull(String string, IBinding binding) {
		assertTrue(string, binding != null);
	}
	
	private static void assertNull(String string, IJavaElement javaElement) {
		assertTrue(string, javaElement == null);
	}
	
	private static void assertTrue(String string, boolean b) {
		if (!b) throw new AssertionError("");
	}
	
}