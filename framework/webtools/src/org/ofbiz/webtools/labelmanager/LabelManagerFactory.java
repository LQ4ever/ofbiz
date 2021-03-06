/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ofbiz.webtools.labelmanager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastList;

import org.ofbiz.base.component.ComponentConfig;
import org.ofbiz.base.component.ComponentConfig.ClasspathInfo;
import org.ofbiz.base.util.FileUtil;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.owasp.esapi.errors.EncodingException;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class LabelManagerFactory {

    public static final String module = LabelManagerFactory.class.getName();
    public static final String resource = "WebtoolsUiLabels";
    public static final String keySeparator = "#";

    protected static Set<String> componentNamesFound = null;
    protected static Map<String, LabelFile> filesFound = null;

    protected Map<String, LabelInfo> labels = new TreeMap<String, LabelInfo>();
    protected Set<String> localesFound = new TreeSet<String>();
    protected List<LabelInfo> duplicatedLocalesLabelsList = FastList.newInstance();

    public static synchronized LabelManagerFactory getInstance() throws GeneralException, IOException {
        if (componentNamesFound == null) {
            loadComponentNames();
        }
        if (filesFound == null) {
            loadLabelFiles();
        }
        return new LabelManagerFactory();
    }

    protected LabelManagerFactory() {
    }

    protected static void loadComponentNames() {
        componentNamesFound = new TreeSet<String>();
        Collection<ComponentConfig> componentConfigs = ComponentConfig.getAllComponents();
        for (ComponentConfig componentConfig : componentConfigs) {
            componentNamesFound.add(componentConfig.getComponentName());
        }
        componentNamesFound.add("shark");
    }

    protected static void loadLabelFiles() throws IOException {
        filesFound = new TreeMap<String, LabelFile>();
        List<ClasspathInfo> cpInfos = ComponentConfig.getAllClasspathInfos();
        for (ClasspathInfo cpi : cpInfos) {
            if ("dir".equals(cpi.type)) {
                String configRoot = cpi.componentConfig.getRootLocation();
                configRoot = configRoot.replace('\\', '/');
                if (!configRoot.endsWith("/")) {
                    configRoot = configRoot + "/";
                }
                String location = cpi.location.replace('\\', '/');
                if (location.startsWith("/")) {
                    location = location.substring(1);
                }
                List<File> resourceFiles = FileUtil.findXmlFiles(configRoot + location, null, "resource", null);
                for (File resourceFile : resourceFiles) {
                    filesFound.put(resourceFile.getName(), new LabelFile(resourceFile, cpi.componentConfig.getComponentName()));
                }
            }
        }
        List<File> resourceFiles = FileUtil.findXmlFiles(System.getProperty("ofbiz.home") + "/specialpurpose/shark/config", null, "resource", null);
        for (File resourceFile : resourceFiles) {
            filesFound.put(resourceFile.getName(), new LabelFile(resourceFile, "shark"));
        }
    }

    public void findMatchingLabels(String component, String fileName, String key, String locale) throws MalformedURLException, SAXException, ParserConfigurationException, IOException, EncodingException, GeneralException {
        if (UtilValidate.isEmpty(component) && UtilValidate.isEmpty(fileName) && UtilValidate.isEmpty(key) && UtilValidate.isEmpty(locale)) {
            // Important! Don't allow unparameterized queries - doing so will result in loading the entire project into memory
            return;
        }
        for (LabelFile fileInfo : filesFound.values()) {
            if (UtilValidate.isNotEmpty(component) && !component.equals(fileInfo.componentName)) {
                continue;
            }
            if (UtilValidate.isNotEmpty(fileName) && !fileName.equals(fileInfo.getFileName())) {
                continue;
            }
            Document resourceDocument = UtilXml.readXmlDocument(fileInfo.file.toURI().toURL(), false);
            Element resourceElem = resourceDocument.getDocumentElement();
            String labelKeyComment = "";
            for (Node propertyNode : UtilXml.childNodeList(resourceElem.getFirstChild())) {
                if (propertyNode instanceof Element) {
                    Element propertyElem = (Element) propertyNode;
                    String labelKey = StringUtil.defaultWebEncoder.canonicalize(propertyElem.getAttribute("key"));
                    String labelComment = "";
                    for (Node valueNode : UtilXml.childNodeList(propertyElem.getFirstChild())) {
                        if (valueNode instanceof Element) {
                            Element valueElem = (Element) valueNode;
                            String localeName = valueElem.getAttribute("xml:lang");
                            String labelValue = StringUtil.defaultWebEncoder.canonicalize(UtilXml.nodeValue(valueElem.getFirstChild()));
                            LabelInfo label = labels.get(labelKey + keySeparator + fileInfo.getFileName());

                            if (UtilValidate.isEmpty(label)) {
                                label = new LabelInfo(labelKey, labelKeyComment, fileInfo.getFileName(), localeName, labelValue, labelComment);
                                labels.put(labelKey + keySeparator + fileInfo.getFileName(), label);
                            } else {
                                if (label.setLabelValue(localeName, labelValue, labelComment, false)) {
                                    duplicatedLocalesLabelsList.add(label);
                                }
                            }
                            localesFound.add(localeName);
                            labelComment = "";
                        } else if (valueNode instanceof Comment) {
                            labelComment = labelComment + StringUtil.defaultWebEncoder.canonicalize(valueNode.getNodeValue());
                        }
                    }
                    labelKeyComment = "";
                } else if (propertyNode instanceof Comment) {
                    labelKeyComment = labelKeyComment + StringUtil.defaultWebEncoder.canonicalize(propertyNode.getNodeValue());
                }
            }
        }
    }

    public LabelFile getLabelFile(String fileName) {
        return filesFound.get(fileName);
    }

    public Map<String, LabelInfo> getLabels() {
        return labels;
    }

    public Set<String> getLocalesFound() {
        return localesFound;
    }

    public static Collection<LabelFile> getFilesFound() {
        return filesFound.values();
    }

    public static Set<String> getComponentNamesFound() {
        return componentNamesFound;
    }

    public Set<String> getLabelsList() {
        return labels.keySet();
    }

    public int getDuplicatedLocalesLabels() {
        return duplicatedLocalesLabelsList.size();
    }

    public List<LabelInfo> getDuplicatedLocalesLabelsList() {
        return duplicatedLocalesLabelsList;
    }

    public int updateLabelValue(List<String> localeNames, List<String> localeValues, List<String> localeComments, LabelInfo label, String key, String keyComment, String fileName) {
        int notEmptyLabels = 0;
        for (int i = 0; i < localeNames.size(); i++) {
            String localeName = localeNames.get(i);
            String localeValue = localeValues.get(i);
            String localeComment = null;
            if (UtilValidate.isNotEmpty(localeComments)) localeComment = localeComments.get(i);
            if (UtilValidate.isNotEmpty(localeValue) || UtilValidate.isNotEmpty(localeComment)) {
                if (label == null) {
                    try {
                        label = new LabelInfo(key, keyComment, fileName, localeName, localeValue, localeComment);
                        labels.put(key + keySeparator + fileName, label);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    label.setLabelKeyComment(keyComment);
                }
                label.setLabelValue(localeName, localeValue, localeComment, true);
                notEmptyLabels++;
            }
        }
        return notEmptyLabels;
    }
}
