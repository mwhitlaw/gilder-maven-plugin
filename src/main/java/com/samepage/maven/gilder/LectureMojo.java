package com.samepage.maven.gilder;

import com.samepage.maven.PomManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.net.URISyntaxException;

@Mojo(name = "lecture", aggregator = true)
public class LectureMojo extends AbstractMojo {


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("One does not simply add a dependency");
            PomManager pomManager = new PomManager();
            pomManager.printOriginal();
            pomManager.printWorking();
        } catch (XmlPullParserException | IOException | URISyntaxException e) {
            getLog().error(e);
        }
    }
}
