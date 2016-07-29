var myApp = angular.module('cheetah.SubjectManagement', ['ngRoute']);
myApp.controller('SubjCtrl', function ($scope, $http, $q, $timeout) {
    $scope.subjects = [];
    $scope.search = {};
    $scope.search.freeText = "";
    $scope.selection = [];
    $scope.changeMail = "";
    $scope.highLighted = {};

    var storedSearch = localStorage.getItem('subjectMangement.search');
    if (storedSearch) {
        $scope.search = JSON.parse(storedSearch);
    }

    var subjectsRequest = $http.get("../../private/listSubjects");
    var studiesRequest = $http.get("../../private/listStudies");
    $q.all([subjectsRequest, studiesRequest]).then(function (arrayOfResults) {
        $scope.subjects = arrayOfResults[0].data;
        $scope.fullStudies = arrayOfResults[1].data;
        $scope.studies = arrayOfResults[1].data.map(function (input) {
            return input.name;
        });
    });

    $scope.isSelected = function (subject) {
        if (getSelectionIndex(subject) >= 0) {
            return "icon-cheetah-selected";
        }
        return "icon-cheetah-not-selected";
    };

    function getSelectionIndex(subject) {
        var selected = -1;
        $.each($scope.selection, function (index, element) {
            if (element.id === subject.id) {
                selected = index;
                return false;
            }
        });
        return selected;
    }

    $scope.filteredSubjects = function () {
        var filtered = $.grep($scope.subjects, function (subject) {

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
        if (getSelectionIndex(subject) >= 0) {
            return "alert-success";
        }
        if ($scope.highLighted[subject.id] === true) {
            return "cheetah-highlighted";
        }
        return "";
    };

    $scope.deselectAll = function () {
        $scope.selection = [];
    };

    $scope.selectAll = function () {
        $scope.selection = $scope.filteredSubjects();
    };

    $scope.setSelection = function (subjects) {
        var selectionIndex = getSelectionIndex(subjects);
        if (selectionIndex >= 0) {
            $scope.selection.splice(selectionIndex, 1);
        } else {
            $scope.selection.push(subjects);
        }
        if ($scope.selection.length > 0) {
            $("#button-visualize").removeAttr("disabled");
        } else {
            $("#button-visualize").attr("disabled");
        }
    };

    $scope.deleteSubject = function () {
        if ($scope.selection.length < 1) {
            BootstrapDialog.alert({
                title: 'Invalid Selection',
                message: 'At least one subject must be selected. Please adapt your selection accordingly.'
            });
            return;
        }

        var objectToDelete = $scope.selection.map(function (input) {
            return input.subjectId;
        });
        BootstrapDialog.show({
            title: 'Delete',
            message: 'Do you really want to delete subject: "' + objectToDelete + '"?',
            buttons: [{
                label: 'Yes',
                action: function (dialog) {
                    var subjectsToDelete = $scope.selection.map(function (input) {
                        return input.id;
                    });
                    $http.post('../../private/deleteSubject', subjectsToDelete).then(function (response) {
                        if (response.data != "null") {
                            var notDeleteAble = response.data;
                            var notDeleteAbleSubjects = [];
                            $.each(notDeleteAble, function (index, idOfSubject) {
                                $.each($scope.subjects, function (index, subj) {
                                    if (subj.id == idOfSubject) {
                                        notDeleteAbleSubjects.push(subj);
                                    }
                                })
                            });
                            var messageString = notDeleteAbleSubjects.map(function (input) {
                                return input.subjectId;
                            })
                            BootstrapDialog.alert({
                                title: 'Could not delete',
                                message: "Could not delete " + messageString + ". The subjects have already attached files."
                            });
                            dialog.close();
                        } else {
                            BootstrapDialog.alert({
                                title: 'Subject deleted',
                                message: 'Subject "' + objectToDelete + '" was deleted successfully.'
                            });

                            $.each($scope.selection, function (index, subject) {
                                var tmpIndex = $scope.subjects.indexOf(subject);
                                $scope.subjects.splice(tmpIndex, 1);
                            });

                            $scope.selection = [];
                            dialog.close();
                        }
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

    $scope.showChangeSubjectModal = function () {
        if ($scope.selection.length !== 1) {
            BootstrapDialog.alert({
                title: 'Select exactly one subject',
                message: 'You have selected the wrong number of subjects.'
            });
        } else {
            $scope.changeMail = $scope.selection[0].email;
            $scope.changeSubjectID = $scope.selection[0].subjectId;
            $scope.changeComment = $scope.selection[0].comment;
            $("#cheetah-change-subject-dialog").modal('show');
        }
    };

    $scope.addSub = function () {
        $scope.selectedStudy = $scope.fullStudies[0];
        $("#cheetah-create-subject-dialog").modal('show');
    };

    $scope.showUpload = function () {
        $("#cheetah-load-csv-dialog").modal('show');
    };


    $scope.changeSubject = function () {
        $("#cheetah-change-subject-dialog").modal('hide');
        var changeSubject = {
            id: $scope.selection[0].id,
            email: $scope.changeMail,
            subjectId: $scope.changeSubjectID,
            comment: $scope.changeComment
        };
        $http.post('../../private/changeSubject', changeSubject).then(function successCallback(response) {
            BootstrapDialog.alert({
                title: 'Successfully changed',
                message: 'Successfully changed.'
            });
            $scope.selection[0].email = changeSubject.email;
            $scope.selection[0].subjectId = changeSubject.subjectId;
            $scope.selection[0].comment = changeSubject.comment;
            $scope.selection = [];
        }, function errorCallback(response) {
            BootstrapDialog.alert({
                title: 'Error',
                message: 'An error occured while changing a subject.'
            });
        });
    };

    $scope.addUserToDB = function () {
        $("#cheetah-create-subject-dialog").modal('hide');
        var createdSubject = {
            email: $scope.mail,
            subjectId: $scope.subjectID,
            studyId: $scope.selectedStudy.id,
            comment: $scope.comment,
            allowDouble: false
        };
        $http.post('../../private/createSubject', createdSubject).then(function (response) {
            if (response.data.error) {
                BootstrapDialog.show({
                    title: 'Subject already exists',
                    message: 'There is already a subject in the database with the email address "' + createdSubject.email + '".\n Do you really want to add another subject with the same email address?',
                    buttons: [{
                        label: 'Yes',
                        action: function (dialogSelf) {
                            var createdSubject = {
                                email: $scope.mail,
                                subjectId: $scope.subjectID,
                                studyId: $scope.selectedStudy.id,
                                comment: $scope.comment,
                                allowDouble: true
                            };
                            $http.post('../../private/createSubject', createdSubject).then(function (response) {
                                if (response.data.error == undefined) {
                                    BootstrapDialog.alert({
                                        title: 'Subject created successfully',
                                        message: 'The subject with the email address "' + createdSubject.email + '" was created successfully.'
                                    });

                                    var createdSubjectWithPK = {
                                        id: response.data.id,
                                        email: response.data.email,
                                        subjectId: response.data.subjectId,
                                        study: $scope.selectedStudy,
                                        comment: response.data.comment
                                    };
                                    $scope.subjects.push(createdSubjectWithPK);
                                    $scope.highLighted[createdSubjectWithPK.id] = true;
                                    $timeout(function () {
                                        $scope.highLighted = {};
                                    }, 5000);
                                    sortSubjectList();
                                }
                            });
                            dialogSelf.close();
                        }
                    }, {
                        label: 'No',
                        action: function (dialogSelf) {
                            dialogSelf.close();
                        }
                    }]
                });
            } else {
                BootstrapDialog.alert({
                    title: 'Subject created successfully',
                    message: 'The subject "' + createdSubject.email + '" was created successfully.'
                });
                var createdSubjectWithPK = {
                    id: response.data.id,
                    email: response.data.email,
                    subjectId: response.data.subjectId,
                    study: $scope.selectedStudy,
                    comment: response.data.comment
                };
                $scope.subjects.push(createdSubjectWithPK);
                $scope.highLighted[createdSubjectWithPK.id] = true;
                $timeout(function () {
                    $scope.highLighted = {};
                }, 5000);
                sortSubjectList();
            }
        });

    };

    function filterAndAppend(subject, property, search) {
        if (search != null && search.trim().length > 0) {
            return filter(subject, property, search);
        }

        return true;
    }

    $scope.uploadFile = function () {
        var file = $scope.myFile;
        var postData = new FormData();
        postData.append('file', file);

        $http.post("../../private/fileUpload", postData, {
            withCredentials: false,
            headers: {
                'Content-Type': undefined
            },
            transformRequest: angular.identity
        }).then(function successCallback(response) {
            BootstrapDialog.alert({
                title: 'Successfully uploaded',
                message: 'Successfully uploaded.'
            });
            $.each(response.data.subjectList, function (index, newCreatedSubject) {
                var studyForSubject = {};
                $.each($scope.fullStudies, function (index, studyInFullStudies) {
                    if (studyInFullStudies.id === newCreatedSubject.studyId) {
                        studyForSubject = studyInFullStudies;
                        return;
                    }
                });
                var createdSubjectWithPK = {
                    id: newCreatedSubject.id,
                    email: newCreatedSubject.email,
                    subjectId: newCreatedSubject.subjectId,
                    study: studyForSubject,
                    comment: newCreatedSubject.comment
                };
                $scope.subjects.splice(0, 0, createdSubjectWithPK);
                $scope.highLighted[createdSubjectWithPK.id] = true;
            });

            $timeout(function () {
                $scope.highLighted = {};
            }, 5000);
            sortSubjectList();
            $scope.myFile = null;
            document.getElementById("uploadField").value = null;
            $("#cheetah-load-csv-dialog").modal('hide');
        }, function errorCallback(response) {
            BootstrapDialog.alert({
                title: 'Error',
                message: response.data.message
            });
        });
    };


    function filter(value, property, search) {
        var propertyToMatch = value[property];
        if (property.indexOf('.') !== -1) {
            var tokens = property.split('.');
            propertyToMatch = value[tokens[0]][tokens[1]];
        }

        propertyToMatch = propertyToMatch.toLowerCase();
        return propertyToMatch.indexOf(search.toLowerCase()) > -1;
    };

    function sortSubjectList() {
        $scope.subjects.sort(function (a, b) {
            var studyDiff = a.study.id - b.study.id;
            if (studyDiff !== 0) {
                return studyDiff;
            } else {
                return a.id - b.id;
            }
        });
    }


}).config(function ($routeProvider) {
    $routeProvider.when('/', {
        templateUrl: 'angular/subject-management/subject-management.htm',
        controller: 'SubjCtrl'
    });
});

myApp.directive('fileModel', ['$parse', function ($parse) {
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