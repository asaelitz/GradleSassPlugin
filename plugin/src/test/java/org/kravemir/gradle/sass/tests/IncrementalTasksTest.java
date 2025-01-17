package org.asaelitz.gradle.sass.tests;


import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Miroslav Kravec
 */
public class IncrementalTasksTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private File fooScss;


    @Before
    public void setup() throws IOException {
        File buildFile = testProjectDir.newFile("build.gradle");
        writeFile(buildFile, "plugins {\n id 'org.asaelitz.gradle.sass'\n }\nsass{\nmain{}}");

        Path mainSassPath = Paths.get(testProjectDir.getRoot().getPath(),"src","main","sass");
        Files.createDirectories(mainSassPath);

        File mainScss = mainSassPath.resolve("main.scss").toFile();
        mainScss.createNewFile();

        fooScss = mainSassPath.resolve("foo.scss").toFile();
        fooScss.createNewFile();

    }


    @Test
    public void testFooDeletion() {
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("mainSass")
                .withPluginClasspath()
                .build();

        assertEquals("check that ':mainSass' task succeeded", result.task(":mainSass").getOutcome(), TaskOutcome.SUCCESS);

        Path mainSassBuildRoot = Paths.get(testProjectDir.getRoot().getPath(), "build", "sass", "main");
        assertTrue(mainSassBuildRoot.resolve(Paths.get("main.css")).toFile().exists());
        assertTrue(mainSassBuildRoot.resolve(Paths.get("foo.css")).toFile().exists());

        fooScss.delete();

        BuildResult result2 = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withArguments("mainSass")
                .withPluginClasspath()
                .build();

        assertEquals("check that ':mainSass' task succeeded", result2.task(":mainSass").getOutcome(), TaskOutcome.SUCCESS);

        assertTrue(mainSassBuildRoot.resolve(Paths.get("main.css")).toFile().exists());
        assertFalse(mainSassBuildRoot.resolve(Paths.get("foo.css")).toFile().exists());
    }


    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
