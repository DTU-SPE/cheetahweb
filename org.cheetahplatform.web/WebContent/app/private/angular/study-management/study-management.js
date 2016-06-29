angular.module('cheetah.StudyManagement', ['ngRoute'])
    .controller('StudyCtrl', function ($scope, $http, $q) {
        $scope.studies = [];
        $scope.newStudyName = "";
        $scope.newStudyComment = "";

        $http.get("../../private/listStudies").success(function (studies) {
            $scope.studies = studies;
        });

        $scope.openAddStudyDialog = function () {
            $('#addStudyDialog').modal();
        };

        $scope.submitNewStudy = function () {
            var postData = {
                name: $scope.newStudyName,
                comment: $scope.newStudyComment
            };

            $http.post("../../private/addStudy", angular.toJson(postData)).success(function (studies) {
                $scope.studies = studies;
                $('#addStudyDialog').modal('hide');
                $scope.newStudyName = "";
                $scope.newStudyComment = "";
            });
        };

    }).config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'angular/study-management/study-management.htm',
        controller: 'StudyCtrl'
    });
});
