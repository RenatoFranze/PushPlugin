var exec = cordova.require('cordova/exec');

var Push = function(options){
	this._handlers = {
		'registration': [],
		'notification': [],
		'error': []
	};

	if(typeof options === 'undefined'){
		throw new Error('O argumento opção é necessário.');
	}

	this.options = options;

	var that = this;
	var success = function(result){
		if(result && typeof result.registrationId !== 'undefined'){
			that.emit('registration', result);
		}else if(result){
			that.emit('notification', result);
		}
	}

	var fail = function(msg){
		var e = (typeof msg === 'string') ? new Error(msg) : msg;
		that.emit('error', e);
	}

	setTimeout(function() {
		exec(success, fail, 'Push', 'init', [options]);
	}, 10);
};

Push.prototype.on = function(eventName, callback) {
	if (this._handlers.hasOwnProperty(eventName)) {
		this._handlers[eventName].push(callback);
	}
};

Push.prototype.off = function (eventName, handle) {
	if (this._handlers.hasOwnProperty(eventName)) {
		var handleIndex = this._handlers[eventName].indexOf(handle);
		if (handleIndex >= 0) {
			this._handlers[eventName].splice(handleIndex, 1);
		}
	}
};

Push.prototype.unregister = function(successCallback, errorCallback) {
    if (!errorCallback) { errorCallback = function() {}; }

    if (typeof errorCallback !== 'function')  {
        console.log('Push.unregister falhou: errorCallback não é uma função');
        return;
    }

    if (typeof successCallback !== 'function') {
        console.log('Push.unregister falhou: successCallback não é uma função');
        return;
    }

    var that = this;
    var cleanHandlersAndPassThrough = function() {
		that._handlers = {
			'registration': [],
			'notification': [],
			'error': []
		};
        successCallback();
    };

    exec(cleanHandlersAndPassThrough, errorCallback, 'Push', 'unregister', []);
};

Push.prototype.emit = function() {
	var args = Array.prototype.slice.call(arguments);
	var eventName = args.shift();

	if (!this._handlers.hasOwnProperty(eventName)) {
		return false;
	}

	for (var i = 0, length = this._handlers[eventName].length; i < length; i++) {
		this._handlers[eventName][i].apply(undefined,args);
	}

	return true;
};

module.exports = {
	init: function(options){
		return new Push(options);
	}
};