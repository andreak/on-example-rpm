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


/**
 * Date extensions
 */

Date.$VERSION = 1.02;

// Utility function to append a 0 to single-digit numbers
Date.LZ = function(x) {return(x<0||x>9?"":"0")+x};
// Full month names. Change this for local month names
Date.monthNames = new Array('January','February','March','April','May','June','July','August','September','October','November','December');
// Month abbreviations. Change this for local month names
Date.monthAbbreviations = new Array('Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec');
// Full day names. Change this for local month names
Date.dayNames = new Array('Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday');
// Day abbreviations. Change this for local month names
Date.dayAbbreviations = new Array('Sun','Mon','Tue','Wed','Thu','Fri','Sat');
// Used for parsing ambiguous dates like 1/2/2000 - default to preferring 'American' format meaning Jan 2.
// Set to false to prefer 'European' format meaning Feb 1
Date.preferAmericanFormat = true;

// If the getFullYear() method is not defined, create it
if (!Date.prototype.getFullYear) {
	Date.prototype.getFullYear = function() { var yy=this.getYear(); return (yy<1900?yy+1900:yy); } ;
}

// Parse a string and convert it to a Date object.
// If no format is passed, try a list of common formats.
// If string cannot be parsed, return null.
// Avoids regular expressions to be more portable.
Date.parseString = function(val, format) {
	// If no format is specified, try a few common formats
	if (typeof(format)=="undefined" || format==null || format=="") {
		var generalFormats=new Array('y-M-d','MMM d, y','MMM d,y','y-MMM-d','d-MMM-y','MMM d','MMM-d','d-MMM');
		var monthFirst=new Array('M/d/y','M-d-y','M.d.y','M/d','M-d');
		var dateFirst =new Array('d/M/y','d-M-y','d.M.y','d/M','d-M');
		var checkList=new Array(generalFormats,Date.preferAmericanFormat?monthFirst:dateFirst,Date.preferAmericanFormat?dateFirst:monthFirst);
		for (var i=0; i<checkList.length; i++) {
			var l=checkList[i];
			for (var j=0; j<l.length; j++) {
				var d=Date.parseString(val,l[j]);
				if (d!=null) {
					return d;
				}
			}
		}
		return null;
	};

	this.isInteger = function(val) {
		for (var i=0; i < val.length; i++) {
			if ("1234567890".indexOf(val.charAt(i))==-1) {
				return false;
			}
		}
		return true;
	};
	this.getInt = function(str,i,minlength,maxlength) {
		for (var x=maxlength; x>=minlength; x--) {
			var token=str.substring(i,i+x);
			if (token.length < minlength) {
				return null;
			}
			if (this.isInteger(token)) {
				return token;
			}
		}
	return null;
	};
	val=val+"";
	format=format+"";
	var i_val=0;
	var i_format=0;
	var c="";
	var token="";
	var token2="";
	var x,y;
	var year=new Date().getFullYear();
	var month=1;
	var date=1;
	var hh=0;
	var mm=0;
	var ss=0;
	var ampm="";
	while (i_format < format.length) {
		// Get next token from format string
		c=format.charAt(i_format);
		token="";
		while ((format.charAt(i_format)==c) && (i_format < format.length)) {
			token += format.charAt(i_format++);
		}
		// Extract contents of value based on format token
		if (token=="yyyy" || token=="yy" || token=="y") {
			if (token=="yyyy") {
				x=4;y=4;
			}
			if (token=="yy") {
				x=2;y=2;
			}
			if (token=="y") {
				x=2;y=4;
			}
			year=this.getInt(val,i_val,x,y);
			if (year==null) {
				return null;
			}
			i_val += year.length;
			if (year.length==2) {
				if (year > 70) {
					year=1900+(year-0);
				}
				else {
					year=2000+(year-0);
				}
			}
		}
		else if (token=="MMM" || token=="NNN"){
			month=0;
			var names = (token=="MMM"?(Date.monthNames.concat(Date.monthAbbreviations)):Date.monthAbbreviations);
			for (var i=0; i<names.length; i++) {
				var month_name=names[i];
				if (val.substring(i_val,i_val+month_name.length).toLowerCase()==month_name.toLowerCase()) {
					month=(i%12)+1;
					i_val += month_name.length;
					break;
				}
			}
			if ((month < 1)||(month>12)){
				return null;
			}
		}
		else if (token=="EE"||token=="E"){
			var names = (token=="EE"?Date.dayNames:Date.dayAbbreviations);
			for (var i=0; i<names.length; i++) {
				var day_name=names[i];
				if (val.substring(i_val,i_val+day_name.length).toLowerCase()==day_name.toLowerCase()) {
					i_val += day_name.length;
					break;
				}
			}
		}
		else if (token=="MM"||token=="M") {
			month=this.getInt(val,i_val,token.length,2);
			if(month==null||(month<1)||(month>12)){
				return null;
			}
			i_val+=month.length;
		}
		else if (token=="dd"||token=="d") {
			date=this.getInt(val,i_val,token.length,2);
			if(date==null||(date<1)||(date>31)){
				return null;
			}
			i_val+=date.length;
		}
		else if (token=="hh"||token=="h") {
			hh=this.getInt(val,i_val,token.length,2);
			if(hh==null||(hh<1)||(hh>12)){
				return null;
			}
			i_val+=hh.length;
		}
		else if (token=="HH"||token=="H") {
			hh=this.getInt(val,i_val,token.length,2);
			if(hh==null||(hh<0)||(hh>23)){
				return null;
			}
			i_val+=hh.length;
		}
		else if (token=="KK"||token=="K") {
			hh=this.getInt(val,i_val,token.length,2);
			if(hh==null||(hh<0)||(hh>11)){
				return null;
			}
			i_val+=hh.length;
			hh++;
		}
		else if (token=="kk"||token=="k") {
			hh=this.getInt(val,i_val,token.length,2);
			if(hh==null||(hh<1)||(hh>24)){
				return null;
			}
			i_val+=hh.length;
			hh--;
		}
		else if (token=="mm"||token=="m") {
			mm=this.getInt(val,i_val,token.length,2);
			if(mm==null||(mm<0)||(mm>59)){
				return null;
			}
			i_val+=mm.length;
		}
		else if (token=="ss"||token=="s") {
			ss=this.getInt(val,i_val,token.length,2);
			if(ss==null||(ss<0)||(ss>59)){
				return null;
			}
			i_val+=ss.length;
		}
		else if (token=="a") {
			if (val.substring(i_val,i_val+2).toLowerCase()=="am") {
				ampm="AM";
			}
			else if (val.substring(i_val,i_val+2).toLowerCase()=="pm") {
				ampm="PM";
			}
			else {
				return null;
			}
			i_val+=2;
		}
		else {
			if (val.substring(i_val,i_val+token.length)!=token) {
				return null;
			}
			else {
				i_val+=token.length;
			}
		}
	}
	// If there are any trailing characters left in the value, it doesn't match
	if (i_val != val.length) {
		return null;
	}
	// Is date valid for month?
	if (month==2) {
		// Check for leap year
		if ( ( (year%4==0)&&(year%100 != 0) ) || (year%400==0) ) { // leap year
			if (date > 29){
				return null;
			}
		}
		else {
			if (date > 28) {
				return null;
			}
		}
	}
	if ((month==4)||(month==6)||(month==9)||(month==11)) {
		if (date > 30) {
			return null;
		}
	}
	// Correct hours value
	if (hh<12 && ampm=="PM") {
		hh=hh-0+12;
	}
	else if (hh>11 && ampm=="AM") {
		hh-=12;
	}
	return new Date(year,month-1,date,hh,mm,ss);
};

