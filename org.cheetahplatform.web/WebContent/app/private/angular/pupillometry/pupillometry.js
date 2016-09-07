angular
    .module('cheetah.Pupillometry', ['ngRoute'])
    .controller('PupillometryCtrl', function ($scope, $http) {
        $scope.percentiles = [];
        $scope.currentTime = -1; //start at beginning of session
        $scope.threshold = 5000;
        $scope.slidingWindowDurations = [];
        $scope.colors = ['blue', 'red', 'green', 'black', 'yellow'];
        $scope.displayedPupils = {average: true};

        $('.cheetah-pupillometry-visualization input').on('click', function (event) {
            event.stopPropagation();
        });

        $("#percentile").attr("disabled", "disabled");
        $("#slidingWindow").attr("disabled", "disabled");
        $("#dropdownMenu1").attr("disabled", "disabled");

        for (var i = 20; i > 0; i--) {
            $scope.slidingWindowDurations.push(i * 100);
        }

        cheetah.on("cheetah-time", function (data) {
            updateTime(data);
        });

        cheetah.on("cheetah-size", function () {
            refreshData($scope.startTime);
        });

        function updateTime(data) {
            var time = data.time;
            //there may not be an eyetracking data entry for any ms, take care of that
            if ($scope.startTime - $scope.threshold <= time && time <= $scope.endTime + $scope.threshold) {
                $scope.currentTime = time;
                updateTimeLine();
            } else {
                refreshData(time);
            }
        }

        function updateTimeLine() {
            var time = $scope.currentTime;
            $('.timestamp-line').remove();

            var x = $scope.x(time - $scope.sessionStartTimestamp);
            $scope.graph.append("line").style("stroke", "red").attr("x1", x).attr("y1", $scope.y($scope.yMin)).attr("x2", x).attr("y2", $scope.y($scope.yMax)).attr("class", "timestamp-line");
        }

        function initializePercentiles(line) {
            $scope.percentiles = [];
            $.each(line.percentiles, function (percentile, value) {
                if (percentile % 5 === 0) {
                    $scope.percentiles.push({percentile: percentile, value: value});
                }
            });
            $scope.percentiles.sort(function (p1, p2) {
                return p2.percentile - p1.percentile;
            });
        }

        function initializePercentilesIfNecessary(line) {
            if ($scope.percentiles.length === 0) {
                initializePercentiles(line);
            }
        }

        function getCurrentLine() {
            for (var i = 0; i < $scope.pupillometryLines.length; i++) {
                var line = $scope.pupillometryLines[i];
                if (line.label === $scope.additionalInformationLineLabel) {
                    return line;
                }
            }
        }

        $scope.updateAdditionalInformationLine = function () {
            var line = getCurrentLine();
            if (line !== undefined) {
                initializePercentiles(line);
            }

            $scope.percentile = "";
            $scope.slidingWindowDuration = "";
            refreshData($scope.startTime);

            if ($scope.additionalInformationLineLabel === "") {
                $("#percentile").attr("disabled", "disabled");
                $("#slidingWindow").attr("disabled", "disabled");
                $("#dropdownMenu1").attr("disabled", "disabled");
            } else {
                $("#percentile").removeAttr("disabled");
                $("#slidingWindow").removeAttr("disabled");
                $("#dropdownMenu1").removeAttr("disabled");
            }
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
                initializePercentilesIfNecessary(data);

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
                var xAxis = d3.svg.axis().scale(x).tickSize(-height).tickFormat(formatMinutes).ticks(ticks);
                // Add the x-axis.
                graph.append("svg:g").attr("class", "x axis").attr("transform", "translate(0," + height + ")").call(xAxis);

                // create left yAxis
                var yAxisLeft = d3.svg.axis().scale(y).ticks(4).orient("left");
                // Add the y-axis to the left
                graph.append("svg:g").attr("class", "y axis").attr("transform", "translate(-25,0)").call(yAxisLeft);
                graph.append("clipPath").attr("id", "line-clip").append('rect').attr('x', 0).attr('y', 0).attr('height', height).attr('width', width);

                for (var j = 0; j < $scope.pupillometryData.length; j++) {
                    var currentData = $scope.pupillometryData[j];
                    var colorNumber = j % $scope.colors.length;
                    var color = $scope.colors[colorNumber];
                    $scope.pupillometryLines[j].color = color;
                    if (!currentData.selected) {
                        continue;
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
                        if (currentData.label === $scope.additionalInformationLineLabel && currentData.slidingWindow != undefined) {
                            graph.append("svg:path").attr("d", createLine(currentData.slidingWindow, 'average')).attr("class", "sliding-window");
                        }
                    });
                }

                updateTimeLine();
                $scope.updatePercentile();
                $scope.updatingData = false;
                $('.loading').hide();
            }
        };

        function refreshData(time) {
            if ($scope.updatingData === true) {
                return;
            }

            if (time === undefined) {
                time = -1;
            }
            var time = Math.floor(time);
            var slidingWindowDuration = -1;
            var tmpWindow = $scope.slidingWindowDuration;
            if (tmpWindow != null && tmpWindow != "") {
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
            $http.get(url + '&start=' + time + '&slidingWindowDuration=' + slidingWindowDuration).success(function (pupillometryData) {
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

            if (0 <= relativeToChart && relativeToChart <= $scope.diagramWidth) {
                var percent = relativeToChart / $scope.diagramWidth;
                newTime = percent * frameDuration + $scope.startTime;
            } else if (relativeToChart < 0) {
                newTime = Math.max($scope.sessionStartTimestamp, $scope.startTime - frameDuration);
            } else {
                newTime = Math.min($scope.endTime + 2 * $scope.threshold, $scope.sessionEndTimeStamp);
            }

            cheetah.broadcast("cheetah-time", {time: newTime});
        }

        $scope.updatePercentile = function () {
            $(".percentile").remove();
            if ($scope.percentile === "" || $scope.percentile === undefined) {
                return;
            }

            var selected = $.grep($scope.percentiles, function (percentile) {
                if (percentile.percentile == $scope.percentile) {
                    return true;
                }

                return false;
            })[0];

            $scope.graph.append("line")
                .style("stroke", "grey")
                .attr("x1", $scope.x($scope.startTime - $scope.sessionStartTimestamp))
                .attr("y1", $scope.y(selected.value))
                .attr("x2", $scope.x($scope.endTime - $scope.sessionStartTimestamp))
                .attr("y2", $scope.y(selected.value))
                .attr("class", "percentile");

            $scope.graph.append("text")
                .style("fill", "black")
                .attr("x", $scope.x($scope.endTime - $scope.sessionStartTimestamp))
                .attr("y", $scope.y(selected.value))
                .text(selected.percentile)
                .attr("class", "percentile");

        };

        $scope.exploreHighLoad = function () {
            openExploreDialog('raw');
        };

        $scope.exploreHighLoadWithSlidingWindow = function () {
            if ($scope.slidingWindowDuration == '' || $scope.slidingWindowDuration == undefined) {
                $('#slidingWindowRequiredDialog').modal();
                return;
            }

            openExploreDialog('slidingWindow');
        };

        function openExploreDialog(mode) {
            var slidingWindowInit = "";
            if ($scope.slidingWindowDuration != null && $scope.slidingWindowDuration != "") {
                slidingWindowInit = "slidingWindowDuration=" + $scope.slidingWindowDuration + ";"
            }
            var line = getCurrentLine();
            var lineSelection = "pupillometryId=" + line.id + ";";

            var title = "Find Phases With High Load";
            if (mode === 'slidingWindow') {
                title += ' (Sliding Window)';
            }

            var dialog = $("<div ng-app=\"cheetah.Percentile\" ng-view=\"\" ng-init=\"mode='" + mode + "';" + slidingWindowInit + lineSelection + "\"></div>");
            dialog.dialog({
                width: 600,
                height: 400,
                resizable: false,
                title: title
            });

            angular.bootstrap(dialog, ['cheetah.Percentile']);
        }

        $scope.updateSlidingWindow = function (newDuration) {
            $('.loading').show();
            refreshData($scope.startTime);
        };

        refreshData(-1);
    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/pupillometry/pupillometry.htm',
            controller: 'PupillometryCtrl'
        });
});
