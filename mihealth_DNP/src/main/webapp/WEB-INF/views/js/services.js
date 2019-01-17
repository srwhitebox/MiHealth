mihealthApp
	// =========================================================================
    // Header Messages and Notifications list Data
    // =========================================================================
    .service('propertyService', function($http, $filter, $q){
    	var propertyService = {};
    	
    	propertyService.fetch = function(){
    		if (angular.isUndefined(propertyService.properties)){
    			propertyService.refresh();
    		}
    	}
    	
    	propertyService.refresh = function(){
        	$http.get('api/properties').success(function(data) {
    			propertyService.properties = data.data;
    			$q.defer().resolve();
    		});
    	}
    	
    	propertyService.getName = function(category, code, locale){
    		var property = $filter('filter')(propertyService.properties, {code: code})[0];
    		var name = angular.isUndefined(property) ? '' : property.properties[locale]; 
    		return angular.isUndefined(name) ||  name.length == 0 ? '#'+code : name;
    	}

    	return propertyService;    	
    })

    	// =========================================================================
    // Header Messages and Notifications list Data
    // =========================================================================

    .service('localeService', function($http, $filter, $q){
    	var localeService = {refresh:refresh};
    	
    	localeService.fetch = function(){
    		if (angular.isUndefined(localeService.curLocale)){
    			refresh();
    		}
    	}
    	
    	var refresh = function(){
    		$http.get('api/locale').success(function(data){
        		localeService.curLocale = data.data;
        		$q.defer().resolve();
        	})
    	}
    	
    	return localeService;
    })

    // =========================================================================
    // Vision Grade 
    // =========================================================================
    .service('visionService', ['$resource', function($resource){
        this.displayGrade = function(notation, grade) {
        	if (!grade)
        		return;
        	var value = "";
        	switch(notation){
        	case "decimal":
        	case "d":
        	case "2":
        		value = grade.toFixed(3);
        		if (value.endsWith("00"))
        			value = value.substring(0, value.length -2);
        		else if (value.endsWith("0"))
        			value = value.substring(0, value.length -1);
        		break;
        	case "arc5min":
        	case "5min":
        	case "min5":
        	case "5":
            	value = this.toMin5("decimal", grade).toFixed(1);
            	break;
        	case "logmar":
        	case "l":
        		value = this.toLogMar("decimal", grade).toFixed(1);
        		break;
        	case "senellen6m":
        	case "6m":
        		value = this.toSnellen4m("decimal", grade);
        		break;
        	case "senellen4m":
        	case "4m":
        		value = this.toSnellen6m("decimal", grade);
        		break;
        	case "senellen20ft":
        	case "20ft":
        		value = this.toSnellen20ft("decimal", grade);
        		break;
        	}
        	
        	return value;
        }
        
        this.toLogMar = function(notation, grade){
        	var logMar = -1;
        	switch(notation){
        	case "decimal":
        	case "d":
        	case "2":
            	logMar = - Math.log10(grade);
            	break;
        	case "5min":
        	case "arc5min":
        	case "min5":
        	case "5":
            	logMar = 5 - grade;
            	break;
        	case "logmar":
        	case "l":
        		logMar = grade;
        		break;
        	case "senellen4m":
        	case "4m":
        	case "senellen6m":
        	case "6m":
        	case "senellen20ft":
        	case "20ft":
        		var tokens = grade.split("/");
        		if (tokens.length == 2){
        			logMar = parseFloat(tokens[0]) / parseFloat(tokens[1]);
        		}
        		break;
        	}
        	
        	return logMar;
        }
        
        this.toMin5 = function(notation, grade){
        	return 5 - this.toLogMar(notation, grade);
        }
        
        this.toDecimal = function(notation, grade){
        	return Math.pow(10, -(this.toLogMar(notation, grade)));
        }
        
        this.toSnellen4m = function(notation, grade){
        	return "4/" + this.denominator(4, notation, grade);
        }
        
        this.toSnellen6m = function(notation, grade){
        	return "6/" + this.denominator(6, notation, grade);
        }

        this.toSnellen20ft = function(notation, grade){
        	return "20/" + this.denominator(20, notation, grade); ;
        }

        this.denominator = function(nominator, notation, grade){        	
        	var decimalGrade = this.toDecimal(notation, grade);
	       	var denominator = nominator / decimalGrade;
	        var denominatorStr = denominator.toFixed(1);
	        
	        return denominatorStr.endsWith(".0") ? denominatorStr.substring(0, denominatorStr.length - 2) : denominatorStr;
        }
        
    }])
    
    // =========================================================================
    // Header Messages and Notifications list Data
    // =========================================================================

    .service('messageService', ['$resource', function($resource){
        this.getMessage = function(img, user, text) {
            var gmList = $resource("data/messages-notifications.json");
            
            return gmList.get({
                img: img,
                user: user,
                text: text
            });
        }
    }])
    
    // =========================================================================
    // Malihu Scroll - Custom Scroll bars
    // =========================================================================
    .service('scrollService', function() {
        var ss = {};
        ss.malihuScroll = function scrollBar(selector, theme, mousewheelaxis) {
            $(selector).mCustomScrollbar({
                theme: theme,
                scrollInertia: 100,
                axis:'yx',
                mouseWheel: {
                    enable: true,
                    axis: mousewheelaxis,
                    preventDefault: true
                }
            });
        }
        
        return ss;
    })


    //==============================================
    // BOOTSTRAP GROWL
    //==============================================

    .service('growlService', function(){
        var gs = {};
        gs.growl = function(message, type) {
            $.growl({
                message: message
            },{
                type: type,
                allow_dismiss: false,
                label: 'Cancel',
                className: 'btn-xs btn-inverse',
                placement: {
                    from: 'top',
                    align: 'right'
                },
                delay: 2500,
                animate: {
                        enter: 'animated bounceIn',
                        exit: 'animated bounceOut'
                },
                offset: {
                    x: 20,
                    y: 85
                }
            });
        }
        
        return gs;
    })
