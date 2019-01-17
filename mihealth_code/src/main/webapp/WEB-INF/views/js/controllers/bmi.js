/**
 * Locale Controller
 */
angular.module('mihealth', [
	'angular.filter',
    'ngAnimate',
    'ngResource',
    'ui.router',
    'ui.bootstrap',
    'angular-loading-bar',
    'oc.lazyLoad',
    'nouislider',
    'ngTable'
]).controller('localeCtrl', function($scope, $http, $filter, $uibModal, $sce, ngTableParams, growlService) {
	$scope.init = function(){
		$scope.isFilterVisible = true;
		$scope.refresh();
	}
	
	$scope.refresh = function(){
    	$http.get('api/bmi/list').success(function(data) {
    		$scope.bmis = data.data;
		}).error(function(error) {
			$scope.bmis = [];
	  	});
	}
	
	$scope.add = function(){
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/addLocale})]',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: {enabled:true, order:0}
            }
        });
    	
    	modalInstance.result.then(function (resource) {
    		$scope.save(resource);
   	    }, function () {
   	    });
    }
    
	$scope.save = function(locale){
    	$http({
            url : "api/locale/save",
            method : "POST",
            data : locale,
            headers : {
                  "Content-Type" : "application/json; charset=utf-8",
                  "Accept" : "application/json"
            }
    	}).success(function(data) {
    		growlService.growl('[(#{locale_saved})]', 'success');
    		$scope.refresh();
    	}).error(function(data) {
    	});
    }

	$scope.remove = function(locale){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{locale_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]",   
            closeOnConfirm: false 
        }, function(){   
        	$http({
                url : "api/locale/delete",
                method : "POST",
                data : locale,
                headers : {
                      "Content-Type" : "application/json; charset=utf-8",
                      "Accept" : "application/json"
                }
        	}).success(function(data) {
                swal("[(#{deleted})]", "[(#{locale_deleted})]", "success"); 
                $scope.refresh();
        	}).error(function(data) {
        	});        	
        });
    }
})
