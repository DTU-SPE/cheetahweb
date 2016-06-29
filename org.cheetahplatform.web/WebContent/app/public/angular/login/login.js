var app = angular.module('cheetah.Login', ['ngRoute', 'dgAuth'])
    .controller('LoginCtrl', function ($scope, $http) {

        app.run(['dgAuthService', function (dgAuthService) {
            /**
             * It tries to sign in. If the service doesn't find
             * the credentials stored or the user is not signed in yet,
             * the service executes the required function.
             */
            dgAuthService.start();
        }]);


        $scope.login = function () {
            var success = function (data) {
                var token = data.userId;
                $http.defaults.headers.common['X-Access-Token'] = token || cheetah.userId;
                cheetah.userId = token;
            };

            var error = function () {
                // TODO: apply user notification here..
            };
            app.run(['dgAuthService', function (dgAuthService) {
                /**
                 * It tries to sign in. If the service doesn't find
                 * the credentials stored or the user is not signed in yet,
                 * the service executes the required function.
                 */
                dgAuthService.setCredentials($scope.credentials.username, $scope.credentials.password);
                dgAuthService.signin();
            }]);
        };
    }).config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'angular/login/login.htm',
                controller: 'LoginCtrl'
            });
    }).config(['dgAuthServiceProvider', function (dgAuthServiceProvider) {
        dgAuthServiceProvider.setConfig({
            login: {
                method: 'POST',
                url: '/../../public/login'
            },
            logout: {
                method: 'POST',
                url: '/signout'
            }
        });
    }]);


