<html>
	<head>
		<title>Performance Report</title>
	</head>
    <body>
        <h3>${timeUnit}</h3>
        <ul>
            <#list chartsMap?keys as testName>
            <li><a href="${chartsMap[testName]}" target="_blank">${testName}</a></li>
            </#list>
        </ul>
   </body>
</html>