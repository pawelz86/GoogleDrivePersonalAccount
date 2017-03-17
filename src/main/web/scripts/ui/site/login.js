'use strict';

angular.module('personalApp').config(function($stateProvider){
    $stateProvider
    .state("login", {
        parent: 'site',
        url: "/login",
        data: {
            isArchive: false
        },
        views: {
            'content@' : {
                templateUrl: 'scripts/ui/html/login.html',
                controller: 'LoginCtrl as logCtrl'
            }
        },
        resolve: {
        }
    });
});