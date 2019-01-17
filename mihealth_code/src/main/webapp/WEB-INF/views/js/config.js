mihealthApp
    .config(function ($stateProvider, $urlRouterProvider){
        $urlRouterProvider.otherwise("/home");


        $stateProvider
            //------------------------------
            // HOME
            //------------------------------

            .state ('home', {
                url: '/home',
                templateUrl: 'views/home',
                resolve: {
                    loadPlugin: function($ocLazyLoad, localeService) {
                    	
                    	localeService.fetch();
                    	
                        return $ocLazyLoad.load ([
                            {
                                name: 'css',
                                insertBefore: '#app-level',
                                files: [
                                    '[(@{/lib/chosen/chosen.min.css})]',
                                ]
                            },
                            {
                                name: 'vendors',
                                insertBefore: '#app-level-js',
                                files: [
                                    '[(@{/lib/chosen/chosen.jquery.js})]',
                                    '[(@{/lib/angular-chosen-localytics/chosen.js})]'
                                ]
                            }
                        ])
                    }
                }
            })

            //------------------------------
            // User
            //------------------------------
        
            .state ('user', {
                url: '/user',
                templateUrl: 'views/common',
                resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/userProfile.js',
                                ]
                            }
                        ])
                    }
                }
                	
            })

            .state ('user.profile', {
                url: '/profile',
                templateUrl: 'views/user/profile'
            })
            
            .state ('user.profile.about', {
                url: '/about',
                templateUrl: 'views/user/about'
            })
        
            .state ('user.profile.accounts', {
                url: '/accounts',
                templateUrl: 'views/user/accounts'
            })

            .state ('user.profile.settings', {
                url: '/settings',
                templateUrl: 'views/user/settings'
            })

            //------------------------------
            // Student
            //------------------------------
            .state ('student', {
                url: '/student',
                templateUrl: 'views/common',
                resolve: {
                    loadPlugin: function($ocLazyLoad, localeService, propertyService) {
                    	localeService.fetch();
                    	propertyService.fetch();
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/studentProfile.js',
                                ]
                            }
                        ])
                    }
                }
                	
            })

            .state ('student.profile', {
                url: '/profile',
                templateUrl: 'views/student/profile'
            })
            
            .state ('student.profile.about', {
                url: '/about/{uid}',
                templateUrl: 'views/student/about'
            })
        
            .state ('student.profile.measurement', {
                url: '/measurement',
                templateUrl: 'views/student/measurement'
            })

            .state ('student.profile.disease', {
                url: '/disease',
                templateUrl: 'views/student/disease'
            })
            
            //------------------------------
            // Accounts
            //------------------------------
            .state ('reports', {
                url: '/reports',
                templateUrl: 'views/common',
                resolve: {
                    loadPlugin: function($ocLazyLoad, localeService, propertyService) {
                    	localeService.fetch();
                    	propertyService.fetch();
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/reports.js',
                                ]
                            }
                        ])
                    }
                }
                	
            })

            .state ('reports.view', {
                url: '/view',
                templateUrl: 'views/reports/view'
            })

            //------------------------------
            // Accounts
            //------------------------------
           .state ('account', {
                url: '/account',
                templateUrl: 'views/common',
                resolve: {
                    loadPlugin: function($ocLazyLoad, localeService) {
                    	
                    	localeService.fetch();
                    	
                        return $ocLazyLoad.load ([
                            {
                                name: 'css',
                                insertBefore: '#app-level',
                                files: [
                                    '[(@{/lib/chosen/chosen.min.css})]',
                                ]
                            },
                            {
                                name: 'vendors',
                                insertBefore: '#app-level-js',
                                files: [
                                    '[(@{/lib/chosen/chosen.jquery.js})]',
                                    '[(@{/lib/angular-chosen-localytics/chosen.js})]'
                                ]
                            }
                        ])
                    }
                }                
            })
            
            .state ('account.administrator', {
                url: '/administrator',
                templateUrl: 'views/account/administrator',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/account.js?url=/api/admins&template=/dialog/editAdmin',
                                ]
                            }
                        ])
                    }
                }
            })
            
            .state ('account.teacher', {
                url: '/teacher',
                templateUrl: 'views/account/teacher',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/account.js?url=/api/teachers&template=/dialog/editTeacher',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('account.nurse', {
                url: '/nurse',
                templateUrl: 'views/account/nurse',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/account.js?url=/api/nurses&template=/dialog/editNurse',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('account.student', {
                url: '/student',
                templateUrl: 'views/account/student',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/student.js',
                                ]
                            }
                        ])
                    }
                }
            })
            
            //------------------------------
            // Measurement
            //------------------------------
        
            .state ('mikio', {
                url: '/mikio',
                templateUrl: 'views/common'
            })
            .state ('mikio.list', {
                url: '/list',
                templateUrl: 'views/mikio/list',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/mikio.js',
                                ]
                            }
                        ])
                    }
                }
            })
            .state ('mikio.batch', {
                url: '/batch/{uid}',
                templateUrl: 'views/mikio/batch',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/mikio.js',
                                ]
                            }
                        ])
                    }
                }
            })
            
            
            //------------------------------
            // Measurement
            //------------------------------
        
            .state ('measurement', {
                url: '/measurement',
                templateUrl: 'views/common'
            })
            
            .state ('measurement.list', {
                url: '/list',
                templateUrl: 'views/measurement/measurement',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/measurement.js',
                                ]
                            }
                        ])
                    }
                }
            })
            
            .state ('measurement.fat', {
                url: '/weightHeight',
                templateUrl: 'views/measurement/fat'
            })

            .state ('measurement.vision', {
                url: '/vision',
                templateUrl: 'views/measurement/vision'
            })


            //------------------------------
            // Disease records
            //------------------------------
        
            .state ('disease', {
                url: '/disease',
                templateUrl: 'views/common',
                resolve: {
                	load:function(localeService, propertyService){
                		localeService.fetch();
                    	propertyService.fetch();
                	}
                }
            })
            
            .state ('disease.list', {
                url: '/list',
                templateUrl: 'views/disease/disease',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/care.js',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('disease.internal', {
                url: '/internal',
                templateUrl: 'views/disease/internal'
            })
            
            .state ('disease.surgery', {
                url: '/surgery',
                templateUrl: 'views/disease/surgery'
            })

            //------------------------------
            // Board
            //------------------------------
        
            .state ('board', {
                url: '/board',
                templateUrl: 'views/common'
            })
            
            .state ('board.list', {
                url: '/list',
                templateUrl: 'views/board/boards',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/board.js',
                                ]
                            }
                        ])
                    }
                }
            })
            
            .state ('board.contents', {
                url: '/contents',
                templateUrl: 'views/board/contents',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                name: 'css',
                                insertBefore: '#app-level',
                                files: [
                                    'lib/ngWig/css/ng-wig.css'
                                ]
                            },
                            {
                                files: [
                                    'js/controllers/boardContent.js',
                                    'lib/ngWig/ng-wig.min.js',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('board.manage', {
                url: '/board_manage',
                templateUrl: 'views/board/manage'
            })

            //------------------------------
            // System
            //------------------------------
        
            .state ('system', {
                url: '/system',
                templateUrl: 'views/common'
            })
            
            .state ('system.locale', {
                url: '/locale',
                templateUrl: 'views/system/locale',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/locale.js',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('system.altLocale', {
                url: '/altLocale',
                templateUrl: 'views/system/altLocale',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/alt-locale.js',
                                ]
                            }
                        ])
                    }
                }
                
            })

            .state ('system.property', {
                url: '/property',
                templateUrl: 'views/system/property',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/property.js',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('system.propertyCategory', {
                url: '/propertyCategory',
                templateUrl: 'views/system/resource?category=property',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/resource.js',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('system.resource', {
                url: '/resource',
                templateUrl: 'views/system/resource?category=app',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/resource.js',
                                ]
                            }
                        ])
                    }
                }
            })
            .state ('system.bmi', {
                url: '/bmi',
                templateUrl: 'views/system/bmi',
            	resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/bmi.js',
                                ]
                            }
                        ])
                    }
                }
            })

            //------------------------------
            // Campus
            //------------------------------
        
            .state ('campus', {
                url: '/campus',
                templateUrl: 'views/common',
                resolve: {
                    loadPlugin: function($ocLazyLoad) {
                        return $ocLazyLoad.load ([
                            {
                                files: [
                                    'js/controllers/campus.js',
                                ]
                            }
                        ])
                    }
                }
            })

            .state ('campus.list', {
                url: '/list',
                templateUrl: 'views/campus/campuses'
            })

            .state ('campus.profile', {
                url: '/profile',
                templateUrl: 'views/campus/profile'
            })
            
            .state ('campus.profile.about', {
                url: '/about',
                templateUrl: 'views/campus/about'
            })
        
            .state ('campus.profile.settings', {
                url: '/settings',
                templateUrl: 'views/campus/settings'
            })

            //------------------------------
            // HEADERS
            //------------------------------
            .state ('headers', {
                url: '/headers',
                templateUrl: 'views/common-2'
            })

            .state('headers.textual-menu', {
                url: '/textual-menu',
                templateUrl: 'views/textual-menu'
            })

            .state('headers.image-logo', {
                url: '/image-logo',
                templateUrl: 'views/image-logo'
            })

            .state('headers.mainmenu-on-top', {
                url: '/mainmenu-on-top',
                templateUrl: 'views/mainmenu-on-top'
            })

    });
