package com.jdttst;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class App3 {

	private static Framework framework;

    public static void main(String[] args) {
    	System.setProperty("eclipse.log.level", "ALL");
        try {
            System.setProperty("osgi.instance.area", "file:///D|/Projects/Test/Java/JDTTest/SourceWS");
            System.setProperty("osgi.configuration.area", "file:configuration/");
            System.setProperty("osgi.user.area", "file:user/");

            // Start the OSGi framework
            startOSGiFramework();


            // Initialize the workspace
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();

            // Refresh the workspace root
            root.refreshLocal(IResource.DEPTH_INFINITE, null);
            System.out.println("Workspace location: " + root.getLocation());

            // Perform further operations on the workspace here

        } catch (CoreException e) {
            e.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            try {
                // Stop the OSGi framework
                stopOSGiFramework();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void startOSGiFramework() throws Exception {
        FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        Map<String, String> config = new HashMap<>();
        config.put("osgi.console", "");
        framework = frameworkFactory.newFramework(config);
        framework.start();
        System.out.println("OSGi framework started.");
    }

    private static void stopOSGiFramework() throws Exception {
        if (framework != null) {
            framework.stop();
            framework.waitForStop(0);
            System.out.println("OSGi framework stopped.");
        }
    }
}

