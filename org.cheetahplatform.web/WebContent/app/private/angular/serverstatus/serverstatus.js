angular.module('cheetah.ServerStatus', ['ngRoute']).controller('ServerStatusCtrl', function ($scope, $http, $interval) {
    $scope.refreshServerStatus = function () {
        $http.get("../../private/serverStatus").success(function (status) {
            $scope.status = status;
        });
    };
    $scope.refreshServerStatus();

    $scope.cancelWorkItem = function (workItem) {
        var postData = {};
        postData.workItems = [];
        postData.workItems.push({
            id: workItem.id,
            message: workItem.message,
            editable: workItem.editable
        });

        $http.post("../../private/deleteWorkItem", postData).success(function () {
            $scope.refreshServerStatus();
        });
    };

    $scope.moveWorkItemToTop = function (workItem) {
        var postData = {};
        postData.workItems = [];
        postData.workItems.push({
            id: workItem.id,
            message: workItem.message,
            editable: workItem.editable
        });

        $http.post("../../private/moveWorkItemToTop", postData).success(function () {
            $scope.refreshServerStatus();
        });
    };

    $scope.computeWorkItemClass = function (editable) {
        if (editable) {
            return "alert-success";
        }
        return "alert-info";
    };

    $interval($scope.refreshServerStatus, 1000);
}).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/serverstatus/serverstatus.htm',
            controller: 'ServerStatusCtrl'
        });
});