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
require.config({
		waitSeconds: 0,
		baseUrl: '',
    paths: {
    	bootstrap:    "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min",
    	datatables:   "https://cdn.datatables.net/1.10.7/js/jquery.dataTables.min",
    	domReady:     "https://cdnjs.cloudflare.com/ajax/libs/require-domReady/2.0.1/domReady.min",
    	flight:       "https://cdnjs.cloudflare.com/ajax/libs/flight/1.1.4/flight.min",
    	jquery:       "https://cdnjs.cloudflare.com/ajax/libs/jquery/1.10.2/jquery.min",
    	lodash:		    "https://cdnjs.cloudflare.com/ajax/libs/lodash.js/3.9.0/lodash.min",
    	autocomplete: "https://cdnjs.cloudflare.com/ajax/libs/jquery.devbridge-autocomplete/1.2.21/jquery.autocomplete",
    	Clipboard:    "https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/1.5.10/clipboard",
    	mergely:      "lib/mergely/mergely.min",
    	text:         "https://cdnjs.cloudflare.com/ajax/libs/require-text/2.0.12/text.min"
    },
    map : {
    	'mergely' : { 'jQuery':'jquery'},
    },
    shim: {
    	bootstrap: { deps : ["jquery"] },
    	flight:    { deps : ["jquery"], exports: 'flight'},
    	autocomplete:   { deps : ["jquery"], exports: 'autocomplete'},
    	codemirror: { exports: 'CodeMirror'},
    	mergely:   { deps : ["codemirror","jQuery"], exports: 'mergely' },
    },
    packages: [{
      name: "codemirror",
      location: "lib/codemirror",
      main: "lib/codemirror"
  }]


});

require(
[

],
function() {
  require(['jquery','boot','codemirror','text!config.json','mixin/withConfig'],function($,Boot,CodeMirror,config,withConfig){
  	window.CodeMirror = CodeMirror;
  	window.YADAAdmin  = config;
  	withConfig.call(Boot.prototype);
   	Boot.initialize();
  });
});
