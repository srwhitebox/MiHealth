var app = angular.module('mainApp', ['ngMaterial', 'ngFileUpload']);
app.controller('mainCtrl', function($scope, $http, $timeout, $mdToast, Upload) {
	$scope.upload = function(files) {
		$scope.files = files;
		angular.forEach(files, function(file) {
			if (file && !file.$error) {
				file.upload = Upload.upload({
					url : /*[[@{/api/david/student/upload}]]*/ 'api/1234/student/upload',
					file : file
				});

				file.upload.then(
					function(response) {
						showToast(file.name + ' has been uploaded.', 3000);
						refresh();
					},
					function(response) {
					}
				);

				file.upload.progress(function(evt) {
					file.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
				});
			}
		});
	}

	function showToast(message, interval){
	    $mdToast.show(
	    	$mdToast.simple()
  	        	.content(message)
  	        	.hideDelay(interval)
  	    );
	}

});