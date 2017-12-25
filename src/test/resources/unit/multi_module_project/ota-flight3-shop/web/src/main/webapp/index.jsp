<%--

    Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved

    This file is subject to the terms and conditions defined in
    file 'LICENSE.txt', which is part of this source code package.

--%>
<%@ page import="com.allegiant.util.property.Property" %>
<%@ page import="com.allegiant.util.property.PropertySet" %>
<%@ page import="java.util.List" %>
<%@ page import="com.allegiant.util.property.PropertySetEnumLoader" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%--
  ~ /*
  ~  * Copyright (C) 2017 Allegiant Travel Company - All Rights Reserved
  ~  *
  ~  * This file is subject to the terms and conditions defined in
  ~  * file 'LICENSE.txt', which is part of this source code package.
  ~  */
  ~
  --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${project.artifactId}</title>
</head>
<body>
<h1>${project.artifactId}</h1>

<h2>Version: ${project.version}</h2>

<h2>Build DateTime: ${build.timestamp}</h2>

<h2>Source Commit: ${build.number}</h2>
<br/>

<h2>Web Service(s):</h2>
<ul>
<li>${project.artifactId}</li>
<li><a href="api/swagger.json">Swagger JSON</a></li>
<li><a href="api/swagger.yaml">Swagger YAML</a></li>
</ul>


<%
    List<PropertySet> propertySets = PropertySetEnumLoader.load();
    if (propertySets != null) {
%>
<br/>

<h2>Configuration Information</h2>
<table border=”1″>
    <tr>
        <td><b>System Property Name</b></td>
        <td><b>System Property Value</b></td>
        <td><b>ENUM Value Name</b></td>
    </tr>

    <%
        for (PropertySet propertySet : propertySets) {
    %>
    <tr bgcolor="#d3d3d3">
        <td colspan="3"><h4><%= propertySet.getName() %>
        </h4></td>
    </tr>
    <%
        for (Property p : propertySet.getProperties()) {
    %>
    <tr>
        <td><%= p.getSystemPropertyName() %>
        </td>
        <% if (p.getIsValueDefault()) { %>
        <td>NOT SET, DEFAULTED TO: <%= p.getValueDefault() %>
        </td>
        <% } else { %>
        <td><%= p.getValue() %>
        </td>
        <% } %>
        <td><%= p.getEnumValue() %>
        </td>
    </tr>
    <% } %>
    <% } %>
</table>
<% } %>

</body>
</html>
