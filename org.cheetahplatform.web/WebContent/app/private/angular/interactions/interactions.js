angular
    .module('cheetah.Interactions', ['ngRoute'])
    .controller('InteractionsCtrl', function ($scope, $http, $interval) {
        if (cheetah.processInstance === undefined) {
            $("#messageLabel").text("No interactions found for this PPM instance");
        } else {
            $http.get('../../private/interactions?ppmInstanceId=' + cheetah.processInstance).success(function (data) {
                $("#messageLabel").text("Interactions with the modeling environment for this PPM instance");
                $scope.interactions = data;
            });
        }

        $scope.jumpToInteraction = function (interaction) {
            cheetah.broadcast("cheetah-time", {time: interaction.timestamp});
        };

        $scope.formatTimestamp = function (timestamp) {
            //if the timestamp is not available yet, defer the update
            if (cheetah.sessionStartTimestamp === undefined) {
                $interval($scope.apply, 25);
                return;
            }

            var relativeTime = timestamp - cheetah.sessionStartTimestamp;
            var relativeTimeInSeconds = relativeTime / 1000 / 1000;
            var minutes = Math.floor(relativeTimeInSeconds / 60);
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            var seconds = Math.floor(relativeTimeInSeconds) % 60;
            if (seconds < 10) {
                seconds = "0" + seconds;
            }
            return minutes + ":" + seconds;
        };
    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/interactions/interactions.htm',
            controller: 'InteractionsCtrl'
        });
});