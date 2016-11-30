angular.module('cheetah.CleanData', []).controller('CleanDataModalController', function ($scope, $http) {
    if ($scope.decimalSeparator === undefined) {
        var previousSeparator = localStorage.getItem('decimalSeparator');
        if (previousSeparator != undefined) {
            $scope.decimalSeparator = previousSeparator;
        } else {
            var tmp = 1.1;
            $scope.decimalSeparator = tmp.toLocaleString().substring(1, 2);
        }
    }

    $scope.$on('cheetah-clean-data-modal.show', function (event, data) {
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

            //show predefined data, if provided
            if (data) {
                if (data.parameters) {
                    var allParameters = {};
                    $.each($scope.filters, function (index, filter) {
                        $.each(filter.parameters, function (index, parameter) {
                            allParameters[parameter.key] = parameter;
                        });
                    });

                    $.each(data.parameters, function (key, value) {
                        var preselectedParameter = allParameters[key];
                        preselectedParameter.value = value;
                        $scope.parameters.push(preselectedParameter);
                    });
                }
                if (data.filters) {
                    $.each($scope.filters, function (index, filter) {
                        if (data.filters.indexOf(filter.id) !== -1) {
                            filter.selected = true;
                        }
                    });
                }
                if (data.decimalSeparator) {
                    $scope.decimalSeparator = data.decimalSeparator;
                }
                if (data.fileNamePostFix) {
                    $scope.fileNamePostFix = data.fileNamePostFix;
                }
            }
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

        var data = {
            parameters: parameters,
            filters: filters,
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
