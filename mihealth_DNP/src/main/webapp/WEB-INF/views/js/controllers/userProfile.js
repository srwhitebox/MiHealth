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
]).controller('userProfileCtrl', function($scope, $http, $filter, $uibModal, $sce, ngTableParams, Upload, growlService) {
    //Edit
    this.editInfo = 0;
    this.editContact = 0;

    $scope.init = function(){
		/*[+
			$scope.userUid = '[(${user.getUid()})]';
		+]*/ 
	    
	    loadUserInfo($scope.userUid);
	}
	
    var loadUserInfo = function(userUid){
	    if (!angular.isUndefined(userUid)){
		    $http.get('api/user/'+userUid).success(function(data) {
	    		$scope.user = data.data;
	    		$scope.profileUrl = getProfileUrl(); 
			}).error(function(error) {
		  	});
	    }
    }

    
    this.save = function(user) {            
		this.editInfo = 0;
		this.editContact = 0;

		var config = {
            headers : {
                'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
            }
	    }
	    	
    	$http.post("api/user/save", user, config).success(function(data) {
    		growlService.growl('[(#{user_saved})]', 'success');
		}).error(function(error) {
			
	  	});  
    }
    
    this.getRole = function(roles){
    	var role = "";
    	angular.forEach(roles, function(value, key) {
    		role += key.substring(5);
    	});
    	
    	return role;
    }

    $scope.idTypes=[
	        	    {idType:'LOGIN_ID', name:'Login ID'},
	        	    {idType:'EMAIL', name:'Email address'},
	        	    {idType:'NATIONAL_ID', name:'National ID'},
	        	    {idType:'NFC', name:'NFC'},
	        	    {idType:'LOGIN_ID', name:'Login ID'},
	        	    {idType:'MOBILE_PHONE', name:'Mobile phone'},
	        	    ];

    
 // Account Table
	$scope.table = new ngTableParams({
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {registeredAt:'desc'}
    }, {
        total: 0, // length of data
        getData: function($defer, params) {
        	var urlParams = $.param({
                orderBy : angular.toJson(params.sorting()),
                filter : angular.toJson(params.filter()),
                count : params.count(),
                page : params.page()
            });
        	
        	$http.get('api/accounts/'+$scope.user.uid).success(function(data) {
        		var records = data.data;
        		angular.forEach(records, function(account){
	    			account.activated = !angular.isUndefined(account.activatedAt);
	    		})
        		params.total(records.length);
                $defer.resolve(records.slice((params.page() - 1) * params.count(), params.page() * params.count()));
			}).error(function(error) {
		  	});
        }
    });
	
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
	
	$scope.upload = function(file) {
		if (file && !file.$error) {
			file.upload = Upload.upload({
				url : 'api/profile/upload',
				data : {file : file, userUid: $scope.userUid}
			});

			file.upload.then(
				function(response) {
					$scope.profileUrl = getProfileUrl();
					growlService.growl('[(#{profile_uploaded})]');
				},
				function(response) {
				}
			);

			file.upload.progress(function(evt) {
				file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
			});
		}
	}
	
	var getProfileUrl = function(){
		return "profile/"+$scope.userUid+"#"+ new Date().getTime();
	}	
})
