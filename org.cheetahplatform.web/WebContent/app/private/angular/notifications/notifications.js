angular
    .module('cheetah.Notifications', ['ngRoute'])
    .controller('NotificationsCtrl', function ($scope, $http) {
        var NOTIFICATION_SEARCH_LOCAL_STORAGE = 'cheetah.notifications.search';

        $http.useXDomain = true;
        $http.withCredentials = true;
        $scope.search = "";
        $scope.notifications = [];

        var storedSearch = localStorage.getItem(NOTIFICATION_SEARCH_LOCAL_STORAGE);
        if (storedSearch) {
            $scope.search = JSON.parse(storedSearch);
        }

        $scope.filteredNotifications = function () {
            function filter(searchProperty) {
                if (searchProperty === null) {
                    return false;
                }

                var searchString = $scope.search.toLowerCase();
                if (searchString === "error") {
                    searchString = "danger";
                }

                return searchProperty.toLowerCase().indexOf(searchString) > -1;
            }

            var filtered = $.grep($scope.notifications, function (notification, index) {
                return filter(notification.message) || filter(notification.type);
            });

            localStorage.setItem(NOTIFICATION_SEARCH_LOCAL_STORAGE, JSON.stringify($scope.search));
            return filtered;
        };

        $http.get("../../private/notifications?onlyUnread=false").success(function (files) {
            $scope.notifications = files;
        });

        $scope.toggleReadStatus = function (notification) {
            notification.read = !notification.read;
            $.post("../../private/saveNotification", angular.toJson(notification));
        };

        $scope.getNotificationIcon = function (notification) {
            var icon = "notification_icon";
            if (notification.read) {
                return icon + ' icon-mail-read';
            }
            return icon + ' icon-mail-unread';
        };

        $scope.deleteNotifications = function () {
            $http.get("../../private/clearNotifications").success(function (notifications) {
                $scope.notifications = notifications;
            });
        };

        $scope.openClearNotificationsPromt = function () {
            BootstrapDialog.show({
                title: 'Clear Notifications',
                message: 'Do you really want to remove all notifications?',
                buttons: [{
                    label: 'Yes, get rid of them!',
                    cssClass: 'btn',
                    action: function (dialogItself) {
                        dialogItself.close();
                        $scope.deleteNotifications();
                    }
                }, {
                    label: 'Hell, no!',
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
            templateUrl: 'angular/notifications/notifications.htm',
            controller: 'NotificationsCtrl'
        });
});