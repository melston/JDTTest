package com.jdttst.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.*;

public class TypeVisitor extends ASTVisitor {
	
	List<Type> foundTypes = new ArrayList<>();

    @Override
    public boolean visit(ParameterizedType type) {
        System.out.println("(TypeVisitor) ParameterizedType: " + getTypeName(type, false));

        @SuppressWarnings("unchecked")
		List<Type> typeArguments = type.typeArguments();
        for (Type typeArg : typeArguments) {
        	String name = getTypeName(typeArg, false);
            System.out.println("\tType argument name: " + name);
        }
        
        if (foundTypes.contains(type)) { System.out.println("\t**Found Type"); }
        
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration td) {
        System.out.println("(TypeVisitor) TypeDecl: " + td.getName().toString());
        
        //foundTypes.add(td.type)

        @SuppressWarnings("unchecked")
		List<TypeParameter> typeParameters = td.typeParameters();
        for (TypeParameter typeParam : typeParameters) {
            String name = typeParam.getName().toString();
            System.out.println("\tType parameter name: " + name);
        }
        
        @SuppressWarnings("unchecked")
		List<Type> supers = td.superInterfaceTypes();
        for (Type t : supers) {
        	if (t instanceof ParameterizedType pt) {
        		System.out.println("\tParameterized Super: " + getTypeName(t, false));
        		@SuppressWarnings("unchecked")
				List<Type> typeArgs = pt.typeArguments(); 
        		for (Type tat : typeArgs) {
        			System.out.println("\t\tType Arg: " + getTypeName(tat, false));
        		}
        	}
        }
        
        return true;
    }
    
	public static String getTypeName(Type t, boolean includeTypeParams) {
		if (t == null) return "void";
		if (t instanceof ParameterizedType pt) {
			String typeStr = getTypeName(pt.getType(), false);
			if (includeTypeParams) {
				@SuppressWarnings("unchecked")
				List<Type> targs = pt.typeArguments();
				String typeArgs = targs.stream()
						.map((Type inst) -> getTypeName(inst, false))
						.collect(Collectors.joining(","));
				typeStr = typeStr + "<" + typeArgs + ">";
			}
			return typeStr;
		}
		if (t instanceof SimpleType s) {
			return s.getName().getFullyQualifiedName();
		}
		if (t instanceof QualifiedType qt) {
			return getTypeName(qt.getQualifier(), false)
					+ "." 
					+ qt.getName().getFullyQualifiedName();
		}
		if (t instanceof NameQualifiedType nt) {
			return nt.getQualifier().getFullyQualifiedName() 
					+ "." 
					+ nt.getName().getFullyQualifiedName();
		}
		return t.toString();
	}
}
