angular
    .module('cheetah.SynchronizeData', ['ngRoute'])
    .controller('SynchronizeDataCtrl', function ($scope, $http) {
        $http.get('../../private/getDatabaseConfigurations').then(function (response) {
            $scope.databaseConfigurations = response.data;
        });

        $scope.addDatabase = function () {
            var toAdd = $scope.newDatabase;
            var port = Number(toAdd.port);
            if (port % 1 !== 0) {
                BootstrapDialog.alert({message: 'Please enter the port as number.'});
                return;
            }

            var newConfiguration = {
                host: toAdd.host,
                port: port,
                schema: toAdd.schema,
                username: toAdd.username,
                password: toAdd.password
            };

            $http.post('../../private/addDatabaseConfiguration', newConfiguration).then(function (response) {
                if (response.data.message) {
                    BootstrapDialog.alert({message: 'Could not add the database configuration. Error message:\n\n' + response.data.message});
                } else {
                    $scope.databaseConfigurations.push(response.data);
                    $scope.newDatabase = {};
                }
            });
        };

        $scope.deleteDatabaseConfiguration = function (configuration) {
            BootstrapDialog.show({
                title: 'Remove Database Configuration',
                message: 'Do you really want to delete this database configuration?',
                buttons: [{
                    label: 'Yes',
                    action: function (dialog) {
                        $http.get('../../private/deleteDatabaseConfiguration?configurationId=' + configuration.id).then(function () {
                            $scope.databaseConfigurations = $scope.databaseConfigurations.filter(function (current) {
                                return current.id !== configuration.id;
                            });
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

        $scope.showSyncronizationDialog = function (configuration) {
            $scope.activeDatabase = configuration;

            $http.post('../../private/analyzeDatabaseForSynchronization', configuration).then(function (response) {
                $scope.activeStudies = response.data;
                $('#cheetah-synchronize-modal').modal();
            });
        };

        $scope.synchronizeData = function (configuration) {
            var selectedStudies = $scope.activeStudies.filter(function (study) {
                return study.selected;
            }).map(function (study) {
                return {id: study.id, name: study.name};
            });

            if (selectedStudies.length === 0) {
                BootstrapDialog.alert({message: 'Please select at least one study to synchronize.'});
                return;
            }

            var postData = {
                configuration: configuration,
                studies: selectedStudies
            };
            $('#cheetah-synchronize-modal').modal('hide');
            $('#cheetah-synchronize-progress-modal').modal({backdrop: 'static'});
            $http.post('../../private/synchronizeStudies', postData).then(function (response) {
                $('#cheetah-synchronize-progress-modal').modal('hide');

                $scope.results = response.data.all;
                $('#cheetah-synchronize-finished-modal').modal('show');
            });
        };

    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/synchronize-data/synchronize-data.htm',
            controller: 'SynchronizeDataCtrl'
        });
});