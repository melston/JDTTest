package com.jdttst.utils;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JDTUtils {

	public static ITypeBinding getContainingType(AbstractTypeDeclaration atd) {
		ITypeBinding rv = null;
		if (atd.isLocalTypeDeclaration()) {
			rv = ((TypeDeclaration) atd.getParent()).resolveBinding();
		}
		return rv;
	}
}
