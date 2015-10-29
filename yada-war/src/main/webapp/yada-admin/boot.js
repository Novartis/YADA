define(
	
	function(require) {

		return { initialize: initialize };

		function initialize() {
			//DEBUG.events.logAll();
			// draw UI header, grid
			require(['jquery',
			         'domReady',
			         'bootstrap',
			         'component/header',
			         'component/query-table',
			         'component/app-selector',
			         'component/migration',
			         'component/query-params'
			         ], 
			         
		  function ($, domReady, bootstrap, header, qtable, appSelector, migration, params) {
				
				// set ajax defaults
				var appContext = (this.context() != "ROOT" ? this.context() + '/': '');
				var defaultUrl = '/' + appContext + 'yada.jsp';
				$.ajaxSetup({
					url: defaultUrl,
		      type: 'GET',
		      dataType: 'json'
				});
				
			    
		    $.fn.when = function( events, eventData ) {
		        
		        events = events.split( /\s/g ); // parse list of events
		        
		        var deferreds = [], // array of deferreds
		            iElem, // each $node
		            lengthElem = this.length, // length of $nodes array
		            iEvent, // each event
		            lengthEvent = events.length, // length of events array
		            elem; // each elemment
		        
		        for( iElem = 0; iElem < lengthElem; iElem++ ) { // for each $node
		            elem = $( this[ iElem ] );
		            for ( iEvent = 0; iEvent < lengthEvent; iEvent++ ) { // for each event
		                deferreds.push( $.Deferred(function( defer ) { // create deferred and add to arary
		                    var element = elem, // current element
		                        event = events[ iEvent ]; // current event
		                    function callback() { // event handler
		                        element.unbind( event, callback ); // unbind handler after it fires
		                        var payload = null;
		                        if(typeof eventData == 'function')
		                        	payload = eventData();
		                        else if(typeof eventData == 'object')
		                        	payload = eventData;
		                        else if(typeof eventData == 'string')
		                        	payload = eventData;
		                        defer.resolve(payload);  // resolve the deferred
		                    }
		                    element.bind( event, callback ); // bind the event and handler to the element
		                }) );
		            }
		        }
		        return $.when.apply( null, deferreds ); // call $.when with all the deferreds and return the promise
		    };

				$.fn.highlight = function() {
				   $(this).each(function() {
				        var el = $(this);
				        el.before("<div/>");
				        el.prev()
				            .width(el.width())
				            .height(el.height())
				            .css({
				                "position": "absolute",
				                "background-color": "#ffff99",
				                "opacity": ".9"   
				            })
				            .fadeOut(1500);
				    });
				};

				var nest = $('.nest');

				header.attachTo('body');
				qtable.attachTo(document);
				appSelector.attachTo('#app-selector');
				migration.attachTo('.nest');
				params.attachTo('#default-params');
			});
		}
	}
);