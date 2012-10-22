<!DOCTYPE html>
<html>
<head>
    <title>Performance Report</title>

    <script type="text/javascript" src="${resourcesDir}/d3.js"></script>
    <script type="text/javascript" src="${resourcesDir}/d3.chart.js"></script>

    <link type="text/css" rel="stylesheet" href="${resourcesDir}/box.css"/>
</head>
<body>
<div id="header">
    <h2>${title}</h2>
</div>
<div id="machineInformation">
    <p>
    <#list machineInfo as info>
        ${info}<br/>
    </#list>
    </p>
</div>
<div id="chart"></div>
<div id="labels">
<#list repositoryValuesMap?keys as repositoryName>
    <div class="label">${repositoryName}</div>
</#list>
</div>
<script type="text/javascript" src="${resourcesDir}/box.js"></script>
<script type="text/javascript">
    window.onload = function () {
        var w = 120, h = 500, m = [10, 50, 20, 50]; // top right bottom left

        var chart = d3.chart.box()
                .width(w - m[1] - m[3])
                .height(h - m[0] - m[2]);
        var globalMin = Infinity;
        var globalMax = -Infinity;
        var data = [];
        var series = [];
        var flatArray = [];
    <#list repositoryValuesMap?keys as repositoryName>
        <#list repositoryValuesMap[repositoryName] as value>
            series.push(${value});
            flatArray.push(${value});
        </#list>
        data.push(series);
        series = [];
    </#list>
        chart.domain([d3.min(flatArray) , d3.max(flatArray)]);
        var vis = d3.select("#chart").selectAll("svg")
                .data(data)
                .enter().append("svg")
                .attr("class", "box")
                .attr("width", w)
                .attr("height", h)
                .append("g")
                .attr("transform", "translate(" + m[3] + "," + m[0] + ")")
                .call(chart);
    };

    function iqr( k ) {
        return function( d, i ) {
            var q1 = d.quartiles[0], q3 = d.quartiles[2], iqr = (q3 - q1) * k, i = -1, j = d.length;
            while (d[++i] < q1 - iqr) {
            }
            while (d[--j] > q3 + iqr) {
            }
            return [i, j];
        };
    }
</script>
</body>
</html>
