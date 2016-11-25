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
                $("#button-visualize").removeAttr("disabled");
            } else {
                $("#button-visualize").attr("disabled");
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
            $('#cheetah-repeat-visualization-button').addClass('disabled');
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
                        $('#cheetah-repeat-visualization-button').removeClass('disabled');
                    }
                });
            };

            $timeout(updateWorkersFunction, 500);
        }

        $scope.closePrepareModalAndVisualize = function () {
            $('#cheetah-wait-for-workers-modal').modal('hide');
            $scope.visualize();
        };

        $scope.visualize = function () {
            if ($scope.selection === undefined) {
                BootstrapDialog.alert({
                    title: 'No Subject Selected',
                    message: 'Please select first the subject you would like to visualize.'
                });
                return;
            }

            var postData = {subjectId: $scope.selection.id};
            $http.post('../../private/listFilesForSubject', angular.toJson(postData)).then(function (response) {
                var data = response.data;
                var hasDataToVisualize = data.files.length > 0 || data.movies.length > 0;
                var hasDataToPrepare = data.candidatesForConnecting.length > 0;

                var showPrepareDataModal = function () {
                    $scope.filesToConnect = {};
                    $scope.connectTimestampColumn = "EyeTrackerTimestamp";
                    $scope.connectLeftPupilColumnName = "PupilLeft";
                    $scope.connectRightPupilColumnName = "PupilRight";
                    $scope.candidatesForConnecting = data.candidatesForConnecting;
                    $('#cheetah-connect-modal').modal('show');
                };


                var visualizeDate = function () {
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

                    $scope.filesToVisualize = data.files;
                    $scope.selectedFiles = {};
                    $scope.moviesToVisualize = data.movies;
                    $scope.selectedMovie = {};
                    $scope.processInstancesToVisualize = [];
                    $.each(processInstanceIds, function (key, value) {
                        $scope.processInstancesToVisualize.push(value);
                    });
                    $scope.selectedProcessInstance = {};
                    if ($scope.processInstancesToVisualize.length > 0) {
                        $scope.selectedProcessInstance = $scope.processInstancesToVisualize[0];
                    }

                    $.each(allData, function (index, data) {
                        var processInstance = undefined;
                        $.each($scope.processInstancesToVisualize, function (index, instance) {
                            if (instance.id === data.processInstanceId) {
                                processInstance = instance;
                                return false;
                            }
                        });

                        if (!processInstance) {
                            return; //not all data is assigned to a process instance
                        }

                        processInstance.data = processInstance.data || [];
                        if (data.filename) {
                            processInstance.data.push({name: data.filename, id: data.id, type: 'data'}); //files
                        } else {
                            //videos
                            var name = data.url;
                            name = name.substring(name.lastIndexOf('/'));
                            name = name.substring(name.indexOf('_') + 1);
                            processInstance.data.push({name: name, id: data.id, type: 'video'});
                        }
                    });

                    $('#cheetah-visualize-modal').modal('show');
                    $('#cheetah-visualize-modal').on('shown.bs.modal', function () {
                        if ($scope.processInstancesToVisualize.length > 0) {
                            $('#cheetah-visualize-ppm-tab a').tab('show');
                        } else {
                            $('#cheetah-visualize-custom-tab a').tab('show');
                        }
                    });
                };

                if (hasDataToPrepare) {
                    if (hasDataToVisualize) {
                        //data for visualization as well as preparation
                        BootstrapDialog.show({
                            title: 'Unprepared Data Available',
                            message: 'Cheetah web found additional data that was not prepared for visualization yet. Before you can visualize this data, you need to prepare it first. Do you want to prepare this data now?',
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
                                        visualizeDate();
                                    });
                                }
                            }]
                        });
                    } else {
                        //no data, but data to prepare
                        showPrepareDataModal();
                    }
                } else {
                    if (hasDataToVisualize) {
                        //nothing to prepare, but data for visualization
                        visualizeDate();
                    } else {
                        //no data, nothing to prepare
                        BootstrapDialog.alert({
                            title: 'No Files Available',
                            message: 'There are no files and videos to be visualized for the selected subject.'
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

        $scope.visualizeData = function () {
            var visualizePpm = $('#cheetah-visualize-ppm-tab').hasClass('active');
            var visualizeCustom = $('#cheetah-visualize-custom').hasClass('active');
            var visualizeWorkload= $('#cheetah-visualize-workload').hasClass('active');
            
            if (visualizePpm) {
                if ($scope.selectedProcessInstance.id === undefined) {
                    BootstrapDialog.alert({
                        title: 'Please Select a PPM',
                        message: 'Please select the process of process modeling to visualize.'
                    });
                    return;
                }

                var url = "visualize.htm?processInstance=" + $scope.selectedProcessInstance.id;
                window.location.href = url;
            } else if (visualizeCustom){
                var selectedFiles = [];
                $.each($scope.filesToVisualize, function (index, file) {
                    if ($scope.selectedFiles[file.id]) {
                        selectedFiles.push(file);
                    }
                });

                var selectedMovie = undefined;
                if ($scope.selectedMovie.id) {
                    $.each($scope.moviesToVisualize, function (index, movie) {
                        if (movie.id == $scope.selectedMovie.id) {
                            selectedMovie = movie;
                            return false;
                        }
                    });
                }

                if (selectedFiles.length === 0 && (!selectedMovie || selectedMovie.length === 0)) {
                    BootstrapDialog.alert({
                        title: 'Empty Selection',
                        message: 'Please select at least one file or movie to visualize.'
                    });
                    return;
                }

                var fileIds = selectedFiles.map(function (file) {
                    return file.id;
                });
                var url = "visualize.htm?files=" + encodeURIComponent(fileIds);
                if (selectedMovie) {
                    url = url + "&movie=" + selectedMovie.id;
                }
                window.location.href = url;
            }else if (visualizeWorkload) {
                if ($scope.selectedProcessInstance.id === undefined) {
                    BootstrapDialog.alert({
                        title: 'Please Select a PPM',
                        message: 'Please select the process of process modeling to visualize.'
                    });
                    return;
                }

                var url = "visualize-workload-phase.htm?processInstance=" + $scope.selectedProcessInstance.id;
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

        /**
         * Deletes pre-processed data.
         * @param data the data to delete
         * @param container the container where the data should be deleted from
         */
        $scope.deleteData = function (data, container) {
            BootstrapDialog.show({
                title: 'Delete prepared data?',
                message: 'What is this good for?<br><br>Before CEP-Web can visualize data, it will strip all unnecessary data and save it in a new file - these are the files you can see here. This is required, since reading the data would take too long otherwise and visualizing the data would not run that fluently. If you do not need these files anymore you can delete them safely - CEP-Web will recreate them, if necessary.',
                buttons: [{
                    label: 'Yes',
                    cssClass: 'btn',
                    action: function (dialogItself) {
                        dialogItself.close();

                        $http.get('../../private/deleteData?id=' + data.id).then(function () {
                            var index = container.indexOf(data);
                            container.splice(index, 1);
                        });
                    }
                }, {
                    label: 'No',
                    cssClass: 'btn btn-primary',
                    action: function (dialogItself) {
                        dialogItself.close();
                    }
                }]
            });
        };

    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/search/search.htm',
            controller: 'SearchCtrl'
        })
});
