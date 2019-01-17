/**
 * Measurement data Controller
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
]).controller('measurementCtrl', function($scope, $http, $filter, $uibModal, $sce, ngTableParams, growlService, visionService) {
	$scope.isFilterVisible = true;

	$scope.init = function(){
		initBmi();
	}
	
	initBmi = function(){
    	$http.get('api/bmi/list').success(function(data) {
    		$scope.bmis = data.data;
		}).error(function(error) {
			$scope.bmis = [];
	  	});
	}

	$scope.refresh = function(){
		$scope.table.reload();
	}
	
	$scope.bmiDesc = ['[(#{bmi_under})]', '[(#{bmi_normal})]', '[(#{bmi_over})]', '[(#{bmi_obesity})]', '[(#{bmi_obese})]'];
	
	$scope.getBmiLevel = function(record){
		if (!record.properties.bmi)
			return;
		
		if (angular.isDefined(record.properties.bmiLevel))
			return record.properties.bmiLevel;
		
		var age = getAge(record.birthDate);
		var bmiStandards = $filter('filter')($scope.bmis, {age: age.toString()});
		var bmiStandard = bmiStandards.length > 0 ? bmiStandards[0] : $scope.bmis[$scope.bmis.length-1]
		bmiStandard = record.gender == 'MALE' ? bmiStandard.male : bmiStandard.female;
		var bmi = record.properties.bmi;
		
		if (bmi < bmiStandard.normal)
			record.properties.bmiLevel = 0;
		else if (bmi >= bmiStandard.normal && bmi < bmiStandard.over)
			record.properties.bmiLevel = 1;
		else if (bmi >= bmiStandard.over && bmi < bmiStandard.obesity)
			record.properties.bmiLevel = 2;
		else if (bmi >= bmiStandard.obesity)
			record.properties.bmiLevel = 3;
		else
			record.properties.bmiLevel = -1;
	}
	
	
	getAge = function(birthDate){
		var dob = new Date(birthDate);
	    var endDt = new Date();
	    var age = new Date(endDt.getTime() - dob.getTime());
	    return age.getFullYear() - 1970 + (age.getMonth() < 6 ? 0 : 0.5); 
	}
	
    // Measurement Table
	$scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {registeredAt:'desc'}
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
        	
        	$http.get('api/[(${user.getCurCampus()})]/measurement?'+urlParams).success(function(data) {
        		var records = data.data;

        		//        		var orderedData = params.filter() ? $filter('filter')(records, params.filter()) : data.data;
//            	orderedData = params.sorting() ? $filter('orderBy')(orderedData, params.orderBy()) : orderedData;

        		params.total(records.length);
                $defer.resolve(records.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });

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
    
	$scope.save = function(measuredData){
		var params = $.param({
			campusId : '[(${user.getCurCampus()})]',
			dataUid : measuredData.dataUid,
			userUid : measuredData.userUid,
			registeredAt : measuredData.registeredAt,
			properties : angular.toJson(measuredData.properties)
		});
		
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    	
    	$http.post("api/measurement/save", params, config).success(function(data) {
    		growlService.growl('[(#{measurement_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});  
		
    }
	
	this.grades = [{id:'4', title:'[(#{4})]'}, {id:'5', title:'[(#{5})]'}]; 
	this.genders = [{id:'MALE', title:'[(#{male})]'}, {id:'FEMALE', title:'[(#{female})]'}]; 
	
	this.calcBmi = function(measurementData){
		var height = measurementData.properties.height;
		var weight = measurementData.properties.weight;
		var value = weight == 0 || height == 0 ? 0 : weight / (height/100 * height/100);
		measurementData.properties.bmi = Math.round(value * 1e1) / 1e1;
	}

	$scope.remove = function(measuredData){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{measurement_delete_desc})]!",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]",   
            closeOnConfirm: false 
        }, function(){   
        	$http({
                url : "api/measurement/delete?uid="+measuredData.dataUid,
                method : "POST"
        	}).success(function(data) {
                swal("[(#{deleted})]", "[(#{measurement_deleted})]", "success"); 
                $scope.refresh();
        	}).error(function(data) {
        	});        	
        });
    }
	
	$scope.downloadReports = function(item){
		var urlParams = $.param({
            timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
			orderBy : angular.toJson($scope.table.sorting()),
            filter : function(){
            	var filter = $scope.table.filter();
            	var classId = '[(${user.hasRole("ROLE_TEACHER") ? user.getPropertyAsString("classId"): ""})]';
            	if (classId.length > 0){
            		filter['registerProperties.classId'] = classId;
            	}
            	return angular.toJson(filter);
            }
        });
		
		return 'api/[(${user.getCurCampus()})]/reports/measurement/'+item+'?'+urlParams;
	}

	$scope.visionGrade = function(grade){
		if (grade)
			return visionService.displayGrade($scope.visionGradeUnit, parseFloat(grade));
	}
	
	this.toDate = function(date){
		return moment.utc(date).toDate();
	}
})
