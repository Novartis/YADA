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
	'lodash',
	'mixin/withDateUtils',
	'mixin/withConfig',
	'mergely'
  ],
  function (flight,$,datatables,_,withDateUtils,withConfig,mergely) {
  	
	  'use strict';
	  
	  
	  return flight.component(migration,withDateUtils,withConfig);
	  	  
	  function migration() {
	  	this.data          = {}; 
	  	this.sourceQnames  = [];
	  	this.sourceObjects = [];
	  	this.blacklist     = ['YADA apps',
	  	                      'YADA queries',
	  	                      'YADA new query',
	  	                      'YADA delete query',
	  	                      'YADA update query',
	  	                      'YADA insert usage log',
	  	                      'YADA select default params for app',
	  	                      'YADA update default param',
	  	                      'YADA insert default param',
	  	                      'YADA delete default param'];
	  	this.whitelist     = [];
	  	this.deletes       = [];
	  	this.merged        = [];
	  	this.addParams     = [];
	  	this.delParams     = [];
	  	this.updParams     = [];
	  	this.tgtParams     = [];
	  	this.srcParams     = [];
	  	this.target        = '';
	  	
	  	
	  	this.enrich = function() {
	  		var self = this;
	  		document.domain = this.domain();
		  	$('.migration-target-selector').autocomplete({
		  		lookup:this.migrationHosts(),
		  		onSelect: self.handleTargetSelection.bind(self),
		  		width: 190
		  	});
	  	};
	  	
	  	this.handleTargetSelection = function(target) {
	  		this.target = target.data;
	  		this.data = $('#query-table').DataTable().data();
	  		$('#query-table_wrapper').hide();
	  		$('#migration-table_wrapper').show();
	  		$('#toggle-view,#new-query,#backup').addClass('disabled');
		  	this.sourceQnames  = _.pluck(this.data,'QNAME');
		  	this.sourceObjects = _.map(this.data,function(row){ return { QNAME:row.QNAME, QUERY:row.QUERY, MODIFIED:row.MODIFIED, COMMENTS:row.COMMENTS};});
		  	this.whitelist     = _.difference(this.sourceQnames,this.blacklist);//['YADA default'];//
		  	this.trigger('close-selector',{target:target.data});
	  	};
	  	
	  	
	  	this.getTargetQueries = function(target) {
	  		var self = this;
	  		
	  		var targetQueries = $.ajax({
	  			url:'http://'+target,
	  			type:'POST',
	  			xhrFields: {
	  			  withCredentials: true
	  			},
	  			data:{
	  				q:'YADA queries',
	  				p:$('#app-selection h1').text(),
	  				pz:-1,
	  				c:false
	  			}
	  		});
	  		
	  		var targetParams = $.ajax({
	  			url:'http://'+target,
	  			type:'POST',
	  			xhrFields: {
	  			  withCredentials: true
	  			},
	  			data:{
	  				q:'YADA select default params',
	  				p:self.whitelist.join(","),
	  				pz:-1,
	  				c:false
	  			}
	  		});
	  		
	  		$.when(
	  				targetQueries,
	  				targetParams
		  	).then(function(targetQueries, targetParams) {
		  		self.tgtParams = targetParams[0].RESULTSET.ROWS;
		  		self.srcParams = _.map($('.nest').data('defaultParams'),function(param) { return param; });
		  		self.mergeDefaultParams(self.srcParams,self.tgtParams);
		  		self.mergeQuerySets(targetQueries[0].RESULTSET.ROWS);
					setTimeout(function() {
						self.renderMigTable();
					},1000);
		  	});
	  	};
	  	
	  	this.mergeDefaultParams = function(a,b) {
	  		var self = this;
	  		
	  	  function flatten(array) {
	  	  	return _.map(array,function(o){ 
	  	  	  return [o.TARGET,o.NAME,o.RULE,o.VALUE].join('||'); 
	  	  	});
	  	  }
	  	  
	  	  function splitSpliceJoin(flat) {
	  	  	return flat.split('||').splice(0,2).join('||');
	  	  }
	  	  
				var a_flat = flatten(a),
	  				b_flat = flatten(b),
	  				a_flat_only = _.filter(a_flat,function(a){ return _.indexOf(b_flat,a) == -1; }), // adds and updates
						b_flat_only = _.filter(b_flat,function(b){ return _.indexOf(a_flat,b) == -1; }), // dels and updates
	  				flatUpdParams = [];
				_.each([self.addParams,self.delParams,self.updParams],function(array) { 
				  array.splice(0,array.length); 
				  }); // clear arrays
				// get updates
				
				for(var i=0;i<a_flat_only.length;i++)
				{
					for(var j=0;j<b_flat_only.length;j++)
					{
						if(_.isEqual(splitSpliceJoin(a_flat_only[i]), splitSpliceJoin(b_flat_only[j])))
						{
							flatUpdParams.push(a_flat_only[i]);
							a_flat_only = _.without(a_flat_only,a_flat_only[i]);
							b_flat_only = _.without(b_flat_only,b_flat_only[j]);
						}
					}
				}

				_.each([{flat:a_flat_only, full:self.addParams},{flat:b_flat_only, full:self.delParams},{flat:flatUpdParams, full:self.updParams}],
						function(obj) { _.each(obj.flat,function(o) {
							obj.full.push(o.split('||'));
						});
				});
	  	};
	  	
	  	this.getStatus = function(row) {
	  		if(row.TGT_QUERY !== row.SRC_QUERY)
	  		{
	  		  if(row.TGT_MOD === '' || (new Date(this.getFormattedDate(row.SRC_MOD)) > new Date(this.getFormattedDate(row.TGT_MOD))))
		  			return 1;
		  		else if(row.SRC_MOD === '' || new Date(this.getFormattedDate(row.SRC_MOD)) < new Date(this.getFormattedDate(row.TGT_MOD)))
		  			return 2;
	  		}
	  		else
	  			return 0;
	  	};
	  	
	  	this.mergeQuerySets = function(targetData) {
	  		var self = this;
	  		var $compare = $('#compare');
	  		$('[id^="compare-"]').remove();
	  		self.merged = [];
	  		
	  		self.deletes = _.difference(_.pluck(targetData,'QNAME'),self.whitelist,self.blacklist);
	  		
	  		_.each(self.deletes,function(qname){
	  			var tgt = _.filter(targetData,{QNAME:qname})[0];
	  			var row = {
	  				SRC_QNAME:qname,
	  				SRC_QUERY:'',
	  				SRC_MOD:'',
	  				TGT_QUERY:tgt.QUERY,
	  				TGT_MOD:tgt.MODIFIED,
	  				STATUS:4
	  			};
	  			setCodeMirror(row);
	  			self.merged.push(row);
	  		},self);
	  		
	  		_.each(self.whitelist,processRow,self); // inserts, updates
	  		
	  		function setCodeMirror(row) {
	  			var qname = row.SRC_QNAME;
	  			var $comp  = $('<div id="compare-'+qname.replace(/\s/g,'-')+'"/>');
	  			$compare.append($comp);
	  			$comp.mergely({
						cmsettings: { readOnly: 'nocursor', 
												  viewportMargin: Infinity, 
												  lineNumbers: true,
												  lineWrapping: true},
						autoresize: true,
						editor_height: 'auto',
						editor_width:'450px',
						fgcolor: {a:'#cdf0c0',c:'#fefec8',d:'#edc0c0'},
						sidebar: false,
						fadein: false,
						lhs: function(setValue) {
							if(_.has(row,'TGT_QUERY') && row.TGT_QUERY != null)
								setValue(row.TGT_QUERY);
							else
								setValue('');
						},
						rhs: function(setValue) {
							if(_.has(row,'SRC_QUERY') && row.SRC_QUERY != null)
								setValue(row.SRC_QUERY);
							else
								setValue('');
						},
						
					});
		  		$comp.show().css('visibility','hidden');
	  			$comp.data('qname',qname);
  				row.QUERY = '';
  			};
	  		
	  		function processRow(qname) {	
	  			var self = this;
	  			var row = {};
	  			
	  			var flatParams = _.flatten(_.union(self.addParams,self.delParams,self.updParams));
	  		  // if the data requires transfer need another data point to eval default params (add,del,upd)
	  			
	  			var srcObj = _.filter(self.sourceObjects,'QNAME',qname);
	  			var tgtObj = _.filter(targetData,'QNAME',qname);
	  			
	  			// set the attributes
	  			row.SRC_QNAME    = qname;
	  			row.SRC_QUERY    = _.pluck(srcObj,'QUERY')[0];
	  			row.SRC_COMMENTS = _.pluck(srcObj,'COMMENTS')[0];
	  			row.SRC_MOD      = _.pluck(srcObj,'MODIFIED')[0];
	  			row.TGT_QUERY    = _.pluck(tgtObj,'QUERY')[0];
	  			if(tgtObj !== null && tgtObj !== undefined && tgtObj.length > 0)
	  			{
		  			row.TGT_MOD   = _.pluck(tgtObj,'MODIFIED')[0];
		  			var targetComments = _.pluck(tgtObj,'COMMENTS')[0];
		  			if(targetComments !== undefined && targetComments.length > 0)
		  			  row.TGT_COMMENTS = "\n\n" + targetComments; 
	  			}
	  			else
	  			{
	  				row.TGT_MOD   = '';
	  				row.TGT_COMMENTS = '';
	  			}
	  			row.STATUS    = _.contains(flatParams,qname) ? 3 : self.getStatus(row);
	  			if(row.STATUS > 0)
	  			{
		  			setCodeMirror(row);
		  			self.merged.push(row);	
	  			}
	  		}
	  	};
	  			  	
	  	this.renderMigTable = function() {
	  		var self = this;
	  		var $table = $('#migration-table');
	  		if($.fn.DataTable.isDataTable( '#migration-table' ))
	  		{
	  			var table = $table.DataTable(); 
	  			table.clear();
	  			table.rows.add(self.merged);
	  			table.draw();
	  		}
	  		else
	  		{
			  	$table.dataTable({
			  		
		  			data:self.merged,
		  			"lengthMenu": [[ 5, 10, 25, 50, 75, -1 ],[5, 10, 25, 50, 75, 'All']],
		  			dom:'<"top"f>t<"bottom-l"i><"bottom-c"p><"bottom-r"l>',
		  			language: {
		  				search: 'Filter on qname or query:',
		  				searchPlaceholder: 'Filter...',
		  				emptyTable: 'There are no modified queries to migrate.'
		  			},
		  			columnDefs:[
		  			{
		  				targets:0,
		  				render:function(data,type,row,meta) {
		  					var cb  = '<input type="checkbox"';
		  							// 1 == queries are different and src is newer, 
		  							// 2 == queries are different and target is newer (no check,)
		  							// 3 == params are different,
		  							// 4 == queries to delete on target
		  					    cb += data == 1 || data > 2 ? ' checked' : '';
		  				      cb +='>';
		  					return cb;
		  				}
		  			},
		  		  {
		  		  	targets:3, // query cols
		  		  	render:function(data,type,row,meta) {
		  		  		var qname = row.SRC_QNAME.replace(/\s/g,'-');
		  		  		var $comp = $('#compare-'+qname);
		  		  		if($comp.length > 0)
		  		  			return '<table id="mig-data-'+qname.replace(/\s/g,"-")+'" class="migration-data"><tr><td class="mig-mergely" colspan="7">'+$comp.html()+'</td></tr></table>';
		  		  		else
		  		  			return data;
		  		  	}
		  		  },
		  		  {
		  				targets:[2,4], // date columns
		  				type:"date",
		  				className: 'dt-body-nowrap dt-body-center',
		  				render:function(data,type,row,meta) {
		  					if(data == "" || data == undefined)
		  						return '';
		  					return self.getFormattedDate(data,'oracle');
		  				}
		  			},
		  			],
		  			columns:[
		  			  {data:"STATUS",title:'Include',name:"inc"},
		  				{data:"SRC_QNAME",title:"Qname",searchable:true},
		  				{data:"TGT_MOD",title:"Target Mod",searchable:false},	  
		  				{data:"QUERY",title:"Compare Queries <span style='font-weight:normal'>(Left Side = &quot;OLD query&quot; on target system; Right Side = &quot;NEW query&quot; on current or source system)</span>",searchable:false,width:'60%'},
		  				{data:"SRC_MOD",title:"Source Mod",searchable:false}
		  			]
		  		});
	  		}
	  		$('#migration-table caption').show();
	  		if($('#migration-submit').length == 0)
	  		{
		  		var submit = $('<div id="migration-submit"><button>Migrate</button></div>');
		  		$('#migration-table_wrapper').append(submit);
	  		}
	  	};
	  	
	  	this.handleRowClick = function(e,d) {
	  		var $target = $(e.target).closest('tr');
	  		var $cb     = $target.find('input[type="checkbox"]');
	  		$cb.prop('checked',!$cb.prop('checked'));
	  	};
    
	  	this.closeDialog = function(e,d) {
	  		$('#migration-target-selector').modal('hide');
	  		$('.migration-target-selector').val('');
		  	this.getTargetQueries(d.target);
	  	};
	  	
	  	this.migrate = function(e,d) {
	  		var self = this,
	  		    j = [],
	  		    jn = [],
	  		    queries = $('#migration-table').DataTable().rows(':has("input:checked")').data(),
	  		    app     = $('#app-selection h1').text(),
	  		    params  = [];
	  		
	  		
	  		// updates
	  		var updates = _.filter(queries,function(query) { 
	  			return query.TGT_QUERY !== undefined 
	  					&& query.TGT_QUERY !== '' 
	  					&& query.SRC_QUERY !== undefined 
	  					&& query.SRC_QUERY !== ''
	  					&& query.SRC_QUERY !== query.TGT_QUERY; });
	  		if(updates.length > 0)
	  		{
	  			j.push({qname:'YADA update query',DATA:[]});
	  			_.each(updates,function(query){
		  			var o = {QNAME:query.SRC_QNAME,
		  							 QUERY:query.SRC_QUERY,
		  							 MODIFIED_BY:self.loggedUser(),
		  							 MODIFIED:self.getFormattedDate(new Date()),
		  							 APP:app,
		  							 COMMENTS: query.SRC_COMMENTS + query.TGT_COMMENTS};
		  			j[j.length-1].DATA.push(o);
		  		});	  			
	  		}
	  		// inserts
	  		var inserts = _.filter(queries,function(query) { 
	  		  return query.TGT_QUERY === undefined || query.TGT_QUERY === ''; 
	  		});
	  		if(inserts.length > 0)
	  		{
		  		j.push({qname:'YADA new query',DATA:[]});
		  		_.each(inserts,function(query){
		  			var o = {QNAME:query.SRC_QNAME,
		  							 QUERY:query.SRC_QUERY,
		  							 MODIFIED_BY:self.loggedUser(),
		  							 CREATED_BY:self.loggedUser(),
		  							 APP:app,
		  							 COMMENTS: query.SRC_COMMENTS,
		  							 CREATED:self.getFormattedDate(new Date()),
		  							 MODIFIED:self.getFormattedDate(new Date()),
		  							 ACCESS_COUNT:0};
		  			j[j.length-1].DATA.push(o);
		  		});	  			
	  		}
	  		
	  		// deletes derived from selected 'queries' list
	  		var deletes = _.filter(queries,function(query) {
          return (query.SRC_QUERY === undefined || query.SRC_QUERY === '') && query.TGT_QUERY !== undefined && query.TGT_QUERY !== '';
        });
	  		if(deletes.length > 0) 
	  		{
		  		j.push({qname:'YADA delete query',DATA:[]});
		  		_.each(deletes,function(query){
		  			var o = {QNAME: query.SRC_QNAME, APP: app};
		  			j[j.length-1].DATA.push(o);
		  		});	  			
	  		}
	  		
	  		if(self.updParams.length > 0)
	  		{
	  		  var data = [];
		  		_.each(self.updParams,function(param) {
		  		  _.each(queries,function(query) {
		  		    if(_.includes(query,param[0]))
		  		    {
		  		      var o = {TARGET:param[0],NAME:param[1],VALUE:param[3],RULE:param[2]};
		  		      data.push(o);
		  		    }
		  		  });
		  		});
		  		if(data.length > 0)
		  		{
		  		  j.push({qname:'YADA update default param',DATA:data});
		  		}
	  		}
	  		
	  		if(self.addParams.length > 0)
        {
          var data = [];
          _.each(self.addParams,function(param) {
            _.each(queries,function(query) {
              if(_.includes(query,param[0]))
              {
                var o = {TARGET:param[0],NAME:param[1],VALUE:param[3],RULE:param[2]};
                data.push(o);
              }
            });
          });
          if(data.length > 0)
          {
            j.push({qname:'YADA insert default param',DATA:data});
          }
        }
	  		
	  		if(self.delParams.length > 0)
        {
          var data = [];
          _.each(self.delParams,function(param) {
            _.each(queries,function(query) {
              if(_.includes(query,param[0]))
              {
                var o = {TARGET:param[0],NAME:param[1],VALUE:param[3],RULE:param[2]};
                data.push(o);
              }
            });
          });
          if(data.length > 0)
          {
            j.push({qname:'YADA delete default param',DATA:data});
          }
        }
	  		
	  		
	  		// update yada_query set qname=?v, query=?v, modified_by=?v, modified=?d where qname=?v and app=?v
	  		
	  		$.ajax({
	  			url:'http://'+self.target,
	  			type:'POST',
	  			crossDomain:true,
	  			xhrFields: {
	  			  withCredentials: true
	  			},
	  			data:{
	  				j:JSON.stringify(j)
	  			},
	  			success: function(data) {
	  				self.handleTargetSelection({data:self.target});
	  			}
	  		});
	  	};
	  	
      this.destroy = function(e,d) {
        $('nav.main-menu li').addClass('disabled');
        $('#new-query,#migration').removeAttr('data-toggle');
        $('#new-query,#migration').removeAttr('data-target');
        if ($.fn.DataTable.isDataTable(this.attr['mig-table']))
          this.select('mig-table').DataTable().destroy();
        this.teardown();
      }
	  	
	  	this.displayParamsForMigration = function(e,d) {
	  		var self = this;
	  		if($.fn.DataTable.isDataTable( '#migration-table' ))
	  		{
		  		var table = $('#migration-table').DataTable();
		  		
		  		function addParamHeaders(elem) {
		  			var cols = '<td class="mig-param-name">Name</td><td class="mig-param-val">Value</td><td class="mig-param-rule">Rule</td>';
		  			var hdr  = '<tr class="mig-param-cap"><td colspan="7">Default Params</td></tr>';
		  			    hdr += '<tr class="mig-param-hdr">';
		  			    hdr += cols + '<td class="mig-param-gap"></td>' + cols;
		  			    hdr += '</tr>';
		  			elem.append(hdr);
		  		}
		  		
		  		function addParamRow(elem,values) {
		  			function rule(val) {
		  				return val === "1" ? "Non-overridable" : val === "0" ? "Overridable" : "";
		  			}
		  			var colsL = '<td class="mig-param-name">'+values[0]+'</td><td class="mig-param-val">'+values[1]+'</td><td class="mig-param-rule">'+rule(values[2])+'</td>';
		  			var colsR = '<td class="mig-param-name">'+values[3]+'</td><td class="mig-param-val">'+values[4]+'</td><td class="mig-param-rule">'+rule(values[5])+'</td>';
		  			elem.append('<tr class="mig-param-val">'+colsL+'<td class="mig-param-gap"></td>'+colsR+'</tr>');
		  		}
		  		
		  		_.each(table.column(1).data(),function(qname) {
		  			var addParams   = _.filter(self.addParams,function(arr){ return _.indexOf(arr,qname) > -1; }),
		  					delParams   = _.filter(self.delParams,function(arr){ return _.indexOf(arr,qname) > -1; }),
		  					updParams   = _.filter(self.updParams,function(arr){ return _.indexOf(arr,qname) > -1; });
		  			
		  			if(addParams.length > 0 || delParams.length > 0 || updParams.length > 0)
		  			{
			  			var $table = $('#mig-data-'+qname.replace(/\s/g,'-'));
			  					
			  			if($('#mig-data-'+qname.replace(/\s/g,'-')+' tr').length == 1)
			  				addParamHeaders($table);
			  			// param array structure: [o.TARGET,o.NAME,o.RULE,o.VALUE]
			  			_.each(updParams,function(param){ 
		  						var tgt = _.filter(self.tgtParams,{TARGET:qname,NAME:param[1]})[0];
		  						addParamRow($table,[tgt.NAME,tgt.VALUE,tgt.RULE,param[1],param[3],param[2]]);
			  			});
			  			_.each(addParams,function(param){ 
			  				  addParamRow($table,['','','',param[1],param[3],param[2]]);
			  			});
			  			_.each(delParams,function(param){ 
			  				 	addParamRow($table,[param[1],param[3],param[2],'','','']);
			  			});
			  		}
		  		});	  			
	  		}
	  	};
	  	
	  	this.defaultAttrs({
	  		'migrationButton':'#migration-submit>button',
	  		'migrationTableBody':'#migration-table tbody',
	  		'mig-table':'#migration-table'
	  	});
	  	
	  	this.after('initialize', function () {
	  		this.enrich();
	  		this.on('close-selector',this.closeDialog);
	  		this.on('click',{
	  			migrationTableBody:this.handleRowClick,
	  			migrationButton:this.migrate
	  		});
	  		this.on('draw.dt',this.displayParamsForMigration);
	  		this.on('destroy.ya.migration-table', this.destroy);
      });
	  }
});