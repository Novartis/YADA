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
//require.config({
//  baseUrl: "",
//  paths: {
//    lodash:     "file:///utils/lodash.min",
//    JSON:       "file:///utils/json2"
//  }
//});
//
//require(["lodash","JSON"], function(_,JSON) {
(function() {    
  this.expandArrayKeys = function(element, src) {
  	var indices = [];
  	if (/\-/.test(element)) // range
  	{
  		var min = parseInt(element.substring(1, element.indexOf('-')));
  		var max = parseInt(element.substring(element.indexOf('-') + 1));
  		for (var i = min; i <= max; i++) {
  			indices.push(i);
  		}
  	} else if (/\,/.test(element)) // non-contig selection
  	{
  		_.each(element, function(i) {
  			indices.push(i);
  		});
  	} else if (/\*/.test(element)) // all
  	{
  		var min = 0;
  		var max = src.length;
  		for (i = min; i < max; i++) {
  			indices.push(i);
  		}
  	} else {
  		// single array ref in compound key
  	}
  	return indices;
  };
  
  this.mergeArrayKeys = function(elem, elements, matchHash, keyContainer) {
  	var keyCount = keyContainer.length;
  	var matchKeys = _.keys(matchHash);
  	var elementCount = elements.length;
  	var matchHashVals = _.values(matchHash);
  	// pad array with subarray.length == 1
  	// in case matchHash.length == 1 && matchHash[0].length = 1
  	matchHashVals.push([ 0 ]);
  	var maxIter = _.reduce(matchHashVals, function(a, b) {
  		return a.length * b.length;
  	});
  	// iterate over each element of the src key
  	if (elem < elementCount && (keyContainer.length <= maxIter)) {
  		// iterate over all currently stored variations of this src key
  		for (var k = 0; k < keyCount; k++) {
  			var currentKey = keyContainer[k].split(/\./).slice();
  			// is this element in need of expansion?
  			if (_.indexOf(matchKeys, elem.toString()) == -1) // elem is not
  																// stored in
  																// matchKeys
  			{
  				// has element been added to currentKey
  				if (elem >= currentKey.length - 1)
  					currentKey.push(elements[elem]); // simple append
  				// special case for elem == 0
  				else if (elem == 0 && currentKey.length == 1
  						&& currentKey[0] == "")
  					currentKey[0] = elements[elem];
  
  				keyContainer[k] = currentKey.join('.');
  			} else // needs expansion
  			{
  				// get base key
  				currentKey = keyContainer[k].split(/\./).slice(0, elem);
  				// length of expanded key array
  				var currentCount = matchHash[elem].length;
  				// iterate over key-expansion entries
  				for (var n = 0; n < currentCount; n++) {
  					// clone the base key
  					var neoKey = currentKey.slice();
  					// add the array ref to the neoKey
  					neoKey.push('[' + matchHash[elem][n] + ']');
  					// add or replace the key
  					if (n == 0)
  						keyContainer[k] = neoKey.join('.');
  					else
  						keyContainer.push(neoKey.join('.'));
  				}
  			}
  		}
  		// recurse to next elem
  		mergeArrayKeys(++elem, elements, matchHash, keyContainer);
  	}
  };
  
  
  this.harmonize = function(s,h) {
    var src   = this.JSON.parse(s);
    var hm    = this.JSON.parse(h);
    if(!_.isArray(src))
      return this.JSON.stringify(this._harmonize(src,hm));
    else
    {
      var result = [];
      _.each(src,function(o){
        result.push(this._harmonize(o,hm));
      });
      return this.JSON.stringify(result);
    }
  };
  
  this._harmonize = function(src,hm) {
  	var prune = hm.prune; // prune flag
  	var neo   = src; // set neo to src by default
  
  	if (prune) {
  		neo = {}; // only mapped keys will be in result
  	}
  	
  	_.each(_.keys(hm), function(key)             // iterate over hmap
  	{
  		var RX_ARRAY = /(.+)\[([*\d\-,]+)\](.+)?/, // key contains array i.e, x.[*].y
  		RX_IDX  = /\[(\*|\d+[\,-]\d+)\]/,          // index matches
  		srcComp = false,                           // compound key in original
  		k       = [],                              // container for key iteration
  		hmkey   = hm[key],                         // original src key value
  		keyMap  = {};
  
  		if (/.+[.](.+)?/.test(key)) // test for compound key in original
  			srcComp = true;
  
  		if (/.+[.].+/.test(hmkey))  // test for compound key in value
  			neoComp = true;
  
  		// This conditional is all about handling array signifiers in the "src" key.
  
  		if (srcComp && RX_ARRAY.test(key)) // test for array in key
  		{
  			var elements  = key.split(/\./);
  			var matchHash = {};
  
  			// Iterate over all elements of compound key
  			for (var m = 0; m < elements.length; m++) {
  				// If element is array ref, store it's index as a hash key
  				// e.g., k1.[*].k2.[1-4]
  				if (RX_IDX.test(elements[m])) {
  					// e.g., matchHash[1] = [], matchHash[3] = []
  					matchHash[m] = [];
  				}
  			}
  
  			// Iterate over the hash keys and set each hash value
  			// to an array of desired indices from the src object
  			_.each(_.keys(matchHash), function(m) {
  				// e.g., matchHash[1] = [1,2,3,4,5], matchHash[3] = [1,2,3,4]
  				matchHash[m] = expandArrayKeys(elements[m],
  						src[elements[m - 1]]);
  			});
  			k.push(key);
  			// Iterate over the hash and create the expanded keys for processing
  			mergeArrayKeys(0, elements, matchHash, k);
  		} else {
  			k.push(key); // no array ref, just simple key
  		} // end array ref handling
  
  		// map expanded keys et al to neo keys
  		for (var km = 0; km < k.length; km++) {
  			keyMap[k[km]] = key;
  		}
  
  		// process keys into return object
  		_.each(k, function(key) {
  			var neoKey = hmkey,
  			// multiple (>1) src keys point to the same neo key:
  			needsArray = _.filter(_.values(keyMap), function(o) {
  				return o == keyMap[key];
  			}).length > 1;
  
  			if (needsArray) {
  				if (!Array.isArray(neo[neoKey]))
  					neo[neoKey] = [];
  				var val = _.get(src, key);
  				/*
  				 * accounts for mapped keys omitted from nested arrays, i.e.,
  				 * key1[*].keyx mapped would result in null indexs 1,2
  				 * {"key1":[{"keya":"x","keyx":"a"}, {"keya":"y","keyy":"b"},
  				 * {"keya":"z","keyz":"c"}]}
  				 */
  				if (val != null)
  					neo[neoKey].push(val);
  			} else {
  				neo[hm[key]] = _.get(src, key); // set the new key val to the old key val
  			}
  
  			// delete the old key if necessary
  			if (!prune) // !(prune) bc (prune) == only hm keys are present in the 1st place
  			{
  				if (srcComp) {
  					var comp = key.split(/./), j = comp.length - 1;
  					while (j > 0) {
  						// do some compound src key pruning magic here
  						j--;
  					}
  				} else {
  					delete neo[key];
  				}
  			}
  		});
  
  		// explode compound neoKeys
  		_.each(_(_.keys(neo)).filter(function(nk) {
  			return /[.]/.test(nk);
  		}).value(), function(nk) {
  			var kys = nk.split(/\./);
  			var value = neo[nk];
  			var obj = {};
  			for (var i = 0; i < kys.length; i++) {
  				if (i == 0) // first element
  				{
  					value = neo[nk];
  					if (!_.has(neo, kys[i])) // key doesn't exist yet
  					{
  						neo[kys[i]] = obj;
  					} else // key does exist
  					{
  
  					}
  				} else if (i == kys.length - 1) // last element
  				{
  					obj[kys[i]] = value;
  				} else {
  					obj[kys[i + 1]] = {};
  					obj = obj[kys[i + 1]];
  				}
  			}
  			delete neo[nk];
  		});
  	});
  	return neo;
  };
  
  this.flatten = function(s) 
  {
    // convert string to obj
    var src     = this.JSON.parse(s);
    var data    = {};
    var a       = [];
    var result  = "", val = "";
    var maxRows = 0, rows = 0;
    
    // put src into array
    if(!_.isArray(src)) 
    {
      a.push(src);
    }
    else
    {
      a = src.slice();
    }
    // process array
    this._flatten("",a,data);
    
    // create the response string
    var keys = _.keys(data);
    _.each(keys, function(k) {
      rows = data[k].length;
      if(rows > maxRows)
        maxRows = rows;
      result += "\""+k+"\",";
    });
    result = result.slice(0,-1)+"\n";
    
    for(var i=0;i<maxRows;i++)
    {
      _.each(keys,function(k) { 
        var val = "";  
        if(i < data[k].length)
          val = data[k][i];
        result += "\""+val+"\",";
      });
      result = result.slice(0,-1)+"\n";
    }
    return result;
  };
  
  this._flatten = function(flatKey, lastValue, data)
  {
    if (_.isArray(lastValue))
    {
      _.each(lastValue,function(o) {
        this._flatten(flatKey,o,data);
      });
    }
    else if(typeof lastValue == "object")
    {
      _.each(_.keys(lastValue),function(key) {
        if(_.isArray(lastValue[key]))
        {
          var a   = lastValue[key];
          var dot = flatKey === "" ? "" : ".";
          flatKey += dot+key;
          _.each(a,function(o) {
            this._flatten(flatKey,o,data);
          });
        }
        else if(typeof lastValue[key] == "object")
        {
          var o   = lastValue[key];
          var dot = flatKey === "" ? "" : ".";
          flatKey += dot+key;
          this._flatten(flatKey,o,data);
        }
        else
        {
        
          var val = lastValue[key];
          if(flatKey !== "")
          {
            key = flatKey + "." + key;
          }
          if(!_.has(data,key))
          {
            data[key] = [];
          }
          data[key].push(val);
        }
      });
    }
  };
})();
