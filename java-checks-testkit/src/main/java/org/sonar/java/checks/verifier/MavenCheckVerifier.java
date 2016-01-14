/*
 * SonarQube Java
 * Copyright (C) 2012-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks.verifier;

import com.google.common.annotations.Beta;
import org.fest.assertions.Fail;
import org.sonar.java.AnalyzerMessage;
import org.sonar.java.checks.verifier.XmlCheckVerifier.FakeXmlFileScannerContext;
import org.sonar.java.xml.XmlCheck;
import org.sonar.java.xml.XmlParser;
import org.sonar.java.xml.maven.MavenFileScanner;
import org.sonar.java.xml.maven.MavenFileScannerContext;
import org.sonar.java.xml.maven.MavenParser;
import org.sonar.maven.model.LocatedTree;
import org.sonar.maven.model.maven2.MavenProject;
import org.w3c.dom.Document;

import java.io.File;
import java.util.List;

@Beta
public class MavenCheckVerifier extends CheckVerifier {

  private MavenCheckVerifier() {
  }

  @Override
  public String getExpectedIssueTrigger() {
    return ISSUE_MARKER;
  }

  public static void verify(String filename, MavenFileScanner check) {
    MavenCheckVerifier mavenCheckVerifier = new MavenCheckVerifier();
    scanFile(filename, check, mavenCheckVerifier);
  }

  public static void verifyNoIssue(String filename, MavenFileScanner check) {
    MavenCheckVerifier mavenCheckVerifier = new MavenCheckVerifier();
    mavenCheckVerifier.expectNoIssues();
    scanFile(filename, check, mavenCheckVerifier);
  }

  public static void verifyIssueOnFile(String filename, String message, MavenFileScanner check) {
    MavenCheckVerifier mavenCheckVerifier = new MavenCheckVerifier();
    mavenCheckVerifier.setExpectedFileIssue(message);
    scanFile(filename, check, mavenCheckVerifier);
  }

  private static void scanFile(String filename, MavenFileScanner check, MavenCheckVerifier mavenCheckVerifier) {
    File pom = new File(filename);
    MavenProject project = MavenParser.parseXML(pom);
    if (project != null) {
      XmlCheckVerifier.retrieveExpectedIssuesFromFile(pom, mavenCheckVerifier);
      FakeMavenFileScannerContext context = new FakeMavenFileScannerContext(XmlParser.parseXML(pom), pom, project);
      check.scanFile(context);
      mavenCheckVerifier.checkIssues(context.getMessages(), false);
    } else {
      Fail.fail("The test file can not be parsed");
    }
  }

  private static class FakeMavenFileScannerContext extends FakeXmlFileScannerContext implements MavenFileScannerContext {

    private final MavenProject project;

    public FakeMavenFileScannerContext(Document document, File pom, MavenProject project) {
      super(document, pom);
      this.project = project;
    }

    @Override
    public MavenProject getMavenProject() {
      return project;
    }

    @Override
    public void reportIssue(XmlCheck check, LocatedTree tree, String message) {
      reportIssue(check, tree.startLocation().line(), message);
    }

    @Override
    public void reportIssue(XmlCheck check, int line, String message, List<Location> secondary) {
      AnalyzerMessage analyzerMessage = new AnalyzerMessage(check, getXmlFile(), line, message, 0);
      for (Location location : secondary) {
        AnalyzerMessage secondaryLocation = new AnalyzerMessage(check, getXmlFile(), location.tree.startLocation().line(), location.msg, 0);
        analyzerMessage.secondaryLocations.add(secondaryLocation);
      }
      getMessages().add(analyzerMessage);
    }
  }
}
