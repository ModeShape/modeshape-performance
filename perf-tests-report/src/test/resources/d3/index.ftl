<!DOCTYPE html>
<html>
<head>
    <title>Performance Report</title>
</head>
<body>
<div id="header">
    <h2>Performance Report</h2>
</div>
<div id="reports">
  <ol>
<#list reportsMap?keys as reportName>
    <li><a href="${reportName}.html">${reportName}</a></li>
</#list>
  </ol>
</div>
</body>
</html>
