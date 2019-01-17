/**
 * User Profile Controller
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
    'ngFileUpload'
]).controller('campusCtrl', function($scope, $http, $filter, $uibModal, $sce, ngTableParams, Upload, growlService) {
	$scope.config = {
		editMode : {basic:false, contact:false},
		notationMode : [
			{name : '[(#{decimal_notation})]', value: 'decimal'},
			{name : '[(#{arc_5_minutes})]', value: '5min'},
			{name : '[(#{snellen_4m})]', value: '4m'},
			{name : '[(#{snellen_6m})]', value: '6m'},
			{name : '[(#{snellen_20ft})]', value: '20ft'},
			{name : '[(#{logmar})]', value: 'logmar'},
		],
	}
    $scope.init = function(){
	    $scope.getCampusInfo();
	    //Edit
	    $scope.editInfo = false;
	    $scope.editContact = 0;
	}
    
	$scope.getNotaionname = function(notation){
    	switch(notation){
    	case "decimal":
    	case "d":
    	case "2":
    		return '[(#{decimal_notation})]';
    	case "arc5min":
    	case "5min":
    	case "min5":
    	case "5":
    		return '[(#{arc_5_minutes})]';
    	case "logmar":
    	case "l":
    		return '[(#{logmar})]';
    	case "senellen6m":
    	case "6m":
    	case "6":
    		return '[(#{snellen_6m})]';
    	case "senellen4m":
    	case "4m":
    	case "4":
    		return '[(#{snellen_4m})]';
    	case "senellen20ft":
    	case "20ft":
    	case "20":
    		return '[(#{snellen_20ft})]';
    	}
	}
	
    $scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10           // count per page
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
        	var urlParams = $.param({
                orderBy : angular.toJson(params.sorting()),
                filter : angular.toJson(params.filter()),
                count : params.count(),
                page : params.page()
            });

        	$http.get('api/campus/findAll').success(function(data) {
        		var campus = data.size == 0 ? [] : data.data;
            	params.total(campus.length);
                $defer.resolve(campus.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });
    
	
    $scope.getCampusInfo = function(){
    	$http.get('api/campus/find/?uid='+'[(${user.getCurCampus()})]').success(function(data) {
    		$scope.campus = data.data;
    	    $scope.campusProfile = $scope.getProfileUrl($scope.campus.uid, 'square');
		}).error(function(error) {
	  	});
    }

	$scope.edit = function(campus){
		var reference = angular.isUndefined(campus) ? {enabled:true} : angular.copy(campus)
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/editCampus})]',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: reference
            }
        });
    	
    	modalInstance.result.then(function (campus) {
    		$scope.save(campus);
   	    }, function () {
   	    });
    }
	
	$scope.save = function(campus){
		if (angular.isUndefined(campus.settings)){
			campus.settings = {visionGradeUnit : 'decimal'};
		}else if (angular.isUndefined(campus.settings.visionGradeUnit)){
			campus.settings.visionGrade = 'decimal';
		}

		var config = {
	            headers : {
	                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
	            }
		    }
		    	
    	$http.post("api/campus/save", campus, config).success(function(data) {
    		growlService.growl('[(#{campus_saved})]', 'success');
    		$scope.table.reload();
    		$scope.cancelEditMode();
    		window.location.reload();
		}).error(function(error) {
			
	  	});
		
		$scope.visionGradeUnit = campus.settings.visionGradeUnit;
	}
	
	$scope.cancelEditMode = function(){
		$scope.config.editMode.basic = false;
		$scope.config.editMode.contact = false;
		$scope.config.editMode.settings = false;
	}
	
	$scope.resetAuthKey = function(campus){
		var config = {
	            headers : {
	                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
	            }
		    }
		    	
    	$http.post("api/campus/resetAuthkey", campus, config).success(function(data) {
    		growlService.growl('[(#{campus_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});  
	}

	$scope.remove = function(campus){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{campus_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]",
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){
        	var config = {
                    headers : {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
                    }
        	    }
        	
        	$http.post("api/campus/delete", campus, config).success(function(data) {
                swal("[(#{deleted})]", "[(#{campus_deleted})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }

    
    
	$scope.upload = function(file, itemType) {
		if (file && !file.$error) {
			file.upload = Upload.upload({
				url : 'api/campus/profile/upload/' + $scope.campus.uid + '/' + itemType,
				data: {file : file, campusUid : '[(${user.getCurCampus()})]'}
			});

			file.upload.then(
				function(response) {
					$scope.campusProfile = $scope.getProfileUrl($scope.campus.uid, itemType);
					growlService.growl('[(#{logo_uploaded})]');
				},
				function(response) {
				}
			);

			file.upload.progress(function(evt) {
				file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
			});
		}
	}
	
	$scope.getProfileUrl = function(campusUid, itemType){
		return "profile/campus/"+campusUid+"/"+itemType+"#"+ new Date().getTime();
	}
})
