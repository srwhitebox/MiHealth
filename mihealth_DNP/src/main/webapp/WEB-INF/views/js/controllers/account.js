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
]).controller('accountCtrl', function($scope, $http, $filter, $uibModal, $sce, ngTableParams, growlService) {
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
	        	$http.get('[(${accountsUrl})]?'+urlParams).success(function(data) {
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

	$scope.editUser = function(user, role){
		if (user == null || angular.isUndefined(user)){
			user = {
				account: {idType:'LOGIN_ID'},
				roles:{},
				campuses:{},
				enabled:true
			};
			user.roles[role] = true;
		}
		
		user.campus = $scope.curCampus;
		
		var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(${template})]?',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: user
            }
        });
    	
    	modalInstance.result.then(function (user) {
    		$scope.save(user);
   	    }, function () {
   	    });
    }
    
	$scope.save = function(user){
		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    	
    	$http.post("api/user/save", user, config).success(function(data) {
    		growlService.growl('[(#{user_record_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	});  
		
    }
	
	$scope.remove = function(user){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{user_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]", 
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){   
        	$http({
                url : "api/user/delete/"+user.uid,
                method : "POST"
        	}).success(function(data) {
                swal("[(#{deleted})]", "[(#{user_deleted})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }
	
	$scope.showAccounts = function(user){
		if (!user.hasAccount)
			return;
		user.showAccounts =!user.showAccounts;
		if (user.showAccounts){
			$http.get('api/accounts/'+user.uid).success(function(data) {
	    		user.accounts= data.data;
	    		angular.forEach(user.accounts, function(account){
	    			account.activated = !angular.isUndefined(account.activatedAt);
	    		})
			}).error(function(error) {
		  	});
		}
	}
	
	$scope.saveAccount = function(account, mode){
		account.mode = mode;
		var config = {
	            headers : {
	                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
	            }
		    }
		    	
    	$http.post("api/account/update", account, config).success(function(data) {
    		growlService.growl('[(#{account_saved})]', 'success');
    		$scope.table.reload();
		}).error(function(error) {
			
	  	}); 
	}
	
	$scope.activate = function(account){
		if (account.activated){
			account.activatedAt = new Date();
		}else
			delete account.activatedAt;
		$scope.saveAccount(account, "activate");
	}
	
	$scope.isActivated = function(account){
		return !angular.isUndefined(account.activatedAt);
	}
	
	$scope.needPassword = function(account){
		return account.idType == 'LOGIN_ID' || account.idType == 'EMAIL'; 
	}
	
	$scope.idTypes=[
	    {idType:'LOGIN_ID', name:'Login ID'},
	    {idType:'EMAIL', name:'Email address'},
	    {idType:'NATIONAL_ID', name:'National ID'},
	    {idType:'NFC', name:'NFC'},
	    {idType:'LOGIN_ID', name:'Login ID'},
	    {idType:'MOBILE_PHONE', name:'Mobile phone'},
	    ];
	
	$scope.editAccount = function(user, oldAccount, mode){
		var account={};
		if (oldAccount === null || angular.isUndefined(oldAccount)){
			account = {userUid : user.uid, idType:'LOGIN_ID', name:user.name, mode:mode};
		} else {
			if (!angular.isUndefined(oldAccount.idType) && !$scope.needPassword(oldAccount)){
				return;
			}
			angular.copy(oldAccount, account);
			
			account.userUid = user.uid;
			account.name = user.name;
			account.mode = mode;
			
			if (mode == 'password' || mode == 'edit')
				account.password = "";
		}
		
		account.idTypes = $scope.idTypes;
		
    	var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: '[(@{/dialog/editAccount})]',
            controller: 'modalResourceCtrl',
            size: 'sm',
            backdrop: 'static',
            keyboard: true,
            resolve: {
            	reference: account,
            }
        });
	    	
    	modalInstance.result.then(function (reference) {
    		if (reference.password == reference.repassword){
    			$scope.saveAccount(reference, mode);
    		}else{
    			swal("[(#{password_error})]", "[(#{wrong_password})]", "warning"); 
    		}
   	    }, function () {
   	    });
	}

	$scope.removeAccount = function(id){
    	swal({   
            title: "[(#{are_you_sure})]",   
            text: "[(#{account_delete_desc})]",   
            type: "warning",   
            showCancelButton: true,   
            confirmButtonColor: "#DD6B55",   
            confirmButtonText: "[(#{delete_it})]", 
            cancelButtonText: "[(#{cancel})]",
            closeOnConfirm: false 
        }, function(){   
        	$http({
                url : "api/account/delete/"+id,
                method : "POST"
        	}).success(function(data) {
                swal("[(#{deleted})]", "[(#{account_deleted})]", "success"); 
                $scope.table.reload();
        	}).error(function(data) {
        	});        	
        });
    }

	this.toDate = function(date){
		return moment.utc(date).toDate();
	}
	
	this.extractRoles = function(roles){
		var rolesSet = [];
		angular.forEach(roles, function(value, key) {
			rolesSet.push(key.slice(5));
		});
		return rolesSet.join();
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
