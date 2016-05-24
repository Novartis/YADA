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
	'datatables',
	'component/query-params',
	'mixin/withDateUtils',
	'mixin/withConfig',
	'Clipboard',
	'codemirror',
	'codemirror/mode/sql/sql',
	'codemirror/mode/javascript/javascript.min',
	'codemirror/mode/xml/xml.min',
	],
  function (flight,$,dataTables,Params,withDateUtils,withConfig,Clipboard,CodeMirror) {
	  
	  'use strict';
	  return flight.component(queryTable,withDateUtils,withConfig);
	  
	  //TODO yada mode for codemirror (yada-sql, yada-xml, yada-json, yada-soap (json/xml)
	  
	  function queryTable() {
	  	
	  		this.app   = 'YADA';
	  		this.qname = '';
	  		this.comments = '';
	  		this.edit  = '';
	  		this.editor = {};
	  		this.query = '';
	  		
		  	this.enrich = function() { // triggered at init
		  		var self = this;
		  		$('#toggle-view,#new-query').removeClass('disabled');
		  		
		  		this.select('query-table').dataTable({
		  		  autoWidth:false,
		  			ajax:{
		  				url:$.ajaxSettings.url,
		  				type:"POST",
		  				data:function(d) {
		  					return $.extend( {}, d, {
		  						qname:"YADA queries",
		    					params:self.app,
		    					pz:"-1"
		  		      });
		  				},
		  				dataSrc:"RESULTSET.ROWS"
		  			},
		  			"lengthMenu": [[ 5, 10, 25, 50, 75, -1 ],[5, 10, 25, 50, 75, 'All']],
		  			dom:'<"top"f>t<"bottom-l"i><"bottom-c"p><"bottom-r"l>',
		  			language: {
		  				search: 'Filter on qname, query, or comments:',
		  				searchPlaceholder: 'Filter...'
		  			},
		  			columnDefs:[
		  		  {
		  		  	targets:2,
		  		  	render:function(data,type,row,meta) {
		  		  		return '<pre>'+data.replace(/</g,"&lt;").replace(/>/g,"&gt;")+'</pre>';
		  		  	}
		  		  },
		  		  {
		  				targets:3,
		  				render:function(data,type,row,meta) {
		  				  return data.replace(/</g,"&lt;").replace(/>/g,"&gt;");
		  				}
		  			},
		  			{
              targets:4,
              render: function(data,type,row,meta) {
                var len = 12;
                if(type === 'display' && data.length > len) 
                {
                  return '<span data-toggle="tooltip" title="' + data + '">' + data.substr(0,len) + '…</span>';  
                }
                return data; //"short display", or filter
              }
            },
		  		  {
		  				targets:[6,8], // date columns
		  				type:"date",
		  				className: 'dt-body-center',
		  				render:function(data,type,row,meta) {
		  					if(data == "")
		  						return data;
		  					var txt = self.getFormattedDate(data,"oracle");
		  					return '<span title="'+txt+'">'+txt.substr(0,10)+'</span>';
		  				}
		  			}],
		  			columns:[
			        {
	                "className":      '',
	                "orderable":      false,
	                "data":           null,
	                "defaultContent": '<span class="fa fa-chevron-circle-right fa-md"></span>',
	                visible:          false
	            },
		  				{data:"QNAME",title:"Qname",searchable:true},
		  				{data:"QUERY",title:"Query",name:"QueryCode",searchable:true},
		  				{data:"QUERY",title:"Query",name:"QueryText",searchable:true,visible:false},
		  				{data:"COMMENTS",title:"Comments",searchable:true},
		  				{data:"DEFAULT_PARAMS",title:'Default Parameters',type:'num',searchable:false,className:'dt-body-center'},
		  				{data:"LAST_ACCESS",title:"Last Accessed",searchable:false},
		  				{data:"ACCESS_COUNT",title:"Access Count",searchable:false,type:'num',className:'dt-body-right'},
		  				{data:"MODIFIED",title:"Last Modified",searchable:false},
		  				{data:"MODIFIED_BY",title:"Modified By",searchable:false}
		  			]
		  		});
	  		};
		  	
		  	this.getDefaultParams = function(e,d) {
		  	  var self = this;
		  		var sourceParams = $.ajax({
		  			type:'POST',
		  			data:{
		  				q:'YADA select default params for app',
		  				p:this.app,
		  				pz:-1,
		  				c:false
		  			} 
		  		});
		  		var resolve = function(data) {
            var params = [];
              _.each(data.RESULTSET.ROWS, function(param) {
                params.push(param);
            });
            $.removeData('.nest','defaultParams');
            self.select('nest').data('defaultParams',params);
          };
		  		sourceParams.then(resolve);
		  	};
	  		
	  		this.reloadTable = function(e,d) {
	  			$('#migration-table_wrapper').hide();
	  			$('#query-table_wrapper').show();
	  			this.app = d.app;
	  			var table = this.select('query-table').DataTable(); 
	  			table.ajax.reload();
	  			table.draw();
	  		};
	  		
	  		this.editQuery = function(e,d) {
	  			this.edit   = 'update';
	  			var  self   = this,
	  			     target = $(e.target),
	  			     table  = this.select('query-table').DataTable();
	  			
	  			// get the row
	  			if(e.target.nodeName == 'PRE' || e.target.nodeName == 'SPAN')
	  			{
	  				target = target.parent();
	  			}
	  			var row     = table.row(target.closest('tr')).index()
	  		  var cell    = table.cell(row, 2);
	  			
	  			// store the data
		  		this.query  = cell.data();
		  		this.qname  = table.cell(row,1).data();
		  		this.comments = table.cell(row,4).data();
		  		
		  		// adjust ui
		  		this.select('qname').val(this.qname).attr('readonly','readonly');
		  		this.select('qname-box').addClass('input-group');
		  		this.select('clipboard').show();
		  		this.select('button-copy').show();
		  		this.select('button-rename').show();
          this.select('button-delete').show();
		  		this.select('query-comments').val(this.comments);
		  		
		  		// display the modal
		  	  this.select('container').modal('show');
		  		var $tr = target.closest('tr');
  				this.trigger('#default-params','show-params',{
  					tr    : $tr,
  					qname : self.qname,
  					params: _.filter(this.select('nest').data('defaultParams'),{TARGET:self.qname})
  				});
	  		};
	  		
	  		
	  		this.renderEditor = function(e,d) {
	  			var self = this;
	  			if($(e.target).prop('id') == 'query-editor-container') // specific modal
	  			{
		  			this.editor = CodeMirror(
	      			document.getElementById('query-editor'),
	  					{
	  						value:self.query,
	  						lineNumbers:true,
	  						firstLineNumber:1,
	  						theme:'eclipse'
	  				});
		  				
		  	    var mode = 'text/plain';
		  	    if(/^\s*(select|insert|update|delete)/.test(self.query.toLowerCase())) 
		  	    {
		  	    	mode = 'text/x-sql';
		  			}
		  	    else if(/^\s*[{[]/.test(self.query))
		  	    {
		  	    	mode = 'application/json';
		  	    }
		  	    else if(/^\s*</.test(self.query))
		  	    {
		  	    	mode = 'application/xml';
		  	    }
		  	    this.editor.setOption('mode',mode);
		  	    this.clip = new Clipboard(this.attr['clipboard'],
              { text: function(trigger) {
                  if(trigger.id == self.attr['clipboard-editor'].replace('#',''))
                    return self.editor.getValue();
                }
              }
		  	    );
		  	    this.clip.on('success',function(e) {
		  	      var trig = $(e.trigger);
		  	      trig.tooltip('show');
		  	      setTimeout(function(){
  		  	       trig.tooltip('hide');
  		  	    }, 1000);
		  	    });
		  	    this.select('tooltip').tooltip({trigger:'manual',title:'Copied!'});
	  			}
	  		};
	  		
	  		this.addQuery = function(e,d) { // triggered by 'new query' button and row-click
	  		  this.select('params-wrapper').hide()
	  		  this.restoreFooter();
	  		  this.renderEditor(e,d);
	  		  var self = this;
	  		  if(this.edit !== 'update') // new
	  		  {
	  		    if($.fn.DataTable.isDataTable( this.attr.params ))
	          {
	  		      this.select('params').DataTable().destroy();
	  		      this.select('params').empty();
	          }
	  		    this.select('container-title').text('Create query');
	  		    this.select('qname').removeAttr('readonly').val(this.app + ' ');
	          this.select('clipboard').hide();
	          this.select('button-copy').hide();
	          this.select('button-rename').hide();
	          this.select('button-delete').hide();
	          this.select('qname-box').removeClass('input-group');
	          this.qname = '';
	          this.comments = '';
	          this.edit  = 'new';
	          setTimeout(function(){
	            self.select('qname').focus();
	          }, 100); 
	  		  }
	  		  else
	  		  {
	  		    this.select('params-wrapper').show();
	  		    this.select('container-title').text('Edit query');
	  		  }
	  		};
	  		
	  		this.clearQuery = function(e,d) { // triggered by 'cancel' buttons
	  			if($(e.target).prop('id') == 'query-editor-container')
	  			{
	  			  this.edit = '';
	  				this.query = '';
	  				this.comments = '';
	  				this.query = '';
		  			this.select('qname').val('');
		  			this.select('query-comments').val('');
		  			
		  			$('#query-editor').empty();
	  			}
	  			this.select('alert').remove();
	  		};
	  		
	  		this.saveQuery = function(e,d) {
          var self = this,
              query = this.editor.getDoc().getValue(),
              comments = this.select('query-comments').val(),
              date  = new Date(),
              d = date.getDate() < 10 ? "0"+date.getDate() : date.getDate(),
              m = (date.getMonth() + 1) < 10 ? "0"+(date.getMonth()+1) : (date.getMonth()+1),
              y = date.getFullYear(),
              h = date.getHours() < 10 ? "0"+ date.getHours() : date.getHours(),
              M = date.getMinutes() < 10 ? "0"+ date.getMinutes() : date.getMinutes(),
              s = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds(),
              
              
              // update yada_query set qname=?v, query=?v, modified_by=?v, modified=?d where qname=?v and app=?v
              editData = { QNAME:self.qname,
                               QUERY:query,
                               CREATED_BY: self.loggedUser(),
                               CREATED: y+'-'+m+'-'+d+' '+h+':'+M+':'+s,
                               MODIFIED_BY: self.loggedUser(),
                               MODIFIED: y+'-'+m+'-'+d+' '+h+':'+M+':'+s,
                               APP: self.app,
                               ACCESS_COUNT:0,
                               COMMENTS:comments},

              j = [{qname:'YADA '+self.edit+' query',DATA:[editData]}];
          
          // save parameters
          if(_.filter(this.select('nest').data('defaultParams'),{TARGET:self.qname}).length > 0)
          {
            if(self.edit == 'delete')
              $('.fa-remove').trigger('click');
            else
              $('.fa-save').trigger('click');
          }

          // save changes
          $.ajax({
            type:'POST',
            data:{j:JSON.stringify(j)},
            success: function(data) {
              if(data.RESULTSET === null || data.RESULTSET === undefined)
                $(document).trigger('save-error',{'error':data,'selector':self.attr['container-body']});
              else
              {
                $(document).trigger('save-success',{'qname':self.qname,'selector':self.attr['container-body']});
              }
            },
            error: function(data) {
              $(document).trigger('save-error',{'error':data,'selector':self.attr['container-body']});
            }
            
          });
        };
        
        this.copyQuery = function(e,d) { // triggered by 'copy' and 'rename' buttons
          this.restoreFooter();
          var self = this,
              qname = this.qname, 
              query = this.query,
              action = $(e.target).prop('id').replace('button-','');
          this.select('button-cancel').click();
          this.select('qname-copy').modal('show').data({'qname':qname,'query':query,'action':action});
          
          if(action === 'rename') {
            this.select('qname-copy-title').text('Rename query');
            this.select('qname-copy-incl').parent().hide();
          }
          else {
            this.select('qname-copy-title').text('Copy query');
            this.select('qname-copy-incl').removeAttr('disabled').removeAttr('checked').parent().show();
          }
          
          var jParams = _.filter(this.select('nest').data('defaultParams'),{TARGET:qname});
          if(jParams.length > 0)
          {
            if(action === 'rename')
              self.select('qname-copy-incl').attr('checked','checked');
          }
          else
          {
            self.select('qname-copy-incl').removeAttr('checked').attr('disabled','disabled');
          }
          
          var placeholder = action == 'rename' ? 'RENAME' : 'Copy of';
          self.select('qname-copy-input').val(self.app + ' '+placeholder+' ' + qname.replace(self.app+' ',''));
        };
        
        this.prepareToCopy = function(e,d) { // triggered by 'save' button on copy/rename form
          var self   = this;
          var qname  = this.select('qname-copy-input').val();
          var query  = this.select('qname-copy').data('query');
          var action = this.select('qname-copy').data('action');
          var inclParam = this.select('qname-copy-incl').is(':checked');
          var check  = $.ajax({
            type:'POST',
            data:{
              q:'YADA check uniqueness',
              p:qname
            }
          });
          var resolve = function(data) {
            if(data.RESULTSET.ROWS[0].count == 0)
              self.execCopyQuery(qname,query,action,inclParam);
            else
              $(document).trigger('save-error',{'error':'A query with this name already exists','selector':self.attr['qname-copy-body']});
          };
          var reject  = function(errData) {$(document).trigger('save-error',{'error':errData,'selector':self.attr['qname-copy-body']});};
          check.then(resolve,reject);
        };
	  		
	  		this.execCopyQuery = function(qname,query,action,inclParam) {
	  		  var self = this,
	  		      origQname = this.select('qname-copy').data('qname'),
    	  			date  = new Date(),
    	  			d = date.getDate() < 10 ? "0"+date.getDate() : date.getDate(),
    	  			m = (date.getMonth() + 1) < 10 ? "0"+(date.getMonth()+1) : (date.getMonth()+1),
    	  			y = date.getFullYear(),
    	  			h = date.getHours() < 10 ? "0"+ date.getHours() : date.getHours(),
    	  			M = date.getMinutes() < 10 ? "0"+ date.getMinutes() : date.getMinutes(),
    	  			s = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds(),
    	  			
    	  			
    	  			// update yada_query set qname=?v, query=?v, modified_by=?v, modified=?d where qname=?v and app=?v
    	  			editData = { QNAME: qname,
    													 QUERY:query,
    													 CREATED_BY: self.loggedUser(),
    													 CREATED: y+'-'+m+'-'+d+' '+h+':'+M+':'+s,
    													 MODIFIED_BY: self.loggedUser(),
    													 MODIFIED: y+'-'+m+'-'+d+' '+h+':'+M+':'+s,
    													 APP: self.app,
    													 ACCESS_COUNT:0,
    													 COMMENTS:''},
    
    	  			j = [{qname:'YADA new query',DATA:[editData]}];
    	  			
	  			
    	  	if(inclParam) {
    	  	  var jParams = _.filter(this.select('nest').data('defaultParams'),{TARGET:origQname});
    	  	  jParams = _.map(jParams,function(el){
              el.TARGET = qname;
              return el;
            });
    	  	  j.push({qname:'YADA insert default param',DATA:jParams});
	  			}
 	  			
	  			// save changes
    	  	var submit = $.ajax({
            type:'POST',
            data:{j:JSON.stringify(j)}
    	  	});
    	  	
          var resolve = function(data) {
            self.trigger('save-success',{'qname':self.qname,'selector':self.attr['qname-copy-body']});
              if(action === 'rename')
                self.headlessDelete(origQname);
          };
          var reject = function(data) {
            self.trigger('save-error',{'error':data,'selector':self.attr['qname-copy-body']});
          };
	  			
          submit.then(resolve,reject);
	  		};
	  		
	  		this.headlessDelete = function(qname) {
	  		  var self = this;
	  		  var j = [{'qname':'YADA delete query','DATA':[{'APP':this.app,'QNAME':qname}]}];
	  		  var params = _.filter(this.select('nest').data('defaultParams'),{TARGET:qname});
	  		  if(params.length > 0)
	  		    j.push({'qname':'YADA delete default param','DATA':params});
	  		  var del = $.ajax({
	  		    type:'POST',
	  		    data:{
	  		      j:JSON.stringify(j)
	  		    }
	  		  });
	  		  var resolve = function(data) {
	  		    self.trigger('app-requested',{app:self.app});
	  		  };
	  		  var reject  = function(data) {self.trigger('save-error',{'error':data,'selector':self.attr['qname-copy-body']});}
	  		  del.then(resolve,reject);
	  		};
	  		
	  		this.deleteQuery = function() { // triggered by 'delete' button
	  			if(confirm('Are you sure you want to delete the query "'+this.qname+'"?'))
	  			{
	  				this.edit = 'delete';
	  				this.saveQuery();
	  			}
	  			else
	  			{
	  				return false;
	  			}
	  		};
	  		
	  		this.setQname = function() {
	  			this.qname = this.select('qname').val();
	  		};
	  		
	  		this.error = function(e,d) {
	  		  var msg  = 'There was a problem saving your query.';
	  		  var details = '';
	  			this.edit = '';
	  			
	  			if(d.error.Exception != undefined && d.error.StackTrace != undefined)
	  			{
	  			  details = '<div id="error-details">';
	          if(d.error.Exception != undefined)
	              details += "<br/>Exception:"+d.error.Exception;
	          if(d.error.StackTrace != undefined)
	          {
	              for (var i=0;i<5;i++){
	                details += "<br/>"+d.error.StackTrace[i];
	              }
	              details += "<br/>...";
	          }
	          details += '</div>';
	          msg += ' <a href="#" id="view-error-details" class="alert-link">View Details</a>'
	          
	  			}
	  			else if(typeof d.error == 'string')
	  		  {
              msg += ' '+d.error;
	  		  }
	  			var html  = '<div class="alert alert-danger" role="alert">';
          html += '<strong>Uh Oh!</strong> '+msg+'</div>';
	  			$(html).prependTo(d.selector);
	  			$(details).appendTo('.alert').hide();
	  			this.alertFooter();
	  		};
	  		
	  		this.alertFooter = function() {
	  		  this.select('button').hide();
	  		  this.select('cancel-button').text('Close').show();
	  		};
	  		
	  		this.restoreFooter = function() {
	  		  this.select('cancel-button').text('Cancel');
	  		  this.select('button').show();
	  		};
	  		
	  		this.viewErrorDetails = function(e,d) { 
	  		  this.select('error-details').show();
	  		};
	  		
	  		this.saveSuccess = function(e,d) {
	  			var action = this.edit == 'copy' ? 'copied' : this.edit == 'new' ? 'saved' : this.edit+'d';
	  			this.edit = '';
	  			var msg   = 'Query <span style="font-family:Monaco, monospace"><mark>'+d.qname+'</mark></span> was '+action+' successfully.' 
	  			var html  = '<div class="alert alert-success" role="alert">';
	  		      html += '<strong>Hooray!</strong> '+msg+'</div>';
	  			$(html).prependTo(d.selector);
	  			this.alertFooter();
	  			this.select('query-table').DataTable().ajax.reload();
	  		};
	  		
	  		this.toggleView = function(e,d) {
	  			var table = this.select('query-table').DataTable(); 
	  			table.column(2).visible(!table.column(2).visible());
	  			table.column(3).visible(!table.column(3).visible());
	  		};
	  		
	  		this.backup = function(e,d) {
	  		  var data    = $('#query-table').DataTable().data();
	  		  var len     = data.length;
	  		  var queries = _.values(data).slice(0,len); //TODO might have to transform date 
	  		  var params  = this.select('nest').data('defaultParams');
	  		  var jp      = [{"qname":"YADA new query","DATA":queries}];
	  		  if(params.length > 0)
	  		    jp.push({"qname":"YADA insert default param","DATA":params});
	  		  var json = JSON.stringify(jp);
	  		  var blob = new Blob([json], {type: "application/json"});
	  		  var url  = URL.createObjectURL(blob);

	  		  var a = document.createElement('a');
	  		  a.id          = "backup-link";
	  		  a.download    = "YADA_"+this.app+"_backup.json";
	  		  a.href        = url;
	  		  a.textContent = " ";
	  		  document.getElementsByClassName('nest')[0].appendChild(a);
	  		  var evt = new MouseEvent('click', {
	          view: window,
	          bubbles: true,
	          cancelable: true
	  		  });
	  		  a.dispatchEvent(evt);
	  		};
	  			  		
	  		this.defaultAttrs({
	  		  'params'           :'#default-params',
	  		  'params-wrapper'   :'#default-params_wrapper',
	  		  'nest'             :'.nest',
	  		  'new-query'        :'#new-query',
	  		  'toggle-view'      :'#toggle-view',
	  		  'backup'           :'#backup',
	  		  'button'           :'.btn',
	  		  'has-alert'        :'.has-alert',
	  		  'alert'            :'.alert',
	  		  'cancel-button'    :'.btn-call-to-action',
	  		  'button-save'      :'#query-editor-container .btn-primary',
	  		  'button-delete'    :'#query-editor-container .btn-danger',
	  		  'button-cancel'    :'#query-editor-container .btn-call-to-action',
	  		  'button-copy'      :'#query-editor-container #button-copy',
	  		  'button-rename'    :'#query-editor-container #button-rename',
	  		  'container'        :'#query-editor-container',
	  		  'container-title'  :'#query-editor-container .modal-title',
	  		  'container-body'   :'#query-editor-container .modal-body',
	  		  'query-table'      :'#query-table',
	  		  'query-table-body' :'#query-table tbody',
	  		  'qname'            :'#query-name',
	  		  'qname-box'        :'#query-name-box',
	  		  'query-comments'   :'#query-comments',
	  		  'clipboard'        :'span.glyphicon-copy',
	  		  'qname-copy'       :'#qname-copy',
	  		  'qname-copy-input' :'#uniq-name',
	  		  'qname-copy-incl'  :'#incl-param',
	  		  'qname-copy-save'  :'#qname-copy .btn-primary',
	  		  'qname-copy-cancel':'#qname-copy .btn-call-to-action',
	  		  'qname-copy-title' :'#qname-copy .modal-title',
	  		  'qname-copy-body'  :'#qname-copy .modal-body',
	  		  'view-error-details' :'#view-error-details',
	  		  'error-details'    :'#error-details',
	  		  'clipboard-editor' :'#query-code-copy',
	  		  'code'             :'#query-editor textarea',
	  		  'tooltip'          :'#query-editor-container [data-toggle="tooltip"]'
 	  		});
	  		
	      this.after('initialize', function () {
	        var self = this;
	      	this.enrich();
	      	// event handlers
	      	this.on('app-requested',this.reloadTable);
	      	this.on('hide.bs.modal',this.clearQuery);
	      	this.on('save-error',this.error);
	      	this.on('save-success',this.saveSuccess);
	      	this.on('shown.bs.modal',this.addQuery);
	      	this.on('xhr.dt',this.getDefaultParams);
	      	this.on('click', {
	      	  'backup':this.backup,
	      	  'toggle-view':this.toggleView,
	      	  'query-table-body':this.editQuery,
	      	  'button-save':this.saveQuery,
	      	  'button-delete':this.deleteQuery,
	      	  'button-copy':this.copyQuery,
	      	  'button-rename':this.copyQuery,
	      	  'qname-copy-save':this.prepareToCopy,
	      	  'view-error-details':this.viewErrorDetails
	      	});
	      	this.on('focusout',{
	      	  'qname':this.setQname
	      	});	      	
	      });
	  }
});