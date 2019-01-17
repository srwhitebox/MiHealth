/**
 * Substitute Locale Controller
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
]).controller('altLocaleCtrl', function($scope, $http, $filter, $uibModal, $sce, ngTableParams, growlService) {
	$scope.isFilterVisible = true;
	
	$scope.refresh = function(){
    	$http.get('api/altLocales').success(function(data) {
    		$scope.locales = data.data;
    		$scope.table.reload();
		}).error(function(error) {
			$scope.locales = [];
			$scope.table.reload();
	  	});
	}

    // Locale Table
	$scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10           // count per page
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
        	if (angular.isDefined($scope.locales)){
	    		var locales = params.filter() ? $filter('filter')($scope.locales, params.filter()) : $scope.locales;
	        	params.total(locales.length);
	            $defer.resolve(locales.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        	}
        }
    });
    
	$scope.add = function(){
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/addAltLocale})]',
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
            url : "api/altLocale/save",
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
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){   
        	$http({
                url : "api/altLocale/delete",
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
