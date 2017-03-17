'use strict';

angular.module('personalApp').factory('login', function ($resource) {
    
        return {
                login: $resource('/api/google/authenticate', {}, {'query': {method: 'POST', isArray: true}})
                };
});