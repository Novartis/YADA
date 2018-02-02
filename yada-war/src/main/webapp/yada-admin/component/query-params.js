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
	'autocomplete'
  ],
  function (flight,$,autocomplete ) {

	  'use strict';
	  return flight.component(queryParamList);

	  //TODO parameter number value validation (is a number, is an integer)
	  //TODO parameter styling



	  function queryParamList() {

	  	// parameter dropdown
	  	this.yadaParams = [
		  	{value:'a',data:{longname:"a (args)",tooltip:"A comma-separated list of arguments expected by the plugin. For script plugins, the script name should be first."}},
		  	{value:'b',data:{longname:"b (bypassargs)",tooltip:"A comma-separated list of arguments expected by the bypass plugin. For script plugins, the script name should be first."}},
		  	{value:'c',data:{longname:"c (count)",tooltip:'"true" (default) for YADA to execute a second "count" query returning the total number records in the result set, rather than just the in the first page.  "false" to suppress this second query.'}},
		  	{value:'ck',data:{longname:"ck (cookies)",tooltip:'A comma-separated list of cookie names to pass to a YADA REST query'}},
		  	{value:'cq',data:{longname:"cq (commitQuery)",tooltip:'"true" to execute a commit after each query, "false" (default) will execute commits after all queries in the request have been executed.'}},
		  	{value:'co',data:{longname:"co (countOnly)",tooltip:'When "true", YADA will execute the secondary "count" query only, instead of retrieving the data.'}},
		  	{value:'cv',data:{longname:"cv (converter)",tooltip:'The classname or FQCN of a com...yada.format.Converter implementation.'}},
		  	//{value:'colhead',data:{longname:"colhead",tooltip:''}},
		  	//{value:'compact',data:{longname:"compact",tooltip:''}},
		  	{value:'d',data:{longname:"d (delimiter)",tooltip:'The column delimiting character or string'}},
		  	{value:'e',data:{longname:"e (export)",tooltip:'When "true", the result set is written to disk and YADA returns the path to the new file'}},
		  	{value:'el',data:{longname:"el (exportlimit)",tooltip:'The maximum number of records to include in an export. This is useful for restricting web clients from downloading very large datasets in entirety'}},
		  	{value:'fi',data:{longname:"fi (filters)",tooltip:'Documentation pending'}},
		  	{value:'f',data:{longname:"f (format)",tooltip:'The format of the data in the response: JSON (default), csv, tsv, pipe, xml, custom (use delimiter params)'}},
		  	//{value:'j',data:{longname:"j (JSONParams)",tooltip:''}},
		  	//{value:'labels',data:{longname:"labels",tooltip:''}},
		  	//{value:'mail',data:{longname:"mail"}},
		  	{value:'h',data:{longname:"h (harmonyMap)",tooltip:'A JSON string pairing source-result field names or paths to response field names or paths.'}},
        {value:'H',data:{longname:"H (httpHeaders)",tooltip:'A comma-separated list of header names OR a JSON String with header name keys and String or boolean values.'}},
		  	//{value:'o',data:{longname:"o (overArgs)",tooltip:''}},
		  	{value:'pg',data:{longname:"pg/ps (page/pagestart)",tooltip:'When pagination is in use, use this parameter to set the default first page, if > 1 (default)'}},
		  	{value:'path',data:{longname:"path",tooltip:''}},
		  	{value:'pz',data:{longname:"pz (pagesize)",tooltip:'Integer denoting the number of records to return per page. Use "-1" to return all records.  The default is 20.'}},
		  	//{value:'p',data:{longname:"p (params)",tooltip:''}},
		  	//{value:'ps',data:{longname:"ps (paramset)",tooltip:''}},
		  	{value:'pl',data:{longname:"pl (plugin)",tooltip:'The classname FQDN of the plugin class in the com...yada.plugin package'}},
		  	{value:'pa',data:{longname:"pa (postargs)",tooltip:'A comma-separated list of arguments expected by the postprocessor plugin. For script plugins, the script name should be first'}},
		  	{value:'pr',data:{longname:"pr (preargs)",tooltip:'A comma-separated list of arguments expected by the preprocessor plugin. For script plugins, the script name should be first'}},
		  	{value:'py',data:{longname:"py (pretty)",tooltip:'Format a JSON result with linefeeds and indentation'}},
		  	//{value:'pc',data:{longname:"pc (protocol)",tooltip:''}},
		  	{value:'px',data:{longname:"px (proxy)",tooltip:'The hostname and address of the proxy server.  This may be required for external REST queries, for example.'}},
		  	//{value:'q',data:{longname:"q (qname)",tooltip:''}},
		  	{value:'rd',data:{longname:"rd (rowDelimiter)",tooltip:'The character or string to delimit rows of tabular data returned in a delimited response'}},
		  	{value:'r',data:{longname:"r (response)",tooltip:'The classname or FQDN of the com...yada.format.Response implementation'}},
		  	{value:'s',data:{longname:"s (sortkey)",tooltip:'The column name on which to sort results before the result set is returned from the server. Should support pagination.'}},
		  	{value:'so',data:{longname:"so (sortorder)",tooltip:'asc (default) or desc, referring to the sortkey'}},
		  	{value:'u',data:{longname:"u (user)",tooltip:'The user id, useful when such information is not trasmitted automatically in the request.'}},
		  	{value:'vl',data:{longname:"vl (viewlimit)",tooltip:'Integer defining the maximum number of results to return even in pagination use-cases. The view limit effectively caps the secondary "count" query. This is useful when count queries run on very large datasets, causing latency.'}}
	  	];

	  	this.paramControls = {
  			'a':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Comma-separated list...');
  			}, // placeholders
  	  	'b':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Comma-separated list...');
  			}, // placeholders
  	  	'c':function(node,context) {
  	  		context.setBoolean(context.getParamValueNode(node),"false");
  	  	}, // bool
  	  	'cq':function(node,context) {
  	  		context.setBoolean(context.getParamValueNode(node),"true");
  	  	}, // bool
  	  	'co':function(node,context) {
  	  		context.setBoolean(context.getParamValueNode(node),"true");
  	  	}, // bool
  	  	'cv':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Class name or FQCN...');
  			}, // placeholders
  	  	//'colhead':function() { }, // tf
  	  	//'compact':function() { }, // tf
  	  	'd':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Character or string...');
  			}, // placeholders
  	  	'e':function(node,context) {
  	  		context.setBoolean(context.getParamValueNode(node),"true");
  	  	}, // bool
  	  	'el':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Number...');
  			}, // validate number
  	  	'fi':function() { }, // textarea
  	  	'f':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'csv,tsv,xml,pipe,etc');
  			}, // placeholders
  	  	//'j':function() { },
  	  	//'labels':function() { }, // ??
  	  	//'mail':function() { }, // ??
  	  	//'h':function() { },
  	  	//'o':function() { },
  	  	'pg':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Number...');
  			}, // validate number
  	  	//'path':function() { }, // ??
  	  	'pz':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Number...');
  			}, // validate number
  	  	//'p':function() { },
  	  	//'ps':function() { },
  	  	'pl':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Script name, Class name, or FQCN...');
  			}, // placeholders
  	  	'pa':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Comma-separated list...');
  			}, // placeholders
  	  	'pr':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Comma-separated list...');
  			}, // placeholders
  	  	'py':function(node,context) {
  	  		context.setBoolean(context.getParamValueNode(node),"true");
  	  	}, // bool
  	  	//'pc':function() { },
  	  	'px':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'hostname:port...');
  			}, // placeholders
  	  	//'q':function() { },
  	  	'rd':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Character or string...');
  			}, // placeholders
  	  	'r':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Class name or FQCN...');
  			}, // placeholders
  	  	's':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Column name...');
  			}, // placeholders
  	  	'so':function(node,context) {
  	  		context.setSortOrder(context.getParamValueNode(node),"desc");
  	  	}, // asc/desc
  	  	'u':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'User id...');
  			}, // placeholders
  	  	'vl':function(node,context) {
  				context.setPlaceholder(
  						context.getParamValueNode(node),
  						'Number...');
  			} // validate number
	  	};

	  	this.getParamValueNode = function(node) {
	  		return $(node).closest('tr').find('input[id^="value-"]');
	  	};

	  	this.setPlaceholder = function(elem,value) {
	  		elem.attr('placeholder',value);
	  	};

	  	this.setBoolean = function(elem,defalt) {
	  		var val = elem.val();
	  		if(val == "") val = defalt;
	  		var html  = '<input type="radio" name="'+elem.attr('id')+'" value="true"'+(val == 'true' ? ' checked="true"' : '')+'/>True';
	  		    html += '<input type="radio" name="'+elem.attr('id')+'" value="false"'+(val == 'false' ? ' checked="true"' : '')+'/>False';
	  		elem.parent().append(html);
	  		elem.remove();
	  	};

	  	this.setSortOrder = function(elem,defalt) {
	  		var val = elem.val();
	  		if(val == "") val = defalt;
	  		var html  = '<input type="radio" name="'+elem.attr('id')+'" value="asc"'+(val == 'asc' ? ' checked="true"' : '')+'/>asc';
	  		    html += '<input type="radio" name="'+elem.attr('id')+'" value="desc"'+(val == 'desc' ? ' checked="true"' : '')+'/>desc';
	  		elem.parent().append(html);
	  		elem.remove();
	  	};

	  	this.showParams = function(e,d) {
	  		var self = this;
	  		var $table = $('#default-params');
	  		var params = [{TARGET:$('#query-name').val(),NAME:'',VALUE:'',RULE:1,ID:0}];
  			if(d.params != undefined && d.params.length > 0)
  			{
  				params = d.params;
  			}
	  		if($.fn.DataTable.isDataTable( '#default-params' ))
	  		{
	  			var table = $table.DataTable();
	  			table.clear();
	  			table.rows.add(params);
	  			table.draw();
	  		}
	  		else
	  		{
			  	$table.dataTable({
		  			data:params,
		  			type:"POST",
		  			//"lengthMenu": [[ 5, 10, -1 ],[5, 10, 'All']],
		  			//dom:'t<"bottom-l"i><"bottom-c"p><"bottom-r"l>',
		  			dom:'',
		  			columnDefs:[
							{
								targets:1,
								render:function(data,type,row,meta) {
									var qname = row.TARGET == '' ? $('#query-name').val() : row.TARGET;
									var id = 'name-'+qname.replace(/\s/g,'-')+'-'+meta.row;
									var elem = '<input type="text" id="'+id+'" value="'+data+'"/>';
									return elem;
								}
							},
		  			  {
		  			  	targets:2,
		  			  	render:function(data,type,row,meta) {
		  			  		var qname = row.TARGET == '' ? $('#query-name').val() : row.TARGET;
									var id = 'value-'+qname.replace(/\s/g,'-')+'-'+meta.row;
									var elem;
									if(data == 'true' || data == 'false')
									{
										elem  = '<input type="radio" name="'+id+'" value="true"'+(data == 'true' ? ' checked="true"' : '')+'/>True';
					  		    elem += '<input type="radio" name="'+id+'" value="false"'+(data == 'false' ? ' checked="true"' : '')+'/>False';
									}
									else if(data =='asc' || data == 'desc')
									{
										elem  = '<input type="radio" name="'+id+'" value="asc"'+(data == 'asc' ? ' checked="true"' : '')+'/>Ascending';
					  		    elem += '<input type="radio" name="'+id+'" value="desc"'+(data == 'desc' ? ' checked="true"' : '')+'/>Descending';
									}
									else
					  		    elem = '<input type="text" id="'+id+'" value="'+data+'"/>';
		  			  		return elem;
		  			  	}
		  			  },
		  			  {
		  			  	targets:3,
		  			  	render:function(data,type,row,meta) {
		  			  		var qname = row.TARGET == '' ? $('#query-name').val() : row.TARGET;
		  			  	  var index = qname.replace(/\s/g,'-')+'-'+meta.row;
		  			  	  var elem  = '<input type="radio" name="rule-'+index+'" value="1" '+(data==1?'checked="true"':'')+'>Non-overridable';
		  			  	      elem += '<input type="radio" name="rule-'+index+'" value="0" '+(data==0?'checked="true"':'')+'>Overridable';
		  			  	  return elem;
		  			    }
		  			  }
		  			],
		  			columns:[
		  			  {data:"TARGET",title:"Target",visible:false},
		  				{data:"NAME",title:"Parameter",sortable:false},
		  				{data:"VALUE",title:"Value",sortable:false},
		  				{data:"RULE",title:"Mutability",sortable:false},
		  				{data:"ACTION",title:"Action",sortable:false,defaultContent:'<button type="button" class="fa fa-save fa-save-md" title="Save"><button type="button" class="fa fa-remove fa-remove-md" title="Remove" style="color:red"/><button type="button" class="fa fa-plus fa-plus-md" title="Add Another" style="color:green"/>'},
		  				{data:"ID",title:"Id",visible:false,name:"ID"}
		  			]
		  		});
	  		}
	  	};

	  	this.enrich = function(e,d) {
	  		var self = this;
	  		var $input = $('#query-name');
	  		var val    = $input.val().replace(/\s/g,"-");
	  		$('input[id^=name-'+val+'-]').each(function() {
	      	var $nameAc = $(this);
	      	$nameAc.autocomplete({
	        	lookup:self.yadaParams,
	        	formatResult: function(suggestion,currentValue){
	        		// without calling the builtin formatResult the dropdown loses the
	        		// default formatting with <strong> tags around the search term
	        		return $.Autocomplete.formatResult({value:suggestion.data.longname},currentValue);
	        	},
	        	onSelect: function(suggestion) {
	        			self.paramControls[suggestion.value](this,self);
	        	},
	        	beforeRender: function(container) {
	        		container.children('div.autocomplete-suggestion').each(function() {
	        			var $this = $(this);
	        			var td   = $nameAc.closest('td')[0];
	        			$this.data({toggle:'tooltip',placement:'right'})
	        			.attr('title', _.filter(self.yadaParams,{ data: {longname: $(this).text() }})[0].data.tooltip)
	        			.tooltip({container:td});
	        		});
	        	},
	        	width: 171
	        });
	      });
	  		var table = $('#default-params').DataTable();
	  		var data  = table.data();
	  		if (data.length == 1 && data[0].NAME == "" && data[0].VALUE == "")
	  		{
	  		  // set data 'status' to new on new row html tr nodes in table
	  			$(table.rows().nodes()[data.length - 1]).data('status','new');
	  		}
	  	};

	  	this.saveParam = function(e,d) {
	  	  e.preventDefault();
	  		var self = this,
	  		$table   = $('#default-params'),
	  		table    = $table.DataTable(),
	  		$tr      = $(e.target).closest('tr'),
	  		data     = table.row($tr).data(),
	  		action   = $(e.target).closest('tr').data('status') == 'new' ? 'insert' : 'update',
	  		target   = data.TARGET,
	  		id       = data.ID,
	  		name     = $tr.find('input[id^="name-"]').val(),
	  		rule     = $tr.find('input[name^="rule-"]:checked').val(),
	  		radio    = $tr.find('input[type="radio"][name^="value-"]'),
	  		val      = '';
	  		if(radio.length > 0)
	  			val = radio.filter(':checked').val();
	  		else
	  			val = $tr.find('input[id^="value-"]').val();
	  		if(name != "")
	  		{
  	  		var params   = {ID:id,TARGET:target,NAME:name,VALUE:val,RULE:rule},
  	  		j = [{qname:'YADA '+action+' default param',DATA:[params]}];
  	  		$.ajax({
  	  			//url:'/yada.jsp',
  	  			type:'POST',
  	  			data:{
  	  				j:JSON.stringify(j)
  	  			},
  	  			success: function(data) {
  	  				table.row($tr).data(params).draw();
  	  				$tr.removeData('status');
  	  			}
  	  		});
	  		}
	  	};

	  	this.removeParam = function(e,d) {
	  	  e.preventDefault();
	  		var self = this,
	  		$table   = $('#default-params'),
	  		table    = $table.DataTable(),
	  		$tr      = $(e.target).closest('tr');
	  		if($(table.row($tr).nodes()).data('status') != 'new')
	  		{
	  		  var app = $('#app-selection').text().trim();
	  		  var data = $.extend({},table.row($tr).data(),{APP:app});
  	  		var j = [{qname:'YADA delete default param',DATA:[data]}];
  	  		j.push({qname:'YADA delete prop for target',DATA:[{TARGET:data.TARGET + '-' + data.ID, APP:app}]});
  	  		$.ajax({
  	  			//url:'/yada.jsp',
  	  			type:'POST',
  	  			data:{
  	  				j:JSON.stringify(j)
  	  			},
  	  			success: function(data) {
  	  	  		table.row($tr).remove().draw();
  	  	  		if(table.data().length == 0)
  	  	  			self.addParam();
  	  	  		self.trigger('update-security-panel',{});
  	  			}
  	  		});
	  		}
	  	};



	  	this.addParam = function(e,d) {
	  		var self = this;
	  		var $table = $('#default-params');
	  		var table  = $.fn.DataTable.isDataTable( '#default-params' ) ? $table.DataTable() : null;
	  		var name = '', value = '', rule = 1, id = 1, idCol;
	  		if(d !== undefined)
	  	  {
	  		  name = d.NAME||'';
	  		  value = d.VALUE||'';
	  		  rule = d.RULE||1;
	  		  var idCol = table !== null ? table.column('ID:name') : null;
	  		  id = d.ID || (idCol !== null && idCol.length > 0) ? idCol.data().sort().reverse()[0] + 1 : 1;
	  	  }

	  		var params = {TARGET:$('#query-name').val(),NAME:name,VALUE:value,RULE:rule,ID:id};

	  		table.row.add(params).draw();
	  		var lastIndex = table.data().length - 1;
	  		$(table.rows().nodes()[lastIndex]).data('status','new');
	  	};

	  	this.defaultAttrs({
	  		paramSave:   '.fa-save',
	  		paramRemove: '.fa-remove',
	  		paramAdd:    '.fa-plus'
	  	});

	    this.after('initialize', function () {
	      this.on('add-param',this.addParam)
	     	this.on('show-params',this.showParams);
	     	this.on('draw.dt',this.enrich);
	     	this.on('click',{
	     		paramSave: this.saveParam,
	     		paramRemove: this.removeParam,
	     		paramAdd: this.addParam

	     	});
	    });
	  }
});
