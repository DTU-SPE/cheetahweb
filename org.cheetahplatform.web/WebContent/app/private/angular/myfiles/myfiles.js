angular
    .module('cheetah.MyFiles', ['ngRoute'])
    .controller('MyFilesCtrl', function ($scope, $http) {

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
        $scope.search = "";
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
            'background-color': "blueviolet"
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

        /*, {
         id: 'thomas_maran',
         name: 'Thomas Maran (Experiment 1)'
         }*/
        $scope.analysisOptions = [{id: 'no_analysis', name: 'Daten nicht analysieren'}, {
            id: 'thomas_maran_persona_experiment',
            name: 'Thomas Maran (Persona Experiment)'
        }];

        if ($scope.analysisOption === undefined) {
            var previousSetting = localStorage.getItem('analysisOption');
            if (previousSetting != undefined) {
                $.each($scope.analysisOptions, function (index, option) {
                    if (option.id === previousSetting) {
                        $scope.analysisOption = option;
                        return false;
                    }
                });
                if ($scope.analysisOption === undefined) {
                    $scope.analysisOption = $scope.analysisOptions[0];
                }
            } else {
                $scope.analysisOption = $scope.analysisOptions[0];
            }
        }

        if ($scope.decimalSeparator === undefined) {
            var previousSeparator = localStorage.getItem('decimalSeparator');
            if (previousSeparator != undefined) {
                $scope.decimalSeparator = previousSeparator;
            } else {
                var tmp = 1.1;
                $scope.decimalSeparator = tmp.toLocaleString().substring(1, 2);
            }
        }

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
                    })
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

        $scope.toggleFilter = function (filter) {
            filter.selected = !filter.selected;
            filter.timestamp = new Date().valueOf();

            updateFilterColumns();
        };

        function updateFilterColumns() {
            $scope.parameters = [];

            for (var i = 0; i < $scope.filters.length; i++) {
                var filter = $scope.filters[i];
                if (filter.selected === true) {
                    for (var j = 0; j < filter.parameters.length; j++) {
                        var parameter = filter.parameters[j];
                        var found = false;
                        for (var k = 0; k < $scope.parameters.length; k++) {
                            if ($scope.parameters[k].key === parameter.key) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            $scope.parameters.push(parameter);
                        }
                    }
                }
            }

            $.each($scope.parameters, function (index, parameter) {
                var previousValue = localStorage.getItem(parameter.key);

                if (previousValue !== undefined) {
                    parameter.value = previousValue;
                } else {
                    var defaultValue = parameter.defaultValue;
                    parameter.value = defaultValue || "";
                }
            });
        }

        $scope.openPupillometryDataDialog = function () {
            $http.get("../../private/availablePupillometryFilters").success(function (filters) {
                $scope.filters = [];
                var timestamp = 9999999999900;

                $.each(filters, function (index, value) {
                    value.selected = false;
                    //initialize with timestamp for sorting. not selected is sorted to the end
                    value.timestamp = timestamp++;
                    $scope.filters.push(value);
                });
                $("#cleanPupillometryDataDialog").modal('show');
            });
        };

        function extractFiles(postData) {
            $.each($scope.files, function (index, value) {
                if (value.selection) {
                    postData.push(value.id);
                }
            });
        }

        $scope.numberOfSelectedFiles = function () {
            var selected = [];
            extractFiles(selected);
            return selected.length;
        };

        $scope.cleanPupillometryData = function () {
            var postData = {};
            postData.parameters = {};

            var parameters = $scope.parameters;
            for (var i = 0; i < $scope.parameters.length; i++) {
                var parameter = $scope.parameters[i];
                postData.parameters[parameter.key] = parameter.value;
            }

            postData.filters = [];
            $scope.filters.sort(function (a, b) {
                return a.timestamp - b.timestamp;
            });
            $.each($scope.filters, function (index, filter) {
                if (filter.selected) {
                    postData.filters.push(filter.id);

                    //store column definitions
                    $.each(filter.parameters, function (index, parameter) {
                        if (parameter.value !== undefined) {
                            localStorage.setItem(parameter.key, parameter.value);
                        }
                    });
                }
            });
            localStorage.setItem('decimalSeparator', $scope.decimalSeparator);
            localStorage.setItem('analyzeData', $scope.analyzeData);
            localStorage.setItem('analysisOption', $scope.analysisOption.id);

            postData.files = [];
            extractFiles(postData.files);
            postData.analyzeData = $scope.analysisOption.id;
            postData.decimalSeparator = $scope.decimalSeparator;

            $http.post("../../private/cleanPupillometryData", angular.toJson(postData));
            $("#cleanPupillometryDataDialog").modal('hide');
        };

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

    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/myfiles/myfiles.htm',
            controller: 'MyFilesCtrl'
        });
});