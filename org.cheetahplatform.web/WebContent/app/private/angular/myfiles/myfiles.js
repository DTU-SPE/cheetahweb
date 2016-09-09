angular.module('cheetah.MyFiles', ['ngRoute', 'cheetah.CleanData']).controller('MyFilesCtrl', function ($rootScope, $scope, $http) {
    $scope.columns = {timestamp: "", leftPupil: "", rightPupil: ""};
    $scope.connectTimestampColumn = "EyeTrackerTimestamp";
    $scope.connectLeftPupilColumnName = "PupilLeft";
    $scope.connectRightPupilColumnName = "PupilRight";
    $scope.leftPupilColumnForCognitiveLoad = "PupilLeft";
    $scope.rightPupilColumnForCognitiveLoad = "PupilRight";
    $scope.files = [];
    $scope.defaultTags = [];
    $scope.tagsToAdd = [];
    $scope.tagsOfSelectedFiles = [];
    $scope.tagsToRemove = [];
    $scope.filesToRename = [];
    $scope.cleanFileNamePattern = "";
    $scope.search = "";
    $scope.studies = [];
    $scope.selectedStudy;

    //include default tags with fixed colors.
    $scope.tagColors = {
        "raw-data": {
            color: "white",
            'background-color': "darksalmon"
        },
        trimmed: {
            color: "white",
            'background-color': "darkturquoise"
        },
        cleaned: {
            color: "white",
            'background-color': "green"
        },
        video: {
            color: "white",
            'background-color': "darkviolet"
        },
        result: {
            color: "black",
            'background-color': "lightgreen"
        },
        "study-data-processed": {
            color: "white",
            'background-color': "darkorange"
        }
    };
    $scope.availableColors = [{color: "black", 'background-color': "pink"}, {
        color: "white",
        'background-color': "darkorange"
    }, {
        color: "white",
        'background-color': "darkblue"
    }, {
        color: "white",
        'background-color': "darkcyan"
    }, {
        color: "white",
        'background-color': "darkgoldenrod"
    }, {
        color: "black",
        'background-color': "aqua"
    }, {
        color: "white",
        'background-color': "cornflowerblue"
    }, {
        color: "white",
        'background-color': "darkkhaki"
    }];

    $http.get("../../private/settings").success(function (settings) {
        $scope.settings = settings;
    });

    var storedSearch = localStorage.getItem('myfiles.search');
    if (storedSearch) {
        $scope.search = JSON.parse(storedSearch);
    }
    $scope.trimTimestampColumn = localStorage.getItem('trimTimestampColumn');

    $scope.renameFiles = function () {
        $scope.filesToRename = [];
        $.each($scope.files, function (index, file) {
            if (file.selection) {
                $scope.filesToRename.push({id: file.id, oldName: file.filename, newName: file.filename});
            }
        });
        $("#renameFilesDialog").modal();
    };

    $scope.openExecuteDataProcessDialog = function () {
        var data = {};
        data.selectedFiles = [];
        getSelectedFiles(data.selectedFiles);
        data.selectedStudy = undefined;
        if ($scope.selectedStudy) {
            $.each($scope.studies, function (index, study) {
                if (study.id === parseInt($scope.selectedStudy, 10)) {
                    data.selectedStudy = study;
                    return false;
                }
            });
        }
        cheetah.showModal($rootScope, 'cheetah-execute-data-processing', data);
    };

    $scope.submitRenameFiles = function () {
        var postData = {files: {}};
        $.each($scope.filesToRename, function (index, file) {
            postData.files[file.id] = file.newName;
        });

        $http.post('../../private/renameFiles', postData).success(function () {
            $("#renameFilesDialog").modal('hide');
            $scope.refreshFiles();
        });
    };

    $scope.selectAllDisplayedFiles = function () {
        $.each($scope.filteredFiles(), function (index, file) {
            file.selection = true;
        });
    };

    $scope.deselectAllFiles = function () {
        $.each($scope.files, function (index, file) {
            file.selection = false;
        });
    };

    $scope.refreshFiles = function () {
        $http.get("../../private/myfiles").success(function (files) {
            $scope.files = files;
        });
    };

    $scope.refreshFiles();

    $http.get("../../private/listStudies").success(function (studies) {
        $scope.studies = studies;
    });

    $http.get("../../private/defaultUserDataTags").success(function (defaultTags) {
        $scope.defaultTags = defaultTags;
    });

    $scope.downloadFiles = function () {
        $.each($scope.files, function (index, value) {
            if (value.selection) {
                window.open(value.url, '_blank');
            }
        })
    };

    $scope.addCustomTag = function () {
        var tag = $('#cheetah-files-add-tag-input').val();
        $scope.addTagToBeAdded(tag);
        $('#cheetah-files-add-tag-input').val("");
    };

    $scope.addTagToBeAdded = function (tag) {
        tag = tag.split(" ").join("-");

        if ($.inArray(tag, $scope.tagsToAdd) !== -1) {
            return;
        }

        $scope.tagsToAdd.push(tag);
    };

    $scope.addTagToBeRemoved = function (tag) {
        $scope.tagsToRemove.push(tag);
    };

    $scope.removeTagToBeRemoved = function (tag) {
        $scope.tagsToRemove.splice($.inArray(tag, $scope.tagsToRemove), 1);
    };

    $scope.submitRemoveTagFromFiles = function () {
        var postData = {};
        postData.fileIds = [];
        extractFiles(postData.fileIds);
        postData.tags = $scope.tagsToRemove;

        $http.post("../../private/removeUserDataTags", postData).success(function () {
            $scope.refreshFiles();
            $("#removeFileTags").modal('hide');
            $scope.tagsToRemove = [];
        });
    };

    $scope.openRemoveTagsDialog = function () {
        $scope.tagsOfSelectedFiles = [];
        $.each($scope.files, function (index, value) {
            if (value.selection) {
                $.each(value.tags, function (index, tag) {
                    if ($.inArray(tag.tag, $scope.tagsOfSelectedFiles) === -1) {
                        $scope.tagsOfSelectedFiles.push(tag.tag);
                    }
                });
            }
        });

        $("#removeFileTags").modal();
    };

    $scope.submitTagsToAdd = function () {
        var postData = {};
        postData.fileIds = [];
        extractFiles(postData.fileIds);
        postData.tags = $scope.tagsToAdd;

        $http.post("../../private/addUserDataTags", postData).success(function () {
            $scope.refreshFiles();
            $("#addFileTags").modal('hide');
            $scope.tagsToAdd = [];
        });
    };

    $scope.removeTagToBeAdded = function (tag) {
        $scope.tagsToAdd.splice($.inArray(tag, $scope.tagsToAdd), 1);
    };

    $scope.openPupillometryDataDialog = function () {
        cheetah.showModal($rootScope, 'cheetah-clean-data-modal');
    };

    function extractFiles(postData) {
        $.each($scope.files, function (index, value) {
            if (value.selection) {
                postData.push(value.id);
            }
        });
    }

    function getSelectedFiles(selectedFiles) {
        $.each($scope.files, function (index, file) {
            if (file.selection) {
                selectedFiles.push(file);
            }
        });
    }

    $scope.numberOfSelectedFiles = function () {
        var selected = [];
        extractFiles(selected);
        return selected.length;
    };

    $scope.$on('cheetah-clean-data-modal.hide', function (event, data) {
        data.files = [];
        extractFiles(data.files);
        $http.post("../../private/cleanPupillometryData", angular.toJson(data));
    });

    $scope.openAddTagsDialog = function () {
        $("#addFileTags").modal();
    };

    $scope.openTrimToPpmInstnceDialog = function () {
        $("#trimToPpmInstanceDialog").modal();
    };

    $scope.filteredFiles = function () {
        function filter(searchProperty) {
            if (searchProperty === null) {
                return false;
            }
            return searchProperty.toLowerCase().indexOf($scope.search.toLowerCase()) > -1;
        }

        function matchesTag(file, searchString) {
            for (var i = 0; i < file.tags.length; i++) {
                if (file.tags[i].tag === searchString) {
                    return true;
                }
            }
            return false;
        }

        function matchesStudy(file) {
            if (!$scope.selectedStudy) {
                return true;
            }

            if (!file.studyId) {
                return false;
            }

            return $scope.selectedStudy === file.studyId.toString();
        }

        var filtered = $.grep($scope.files, function (file, index) {
            var searchString = $scope.search.toLowerCase();
            var splitted = searchString.split(" ");
            var tagFound = true;
            for (var j = 0; j < splitted.length; j++) {
                tagFound = tagFound && matchesTag(file, splitted[j]);
                if (!tagFound) {
                    break;
                }
            }

            if (tagFound) {
                return true;
            }

            if (!matchesStudy(file)) {
                return false;
            }

            return filter(file.filename) || filter(file.type) || filter(file.comment);
        });

        localStorage.setItem('myfiles.search', JSON.stringify($scope.search));

        return filtered;
    };

    $scope.openConnectToSubjectDialog = function () {
        $("#connectToSubjectDialog").modal();
    };
    $scope.openCalculateAverageLoadDialog = function () {
        $("#calculateAverageLoadDialog").modal();
    };

    $scope.openDeleteDialog = function () {
        $("#deleteConfirmationDialog").modal();
    };

    $scope.deleteFiles = function () {
        var files = [];
        extractFiles(files);
        $http.post("../../private/deleteUserFiles", angular.toJson(files)).success(function (data) {
                $scope.files = data;
                $("#deleteConfirmationDialog").modal('hide');
            }
        );
    };

    $scope.connectToPpmInstance = function () {
        var request = {};
        request.timestampColumn = $scope.connectTimestampColumn;
        request.leftPupilColumn = $scope.connectLeftPupilColumnName;
        request.rightPupilColumn = $scope.connectRightPupilColumnName;
        request.files = [];
        extractFiles(request.files);
        $http.post("../../private/connect", angular.toJson(request));
        $("#connectToSubjectDialog").modal('hide');
    };

    $scope.openTrimToPpmInstanceDialog = function () {
        var listActivitiesRequest = {files: []};
        extractFiles(listActivitiesRequest.files);
        $http.post("../../private/listExperimentActivities", angular.toJson(listActivitiesRequest)).then(function (response) {
            if (response.data.error) {
                BootstrapDialog.alert({message: response.data.error});
            } else {
                $scope.codeToWorkflowActivities = response.data.codeToWorkflowActivities;
                $("#trimToPpmInstanceDialog").modal('show');
            }
        });
    };

    $scope.submitCalculateAverageLoad = function () {
        var postData = {};
        postData.fileIds = [];
        extractFiles(postData.fileIds);
        postData.leftPupilColumn = $scope.leftPupilColumnForCognitiveLoad;
        postData.rightPupilColumn = $scope.rightPupilColumnForCognitiveLoad;

        $http.post("../../private/calculateAverageLoadForTsvFile", angular.toJson(postData)).then(function (response) {
            $("#calculateAverageLoadDialog").modal('hide');
        });
    };

    $scope.trimToPpmInstance = function () {
        var activities = [];
        $('#trimToPpmInstanceIds input:checked').each(function (index, input) {
            var $input = $(input);
            activities.push({code: $input.data('code'), activity: $input.data('activity')});
        });
        if (activities.length === 0) {
            BootstrapDialog.alert({message: 'Please select at least one experiment activity.'});
            return;
        }

        localStorage.setItem('trimTimestampColumn', $scope.trimTimestampColumn);
        var request = {};
        request.timestampColumn = $scope.trimTimestampColumn;
        request.files = [];
        extractFiles(request.files);
        request.activities = activities;

        $http.post("../../private/trimToPpmInstance", angular.toJson(request));
        $("#trimToPpmInstanceDialog").modal('hide');
    };

    $scope.getTagColorsForTagName = function (tagName) {
        var colors = $scope.tagColors[tagName];
        if (colors === undefined) {
            if ($scope.availableColors.length > 0) {
                colors = $scope.availableColors.pop();
            } else {
                //ok I ran out of beautiful color combinations
                colors = {
                    color: "white",
                    'background-color': "black"
                }
            }
            $scope.tagColors[tagName] = colors;
        }

        return "color: " + colors.color + "; background-color: " + colors['background-color'] + ";";
    };

    $scope.getTagColors = function (tag) {
        return $scope.getTagColorsForTagName(tag.tag);
    };

    $scope.setSelection = function (file, event) {
        //prevent multiple calls when clicking on check box
        var target = event.target;
        if (target.type === 'checkbox') {
            return;
        }
        file.selection = !file.selection;
    };

    $scope.openConnectVideotoPpmInstance = function () {
        $("#connectVideoToPpmInstanceDialog").modal();
        if ($scope.connectVideoTimestampColumn === undefined) {
            $scope.connectVideoTimestampColumn = "EyeTrackerTimestamp";
        }
    };

    $scope.connectVideoToPpmInstance = function () {
        var request = {};
        request.files = [];
        extractFiles(request.files);
        request.timestampColumn = $scope.connectVideoTimestampColumn;

        $http.post("../../private/connectVideoToPpmInstance", angular.toJson(request));

        $("#connectVideoToPpmInstanceDialog").modal('hide');
    };

}).controller('ExecuteDataProcessingController', function ($rootScope, $scope, $http) {
    $scope.$on('cheetah-execute-data-processing.show', function (event, data) {
        $scope.selectedFiles = data.selectedFiles;
        $scope.selectedStudy = data.selectedStudy;
        $scope.containsFilesFromOtherStudy = false;
        $scope.selectedDataProcessing = undefined;

        if ($scope.selectedStudy) {
            $.each($scope.selectedFiles, function (index, file) {
                if (file.studyId !== $scope.selectedStudy.id) {
                    $scope.containsFilesFromOtherStudy = true;
                    return false;
                }
            })
        }
    });

    $scope.startProcessing = function () {
        var postData = {};
        postData.fileIds = [];
        postData.dataProcessingId = parseInt($scope.selectedDataProcessing, 10);
        postData.studyId = $scope.selectedStudy.id;

        $.each($scope.selectedFiles, function (index, file) {
            if (file.studyId === $scope.selectedStudy.id) {
                postData.fileIds.push(file.id);
            }
        });


        $http.post('../../private/executeDataProcessingStep', postData).success(function () {
            cheetah.hideModal($scope, 'cheetah-execute-data-processing');
        });
    };

    $scope.closeDialog = function () {
        cheetah.hideModal($scope, 'cheetah-execute-data-processing');
    }
}).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/myfiles/myfiles.htm',
            controller: 'MyFilesCtrl'
        });
});