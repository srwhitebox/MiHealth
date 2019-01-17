/**
 * Care data Controller
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
]).controller('careCtrl', function($scope, $http, $filter, $uibModal, $sce, ngTableParams, growlService, propertyService, localeService) {
	$scope.isFilterVisible = true;
	
	$scope.refresh = function(){
		$scope.table.reload();
	}
	
    // Disease Table
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
        	
        	$http.get('api/[(${user.getCurCampus()})]/care?'+urlParams).success(function(data) {
        		var records = data.data;
        		params.total(data.total);
        		$defer.resolve(records);
//        		params.total(records.length);
//                $defer.resolve(records.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });

	$scope.editDisease = function(disease){
		disease.diseaseName = $scope.getDiseaseName(disease.deptId, disease.properties.disease);
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/editDisease})]',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: {disease:disease}
            }
        });
    	
    	modalInstance.result.then(function (reference) {
    		$scope.saveDisease(reference.disease);
   	    }, function () {
   	    });
    }
    
	$scope.saveDisease = function(disease){
		
		disease.uid = disease.dataUid;
		
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    	
    	$http.post("api/care/save", disease, config).success(function(data) {
    		growlService.growl('[(#{disease_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});  
		
    }
	
	$scope.removeDisease = function(disease){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{disease_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]",
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){
        	disease.uid = disease.dataUid;
        	var config = {
                    headers : {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
                    }
        	    }
        	    	
        	$http.post("api/care/delete", disease, config).success(function(data) {
                swal("[(#{deleted})]", "[(#{disease_deleted})]", "success"); 
                $scope.table.reload();
    		}).error(function(error) {
    	  	});	
    	
        });
    }
	
	$scope.editTreatment = function(disease, treatment){
		if (angular.isUndefined(treatment)){
			treatment = {
				careUid : disease.dataUid,
			}
		}
		
		treatment.disease = disease;
		treatment.diseaseName = $scope.getDiseaseName(disease.deptId, disease.properties.disease);
    	
		var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/editTreatment})]',
            controller: 'modalResourceCtrl',
            size: 'lm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference : treatment
            }
        });
    	
    	modalInstance.result.then(function (treatment) {
    		$scope.saveTreatment(treatment);
   	    }, function () {
   	    });
    }

	$scope.saveTreatment = function(treatment){
		var tempTreatment = angular.copy(treatment);
		if (tempTreatment.disease)
			delete tempTreatment.disease;
		
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    	
    	$http.post("api/care/treatment/save", tempTreatment, config).success(function(data) {
    		growlService.growl('[(#{treatment_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});		
	} 
	
	$scope.removeTreatment = function(uid){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{treatment_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]", 
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){   
        	$http({
                url : "api/care/treatment/delete/"+uid,
                method : "POST"
        	}).success(function(data) {
                swal("[(#{deleted})]", "[(#{treatment_deleted})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }
	
	$scope.showDetails = function(disease){
		if (!disease.hasTreatment)
			return;
		disease.showDetails =!disease.showDetails;
		if (disease.showDetails){
			$http.get('api/care/treatments/'+disease.dataUid).success(function(data) {
	    		disease.treatments = data.data;
			}).error(function(error) {
		  	});
		}
	}
	
	$scope.downloadReports = function(){
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
            },
        });
		
		return 'api/[(${user.getCurCampus()})]/reports/care?'+urlParams;
	}

	
	var promise = localeService.promise;
	
	this.curLocale = localeService.curLocale;
	
	this.getDeptName = function(deptId){
		return propertyService.getName("medicalDept", deptId, localeService.curLocale.localeTag);
	}

	$scope.getDiseaseName = function(deptId, diseases){
		var diseaseToken = diseases.split(",");
		var diseaseLocalized = [];
		angular.forEach(diseaseToken, function(disease){
			diseaseLocalized.push(propertyService.getName(deptId.startsWith('internal') ? 'internalDisease' : 'surgeryDisease', disease.trim(), localeService.curLocale.localeTag));
		});
		return diseaseLocalized.join(', ');
	}

	$scope.getInjuredPlaceName = function(places){
		if (!places)
			return;
		var placeToken = places.split(",");
		var localized = [];
		angular.forEach(placeToken, function(place){
			localized.push(propertyService.getName('injuredPlace', place.trim(), localeService.curLocale.localeTag));
		});
		return localized.join(', ');
	}
	
	$scope.getInjuredPartName = function(parts){
		if (!parts)
			return;
		
		var partToken = parts.split(",");
		var localized = [];
		angular.forEach(partToken, function(part){
			localized.push(propertyService.getName('injuredPart', part.trim(), localeService.curLocale.localeTag));
		});
		return localized.join(', ');
	}
	
	$scope.getTreatmentName = function(deptId, treatments){
		var treatmentToken = treatments.split(",");
		var treatmentLocalized = [];
		angular.forEach(treatmentToken, function(treatment){
			treatmentLocalized.push(propertyService.getName(deptId.startsWith('internal') ? 'internalTreatment' : 'surgeryTreatment', treatment.trim(), localeService.curLocale.localeTag));
		});
		return treatmentLocalized.join(', ');
	}

	
	this.toDate = function(date){
		return moment.utc(date).toDate();
	}
	
	$scope.rowStyle = function(index, disease){
		var style = {'background' : index % 2 ? 'transparent' : '#f4f4f4'};
		if (!angular.isUndefined(disease)){
			if (disease.hasTreatment)
				style.cursor = 'pointer';
		}
		return style;
	}
})
