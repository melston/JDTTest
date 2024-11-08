package com.elsoft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class App {
    
    public static void main(String[] args) {
        TypeVisitor tpv = new TypeVisitor();
        
        List<String> myFiles = List.of("PType1.java", "CType1.java", 
                "PType2.java", "CType2.java");

        myFiles.stream().forEach(p -> {
            processFile(tpv, p);
            System.out.println("-----------------");
        });
    }
    
    class MyFileASTRequestor extends FileASTRequestor {
        
    }
    
    protected static void processFile(ASTVisitor impl, String path) {
        String source;
        try {
            source = readFile("../SourceProj/src/com/elsoft/testsrc/" + path);
            CompilationUnit cu = parse(source);
            cu.accept(impl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String readFile(String pathToFile) throws IOException {
        byte[] ba = Files.readAllBytes(Paths.get(pathToFile));
        return new String(ba);
    }

    public static CompilationUnit parse(String source) {
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());

        Hashtable<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        parser.setCompilerOptions(options);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        
        return (CompilationUnit) parser.createAST(new NullProgressMonitor());
    }

}
