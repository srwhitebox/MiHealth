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
]).controller('resourceCtrl', function($scope, $http, $locale, $filter, $uibModal, $sce, ngTableParams, growlService) {
	
	$scope.init = function(category){
		$scope.category = category;
		$scope.isFilterVisible = true;
		$scope.getLocales();
		$scope.initCurLocale();
	}
	
	// Resource table
    $scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {code:'asc'}
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
        	var urlParams = $.param({
        		category : $scope.category,
                orderBy : angular.toJson(params.sorting()),
                filter : angular.toJson(params.filter()),
                count : params.count(),
                page : params.page()
            });
        	
        	$http.get('api/resources?'+urlParams).success(function(data) {
        		var resources = data.size > 0 ? data.data : [];
            	params.total(resources.length);
                $defer.resolve(resources.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });
    
    $scope.getFilter = function(localeTag){
    	var filter = {};
    	filter['properties[\'' + localeTag + '\']'] = 'text'; 
    	return filter;
    }
    
    $scope.getLocales = function(){
        $http.get('api/locales').success(function(data) {
    		$scope.locales = data.data;
    	}).error(function(error) {
    		$scope.locales = [];
      	});
    }

    $scope.initCurLocale = function(){
        $http.get('api/locale').success(function(data) {
        	$scope.curLocale = data.data;
    	}).error(function(error) {
    	    $scope.curLocale = {localeTag:"en-US", displayName:'English'};
      	});
    }
    
    // Locale tag which is listed and managed.
    $scope.setCurLocale = function(index){
    	$scope.curLocale = $scope.locales[index];
    	$scope.table.reload();
    }
    
    $scope.add = function(category){
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/addResource})]?category='+category,
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: {category:$scope.category, language:$scope.curLocale.localeTag}
            }
        });
    	
    	modalInstance.result.then(function (resource) {
    		$scope.saveResource(resource.category, resource.code, resource.language, resource.value, resource.comment);
   	    }, function () {
   	    });
    }
    
    $scope.save = function(resource){
    	this.saveResource(resource.category, resource.code, $scope.curLocale.localeTag, resource.properties[$scope.curLocale.localeTag], resource.comment);
    }
    
    $scope.saveResource = function(category, code, language, value, comment){
    	var data = $.param({
            language : language,
            value : value,
            comment : comment
        });
    
        var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        }
    	
    	$http.post("api/resource/add/"+category+"/"+code, data, config).success(function(data) {
    		growlService.growl('[(#{resource_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});    	
    }
    
    $scope.remove = function(resource){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{resource_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]",
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){
        	$http.post("api/resource/delete/"+resource.category+"/"+resource.code).success(function(data) {
                swal("[(#{deleted})]", "[(#{resource_removed})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }
    
})
