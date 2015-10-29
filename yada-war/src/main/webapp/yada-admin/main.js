
require.config({
		waitSeconds: 0,
		baseUrl: '',
    paths: {
    	bootstrap:    "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min",
    	datatables:   "http://cdn.datatables.net/1.10.7/js/jquery.dataTables.min",
    	domReady:     "http://cdnjs.cloudflare.com/ajax/libs/require-domReady/2.0.1/domReady.min",
    	flight:       "https://cdnjs.cloudflare.com/ajax/libs/flight/1.1.4/flight.min",
    	jquery:       "http://cdnjs.cloudflare.com/ajax/libs/jquery/1.10.2/jquery.min",
    	lodash:		    "http://cdnjs.cloudflare.com/ajax/libs/lodash.js/3.9.0/lodash.min",
    	autocomplete: "https://cdnjs.cloudflare.com/ajax/libs/jquery.devbridge-autocomplete/1.2.21/jquery.autocomplete",
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
