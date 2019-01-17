mihealthApp
//-----------------------------------------------
// PIE AND DONUT
//-----------------------------------------------

.directive('pieDonut', function(){
    return {
        restrict: 'A',
        link: function(scope, element, attrs){
            var pieChart = {
            	series : {
                    pie: {
                        innerRadius: 0.5,
                        show: true,
                        stroke: { 
                            width: 2,
                        },
                    },
            	},
            	legend : {
                    container: '.flc-donut',
                    backgroundOpacity: 0.5,
                    noColumns: 0,
                    backgroundColor: "white",
                    lineWidth: 0
                },
                grid : {
            		hoverable: true,
                    clickable: true	
                },
                tooltip: true,
                tooltipOpts : {
                    content: "%s : %p.0%, ", // show percentages, rounding to 2 decimal places
                    shifts: {
                        x: 20,
                        y: 0
                    },
                    defaultTheme: false,
                    cssClass: 'flot-tooltip'
                }
            }
            
            var colors = {
            	unknown : '#ECECEC',
            	under : '#7181BF',
            	normal : '#8AC85B',
            	over : '#FED2A1',
            	obesity : '#EF4C21'
            }
            
            /* Donut Chart */
            
            if($('#donut-chart-bmi-boy')[0]){
                var pieData = [
                               {data: scope.bmi.boy.under, color: colors.under, label: '[(#{bmi_under})]'},
                               {data: scope.bmi.boy.normal, color: colors.normal, label: '[(#{bmi_normal})]'},
                               {data: scope.bmi.boy.over, color: colors.over, label: '[(#{bmi_over})]'},
                               {data: scope.bmi.boy.obesity, color: colors.obesity, label: '[(#{bmi_obesity})]'},
                               {data: scope.bmi.boy.unknown, color: colors.unknown, label: '[(#{bmi_unknown})]'},
                           ];
                           
                $.plot('#donut-chart-bmi-boy', pieData, pieChart);
            }
            
            if($('#donut-chart-bmi-girl')[0]){
                var pieData = [
                               {data: scope.bmi.girl.under, color: '#7181BF', label: '[(#{bmi_under})]'},
                               {data: scope.bmi.girl.normal, color: '#8AC85B', label: '[(#{bmi_normal})]'},
                               {data: scope.bmi.girl.over, color: '#FED2A1', label: '[(#{bmi_over})]'},
                               {data: scope.bmi.girl.obesity, color: '#EF4C21', label: '[(#{bmi_obesity})]'},
                               {data: scope.bmi.girl.unknown, color: '#ECECEC', label: '[(#{bmi_unknown})]'},
                           ];
                           
                $.plot('#donut-chart-bmi-girl', pieData, pieChart);
            }
        }
    }
})
