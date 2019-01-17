var mihealthApp = angular.module('mihealth', [
    'angular.filter',
    'ngAnimate',
    'ngResource',
    'ui.router',
    'ui.bootstrap'
])

mihealthApp
.controller('loginCtrl', function($scope, $http, growlService){
	$scope.user = {};

	this.init = function(){
		$scope.getLocales();
		$scope.initCurLocale();
		this.activateLogin();
	}
	
	this.doLogin = function(user){
		var params = $.param(user);
		
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    	
    	$http.post("api/user/login", params, config).success(function(data) {
			switch(data.code){
			case 403:
    			growlService.growl('[(#{user_not_found})]', 'danger');
    			user.id = "";
    			user.password = "";
				break;
			case 404:
    			growlService.growl('[(#{wrong_password})]', 'danger');
    			user.password = "";
				break;
			case 408:
    			growlService.growl('[(#{accound_not_enabled})]', 'danger');
    			user.password = "";
				break;
			default:
    			location.reload();
				break;
			}
		}).error(function(error) {
    		growlService.growl('[(#{host_not_found})]', 'danger');
	  	});
	}
	
	this.doRegist = function(user){
		user.account.idType='EMAIL';
		user.properties.email = user.account.id;
		user.roles = {};
		user.roles[user.role] = true;
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    
    	$http.post("api/user/register", user, config).success(function(data) {
    		growlService.growl('[(#{user_registered})]', 'success');
    		location.reload();
		}).error(function(error) {
			
	  	});  		
    }

	$scope.checkCampus = function (id){
		this.accountExists = false;
		$http.get("api/campus/find?id=" + id)
	    .then(function(response) {
	        if (response.data.code == 200){
	        	$scope.user.campus = response.data.data; 
	        }else{
	        	if ($scope.isAvailableCampus())
	        		delete $scope.user.campus;
	        }
	    });
	}
	
	$scope.isAvailableCampus = function(){
		return angular.isDefined($scope.user) && angular.isDefined($scope.user.campus);
	}

	$scope.checkAccount = function (id){
		this.accountExists = false;
		$http.get("api/account/exists?id=" + id)
	    .then(function(response) {
	        $scope.accountExists = response.data.data; 
	    });
	}
	
	$scope.isAvailableAccount = function(id){
		if (angular.isUndefined(id) || angular.isUndefined($scope.accountExists))
			return false;
		return !$scope.accountExists;
	}
	
    this.activateLogin = function(){
    	this.register = this.forgot = false;
    	this.login = true;
    }
    
    this.activateRegister = function(){
    	this.login = this.forgot = false;
    	this.register = true;
    }
    
    this.activateForgot = function(){
    	this.login = this.register = false;
    	this.forgot = true;
    }
    
    $scope.setCurLocale = function(index){
    	$scope.curLocale = $scope.locales[index];
    	var url = "[(@{/})]?language=" + $scope.curLocale.localeTag;
    	window.location.href= url;
    }
    
    $scope.initCurLocale = function(){
        $http.get('api/locale').success(function(data) {
        	$scope.curLocale = data.data;
    	}).error(function(error) {
    	    $scope.curLocale = {localeTag:"en-US", displayName:'English'};
      	});
    }

    $scope.getLocales = function(){
        $http.get('api/locales').success(function(data) {
    		$scope.locales = data.data;
    	}).error(function(error) {
    		$scope.locales = [];
      	});
    }

})
