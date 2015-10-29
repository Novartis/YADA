define(
  [
	'flight',
	'jquery',
	'bootstrap',
  ],
  function (flight,$) {
	'use strict';
    
	  return flight.component(header);
	  
	  function header() {
		  
	  	this.enrich = function() {

	  	}; 
	      
      this.after('initialize', function () {
      	this.enrich();
      });
	  }
});