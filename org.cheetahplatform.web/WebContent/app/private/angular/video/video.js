angular
    .module('cheetah.Video', ['ngRoute'])
    .controller('VideoCtrl', function ($scope, $http) {
        if (!cheetah.processInstance && !cheetah.movie) {
            return;
        }

        var id = cheetah.processInstance;
        var postFix = undefined;
        if (cheetah.processInstance) {
            postFix = 'processInstance=' + cheetah.processInstance;
        } else {
            postFix = 'id=' + cheetah.movie;
        }
        $http.get('../../private/movie?' + postFix).success(function (videos) {
            $scope.videos = videos;
            if (videos.length > 0) {
                cheetah.sessionStartTimestamp = videos[0].sessionStartTimestamp;
            }

            //if there are no files and no process instance, we need to hide the loading
            if (!cheetah.processInstance && !cheetah.files) {
                $('.loading').hide();
            }
        });

        setInterval(update, 100);
        cheetah.on("cheetah-time", timeChanged);
        var ignoreUpdate;

        function update() {
            if ($scope.videos === undefined || $scope.videos.length === 0) {
                return;
            }

            var time = document.getElementById('video_container').currentTime;
            var newTime = $scope.videos[0].startTimestamp + time * 1000 * 1000;
            ignoreUpdate = true;
            cheetah.broadcast("cheetah-time", {time: newTime});
            ignoreUpdate = false;
        }

        function timeChanged(data) {
            if (ignoreUpdate === true) {
                return;
            }

            var videoTime = (data.time - $scope.videos[0].startTimestamp) / (1000 * 1000); //convert from microsecond to seconds
            document.getElementById('video_container').currentTime = videoTime;
        }
    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/video/video.htm',
            controller: 'VideoCtrl'
        });
});
