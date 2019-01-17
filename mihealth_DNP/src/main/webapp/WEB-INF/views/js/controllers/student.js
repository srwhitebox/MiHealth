/**
 * Student Controller
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
]).controller('studentCtrl', function($scope, $http, $filter, $state, $uibModal, $sce, ngTableParams, Upload, growlService) {
	$scope.isFilterVisible = true;

	var curCampusUid = '[(${user.getCurCampus()})]';
	
	$http.get('api/campus/findAll').success(function(data) {
		$scope.campuses = data.data;
		
		if (curCampusUid.length==0)
			$scope.curCampus = $scope.campuses[0];
		else{
			angular.forEach($scope.campuses, function(campus){
				if (campus.uid == curCampusUid)
					$scope.curCampus = campus;
			});
		}
		
		$scope.table.reload();
	}).error(function(error) {
  	});
	
	$scope.refresh = function(){
	}
	
    // Student Table
	$scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {registerRegisteredAt:'desc'}
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
        	var urlParams = $.param({
                orderBy : angular.toJson(params.sorting()),
                filter : function(){
                	var filter = params.filter();
                	var classId = '[(${user.hasRole("ROLE_TEACHER") ? user.getPropertyAsString("classId"): ""})]';
                	if (classId.length > 0){
                		filter['registerProperties.classId'] = classId;
                	}
                	return angular.toJson(filter);
                },
                count : params.count(),
                page : params.page()
            });
        	
        	$http.get('api/[(${user.getCurCampus()})]/student/list?'+urlParams).success(function(data) {
        		var records = data.data;
        		params.total(data.total);
        		$defer.resolve(records);
//                $defer.resolve(records.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });
	
	$scope.openStudentDetails = function(student){
		$state.go('student.profile.about', {uid:student.userUid}, {location:true, reload:true});
	}

	$scope.downloadReport = function(student){
		return 'api/campus/student/report?userUid='+student.userUid;
	}

	$scope.downloadReports = function(){
		var urlParams = $.param({
            orderBy : angular.toJson($scope.table.sorting()),
            filter : function(){
            	var filter = $scope.table.filter();
            	var classId = '[(${user.hasRole("ROLE_TEACHER") ? user.getPropertyAsString("classId"): ""})]';
            	if (classId.length > 0){
            		filter['registerProperties.classId'] = classId;
            	}
            	return angular.toJson(filter);
            },
        });
		
		return 'api/'+curCampusUid + '/student/reports?'+urlParams;
	}

	$scope.update = function(srcStudent){
		var student = angular.copy(srcStudent);
		
		if (angular.isDefined(student)){
			if (angular.isDefined(student.birthDate))
				student.birthDate = new Date(student.birthDate);
			else 
				student.birthDate = new Date();
		}else{
			student = {birthDate : new Date()};
		}
		
		student.campus = $scope.curCampus;
		if (angular.isDefined(student.userProperties) && student.userProperties.isGuest){
			student.userProperties.isGuest = JSON.parse(student.userProperties.isGuest);
		}else{
			if (angular.isUndefined(student.userProperties))
				student.userProperties = {};
			student.userProperties.isGuest = false;
		}
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/editStudent})]',
            controller: 'modalResourceCtrl',
            size: 'lm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: student
            }
        });
    	
    	modalInstance.result.then(function (resource) {
    		$scope.save(resource);
   	    }, function () {
   	    });
    }
	
	$scope.save = function(student){
		if (student.birthDate)
			student.birthDate = moment(student.birthDate).format("YYYY-MM-DD");
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    	
    	$http.post("api/[(${user.getCurCampus()})]/student/save", student, config).success(function(data) {
    		growlService.growl('[(#{student_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});  
		
    }

	$scope.upload = function(file) {
		if (file && !file.$error) {
			file.upload = Upload.upload({
				url : 'api/[(${user.getCurCampus()})]/student/upload',
				file : file
			});

			file.upload.then(
				function(response) {
					growlService.growl('[(#{file_uploaded})]');
					$scope.table.reload();
				},
				function(response) {
				}
			);

			file.upload.progress(function(evt) {
				file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
			});
		}
	}
	
	this.grades = [{id:'4', title:'[(#{4})]'}, {id:'5', title:'[(#{5})]'}]; 
	this.genders = [{id:'MALE', title:'[(#{male})]'}, {id:'FEMALE', title:'[(#{female})]'}]; 
	
	$scope.remove = function(student){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{student_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]",
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){   
    		var params = $.param({
    			userUid : student.userUid
    		});
    		
    		var config = {
                headers : {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
                }
    	    }
    	    	
        	$http.post("api/[(${user.getCurCampus()})]/student/delete", params, config).success(function(data) {
                swal("[(#{deleted})]", "[(#{student_deleted})]", "success"); 
                $scope.table.reload();
    		}).error(function(error) {
    			swal("[(#{failed})]", "[(#{student_not_deleted})]", "warning");
    	  	});  
        });
    }
	
	this.toDate = function(date){
		return angular.isDefined(date) ? moment.utc(date).toDate() : null;
	}
})