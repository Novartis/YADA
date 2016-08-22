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
	'component/query-table',
	'bootstrap'
  ],
  function (flight,$,queryTable) {
	'use strict';
    
	  return flight.component(appMgr);
	  
	  function appMgr() {
		  $('nav.main-menu li').addClass('disabled');
	    this.getAppTemplate = function(app) {
	  	  var html = '<div class="panel panel-default">'+
          '<div class="panel-heading clearfix" role="tab" id="app-hdr-'+app+'" data-toggle="collapse" data-parent="#app-accordion" data-target="#app-'+app+'">'+
            '<button id="app-qname-'+app+'" type="button" class="btn-qname btn btn-default pull-right">Queries</button>'+
            '<h4 class="panel-title">'+app+'</h4>'+
          '</div>'+
          '<div id="app-'+app+'" class="panel-collapse collapse" role="tabpanel" aria-labelledby="app-hdr-'+app+'">'+
            '<div class="panel-body">'+
              '<form>'+
                '<div class="row">'+
                  '<div class="form-group col-lg-1 col-md-2 col-sm-3">'+
                    '<label for="app-code-'+app+'">Code</label>'+
                    '<input type="text" id="app-code-'+app+'" class="form-control"/>'+
                  '</div>'+
                  '<div class="form-group col-lg-1 col-md-1 col-sm-3">'+
                    '<label for="app-active-'+app+'">Active</label>'+
                    '<input type="checkbox" id="app-active-'+app+'" class="form-control" value="1"/>'+
                  '</div>'+
                  '<div class="form-group col-lg-3 col-md-3 col-sm-6">'+
                    '<label for="app-name-'+app+'">Name</label>'+
                    '<input type="text" id="app-name-'+app+'" class="form-control"/>'+
                  '</div>'+
                  '<div class="form-group col-lg-7 col-md-6 col-sm-12">'+
                    '<label for="app-desc-'+app+'">Description</label>'+
                    '<textarea id="app-desc-'+app+'" class="form-control"></textarea>'+
                  '</div>'+
                '</div>'+
                '<div class="row">'+
                  '<div class="form-group col-lg-12 col-md-12 col-sm-12">'+
                    '<label for="app-conf-'+app+'">Configuration</label>'+
                    '<textarea id="app-conf-'+app+'" class="form-control" rows="11"></textarea>'+
                  '</div>'+
                '</div>'+
                '<div class="row">'+
                  '<div class="form-group col-lg-12 col-md-12 col-sm-12">'+
                    '<button id="app-submit-'+app+'" class="app-save btn btn-primary pull-right">Save</button>'+
                  '</div>'+
                '</div>'+
              '</form>'+
            '</div>'+
          '</div>'+
        '</div>';
	  	  return html;
	  	};
	  	
	  	this.populate = function(e,d) {
        for(var i in d.data.RESULTSET.ROWS) {
          
          var appObj = d.data.RESULTSET.ROWS[i];
          if(!/BSRRSW/.test(appObj.APP))
          {
            var html = this.getAppTemplate(appObj.APP)
            this.select('accordion').append(html);
            if(appObj.ACTIVE == 0)
              $('#app-hdr-'+appObj.APP+' .panel-title').css('color','#777');
            $('#app-'+appObj.APP).data('app-data',appObj);
          }
        }
      };
      
      this.expressValues = function(e,d) {
        var panel  = $(e.target);
        var appObj = panel.data('app-data');
        var app;
        if(appObj !== undefined && appObj !== null)
        {
          app = appObj.APP;
          panel.find('#app-code-'+app).val(app);
          panel.find('#app-name-'+app).val(appObj.NAME);
          panel.find('#app-desc-'+app).val(appObj.DESCR);
          panel.find('#app-conf-'+app).val(appObj.CONF);
          if(appObj.ACTIVE == 1)
            panel.find('#app-active-'+app).attr('checked','checked');
          else
            panel.find('#app-active-'+app).removeAttr('checked');
        }

      };
      
	  	this.show = function(e,d) {
	  	  var self = this;
	  	  this.$node.removeClass('hidden');
	  	  this.select('accordion').empty().append(this.getAppTemplate('new'));
	  	  $('#app-hdr-new button').css('visibility','hidden');
	  	  $('#app-code-new').attr('placeholder','e.g., FOO');
	  	  $('#app-name-new').attr('placeholder','e.g., E-Scape, Smartopedia...');
	  	  $('#app-desc-new').attr('placeholder','Describe the data source here...');
	  	  $('#app-conf-new').val('#This is real example code.\n'+
	  	      '#see https://github.com/brettwooldridge/HikariCP for documentation\n'+
	  	      'jdbcUrl=e.g., jdbc:sqlite:/apps/yada/tomcat/webapps/ROOT/YADA.db\n'+
	  	      'username=<replace>\n'+
	  	      'password=<replace>\n'+
	  	      'autoCommit=false\n'+
	  	      'connectionTimeout=300000\n'+
	  	      'idleTimeout=600000\n'+
	  	      'maxLifetime=1800000\n'+
	  	      'minimumIdle=5\n'+
	  	      'maximumPoolSize=100\n'+
	  	      'driverClassName=e.g., org.sqlite.JDBC');
	  	  $.ajax({
          data: {
            q: self.attr.q_select_apps,
            s: 'APP'
          },
          success: function(data) { self.trigger('populate-request.ya.app-mgr',{data:data}) }
        });
	  	};
	  	
	  	this.saveApp = function(e,d) {
	  	  e.preventDefault();
	  	  var self   = this;
	  	  var q      = 'q_update_app';
	  	  var panel  = $(e.target).parents('.panel-collapse');
	  	  var appObj = panel.data('app-data');
	  	  var app, code, name, desc, conf, active;
	  	  if(appObj === undefined || appObj === null)
	  	  {
          app = 'new';
          code   = panel.find('#app-code-'+app).val();
          name   = panel.find('#app-name-'+app).val();
          desc   = panel.find('#app-desc-'+app).val();
          conf   = panel.find('#app-conf-'+app).val();
          active = panel.find('#app-active-'+app).is(':checked') ? 1 : 0;
          q   = 'q_insert_app';
	  	  }
	  	  else
	  	  {
	  	    app    = appObj.APP;
	  	    code   = app;
	  	    name   = panel.find('#app-name-'+app).val();
          desc   = panel.find('#app-desc-'+app).val();
          conf   = panel.find('#app-conf-'+app).val();
          active = panel.find('#app-active-'+app).is(':checked') ? 1 : 0;
	  	  }
	  	  var progress = $('<div class="progress progress-striped active col-lg-11 col-md-11 col-sm-10">'+
              '<div class="progress-bar progress-bar-warning" role="progressbar" aria-valuenow="45" aria-valuemin="0" aria-valuemax="100" style="width: 0%">'+
            '<span class="sr-only">100% Complete</span>'+
          '</div>'+
        '</div>');
	  	  progress.insertBefore('#app-submit-'+app);
	  	  var start = parseInt(new Date().getTime()/1000)
	  	  setInterval(function() {
	  	    var now = parseInt(new Date().getTime()/1000);
	  	    $('.progress-bar').css('width',((now-start)/300)*100 + '%');
	  	  },3000)
        
	  	  $.ajax({
	  	    type:'POST',
	  	    data:{
	  	      j : JSON.stringify([{qname:self.attr[q],DATA:[{APP:code,NAME:name,DESCR:desc,CONF:conf,ACTIVE:active}]}])
	  	    },
	  	    success: function(resp) {
	  	      if(app == 'new') 
	  	      {
	  	        var html = self.getAppTemplate(code);
	  	        var current, last;
	  	        var panels = $('.panel-title');
	  	        for(var i=0; i < panels.length; i++)
	  	        {
	  	          var current = $(panels[i]);
	  	          var next    = $(panels[i+1]);
	  	          if(current.text() < code && next.text() > code)
	  	          {
	  	            var obj = {APP:code,NAME:name,DESCR:desc,CONF:conf,ACTIVE:active};
	  	            var $html = $(html) 
	  	            $html.insertAfter(current.closest('.panel'));
	  	            $html.find('.panel-collapse').data('app-data',obj);
	  	            break;
	  	          }
	  	          $('#app-new').collapse('hide');
	  	        };
	  	        panel.find('#app-code-'+app).val('');
	            panel.find('#app-name-'+app).val('');
	            panel.find('#app-desc-'+app).val('');
	            panel.find('#app-conf-'+app).val('');
	            panel.find('#app-active-'+app).removeAttr('checked');
	  	      }
	  	      progress.remove();
	  	      var alert = $('<div class="alert alert-success col-lg-11 col-md-11 col-sm-10"><strong>Now we\'re cookin\'!</strong> Data saved successfully</div>');
	  	      alert.insertBefore('#app-submit-'+app);
	  	      setTimeout(function() { alert.fadeOut(); }, 1500);
	  	    },
	  	    error: function(xhr,status,error) {
	  	      progress.remove();
	  	      var alert = $('<div class="alert alert-danger alert-dismissable col-lg-11 col-md-11 col-sm-10"><strong>Oh man!</strong> Data was not saved. Try again. (Status: '+xhr.status+', '+error+'.) This message will self-destruct in 30 seconds.</div>');
            alert.insertBefore('#app-submit-'+app);
            setTimeout(function() { alert.fadeOut(); }, 30000);
	  	    }
	  	  });
	  	};
	  	
	  	this.goToQueries = function(e,d) {
	  	  var app = $(e.target).closest('.panel').find('.panel-collapse').data('app-data').APP
	  	  $(this.attr['query-table']).data('app',app);
	  	  var self = this;
        this.$node.addClass('hidden');
        this.select('accordion').empty();
        queryTable.attachTo(document);
        this.trigger('view.ya.query-table',{app:app});
	  	};
	  	
	  	this.defaultAttrs({
	  	  'q_select_apps' : 'YADA select apps',
	  	  'q_insert_app'  : 'YADA new app',
	  	  'q_update_app'  : 'YADA update app',
	  	  'q_delete_app'  : 'YADA delete app',
	  	  'accordion'     : '#app-accordion',
	  	  'save'          : '.app-save',
	  	  'btn-qname'     : '.btn-qname',
	  	  'query-table'   : '#query-table'
 	  	});
	      
      this.after('initialize', function () {

      	this.on('init-request.ya.app-mgr',this.show);
      	this.on('populate-request.ya.app-mgr',this.populate);
      	this.on('shown.bs.collapse',this.expressValues);
      	this.on('click',{
      	  'save':this.saveApp,
      	  'btn-qname':this.goToQueries
      	});
      	this.trigger(this.$node,'init-request.ya.app-mgr',{});
      });
	  }
});