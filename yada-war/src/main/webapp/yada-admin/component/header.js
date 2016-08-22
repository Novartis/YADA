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
  [
	'flight',
	'jquery',
	'bootstrap'
  ],
  function (flight,$) {
	'use strict';
    
	  return flight.component(header);
	  
	  function header() {
		  
	  	this.enrich = function() {

	  	}; 
	  	
	  	this.defaultAttrs({
	  	  'app-menu': '#app a',
	  	  'mig-menu': '#migration a',
	  	  'app-mgr' : '#app-mgr',
	  	  'query-table' : '#query-table',
	  	  'mig-table'   : '#migration-table'
	  	});
	      
      this.after('initialize', function () {
      	//this.enrich();
        this.on('click',{
          'app-menu':function() { 
             this.trigger(this.attr['query-table'], 'destroy.ya.query-table',{});
             this.trigger(this.attr['mig-table'], 'destroy.ya.migration-table',{});
             this.trigger(this.attr['app-mgr'],'init-request.ya.app-mgr',{}); 
           }
        });
      });
	  }
});