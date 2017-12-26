package com.samepage.maven.gilder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "lecture", aggregator = true)
public class LectureMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject rootProject;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {



        List moduleNames = rootProject.getModules();
        getLog().info(String.format("%s: One does not simply add a dependency", rootProject.getName()));



        if (moduleNames != null && moduleNames.size() > 0) {
            moduleNames.forEach((i) -> {
                String moduleName = (String) i;
                getLog().info(String.format("Module: %s", moduleName));
            });
        } else {
            getLog().info("This project has no modules");
        }
    }
}
