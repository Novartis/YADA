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
define(
  [
	'flight',
	'jquery',
	'datatables',
	'component/query-params',
	'mixin/withDateUtils',
	'mixin/withConfig',
	'codemirror',
	'codemirror/mode/sql/sql',
	'codemirror/mode/javascript/javascript.min',
	'codemirror/mode/xml/xml.min',
	],
  function (flight,$,dataTables,Params,withDateUtils,withConfig,CodeMirror) {
	  
	  'use strict';
	  return flight.component(queryTable,withDateUtils,withConfig);
	  
	  //TODO yada mode for codemirror (yada-sql, yada-xml, yada-json, yada-soap (json/xml)
	  
	  function queryTable() {
	  	
	  		this.app   = 'YADA';
	  		this.qname = '';
	  		this.edit  = 'update';
	  		this.editor = {};
	  		this.query = '';
	  		
		  	this.enrich = function() {
		  		var self = this;
		  		$('#toggle-view,#new-query').removeClass('disabled');
		  		$('#query-table').dataTable({
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
		  				search: 'Filter on qname or query:',
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
		  				targets:[5,7], // date columns
		  				type:"date",
		  				className: 'dt-body-nowrap dt-body-center',
		  				render:function(data,type,row,meta) {
		  					if(data == "")
		  						return data;
		  					return self.getOracleDateStr(data);
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
		  				{data:"DEFAULT_PARAMS",title:'Default Parameters',type:'num',searchable:false,className:'dt-body-center'},
		  				{data:"LAST_ACCESS",title:"Last Accessed",searchable:false},
		  				{data:"ACCESS_COUNT",title:"Access Count",searchable:false,type:'num',className:'dt-body-right'},
		  				{data:"MODIFIED",title:"Last Modified",searchable:false},
		  				{data:"MODIFIED_BY",title:"Modified By",searchable:false},
		  				
		  			]
		  		});
	  		};
		  	
		  	this.getDefaultParams = function(e,d) {
		  		var sourceParams = $.ajax({
		  			type:'POST',
		  			data:{
		  				q:'YADA select default params for app',
		  				p:this.app,
		  				pz:-1,
		  				c:false
		  			} 
		  		});
		  		sourceParams.then(
		  				function(data) {
		  					
			  				var params = [];
			  					_.each(data.RESULTSET.ROWS, function(param) {
			  						params.push(param);
			  				});
			  				$.removeData('.nest','defaultParams');
			  				$('.nest').data('defaultParams',params);
			  			}
		  		);
		  	};
	  		
	  		this.reloadTable = function(e,d) {
	  			$('#migration-table_wrapper').hide();
	  			$('#query-table_wrapper').show();
	  			this.app = d.app;
	  			var table = $('#query-table').DataTable(); 
	  			table.ajax.reload();
	  			table.draw();
	  			$(document).trigger('close-selector',{});
	  		};
	  		
	  		this.handleCellClick = function(e,d) {
	  			
	  			this.edit   = 'update';
	  			var  self   = this,
	  			     target = $(e.target),
	  			     table  = $('#query-table').DataTable();
	  			
	  			if(e.target.nodeName == 'PRE' || e.target.nodeName == 'SPAN')
	  			{
	  				target = target.parent();
	  			}
	  			
	  		  var cell    = table.cell(table.row(target.closest('tr')).index(), 2);
		  		this.query  = cell.data();
		  		this.qname  = table.cell(cell.index().row,1).data();
		  		
		  		$('#query-name input').val(this.qname).attr('disabled','disabled');
		  	  $('#query-editor-container').modal('show');
		  			var $tr = target.closest('tr');
	  				this.trigger('#default-params','show-params',{
	  					tr    : $tr,
	  					qname : self.qname,
	  					params: _.filter($('.nest').data('defaultParams'),{TARGET:self.qname})
	  					
	  				});
	  		};
	  		
	  		
	  		this.renderEditor = function(e,d) {
	  			var self = this;
	  			if($(e.target).prop('id') == 'query-editor-container')
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
	  			}
	  		};
	  		
	  		this.addQuery = function(e) {
	  			$('#query-name input').removeAttr('disabled');
	  			this.qname = '';
	  			this.edit  = 'new';
	  		};
	  		
	  		this.clearQuery = function(e,d) {
	  			if($(e.target).prop('id') == 'query-editor-container')
	  			{
	  				this.query = '';
		  			$('#query-name input').val('');
		  			$('#query-editor').empty();
	  			}
	  		};
	  		
	  		this.saveQuery = function(e,d) {
	  			var self = this,
	  			query = this.editor.getDoc().getValue(),
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
													 MODIFIED_BY: self.loggedUser(),
													 MODIFIED: y+'-'+m+'-'+d+' '+h+':'+M+':'+s,
													 APP: self.app},

	  			j = [{qname:'YADA '+self.edit+' query',DATA:[editData]}];
	  			
	  			// save parameters
	  			if(_.filter($('.nest').data('defaultParams'),{TARGET:self.qname}).length > 0)
	  			{
		  			if(self.edit == 'delete')
		  				$('.fa-remove').trigger('click');
		  			else
		  				$('.fa-save').trigger('click');
	  			}

	  			// save changes
	  			$.ajax({
	  				//url: '/yada.jsp',
	  				type:'POST',
	  				data:{j:JSON.stringify(j)},
	  				success: function(data) {
	  					if(data.RESULTSET === null || data.RESULTSET === undefined)
	  						$(document).trigger('save-error',{error:data});
	  					else
	  					{
	  						$(document).trigger('save-success',{qname:self.qname});
	  						self.trigger('reload-params',{});
	  					}
	  				},
	  				error: function(data) {
	  					$(document).trigger('save-error',{error:data});
	  				}
	  				
	  			});
	  		};
	  		
	  		this.deleteQuery = function() {
	  			
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
	  			this.qname = $('#query-name input').val();
	  		};
	  		
	  		this.error = function(e,d) {
	  			$('#query-editor-container .btn-default').click();
	  			this.edit = '';
	  			var error  = 'There was a problem saving your query:';
	  			    error += "\nException:"+d.error.Exception;
	  			    for (var i=0;i<5;i++){
	  			    	error += "\n"+d.error.StackTrace[i];
	  			    }
	  			    error += "\n...";
	  			alert(error);
	  		};
	  		
	  		this.saveSuccess = function(e,d) {
	  			var action = this.edit == 'delete' ? 'deleted' : 'saved';
	  			this.edit = '';
	  			$('#query-editor-container .btn-default').click();
	  			alert('Query "'+d.qname+'" '+action+' successfully.');
	  			$('#query-table').DataTable().ajax.reload();
	  		};
	  		
	  		this.toggleView = function(e,d) {
	  			var table = $('#query-table').DataTable(); 
	  			table.column(2).visible(!table.column(2).visible());
	  			table.column(3).visible(!table.column(3).visible());
	  		};
	  		
	      this.after('initialize', function () {
	      	this.enrich();
	      	// event handlers
	      	this.on('app-requested',this.reloadTable);
	      	this.on('hide.bs.modal',this.clearQuery);
	      	this.on('save-error',this.error);
	      	this.on('save-success',this.saveSuccess);
	      	this.on('shown.bs.modal',this.renderEditor);
	      	this.on('#new-query','click',this.addQuery);
	      	this.on('#toggle-view','click',this.toggleView);
	      	this.on('.btn-primary','click',this.saveQuery);
	      	this.on('.btn-danger','click',this.deleteQuery);
	      	this.on('#query-name input','blur',this.setQname);
	      	this.on('#query-table tbody','click',this.handleCellClick);
	      	this.on('xhr.dt',this.getDefaultParams);
	      	this.on('reload-params',this.getDefaultParams);
	      });
	  }
});