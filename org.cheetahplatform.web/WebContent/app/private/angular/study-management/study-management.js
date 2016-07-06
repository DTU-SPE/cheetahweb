angular.module('cheetah.StudyManagement', ['ngRoute'])
    .controller('StudyCtrl', function ($scope, $http, $q) {
        $scope.studies = [];
        $scope.newStudyName = "";
        $scope.newStudyComment = "";
        $scope.selection = {id: -1};

        $http.get("../../private/listStudies").success(function (studies) {
            $scope.studies = studies;
        });

        $scope.openAddStudyDialog = function () {
            $('#addStudyDialog').modal();
        };

        $scope.deleteStudy = function () {
            BootstrapDialog.confirm('Do you really wan to delete the selected study?', function (result) {
                if (result) {
                    var postData = {studyId: $scope.selection.id};
                    $http.post('../../private/deleteStudy', postData).success(function (studies) {
                        $scope.studies = studies;
                    }).error(function (error) {
                        BootstrapDialog.show({
                            title: 'Sorry, a problem occurred...',
                            message: error.message,
                            buttons: [{
                                id: 'btn-ok',
                                label: 'OK',
                                cssClass: 'btn-primary',
                                autospin: false,
                                action: function (dialogRef) {
                                    dialogRef.close();
                                }
                            }]
                        });
                    });
                }
            });
        };

        $scope.setSelection = function (study) {
            $scope.selection = study;
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
