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
	  	  'app-menu'    : '#app',
	  	  'new-query'   : '#new-query',
	  	  'toggle-view' : '#toggle-view',
	  	  'mig-menu'    : '#migration',
	  	  'backup-menu' : '#backup',
	  	  
	  	  'app-mgr'     :  '#app-mgr',
	  	  'query-table' : '#query-table',
	  	  'mig-table'   : '#migration-table'
	  	});
	  	
	  	this.enableAppMgr = function(e,d) {
	  	  this.select('app-menu').removeClass('disabled')
	  	  .removeAttr('disabled')
	  	  .find('a').attr('href','#');
	  	};
	  	
	  	this.enableNewQuery = function(e,d) {
	  	  this.select('new-query').removeClass('disabled')
	  	  .removeAttr('disabled')
	  	  .attr('data-toggle','modal')
	  	  .attr('data-target','#query-editor-container')
	  	  .find('a').attr('href','#');
	  	};
	  	
	  	this.enableToggleView = function(e,d) {
	  	  this.select('toggle-view').removeClass('disabled')
	  	  .removeAttr('disabled')
	  	  .on('click',d.fn)
	  	  .find('a').attr('href','#');
	  	};
	  	
	  	this.enableBackup = function(e,d) {
	  	  this.select('backup-menu').removeClass('disabled')
	  	  .removeAttr('disabled')
	  	  .on('click',d.fn)
	  	  .find('a').attr('href','#')
	  	  
	  	}
	  	
	  	this.enableMigration = function(e,d) {
	  	  this.select('mig-menu').removeClass('disabled')
	  	  .removeAttr('disabled')
	  	  .on('click',d.fn)
	  	  .attr('data-toggle','modal')
        .attr('data-target','#migration-target-selector')
        .find('a').attr('href','#');
	  	};

      // disable	  	
	  	this.disableAppMgr = function(e,d) {
	  	  this.select('app-menu').addClass('disabled')
	  	  .attr('disabled','disabled')
	  	  .find('a').removeAttr('href');
	  	};

	  	this.disableNewQuery = function(e,d) {
	  	  this.select('new-query').addClass('disabled')
	  	  .attr('disabled','disabled')
	  	  .removeAttr('data-toggle','modal')
        .removeAttr('data-target','#query-editor-container')
	  	  .find('a').removeAttr('href');
      };
      
      this.disableToggleView = function(e,d) {
        this.select('toggle-view').addClass('disabled')
        .attr('disabled','disabled')
        .off('click',d.fn)
        .find('a').removeAttr('href');
      };
      
      this.disableBackup = function(e,d) {
        this.select('backup-menu')
        .addClass('disabled')
        .attr('disabled','disabled')
        .off('click',d.fn)
        .find('a').removeAttr('href');
      }
      
      this.disableMigration = function(e,d) {
        this.select('mig-menu').addClass('disabled')
        .attr('disabled','disabled')
        .off('click',d.fn)
        .removeAttr('data-toggle')
        .removeAttr('data-target')
        .find('a').removeAttr('href');
      };
      
      this.after('initialize', function () {
      	//this.enrich();
        this.on('enable-app-mgr.ya.menu',this.enableAppMgr),
        this.on('enable-new-query.ya.menu',this.enableNewQuery),
        this.on('enable-toggle-view.ya.menu',this.enableToggleView),
        this.on('enable-backup.ya.menu',this.enableBackup),
        this.on('enable-migration.ya.menu',this.enableMigration),
        this.on('disable-app-mgr.ya.menu',this.disableAppMgr),
        this.on('disable-new-query.ya.menu',this.disableNewQuery),
        this.on('disable-toggle-view.ya.menu',this.disableToggleView),
        this.on('disable-backup.ya.menu',this.disableBackup),
        this.on('disable-migration.ya.menu',this.disableMigration),
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