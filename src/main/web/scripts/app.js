/* global angular */

'use strict';

angular .module('personalApp', [
        'ngResource',
//        'angular-loading-bar',
        'ui.router',
//        'ngSanitize'
    ])

.config(function($stateProvider, $urlRouterProvider, $httpProvider){
    $urlRouterProvider.otherwise('login');
})

.run(function ($rootScope, $state) {
    });



