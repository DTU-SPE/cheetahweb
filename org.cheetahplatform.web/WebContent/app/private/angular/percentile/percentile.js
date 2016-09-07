angular
    .module('cheetah.Percentile', ['ngRoute'])
    .controller('PercentileCtrl', function ($scope, $http) {
        $scope.percentile = 95;
        $scope.minDuration = 500;
        $scope.percentiles = [];
        $scope.sortDirection = "ascending";
        $scope.sortType = "start";

        for (var i = 19; i > 0; i--) {
            $scope.percentiles.push(i * 5);
        }

        function loadData() {
            var minDuration = Number($scope.minDuration);
            if (minDuration === NaN) {
                return;
            }

            var id = cheetah.processInstance;
            var mode = $scope.mode;
            var extra = "";
            if (mode === "slidingWindow") {
                extra = "&slidingWindowDuration=" + $scope.slidingWindowDuration * 1000;
            }

            var dataId = "&eyetrackingData=" + $scope.pupillometryId;

            $http.get('../../private/percentile?id=' + id + '&minDuration=' + minDuration + '&percentile=' + $scope.percentile + '&mode=' + mode + extra + dataId).success(function (data) {
                var formatTime = d3.time.format("%M:%S");

                $.each(data.percentiles, function (index, phase) {
                    phase.duration = ((phase.end - phase.start) / 1000).toFixed(0);
                    var relativeStart = (phase.start - data.sessionStart) / 1000;
                    phase.startHumanReadable = formatTime(new Date(relativeStart));
                });

                $scope.phases = data.percentiles;
            });
        }

        $scope.queryChanged = function () {
            loadData();
        };

        $scope.jumpTo = function (phase) {
            cheetah.broadcast("cheetah-time", {time: phase.start});
            $scope.phase = phase;
        };

        $scope.orderBy = function (type) {
            //switch order when already selected according to type
            if ($scope.sortType === type) {
                if ($scope.sortDirection === 'ascending') {
                    $scope.sortDirection = 'descending'
                } else {
                    $scope.sortDirection = 'ascending';
                }
            } else {
                //new sort direction --> ascending
                $scope.sortDirection = 'ascending';
            }

            $scope.sortType = type;

            $scope.phases.sort(function (phase1, phase2) {
                var value1 = phase1[type];
                var value2 = phase2[type];

                if ($scope.sortDirection === 'ascending') {
                    return value1 - value2;
                }

                return value2 - value1;
            });
        };

        $scope.getPhaseClass = function (phase) {
            if ($scope.phase !== undefined && phase.start === $scope.phase.start) {
                return "alert-success";
            }

            return "";
        };

        loadData();
    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/percentile/percentile.htm',
            controller: 'PercentileCtrl'
        });
});
