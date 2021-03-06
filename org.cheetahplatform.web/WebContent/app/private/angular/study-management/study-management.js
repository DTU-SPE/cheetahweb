angular.module('cheetah.StudyManagement', ['ngRoute', 'cheetah.CleanData', 'ui.select']).controller('StudyOverviewController', function ($scope, $http) {
}).controller('StudyController', function ($rootScope, $scope, $http, $timeout) {
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
}).controller('DataProcessingController', function ($rootScope, $scope, $http, $sce) {
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
        if (!dataProcessing.timestampColumn || dataProcessing.timestampColumn.trim().length === 0) {
            BootstrapDialog.alert({
                title: 'No timestamp column defined',
                message: 'The definition of the trial computation requires the definition of the timestamp column.'
            });
            return;
        }
        if (!dataProcessing.decimalSeparator || dataProcessing.decimalSeparator.trim().length === 0) {
            BootstrapDialog.alert({
                title: 'No decimal separator defined',
                message: 'The definition of the trial computation requires the definition of the decimal separator.'
            });
            return;
        }

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

    $rootScope.renderTrialScene = function (element) {
        if (element.type === 'scene') {
            return $sce.trustAsHtml(element.name);
        }
        if (element.type === 'marker') {
            return $sce.trustAsHtml('<i>---' + element.name + '---</i>');
        }

        return $sce.trustAsHtml('<strong>' + element.name + '</strong>');
    };

    $scope.getStepsForType = function (dataProcessing, type) {
        return dataProcessing.steps.filter(function (step) {
            return step.type === type;
        });
    };

    $scope.assembleStepName = function (step) {
        var name = "";
        if (step.name && step.name.length > 0) {
            name = step.name + ' - ';
        }

        if (step.type === 'analyze') {
            var configuration = JSON.parse(step.configuration);
            var type = configuration.type;
            if (type === 'blinks') {
                return name + 'Blinks';
            }
            if (type === 'missing_total') {
                return name + "Missing - Total";
            }
            if (type === 'missing_percent') {
                return name + 'Missing - Percentage';
            }

            var computation = '';
            if (type.indexOf('mean') != -1) {
                computation = 'Mean';
            } else if (type.indexOf('standard_deviation') != -1) {
                computation = 'Standard Deviation';
            } else if (type.indexOf('standard_error') != -1) {
                computation = 'Standard Error';
            } else if (type.indexOf('median') != -1) {
                computation = 'Median';
            } else if (type.indexOf('maximum') != -1) {
                computation = 'Maximum';
            } else if (type.indexOf('minimum') != -1) {
                computation = 'Minimum';
            }

            var computationType = '';
            if (type.indexOf('absolute') != -1) {
                computationType = 'Absolute';
            } else if (type.indexOf('relative_divided') != -1) {
                computationType = 'Relative, Divided by Baseline';
            } else if (type.indexOf('relative_subtracted') != -1) {
                computationType = 'Relative, with Baseline Subtracted';
            }

            return name + computation + ' - ' + computationType;
        }

        return step.name;
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
        if ($scope.type === 'clean') {
            cheetah.hideModal($scope, 'cheetah-add-data-processing-step-modal');
            cheetah.showModal($rootScope, 'cheetah-clean-data-modal');
        } else if ($scope.type === 'analyze') {
            $http.get('../../private/listAnalyzeTypes').then(function (response) {
                var analyzeTypes = response.data;
                cheetah.hideModal($scope, 'cheetah-add-data-processing-step-modal');
                cheetah.showModal($rootScope, 'cheetah-add-analyze-step-modal', {
                    analyzeTypes: analyzeTypes,
                    name: $scope.name,
                    dataProcessing: $scope.dataProcessing
                });
            });
        } else {
            throw "The following step is not supported yet: " + $scope.type;
        }
    };
}).controller('AddAnalyzeStepModalController', function ($scope, $http) {
    $scope.computations = [{id: 'mean', name: 'Mean'}, {
        id: 'standard_deviation',
        name: 'Standard Deviation'
    }, {id: 'standard_error', name: 'Standard Error'}, {id: 'median', name: 'Median'}, {
        id: 'maximum',
        name: 'Maximum'
    }, {id: 'minimum', name: 'Minimum'}, {id: 'missing_total', name: 'Missing - Total'}, {
        id: 'missing_percent',
        name: 'Missing - Percentage'
    }, {id: 'blinks', name: 'Blinks'}];
    $scope.types = [{id: 'absolute', name: 'Absolute'}, {
        id: 'relative_divided',
        name: 'Relative Divided'
    }, {id: 'relative_subtracted', name: 'Relative Subtracted'}];

    $scope.$on('cheetah-add-analyze-step-modal.show', function (event, data) {
        $scope.analyzeTypes = data.analyzeTypes;
        $scope.dataProcessing = data.dataProcessing;
        $scope.computation = $scope.computations[0];
        $scope.type = $scope.types[0];
        $scope.name = data.name;
        $scope.startTime = 'startWithStimulus';
        $scope.endTime = 'endWithStimulus';

        $scope.updateAnalyzeType();
    });

    function isValidInteger(number) {
        var parsed = parseInt(number);
        if (isNaN(parsed)) {
            return false;
        }

        return parsed > 0 && parsed % 1 == 0;
    }

    $scope.addAnalyzeStep = function () {
        var startTime = -1;
        var endTime = -1;
        var error;
        if ($scope.startTime === 'startAfterDuration') {
            if (!isValidInteger($scope.startTimeOffset)) {
                error = 'Please enter a valid, positive integer for the start time.'
            } else {
                startTime = parseInt($scope.startTimeOffset);
            }
        }
        if ($scope.endTime === 'endAfterDuration') {
            if (!isValidInteger($scope.endTimeOffset)) {
                error = 'Please enter a valid, positive integer for the end time.'
            } else {
                endTime = parseInt($scope.endTimeOffset);
            }
        }

        if ($scope.dataProcessing.steps) {
            $.each($scope.dataProcessing.steps, function (index, step) {
                if (step.type !== 'analyze') {
                    return;
                }

                var stepConfiguration = angular.fromJson(step.configuration);
                if (stepConfiguration.type === $scope.analyzeType.id) {
                    if ($.trim(step.name) === $.trim($scope.name)) {
                        error = 'There is another analysis step with the same computation and the same name. Please change the name of the analysis step.';
                        return false;
                    }
                }
            });
        }

        if (error) {
            BootstrapDialog.alert({title: 'Invalid Input', message: error});
            return;
        }

        var configuration = {type: $scope.analyzeType.id, startTime: startTime, endTime: endTime};
        var step = {
            dataProcessingId: $scope.dataProcessing.id,
            name: $scope.name,
            type: 'analyze',
            configuration: angular.toJson(configuration)
        };

        cheetah.hideModal($scope, 'cheetah-add-analyze-step-modal');
        $http.post('../../private/addDataProcessingStep', step).then(function (result) {
            step.id = parseInt(result.data, 10);
            $scope.dataProcessing.steps.push(step);
        });
    };

    $scope.updateAnalyzeType = function () {
        var id = $scope.computation.id + "_" + $scope.type.id;
        var typeFound = false;
        $.each($scope.analyzeTypes, function (index, type) {
            if (type.id === id) {
                $scope.analyzeType = type;
                $scope.isAggregatedType = true;
                typeFound = true;
                return false;
            }
        });

        //check for non-assembled types as well
        $.each($scope.analyzeTypes, function (index, type) {
            if (type.id === $scope.computation.id) {
                $scope.analyzeType = type;
                $scope.isAggregatedType = false;
                typeFound = true;
                return false;
            }
        });

        //cross check to make sure types are found. see #655
        if (!typeFound) {
            throw "Unknown analysis type: " + id;
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
            cheetah.showModal($rootScope, 'cheetah-progress-modal', {
                title: 'Preparing File',
                message: 'CEP-Web is now preparing your file. Please stand by, this could take some seconds.'
            });

            $http.post('../../private/computeScenes', postData).then(function (response) {
                cheetah.hideModal($scope, "cheetah-progress-modal");

                var data = {};
                data.scenes = response.data;
                data.selectedFile = $scope.selectedFile.id;
                data.dataProcessing = $scope.dataProcessing;
                //if there is already a configuration defined, edit it; otherwise create a new one
                if (data.dataProcessing.trialComputationConfiguration) {
                    if (typeof data.dataProcessing.trialComputationConfiguration === 'string') {
                        data.config = JSON.parse(data.dataProcessing.trialComputationConfiguration);
                    } else {
                        data.config = data.dataProcessing.trialComputationConfiguration;
                    }
                } else {
                    data.config = {};
                }

                cheetah.showModal($rootScope, "cheetah-define-trial-modal", data);
            });
        }
    };
}).controller('DefineTrialController', function ($scope, $rootScope, $http) {
    $scope.$on('cheetah-define-trial-modal.show', function (event, data) {
        //might be called from other modals, keep the data in this case
        if (!data) {
            return;
        }

        $scope.data = data;
        if (!$scope.data.config.useTrialStartForTrialEnd) {
            $scope.data.config.useTrialStartForTrialEnd = true;
        }
        if (!$scope.data.config.ignoredTrials) {
            $scope.data.config.ignoredTrials = 0;
        }

        var aggregatedScenes = [];
        $.each($scope.data.scenes, function (index, scene) {
            if (!aggregatedScenes[scene]) {
                aggregatedScenes[scene] = {
                    name: scene,
                    count: 1
                };
            } else {
                var temp = aggregatedScenes[scene];
                aggregatedScenes[scene] = {
                    name: scene,
                    count: temp.count + 1
                };
            }
        });

        $scope.data.aggregatedScenes = Object.keys(aggregatedScenes).map(function (key) {
            return {name: key, count: aggregatedScenes[key].count};
        });
    });

    $scope.showStimulus = function () {
        //make sure there is no unnecessary data in the dialog.
        if ($scope.data.config.useTrialStartForTrialEnd === true) {
            delete $scope.data.config.trialEnd;
        }

        cheetah.hideModal($rootScope, "cheetah-define-trial-modal");
        cheetah.showModal($rootScope, "cheetah-define-stimulus-modal", $scope.data);
    };

    $scope.previewTrialComputation = function () {
        var request = {};
        request.config = $scope.data.config;
        request.fileId = $scope.data.selectedFile;
        request.timestampColumn = $scope.data.dataProcessing.timestampColumn;
        request.decimalSeparator = $scope.data.dataProcessing.decimalSeparator;

        cheetah.hideModal($rootScope, "cheetah-define-trial-modal");
        cheetah.showModal($rootScope, 'cheetah-progress-modal', {
            title: 'Computing Trial Preview',
            message: 'CEP-Web is now computing a preview of the trials. Please stand by, this could take some seconds.'
        });

        $http.post("../../private/previewTrials", request).then(function (response) {
            cheetah.hideModal($scope, 'cheetah-progress-modal');
            cheetah.showModal($rootScope, 'cheetah-preview-trial-modal', response.data);
        });
    };
}).controller('DefineStimulusController', function ($scope, $rootScope, $http) {
    $scope.stimulusDetectionTypes = [{
        name: 'Default detection',
        type: 'default',
        description: 'The default detection of a stimulus based on a scene with a predefined name.'
    }, {
        name: 'Detection based on a scene that precedes the stimuli',
        type: 'triggered_by_scene',
        description: 'This detection identifies the stimulus based on a predefined scene which precedes the stimulus.'
    }];
    $scope.stimulusDetectionType = $scope.stimulusDetectionTypes[0];

    $scope.$on('cheetah-define-stimulus-modal.show', function (event, data) {
        if (!data) {
            return;
        }

        $scope.data = data;
        if (!$scope.data.config.stimulus) {
            $scope.data.config.stimulus = {
                stimulusEndsWithTrialEnd: true
            };
        }

        $.each($scope.stimulusDetectionTypes, function (index, type) {
            if ($scope.data.config.stimulus && $scope.data.config.stimulus.type === type.type) {
                $scope.stimulusDetectionType = type;
            }
        });
    });

    $scope.showTrials = function () {
        cheetah.hideModal($rootScope, "cheetah-define-stimulus-modal");
        cheetah.showModal($rootScope, "cheetah-define-trial-modal", $scope.data);
    };

    function cleanUpConfiguration() {
        var stimulus = $scope.data.config.stimulus;
        stimulus.type = $scope.stimulusDetectionType.type;

        //delete all data that does not belong to this detection type
        if ($scope.stimulusDetectionType.type === 'default') {
            if (stimulus.stimulusEndsWithTrialEnd === true) {
                delete stimulus.stimulusEnd;
            }

            delete $scope.data.config.stimulus.precedesStimulus;
        } else if ($scope.stimulusDetectionType.type === 'triggered_by_scene') {
            delete stimulus.stimulusStart;
            delete stimulus.stimulusEndsWithTrialEnd;
            delete stimulus.stimulusEnd;
        }
    }

    $scope.showBaseline = function () {
        cleanUpConfiguration();

        cheetah.hideModal($rootScope, "cheetah-define-stimulus-modal");
        cheetah.showModal($rootScope, "cheetah-define-baseline-modal", $scope.data);
    };

    $scope.validateInput = function () {
        if (!$scope.data) {
            return false;
        }

        if ($scope.stimulusDetectionType.type === 'default') {
            if (!$scope.data.config.stimulus.stimulusStart) {
                return false;
            }
            if ($scope.data.config.stimulus.stimulusEndsWithTrialEnd === false && !$scope.data.config.stimulus.stimulusEnd) {
                return false;
            }
        } else if ($scope.stimulusDetectionType.type === 'triggered_by_scene') {
            if (!$scope.data.config.stimulus.precedesStimulus) {
                return false;
            }
        } else {
            throw 'Unknown stimulus detection type: ' + $scope.stimulusDetectionType;
        }

        return true;
    };

    $scope.previewStimulus = function () {
        if (!$scope.validateInput()) {
            BootstrapDialog.alert({
                title: 'Invalid Configuration',
                message: 'The configuration is invalid - therefore the stimulus cannot be previewed.'
            });
            return;
        }

        cheetah.hideModal($scope, 'cheetah-define-stimulus-modal');
        cheetah.showModal($rootScope, 'cheetah-progress-modal', {
            title: 'Computing the Stimulus Preview',
            message: 'CEP-Web is computing the stimulus preview. Please stand by, this may take some time.'
        });

        cleanUpConfiguration();

        var postData = {};
        postData.config = $scope.data.config;
        postData.fileId = $scope.data.selectedFile;
        postData.timestampColumn = $scope.data.dataProcessing.timestampColumn;
        postData.decimalSeparator = $scope.data.dataProcessing.decimalSeparator;
        $http.post('../../private/previewStimulus', postData).then(function (response) {
            cheetah.hideModal($scope, 'cheetah-progress-modal');
            cheetah.showModal($rootScope, 'cheetah-preview-stimulus-modal', response.data);
        });
    };
}).controller('DefineBaselineController', function ($scope, $rootScope, $http) {
    $scope.baseLineCalcuationsTypes = [{
        id: 'baseline-duration-before-stimulus',
        label: "Predefined duration before stimulus"
    }, {
        id: 'baseline-scene-trigger',
        label: 'Select Markers'
    }];

    $scope.$on('cheetah-define-baseline-modal.show', function (event, data) {
        $scope.data = data;
        if (!$scope.data.config.baseline) {
            $scope.data.config.baseline = {};
        }

        if (!$scope.data.config.baseline.baselineCalculation) {
            $scope.data.config.baseline.baselineCalculation = $scope.baseLineCalcuationsTypes[0];
        }
    });

    $scope.isValid = function () {
        if ($scope.data) {
            if ($scope.data.config.baseline.baselineCalculation === 'baseline-duration-before-stimulus') {
                return $scope.data.config.baseline.durationBeforeStimulus && !isNaN($scope.data.config.baseline.durationBeforeStimulus);
            } else if ($scope.data.config.baseline.baselineCalculation === 'baseline-scene-trigger') {
                if (!($scope.data.config.baseline.baselineStart && $scope.data.config.baseline.baselineEnd)) {
                    return false;
                }

                if ($scope.data.config.baseline.startOffset) {
                    if (!(!isNaN($scope.data.config.baseline.startOffset && $scope.data.config.baseline.startOffset >= 0))){
                        return false;
                    }
                }

                if ($scope.data.config.baseline.endOffset) {
                    if (!(!isNaN($scope.data.config.baseline.endOffset && $scope.data.config.baseline.endOffset >= 0))){
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    };

    $scope.showStimulus = function () {
        cheetah.hideModal($rootScope, "cheetah-define-baseline-modal");
        cheetah.showModal($rootScope, "cheetah-define-stimulus-modal", $scope.data);
    };

    function cleanUpBaseline() {
        if ($scope.data.config.baseline.baselineCalculation === 'baseline-scene-trigger') {
            delete $scope.data.config.baseline.durationBeforeStimulus;
        } else if ($scope.data.config.baseline.baselineCalculation === 'baseline-duration-before-stimulus') {
            delete $scope.data.config.baseline.baselineStart;
            delete $scope.data.config.baseline.baselineEnd;
            delete $scope.data.config.baseline.endOffset;
            delete $scope.data.config.baseline.startOffset;
        }
    }

    $scope.showLoadCalculations = function () {
        cheetah.hideModal($rootScope, "cheetah-define-baseline-modal");
        cheetah.showModal($rootScope, "cheetah-progress-modal", {
            title: 'Preparing Overview',
            message: 'CEP-Web is preparing an overview of your trial configuration. Please stand by, this could take some time.'
        });

        cleanUpBaseline();

        var request = {};
        request.config = $scope.data.config;
        request.fileId = $scope.data.selectedFile;
        request.timestampColumn = $scope.data.dataProcessing.timestampColumn;
        request.decimalSeparator = $scope.data.dataProcessing.decimalSeparator;

        $http.post("../../private/previewBaseline", request).success(function (data) {
            cheetah.hideModal($rootScope, "cheetah-progress-modal");
            $scope.data.trialOverview = data;
            cheetah.showModal($rootScope, "cheetah-trial-overview-modal", $scope.data)
        });
    };

}).controller('TrialOverviewController', function ($http, $scope, $rootScope) {
    $scope.$on('cheetah-trial-overview-modal.show', function (event, data) {
        $scope.data = data;
    });

    $scope.showBaseline = function () {
        cheetah.hideModal($rootScope, "cheetah-trial-overview-modal");
        cheetah.showModal($rootScope, "cheetah-define-baseline-modal", $scope.data);
    };

    $scope.saveTrialConfiguration = function () {
        var postData = {
            dataProcessingId: $scope.data.dataProcessing.id,
            trialConfiguration: $scope.data.config
        };

        $http.post('../../private/saveTrialConfiguration', postData).then(function (response) {
            cheetah.hideModal($scope, 'cheetah-trial-overview-modal');
            BootstrapDialog.alert({
                title: 'Configuration Saved',
                message: 'The trial configuration was saved successfully.'
            });

            //populate the new configuration back so it will be displayed
            $scope.data.dataProcessing.trialComputationConfiguration = $scope.data.config;
        });
    };
}).controller('PreviewTrialController', function ($rootScope, $scope) {
    $scope.$on('cheetah-preview-trial-modal.show', function (event, data) {
        $scope.trials = data.trials;
        $scope.notifications = data.notifications;
    });

    $scope.backToTrialDefinition = function () {
        cheetah.hideModal($scope, 'cheetah-preview-trial-modal');
        cheetah.showModal($rootScope, 'cheetah-define-trial-modal');
    };
}).controller('ProgressController', function ($scope) {
    $scope.$on('cheetah-progress-modal.show', function (event, data) {
        $scope.title = data.title;
        $scope.message = data.message;
    });
}).controller('PreviewStimulusController', function ($rootScope, $scope, $sce) {
    $scope.$on('cheetah-preview-stimulus-modal.show', function (event, data) {
        $scope.trials = data.trials;
        $scope.notifications = data.notifications;
    });

    $scope.backToStimulusDefinition = function () {
        cheetah.hideModal($scope, 'cheetah-preview-stimulus-modal');
        cheetah.showModal($rootScope, 'cheetah-define-stimulus-modal');
    };
}).config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'angular/study-management/study-management.htm',
        controller: 'StudyController'
    });
});

