define(function(require) {
	
	return withConfig;
	
	function withConfig() {
		  	
		this.config = $.parseJSON(window.YADAAdmin);
		
		this.isEmpty = function(v) {
			if(v === null || v === undefined || v === '')
				return true;
			return false;
		};
		
		this.domain = function() {
			var defaultDomain = document.domain.split(/\./).splice(-2).join('.');
			if (this.isEmpty(this.config.domain))
				return defaultDomain;
			return this.config.domain;
		};
		
		this.loggedUser = function() {
			var defaultUser = 'UNKNOWN1', loggedUser;
			if (this.isEmpty(this.config.loggedUser))
				loggedUser = defaultUser;
			else
				loggedUser = this.config.loggedUser;
			return loggedUser;
		};
		
		this.context = function() {
			return this.config.context;
		};
		
		this.migrationHosts = function() {
			var hosts = [
			   					 {value:'dev',  data:this.config.yadaDev},
			  					 {value:'test', data:this.config.yadaTest},
			  					 {value:'load', data:this.config.yadaLoad},
			  					 {value:'prod', data:this.config.yadaProd}
			  				  ]; 
			return hosts;
		};
	};
});