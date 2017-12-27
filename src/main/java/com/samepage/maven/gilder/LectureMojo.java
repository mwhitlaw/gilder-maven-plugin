package com.samepage.maven.gilder;

import com.samepage.maven.PomManager;
import com.samepage.maven.Problem;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Mojo(name = "lecture", aggregator = true)
public class LectureMojo extends AbstractMojo {


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("One does not simply add a dependency");
            PomManager pomManager = new PomManager();
            List<Problem> problems = pomManager.getProblems();
            if (problems.size() > 0) {
                for (Problem problem : problems) {
                    getLog().error(problem.toString());
                }
            } else {
                getLog().info("No Problems Found");
            }
        } catch (XmlPullParserException | IOException | URISyntaxException e) {
            getLog().error(e);
        }
    }
}
