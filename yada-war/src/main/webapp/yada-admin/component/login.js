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
	'bootstrap',
  ],
  function (flight,$) {
	'use strict';
    
	  return flight.component(login);
	  
	  function login() {
		  
	  	this.enrich = function() {

	  	}; 
	  	
	  	this.acceptCredentials = function(uid) {
	  	  // set user, role in scope
	  	  // trigger app-mgr
	  	  this.closeDialog();
	  	  $(this.attr.nest).data('uid',uid);
	  	  this.trigger(this.attr['app-mgr'],'init-request.ya.app-mgr',{});
	  	};
	  	
	  	this.checkCredentials = function(e,d) {
	  	  var self = this;
	  	  var valid = false;
	  	  var uid = this.select('user').val();
	  	  var pw = this.select('logpw').val();
	  	  // do ajaxy stuff
	  	  $.ajax({
	  	    type:'POST',
	  	    data:{
	  	      q:'YADA check credentials',
	  	      p:[uid,pw].join(',')
	  	    },
	  	    success: function(resp) {
	  	      var o = resp.RESULTSET.ROWS[0]; 
	  	      if(o.AUTH == true)
	  	        self.acceptCredentials(o.USER);
	  	    }
	  	  });
	  	  // in success handler:
	  	    
	  	};
	  	
	  	this.closeDialog = function() {
        this.select('input').val('');
	  	  this.select('close').click();
      };
	    
	  	this.defaultAttrs({
	  	  'button': '#login-btn',
	  	  'close' : '.close',
	  	  'input' : 'input',
	  	  'user'  : '#login-user',
	  	  'app-mgr': '#app-mgr',
	  	  'logusr': '#login-user',
	  	  'logpw' : '#login-pw',
	  	  'nest'  : '.nest'
	  	});
	  	
      this.after('initialize', function () {
        this.on('click',{
          'button':this.checkCredentials
        });
        //this.enrich();
        $('#login').modal({show:true});
      });
	  }
});