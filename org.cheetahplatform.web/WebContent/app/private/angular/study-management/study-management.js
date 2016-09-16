angular.module('cheetah.StudyManagement', ['ngRoute', 'cheetah.CleanData']).controller('StudyOverviewController', function ($scope, $http) {
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
    $(".js-example-basic-single").select2();
}).controller('DataProcessingController', function ($rootScope, $scope, $http) {
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

    $scope.showAddDataProcessingStepModal = function (dataProcessing) {
        cheetah.showModal($rootScope, 'cheetah-add-data-processing-step-modal', dataProcessing);
        $rootScope.dataProcessingMode = 'add';
    };

    $scope.deleteDataProcessingStep = function (dataProcessing, step) {
        BootstrapDialog.show({
            title: 'Delete Data Processing Step?',
            message: 'Do you really want to delete this data processing step?',
            buttons: [{
                label: 'Yes',
                action: function (dialog) {
                    $http.get('../../private/deleteDataProcessingStep?id=' + step.id).then(function () {
                        var index = dataProcessing.steps.indexOf(step);
                        dataProcessing.steps.splice(index, 1);
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

    $scope.showEditDataProcessingColumnsModal = function (dataProcessing) {
        cheetah.showModal($rootScope, 'cheetah-edit-data-processing-modal', dataProcessing);
    };

    $scope.showEditDataProcessingStepModal = function (step) {
        $rootScope.dataProcessingMode = 'edit';

        if (step.type === 'clean') {
            var configuration = JSON.parse(step.configuration);
            cheetah.showModal($rootScope, 'cheetah-clean-data-modal', configuration);
        } else {
            throw 'Editing step of type ' + step.type + ' is not supported yet.';
        }
    };

    $scope.getTrialProcessingLabel = function (dataProcessing) {
        if (!dataProcessing.trialComputationConfiguration) {
            return 'No trial computation defined';
        }

        return 'Data processing defined';
    };

    $scope.showEditTrialComputationModal = function (dataProcessing) {
        var postData = {studyId: $scope.study.id};
        $http.post('../../private/listAvailablePupillometryFiles', postData).then(function (response) {
            var data = response.data;
            if (data.length === 0) {
                BootstrapDialog.alert({
                    title: 'No data available',
                    message: 'The definition of the trial computation requires at least one pupillometry file from which the scenes can be extracted. Please upload a data file for this study first.'
                });
            } else {
                cheetah.showModal($rootScope, 'cheetah-select-pupillometry-file-modal', {
                    dataProcessing: dataProcessing,
                    files: data
                });
            }
        });
    };
}).controller('AddDataProcessingModalController', function ($scope, $http) {
    $scope.$on('cheetah-add-data-processing-modal.show', function (event, study) {
        $scope.study = study;
        $scope.name = '';
        $scope.comment = '';
        $scope.timestampColumn = '';
        $scope.leftPupilColumn = '';
        $scope.rightPupilColumn = '';
        $scope.decimalSeparator = '';
    });

    $scope.addDataProcessing = function () {
        if ($scope.name.trim().length === 0) {
            return;
        }

        var postData = {
            studyId: $scope.study.id,
            name: $scope.name,
            comment: $scope.comment,
            timestampColumn: $scope.timestampColumn,
            leftPupilColumn: $scope.leftPupilColumn,
            rightPupilColumn: $scope.rightPupilColumn,
            decimalSeparator: $scope.decimalSeparator
        };
        $http.post('../../private/addDataProcessing', postData).then(function (response) {
            $scope.study.dataProcessing.push(response.data);
            cheetah.hideModal($scope, 'cheetah-add-data-processing-modal');
        });
    };
}).controller('AddDataProcessingStepModalController', function ($rootScope, $scope, $http) {
    $scope.$on('cheetah-add-data-processing-step-modal.show', function (event, dataProcessing) {
        $scope.type = 'clean';
        $scope.name = '';
        $scope.dataProcessing = dataProcessing;
    });

    $rootScope.$on('cheetah-clean-data-modal.hide', function (event, data) {
        var step = {
            dataProcessingId: $scope.dataProcessing.id,
            name: $scope.name,
            type: $scope.type,
            configuration: angular.toJson({
                filters: data.filters,
                parameters: data.parameters,
                decimalSeparator: data.decimalSeparator,
                fileNamePostFix: data.fileNamePostFix
            })
        };

        $http.post('../../private/addDataProcessingStep', step).then(function (id) {
            step.id = id;
            $scope.dataProcessing.steps.push(step);
        });
    });

    $scope.addDataProcessingStep = function () {
        if ($scope.name.trim().length === 0) {
            BootstrapDialog.alert({title: 'Name Missing', message: 'Please enter a name for the step.'});
            return;
        }

        if ($scope.type === 'clean') {
            cheetah.hideModal($scope, 'cheetah-add-data-processing-step-modal');
            cheetah.showModal($rootScope, 'cheetah-clean-data-modal');
        } else {
            throw "The following step is not supported yet: " + $scope.type;
        }
    };
}).controller('EditDataProcessingModalController', function ($scope, $http) {
    $scope.$on('cheetah-edit-data-processing-modal.show', function (event, dataProcessing) {
        $scope.timestampColumn = dataProcessing.timestampColumn;
        $scope.leftPupilColumn = dataProcessing.leftPupilColumn;
        $scope.rightPupilColumn = dataProcessing.rightPupilColumn;
        $scope.decimalSeparator = dataProcessing.decimalSeparator;
        $scope.dataProcessing = dataProcessing;
    });

    $scope.saveDataProcessing = function () {
        var postData = {
            dataProcessingId: $scope.dataProcessing.id,
            timestampColumn: $scope.timestampColumn,
            leftPupilColumn: $scope.leftPupilColumn,
            rightPupilColumn: $scope.rightPupilColumn,
            decimalSeparator: $scope.decimalSeparator
        };

        $http.post('../../private/updateDataProcessing', postData).then(function () {
            //post request was ok, update the data
            $scope.dataProcessing.timestampColumn = $scope.timestampColumn;
            $scope.dataProcessing.leftPupilColumn = $scope.leftPupilColumn;
            $scope.dataProcessing.rightPupilColumn = $scope.rightPupilColumn;
            $scope.dataProcessing.decimalSeparator = $scope.decimalSeparator;

            cheetah.hideModal($scope, 'cheetah-edit-data-processing-modal');
        });
    };
}).controller('SelectPupillometryFileModalController', function ($scope, $http, $rootScope) {
    $scope.$on('cheetah-select-pupillometry-file-modal.show', function (event, data) {
        $scope.dataProcessing = data.dataProcessing;
        $scope.files = data.files;
        delete $scope.selectedFile;
    });

    $scope.showScenes = function () {
        if (!$scope.selectedFile) {
            BootstrapDialog.alert({title: 'No File Selected', message: 'Please select the file to be parsed.'});
        } else {
            var postData = {fileId: $scope.selectedFile.id, timestampColumn: $scope.dataProcessing.timestampColumn};

            cheetah.hideModal($scope, 'cheetah-select-pupillometry-file-modal');
            cheetah.showModal($scope, 'cheetah-prepare-pupillometry-file-modal');

            $http.post('../../private/computeScenes', postData).then(function (response) {
                cheetah.hideModal($scope, "cheetah-prepare-pupillometry-file-modal");
                cheetah.showModal($rootScope, "cheetah-define-trial-modal", response.data);
            });
        }
    };
}).controller('DefineTrialController', function ($scope, $http, $rootScope) {
    $scope.$on('cheetah-define-trial-modal.show', function (event, data) {
        $scope.scenes = data;
    });
}).config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'angular/study-management/study-management.htm',
        controller: 'StudyController'
    });
});