// Check if a date string is valid
Date.isValid = function(val,format) {
	return (Date.parseString(val,format) != null);
};

// Check if a date object is before another date object
Date.prototype.isBefore = function(date2) {
	if (date2==null) {
		return false;
	}
	return (this.getTime()<date2.getTime());
};

// Check if a date object is after another date object
Date.prototype.isAfter = function(date2) {
	if (date2==null) {
		return false;
	}
	return (this.getTime()>date2.getTime());
};

// Check if two date objects have equal dates and times
Date.prototype.equals = function(date2) {
	if (date2==null) {
		return false;
	}
	return (this.getTime()==date2.getTime());
};

// Check if two date objects have equal dates, disregarding times
Date.prototype.equalsIgnoreTime = function(date2) {
	if (date2==null) {
		return false;
	}
	var d1 = new Date(this.getTime()).clearTime();
	var d2 = new Date(date2.getTime()).clearTime();
	return (d1.getTime()==d2.getTime());
};

// Format a date into a string using a given format string
Date.prototype.format = function(format) {
	format=format+"";
	var result="";
	var i_format=0;
	var c="";
	var token="";
	var y=this.getYear()+"";
	var M=this.getMonth()+1;
	var d=this.getDate();
	var E=this.getDay();
	var H=this.getHours();
	var m=this.getMinutes();
	var s=this.getSeconds();
	var yyyy,yy,MMM,MM,dd,hh,h,mm,ss,ampm,HH,KK,K,kk,k;
	// Convert real date parts into formatted versions
	var value=new Object();
	if (y.length < 4) {
		y=""+(+y+1900);
	}
	value["y"]=""+y;
	value["yyyy"]=y;
	value["yy"]=y.substring(2,4);
	value["M"]=M;
	value["MM"]=Date.LZ(M);
	value["MMM"]=Date.monthNames[M-1];
	value["NNN"]=Date.monthAbbreviations[M-1];
	value["d"]=d;
	value["dd"]=Date.LZ(d);
	value["E"]=Date.dayAbbreviations[E];
	value["EE"]=Date.dayNames[E];
	value["H"]=H;
	value["HH"]=Date.LZ(H);
	if (H==0){
		value["h"]=12;
	}
	else if (H>12){
		value["h"]=H-12;
	}
	else {
		value["h"]=H;
	}
	value["hh"]=Date.LZ(value["h"]);
	value["K"]=value["h"]-1;
	value["k"]=value["H"]+1;
	value["KK"]=Date.LZ(value["K"]);
	value["kk"]=Date.LZ(value["k"]);
	if (H > 11) {
		value["a"]="PM";
	}
	else {
		value["a"]="AM";
	}
	value["m"]=m;
	value["mm"]=Date.LZ(m);
	value["s"]=s;
	value["ss"]=Date.LZ(s);
	while (i_format < format.length) {
		c=format.charAt(i_format);
		token="";
		while ((format.charAt(i_format)==c) && (i_format < format.length)) {
			token += format.charAt(i_format++);
		}
		if (typeof(value[token])!="undefined") {
			result=result + value[token];
		}
		else {
			result=result + token;
		}
	}
	return result;
};

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
		},

        InputMask : (function() {

            var applyMask = function(inputfieldId, options) {
                var field = jQuery("#" + inputfieldId);
                field.autoNumeric(options);
            };

            return {
                percent: function(inputfieldId) {
                    applyMask(inputfieldId, {aSep: '', aDec: ',', vMin: '0.00', vMax: '100.00'});
                },
                naturalNumber: function(inputfieldId) {
                    applyMask(inputfieldId, {aSep: ' ', vMin: '0', mDec: '0'});
                }
            };
        })(),

        placeMenu : function(parent, element) {
            parent = $(parent);
            var screenBottom = document.viewport.getScrollOffsets().top + document.viewport.getDimensions().height - 20;
            var dD =  document.viewport.getDimensions();
            var dS =  document.viewport.getScrollOffsets();
            element.setStyle( {
                left : 0,
                top : 0,
                visibility : "hidden",
                display : "",
                position : "absolute"
            });
            var left = parent.cumulativeOffset().left;
            var top  = parent.cumulativeOffset().top + parent.getDimensions().height;
            if(left + element.offsetWidth > dD.width) {
                //don't place any part of tooltip outside visible space
                left =  (dD.width - element.offsetWidth + dS.left);
            }

            if (top + element.getHeight() > screenBottom) {
                top = screenBottom - element.getHeight()
            }

            if (top < 0) top = 0;

            element.setStyle({
                left : left + "px",
                top : top + "px",
                zIndex : Rolf.returnZindex(),
                visibility : "visible",
                display : "none"
            });

        }

    }

})();

