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
package org.sonar.java.xml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.java.SonarComponents;
import org.sonar.java.xml.maven.MavenFileScanner;
import org.sonar.java.xml.maven.MavenFileScannerContext;
import org.sonar.java.xml.maven.MavenFileScannerContextImpl;
import org.sonar.java.xml.maven.MavenParser;
import org.sonar.maven.model.maven2.MavenProject;
import org.sonar.squidbridge.ProgressReport;
import org.sonar.squidbridge.api.CodeVisitor;
import org.w3c.dom.Document;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class XmlAnalyzer {

  private static final Logger LOG = LoggerFactory.getLogger(XmlAnalyzer.class);
  private final SonarComponents sonarComponents;
  private final List<XmlFileScanner> xmlFileScanners;
  private final List<MavenFileScanner> mavenFileScanners;

  public XmlAnalyzer(SonarComponents sonarComponents, CodeVisitor... visitors) {
    ImmutableList.Builder<XmlFileScanner> xmlScannersBuilder = ImmutableList.builder();
    ImmutableList.Builder<MavenFileScanner> mavenScannersBuilder = ImmutableList.builder();
    for (CodeVisitor visitor : visitors) {
      if (visitor instanceof XmlFileScanner) {
        xmlScannersBuilder.add((XmlFileScanner) visitor);
      }
      if (visitor instanceof MavenFileScanner) {
        mavenScannersBuilder.add((MavenFileScanner) visitor);
      }
    }
    this.xmlFileScanners = xmlScannersBuilder.build();
    this.mavenFileScanners = mavenScannersBuilder.build();
    this.sonarComponents = sonarComponents;
  }

  public void scan(Iterable<File> files) {
    boolean hasScanners = !xmlFileScanners.isEmpty() || !mavenFileScanners.isEmpty();
    boolean hasXmlFile = !Iterables.isEmpty(files);
    if (hasScanners && !hasXmlFile) {
      LOG.warn("No 'xml' file have been indexed.");
      return;
    }

    ProgressReport progressReport = new ProgressReport("Report about progress of Xml analyzer", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(Lists.newArrayList(files));

    boolean successfulyCompleted = false;
    try {
      for (File file : files) {
        simpleScan(file);
        progressReport.nextFile();
      }
      successfulyCompleted = true;
    } finally {
      if (successfulyCompleted) {
        progressReport.stop();
      } else {
        progressReport.cancel();
      }
    }
  }

  private void simpleScan(File file) {
    Document document = XmlParser.parseXML(file);
    simpleScanAsXmlFile(file, document);
    if ("pom.xml".equals(file.getName())) {
      simpleScanAsPomFile(file, document);
    }
  }

  private void simpleScanAsXmlFile(File file, Document document) {
    XmlFileScannerContext scannerContext = new XmlFileScannerContextImpl(document, file, sonarComponents);
    for (XmlFileScanner xmlFileScanner : xmlFileScanners) {
      xmlFileScanner.scanFile(scannerContext);
    }
  }

  private void simpleScanAsPomFile(File file, Document document) {
    MavenProject project = MavenParser.parseXML(file);
    MavenFileScannerContext mavenContext = new MavenFileScannerContextImpl(project, document, file, sonarComponents);
    if (project != null) {
      for (MavenFileScanner mavenFileScanner : mavenFileScanners) {
        mavenFileScanner.scanFile(mavenContext);
      }
    }
  }
}
