var exec = require('cordova/exec');

backRun ={
	startServ : function( successCallback, errorCallback ){
		cordova.exec(successCallback, errorCallback, 'backgroundAppRun', 'startBackgroundService')
	},
	stopServ : function(decryptedFilePath, successCallback, errorCallback ){
		cordova.exec(decryptedFilePath, successCallback, errorCallback, 'backgroundAppRun', 'stopBackgroundService')
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
}
module.exports = backRun;

// exports.coolMethod = function (arg0, success, error) {
//     exec(success, error, 'backgroundAppRun', 'coolMethod', [arg0]);
// };
