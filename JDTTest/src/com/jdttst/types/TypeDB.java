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
		String key = tb.getQualifiedName();
		key = key.replaceFirst("<.*>", "");
		return key;
		//return tb.getKey();
	}
			
	private static TypeDB theDB;
	static 
	{
		theDB = new TypeDB();
	}

	/**
	 * Access the one and only DB
	 * @return
	 */
	public static TypeDB instance() {
		return theDB;
	}
	
	private TypeDB() {}
	
	// I tried using ITypeBinding objects as the key but their .equals() method
	// simply check for object equality.  So this is an attempt to use the String
	// returned by the getKey() method on the ITypeBinding object for lookup.
	// That isn't working either.
	private static HashMap<String, TypeInfo> types = new HashMap<>();
	
	public static void addType(AbstractTypeDeclaration atd, List<ImportDeclaration> imports) {
		if (atd instanceof TypeDeclaration td) theDB.addTypeDecl(td, imports);
		if (atd instanceof EnumDeclaration ed) theDB.addEnumDecl(ed);
	}
	
	public static TypeInfo getOrAdd(ITypeBinding tb) {
		TypeInfo ti = getTypeInfoFor(tb);
		if (ti == null) {
			ti = theDB.addType(tb);
		}
		return ti;
	}

	public static Stream<TypeInfo> getAllTypes() {
		return types.values().stream();
	}

	public static TypeInfo getTypeInfoFor(ITypeBinding tb) {
		return types.get(lookupKey(tb)); // Caution - can be null.
	}
	
	private void addTypeDecl(TypeDeclaration td, List<ImportDeclaration> imports) {
		ITypeBinding tb = td.resolveBinding();
		types.put(lookupKey(tb), new TypeInfo(td, imports));
	}
	
	private void addEnumDecl(EnumDeclaration ed) {
		ITypeBinding tb = ed.resolveBinding();
		types.put(lookupKey(tb), new TypeInfo(ed));
	}

	private TypeInfo addType(ITypeBinding tb) {
		TypeInfo rv = new TypeInfo(tb);
		types.put(lookupKey(tb), rv);
		return rv;
	}
}
