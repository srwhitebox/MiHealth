mihealthApp
.factory('wsService', function($websocket) {
	var wsScheme = location.protocol == 'http:' ? 'ws' : 'wss';
	var ws = $websocket(wsScheme + '://' + location.host + '[(@{/query})]');
	ws.reconnectIfNotNormalClose = true;
	var messageReceivers = {};
	const FEEDBACK = {
			APP_LIST 		: 120,
			APP_INSTALLED 	: 121,
			APP_UNINSTALLED : 122,

			ONLINE_STATE_CHANGED : 211,
			
			FILE_LIST 		: 130,
		    FILE_STORAGE 	: 131,
		    FILE_COPY 		: 132,
		    FILE_RENAME 	: 133,
		    FILE_MOVE 		: 134,
		    FILE_REMOVE 	: 135,
		    FILE_MKDIR 		: 136,
		    FILE_PUSH 		: 137,
		    FILE_PULL 		: 138,
		    
			MEMORY_USAGE	: 190,
			BATTERY_INFO	: 191,
			DATA_USAGE		: 192,
			NETWORK_ACTIVE  : 193,
			
			LOCATION 		: 198,
			SCREEN_CAPTURED : 199,
		};
	
	ws.onMessage(function(event) {
		var message = JSON.parse(event.data);
		angular.forEach(messageReceivers, function(receiver, name){
			receiver(message);
		});
	});

	ws.onError(function(event) {
	});

	ws.onClose(function(event) {
	});

	ws.onOpen(function() {
	});

	return {
		FEEDBACK : FEEDBACK,

		status : function() {
			return ws.readyState;
		},
		
		registerReceiver : function(name, sockerReceiver){
			messageReceivers[name] = sockerReceiver;
		},
		
		unregisterReceiver : function(name){
			delete messageReceivers[name];
		},
		
		send : function(receiverUuid, code, request, params){
			var message = {
				query:{
					code: code,
					query:{
						request: request,
						params: params	
					},
					isInstant: true,
					registeredAt: new Date(),
				},
			}
			ws.send(JSON.stringify(message));
		},

		close : function(){
			ws.close(true);
		},
	};
})
