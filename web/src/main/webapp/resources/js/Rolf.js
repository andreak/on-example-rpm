var enableLogging = true;
function log(msg) {
	if(!enableLogging){
		return;
	}
	if(window.console){
		window.console.log(msg);
	} else if (window.opera && !window.console) {
		window.console = {};
		var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml",
					 "group", "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
		for (var i = 0; i < names.length; ++i) {
			window.console[names[i]] = function() {
				opera.postError(arguments);
			}
		}
	}
}

// IE doesn't have String.trim()
if (typeof String.prototype.trim !== 'function') {
	String.prototype.trim = function() {
		return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
	}
}

var Rolf = (function() {

	var baseUrl = "";
	var Rolf_resourceMap;
	var onLoadArray = $A();

	var showLoadingMessage = function() {
		var msg = $("ajaxLoadingMessage");
		if (!msg) {
			msg = document.createElement('div');
			msg.id = "ajaxLoadingMessage";
			msg = $(msg);
			msg.addClassName("ajaxLoadingMessage");
		} else {
			log("Reusing ajaxLoadingMessage object: "+msg.id);
		}
		msg.show();
		$("ajaxLoadingMessageContainer").appendChild(msg);
	};

	var hideLoadingMessage = function() {
		if($("ajaxLoadingMessage")){
			$("ajaxLoadingMessage").remove();
		}
	};

	return {

		setBaseUrl: function(url) {
			baseUrl = url;
		},

		getBaseUrl: function() {
			return baseUrl;
		},

		/**
		 * Returns a text from a resource bundle
		 *
		 * It is possible to pass an array of values which will replace the placeholders in the resouce bundle text.
		 *
		 * example:
		 * my.text.key=My name is {0} and I drive a {1}.
		 *
		 * Rolf.getText("my.text.key", ['John Doe', 'Ford']) => "My name is John Doe and I drive a Ford.
		 *
		 * @param textKey
		 * @param argumentArray
		 */
		getText : function(textKey, argumentArray) {
			if (Rolf_resourceMap && textKey) {
				var text;

				if (argumentArray && argumentArray.length > 0) {
					try {
						var template = new Template(Rolf_resourceMap.get(textKey), /(^|.|\r|\n)({(\w+)})/);
						text = template.evaluate($H(argumentArray).toObject());
					} catch(ignore){}
				} else {
					text = Rolf_resourceMap.get(textKey);
				}
				return text != undefined ? text : textKey;
			} else {
				return null;
			}
		},

		appendProperties : function(resourceMap) {
            if (!Object.isHash(resourceMap)) {
                return;
            }
            Rolf_resourceMap = Rolf_resourceMap ? Rolf_resourceMap.merge(resourceMap) : resourceMap;
        },

		addOnLoad: function(onLoadFunction) {
			onLoadArray.push(onLoadFunction);
		},

		executeOnLoad: function() {
			onLoadArray.each(function(value) {
				value();
			});
		},

		returnZindex : (function() {
			var zIndex = 1000;
			return function() {
				return ++zIndex;
			};
		})(),

		liftAjaxStart: function(){
			showLoadingMessage();
		},
		liftAjaxEnd: function(){
			hideLoadingMessage();
		},

		attachFieldError: function(containerId, inputId, errorsElement) {
			// Remove potentionally old messages
			if ($(containerId).down(".errorContainer")) {
				$(containerId).down(".errorContainer").remove();
			}
			$(containerId).addClassName("errorContainer");
			$(inputId).addClassName("value_error");
			jQuery("#" + containerId).append(errorsElement);
		},

		removeFieldError: function(containerId, inputId, errorsElement) {
			// Remove potentionally old messages
			if ($(containerId).down(".errorContainer")) {
				$(containerId).down(".errorContainer").remove();
			}
			$(containerId).removeClassName("errorContainer");
			$(inputId).removeClassName("value_error");
		}


	}

})();

Event.observe(document, "dom:loaded", function() {
	// Set focus on the first element on the page with class="firstFocus"
	$$(".firstFocus").invoke("focus");
	var ajaxLoadingMessageContainer = document.createElement('div');
	ajaxLoadingMessageContainer.id = "ajaxLoadingMessageContainer";
	document.body.appendChild(ajaxLoadingMessageContainer);
	ajaxLoadingMessageContainer = $(ajaxLoadingMessageContainer);
	$(document.body).observe("mousemove", function(evt) {

		if (!ajaxLoadingMessageContainer) {
			return;
		}
		try{ // This funny try-catch is to make html-unit happy when running web-tests
			if (!ajaxLoadingMessageContainer.visible()) {
				ajaxLoadingMessageContainer.show();
			}
		}catch (e){

		}

		var bo = Position.realOffset(document.body);
		ajaxLoadingMessageContainer.style.zIndex = Rolf.returnZindex();
		ajaxLoadingMessageContainer.style.left = (evt.pointerX() + 20 - bo.left) + "px";
		ajaxLoadingMessageContainer.style.top = (evt.pointerY() + 20 - bo.top) + "px";
	});

	$(document.body).observe("mouseleave", function(evt) {
		if (ajaxLoadingMessageContainer) {
			ajaxLoadingMessageContainer.hide();
		}
	});

	Rolf.executeOnLoad();

});
