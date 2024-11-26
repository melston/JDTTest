package com.jdttst;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

//import com.jdttst.visitors.TypeVisitor;

public class App2 {
    
    public static void main(String[] args) {
        try {
            //TypeVisitor tpv = new TypeVisitor();
            
            // Step 1: Set up workspace location
            String workspaceRootPath = "..";
            File workspaceRoot = new File(workspaceRootPath);
            if (!workspaceRoot.exists()) {
                workspaceRoot.mkdirs(); // Ensure the directory exists
            }
            
            initializePlatform(workspaceRoot);

            // Step 3: Set workspace location using Location service
            Location instanceLocation = Platform.getInstanceLocation();
            instanceLocation.set(new Path(workspaceRootPath).toFile().toURI().toURL(), false);

            // Step 4: Now, get the workspace
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IWorkspaceRoot root = workspace.getRoot();

            // Step 3: Set up project directories and associate with IWorkspace
            String projectName = "SourceProj";
            IProject project = root.getProject(projectName);

            // Check if project already exists
            if (!project.exists()) {
                try {
                    // Specify project location
                    IPath projectLocation = new Path(workspaceRootPath).append(projectName);

                    // Create the project description
                    IProjectDescription description = workspace.newProjectDescription(project.getName());
                    description.setLocation(projectLocation);

                    // Create the project in the workspace
                    project.create(description, null);
                    project.open(null);

                    System.out.println("Project created and opened successfully!");

                } catch (CoreException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Project already exists.");
            }

            System.out.println("Ready to process");
            // Additional steps to handle specific directory trees or resources can be added here        }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void initializePlatform(File workspaceRoot) 
            throws BundleException, IllegalStateException, IOException {
        // Initialize the OSGi platform if necessary
        Bundle bundle = FrameworkUtil.getBundle(App2.class);
        if (bundle != null) {
        	System.out.println("Starting bundle");
            bundle.start();
        }

        // Start the ResourcesPlugin bundle manually to ensure it's available
        Bundle resourcesPluginBundle = Platform.getBundle(ResourcesPlugin.PI_RESOURCES);
        if (resourcesPluginBundle != null && resourcesPluginBundle.getState() != Bundle.ACTIVE) {
        	System.out.println("Starting plugin bundle");
            resourcesPluginBundle.start(Bundle.START_TRANSIENT);
        }

        // Set the instance location (workspace path) before initializing the workspace
        URL workspaceURL = workspaceRoot.toURI().toURL();
        setInstanceLocation(workspaceURL);
    }

    private static void setInstanceLocation(URL workspaceURL) throws IllegalStateException, IOException {
        Location instanceLocation = Platform.getInstanceLocation();
        if (!instanceLocation.isSet()) {
            instanceLocation.set(workspaceURL, false);
        }
    }
}
