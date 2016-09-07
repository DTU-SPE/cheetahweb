angular.module('cheetah.CleanData', []).controller('CleanDataModalController', function ($scope, $http) {
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

    $scope.$on('cheetah-clean-data-modal.show', function () {
        $http.get("../../private/availablePupillometryFilters").success(function (filters) {
            $scope.parameters = [];
            $scope.filters = [];
            var timestamp = 9999999999900;

            $.each(filters, function (index, value) {
                value.selected = false;
                //initialize with timestamp for sorting. not selected is sorted to the end
                value.timestamp = timestamp++;
                $scope.filters.push(value);
            });
        });
    });

    $scope.cleanPupillometryData = function () {
        var parameters = {};
        $.each($scope.parameters, function (index, parameter) {
            parameters[parameter.key] = parameter.value;
        });

        var filters = [];
        $scope.filters.sort(function (a, b) {
            return a.timestamp - b.timestamp;
        });
        $.each($scope.filters, function (index, filter) {
            if (filter.selected) {
                filters.push(filter.id);

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

        var data = {
            parameters: parameters,
            filters: filters,
            analyzeData: $scope.analysisOption.id,
            decimalSeparator: $scope.decimalSeparator,
            fileNamePostFix: $scope.cleanFileNamePattern
        };
        cheetah.hideModal($scope, 'cheetah-clean-data-modal', data);
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
});
