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

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class XmlParserTest {

  private Document doc;
  private NamedNodeMap attributes;

  @Test
  public void should_parse_xml() {
    doc = XmlParser.parseXML(new File("src/test/files/xml/parsing.xml"));

    assertPositionsMatch("assembly-descriptor", 1, 22, 16, 23);
  }

  private void assertPositionsMatch(String tagName, int startLine, int startColumn, int endLine, int endColumn) {
    attributes = doc.getElementsByTagName(tagName).item(0).getAttributes();
    assertAttributeMatch(XmlParser.START_LINE_ATTRIBUTE, startLine);
    assertAttributeMatch(XmlParser.START_COLUMN_ATTRIBUTE, startColumn);
    assertAttributeMatch(XmlParser.END_LINE_ATTRIBUTE, endLine);
    assertAttributeMatch(XmlParser.END_COLUMN_ATTRIBUTE, endColumn);
  }

  private void assertAttributeMatch(String attribute, int value) {
    Node namedItem = attributes.getNamedItem(attribute);
    assertThat(namedItem).isNotNull();

    String actual = namedItem.getNodeValue();
    String expected = String.valueOf(value);
    String message = "'" + attribute + "' : expected '" + expected + "' but got '" + actual + "'";
    assertThat(actual).overridingErrorMessage(message).isEqualTo(expected);
  }
}
