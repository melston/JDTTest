package com.jdttst.types;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.jdttst.utils.StreamUtils;

public class MethodInfo {

	private IMethodBinding methodBinding;
	private List<ParameterInfo> methodParams;
	
	public MethodInfo(IMethodBinding binding) {
		methodBinding = binding;
		fillParams();
	}
	
	public String getName() {
		return methodBinding.getName();
	}
	
	public boolean isConstructor() {
		return methodBinding.isConstructor();
	}
	
	public boolean isSameMethodOrOverrides(IMethodBinding other) {
		// This is the same method as other if 
		// the two methodBindings compare as == or
		// if this one overrides the other.  We don't 
		// have to check for other.overrides(methodBinding)
		// since the only time this method is called is when
		// we are producing a complete list of methods for a 
		// given ITypeBinding and that starts with the current
		// type and recurses into the parent super-interfaces,
		// never in the reverse direction.  So, 'other' could
		// never override this instance.
		return (other == methodBinding) || 
				methodBinding.overrides(other);
	}
	
	public Stream<ParameterInfo> getParameters() {
		return methodParams.stream();
	}
	
	public TypeInfo getReturnType() {
		ITypeBinding tb = methodBinding.getReturnType();
		return TypeDB.getOrAdd(tb);
	}
	
	private void fillParams() {
		methodParams = StreamUtils.zip(
				methodBinding.getParameterTypes(),
				methodBinding.getParameterNames(),
				(pt, pn) -> new ParameterInfo(pt, pn))
				.toList();
	}
}
