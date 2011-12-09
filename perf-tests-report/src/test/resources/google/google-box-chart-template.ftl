<html>
	<head>
		<title>Performance Report</title>
	</head>
    <body>
        <h3>${title}</h3>
        <ul>
            <#list chartUrlsMap?keys as testName>
            <li><a href="${chartUrlsMap[testName]}" target="_blank">${testName}</a></li>
            </#list>
        </ul>
   </body>
</html>