// todo: Set from snippet to follow user-locale
Rolf.DATE_FORMAT = "dd.MM.yyyy";
Rolf.YAHOO_DATE_FORMAT = "MM/dd/yyyy";
Rolf.YAHOO_MONTH_YEAR_FORMAT = "MM/yyyy";

Rolf.Calendar = {
    /**
     * Returns a config object for YUI Calendar.
     * Provides localized month and day names and other standard config parameters.
     *
     * @param config configs which should be included. Specified configes overwrite default.
     */
    getConfig  : function(config) {
        return Object.extend({
            strings : {close : "", previousMonth : "", nextMonth : ""},
            close: true,
            hide_blank_weeks : true,
            start_weekday : Rolf.getText("format_date_first_day_of_week"),
            months_short : [Rolf.getText("format_date_month_of_year_short_JANUARY"),
                Rolf.getText("format_date_month_of_year_short_FEBRUARY"),
                Rolf.getText("format_date_month_of_year_short_MARCH"),
                Rolf.getText("format_date_month_of_year_short_APRIL"),
                Rolf.getText("format_date_month_of_year_short_MAY"),
                Rolf.getText("format_date_month_of_year_short_JUNE"),
                Rolf.getText("format_date_month_of_year_short_JULY"),
                Rolf.getText("format_date_month_of_year_short_AUGUST"),
                Rolf.getText("format_date_month_of_year_short_SEPTEMBER"),
                Rolf.getText("format_date_month_of_year_short_OCTOBER"),
                Rolf.getText("format_date_month_of_year_short_NOVEMBER"),
                Rolf.getText("format_date_month_of_year_short_DECEMBER")],
            months_long :   [Rolf.getText("format_date_month_of_year_JANUARY"),
                Rolf.getText("format_date_month_of_year_FEBRUARY"),
                Rolf.getText("format_date_month_of_year_MARCH"),
                Rolf.getText("format_date_month_of_year_APRIL"),
                Rolf.getText("format_date_month_of_year_MAY"),
                Rolf.getText("format_date_month_of_year_JUNE"),
                Rolf.getText("format_date_month_of_year_JULY"),
                Rolf.getText("format_date_month_of_year_AUGUST"),
                Rolf.getText("format_date_month_of_year_SEPTEMBER"),
                Rolf.getText("format_date_month_of_year_OCTOBER"),
                Rolf.getText("format_date_month_of_year_NOVEMBER"),
                Rolf.getText("format_date_month_of_year_DECEMBER")],
            weekdays_1char : [ Rolf.getText("format_date_day_of_week_1char_SUNDAY"),
                Rolf.getText("format_date_day_of_week_1char_MONDAY"),
                Rolf.getText("format_date_day_of_week_1char_TUESDAY"),
                Rolf.getText("format_date_day_of_week_1char_WEDNESDAY"),
                Rolf.getText("format_date_day_of_week_1char_THURSDAY"),
                Rolf.getText("format_date_day_of_week_1char_FRIDAY"),
                Rolf.getText("format_date_day_of_week_1char_SATURDAY")],
            weekdays_short : [Rolf.getText("format_date_day_of_week_short_SUNDAY"),
                Rolf.getText("format_date_day_of_week_short_MONDAY"),
                Rolf.getText("format_date_day_of_week_short_TUESDAY"),
                Rolf.getText("format_date_day_of_week_short_WEDNESDAY"),
                Rolf.getText("format_date_day_of_week_short_THURSDAY"),
                Rolf.getText("format_date_day_of_week_short_FRIDAY"),
                Rolf.getText("format_date_day_of_week_short_SATURDAY")],
            weekdays_medium :  [Rolf.getText("format_date_day_of_week_medium_SUNDAY"),
                Rolf.getText("format_date_day_of_week_medium_MONDAY"),
                Rolf.getText("format_date_day_of_week_medium_TUESDAY"),
                Rolf.getText("format_date_day_of_week_medium_WEDNESDAY"),
                Rolf.getText("format_date_day_of_week_medium_THURSDAY"),
                Rolf.getText("format_date_day_of_week_medium_FRIDAY"),
                Rolf.getText("format_date_day_of_week_medium_SATURDAY")],
            weekdays_long : [Rolf.getText("format_date_day_of_week_SUNDAY"),
                Rolf.getText("format_date_day_of_week_MONDAY"),
                Rolf.getText("format_date_day_of_week_TUESDAY"),
                Rolf.getText("format_date_day_of_week_WEDNESDAY"),
                Rolf.getText("format_date_day_of_week_THURSDAY"),
                Rolf.getText("format_date_day_of_week_FRIDAY"),
                Rolf.getText("format_date_day_of_week_SATURDAY")]
        }, config);
    }
};

