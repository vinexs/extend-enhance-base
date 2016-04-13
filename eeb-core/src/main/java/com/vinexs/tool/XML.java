/*
 * Copyright (c) 2015. Vin @ vinexs.com (MIT License)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.vinexs.tool;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * XML parser, parse standard XML to JSONObject or JSON Stringify.
 * To fix that org.json components conflict with android internal library.
 * This parser use org.w3c.dom component to re-format XML and put data to JSONObject.
 * <p>
 * Dependence on com.vinexs.tool.Determinator
 */
@SuppressWarnings("unused")
public class XML {

    /**
     * Parse standard XML to JSONObject.
     *
     * @param xml XML document stored by string.
     * @return JSONObject
     */
    public static JSONObject toJSONObject(String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
        return toJSONObject(inputStream);
    }

    /**
     * Parse standard XML to JSONObject.
     *
     * @param file XML document stored in a file.
     * @return JSONObject
     */
    public static JSONObject toJSONObject(File file) {
        try {
            InputStream fileInputStream = new FileInputStream(file);
            return toJSONObject(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse standard XML to JSONObject.
     *
     * @param inputStream InputStream point to a XML document.
     * @return JSONObject
     */
    public static JSONObject toJSONObject(InputStream inputStream) {
        JSONObject json = new JSONObject();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            org.w3c.dom.Element tag = document.getDocumentElement();
            json.put(tag.getTagName(), getChildJSONObject(tag));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * Parse standard XML to JSON Stringify.
     *
     * @param xml XML document stored by string.
     * @return JSONObject
     */
    public static String toJSONStringify(String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
        return toJSONStringify(inputStream);
    }

    /**
     * Parse standard XML to JSON Stringify.
     *
     * @param file XML document stored in a file.
     * @return JSONObject
     */
    public static String toJSONStringify(File file) {
        try {
            InputStream fileInputStream = new FileInputStream(file);
            return toJSONStringify(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse standard XML to JSON Stringify.
     *
     * @param inputStream InputStream point to a XML document.
     * @return JSONObject
     */
    public static String toJSONStringify(InputStream inputStream) {
        JSONObject json = toJSONObject(inputStream);
        if (json == null) {
            return "{}";
        }
        return json.toString();
    }

    private static Object getChildJSONObject(org.w3c.dom.Element tag) {
        int i, k;

        //get attributes && child nodes
        NamedNodeMap attributes = tag.getAttributes();
        NodeList childNodes = tag.getChildNodes();
        int numAttr = attributes.getLength();
        int numChild = childNodes.getLength();

        //get element nodes
        Boolean hasTagChild = false;
        Map<String, ArrayList<Object>> childMap = new HashMap<>();
        for (i = 0; i < numChild; i++) {
            Node node = childNodes.item(i);
            //not process non-element node
            if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            hasTagChild = true;
            org.w3c.dom.Element childTag = (org.w3c.dom.Element) node;
            String tagName = childTag.getTagName();
            if (!childMap.containsKey(tagName)) {
                childMap.put(tagName, new ArrayList<>());
            }
            childMap.get(tagName).add(getChildJSONObject(childTag));
        }
        if (numAttr == 0 && !hasTagChild) {
            // Return String
            return stringToValue(tag.getTextContent());
        } else {
            // Return JSONObject
            JSONObject data = new JSONObject();
            if (numAttr > 0) {
                for (i = 0; i < numAttr; i++) {
                    Node attr = attributes.item(i);
                    try {
                        data.put(attr.getNodeName(), stringToValue(attr.getNodeValue()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (hasTagChild) {
                for (Map.Entry<String, ArrayList<Object>> tagMap : childMap.entrySet()) {
                    ArrayList<Object> tagList = tagMap.getValue();
                    if (tagList.size() == 1) {
                        try {
                            data.put(tagMap.getKey(), tagList.get(0));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        JSONArray array = new JSONArray();
                        for (k = 0; k < tagList.size(); k++) {
                            array.put(tagList.get(k));
                        }
                        try {
                            data.put(tagMap.getKey(), array);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                try {
                    data.put("content", stringToValue(tag.getTextContent()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return data;
        }
    }

    /**
     * Convert a string to its possible data type.
     *
     * @param string String to be converted.
     * @return Value in Boolean, Null, Integer, Double or String.
     */
    public static Object stringToValue(String string) {
        if ("true".equalsIgnoreCase(string)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(string)) {
            return Boolean.FALSE;
        }
        if ("null".equalsIgnoreCase(string)) {
            return JSONObject.NULL;
        }
        if (Determinator.isNumeric(string)) {
            if (Determinator.isInteger(string)) {
                Integer strInt = Integer.valueOf(string);
                if (strInt.toString().equals(string)) {
                    return strInt;
                }
            }
            return Double.valueOf(string);
        }
        return string;
    }

    // =============================================================================================

    /**
     * Parse standard XML to element structure, allow access with css selector.
     *
     * @param xml XML document stored by string.
     * @return Element
     */
    public static Element toElement(String xml) throws IOException, SAXException, ParserConfigurationException {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
        return toElement(inputStream);
    }

    /**
     * Parse standard XML to element structure, allow access with css selector.
     *
     * @param file XML document stored in a file.
     * @return JSONObject
     */
    public static Element toElement(File file) throws IOException, SAXException, ParserConfigurationException {
        try {
            InputStream fileInputStream = new FileInputStream(file);
            return toElement(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse standard XML to element structure, allow access with css selector.
     *
     * @param inputStream InputStream point to a XML document.
     * @return JSONObject
     */
    public static Element toElement(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        parseHandler handler = new parseHandler();
        parser.parse(inputStream, handler);
        return handler.datas;
    }

    public static class parseHandler extends DefaultHandler {

        private StringBuilder stringBuilder = new StringBuilder(1024);
        private Element currentChild = this.datas;
        protected Element datas = new Element(null, "root");

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            this.currentChild = this.currentChild.addChild(!TextUtils.isEmpty(localName) ? localName : qName);
            this.currentChild.addAttributes(attributes);
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            this.currentChild.value = this.stringBuilder.toString().trim();
            this.stringBuilder.setLength(0);
            this.currentChild = this.currentChild.parent;
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            this.stringBuilder.append(chars, start, length);
        }
    }

    public static class Elements extends ArrayList<Element> {

        private static final long serialVersionUID = (new Random()).nextLong();

        public String attr(String attrName) {
            return !this.isEmpty() ? this.get(0).attr(attrName) : "";
        }

        public String val() {
            return !this.isEmpty() ? this.get(0).val() : "";
        }

        public Elements find(String selector) {
            Elements elements = new Elements();

            for (Element element : this) {
                elements.addAll(element.find(selector));
            }
            return elements;
        }

        public Elements findChildren(String tagName) {
            Elements elements = new Elements();

            for (Element element : this) {
                elements.addAll(element.findChildren(tagName));
            }

            return elements;
        }

        public Elements findByAttribute(String key, String value) {
            Elements elements = new Elements();

            for (Element element : this) {
                elements.addAll(element.findByAttribute(key, value));
            }

            return elements;
        }
    }

    public static class Element {


        private static final String TAG = "Element";

        private static final String HOOK_START = "[";
        private static final String REGEX_HOOK_START = "\\[";
        private static final String REGEX_HOOK_END = "\\]";
        private static final String REGEX_DOT_STAR = ".*";
        private static final String REGEX_DOLLAR = "$";

        private static final String TO_STRING_SPACE = " ";
        private static final String TO_STRING_BRACKET_START = "<";
        private static final String TO_STRING_BRACKET_START_SLASH = "</";
        private static final String TO_STRING_BRACKET_END = ">";
        private static final String TO_STRING_BRACKET_END_SLASH = "/>";
        private static final String TO_STRING_NEW_LINE = "\n";
        private static final String TO_STRING_TABULATION = "\t";
        private static final String TO_STRING_DOUBLE_QUOTE = "\"";
        private static final String TO_STRING_EQUALS_QUOTE = "=\"";

        private static final String ATTRIBUTE_EQUALS = "=";
        private static final String ATTRIBUTE_NOT_EQUALS = "!=";
        private static final String ATTRIBUTE_CONTAINS = "~=";
        private static final String ATTRIBUTE_STARTS_WITH = "^=";
        private static final String ATTRIBUTE_ENDS_WITH = "$=";

        public String name;
        protected Element parent;
        private HashMap<String, String> attributes;
        private ArrayList<Element> children;
        protected String value;

        public Element(Element parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        protected void addAttributes(Attributes attributes) {
            int attributesLength = attributes.getLength();
            if (attributesLength > 0) {
                if (this.attributes == null) {
                    this.attributes = new HashMap<>();
                }
                for (int attributeIndex = 0; attributeIndex < attributesLength; attributeIndex++) {
                    this.attributes.put(attributes.getLocalName(attributeIndex), attributes.getValue(attributeIndex));
                }
            }
        }

        protected Element addChild(String childName) {
            if (this.children == null) {
                this.children = new ArrayList<>();
            }
            Element child = new Element(this, childName);
            this.children.add(child);
            return child;
        }

        public Element firstChild() {
            Element child = null;
            if (this.children != null) // if not null, there is at least one child
            {
                child = this.children.get(0);
            }
            return child;
        }

        public String attr(String attrName) {
            String value = null;
            if (this.attributes != null) {
                value = this.attributes.get(attrName);
            }
            if (value == null) {
                value = "";
            }
            return value;
        }

        public String attr(String key, String value) {
            return this.attributes.put(key, value);
        }

        public String val() {
            return this.value;
        }

        public Elements find(String selector) {
            long start = System.currentTimeMillis();
            Elements elements;
            if (TextUtils.isEmpty(selector)) {
                elements = new Elements();
                elements.add(this);
            } else if (selector.contains(Element.TO_STRING_BRACKET_END)) {
                elements = this.selectByArrow(selector);
            } else if (selector.contains(Element.HOOK_START)) {
                elements = this.selectByAttribute(selector, false);
            } else {
                elements = new Elements();
                if (this.children != null) {
                    for (Element element : this.children) {
                        if (element.name.equals(selector)) {
                            elements.add(element);
                        }
                        elements.addAll(element.find(selector));
                    }
                }
            }

            long end = System.currentTimeMillis();
            long duration = end - start;
            if (duration > 1) {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                StackTraceElement caller = stackTraceElements[3];
                String fullClassName = caller.getClassName();
                Log.w(Element.TAG, "find(" + selector + ") " + elements.size() + " elements in " +
                        duration + "ms called by " + fullClassName.substring(fullClassName.lastIndexOf(".") + 1) +
                        "." + caller.getMethodName());
            }
            return elements;
        }

        protected Elements findChildren(String tagName) {
            Elements elements;
            if (TextUtils.isEmpty(tagName)) {
                elements = new Elements();
                if (this.children != null) {
                    elements.addAll(this.children);
                }
            } else if (tagName.contains(Element.HOOK_START)) {
                elements = this.selectByAttribute(tagName, true);
            } else {
                elements = new Elements();
                if (this.children != null) {
                    for (Element element : this.children) {
                        if (element.name.equals(tagName)) {
                            elements.add(element);
                        }
                    }
                }
            }
            return elements;
        }

        protected boolean hasAttribute(String key, String value) {
            boolean hasAttribute;
            if (value == null) {
                hasAttribute = this.hasAttribute(key);
            } else {
                hasAttribute = (this.attributes != null) && value.equals(this.attributes.get(key));
            }
            return hasAttribute;
        }

        protected boolean matchesAttribute(String key, String valueExp) {
            boolean isMatching = false;
            if (valueExp == null) {
                isMatching = this.hasAttribute(key);
            } else if (this.attributes != null) {
                String value = this.attributes.get(key);
                isMatching = (value != null) && value.matches(valueExp);
            }

            return isMatching;
        }

        protected boolean hasAttribute(String key) {
            return (this.attributes != null) && this.attributes.containsKey(key);
        }

        protected Elements findByAttribute(String key) {
            Elements elements = new Elements();
            if (this.children != null) {
                for (Element element : this.children) {
                    if (element.attr(key) != null) {
                        elements.add(element);
                    }
                }
            }
            return elements;
        }

        protected Elements findByAttribute(String key, String value) {
            Elements elements = new Elements();
            if (value == null) {
                elements = this.findByAttribute(key);
            } else if (this.children != null) {
                for (Element element : this.children) {
                    if (value.equals(element.attr(key))) {
                        elements.add(element);
                    }
                }
            }
            return elements;
        }

        protected Elements selectByArrow(String tagName) {
            String[] tags = tagName.split(Element.TO_STRING_BRACKET_END);
            String currentTag = tags[0].trim();
            Elements elements = this.find(currentTag);

            for (int tagIndex = 1; tagIndex < tags.length; tagIndex++) {
                currentTag = tags[tagIndex].trim();
                elements = elements.findChildren(currentTag);
            }

            return elements;
        }

        protected Elements selectByAttribute(String selector, boolean isChild) {
            Elements elements;
            String[] tmpTagNames = selector.split(Element.REGEX_HOOK_START);
            ArrayList<String> attributeSelectors = new ArrayList<>();

            String currentTag = tmpTagNames[0].trim();
            if (isChild) {
                elements = this.findChildren(currentTag);
            } else {
                elements = this.find(currentTag);
            }

            for (String tmpTag : tmpTagNames) {
                currentTag = tmpTag.split(Element.REGEX_HOOK_END)[0].trim();
                attributeSelectors.add(currentTag);
            }

            Elements finalElements = new Elements();
            for (String attributeSelector : attributeSelectors) {
                String key;
                String valueExp = null;
                boolean isPositive = true;
                if (attributeSelector.contains(Element.ATTRIBUTE_NOT_EQUALS)) {
                    String[] tab = attributeSelector.split(Element.ATTRIBUTE_NOT_EQUALS);
                    key = tab[0];
                    valueExp = tab[1];
                    isPositive = false;
                } else if (attributeSelector.contains(Element.ATTRIBUTE_STARTS_WITH)) {
                    String[] tab = attributeSelector.split(Element.ATTRIBUTE_STARTS_WITH);
                    key = tab[0];
                    valueExp = tab[1] + Element.REGEX_DOT_STAR;
                } else if (attributeSelector.contains(Element.ATTRIBUTE_ENDS_WITH)) {
                    String[] tab = attributeSelector.split(Element.ATTRIBUTE_ENDS_WITH);
                    key = tab[0];
                    valueExp = Element.REGEX_DOT_STAR + tab[1] + Element.REGEX_DOLLAR;
                } else if (attributeSelector.contains(Element.ATTRIBUTE_CONTAINS)) {
                    String[] tab = attributeSelector.split(Element.ATTRIBUTE_CONTAINS);
                    key = tab[0];
                    valueExp = Element.REGEX_DOT_STAR + tab[1] + Element.REGEX_DOT_STAR;
                } else if (attributeSelector.contains(Element.ATTRIBUTE_EQUALS)) {
                    String[] tab = attributeSelector.split(Element.ATTRIBUTE_EQUALS);
                    key = tab[0];
                    valueExp = tab[1];
                } else {
                    key = attributeSelector;
                }

                for (Element element : elements) {
                    if (isPositive) {
                        if (element.matchesAttribute(key, valueExp)) {
                            finalElements.add(element);
                        }
                    } else if (!element.hasAttribute(key, valueExp)) {
                        finalElements.add(element);
                    }
                }
            }
            return finalElements;
        }

        @Override
        public String toString() {
            return this.toString(0);
        }

        protected String toString(int index) {
            int nextRound = index + 1;
            StringBuilder text = new StringBuilder(Element.TO_STRING_BRACKET_START).append(this.name);

            if (this.attributes != null) {
                for (String key : this.attributes.keySet()) {
                    text.append(Element.TO_STRING_SPACE).append(key).append(Element.TO_STRING_EQUALS_QUOTE).append(this.attributes.get(key)).append(Element.TO_STRING_DOUBLE_QUOTE);
                }
            }
            if ((this.children == null) && TextUtils.isEmpty(this.value)) {
                text.append(Element.TO_STRING_BRACKET_END_SLASH);
            } else {
                text.append(Element.TO_STRING_BRACKET_END);
                if (!TextUtils.isEmpty(this.value)) {
                    text.append(this.value);
                }
                if (this.children != null) {
                    for (Element element : this.children) {
                        text.append(Element.TO_STRING_NEW_LINE);
                        for (int i = 0; i < nextRound; i++) {
                            text.append(Element.TO_STRING_TABULATION);
                        }
                        text.append(element.toString(nextRound));
                    }
                }
                for (int i = 0; i < index; i++) {
                    text.append(Element.TO_STRING_TABULATION);
                }
                text.append(Element.TO_STRING_BRACKET_START_SLASH).append(this.name).append(Element.TO_STRING_BRACKET_END);
            }
            return text.append(Element.TO_STRING_NEW_LINE).toString();
        }
    }
}
