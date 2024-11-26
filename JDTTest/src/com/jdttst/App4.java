package com.jdttst;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jdttst.types.MethodInfo;
import com.jdttst.types.TypeDB;
import com.jdttst.types.TypeInfo;
import com.jdttst.visitors.FileVisitor;

public class App4 {
	
    public static void main(String[] args) {
		List<Path> basePaths = List.of(Path.of("../SourceProj/src/"));

		String errors = basePaths.stream()
			.map(FileVisitor::processDirectoryTree)
			.flatMap(lst -> lst)
			.collect(Collectors.joining("\n"));
		
		if (errors.length() > 0) {
			System.err.println("---------------\nErrors:\n" + errors);
		}
		
		TypeDB.getAllTypes()
			.collect(Collectors.groupingBy(TypeInfo::packageName))
			// The rest of this is just to get the Map to print 
			// sorted by the package name...
			.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(oldValue, newValue) -> oldValue,
					LinkedHashMap::new))
			.forEach(App4::printPkgInfo);
    }
    
    private static void printPkgInfo(String pn, List<TypeInfo> lti) {
    	System.out.println("Package: " + pn + ":");
    	lti.stream()
    		.forEach(App4::printTypeInfo);
    }
    
    private static void printTypeInfo(TypeInfo ti) {
    	String impl = ti.implementsIFs()
    			.map(inf -> inf.fullyQualifiedName())
    			.collect(Collectors.joining(", "));
    	if (impl.length() > 0) impl = " : " + impl;
    	System.out.println("  " + ti.fullyQualifiedName() + impl);
    	ti.methods()
    		.forEach(App4::printMethodInfo);
    }
    
    private static void printMethodInfo(MethodInfo mi) {
    	if (mi.isConstructor()) return;
    	System.out.print("    - " + mi.getName() + "(");
    	System.out.print(mi.getParameters()
    		.map(pi -> pi.getTypeInfo().fullyQualifiedName() + " " + pi.getName())
    		.collect(Collectors.joining(", ")));
    	System.out.println(") -> " + mi.getReturnType().fullyQualifiedName());
    }
}
