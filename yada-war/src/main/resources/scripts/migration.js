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
Promise  = require("bluebird"),
rp       = require("request-promise"),
_        = require("lodash"),
minimist = require("minimist"),

argv     = minimist(process.argv),

proto    = argv.c ? 'https://' : 'http://',
src      = proto+argv.s,
tgt      = proto+argv.t,
app      = argv.a,
bak      = argv.b
del      = argv.p ? false : true,
dryrun   = argv.d || false,
help     = argv.h || argv["?"],
opts          = '/pz/-1',
data          = {}, 
sourceQnames  = [],
sourceQueries = [],
tgtQnames     = [],
tgtQueries    = [],
defaultExcl   = ['YADA apps',
                 'YADA queries',
                 'YADA new query',
                 'YADA delete query',
                 'YADA insert usage log',
                 'YADA update query'],
blacklist     = argv.x ? _.union(argv.x.split(','), defaultExcl) : defaultExcl,
whitelist     = argv.i ? _.difference(argv.i.split(','),blacklist) : [], 
inserts       = [],
updates       = [],
deletes       = [],
merged        = [],
addParams     = [],
delParams     = [],
updParams     = [],
tgtParams     = [],
srcParams     = [],
jp            = [{"qname":"YADA queries","DATA":[{"APP": app}]},
                  {"qname":"YADA select default params for app","DATA":[{"APP": app}]}];


if(help || isEmpty(src) || isEmpty(tgt) || isEmpty(app))
{
  usage();
}

promise().then(function() {
  compareAndPrepare();
  mergeDefaultParams();
  migrate();
}).
catch(function() {
  process.exit(1);
});

function isEmpty(val) {
  if(val === undefined
      || val === null
      || val === "")
    return true;
  return false;
};

function usage() {
  var msg  = "\nUsage: node migration.js -s source[:port] -t target[:port] -a app ";
      msg += "[-h?dbc] [-i includes] [-x excludes]";
      msg += "\n";
      msg += "\n    Options:";
      msg += "\n      -s the source YADA environment host name and optional port. Do not include";
      msg += "\n         the protocol.";
      msg += "\n      -t the target YADA environment host name and optional port. Do not include";
      msg += "\n         the protocol.";
      msg += "\n      -a the YADA app code";
      msg += "\n      -b Create a backup of target queries and params before execution. The data will be";
      msg += "\n         written to a file named 'YADA_target_backup.json' in the working directory. This";
      msg += "\n         file is not written automatically for security reasons, but it probably should";
      msg += "\n         be, and the option should always be included, for a host of other reasons."
      msg += "\n      -c Use https instead of http";
      msg += "\n      -i a comma-separated list of query names to process. If present, ONLY these";
      msg += "\n         queries will be processed, except those on the global blacklist.";
      msg += "\n      -x a comma-separated list of query names to exclude. If present, none of these";
      msg += "\n         queries will be processed.";
      msg += "\n      -p preserve queries in target that do not exist in source. Default is to ";
      msg += "\n         delete queries that only exist in target. Blacklist (-x) can affect default.";
      msg += "\n      -d A dry run. Only outputs the JSON payload inteneded for target, to the console.";
      msg += "\n      -h print this message";
      msg += "\n      -? print this message";
      msg += "\n\n";
  console.log(msg);
  process.exit()
};

function promise() {
  return Promise.all([getQueries(src),getQueries(tgt)]);
};

function getQueries(host) {
  this.data[host] = {"queries":[],"params":[]}; 
  return rp(host+'/j/'+JSON.stringify(this.jp)+this.opts)
  .then(function(resp) {
    var results = JSON.parse(resp);
    this.data[host]["queries"] = results.RESULTSETS[0].RESULTSET.ROWS;
    this.data[host]["params"]  = results.RESULTSETS[1].RESULTSET.ROWS;
  });
};
	
