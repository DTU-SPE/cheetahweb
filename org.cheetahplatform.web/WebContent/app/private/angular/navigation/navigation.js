angular
    .module('cheetah.Navigation', ['ngRoute'])
    .controller('NavigationCtrl', function ($scope, $http) {
        var SHOW_PPM_FEATURES = 'cheetah.web.show-ppm';
        $scope.settings = {};
        $scope.menu = [
            {
                id: "cheetah-home",
                url: "notifications.htm",
                label: "Home",
                filters: [],
                subItems: []
            }, {
                id: "cheetah-subject",
                url: "subject-management.htm",
                label: "Subject Management",
                filters: [],
                subItems: []
            }, {
                id: "cheetah-analyze",
                url: "analyze.htm",
                label: "Analyze",
                filters: [],
                subItems: []
            }, {
                id: "cheetah-data-management",
                url: "notifications.htm",
                label: "Data Management",
                filters: [],
                subItems: [{
                    id: "cheetah-upload-data",
                    url: "uploaddata.htm",
                    label: "Upload Data",
                    filters: []
                }, {
                    id: "cheetah-my-data",
                    url: "mydata.htm",
                    label: "My Data",
                    filters: []
                }, {
                    id: "cheetah-synchronize-data",
                    url: "synchronize-data.htm",
                    label: "Synchronize Data",
                    filters: [SHOW_PPM_FEATURES]
                }]
            }, {
                id: "cheetah-contact",
                url: "contact.htm",
                label: "Contact",
                filters: [],
                subItems: []
            }
        ];

        $scope.setActive = function (id) {
            $scope.activeMenuItem = id;
        };

        $http.get("../../private/settings").success(function (settings) {
            $scope.settings = settings;
        });

        $http.get("../../private/getUserInfo").success(function (data) {
            $scope.user = data;
        });

        $scope.changePassword = function () {
            $("#changePasswordDialog").modal({keyboard: false});
        };

        $scope.filter = function (menuItems) {
            var filteredMenuItems = [];
            $.each(menuItems, function (index, item) {
                var matchesAll = true;
                $.each(item.filters, function (index, filter) {
                    if ($scope.settings[filter] !== 'true') {
                        matchesAll = false;
                        return false;
                    }
                });
                if (matchesAll) {
                    filteredMenuItems.push(item);
                }
            });

            return filteredMenuItems;
        };

        $scope.sendPassword = function () {
            var user = $scope.user.email;
            var oldPassword = $("#oldPassword").val();
            var newPassword = $("#newPassword").val();
            var confirmedPassword = $("#newPasswordConfirmation").val();
            if (newPassword !== confirmedPassword) {
                $("#changePasswordErrorPasswordNotMatchingDialog").modal();
                return;
            }

            var realm = 'myrealm';
            var oldHash = CryptoJS.MD5(user + ':' + realm + ':' + oldPassword);
            var newHash = CryptoJS.MD5(user + ':' + realm + ':' + newPassword);
            var credentials = {
                email: user,
                oldHash: CryptoJS.enc.Hex.stringify(oldHash),
                newHash: CryptoJS.enc.Hex.stringify(newHash)
            };

            $http.post('../../private/setPassword', angular.toJson(credentials)).error(function () {
                $("#changePasswordServerErrorDialog").modal();
            }).success(function () {
                $("#changePasswordDialog").modal('hide');
                $("#changePasswordSuccessDialog").modal();
            });
        };

        $scope.logout = function () {
            $("#logoutDialog").modal({backdrop: 'static', keyboard: false});
            $http.get("../../private/logout");
        };

        $scope.showCreateUserModal = function () {
            $scope.newUser = {email: '', firstname: '', lastname: '', password1: '', password2: ''};
            $('#createUserDialog').modal('show');
        };

        $scope.createUser = function () {
            var error = undefined;
            var newUser = $scope.newUser;
            if (newUser.password1.trim().length === 0) {
                error = 'Please enter the password.'
            } else if (newUser.password2.trim().length === 0) {
                error = 'Please retype the password.'
            } else if (newUser.email.trim().length === 0) {
                error = 'Please enter an email address.'
            } else if (newUser.firstname.trim().length === 0) {
                error = 'Please enter a firstname.'
            } else if (newUser.lastname.trim().length === 0) {
                error = 'Please enter a lastname.'
            } else if (newUser.password1 != newUser.password2) {
                error = 'The passwords do not match.';
            }

            if (error) {
                BootstrapDialog.alert({title: 'Invalid input', message: error});
                return;
            }

            var toHash = CryptoJS.MD5(newUser.email + ':' + 'myrealm' + ':' + newUser.password1);
            var hash = CryptoJS.enc.Hex.stringify(toHash);
            var request = {email: newUser.email, firstname: newUser.firstname, lastname: newUser.lastname, hash: hash};
            $http.post('../../private/createUser', request).then(function (response) {
                if (!response.data.error) {
                    $('#createUserDialog').modal('hide');
                    BootstrapDialog.alert({
                        title: 'User Created',
                        message: 'The user "' + request.email + '" was created successfully.'
                    });
                } else {
                    BootstrapDialog.alert({
                        title: 'Error',
                        message: 'The user "' + request.email + '" could not be created due to the following reason: ' + response.data.error
                    });
                }
            });
        };
    }).config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'angular/navigation/navigation.htm',
            controller: 'NavigationCtrl'
        });
});