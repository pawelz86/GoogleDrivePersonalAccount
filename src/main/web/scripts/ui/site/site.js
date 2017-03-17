'use strict';

angular.module('personalApp').config(function($stateProvider){
    $stateProvider
    .state("publicSite", {
        abstract: true
    });
    $stateProvider
    .state("site", {
        abstract: true,
        parent: 'publicSite',
        views: {
            'navbar@' : {
                templateUrl: 'scripts/ui/navBar/navBar.html',
                controller: 'NavBarCtrl',
                controllerAs: 'nav'
            }
        }
    });
});