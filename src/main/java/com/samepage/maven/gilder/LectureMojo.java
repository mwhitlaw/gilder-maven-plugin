package com.samepage.maven.gilder;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

@Mojo(name = "lecture")
public class LectureMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        List moduleNames = mavenProject.getModules();
        getLog().info(String.format("%s One does not simply add a dependency", mavenProject.getName()));

        if (moduleNames != null) {
            moduleNames.forEach((i) -> {
                String moduleName = (String) i;
                getLog().info(String.format("Module: %s", moduleName));
            });
        }
    }
}
