package com.jdttst.types;

import org.eclipse.jdt.core.dom.ITypeBinding;

public class ParameterInfo {
	private ITypeBinding typeBinding;
	private String name;
	
	ParameterInfo(ITypeBinding tb, String name) {
		this.name = name;
		this.typeBinding = tb;
	}
	
	public String getName() {
		return this.name;
	}
	
	public TypeInfo getTypeInfo() {
		return TypeDB.getOrAdd(typeBinding);
	}
}
