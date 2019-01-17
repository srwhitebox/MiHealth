/**
 * Property Controller
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
]).controller('boardCtrl', function($scope, $http, $locale, $filter, $uibModal, $sce, $q, ngTableParams, growlService) {
	$scope.init = function(){
		$scope.isFilterVisible = true;
		$scope.getLocales();
		$scope.initCurLocale();
	}
	
	$scope.refresh = function(){
		
	}
	
	// Property table
    $scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {boardId: 'asc'}
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
        	var urlParams = $.param({
                orderBy : angular.toJson(params.sorting()),
                filter : angular.toJson(params.filter()),
                count : params.count(),
                page : params.page()
            });

        	$http.get('api/board/list?'+urlParams).success(function(data) {
        		var properties = data.size == 0 ? [] : data.data;
            	params.total(properties.length);
                $defer.resolve(properties.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });
    
    $scope.addBoard = function(){
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/addBoard})]',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: {language:$scope.curLocale.localeTag}
            }
        });
    	
    	modalInstance.result.then(function (reference) {
    		var board = {
    			boardId : reference.boardId,
    			comment : reference.comment,
    			properties : {},
    			enabled : true
    		};
    		board.properties[$scope.curLocale.localeTag] = reference.name;
    		$scope.saveBoard(board);
   	    }, function () {
   	    });
    }

    
    $scope.saveBoard = function(board){
    	var params = $.param({
			content : angular.toJson(board)
			});

    	var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        }
    	
    	$http.post("api/board/save", board, config).success(function(data) {
    		growlService.growl('[(#{board_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});    	
    }
    
    $scope.remove = function(table, board){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{board_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]",
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){
        	$http.post("api/property/delete/"+board.category+"/"+board.code).success(function(data) {
                swal("[(#{deleted})]", "[(#{board_deleted})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }
    
    $scope.getCategoryName = function(code){
    	var found = $filter('filter')($scope.categories, {id: code}, true);
    	if (found.length){
    		return found[0].title;
    	}else{
    		return '#'+code;
    	}
    }
    
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
    		$scope.getCategories();
    	}).error(function(error) {
    	    $scope.curLocale = {localeTag:"en-US", displayName:'English'};
    		$scope.getCategories();
      	});
    }

    // Locale tag which is listed and managed.
    $scope.setCurLocale = function(index){
    	$scope.curLocale = $scope.locales[index];
    	$scope.table.reload();
    	$scope.getCategories();
    }
    
    $scope.getCategories = function(){
		$scope.categories = [{id:'', title:''}];
    	$http.get('api/resources?category=property').success(function(data) {
    		var resources = data.data;
    		angular.forEach(resources, function(element) {
    			var title = element.properties[$scope.curLocale.localeTag];
    			if (angular.isUndefined(title))
    				title = '#'+element.code;
    			$scope.categories.push({id:element.code, title:title});
    		});
		}).error(function(error) {
	  	});
    }
})
