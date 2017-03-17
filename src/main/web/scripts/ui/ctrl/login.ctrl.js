
    'use strict';
    angular.module('personalApp').controller('LoginCtrl', function(login) {
        var ctrl = this;

        ctrl.googleLogin = function(){
            login.login.query(function(result){
                window.location.href = result[0];
                }, function(fail){
                    console.log(fail);
                });
        };
    });
    ;