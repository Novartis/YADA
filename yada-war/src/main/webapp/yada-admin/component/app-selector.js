define(
  [
	'flight',
	'jquery',
	'lodash',
	'autocomplete'
  ],
  function (flight,$,_,autocomplete) {
	  
  	var flight = require('flight');
	  'use strict';
	  return flight.component(appSelector);
	  
	  function appSelector() {
	  	
	  	this.refresh = function(app) {
	  		this.setTitle(app);
	  		$('#toggle-view,#new-query').removeClass('disabled');
	  		this.trigger(this.$node,'app-requested',{app:app.data});
	  		
	  	};
	  	
	  	this.setTitle = function(app) {
	  		$('#app-selection h1').text(app.data);
	  	};
	  	
	  	this.enrich = function() {
	  		var self = this;
	  		$('.app-selector').autocomplete({
		  		serviceUrl:$.ajaxSettings.url,
		  		paramName: 'p',
		  		params:{
		  			q:'YADA apps',
		  			c:false,
		  			pz:-1,
		  		},
		  		transformResult: function(response) {
		        return {
		            suggestions: $.map($.parseJSON(response).RESULTSET.ROWS, function(dataItem) {
		                return { value: dataItem.LABEL, data: dataItem.LABEL };
		            })
		        };
		  		},
		  		onSelect: self.refresh.bind(self),
		  		width: 190
		  	});
	  	};
	  	
	  	this.closeDialog = function() {
	  		$('#app-selector').modal('hide');
	  		$('.app-selector').val('');
	  	};
	  	
      this.after('initialize', function () {
      	$(document).on('close-selector',this.closeDialog);
      	this.enrich();
      	$('#app-selector').modal({show:true});
      });
	  }
});

