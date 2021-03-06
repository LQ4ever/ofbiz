<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<html xmlns="http://www.w3.org/1999/xhtml">

<#if locale?exists>
    <#assign initialLocale = locale.toString()>
<#else>
    <#assign initialLocale = "en">
</#if>    

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>${(decoratedContent.subcontent.title.render)!"CMS Site Generic Title (Set subcontent 'title' on your content!)"}</title>
    <link rel="shortcut icon" href="/images/ofbiz.ico" />
    <script language="javascript" src="/images/prototypejs/prototype.js" type="text/javascript"></script>
    <script language="javascript" src="/images/fieldlookup.js" type="text/javascript"></script>
    <script language="javascript" src="/images/selectall.js" type="text/javascript"></script>
    <script language="javascript" src="/images/calendarDateSelect/calendar_date_select.js" type="text/javascript"></script>
    <script language="javascript" src="<@ofbizContentUrl>/images/calendarDateSelect/locale/${(parameters.userLogin.lastLocale?substring(0,2))!initialLocale?substring(0,2)!'en'}.js</@ofbizContentUrl>" type="text/javascript"></script>
    <link rel="stylesheet" href="/images/ecommain.css" type="text/css"/>
    <link rel="stylesheet" href="/ecommerce/images/blog.css" type="text/css"/>
    <link rel="stylesheet" href="/content/images/contentForum.css" type="text/css"/>

    <meta name="description" content="${(decoratedContent.subcontent.metaDescription.render)!}"/>
    <meta name="keywords" content="${(decoratedContent.subcontent.metaKeywords.render)!}"/>
</head>
<body>
