angular.module('cheetah.StudyManagement', ['ngRoute']).controller('StudyOverviewController', function ($scope, $http) {
}).controller('StudyController', function ($rootScope, $scope, $http) {
    $scope.studies = [];
    $scope.newStudyName = "";
    $scope.newStudyComment = "";
    $scope.selection = {id: -1};
    $scope.mode = 'overview';

    $scope.$on('cheetah-show-overview', function () {
        $scope.mode = 'overview';
    });

    $http.get("../../private/listStudies").success(function (studies) {
        $scope.studies = studies;
    });

    $scope.openAddStudyDialog = function () {
        $('#addStudyDialog').modal();
    };

    $scope.showDataProcessing = function () {
        $scope.mode = 'data_processing';
        $scope.$broadcast('cheetah-show-data-processing', $scope.selection);
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
}).controller('DataProcessingController', function ($rootScope, $scope, $http) {
    $scope.$on('cheetah-show-data-processing', function (event, study) {
        $scope.study = study;
    });

    $scope.$on('cheetah-show-data-processing', function (event, study) {
        $scope.study = study;
    });

    $scope.showOverview = function () {
        $scope.$emit('cheetah-show-overview');
    };

    $scope.showAddDataProcessingModal = function () {
        cheetah.showModal($rootScope, 'cheetah-add-data-processing-modal', $scope.study);
    };

    $scope.deleteDataProcessing = function (dataProcessing) {
        BootstrapDialog.show({
            title: 'Delete Data Processing?',
            message: 'Do you really want to delete this data processing?',
            buttons: [{
                label: 'Yes',
                action: function (dialog) {
                    $http.get('../../private/deleteDataProcessing?id=' + dataProcessing.id).then(function () {
                        var index = $scope.study.dataProcessing.indexOf(dataProcessing);
                        $scope.study.dataProcessing.splice(index, 1);
                        dialog.close();
                    });
                }
            }, {
                label: 'No',
                action: function (dialog) {
                    dialog.close();
                }
            }]
        });
    };

    $scope.showAddDataProcessingStepModal = function () {
        cheetah.showModal($rootScope, 'cheetah-add-data-processing-step-modal');
    };
}).controller('AddDataProcessingModalController', function ($scope, $http) {
    $scope.$on('cheetah-add-data-processing-modal', function (event, study) {
        $scope.study = study;
        $scope.name = '';
        $scope.comment = '';
    });

    $scope.addDataProcessing = function () {
        if ($scope.name.trim().length === 0) {
            return;
        }

        var postData = {
            studyId: $scope.study.id, name: $scope.name, comment: $scope.comment
        };
        $http.post('../../private/addDataProcessing', postData).then(function (response) {
            $scope.study.dataProcessing.push(response.data);
            cheetah.hideModal($scope, 'cheetah-add-data-processing-modal');
        });
    };
}).controller('AddDataProcessingStepModalController', function ($rootScope, $scope) {
    $scope.$on('cheetah-add-data-processing-step-modal', function () {
        $scope.type = 'clean';
    });

    $scope.addDataProcessingStep = function () {
        if ($scope.type === 'clean') {
            cheetah.hideModal($scope, 'cheetah-add-data-processing-step-modal');
            cheetah.showModal($rootScope, 'cheetah-add-clean-step-modal');
        } else {
            throw "The following step is not supported yet: " + $scope.type;
        }
    };
}).controller('AddCleanStepModalController', function ($scope, $http) {
    $scope.$on('cheetah-add-clean-step-modal', function () {

    });
}).config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'angular/study-management/study-management.htm',
        controller: 'StudyController'
    });
});

