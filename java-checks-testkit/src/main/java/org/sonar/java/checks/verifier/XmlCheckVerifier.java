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
import org.sonar.java.xml.XmlCheck;
import org.sonar.java.xml.XmlFileScanner;
import org.sonar.java.xml.XmlFileScannerContextImpl;
import org.sonar.java.xml.XmlParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Beta
public class XmlCheckVerifier extends CheckVerifier {

  private XmlCheckVerifier() {
  }

  @Override
  public String getExpectedIssueTrigger() {
    return ISSUE_MARKER;
  }

  public static void verify(String filename, XmlFileScanner check) {
    XmlCheckVerifier xmlCheckVerifier = new XmlCheckVerifier();
    scanFile(filename, check, xmlCheckVerifier);
  }

  public static void verifyNoIssue(String filename, XmlFileScanner check) {
    XmlCheckVerifier xmlCheckVerifier = new XmlCheckVerifier();
    xmlCheckVerifier.expectNoIssues();
    scanFile(filename, check, xmlCheckVerifier);
  }

  public static void verifyIssueOnFile(String filename, String message, XmlFileScanner check) {
    XmlCheckVerifier xmlCheckVerifier = new XmlCheckVerifier();
    xmlCheckVerifier.setExpectedFileIssue(message);
    scanFile(filename, check, xmlCheckVerifier);
  }

  private static void scanFile(String filename, XmlFileScanner check, XmlCheckVerifier xmlCheckVerifier) {
    File file = new File(filename);
    Document document = XmlParser.parseXML(file);
    if (document != null) {
      retrieveExpectedIssuesFromFile(file, xmlCheckVerifier);
      FakeXmlFileScannerContext context = new FakeXmlFileScannerContext(document, file);
      check.scanFile(context);
      xmlCheckVerifier.checkIssues(context.messages, false);
    } else {
      Fail.fail("The test file can not be parsed");
    }
  }

  protected static void retrieveExpectedIssuesFromFile(File xmlFile, CheckVerifier checkVerifier) {
    try (FileInputStream is = new FileInputStream(xmlFile)) {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader reader = factory.createXMLStreamReader(is);

      while (reader.hasNext()) {
        int line = reader.getLocation().getLineNumber();
        reader.next();
        if (reader.getEventType() == XMLStreamReader.COMMENT) {
          String text = reader.getText().trim();
          checkVerifier.collectExpectedIssues(text, line);
        }
      }
    } catch (XMLStreamException | IOException e) {
      Fail.fail("The test file can not be parsed to retrieve comments", e);
    }
  }

  public static class FakeXmlFileScannerContext extends XmlFileScannerContextImpl {

    private final Set<AnalyzerMessage> messages = new HashSet<>();

    public FakeXmlFileScannerContext(Document document, File file) {
      super(document, file, null);
    }

    public Set<AnalyzerMessage> getMessages() {
      return messages;
    }

    @Override
    public void reportIssueOnFile(XmlCheck check, String message) {
      reportIssue(check, -1, message);
    }

    @Override
    public void reportIssue(XmlCheck check, Node node, String message) {
      Node lineAttribute = node.getAttributes().getNamedItem(XmlParser.START_LINE_ATTRIBUTE);
      if (lineAttribute != null) {
        Integer line = Integer.valueOf(lineAttribute.getNodeValue());
        reportIssue(check, line, message);
      }
    }

    @Override
    public void reportIssue(XmlCheck check, int line, String message) {
      messages.add(new AnalyzerMessage(check, getXmlFile(), line, message, 0));
    }
  }
}
