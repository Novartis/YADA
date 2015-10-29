define(function(require) {

  return withDateUtils;
  
  function withDateUtils() {
		this.getMonths = function() { return  ['','JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC']; };
		this.getOracleDateStr = function(data) {
			if(typeof data == 'object')
				data = data.toString();
			else if(typeof data != 'string')
				return '';
			var rxYadaDate = /[\d]{2}([\d]{2})-([\d]{2})-([\d]{2})\s([\d:]{6}[\d]{2})/; //2015-05-27 00:00:00
			var rxJsDate   = /[A-Z][a-z]{2}\s([A-Z][a-z]{2})\s([\d]{2})\s[\d]{2}([\d]{2})\s([\d:]{6}[\d]{2})\sGMT-[\d]{4}\s\([A-Z]+\)/;  //Tue Jun 02 2015 18:34:27 GMT-0400 (EDT)
		  var result, y, m, d, hms;
			if((result = data.match(rxYadaDate)) != undefined)
			{
				y = result[1];//data.substr(2,2);
				m = this.getMonths()[parseInt(result[2])];//this.getMonths()[parseInt(data.substr(5,2))];
				d = result[3];//data.substr(8,2);
				hms = result[4];
			}
			else if((result = data.match(rxJsDate)) != undefined)
			{
				y = result[3];
				m = result[1].toUpperCase();
				d = result[2];
				hms = result[4];
			}
			return d+'-'+m+'-'+y+' '+hms;
		};
  }
});