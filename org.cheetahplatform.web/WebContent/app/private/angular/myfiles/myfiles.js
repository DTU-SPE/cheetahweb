myFilesApp=angular.module('cheetah.MyFiles', ['ngRoute', 'cheetah.CleanData']);
myFilesApp.controller('MyFilesCtrl', function ($rootScope, $scope, $http) {
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
    $scope.cleanFileNamePattern = "";
    $scope.search = "";
    $scope.studies = [];
    $scope.selectedStudy;
    $scope.timeslots=[];
    $scope.labelList=[];
    $scope.radio ={};
    $scope.radioLabeled ={};

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

    var storedStudy = localStorage.getItem('myfiles.selectedStudy');
    if (storedStudy) {
        var studyId = parseInt(storedStudy, 10);
        $scope.selectedStudy = studyId;
    }

    $scope.trimTimestampColumn = localStorage.getItem('trimTimestampColumn');

    $scope.renameFiles = function () {
        var data = {};
        data.filesToRename = [];
        $.each($scope.files, function (index, file) {
            if (file.selection) {
                data.filesToRename.push({id: file.id, oldName: file.filename, newName: file.filename});
            }
        });
        cheetah.showModal($rootScope, 'cheetah-rename-files', data);
    };

    $scope.$on('cheetah-rename-files.hide', function () {
        $scope.refreshFiles();
    });

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
                var link = document.createElement("a");
                link.download = value.filename;
                link.href = value.url;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
                delete link;
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

            return $scope.selectedStudy === file.studyId;
        }

        var filtered = $.grep($scope.files, function (file, index) {
            if (!matchesStudy(file)) {
                return false;
            }

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
        localStorage.setItem('myfiles.selectedStudy', $scope.selectedStudy);
        return filtered;
    };

    $scope.openConnectToSubjectDialog = function () {
        $("#connectToSubjectDialog").modal();
    };
    $scope.openCalculationsForPhases = function () {
        var itemTimeStamp = localStorage.getItem("timestampColumn");
        if(itemTimeStamp === undefined){
            timeStampStoredValue=undefined;
        }else{
            timeStampStoredValue= itemTimeStamp;
        }
        var itemLeftPupil = localStorage.getItem("leftPupilColumn");
        if(itemLeftPupil === undefined){
            leftPupilStoredValue=undefined;

        }else{
            leftPupilStoredValue= itemLeftPupil;
        }
        var itemRightPupil = localStorage.getItem("rightPupilColumn");
        if(itemRightPupil === undefined){
            rightPupilStoredValue=undefined;
        }else{
            rightPupilStoredValue=itemRightPupil
        }

        $scope.leftPupilColumnForCalculationsForPhases=leftPupilStoredValue;
        $scope.rightPupilColumnForCalculationsForPhases=rightPupilStoredValue;
        $scope.timeColumnForCalculationsForPhases=timeStampStoredValue;
        $("#calculationsForPhasesDialog").modal();
    };

    $scope.openCalculationsForLabeledPhases = function () {

        $scope.leftPupilColumnForCalculationsForLabeledPhases = localStorage.getItem("leftPupilColumnLabeled");
        $scope.rightPupilColumnForCalculationsForLabeledPhases = localStorage.getItem("rightPupilColumnLabeled");
        $scope.labelColumnForCalculationsForLabeledPhases = localStorage.getItem("labelColumnLabeled");

        $("#calculationsForLabeledPhasesDialog").modal();
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

    $scope.addRow = function () {
        if(isNaN($scope.startTime) ||  isNaN($scope.endTime) ){
            BootstrapDialog.alert({
                title: 'Error',
                message: 'Fields for StartTime and EndTime must contains numbers.'
            });
            return;
        }

        if(isNaN($scope.slotLabel.trim()=="")){
            BootstrapDialog.alert({
                title: 'Error',
                message: 'All fields must contain values.'
            });
            return;
        }

        if($scope.startTime > $scope.endTime){
            BootstrapDialog.alert({
                title: 'Error',
                message: 'First argument must be smaller than second.'
            });
            return;
        }

        if(checkDoubleOccurance($scope.slotLabel.trim())){
            BootstrapDialog.alert({
                title: 'Error',
                message: 'Name for slice already exists.'
            });
            return;
        }

        $scope.timeslots.push({start:$scope.startTime, end:$scope.endTime, slotLabel:$scope.slotLabel});
        $scope.startTime="";
        $scope.endTime="";
        $scope.slotLabel="";
        var optionValue = $scope.radio;
        console.log(optionValue);
    }

    $scope.addLablesRow = function () {

        if(isNaN($scope.label.trim()=="")){
            BootstrapDialog.alert({
                title: 'Error',
                message: 'Field must contain values.'
            });
            return;
        }


        $scope.labelList.push({label:$scope.label});
        $scope.label="";
        var optionValue = $scope.radio;
        console.log(optionValue);
    }

    function checkDoubleOccurance(name){
        for(enVal in $scope.timeslots){
            if (name==$scope.timeslots[enVal].slotLabel){
                return true;
            }
        }
        return false;
    }

    $scope.delRow = function(elementOne, elementTwo){
        for(var i = $scope.timeslots.length - 1; i >= 0; i--) {
            if($scope.timeslots[i][0] === elementOne && $scope.timeslots[i][1] === elementTwo) {
                $scope.timeslots.splice(i, 1);
                break;
            }
        }
    }

    $scope.delLablesRow= function(element){
        for(var i = $scope.labelList.length - 1; i >= 0; i--) {
        if($scope.labelList[i][0] === element) {
            $scope.labelList.splice(i, 1);
            break;
        }
    }
}


    $scope.submitCalculationsForPhases = function () {
        var postData = {};
        postData.fileIds = [];
        extractFiles(postData.fileIds);
        postData.leftPupilColumn = $scope.leftPupilColumnForCalculationsForPhases;
        postData.rightPupilColumn = $scope.rightPupilColumnForCalculationsForPhases;
        postData.timeStampsColumn = $scope.timeColumnForCalculationsForPhases;
        postData.baseline = $scope.radio.baseline;
        localStorage.setItem("timestampColumn", $scope.timeColumnForCalculationsForPhases);
        localStorage.setItem("leftPupilColumn", $scope.leftPupilColumnForCalculationsForPhases);
        localStorage.setItem("rightPupilColumn", $scope.rightPupilColumnForCalculationsForPhases);
        postData.timeSlots = $scope.timeslots;
        $http.post("../../private/sequencesOfPupillometry", angular.toJson(postData)).then(function (response) {
            $("#calculationsForPhasesDialog").modal('hide');
        });
    };


    $scope.submitCalculationsForLabeledPhases = function () {
        var postData = {};
        postData.fileIds = [];
        extractFiles(postData.fileIds);
        postData.leftPupilColumn = $scope.leftPupilColumnForCalculationsForLabeledPhases;
        postData.rightPupilColumn = $scope.rightPupilColumnForCalculationsForLabeledPhases;
        postData.labelColumn = $scope.labelColumnForCalculationsForLabeledPhases;
        postData.baseline = $scope.radioLabeled.baseline;
        localStorage.setItem("labelColumnLabeled", $scope.labelColumnForCalculationsForLabeledPhases);
        localStorage.setItem("leftPupilColumnLabeled", $scope.leftPupilColumnForCalculationsForLabeledPhases);
        localStorage.setItem("rightPupilColumnLabeled", $scope.rightPupilColumnForCalculationsForLabeledPhases);
        postData.labelList = $scope.labelList;
        $http.post("../../private/sequencesOfPupillometryLabeled", angular.toJson(postData)).then(function (response) {
            $("#calculationsForLabeledPhasesDialog").modal('hide');
        });
    };

    $scope.openCalculationsForPhasesCSV= function () {
        $("#CalculationsForPhasesCSV").modal('show');
    };
    $scope.uploadFile = function () {
        var file = $scope.myFileCSV;
        var postData = new FormData();
        postData.append('file', file);

        $http.post("../../private/sequencesOfPupillometryForCSV", postData, {
            withCredentials: false,
            headers: {
                'Content-Type': undefined
            },
            transformRequest: angular.identity
        }).then(function (response) {
            if (response.data.message == null) {
                BootstrapDialog.alert({
                    title: 'Successfully uploaded',
                    message: 'Successfully uploaded.'
                });

                $("#CalculationsForPhasesCSV").modal('hide');
            } else {
                BootstrapDialog.alert({
                    title: 'Error',
                    message: response.data.message
                });
                $("#CalculationsForPhasesCSV").modal('hide');
            }
        });
        console.log(file)
    };
    $scope.uncheck = function (event) {
        if ($scope.radio.baseline == event.target.value)
            $scope.radio.baseline = false
    }

    $scope.uncheckLabeled = function (event) {
        if ($scope.radioLabeled.baseline == event.target.value)
            $scope.radioLabeled.baseline = false
    }


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

}).controller('RenameFilesController', function ($rootScope, $scope, $http) {
    $scope.$on('cheetah-rename-files.show', function (event, data) {
        $scope.filesToRename = data.filesToRename;
    });

    $scope.submitRenameFiles = function () {
        var postData = {files: {}};
        $.each($scope.filesToRename, function (index, file) {
            postData.files[file.id] = file.newName;
        });

        $http.post('../../private/renameFiles', postData).success(function () {
            cheetah.hideModal($scope, 'cheetah-rename-files');
        });
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
            });

            if ($scope.selectedStudy.dataProcessing.length > 0) {
                $scope.selectedDataProcessing = $scope.selectedStudy.dataProcessing[0];
            }
        }
    });

    $scope.startProcessing = function () {
        var postData = {};
        postData.fileIds = [];
        postData.dataProcessingId = $scope.selectedDataProcessing.id;
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

myFilesApp.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function () {
                scope.$apply(function () {
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);