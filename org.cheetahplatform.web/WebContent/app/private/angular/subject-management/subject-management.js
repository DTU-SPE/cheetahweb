angular.module('cheetah.SubjectManagement', ['ngRoute'])
    .controller('SubjCtrl', function ($scope, $http, $q) {
        $scope.subjects = [];
        $scope.search = {};
        $scope.search.freeText = "";
        $scope.selection = [];
        $scope.changeMail="";

        var storedSearch = localStorage.getItem('subjectMangement.search');
        if (storedSearch) {
            $scope.search = JSON.parse(storedSearch);
        }

        var subjectsRequest = $http.get("../../private/listSubjects");
        var studiesRequest = $http.get("../../private/listStudies");
        $q.all([subjectsRequest, studiesRequest]).then(function (arrayOfResults) {
            $scope.subjects = arrayOfResults[0].data;
            $scope.fullStudies = arrayOfResults[1].data;
            $scope.studies= arrayOfResults[1].data.map(function (input) {
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
                            if (response.data!="null") {
                                var notDeleteAble = response.data;
                                var notDeleteAbleSubjects =[];
                                $.each(notDeleteAble, function (index, idOfSubject) {
                                    $.each( $scope.subjects, function (index, subj) {
                                        if(subj.id==idOfSubject){
                                            notDeleteAbleSubjects.push(subj);
                                        }
                                    })
                                });
                                var messageString = notDeleteAbleSubjects.map(function (input) {
                                    return input.subjectId;
                                })
                                BootstrapDialog.alert({
                                    title: 'Could not delete',
                                    message: "Could not delete "+ messageString + ". The subjects have already attached files."
                                });
                                dialog.close();
                            }else {
                                BootstrapDialog.alert({
                                    title: 'Subject deleted',
                                    message: 'Subject "' + objectToDelete + '" was deleted successfully.'
                                });

                                $.each($scope.selection, function (index, subject) {
                                    var tmpIndex = $scope.subjects.indexOf(subject);
                                    $scope.subjects.splice(tmpIndex, 1);
                                });

                                $scope.selection = [];
                                $scope.studies = uniqueProperties($scope.subjects, "study.name");
                                dialog.close();
                            }});
                    }
                }, {
                    label: 'No',
                    action: function (dialog) {
                        dialog.close();
                    }
                }]
            });
        };

        $scope.changeSubject= function () {
            if ($scope.selection.length != 1) {
                BootstrapDialog.alert({
                    title: 'Select exactly one subject',
                    message: 'You have selected the wrong number of subjects.'
                });

            }else {
                $scope.changeMail = $scope.selection[0].email;
                $scope.changeSubjectID=$scope.selection[0].subjectId;
                $scope.changeComment=$scope.selection[0].comment;
                $("#cheetah-change-subject-dialog").modal('show');
            }};

        $scope.addSub = function () {
            $("#cheetah-create-subject-dialog").modal('show');
        };


        $scope.changeUser=function () {
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

                $scope.selection[0].email= changeSubject.email;
                $scope.selection[0].subjectId= changeSubject.subjectId;
                $scope.selection[0].comment=changeSubject.comment;
                $scope.selection = [];
            }, function errorCallback(response)  {
                BootstrapDialog.alert({
                    title: 'no success',
                    message: 'no succ.'
                });


            });


        }
        $scope.addUserToDB = function () {
            $("#cheetah-create-subject-dialog").modal('hide');
            var createdSubject = {
                email: $scope.mail,
                subjectId: $scope.subjectID,
                studyId: $scope.selectedStudy.id,
                comment: $scope.comment
            };
            $http.post('../../private/createSubject', createdSubject).then(function (response) {
                if (response.data.error) {
                    BootstrapDialog.alert({
                        title: 'Subject already exists',
                        message: 'The subject "' + createdSubject.email + '" already exists.'
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
                    $scope.studies = uniqueProperties($scope.subjects, "study.name");
                }
            });

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
    $routeProvider.when('/', {
        templateUrl: 'angular/subject-management/subject-management.htm',
        controller: 'SubjCtrl'
    });
});
