export default {
  install: function(Vue, options) {
    Vue.getMonths = function() { return  ['','JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'] }
    Vue.getFormattedDate = function(data,format) {
      if(format === undefined || format === null || format === '')
        format = "YADA";
      if(typeof data == 'object')
        data = data.toString();
      else if(typeof data != 'string')
        return '';


      var rxMillis10 = /[\d]{10}/; //1234567890
      var rxMillis13 = /[\d]{13}/; //1234567890123
      var rxOrclDate = /([\d]{2})-([JjFbMmAaSsOoNnDd][AaEePpUuCcOo][NnBbRrYyLlGgPpTtVvCc])-(([\d]{2})?[\d]{2})\s([\d:]{6}[\d]{2})/;
      var rxYadaDate = /[\d]{2}([\d]{2})-([\d]{2})-([\d]{2})\s([\d:]{6}[\d]{2})/; //2015-05-27 00:00:00
      var rxJsDate   = /[A-Z][a-z]{2}\s([A-Z][a-z]{2})\s([\d]{2})\s[\d]{2}([\d]{2})\s([\d:]{6}[\d]{2})\sGMT-[\d]{4}\s\([A-Z]+\)/;  //Tue Jun 02 2015 18:34:27 GMT-0400 (EDT)
      var result, y, m, d, hms, form;

      if((result = data.match(rxMillis13)) != undefined)
      {
        var data = new Date(parseInt(result)).toString();
      }
      else if((result = data.match(rxMillis10)) != undefined)
      {
        var data = new Date(parseInt(result)*1000).toString();
      }

      if(format.toLowerCase() == 'oracle')
      {
        if((result = data.match(rxYadaDate)) != undefined)
        {
          y = result[1];
          m = this.getMonths()[parseInt(result[2])];
          d = result[3];
        }
        else if((result = data.match(rxJsDate)) != undefined)
        {
          y = result[3];
          m = result[1].toUpperCase();
          d = result[2];
        }
        hms = result[4];
        form = d+'-'+m+'-'+y+' '+hms;
      }
      else
      {
        if((result = data.match(rxYadaDate)) != undefined)
        {
          form = data;
        }
        else
        {
          if((result = data.match(rxOrclDate)) != undefined)
          {
            m = _.indexOf(this.getMonths(),result[2].toUpperCase());
            d = result[1];
          }
          else if((result = data.match(rxJsDate)) != undefined)
          {
            m = _.indexOf(this.getMonths(),result[1].toUpperCase());
            d = result[2];
          }
          y = result[3];
          hms = result[4];

          m = m < 10 ? "0"+m : m;
          y = y.length == 2 ? "20"+y : y;

          form = y+'-'+m+'-'+d+' '+hms;
        }
      }

      return form;
    }
  }
}
