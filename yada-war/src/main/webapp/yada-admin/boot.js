/*
 * Copyright 2016 Novartis Institutes for BioMedical Research Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
			         'component/login',
			         'component/app-mgr',
			         'component/migration',
			         'component/query-params'
			         ],

		  function ($, domReady, bootstrap, header, login, appMgr, /*qtable, appSelector,*/ migration, params) {

				// set ajax defaults
				var appContext = (this.context() != "ROOT" ? this.context() + '/': '');
        var baseUrl = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
				var defaultUrl = '/' + appContext + 'yada.jsp';
				$.ajaxSetup({
					url: baseUrl + defaultUrl,
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

				$.fn.highlight = function(color) {
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


				header.attachTo('nav.main-menu');
				$.ajax({
				  type: 'POST',
				  data: {
				    q:'YADA select prop value',
				    p:['system','login'].join(',')
				  },
				  success:function(resp) {
				    if(resp.RESULTSET.ROWS[0].VALUE == 'default')
				      login.attachTo('#login');
				  }
				});

				appMgr.attachTo('#app-mgr');
				migration.attachTo('.nest');
			});
		}
	}
);
