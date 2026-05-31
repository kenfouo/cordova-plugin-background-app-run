var exec = require('cordova/exec');

var backRun = {
	startServ : function( successCallback, errorCallback ){
		cordova.exec(successCallback, errorCallback, 'backgroundAppRun', 'startBackgroundService')
	},
	stopServ : function( successCallback, errorCallback ){
		cordova.exec(successCallback, errorCallback, 'backgroundAppRun', 'stopBackgroundService')
	},
	startSoundServ : function( successCallback, errorCallback ){
		cordova.exec(successCallback, errorCallback, 'backgroundAppRun', 'startMakeSoundBackgroundService')
	},
	stopSoundServ : function( successCallback, errorCallback ){
		cordova.exec(successCallback, errorCallback, 'backgroundAppRun', 'stopMakeSoundBackgroundService')
	},
	coolMethod : function( successCallback, errorCallback ){
		cordova.exec(successCallback, errorCallback, 'backgroundAppRun', 'coolMethod')
	},
	fireEvent: function(eventData, successCallback, errorCallback) {
		cordova.exec(successCallback, errorCallback, 'backgroundAppRun', 'fireEvent', [eventData]);
	},
	backgroundDeleteFile: function(decryptedFilePath, successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'backgroundAppRun', 'backgroundDeleteService', [decryptedFilePath]);
	}
};

module.exports = backRun;