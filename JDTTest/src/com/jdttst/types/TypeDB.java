package com.jdttst.types;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDB {
	
	private static String lookupKey(ITypeBinding tb) {
		return tb.getErasure().getQualifiedName();
	}
	
	private TypeDB() {}
	
	private static HashMap<String, TypeInfo> types = new HashMap<>();
	
	public static void addType(AbstractTypeDeclaration atd, List<ImportDeclaration> imports) {
		if (atd instanceof TypeDeclaration td) addTypeDecl(td, imports);
		if (atd instanceof EnumDeclaration ed) addEnumDecl(ed);
	}
	
	/**
	 * Get a {@link TypeInfo} object for the {@link org.eclipse.jdt.core.dom.ITypeBinding}.
	 * If one does not exist in the database then create a new one, add it to the database, and
	 * return it.
	 * @param tb
	 * @return
	 */
	public static TypeInfo getOrAdd(ITypeBinding tb) {
		TypeInfo ti = getTypeInfoFor(tb);
		if (ti == null) {
			ti = addType(tb);
		}
		return ti;
	}

	/**
	 * Get a {@link TypeInfo} object for the {@link org.eclipse.jdt.core.dom.ITypeBinding}.
	 * If one does not exist in the database then create a new one but don't add it to the DB.
	 * @param tb
	 * @return
	 */
	public static TypeInfo getOrNew(ITypeBinding tb) {
		TypeInfo ti = getTypeInfoFor(tb);
		if (ti == null) {
			ti = new TypeInfo(tb);
		}
		return ti;
	}

	/**
	 * Get a {@link TypInfo} object associated with the provided 
	 * {@link org.eclipse.jdt.core.dom.ITypeBinding} object.  If one does not exist in
	 * the database then return null.
	 * @param tb
	 * @return
	 */
	public static TypeInfo getTypeInfoFor(ITypeBinding tb) {
		return types.get(lookupKey(tb)); // Caution - can be null.
	}
	
	/**
	 * Return a {@link Stream} of all {@link TypeInfo} objects in the database.
	 * @return
	 */
	public static Stream<TypeInfo> getAllTypes() {
		return types.values().stream();
	}

	private static void addTypeDecl(TypeDeclaration td, List<ImportDeclaration> imports) {
		ITypeBinding tb = td.resolveBinding();
		types.put(lookupKey(tb), new TypeInfo(td, imports));
	}
	
	private static void addEnumDecl(EnumDeclaration ed) {
		ITypeBinding tb = ed.resolveBinding();
		types.put(lookupKey(tb), new TypeInfo(ed));
	}

	private static TypeInfo addType(ITypeBinding tb) {
		TypeInfo rv = new TypeInfo(tb);
		types.put(lookupKey(tb), rv);
		return rv;
	}
}
