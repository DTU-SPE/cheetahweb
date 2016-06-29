angular
    .module('cheetah.Search', ['ngRoute'])
    .controller('SearchCtrl', function ($scope, $http, $timeout) {
        $scope.subjects = [];
        $scope.search = {};
        $scope.selection = undefined;

        var storedSearch = localStorage.getItem('search');
        if (storedSearch) {
            $scope.search = JSON.parse(storedSearch);
        }

        $http.get("../../private/listSubjects").success(function (subjects) {
            $scope.subjects = subjects;
            $scope.studies = uniqueProperties(subjects, "study.name");
        });

        $scope.filteredSubjects = function () {
            var filtered = $.grep($scope.subjects, function (subject, index) {


                //otherwise filter per field
                var selectSubject = true;
                selectSubject &= filterAndAppend(subject, "subjectId", $scope.search.subjectId);
                selectSubject &= filterAndAppend(subject, "study.name", $scope.search.study);

                return selectSubject;
            });

            localStorage.setItem('search', JSON.stringify($scope.search));

            return filtered;
        };


        $scope.getSubjectClass = function (subject) {
            if ($scope.selection === subject) {
                return "alert-success";
            }
            return "";
        };

        $scope.setSelection = function (subject) {
            $scope.selection = subject;
            if ($scope.selection !== undefined) {
                $("#button-analyze").removeAttr("disabled");
            } else {
                $("#button-analyze").attr("disabled");
            }
        };

        $scope.connectFiles = function () {
            var request = {files: []};
            $.each($scope.filesToConnect, function (id, checked) {
                if (checked) {
                    request.files.push(Number(id));
                }
            });
            if ($scope.connectTimestampColumn.length === 0 || $scope.connectLeftPupilColumnName.length === 0 || $scope.connectRightPupilColumnName.length === 0 || request.files.length === 0) {
                BootstrapDialog.alert({
                    title: 'Missing Input',
                    message: 'Please fill in timestamp, left pupil and right pupil as well as select at least one file to prepare.'
                });
                return;
            }

            request.timestampColumn = $scope.connectTimestampColumn;
            request.leftPupilColumn = $scope.connectLeftPupilColumnName;
            request.rightPupilColumn = $scope.connectRightPupilColumnName;
            $http.post("../../private/connect", angular.toJson(request)).then(function (response) {
                var workers = response.data;
                waitForWorkers(workers);
            });
            $("#cheetah-connect-modal").modal('hide');
        };

        /**
         * Waits for the given workers to finish.
         */
        function waitForWorkers(workers) {
            $scope.workers = workers;
            var workerIds = workers.map(function (worker) {
                return worker.workerId;
            });
            $('#cheetah-repeat-analysis-button').addClass('disabled');
            $('#cheetah-wait-for-workers-modal').modal('show');

            var updateWorkersFunction = function () {
                $http.post('../../private/getWorkItemStatus', workerIds).then(function (response) {
                    var workerToStatus = {};
                    $.each(response.data, function (index, status) {
                        workerToStatus[status.id] = status.status;
                    });

                    $scope.allWorkersFinished = true;
                    $.each($scope.workers, function (index, worker) {
                        var status = workerToStatus[worker.workerId];
                        worker.status = status;
                        if (status !== 'finished') {
                            $scope.allWorkersFinished = false;
                        }
                    });

                    //repeat until all workers have finished
                    if (!$scope.allWorkersFinished) {
                        $timeout(updateWorkersFunction, 500);
                    } else {
                        $('#cheetah-repeat-analysis-button').removeClass('disabled');
                    }
                });
            };

            $timeout(updateWorkersFunction, 500);
        }

        $scope.closePrepareModalAndAnalyze = function () {
            $('#cheetah-wait-for-workers-modal').modal('hide');
            $scope.analyze();
        };

        $scope.analyze = function () {
            if ($scope.selection === undefined) {
                BootstrapDialog.alert({
                    title: 'No Subject Selected',
                    message: 'Please select first the subject you would like to analyze.'
                });
                return;
            }

            var postData = {subjectId: $scope.selection.id};
            $http.post('../../private/listFilesForSubject', angular.toJson(postData)).then(function (response) {
                var data = response.data;
                var hasDataToAnalyze = data.files.length > 0 || data.movies.length > 0;
                var hasDataToPrepare = data.candidatesForConnecting.length > 0;

                var showPrepareDataModal = function () {
                    $scope.filesToConnect = {};
                    $scope.connectTimestampColumn = "EyeTrackerTimestamp";
                    $scope.connectLeftPupilColumnName = "PupilLeft";
                    $scope.connectRightPupilColumnName = "PupilRight";
                    $scope.candidatesForConnecting = data.candidatesForConnecting;
                    $('#cheetah-connect-modal').modal('show');
                };


                var analyzeData = function () {
                    //aggregate by process instance
                    var processInstanceIds = {};
                    var allData = [].concat(data.files).concat(data.movies);
                    $.each(allData, function (index, dataElement) {
                        var existing = processInstanceIds[dataElement.processInstanceId] || {};
                        //videos do not have the process instance name, ensure that the name is not overridden
                        existing.name = existing.name || dataElement.processInstanceName;
                        existing.id = dataElement.processInstanceId;
                        processInstanceIds[dataElement.processInstanceId] = existing;
                    });
                    delete processInstanceIds[-1]; //ignore movies without process instance ids

                    $scope.filesToAnalyze = data.files;
                    $scope.selectedFiles = {};
                    $scope.moviesToAnalyze = data.movies;
                    $scope.selectedMovie = {};
                    $scope.processInstancesToAnalyze = [];
                    $.each(processInstanceIds, function (key, value) {
                        $scope.processInstancesToAnalyze.push(value);
                    });
                    $scope.selectedProcessInstance = {};
                    if ($scope.processInstancesToAnalyze.length > 0) {
                        $scope.selectedProcessInstance = $scope.processInstancesToAnalyze[0];
                    }

                    $('#cheetah-analyze-modal').modal('show');
                    $('#cheetah-analyze-modal').on('shown.bs.modal', function () {
                        if ($scope.processInstancesToAnalyze.length > 0) {
                            $('#cheetah-analyze-ppm-tab a').tab('show');
                        } else {
                            $('#cheetah-analyze-custom-tab a').tab('show');
                        }
                    });
                };

                if (hasDataToPrepare) {
                    if (hasDataToAnalyze) {
                        //data for analysis as well as preparation
                        BootstrapDialog.show({
                            title: 'Unprepared Data Available',
                            message: 'Cheetah web found additional data that was not prepared for analysis yet. Before you can analyze this data, you need to prepare it first. Do you want to prepare this data now?',
                            buttons: [{
                                label: 'Yes',
                                action: function (dialog) {
                                    dialog.close();
                                    $scope.$apply(function () {
                                        showPrepareDataModal();
                                    });
                                }
                            }, {
                                label: 'No',
                                action: function (dialog) {
                                    dialog.close();
                                    $scope.$apply(function () {
                                        analyzeData();
                                    });
                                }
                            }]
                        });

                    } else {
                        //no data, but data to prepare
                        showPrepareDataModal();
                    }
                } else {
                    if (hasDataToAnalyze) {
                        //nothing to prepare, but data for analysis
                        analyzeData();
                    } else {
                        //no data, nothing to prepare
                        BootstrapDialog.alert({
                            title: 'No Files Available',
                            message: 'There are no files and videos to be analyzed for the selected subject.'
                        });
                    }
                }
            });
        };

        $scope.getMovieName = function (movie) {
            var name = movie.url;
            var lastSeparatorIndex = name.lastIndexOf('/');
            if (lastSeparatorIndex === -1) {
                return name;
            }

            name = name.substring(lastSeparatorIndex);
            var underscoreIndex = name.indexOf('_');
            var atIndex = name.indexOf('@');
            if (underscoreIndex !== -1 && underscoreIndex < atIndex) {
                return name.substring(underscoreIndex + 1);
            }

            return name;
        };

        $scope.analyzeData = function () {
            var analyzePpm = $('#cheetah-analyze-ppm-tab').hasClass('active');
            if (analyzePpm) {
                if ($scope.selectedProcessInstance.id === undefined) {
                    BootstrapDialog.alert({
                        title: 'Please Select a PPM',
                        message: 'Please select the process of process modeling to analyze.'
                    });
                    return;
                }

                var url = "analyze.htm?processInstance=" + $scope.selectedProcessInstance.id;
                window.location.href = url;
            } else {
                var selectedFiles = [];
                $.each($scope.filesToAnalyze, function (index, file) {
                    if ($scope.selectedFiles[file.id]) {
                        selectedFiles.push(file);
                    }
                });

                var selectedMovie = undefined;
                if ($scope.selectedMovie.id) {
                    $.each($scope.moviesToAnalyze, function (index, movie) {
                        if (movie.id == $scope.selectedMovie.id) {
                            selectedMovie = movie;
                            return false;
                        }
                    });
                }

                if (selectedFiles.length === 0 && (!selectedMovie || selectedMovie.length === 0)) {
                    BootstrapDialog.alert({
                        title: 'Empty Selection',
                        message: 'Please select at least one file or movie to analyze.'
                    });
                    return;
                }

                var fileIds = selectedFiles.map(function (file) {
                    return file.id;
                });
                var url = "analyze.htm?files=" + encodeURIComponent(fileIds);
                if (selectedMovie) {
                    url = url + "&movie=" + selectedMovie.id;
                }
                window.location.href = url;
            }
        };

        function filterAndAppend(subject, property, search) {
            if (search != null && search.trim().length > 0) {
                return filter(subject, property, search);
            }

            return true;
        }

        function filter(value, property, search) {
            var propertyToMatch = value[property];
            if (property.indexOf('.') !== -1) {
                var tokens = property.split('.');
                propertyToMatch = value[tokens[0]][tokens[1]];
            }

            propertyToMatch = propertyToMatch.toLowerCase();
            return propertyToMatch.indexOf(search.toLowerCase()) > -1;
        }

        function uniqueProperties(subjects, property) {
            var properties = [];

            $.each(subjects, function (index, subject) {
                var value = subject[property];
                if (property.indexOf('.') !== -1) {
                    var tokens = property.split('.');
                    value = subject[tokens[0]][tokens[1]];
                }

                if (properties.indexOf(value) == -1) {
                    properties.push(value);
                }
            });

            return properties;
        }
    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/search/search.htm',
            controller: 'SearchCtrl'
        })
});