function mergeDefaultParams() {
	var self = this;
	
	var a = this.data[this.src]["params"];
	var b = this.data[this.tgt]["params"];
	
  function flatten(array) {
  	return _.map(array,function(o){ return [o.TARGET,o.NAME,o.RULE,o.VALUE].join('||'); });
  }
  
  function splitSpliceJoin(flat) {
  	return flat.split('||').splice(0,2).join('||');
  }
  
	var a_flat = flatten(a),
			b_flat = flatten(b),
			a_flat_only = _.filter(a_flat,function(a){ return _.indexOf(b_flat,a) == -1; }), // adds and updates
			b_flat_only = _.filter(b_flat,function(b){ return _.indexOf(a_flat,b) == -1; }), // dels and updates
			flatUpdParams = [];
	_.each([self.addParams,self.delParams,self.updParams],function(array) { array.splice(0,array.length); }); // clear arrays
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

	
function compareAndPrepare() {
  var self = this;
  
  // list of qnames returned by src query
  this.sourceQnames  = _.pluck(this.data[src]["queries"],'QNAME');
  // src query map
  this.sourceQueries = {};
  _.each(this.data[src]["queries"],function(row){
    self.sourceQueries[row.QNAME] = {"QUERY":row.QUERY,"MODIFIED":row.MODIFIED,"MODIFIED_BY":row.MODIFIED_BY};
  });

  //list of qnames returned by tgt query
  this.targetQnames  = _.pluck(this.data[tgt].queries,'QNAME');
  // tgt query map
  this.targetQueries = {};
  _.each(this.data[tgt]["queries"],function(row){
    self.targetQueries[row.QNAME] = {"QUERY":row.QUERY,"MODIFIED":row.MODIFIED,"MODIFIED_BY":row.MODIFIED_BY};
  });
  
  // pruned list of qnames per exclusions (blacklist)
  this.whitelist     = this.whitelist.length > 0 ? this.whitelist : this.sourceQnames;
  this.whitelist     = _.difference(this.whitelist,this.blacklist); 
  
  // list of queries to delete
  if(this.del)
  {
    this.deletes = _.difference(this.targetQnames,this.whitelist,this.blacklist);
  }
  
  // list of queries to insert (whitelist - targets)
  this.inserts = _.difference(this.whitelist,this.targetQnames);
  
  // list of queries to update
  this.updates = _.filter(_.difference(this.whitelist, this.inserts), function(qname) {
    var s = self.sourceQueries[qname];
    var t = self.targetQueries[qname];
    
    return s.QUERY !== t.QUERY;
  });
};

function migrate() {
		var self = this,
		    j = [];
		
		// updates
		if(this.updates.length > 0)
		{
			j.push({qname:'YADA update query',DATA:[]});
			_.each(this.updates,function(query){
  			var o = {QNAME:query,
  							 QUERY:this.sourceQueries[query].QUERY,
  							 MODIFIED_BY:this.sourceQueries[query].MODIFIED_BY,
  							 MODIFIED:this.sourceQueries[query].MODIFIED,
  							 APP:this.app};
  			j[j.length-1].DATA.push(o);
  		});	  			
		}
		// inserts
		if(this.inserts.length > 0)
		{
  		j.push({qname:'YADA new query',DATA:[]});
  		_.each(this.inserts,function(query){
  			var o = {QNAME:query,
  							 QUERY:this.sourceQueries[query].QUERY,
  							 MODIFIED_BY:this.sourceQueries[query].MODIFIED_BY,
  							 CREATED_BY:this.sourceQueries[query].MODIFIED_BY,
  							 APP:this.app};
  			j[j.length-1].DATA.push(o);
  		});	  			
		}
		// deletes
		if(this.deletes.length > 0)
		{
  		j.push({qname:'YADA delete query',DATA:[]});
  		_.each(this.deletes,function(query){
  			var o = {QNAME: query, APP: this.app};
  			j[j.length-1].DATA.push(o);
  		});	  			
		}
		
		if(this.updParams.length > 0)
		{
  		j.push({qname:'YADA update default param',DATA:[]});
  		_.each(this.updParams,function(param) {
  			var o = {TARGET:param[0],NAME:param[1],VALUE:param[3],RULE:param[2]};
  			j[j.length-1].DATA.push(o);
  		});
		}	  		
		
		if(this.addParams.length > 0)
		{
  		j.push({qname:'YADA insert default param',DATA:[]});
  		_.each(this.addParams,function(param) {
  			var o = {TARGET:param[0],NAME:param[1],VALUE:param[3],RULE:param[2]};
  			j[j.length-1].DATA.push(o);
  		});	  			
		}
		
		if(this.delParams.length > 0)
		{
  		j.push({qname:'YADA delete default param',DATA:[]});
  		_.each(this.delParams,function(param) {
  			var o = {TARGET:param[0],NAME:param[1],VALUE:param[3],RULE:param[2]};
  			j[j.length-1].DATA.push(o);
  		});	  			
		}
		
		if(bak) {
		  var fs = require('fs');
		  var fname = 'YADA_'+tgt.split(/:/)[0]+'_bak.json';
		  var queries = this.data[this.tgt].queries;
		  var params  = this.data[this.tgt].params;
		  var payload =  [{"qname":"YADA delete query","DATA":queries},
		                 {"qname":"YADA delete default param","DATA":params},
		                 {"qname":"YADA new query","DATA":queries},
		                 {"qname":"YADA insert default param","DATA":params}]
		  fs.writeFile(fname, 'j='+JSON.stringify(payload,null,2), "utf8");
		}
		
		
		if (dryrun)
		  console.log(JSON.stringify(j,null,2));
		else
		// update yada_query set qname=?v, query=?v, modified_by=?v, modified=?d where qname=?v and app=?v
		  return rp(this.tgt+'/j/'+JSON.stringify(j));		
};

