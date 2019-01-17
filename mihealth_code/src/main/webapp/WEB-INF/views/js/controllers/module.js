mihealthApp
.controller('modalResourceCtrl', function ($scope, $http, $uibModalInstance, reference) {

	$scope.reference = angular.copy(reference);
    
	$scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1,
            showWeeks: false
        };
    
	$scope.ok = function() {
		$uibModalInstance.close($scope.reference);
	};

	$scope.cancel = function() {
		$uibModalInstance.dismiss('cancel');
	};
      

  	$scope.checkAccount = function(id) {
		this.accountExists = false;
		$http.get("api/account/exists?id=" + id).then(function(response) {
			$scope.accountExists = response.data.data;
		});
	}

	$scope.isAvailableAccount = function(id) {
		if (angular.isUndefined(id)
				|| angular.isUndefined($scope.accountExists))
			return false;
		return !$scope.accountExists;
	}

	$scope.canSaveUser = function(user) {
		return angular.isDefined(user) && angular.isDefined(user.name)
				&& angular.isDefined(user.properties)
				&& angular.isDefined(user.properties.email);
	}

	$scope.canSaveAccount = function(account) {
		return angular.isDefined(account) && angular.isDefined(account.id)
				&& angular.isDefined(account.password)
				&& angular.isDefined(account.repassword)
				&& account.password == account.repassword;
	}

	$scope.canSaveStudent = function(student) {
		return angular.isDefined(student) && angular.isDefined(student.name)
				&& angular.isDefined(student.birthDate)
				&& angular.isDefined(student.registerProperties)
				&& angular.isDefined(student.registerProperties.classId)
				&& angular.isDefined(student.registerProperties.seat);
	}
})
