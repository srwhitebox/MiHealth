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
    'ngTable'
]).controller('mikioCtrl', function($scope, $state, $http, $filter, $uibModal, $sce, ngTableParams, growlService) {
	$scope.isFilterVisible = true;

	var curCampusUid = '[(${user.getCurCampus()})]';

	$scope.init = function(){
		$scope.findMikio();
	}
	
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
	
	$scope.findMikio = function(){
		if (!$state.params.uid)
			return;
			
		$http.get('api/mikio/'+$state.params.uid).success(function(data) {
			if (data.code < 400){
				$scope.curMikio = data.data;
				if ($scope.curMikio.properties.grade){
					$scope.curMikio.properties.grade = parseInt($scope.curMikio.properties.grade);
					$scope.curMikio.properties.hasStudent = true;
				}
				$scope.loadGrades();
			}else{
				$scope.curMikio = undefined;
			}
		}).error(function(error) {
	  	});
	}
	
	$scope.loadGrades = function(){
		$http.get('api/'+curCampusUid+'/grades').success(function(data) {
			if (data.code < 400){
				$scope.grades = data.data;
				if ($scope.grades.length > 0){
					if (!$scope.curMikio.properties.grade)
						$scope.curMikio.properties.grade = $scope.grades[0];
					$scope.loadClassIds();
				}else{
					delete $scope.curMikio.properties.grade;
					delete $scope.curMikio.properties.classId;
				}
			}else{
				delete $scope.curMikio.properties.grade;
				$scope.grades = undefined;
				delete $scope.curMikio.properties.classId;
				$scope.classIds = undefined;
			}
		}).error(function(error) {
	  	});
	}
	
	$scope.loadClassIds = function(){
		if (!$scope.curMikio.properties.grade)
			return;
		
		var config = {
			params: {grade : $scope.curMikio.properties.grade},
		};
		$http.get('api/'+curCampusUid+'/classIds', config).success(function(data) {
			if (data.code < 400){
				$scope.classIds = data.data;
				if (!$scope.curMikio.properties.classId || $scope.classIds.indexOf($scope.curMikio.properties.classId) < 0)
					$scope.curMikio.properties.classId = $scope.classIds[0];
				
				if ($scope.curMikio.properties.hasStudent)
					$scope.loadBatchStudents();
				else
					$scope.loadStudents();
				
			}else{
				$scope.curMikio.properties.classId = undefined;
				$scope.classIds = undefined;		
			}
		}).error(function(error) {
	  	});
	}

	$scope.filteredStudents = function(){
		return $filter('filter')($scope.students, function(student){return student.isAbsent;});
	}
	
	$scope.loadBatchStudents = function(){
		var config = {};
		$http.get('api/mikio/students/' + $scope.curCampus.uid + '/' + $state.params.uid, config).success(function(data) {
			if (data.code < 400){
				delete $scope.curMikio.properties.hasStudent;
				$scope.students = data.data;
			}else{
				$scope.loadStudents();
			}
		});
	}
		
	$scope.loadStudents = function(){
		var config = {
			params: {grade: $scope.curMikio.properties.grade, classId: $scope.curMikio.properties.classId},
		};
		$http.get('api/'+curCampusUid+'/students', config).success(function(data) {
			if (data.code < 400){
				$scope.students = [];
				angular.forEach(data.data, function(student){
					$scope.students.push(
						{
							mikioUid: $state.params.uid,
							studentUid: student.userUid,
							name: student.name,
							nationalId: student.nationalId,
							birthDate: student.birthDate,
							schoolYear: student.schoolYear,
							studentNo:student.studentNo,
							grade: student.grade,
							classId: student.registerProperties.classId,
							seat: parseInt(student.registerProperties.seat),
							
						}
					);
				});
				$scope.students = $filter('orderBy')($scope.students, 'grade');
//				$scope.students = data.data;
//				sortStudents('seat', true);
			}else
				$scope.students = undefined;		
		}).error(function(error) {
	  	});
	}
	
	var sortStudents = function(column, asc){
//		$scope.students = $filter('orderBy')($scope.students, 'registerProperties.seat', asc, $scope.seatComparator);
//	}
		
		if (column.endsWith('grade')){
			$scope.students = $scope.students.sort(function(s1, s2){
				if (asc)
					return s1['registerProperties']['grade'] - s2['registerProperties']['grade'];
				else
					return s2['registerProperties']['grade'] - s1['registerProperties']['grade'];
			});
		}else if (column.endsWith('seat')){
			$scope.students = $scope.students.sort(function(s1, s2){
				if (asc)
					return s1['registerProperties']['seat'] - s2['registerProperties']['seat'];
				else
					return s2['registerProperties']['seat'] - s1['registerProperties']['seat'];
			});
		}

	}
	
	$scope.seatComparator = function (s1, s2){
		return s1-s2;
	}
	
	$scope.moveUp = function(index){
		if (index -1 < 0)
			return;
		var temp = $scope.students[index-1];
		$scope.students[index-1] = $scope.students[index];
		$scope.students[index] = temp;
	}
	
	$scope.moveDown = function(index){
		if (index+1 == $scope.students.length)
			return;
		var temp = $scope.students[index+1];
		$scope.students[index+1] = $scope.students[index];
		$scope.students[index] = temp;
	}
	
	$scope.resetStudents = function(){
		var config = {
	            headers : {
	                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
	            },
	            params: {mikioUid : $state.params.uid}
		    }
		var mikio = {};
		
		delete $scope.curMikio.properties.grade;
		delete $scope.curMikio.properties.classId;
		delete $scope.students;
		
		delete $scope.curMikio.properties.grade;
		delete $scope.curMikio.properties.classId;
		delete $scope.students;
		
		$http.post("api/mikio/resetStudents", mikio, config).success(function(data) {
    		growlService.growl('[(#{batch_student_reset})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});		
		
//		$scope.students = [];
//		
//		delete $scope.curMikio.properties.grade;
//		delete $scope.curMikio.properties.classId;
//		
//		$scope.save($scope.curMikio);
	}
	
	$http.get('api/nurses').success(function(data) {
		if (data.code < 400)
			$scope.nurses = data.data;
		else
			$scope.nurses = [];
		
	}).error(function(error) {
  	});
	
	$scope.nurseName = function(nurseUid){
		var nurseName = undefined;
		if (nurseUid){
			for(i=0; i<$scope.nurses.length; i++){
				if ($scope.nurses[i].uid == nurseUid)
					return $scope.nurses[i].name;
			}
		}			
	}
	
	$scope.refresh = function(){
		$scope.table.reload();
	}
	
    // User Table
	$scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {registeredAt:'desc'}
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
			var records=[];
        	if (angular.isUndefined($scope.curCampus)){
        		params.total(records.length);
    			$defer.resolve(records.slice((params.page() - 1) * params.count(), params.page() * params.count()));
        		return;
        	}
        	
        	var filter = params.filter();
        	filter.campusUid = $scope.curCampus.uid;
        	
        	var urlParams = $.param({
        		campusUid : $scope.curCampus.uid,
                orderBy : angular.toJson(params.sorting()),
                filter : filter,
                count : angular.toJson(params.count()),
                page : params.page()
            });
        	
        	if (angular.isDefined($scope.curCampus)){
	        	$http.get('api/mikio/list').success(function(data) {
	        		if (data.code < 400){
		        		records = data.data;
	        		}
	        		params.total(records.length);
	                $defer.resolve(records.slice((params.page() - 1) * params.count(), params.page() * params.count()));
				}).error(function(error) {
			  	});
        	}
        }
    });

	$scope.save = function(mikio){
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
		
    	$http.post("api/mikio/save", mikio, config).success(function(data) {
    		growlService.growl('[(#{mikio_record_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});  		
    }
	
	$scope.pushStudents = function(mikio){
		var mikioUid = $state.params.uid;
		var batchStudents = angular.copy($scope.students);
		for(i=0; i< batchStudents.length; i++){
			batchStudents[i].measurementOrder = i;
		}
		
		var config = {
	            headers : {
	                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
	            }
		    }
		
    	$http.post("api/mikio/pushStudents/"+mikioUid, batchStudents, config).success(function(data) {
    		growlService.growl('[(#{batch_students_saved})]', 'success');
    		
    		$scope.save($scope.curMikio);
		}).error(function(error) {
			
		});

	}
	
	$scope.editMikio = function(mikio){
		if (!mikio){
			mikio = {
				campusUid : curCampusUid
			};
		} else {
		}
		
		mikio.nurses = $scope.nurses;

		var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/editMikio})]',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: mikio,
            }
        });
	    	
    	modalInstance.result.then(function (reference) {
    		delete reference.nurses;
    		
			$scope.save(reference);
   	    }, function () {
   	    });
	}

	$scope.remove = function(mikio){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{mikio_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]", 
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){   
        	$http({
                url : "api/mikio/delete/"+mikio.uid,
                method : "POST"
        	}).success(function(data) {
                swal("[(#{deleted})]", "[(#{mikio_deleted})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }
	
	$scope.showMikio = function(mikio){
		$state.go('mikio.batch', {uid:mikio.uid}, {location:true, reload:true});
	}
	
	this.toDate = function(date){
		return moment.utc(date).toDate();
	}
	
	$scope.rowStyle = function(index, user){
		var style = {'background' : index % 2 ? 'transparent' : '#f4f4f4'};
		if (!angular.isUndefined(user)){
			if (user.hasAccount)
				style.cursor = 'pointer';
		}
		return style;
	}

})
