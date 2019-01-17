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
    'ngTable',
    'ngWig'
]).controller('boardContentCtrl', function($scope, $http, $locale, $filter, $uibModal, $sce, $q, ngTableParams, growlService) {
	$scope.init = function(){
		$scope.isFilterVisible = true;
		$scope.initCurLocale();
	}
	
	$scope.refresh = function(){
		
	}
	
	// Property table
    $scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {registeredAt : 'asc'}
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
        	var sorting = {};
        	angular.copy(params.sorting(), sorting);
        	if (!angular.isUndefined(sorting.boardName)){
        		var curLocaleTag = $scope.curLocale.localeTag;
        		sorting['boardProperties[\''+curLocaleTag+'\']'] = sorting.boardName;
        		delete sorting.boardName;
        	}
        	
        	var urlParams = $.param({
                orderBy : angular.toJson(sorting),
                filter : angular.toJson(params.filter()),
                count : params.count(),
                page : params.page()
            });
        	
        	$http.get('api/board/content/list?'+urlParams).success(function(data) {
        		var properties = data.size == 0 ? [] : data.data;
            	params.total(properties.length);
                $defer.resolve(properties.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });
    
    $scope.editContent = function(content){
    	
    	if (angular.isUndefined(content)){
    		content = {};
    	}
    	
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/editBoardContent})]',
            controller: 'modalResourceCtrl',
            size: 'lm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: {boards:$scope.boards, content:content}
            }
        });
    	
    	modalInstance.result.then(function (reference) {
    		$scope.saveContent(reference.content);
   	    }, function () {
   	    });
    }

    
    $scope.saveContent = function(content){
    	var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
        }
    	
    	$http.post("api/board/content/save", content, config).success(function(data) {
    		growlService.growl('[(#{content_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});    	
    }
    
    $scope.remove = function(table, resource){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{content_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]", 
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){
        	$http.post("api/board/content/delete/"+resource.contentUid).success(function(data) {
                swal("[(#{deleted})]", "[(#{content_removed})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }
    
    $scope.initCurLocale = function(){
        $http.get('api/locale').success(function(data) {
        	$scope.curLocale = data.data;
    		$scope.getBoards();
    	}).error(function(error) {
    	    $scope.curLocale = {localeTag:"en-US", displayName:'English'};
    		$scope.getBoards();
      	});
    }

    $scope.getBoards = function(){
		$scope.boards = {};
		$scope.boardNames = [{id:"", title:""}];
    	$http.get('api/board/list').success(function(data) {
    		var resources = data.data;
    		angular.forEach(resources, function(element) {
    			var name = element.properties[$scope.curLocale.localeTag];
    			if (angular.isUndefined(name))
    				name = '#'+element.code;
    			$scope.boards[element.uid] = name;
    			$scope.boardNames.push({id:element.uid, title : name});
    		});
		}).error(function(error) {
	  	});
    }
    
	this.toDate = function(date){
		return moment.utc(date).toDate();
	}
})
