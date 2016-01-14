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

import org.sonar.java.SonarComponents;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;

public class XmlFileScannerContextImpl implements XmlFileScannerContext {

  private final Document document;
  private final File file;
  private final SonarComponents sonarComponents;

  public XmlFileScannerContextImpl(Document document, File file, SonarComponents sonarComponents) {
    this.document = document;
    this.file = file;
    this.sonarComponents = sonarComponents;
  }

  @Override
  public File getXmlFile() {
    return file;
  }

  @Override
  public NodeList evaluateXPathExpression(String expression) throws XPathExpressionException {
    XPath xPath = XPathFactory.newInstance().newXPath();
    return (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
  }

  @Override
  public NodeList evaluateXPathExpressionFromNode(Node node, String expression) throws XPathExpressionException {
    XPath xPath = XPathFactory.newInstance().newXPath();
    return (NodeList) xPath.compile(expression).evaluate(node, XPathConstants.NODESET);
  }

  @Override
  public void reportIssueOnFile(XmlCheck check, String message) {
    sonarComponents.addIssue(file, check, -1, message, null);
  }

  @Override
  public void reportIssue(XmlCheck check, int line, String message) {
    sonarComponents.addIssue(file, check, line, message, null);
  }

  @Override
  public void reportIssue(XmlCheck check, Node node, String message) {
    Node lineAttribute = node.getAttributes().getNamedItem(XmlParser.START_LINE_ATTRIBUTE);
    if (lineAttribute != null) {
      Integer line = Integer.valueOf(lineAttribute.getNodeValue());
      sonarComponents.addIssue(file, check, line, message, null);
    }
  }

  public SonarComponents getSonarComponents() {
    return sonarComponents;
  }
}
