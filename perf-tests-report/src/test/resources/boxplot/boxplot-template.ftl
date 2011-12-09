<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
    <link rel="stylesheet" type="text/css" href="boxplot.css">
    <script src="boxplot.js"></script>

    <style type="text/css">
        #header {
            text-align: center;
            width: 823px;
        }

        #header h2 {
            text-align: left;
            margin-top: 5px;
        }

        div.boxplot-container {
            border: none;
            padding-bottom: 30px;
            position: absolute;
            top: 150px;
        }

        <#assign leftMargin = 100>
        <#list repositoryValuesMap?keys as repositoryName>
                div#plot${repositoryName_index} {
                    left: ${leftMargin + "px"}
                }

                div#plotLabel${repositoryName_index} {
                    position: absolute;
                    left: ${leftMargin + "px"};
                    top:  700px;
                }
            <#assign leftMargin = leftMargin + 200>
        </#list>
    </style>
    <title>Box Plot</title>
</head>
<body>
<div id="header">
    <h2>${title}</h2>
</div>

<#list repositoryValuesMap?keys as repositoryName>
<div id="${"plot" + repositoryName_index}" class="boxplot-container">
    <div id="contentoverall"
         style="height: 200px; width: 56px; border-width: medium 1px medium medium; border-style: none dotted none none; border-color: -moz-use-text-color; -moz-border-top-colors: none; -moz-border-right-colors: none; -moz-border-bottom-colors: none; -moz-border-left-colors: none; -moz-border-image: none;">
    </div>
</div>
<div id="${"plotLabel" + repositoryName_index}">
    <h4>${repositoryName}</h4>
</div>
</#list>

<script type="text/javascript">
    <#list repositoryValuesMap?keys as repositoryName>
    var dataArray${repositoryName_index} = new Array();
        <#list repositoryValuesMap[repositoryName] as value>
        dataArray${repositoryName_index}.push(Number(${value}));
        </#list>
    createBoxPlot(dataArray${repositoryName_index}, 300, "${"plot" + repositoryName_index}");
    </#list>
</script>
</body>
</html>