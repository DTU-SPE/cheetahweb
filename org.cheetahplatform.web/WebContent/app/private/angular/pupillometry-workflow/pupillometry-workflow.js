angular
    .module('cheetah.PupillometryWorkflow', ['ngRoute'])
    .controller('PupillometryWorkflowCtrl', function ($scope, $http) {
        $scope.percentiles = [];
        $scope.currentTime = -1; //start at beginning of session
        $scope.threshold = 5000;
        $scope.slidingWindowDurations = [];
        $scope.colors = ['blue', 'red', 'green', 'black', 'yellow'];
        $scope.displayedPupils = {average: true};
        $scope.interactions = [];
        $scope.interactionList = [];
        $scope.interactionTempObject = {};
        $scope.zoomen = {};
        $scope.zoomFactorForServer = 1;
        $scope.radius = 15;
        $scope.instersectCheck = [];
        $scope.useCircles = true;
        $scope.showDurationTime = false;



        $('.cancel-click label').on('click', function(e) {
            e.stopPropagation();
        });

        var div = d3.select("body").append("div")
            .attr("class", "tooltip")
            .style("opacity", 0);
        $('.cheetah-pupillometry-visualization input').on('click', function (event) {
            event.stopPropagation();
        });

        $scope.selectSymbols = [
            {text: "Use Symbols for Events", value: true},
            {text: "Use Lines for Events", value: false}
        ];

        $scope.zoomFactor = [
            {text: "10 seconds", value: 1},
            {text: "20 seconds", value: 2},
            {text: "30 seconds", value: 3},
            {text: "40 seconds", value: 4},
            {text: "50 seconds", value: 5},
            {text: "60 seconds", value: 6},
            {text: "90 seconds", value: 9},
            {text: "2 minuntes", value: 12},
            {text: "3 minuntes", value: 18},
            {text: "4 minuntes", value: 24},
            {text: "5 minuntes", value: 30},
            {text: "6 minuntes", value: 36},
            {text: "7 minuntes", value: 42},
            {text: "8 minuntes", value: 48},
            {text: "9 minuntes", value: 54},
            {text: "10 minuntes", value: 60},
            {text: "20 minuntes", value: 120}
        ];


        $scope.showDurationTime = true;
        $scope.showDuration={
            "show":true
        }

        if (localStorage.getItem("circleOrLine") === undefined || localStorage.getItem("circleOrLine") === "true") {
            $scope.selectCircleOrLines = $scope.selectSymbols[0];
            $scope.useCircles = true;
        } else {
            $scope.selectCircleOrLines = $scope.selectSymbols[1];
            $scope.useCircles = false;
        }

        if (localStorage.getItem("showDuration") === undefined || localStorage.getItem("showDuration") === "false") {
            $scope.showDurationTime = false;
            $scope.showDuration={
                "show":false
            }
        } else {
            $scope.showDurationTime = true;
            $scope.showDuration={
                "show":true
            }
        }

        if (localStorage.getItem("zoomFactorStored") === undefined || localStorage.getItem("zoomFactorStored") === null) {
            $scope.zoomen = $scope.zoomFactor[0];
            $scope.zoomFactorForServer = 1;
        } else {
            var tempZoomFactorOutOfStorage = JSON.parse(localStorage.getItem("zoomFactorStored"));
            $scope.zoomen = $scope.zoomFactor.filter(function (factor) {
                return factor.value === tempZoomFactorOutOfStorage.value;
            })[0];
            $scope.zoomFactorForServer = JSON.parse(localStorage.getItem("zoomFactorStored")).value;
        }

        if (localStorage.getItem("interactionList") !== undefined) {
            $scope.interactionTempObject = JSON.parse(localStorage.getItem("interactionList"));
        } else {
            $scope.interactionTempObject = {
                "AND": true,
                "XOR": true,
                "ACTIVITY": true,
                "SEQUENCE_FLOW": true,
                "START_EVENT": true,
                "END_EVENT": true
            };
        }

        $("#percentile, #slidingWindow, #dropdownMenu1").attr("disabled", "disabled");
        for (var i = 20; i > 0; i--) {
            $scope.slidingWindowDurations.push(i * 100);
        }

        $scope.showDurationOnOff = function () {
            $scope.showDurationTime=$scope.showDuration.show;
            localStorage.setItem("showDuration", $scope.showDuration.show);
            $scope.renderData();
        }

        $scope.getInteractions = function () {
            $scope.interactionList = [];
            $.each($scope.interactionTempObject, function (interaction, displayed) {
                if (!displayed) {
                    return;
                }
                $scope.interactionList.push(interaction);
            });
            if ($scope.interactionList.length > 0) {
                $http.get('../../private/interactionsfiltered?ppmInstanceId=' + cheetah.processInstance + "&interactions=" + $scope.interactionList).success(function (data) {
                    $scope.interactions = data;
                    $scope.renderData();
                });
            } else {
                $scope.interactions = [];
                $scope.renderData();
            }
            localStorage.setItem("interactionList", JSON.stringify($scope.interactionTempObject));
        };

        $scope.updateAdditionalInformationLine = function () {
            var line = $scope.pupillometryLines.filter(function (line) {
                return line.label === $scope.additionalInformationLineLabel;
            })[0];
            if (line !== undefined) {
                initializePercentiles(line);
            }

            $scope.percentile = "";
            $scope.slidingWindowDuration = "";
            refreshData($scope.startTime);

            if ($scope.additionalInformationLineLabel.length === 0) {
                $("#percentile, #slidingWindow, #dropdownMenu1").attr("disabled", "disabled");
            } else {
                $("#percentile, #slidingWindow, #dropdownMenu1").removeAttr("disabled");
            }
        };

        $scope.zoomIn = function () {
            $scope.zoomFactorForServer = $scope.zoomen.value;
            localStorage.setItem("zoomFactorStored", JSON.stringify($scope.zoomen));
            refreshData($scope.startTime);
        };

        $scope.selectSymbol = function () {
            $scope.useCircles = $scope.selectCircleOrLines.value;
            localStorage.setItem("circleOrLine", $scope.selectCircleOrLines.value);
            $scope.renderData();
        };

        $scope.renderData = function () {
            var data;
            for (var k = 0; k < $scope.pupillometryData.length; k++) {
                if ($scope.pupillometryData[k].entries.length > 0) {
                    data = $scope.pupillometryData[k];
                    break;
                }
            }

            if (data === undefined || $scope.pupillometryData[0].percentiles === undefined) {
                $("#pupillometry").hide();
                $("#pupillometry-no-data").show();
                $scope.updatingData = false;
                $('.loading').hide();
            } else {
                $("#pupillometry").show();
                $("#pupillometry-no-data").hide();

                var entries = data.entries;
                var sessionStartTimestamp = data.sessionStartTimestamp;
                var startTime = entries[0].timestamp;
                var endTime = entries[entries.length - 1].timestamp;
                $scope.startTime = startTime;
                $scope.endTime = endTime;
                $scope.currentTime = startTime;
                $scope.sessionStartTimestamp = sessionStartTimestamp;
                $scope.sessionEndTimeStamp = data.sessionEndTimeStamp;
                cheetah.sessionStartTimestamp = sessionStartTimestamp;

                var availableWidth = $('#pupillometry').width();
                var availableHeight = $('#pupillometry').parent().height() - $('#pupillometry-controls').height() - $('.cheetah-pupillometry-visualization').height() - 40;
                $scope.marginLeft = 60;

                var margins = [0, 30, 30, $scope.marginLeft];
                var width = availableWidth - margins[1] - margins[3];
                var height = availableHeight - margins[0] - margins[2];

                $scope.diagramWidth = width;

                var x = d3.scale.linear().domain([startTime - sessionStartTimestamp, endTime - sessionStartTimestamp]).range([0, width]);
                $scope.x = x;
                var yMax = data.percentiles[99];
                var yMin = yMax - 1;
                var y = d3.scale.linear().domain([yMin, yMax]).range([height, 0]);
                $scope.y = y;
                $scope.yMin = yMin;
                $scope.yMax = yMax;

                var createLine = function (dataToRender, key) {
                    return d3.svg.line().x(function (data) {
                        return x(data.timestamp - sessionStartTimestamp);
                    }).y(function (data) {
                        return y(data[key]);
                    })(dataToRender);
                };
                $scope.createLine = createLine;

                $("#pupillometry").html('');
                // Add an SVG element with the desired dimensions and margin.
                var graph = d3.select("#pupillometry").append("svg:svg")
                    .attr("width", width + margins[1] + margins[3])
                    .attr("height", height + margins[0] + margins[2]).on("click", function () {
                        var position = d3.mouse(this);
                        jumpToMousePosition(position);
                    })
                    .append("svg:g")
                    .attr("transform", "translate(" + margins[3] + "," + margins[0] + ")");
                $scope.graph = graph;

                var formatTime = d3.time.format("%M:%S");
                var formatMinutes = function (d) {
                    return formatTime(new Date(d / 1000));
                };

                var ticks = Math.floor((endTime - startTime) / (1000 * 1000));
                if (ticks > 20 && ticks <= 60) {
                    ticks = Math.floor(ticks / 2)
                } else if (ticks > 60 && ticks <= 100) {
                    ticks = Math.floor(ticks / 4)
                } else if (ticks > 100 && ticks <= 150) {
                    ticks = Math.floor(ticks / 10)
                } else if (ticks > 150 && ticks <= 250) {
                    ticks = Math.floor(ticks / 20)
                } else if (ticks > 250) {
                    ticks = Math.floor(ticks / 30)
                }

                var xAxis = d3.svg.axis().scale(x).tickSize(-height).tickFormat(formatMinutes).ticks(ticks);
                // Add the x-axis
                graph.append("svg:g").attr("class", "x axis").attr("transform", "translate(0," + height + ")").call(xAxis);

                // create left yAxis
                var yAxisLeft = d3.svg.axis().scale(y).ticks(4).orient("left");
                // Add the y-axis to the left
                graph.append("svg:g").attr("class", "y axis").attr("transform", "translate(-25,0)").call(yAxisLeft);
                graph.append("clipPath").attr("id", "line-clip").append('rect').attr('x', 0).attr('y', 0).attr('height', height).attr('width', width);

                $.each($scope.pupillometryData, function (index, currentData) {
                    var colorNumber = index % $scope.colors.length;
                    var color = $scope.colors[colorNumber];
                    currentData.color = color;
                    if (!currentData.selected) {
                        return;
                    }

                    $.each($scope.displayedPupils, function (pupil, displayed) {
                        if (!displayed) {
                            return;
                        }

                        var path = createLine(currentData.entries, pupil);
                        var strokeClass = 'cheetah-pupillometry-line-average-pupil';
                        if (pupil === 'leftPupil') {
                            strokeClass = 'cheetah-pupillometry-line-left-pupil';
                        } else if (pupil === 'rightPupil') {
                            strokeClass = 'cheetah-pupillometry-line-right-pupil';
                        }

                        graph.append("svg:path").attr("d", path).classed([color, strokeClass].join(' '), true).attr("id", currentData.id).attr("clip-path", "url(#line-clip)");
                    });
                });

                var points = [];
                $.each($scope.interactions, function (t, interaction) {
                    var relativeTime = interaction.timestamp - cheetah.sessionStartTimestamp;
                    var relativeTimeInSeconds = relativeTime / 1000 / 1000;
                    var minutes = Math.floor(relativeTimeInSeconds / 60);
                    if (minutes < 10) {
                        minutes = "0" + minutes;
                    }
                    var seconds = Math.floor(relativeTimeInSeconds) % 60;
                    if (seconds < 10) {
                        seconds = "0" + seconds;
                    }
                    //console.log(x(timestampOfCreation));
                    var timestampOfCreation = interaction.timestamp - sessionStartTimestamp;

                    var interactionStartTime = null;

                    if (interaction.startTime != null) {
                        interactionStartTime = x(interaction.startTime - sessionStartTimestamp);
                    }

                    points.push({
                        xPos: x(timestampOfCreation),
                        xStartPos: interactionStartTime,
                        timestamp: timestampOfCreation,
                        yPos: y,
                        workflowElement: interaction.workflowElement,
                        type: interaction.type,
                        text: interaction.typeToDisplay + " " + interaction.workflowElement,
                        duration: (interaction.timestamp-interaction.startTime)/1000/1000
                    });
                });
                $scope.intersectCheck = [];

                var textDummy =$("<div>").css('display','inline').appendTo($(document.body)).text(" 010000 sec");
                var textWidth=textDummy.width();
                textDummy.remove();
                if ($scope.useCircles === true) {
                    $.each(points, function (t, point) {
                        var yActPos = height - 34;
                        var listLength = $scope.intersectCheck.length;
                        if(point.xPos>=0) {
                            while (true) {
                                if (listLength === 0) {
                                    break;
                                }
                                if ((point.xPos - $scope.intersectCheck[listLength - 1].onX > (2 * $scope.radius))) {
                                    break;
                                }
                                if ((point.xPos - $scope.intersectCheck[listLength - 1].onX < (2 * $scope.radius)) && $scope.intersectCheck[listLength - 1].onY == yActPos) {
                                    yActPos = yActPos - (2 * $scope.radius);

                                    listLength = $scope.intersectCheck.length;
                                } else {
                                    listLength = listLength - 1;
                                }
                            }

                            $scope.intersectCheck.push({onX: point.xPos, onY: yActPos});
                            var containerSize = 2 * $scope.radius;
                            var container = graph.append('g').attr('transform', 'translate(' + point.xPos + ',' + yActPos + ')').attr("width", containerSize).attr("height", containerSize).on("mouseover", function (d) {
                                    div.transition()
                                        .duration(200)
                                        .style("opacity", 1);
                                    div.html(point.text + "<br/>")
                                        .style("left", (this.getBoundingClientRect().left) + "px")
                                        .style("top", (this.getBoundingClientRect().top - 20) + "px");
                                })
                                .on("mouseout", function (d) {
                                    div.transition()
                                        .duration(500)
                                        .style("opacity", 0);
                                });
                            container.append("image").attr("xlink:href", getMainSymbol(point)).attr("x", 0).attr("y", 0).attr("width", containerSize).attr("height", containerSize);
                            container.append("image").attr("xlink:href", getAdditionalSymbol(point)).attr("x", containerSize * (1 / 2)).attr("y", containerSize * (1 / 2)).attr("width", containerSize * (1 / 2)).attr("height", containerSize * (1 / 2));
                        }
                    });
                } else {
                    var yActPos = height - 29;
                    var containerSize = 2 * $scope.radius;
                    var dxValue = "0.5em";
                    $.each(points, function (t, point) {
                        if (point.xPos > 0) {
                            graph.append("line").attr("x1", point.xPos).attr("y1", (height - $scope.radius)).attr("x2", point.xPos).attr("y2", 0).attr("stroke-width", 2).attr("stroke", "black");
                            var shift = point.xPos + 12;
                            if ($scope.showDurationTime === true &&point.xStartPos != null) {
                                var startCreate = point.xStartPos;
                                if (startCreate < 0) {
                                    startCreate = 0;
                                }else{
                                    graph.append("line").attr("x1", startCreate).attr("y1", (height - $scope.radius)).attr("x2", startCreate).attr("y2", 0).attr("stroke-width", 2).attr("stroke", "black");
                                }

                                graph.append("rect").attr("x", startCreate).attr("y", 0).attr("width", point.xPos - startCreate).attr("height", (height - $scope.radius)).attr("fill", "yellow").attr("opacity", 0.25);


                                if(point.xPos - startCreate<textWidth){
                                    graph.append('text').text("Duration: " + point.duration.toFixed(2) + "sec").attr('fill', "blue").attr("transform", "translate(" + (startCreate +  (point.xPos - startCreate)/2) + "," + (height - 100) + ") rotate(-90)");
                                }else {
                                    var textContainer = graph.append('g').attr("transform", "translate("+ (startCreate + 5)+","+(height - 100)+")");
                                    var text =  textContainer.append('text');
                                    text.append("tspan").text("Duration: ").attr('fill', "blue");
                                    text.append("tspan").attr("dy", 1 + "em").attr("x", 0).text(point.duration.toFixed(2)+ " sec").attr('fill', "blue");
                                }
                            }

                            var temp = graph.append('text').text(point.text)
                                .attr('fill', "black")
                                .attr("transform", "translate(" + shift + "," + (height - $scope.radius * 2) + ") rotate(-90)");
                            var container = graph.append('g').attr('transform', 'translate(' + (point.xPos - $scope.radius) + ',' + yActPos + ')').attr("width", containerSize).attr("height", containerSize);
                            container.append("image").attr("xlink:href", getMainSymbol(point)).attr("x", 0).attr("y", 0).attr("width", containerSize).attr("height", containerSize);
                            container.append("image").attr("xlink:href", getAdditionalSymbol(point)).attr("x", containerSize * (1 / 2)).attr("y", containerSize * (1 / 2)).attr("width", containerSize * (1 / 2)).attr("height", containerSize * (1 / 2));
                        }
                    });
                }
                $scope.updatingData = false;
                $('.loading').hide();
            }
        };

        var getAdditionalSymbol = function (interaction) {
            var type = interaction.type;
            if (type.startsWith("CREATE")) {
                return "./images/add.svg";
            } else if (type.startsWith("DELETE")) {
                return "./images/del.svg";
            } else if (type.startsWith("MOVE")) {
                return "./images/move.svg";
            } else if (type.startsWith("RENAME")) {
                return "./images/edit.svg";
            } else {
                return "./images/del.svg";
            }
        };

        var getMainSymbol = function (interaction) {
            if (interaction.workflowElement.startsWith("XOR")) {
                return "./images/xor-gw.svg";
            } else if (interaction.workflowElement.startsWith("AND")) {
                return "./images/and-gw.svg";
            } else if (interaction.workflowElement.startsWith("Activity")) {
                return "./images/task.svg";
            } else if (interaction.workflowElement.startsWith("Sequence")) {
                return "./images/seq.svg";
            } else if (interaction.workflowElement.startsWith("Start")) {
                return "./images/start-ev.svg";
            } else if (interaction.workflowElement.startsWith("End")) {
                return "./images/end-ev.svg";
            } else {
                return "./images/xor-gw.svg";
            }
        };


        function refreshData(time, calllback) {
            if ($scope.updatingData === true) {
                return;
            }

            if (time === undefined) {
                time = -1;
            }

            time = Math.floor(time);
            var slidingWindowDuration = -1;
            var tmpWindow = $scope.slidingWindowDuration;
            if (tmpWindow != null && tmpWindow.length > 0) {
                slidingWindowDuration = tmpWindow * 1000;
            }
            $scope.updatingData = true;

            //assemble url depending on whether we are analyzing a ppm instance or files
            var url = '../../private/pupillometry?';
            if (cheetah.processInstance) {
                url = url + 'ppmInstance=' + cheetah.processInstance;
            } else {
                url = url + 'files=' + encodeURIComponent(cheetah.files);
            }
            $http.get(url + '&start=' + time + '&slidingWindowDuration=' + slidingWindowDuration + "&zoom=" + $scope.zoomFactorForServer).success(function (pupillometryData) {
                //preserve the selection of lines, #509
                if ($scope.pupillometryLines) {
                    var selectedLines = {};
                    $.each($scope.pupillometryLines, function (index, line) {
                        selectedLines[line.id] = line.selected;
                    });
                    $.each(pupillometryData, function (index, line) {
                        line.selected = selectedLines[line.id];
                    });
                } else {
                    //select all lines by default
                    $.each(pupillometryData, function (index, line) {
                        line.selected = true;
                    });
                }

                $scope.pupillometryLines = pupillometryData;
                $scope.pupillometryData = pupillometryData;

                $scope.renderData();
                if (calllback) {
                    calllback();
                }
            });
        }

        function drawLine(pupillometryLine) {
            $scope.graph.append("svg:path").attr("d", $scope.createLine(pupillometryLine.entries, 'average')).attr("class", pupillometryLine.color).attr("id", pupillometryLine.id);
        }

        $scope.setSelection = function (line, event) {
            //prevent multiple calls when clicking on check box
            var target = event.target;
            if (target.type !== 'checkbox') {
                line.selected = !line.selected;
            }

            if (!line.selected) {
                $('#pupillometry').find("#" + line.id).remove();
            } else {
                drawLine(line);
            }
        };

        $scope.jumpToNextTimeframe = function () {
            jumpToMousePosition([Number.MAX_VALUE, 0]);
        };

        $scope.jumpToPreviousTimeframe = function () {
            if ($scope.sessionStartTimestamp - $scope.startTime === 0) {
                BootstrapDialog.alert({
                    title: 'Start Reached',
                    message: 'You have already reached the start of the session.'
                });
            } else {
                jumpToMousePosition([0, 0]);
            }
        };

        function jumpToMousePosition(position) {
            var relativeToChart = position[0] - $scope.marginLeft;
            var frameDuration = ($scope.endTime - $scope.startTime);
            var newTime = 0;
            if (($scope.endTime + frameDuration) > $scope.sessionEndTimeStamp && position[0] !== 0) {
                newTime = $scope.sessionEndTimeStamp - frameDuration;
                BootstrapDialog.alert({
                    title: 'End Reached',
                    message: 'You have already reached the end of the session.'
                });
            } else if (0 <= relativeToChart && relativeToChart <= $scope.diagramWidth) {
                var percent = relativeToChart / $scope.diagramWidth;
                newTime = percent * frameDuration + $scope.startTime;
            } else if (relativeToChart < 0) {
                newTime = Math.max($scope.sessionStartTimestamp, $scope.startTime - frameDuration);
            } else {
                newTime = Math.min($scope.endTime + 2 * $scope.threshold, $scope.sessionEndTimeStamp);
            }
            refreshData(newTime);
        }

        $scope.updateSlidingWindow = function (newDuration) {
            $('.loading').show();
            refreshData($scope.startTime);
        };

        $scope.interactionList = [];
        refreshData(-1, function () {
            $.each($scope.interactionTempObject, function (interaction, displayed) {
                if (!displayed) {
                    return;
                }
                $scope.interactionList.push(interaction);
            });
            if ($scope.interactionList.length > 0) {
                $http.get('../../private/interactionsfiltered?ppmInstanceId=' + cheetah.processInstance + "&interactions=" + $scope.interactionList).success(function (data) {
                    $scope.interactions = data;
                    $scope.renderData();
                });
            } else {
                $scope.interactions = [];
                $scope.renderData();
            }
        });
    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/pupillometry-workflow/pupillometry-workflow.htm',
            controller: 'PupillometryWorkflowCtrl'
        });
});
