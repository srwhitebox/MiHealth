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
]).controller('reportsCtrl', function($scope, $http, $filter, $state, $uibModal, $sce, ngTableParams, Upload, growlService) {
	$scope.isFilterVisible = true;
	
	$scope.refresh = function(){
	}
	
	$scope.period = {showAllRecords : true};
	
    // Student Table
	$scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
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
        		params.total(records.length);
                $defer.resolve(records.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });
	
	$scope.openStudentDetails = function(student){
		$state.go('student.profile.about', {uid:student.userUid}, {location:true, reload:true});
	}

	$scope.downloadReport = function(student){
		return 'api/[(${user.getCurCampus()})]/student/report?userUid='+student.userUid;
	}

	$scope.downloadPersonalHelthReports = function(){
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
		
		window.location.assign('api/[(${user.getCurCampus()})]/student/reports?'+urlParams);
	}
	
	$scope.downloadReports = function(item){
		var option = {
	            orderBy : angular.toJson($scope.table.sorting()),
	            filter : function(){
	            	var filter = $scope.table.filter();
	            	var classId = '[(${user.hasRole("ROLE_TEACHER") ? user.getPropertyAsString("classId"): ""})]';
	            	if (classId.length > 0){
	            		filter['registerProperties.classId'] = classId;
	            	}
	            	return angular.toJson(filter);
	            }
	        };
		if (!$scope.period.showAllRecords){
			option.from = $scope.period.from; 
			option.to = $scope.period.to; 
		}
		
		var urlParams = $.param(option);
		
		window.location.assign('api/[(${user.getCurCampus()})]/'+ item +'?'+urlParams);
	}

	$scope.searchPeriod = function(){
		var period = angular.copy($scope.period);
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/searchPeriod})]',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: $scope.period
            }
        });
    	
    	modalInstance.result.then(function (period) {
    		$scope.period = period;
   	    }, function () {
   	    });
    }
	
	$scope.getPeriodText = function(){
		if ($scope.period.showAllRecords)
			return "";
		var from = $filter('date')($scope.period.from, 'mediumDate');
		var to = $filter('date')($scope.period.to, 'mediumDate');
		
		return "( " + from + " - " + to + " )";
	}
		
	this.grades = [{id:'4', title:'[(#{4})]'}, {id:'5', title:'[(#{5})]'}]; 
	this.genders = [{id:'MALE', title:'[(#{male})]'}, {id:'FEMALE', title:'[(#{female})]'}]; 
	
	this.toDate = function(date){
		return angular.isDefined(date) ? moment.utc(date).toDate() : null;
	}
})