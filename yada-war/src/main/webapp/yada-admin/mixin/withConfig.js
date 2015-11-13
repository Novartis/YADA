/*
 * Copyright 2015 Novartis Institutes for BioMedical Research Inc.
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