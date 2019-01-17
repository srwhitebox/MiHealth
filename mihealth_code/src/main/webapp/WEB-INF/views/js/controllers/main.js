mihealthApp
	// =========================================================================
    // Base controller for common functions
    // =========================================================================
	.config(function(tmhDynamicLocaleProvider, IdleProvider, KeepaliveProvider) {
		tmhDynamicLocaleProvider.localeLocationPattern('lib/angular/i18n/angular-locale_{{locale}}.js');
		IdleProvider.idle(900);
	    IdleProvider.timeout(10);
	    KeepaliveProvider.interval(900);
	})
	.run(['Idle', function(Idle) {
		  Idle.watch();
	}])
    .controller('materialadminCtrl', function($scope, $http, $locale, $filter, $timeout, $state, $element, $location, $window, Idle, growlService, wsService, tmhDynamicLocale){
        $scope.init = function(){
        	$scope.chartBmiMale={
            		options:{
            			chart: {
                            type: 'pieChart',
                            height: 300,
                            x: function(d){return d.label;},
                            y: function(d){return d.percent;},
                            valueFormat: function(d){
                                return d3.format(',.2%')(d);
                            },
                            showLabels: true,
                            duration: 500,
                            labelThreshold: 0.01,
                            labelSunbeamLayout: false,
                            labelsOutside : true,
                            showLegend: false,
                            labelType : 'percent',
                            color : ['#7181BF', '#8AC85B', '#FED2A1', '#EF4C21', '#9E9E9E'],
                        },
                        title : { enable: true, text:'[(#{boy})]'},
            		},
            		data:[
        	        ]
            	}
            	
        	$scope.chartBmiFemale = angular.copy($scope.chartBmiMale);
        	$scope.chartBmiFemale.options.title.text='[(#{girl})]';
        	
        	$scope.curCampusUid = '[(${user.getCurCampus()})]';
        	$scope.visionGradeUnit = '[(${visionGradeUnit})]';

        	$scope.chartInternal ={
    			options : {
    				chart: {
    	                type: 'discreteBarChart',
    	                height: 450,
    	                margin : {
    	                    top: 20,
    	                    right: 20,
    	                    bottom: 50,
    	                    left: 55
    	                },
    	                x: function(d){return d.label;},
    	                y: function(d){return d.value;},
    	                valueFormat: function(d){
    	                    return d3.format(',d')(d);
    	                },
    	                showValues: true,
    	                noData: '[(#{no_chart_data})]',
    	                xAxis: {
    	                	rotateLabels: -20
    	                },
    	                yAxis: {
    	                    axisLabelDistance: -10,
    	                    tickFormat:function(d){
    	                    	return d3.format(',d')(d);
    	                    }
    	                },
    	                zoom : {enabled : true}
    	            },
    				title : { enable: true, text:'[(#{internal_disease})]'},
    			},
    			data:[
    		            {
    		            	values: [],
    		                key: "Cumulative Return",
    		            }
    		        ]
        	}

        	$scope.chartSurgery = angular.copy($scope.chartInternal);
        	$scope.chartSurgery.options.title.text='[(#{surgery_disease})]';
        	$scope.chartPlace = angular.copy($scope.chartInternal);
        	$scope.chartPlace.options.title.text='[(#{injured_place})]';
        	
        	$scope.diseaseItems = /*[+ [[${user.getSettingsAsInteger("diseaseStatistic.topItems")}]]; +]*/
        		

        	$scope.getLocales();
    	}
    	
        $scope.$on('IdleTimeout', function() {
        	location.href="[(@{/logout})]";
        	
        });

        $scope.setStatisticsItems = function(items){
        	$scope.diseaseItems = items;
        	$scope.reloadDiseaseStatiscs();
        	setUserSetting('diseaseStatistic.topItems', items);
        }
        
        $scope.setStatisticsFilter = function(period){
        	if (period == '')
        		period = 'month';
        	
        	if (!angular.isUndefined($scope.statistics) && !angular.isUndefined($scope.statistics.period) && period == $scope.statistics.period)
        		return;
        	
            $scope.statistics = {
            		period : period,
            		grade: '',
            		classId : '[(${user.hasRole("ROLE_TEACHER") ? user.getPropertyAsString("classId"): ""})]'
            };
            
        	switch(period){
        	case 'day':
        		$scope.statistics.fromDate = moment().startOf('day').toDate();
        		$scope.statistics.toDate = moment().endOf('day').toDate(); 
        		break;
        	case 'week':
        		$scope.statistics.fromDate = moment().startOf('week').toDate();
        		$scope.statistics.toDate = moment().endOf('week').toDate(); 
        		break;
        	case 'month':
        		$scope.statistics.fromDate = moment().startOf('month').toDate();
        		$scope.statistics.toDate = moment().endOf('month').toDate(); 
        		break;
        	case 'semester':
        		// if 1st : 3/1~8/31, 2nd : 9/1~2/28
        		setSemesterPeriod(moment());
        		break;
        	case 'year':
        		$scope.statistics.fromDate = moment().startOf('year').toDate();
        		$scope.statistics.toDate = moment().endOf('year').toDate(); 
        		break;
        	}
        	
        	$scope.reloadDiseaseStatiscs();
        	setUserSetting('diseaseStatistic.period', period);
        }
        
        $scope.moveTo = function(rate){
        
        	switch($scope.statistics.period){
        	case 'day':
        		$scope.statistics.fromDate = moment($scope.statistics.fromDate).add(rate, 'day').toDate();
        		$scope.statistics.toDate = moment($scope.statistics.toDate).add(rate, 'day').toDate();
        		break;
        	case 'week':
        		$scope.statistics.fromDate = moment($scope.statistics.fromDate).add(rate, 'week').toDate();
        		$scope.statistics.toDate = moment($scope.statistics.toDate).add(rate, 'week').toDate();
        		break;
        	case 'month':
        		$scope.statistics.fromDate = moment($scope.statistics.fromDate).add(rate, 'month').toDate();
        		$scope.statistics.toDate = moment($scope.statistics.fromDate).endOf('month').toDate();
        		break;
        	case 'semester':
        		// if 1st : 3/1~8/31, 2nd : 9/1~2/28
        		setSemesterPeriod(moment($scope.statistics.fromDate).add(rate==1 ? 6 : -6, 'month'));
        		break;
        	case 'year':
        		$scope.statistics.fromDate = moment($scope.statistics.fromDate).add(rate, 'year').toDate();
        		$scope.statistics.toDate = moment($scope.statistics.fromDate).endOf('year').toDate();
        		break;
        	}
        	
        	$scope.reloadDiseaseStatiscs();
        }
        
        setSemesterPeriod = function(fromDate){
    		// first
    		if (fromDate.month() >=2 && fromDate.month() < 8){
    			$scope.statistics.fromDate = moment([fromDate.year(), 2, 1]).toDate();
    			$scope.statistics.toDate = moment([fromDate.year(), 7, 31]).toDate();
    		}else{	// second
    			$scope.statistics.fromDate = moment([fromDate.year(), 8, 1]).toDate();
    			$scope.statistics.toDate = moment([fromDate.year()+1, 1, 1]).endOf('month').toDate();
    		}
        }

        $scope.getPeriodString = function(){
        	switch($scope.statistics.period){
        	case 'day':
        		return $scope.statistics.fromDate.toLocaleDateString($scope.curLocale);
        	default:
        		return $scope.statistics.fromDate.toLocaleDateString($scope.curLocale)
        			+ ' - ' + $scope.statistics.toDate.toLocaleDateString($scope.curLocale);
        	}
        }
        
        $scope.reloadDiseaseStatiscs = function(){
    		$scope.getDiseaseReport('internalDisease');
    		$scope.getDiseaseReport('surgeryDisease');
    		$scope.getDiseaseReport('injuredPlace');        	
        }
        
        // Detect Mobile Browser
        if( /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ) {
           angular.element('html').addClass('ismobile');
        }

        // By default Sidebars are hidden in boxed layout and in wide layout only the right sidebar is hidden.
        this.sidebarToggle = {
            left: false,
            right: false
        }
        
        this.menuTooltip = function(){
        	return this.sidebarToggle.left ? '[(#{hide_menu})]':'[(#{show_menu})]';
        }
        
        // By default template has a boxed layout
        this.layoutType = localStorage.getItem('ma-layout-status');
        
        // For Mainmenu Active Class
        this.$state = $state;    
        
        //Close sidebar on click
        this.sidebarStat = function(event) {
            if (!angular.element(event.target).parent().hasClass('active')) {
                this.sidebarToggle.left = false;
            }
        }
        
        this.containerStyle = function(){
        	var style = {}
        	if (this.sidebarToggle.left){
        		style['padding-left'] = '268px';
        	}
        	if (this.sidebarToggle.right){
        		style['padding-right'] = '260px';
        	}
        	return style;
        }
        
        //Listview Search (Check listview pages)
        this.listviewSearchStat = false;
        
        this.lvSearch = function() {
            this.listviewSearchStat = true; 
        }
        
        //Listview menu toggle in small screens
        this.lvMenuStat = false;
        
        //Skin Switch
        this.currentSkin = 'bluegray';

        this.skinList = [
            'lightblue',
            'bluegray',
            'cyan',
            'teal',
            'green',
            'orange',
            'blue',
            'purple'
        ]

        this.skinSwitch = function (color) {
            this.currentSkin = color;
        }
        
        this.confirmLogout = function(){
            swal({   
                title: "[(#{are_you_sure})]",   
                text: "[(#{logout_desc})]",   
                type: "warning",   
                showCancelButton: true,   
                confirmButtonColor: "#DD6B55",   
                confirmButtonText: "[(#{logout_now})]",
                cancelButtonText: "[(#{cancel})]",
                closeOnConfirm: false 
            }, function(){   
            	location.href="[(@{/logout})]";
            });
        }
        
        // Clear Local Storage
        this.clearLocalStorage = function() {
            
            //Get confirmation, if confirmed clear the localStorage
            swal({   
                title: "[(#{are_you_sure})]",   
                text: "[(#{storage_delete_desc})]",   
                type: "warning",   
                showCancelButton: true,   
                confirmButtonColor: "#F44336",   
                confirmButtonText: "[(#{delete_it})]",
                cancelButtonText: "[(#{cancel})]",
                closeOnConfirm: false 
            }, function(){
                localStorage.clear();
                swal("[(#{done})]", "[(#{storage_deleted})]", "success"); 
            });
            
        }
    
        $scope.setCurLocale = function(index){
        	$scope.curLocale = $scope.locales[index];
        	var params = $.param({
        		language : $scope.curLocale.localeTag
    		});
    		
    		var config = {
                headers : {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
                }
    	    }
    	    	
        	$http.post("api/locale/change", params, config).success(function(data) {
        		location.reload();
    		}).error(function(error) {
    			
    	  	}); 
        }
        
        setUserSetting = function(key, value){
        	var params = $.param({
        		key: key,
        		value: value
    		});
    		
    		var config = {
                headers : {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
                }
    	    }
    	    	
        	$http.post("api/user/settings", params, config).success(function(data) {
    		}).error(function(error) {
    			
    	  	}); 
        }
        
        $scope.initCurLocale = function(){
            $http.get('api/locale').success(function(data) {
            	$scope.curLocale = data.data;
            	tmhDynamicLocale.set($scope.curLocale.localeTag.toLowerCase());
        		$scope.setStatisticsFilter('[(${user.getSettingAsString("diseaseStatistic.period")})]');
        		$scope.getBmiReport();
        	}).error(function(error) {
        	    $scope.curLocale = {localeTag:"en-US", displayName:'English'};
        	    tmhDynamicLocale.set($scope.curLocale.localeTag.toLowerCase());
        		$scope.setStatisticsFilter('[(${user.getSettingAsString("diseaseStatistic.period")})]');
        		$scope.getBmiReport();
          	});
        }

        $scope.getLocales = function(){
            $http.get('api/locales').success(function(data) {
        		$scope.locales = data.data;
        		
        		$scope.initCurLocale();
        	}).error(function(error) {
        		$scope.locales = [];
          	});
        }
        
        $scope.searchUsers = function(nameFilter){
        	$scope.searchAdmins(nameFilter);
        	$scope.searchTeachers(nameFilter);
        	$scope.searchNurses(nameFilter);
        	$scope.searchStudents(nameFilter);
        }
        
        $scope.searchAdmins = function(filter){
        	if ($scope.curCampusUid.length == 0)
        		return;
    		$scope.searchedAdmins = [];
    		
        	var urlParams = $.param({
        		campusUid : $scope.curCampusUid ,
                filter : {name : filter},
                count : 10,
                page : 1
            });
        	
        	if (!angular.isUndefined(filter) && filter.length > 0){
	        	$http.get('/mihealth/api/admins?'+urlParams).success(function(data) {
	        		if (data.code < 400){
	        			angular.forEach(data.data, function(teacher){
	        				if (teacher.name.toLowerCase().indexOf(filter.toLowerCase())>=0)
	        					$scope.searchedAdmins.push(teacher);
	        			})
	        		}
				}).error(function(error) {
			  	});
        	}
    		
        }

        
        $scope.searchTeachers = function(filter){
        	if ($scope.curCampusUid.length == 0)
        		return;
    		$scope.searchedTeachers = [];
    		
        	var urlParams = $.param({
        		campusUid : $scope.curCampusUid ,
                filter : {name : filter},
                count : 10,
                page : 1
            });
        	
        	if (!angular.isUndefined(filter) && filter.length > 0){
	        	$http.get('/mihealth/api/teachers?'+urlParams).success(function(data) {
	        		if (data.code < 400){
	        			angular.forEach(data.data, function(teacher){
	        				if (teacher.name.toLowerCase().indexOf(filter.toLowerCase())>=0)
	        					$scope.searchedTeachers.push(teacher);
	        			})
	        		}
				}).error(function(error) {
			  	});
        	}
    		
        }

        $scope.searchNurses = function(filter){
        	if ($scope.curCampusUid.length == 0)
        		return;
    		$scope.searchedNurses = [];
    		
        	var urlParams = $.param({
        		campusUid : $scope.curCampusUid ,
                filter : {name : filter},
                count : 10,
                page : 1
            });
        	
        	if (!angular.isUndefined(filter) && filter.length > 0){
	        	$http.get('/mihealth/api/nurses?'+urlParams).success(function(data) {
	        		if (data.code < 400){
	        			angular.forEach(data.data, function(teacher){
	        				if (teacher.name.toLowerCase().indexOf(filter.toLowerCase())>=0)
	        					$scope.searchedNurses.push(teacher);
	        			})
	        		}
				}).error(function(error) {
			  	});
        	}
    		
        }

        
        $scope.searchStudents = function(filter){
        	if ($scope.curCampusUid.length == 0)
        		return;
    		$scope.searchedStudents = [];
        	if (!angular.isUndefined(filter) && filter.length > 0){
        		
	        	$http.get('api/[(${user.getCurCampus()})]/students?filter='+filter+'&count=10').success(function(data) {
	        		$scope.searchedStudents = data.data;
	        	}).error(function(error) {
	          	});
        	}
        }
        
    	$scope.openStudentDetails = function(student){
    		$state.go('student.profile.about', {uid:student.userUid}, {location:true, reload:true});
    	}
        
        $scope.getBmiReport = function(){
        	if ($scope.curCampusUid.length == 0)
        		return;
        	var config = getRequestConfig(null, null, $scope.statistics.grade, $scope.statistics.classId);
        	
        	var configMale = angular.copy(config);
        	configMale.params.gender='M'
            $http.get('api/[(${user.getCurCampus()})]/measurement/bmiReport', configMale).success(function(data) {
            	$scope.chartBmiMale.data = getBmiRows($scope.chartBmiMale, data.data);
        	}).error(function(error) {
          	});
        	
        	var configFemale = angular.copy(config);
        	configFemale.params.gender='F'
            $http.get('api/[(${user.getCurCampus()})]/measurement/bmiReport', configFemale).success(function(data) {
            	$scope.chartBmiFemale.data = getBmiRows($scope.chartBmiFemale, data.data)
        	}).error(function(error) {
          	});
        }
        
        var getBmiTotal = function(rows){
        	var sum = 0;
        	angular.forEach(rows, function(value, key){
        		sum += value;
        	});
        	return sum;
        }
        
        var getBmiRows = function(chart, bmiRows){
        	var total = chart.total = getBmiTotal(bmiRows);
        	return [
		 	    {label : '[(#{bmi_under})]', value : angular.isUndefined(bmiRows.under) ? 0 : bmiRows.under, percent: angular.isUndefined(bmiRows.under) ? 0 : bmiRows.under / total},
		 	    {label : '[(#{bmi_normal})]', value: angular.isUndefined(bmiRows.normal) ? 0 : bmiRows.normal, percent: angular.isUndefined(bmiRows.normal) ? 0 : bmiRows.normal / total},
		 	    {label : '[(#{bmi_over})]', value : angular.isUndefined(bmiRows.over) ? 0 : bmiRows.over, percent : angular.isUndefined(bmiRows.over) ? 0 : bmiRows.over / total},
		 	    {label : '[(#{bmi_obesity})]', value : angular.isUndefined(bmiRows.obesity) ? 0 : bmiRows.obesity, percent : angular.isUndefined(bmiRows.obesity) ? 0 : bmiRows.obesity / total},
		 	    {label : '[(#{bmi_unknown})]', value : angular.isUndefined(bmiRows.unknown) ? 0 : bmiRows.unknown, percent : angular.isUndefined(bmiRows.unknown) ? 0 : bmiRows.unknown / total},
 	        ];
        }
        
        $scope.getDiseaseReport = function(item){
        	if ($scope.curCampusUid.length == 0)
        		return;

        	var config = getRequestConfig($scope.statistics.fromDate, $scope.statistics.toDate, $scope.statistics.grade, $scope.statistics.classId);
        	
            $http.get('api/[(${user.getCurCampus()})]/care/report/'+item, config).success(function(data) {
            	switch(item){
            	case 'internalDisease':
            		$scope.chartInternal.data[0].values = getDiseaseData(data.data);
                	break;
            	case 'surgeryDisease':
            		$scope.chartSurgery.data[0].values = getDiseaseData(data.data);
                	break;
            	case 'injuredPlace':
            		$scope.chartPlace.data[0].values = getDiseaseData(data.data);
                	break;
            	}
        	}).error(function(error) {
        		$scope.getDiseaseReport(item);
          	});
        }
        
        var getDiseaseData = function(diseaseRows){
        	var rows = [];
        	var i=0;
        	angular.forEach(diseaseRows, function(value, diseaseCode) {
        		if ($scope.diseaseItems == -1 || i < $scope.diseaseItems){
	        		row = {label : value.resource[$scope.curLocale.localeTag], value : value.count};
	        		rows.push(row);
        		}
        		i++;
        	})
        	
        	return rows;
        }

        var getRequestConfig = function(fromDate, toDate, grade, classId){
        	var params = {};

        	if (!angular.isUndefined(fromDate)){
        		params.from = $filter('date')(fromDate, 'yyyy-MM-dd');
        	}
        	
        	if (!angular.isUndefined(toDate)){
        		params.to = $filter('date')(toDate, 'yyyy-MM-dd');
        	}

        	if (!angular.isUndefined(grade)){
        		params.grade = grade;
        	}
        	
        	if (!angular.isUndefined(classId)){
        		params.classId = classId;
        	}
        	
        	return {
            		params : params,
                    headers : {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8;'
                    }
            	}
        }
    })


    // =========================================================================
    // Header
    // =========================================================================
    .controller('headerCtrl', function($timeout, messageService){

        // Top Search
        this.openSearch = function(){
            angular.element('#header').addClass('search-toggled');
            angular.element('#top-search-wrap').find('input').focus();
        }

        this.closeSearch = function(){
            angular.element('#header').removeClass('search-toggled');
        }
        
        // Get messages and notification for header
        this.img = messageService.img;
        this.user = messageService.user;
        this.user = messageService.text;

        this.messageResult = messageService.getMessage(this.img, this.user, this.text);


        //Clear Notification
        this.clearNotification = function($event) {
            $event.preventDefault();
            
            var x = angular.element($event.target).closest('.listview');
            var y = x.find('.lv-item');
            var z = y.size();
            
            angular.element($event.target).parent().fadeOut();
            
            x.find('.list-group').prepend('<i class="grid-loading hide-it"></i>');
            x.find('.grid-loading').fadeIn(1500);
            var w = 0;
            
            y.each(function(){
                var z = $(this);
                $timeout(function(){
                    z.addClass('animated fadeOutRightBig').delay(1000).queue(function(){
                        z.remove();
                    });
                }, w+=150);
            })
            
            $timeout(function(){
                angular.element('#notifications').addClass('empty');
            }, (z*150)+200);
        }
        
        //Fullscreen View
        this.fullScreen = function() {
            //Launch
            function launchIntoFullscreen(element) {
                if(element.requestFullscreen) {
                    element.requestFullscreen();
                } else if(element.mozRequestFullScreen) {
                    element.mozRequestFullScreen();
                } else if(element.webkitRequestFullscreen) {
                    element.webkitRequestFullscreen();
                } else if(element.msRequestFullscreen) {
                    element.msRequestFullscreen();
                }
            }

            //Exit
            function exitFullscreen() {
                if(document.exitFullscreen) {
                    document.exitFullscreen();
                } else if(document.mozCancelFullScreen) {
                    document.mozCancelFullScreen();
                } else if(document.webkitExitFullscreen) {
                    document.webkitExitFullscreen();
                }
            }

            if (exitFullscreen()) {
                launchIntoFullscreen(document.documentElement);
            }
            else {
                launchIntoFullscreen(document.documentElement);
            }
        }
    })