/**
 * <b>Single date  picker:</b>
 * &lt;div class="datePicker"&gt;
 * &lt;input type="text" class="date"/&gt;
 * &lt;span class="calendarButton"&gt;&lt;/span&gt;
 * &lt;/div&gt;
 */
Rolf.DateSetup = (function() {

    var CALENDAR_CONTAINER_ID = "LIFT_CALENDAR_PICKER";

    function setupAllDatePickerInsideNodeOrBody(parentNode) {
        parentNode = $(parentNode) || $(document.body);

        var unConfiguredDateFields = parentNode.select("input[type=text].date:not(.__liftDateIsConfigured)");
        unConfiguredDateFields.each(function(dateField) {
            setupDatePickerForDateField(dateField);
        });

    }

    function setupDatePickerForDateField(dateField) {
        dateField = $(dateField);

        if (dateField.hasClassName("__liftDateIsConfigured")) return;

        var datePicker = dateField.up(".datePicker");
        var calendarButton = datePicker.down(".calendarButton");

        setupOnClickForCalendarIcon();
        markDateFieldAsConfigured();

        function setupOnClickForCalendarIcon() {
            calendarButton.observe("click", function(evt) {
                var currentDate, maxDate, minDate, myCal;

                if (isDatePickerDisabled()) {
                    //don't show calendar picker for disabled datePickers
                    return;
                }

                calculateCurrentDate();
                setupAndDisplayCalendar();
                positionCalendarAccordingToCalendarIcon();

                function isDatePickerDisabled() {
                    return datePicker.hasClassName("disabled") || datePicker.up(".disabled");
                }

                function calculateCurrentDate() {
                    currentDate = Date.parseString(dateField.value, Rolf.DATE_FORMAT);
                    if(!currentDate) {
                        currentDate = new Date();
                    }
                }

                function setupAndDisplayCalendar() {
                    var calendarContainer;

                    createCalendarContainerIfNotPresent();
                    createCalendarPicker();
                    renderAndDisplayCalendarPicker();

                    function createCalendarContainerIfNotPresent() {
                        var calendarContainer = $(CALENDAR_CONTAINER_ID);
                        if(!calendarContainer || !calendarContainer.up("body")) {
                            calendarContainer = document.body.appendChild(
                                new Element("div", { id : CALENDAR_CONTAINER_ID, style : "diplay: none;", 'class' : "calendarContainer" })
                            );
                            document.body.observe(
                                "click",
                                function(evt) {
                                    //hide calendar when user clicks background
                                    if(calendarContainer && calendarContainer != evt.element().up(".calendarContainer")) {
                                        calendarContainer.hide();
                                    }
                                });
                        }
                    }

                    function createCalendarPicker() {
                        var navConfig = {
                            strings : {
                                month: Rolf.getText('js_date_picker_navigator_choose_month'),
                                monthFormat: YAHOO.widget.Calendar.LONG,
                                year: Rolf.getText('js_date_picker_navigator_enter_year'),
                                submit: Rolf.getText('js_date_picker_navigator_submit'),
                                cancel: Rolf.getText('js_date_picker_navigator_cancel'),
                                invalidYear: Rolf.getText('js_date_picker_navigator_invalidYear')
                            },
                            monthFormat: YAHOO.widget.Calendar.LONG,
                            initialFocus: "year"
                        };
                        myCal = new YAHOO.widget.Calendar("cal", CALENDAR_CONTAINER_ID,
                            Rolf.Calendar.getConfig({
                                title: Rolf.getText('js_date_picker_header_label'),
                                selected : currentDate.format(Rolf.YAHOO_DATE_FORMAT),
                                mindate: minDate ? minDate.format(Rolf.YAHOO_DATE_FORMAT): undefined,
                                maxdate: maxDate ? maxDate.format(Rolf.YAHOO_DATE_FORMAT) : undefined,
                                pagedate : currentDate.format(Rolf.YAHOO_MONTH_YEAR_FORMAT),
                                navigator:navConfig
                            }) );
                        myCal.selectEvent.subscribe(dateSelectedFunction, myCal, true);
                        myCal.container = $(CALENDAR_CONTAINER_ID);
                        myCal.boundInputField = dateField;

                        function dateSelectedFunction(type, args, calendar) {
                            var selected = args[0];
                            var selDate = calendar.toDate(selected[0]);
                            calendar.boundInputField.value = selDate.format(Rolf.DATE_FORMAT);
                            jQuery(calendar.boundInputField).trigger("blur");
                            calendar.container.hide();
                            dateField.findForm().setDirty(true);
                        }
                    }

                    function renderAndDisplayCalendarPicker() {
                        myCal.render();
                        myCal.show.bind(myCal).defer();
                    }


                }

                function positionCalendarAccordingToCalendarIcon() {
                    var calendarContainer = $(CALENDAR_CONTAINER_ID);
                    Rolf.placeMenu(calendarButton, calendarContainer);
                }


            });

        }

        function markDateFieldAsConfigured(){
            dateField.addClassName("__liftDateIsConfigured");
        }

    }

    return {
        setupDatePickers : function(parentNode) {
            parentNode = $(parentNode);
            setupAllDatePickerInsideNodeOrBody(parentNode);
        },

        setupDatePicker : function(inputEl) {
            setupDatePickerForDateField(inputEl);
        },

        fixDate: function(el) {
            var sDate = jQuery(el).val();

            if(sDate == undefined || sDate.trim() == "") {
                return true;
            }

            sDate = sDate.trim();

            var currentDate = new Date();

            var day = currentDate.getDate();
            var month = currentDate.getMonth() + 1;
            var year = currentDate.getFullYear();

            var dayRegExp = /^(\d{1,2})$/;
            var dayAndMonthRegExp = /^(\d{1,2})\D*?(\d{1,2})$/;
            var fulldateRegExp = /^(\d{1,2})\D*?(\d{1,2})\D*?(\d{1,4})$/;

            if (sDate.match(dayRegExp)) {
                // Match pattern "dd" and transform it to day of current month in current year
                var dayMatch = dayRegExp.exec(sDate);
                day = Number(dayMatch[1]);
                log("Matched day: " + day);
            } else if (sDate.match(dayAndMonthRegExp)) {
                // Match pattern "dd.MM" and transform it to day of current month in current year
                var dayAndMontMatch = dayAndMonthRegExp.exec(sDate);
                day = Number(dayAndMontMatch[1]);
                month = Number(dayAndMontMatch[2]);
                log("Matched day: " + day + ", month: " + month);
            } else if (sDate.match(fulldateRegExp)) {
                var fulldateMatch = (fulldateRegExp).exec(sDate);
                day = Number(fulldateMatch[1]);
                month = Number(fulldateMatch[2]);
                year = Number(fulldateMatch[3]);
                log("Matched day: " + day + ", month: " + month + ", year: " + year);
            } else {
                log("No match");
                return true;
            }

            if (year < 1000) {
                year = 2000 + year;
            }

            var newDate = new Date(year, month - 1, day);
            if (month == newDate.getMonth() + 1 && day == newDate.getDate()) {
                jQuery(el).val(newDate.format("dd.MM.yyyy"));
            } else {
                log("Invalid date: " + newDate);
            }

            return true;
        }

    }
})();

Rolf.Blog = (function() {
    return {
        prepareVoteBox: function(commentId, hasVoted, voteValue) {
            var commentContainer = $("comment_" + commentId);
            var voteContainer = commentContainer.down(".voteOperationContainer");
            if (!Object.isUndefined(voteContainer)) {
                var plusVoteContainer = voteContainer.down(".addPlusVote");
                var minusVoteContainer = voteContainer.down(".addMinusVote");
                var removeVoteContainer = voteContainer.down(".removeVote");

                var voteValueMessage = (function() {if (voteValue) {return "+";} else return "-";})();
                if (hasVoted) {
                    jQuery(plusVoteContainer).attr("title", "You have already voted (" + voteValueMessage + "), cannot add vote");
                    jQuery(minusVoteContainer).attr("title", "You have already voted (" + voteValueMessage + "), cannot add vote");
                    jQuery(removeVoteContainer).attr("title", "Click to remove your vote");
                } else {
                    jQuery(plusVoteContainer).attr("title", "Click to add a + vote");
                    jQuery(minusVoteContainer).attr("title", "Click to add a - vote");
                    jQuery(removeVoteContainer).attr("title", "You have not voted yet");
                }
            }
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
