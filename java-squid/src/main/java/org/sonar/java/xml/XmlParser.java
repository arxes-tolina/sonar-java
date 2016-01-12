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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

public class XmlParser {

  public static final String START_LINE_ATTRIBUTE = "start_line";
  public static final String START_COLUMN_ATTRIBUTE = "start_column";
  public static final String END_LINE_ATTRIBUTE = "end_line";
  public static final String END_COLUMN_ATTRIBUTE = "end_column";

  private XmlParser() {
  }

  public static Document parseXML(File file) {
    Document document = null;
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      document = docBuilder.newDocument();
      LocationHandler locationHandler = new LocationHandler(document);
      parser.parse(file, locationHandler);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.printStackTrace();
    }
    return document;
  }

  private static class LocationHandler extends DefaultHandler {
    private final Document document;
    private final Deque<Element> elementStack = new LinkedList<>();
    private Locator locator;
    private StringBuilder textBuffer = new StringBuilder();

    public LocationHandler(Document document) {
      this.document = document;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      addTextIfNeeded();
      Element startingElement = document.createElement(qName);

      for (int i = 0; i < attributes.getLength(); i++) {
        startingElement.setAttribute(attributes.getQName(i), attributes.getValue(i));
      }

      savePosition(startingElement, START_LINE_ATTRIBUTE, locator.getLineNumber());
      savePosition(startingElement, START_COLUMN_ATTRIBUTE, locator.getColumnNumber());

      elementStack.push(startingElement);
    }

    private static void savePosition(Element element, String attribute, int value) {
      element.setAttribute(attribute, String.valueOf(value));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      addTextIfNeeded();
      Element endingElement = elementStack.pop();

      savePosition(endingElement, END_LINE_ATTRIBUTE, locator.getLineNumber());
      savePosition(endingElement, END_COLUMN_ATTRIBUTE, locator.getColumnNumber());

      if (elementStack.isEmpty()) {
        // root
        document.appendChild(endingElement);
      } else {
        Element parentElement = elementStack.peek();
        parentElement.appendChild(endingElement);
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      textBuffer.append(ch, start, length);
    }

    private void addTextIfNeeded() {
      if (textBuffer.length() > 0) {
        Element element = elementStack.peek();
        Node textNode = document.createTextNode(textBuffer.toString());
        element.appendChild(textNode);
        textBuffer = new StringBuilder();
      }
    }
  }
}
