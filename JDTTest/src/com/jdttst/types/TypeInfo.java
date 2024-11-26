package com.jdttst.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeInfo {

	private ITypeBinding typeBinding;
	private ArrayList<ImportDeclaration> imports; // Do we even need this now?  Perhaps for code gen...
	private List<MethodInfo> methods;
	
	public TypeInfo(TypeDeclaration td, List<ImportDeclaration> imports) {
		this.typeBinding = td.resolveBinding();
		this.imports = new ArrayList<>(imports);
		this.methods = new ArrayList<>();
		collectMethods(typeBinding, methods);
		
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bds = td.bodyDeclarations();
		bds.stream()
			.filter(bd -> (bd instanceof AbstractTypeDeclaration))
			.forEach(bd -> {
				AbstractTypeDeclaration atd = (AbstractTypeDeclaration) bd;
				TypeDB.addType(atd, imports);
			});
	}

	public TypeInfo(EnumDeclaration ed) {
		this.typeBinding = ed.resolveBinding();
		imports = new ArrayList<>();
		this.methods = new ArrayList<>();
	}
	
	public TypeInfo(ITypeBinding tb) {
		this.typeBinding = tb;
	}
	
	public Stream<ImportDeclaration> getImports() {
		return imports.stream();
	}
	
	public String fullyQualifiedName() {
		return typeBinding.getQualifiedName()
				.replace("java.lang.", "");
	}
	
	public String packageName() {
		return typeBinding.getPackage().getName();
	}
	
	public Stream<MethodInfo> methods() {
		return methods.stream();
	}
	
	public Stream<TypeInfo> implementsIFs() {
		return Arrays.stream(typeBinding.getInterfaces())
				.map(tb -> TypeDB.getOrAdd(tb));
	}
	
	/**
	 * Is this a type that is know without an import statement of some kind.
	 * This amounts to whether this is an atomic type (int, float, etc.), void,
	 * or a type in the 'java.lang' package.
	 * @return
	 */
	public boolean isBuiltIn() {
		List<String> builtIns = List.of(
				"void", "char", "byte", "int", "float", "double"
				);
		return builtIns.contains(fullyQualifiedName()) ||
				packageName().startsWith("java.lang.");
	}
	
	private void collectMethods(ITypeBinding typeBinding, List<MethodInfo> methods) {
	    // Add declared methods
	    for (IMethodBinding method : typeBinding.getDeclaredMethods()) {
	    	boolean found =  methods.stream()
	    						.anyMatch(m -> m.isSameMethodOrOverrides(method));
	    	if (!found) methods.add(new MethodInfo(method));
	    }

	    // Recurse through superinterfaces
	    for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
	        collectMethods(superInterface, methods);
	    }
	}
}
