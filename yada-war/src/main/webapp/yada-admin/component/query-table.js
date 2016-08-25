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
  [ 'flight', 'jquery', 'datatables', 'component/query-params', 'mixin/withDateUtils', 'mixin/withConfig',
      'Clipboard', 'codemirror', 'codemirror/mode/sql/sql', 'codemirror/mode/javascript/javascript.min',
      'codemirror/mode/xml/xml.min'
  ],
  function(flight, $, dataTables, Params, withDateUtils, withConfig, Clipboard, CodeMirror)
  {

    'use strict';
    return flight.component(queryTable, withDateUtils, withConfig);

    // TODO yada mode for codemirror (yada-sql, yada-xml, yada-json, yada-soap
    // (json/xml)

    function queryTable()
    {

      this.app = "";
      this.qname = '';
      this.comments = '';
      this.edit = '';
      this.editor = {};
      this.query = '';
      this.securityOptions = '';
      this.policyGroup = '';

      this.refresh = function(e,d) {
        this.app = d.app;
        $('#app-selection').show().find('h1').text(d.app)
        $('nav.main-menu li').removeClass('disabled');
        
        $('#new-query,#migration').attr('data-toggle','modal');
        $('#new-query').attr('data-target','#query-editor-container');
        $('#migration').attr('data-target','#migration-target-selector');
        
        //this.trigger('app-requested',{app:d.app});
      };
            
      this.enrich = function()
      { // triggered at init
        var self = this;
        $('#app,#toggle-view,#new-query').removeClass('disabled');
        $('#app-selection').removeClass('hidden');
        this.select('query-table').dataTable({
          autoWidth: false,
          ajax: {
            url: $.ajaxSettings.url,
            type: "POST",
            data: function(d)
            {
              var app = self.select('query-table').data('app');
              return $.extend({}, d, {
//                qname: self.attr.q_queries, // "YADA queries",
//                params: app,
                j:JSON.stringify([{qname:self.attr.q_queries,DATA:[{APP:app}]}]),
                pz: "-1"
              });
            },
            dataSrc: "RESULTSET.ROWS"
          },
          "lengthMenu": [ [ 5, 10, 25, 50, 75, -1
          ], [ 5, 10, 25, 50, 75, 'All'
          ]
          ],
          dom: '<"top"f>t<"bottom-l"i><"bottom-c"p><"bottom-r"l>',
          language: {
            search: 'Filter on qname, query, or comments:',
            searchPlaceholder: 'Filter...'
          },
          columnDefs: [ {
            targets: 2,
            render: function(data, type, row, meta)
            {
              return '<pre>' + data.replace(/</g, "&lt;").replace(/>/g, "&gt;") + '</pre>';
            }
          }, {
            targets: 3,
            render: function(data, type, row, meta)
            {
              return data.replace(/</g, "&lt;").replace(/>/g, "&gt;");
            }
          }, {
            targets: 4,
            render: function(data, type, row, meta)
            {
              var len = 12;
              if (type === 'display' && data.length > len)
              {
                return '<span data-toggle="tooltip" title="' + data + '">' + data.substr(0, len) + '…</span>';
              }
              return data; // "short display", or filter
            }
          }, {
            targets: [ 6, 8
            ], // date columns
            type: "date",
            className: 'dt-body-center',
            render: function(data, type, row, meta)
            {
              if (data == "")
                return data;
              var txt = self.getFormattedDate(data, "oracle");
              return '<span title="' + txt + '">' + txt.substr(0, 10) + '</span>';
            }
          }
          ],
          columns: [ {
            "className": '',
            "orderable": false,
            "data": null,
            "defaultContent": '<span class="fa fa-chevron-circle-right fa-md"></span>',
            visible: false
          }, {
            data: "QNAME",
            title: "Qname",
            searchable: true
          }, {
            data: "QUERY",
            title: "Query",
            name: "QueryCode",
            searchable: true
          }, {
            data: "QUERY",
            title: "Query",
            name: "QueryText",
            searchable: true,
            visible: false
          }, {
            data: "COMMENTS",
            title: "Comments",
            searchable: true
          }, {
            data: "DEFAULT_PARAMS",
            title: 'Default Parameters',
            type: 'num',
            searchable: false,
            className: 'dt-body-center'
          }, {
            data: "LAST_ACCESS",
            title: "Last Accessed",
            searchable: false
          }, {
            data: "ACCESS_COUNT",
            title: "Access Count",
            searchable: false,
            type: 'num',
            className: 'dt-body-right'
          }, {
            data: "MODIFIED",
            title: "Last Modified",
            searchable: false
          }, {
            data: "MODIFIED_BY",
            title: "Modified By",
            searchable: false
          }
          ]
        });
      };

      this.getAdditionalAttributes = function(e, d)
      {
        this.getDefaultParams(e, d);
        this.getProperties(e, d);
      };
      
      this.getDefaultParamRequest = function() {
        var sourceParams = $.ajax({
          type: 'POST',
          data: {
            //q: this.attr.q_params, // 'YADA select default params for app',
            //p: this.app,
            j:JSON.stringify([{qname:this.attr.q_params,DATA:[{APP:this.app}]}]),
            pz: -1,
            c: false
          }
        });
        return sourceParams;
      };
      
      this.getPropertiesRequest = function() {
        var self = this;
        var props = $.ajax({
          type: 'POST',
          data: {
            j:JSON.stringify([{qname:self.attr.q_props,DATA:[{TARGET:self.app}]}]),
            pz: -1,
            c: false
          }
        });
        return props;
      };
      
      this.getProtectorsRequest = function() {
        var self = this;
        var protectors = $.ajax({
          type: 'POST',
          data: {
            //q: self.attr.q_protectors, // 'YADA select protectors for target',
            //p: self.qname,
            j:JSON.stringify([{qname:self.attr.q_protectors,DATA:[{TARGET:self.qname}]}]),
            pz: -1,
            c: false
          }
        });
        return protectors;
      };

      this.getDefaultParams = function(e, d)
      {
        var self = this;
        
        var sourceParams = self.getDefaultParamRequest();
        var resolve = function(data)
        {
          var params = [];
          _.each(data.RESULTSET.ROWS, function(param)
          {
            params.push(param);
          });
          $.removeData('.nest', 'defaultParams');
          self.select('nest').data('defaultParams', params);
        };
        return sourceParams.then(resolve);
      };

      this.getProperties = function(e, d)
      {
        var self = this;
        var properties = self.getPropertiesRequest();
        var resolve = function(data)
        {
          var props = [];
          _.each(data.RESULTSET.ROWS, function(prop)
          {
            props.push(prop);
          });
          $.removeData('.nest', 'properties');
          self.select('nest').data('properties', props);
        };
        return properties.then(resolve);
      };
      
      this.getProtectors = function(e, d)
      {
        var self = this;
        var protectors = self.getProtectorsRequest();
        var resolve = function(data)
        {
          var prot = [];
          _.each(data.RESULTSET.ROWS, function(prop)
          {
            prot.push(prop);
          });
          $.removeData('.nest', 'protectors');
          self.select('nest').data('protectors', prot);
        };
        return protectors.then(resolve);
      };
      
      this.destroy = function(e,d) {
        $('nav.main-menu li').addClass('disabled');
        $('#new-query,#migration').removeAttr('data-toggle');
        $('#new-query,#migration').removeAttr('data-target');
        $('#app-selection').hide();
        if ($.fn.DataTable.isDataTable(this.attr.params))
        {
          this.select('params').DataTable().destroy();
          this.select('params').empty();
        }
        if ($.fn.DataTable.isDataTable(this.attr['query-table']))
          this.select('query-table').DataTable().destroy();
        this.select('query-table').empty();
        this.teardown();
      }

      this.reloadTable = function(e, d)
      {
        $('#migration-table_wrapper').hide();
        $('#query-table_wrapper').show();
        this.app = d.app;
        var table = this.select('query-table').DataTable();
        table.ajax.reload();
        table.draw();
      };

      this.editQuery = function(e, d)
      {
        this.edit = 'update';
        var self = this, target = $(e.target), table = this.select('query-table').DataTable();
        
        // get the row
        if (e.target.nodeName == 'PRE' || e.target.nodeName == 'SPAN')
        {
          target = target.parent();
        }
        var row = table.row(target.closest('tr')).index()
        var cell = table.cell(row, 2);

        // store the data
        this.query = cell.data();
        this.qname = table.cell(row, 1).data();
        this.comments = table.cell(row, 4).data();
        
        // adjust ui
        this.select('qname').val(this.qname).attr('readonly', 'readonly');
        this.select('qname-box').addClass('input-group');
        this.select('clipboard').show();
        this.select('button-copy').show();
        this.select('button-rename').show();
        this.select('button-delete').show();
        this.select('panel').show();
        this.select('query-comments').val(this.comments);
        this.securityOptions = this.select('security-options').prop('outerHTML');
        this.policyGroup = this.select('policy-group').prop('outerHTML');

        this.select('container').modal('show');

        // display the modal
        var $tr = target.closest('tr');
        var params = _.filter(this.select('nest').data('defaultParams'), {
          TARGET: self.qname
        });
        // fetch the execution policy queries now (early)
        $.when(this.getProtectors()).then(function(a){
          self.policyPopulateSecurityPanel(params);
          self.trigger('#default-params', 'show-params', {
            tr: $tr,
            qname: self.qname,
            params: params
          });
        });
      };

      this.renderEditor = function(e, d)
      {
        var self = this;
        if ($(e.target).prop('id') == 'query-editor-container') // specific
        // modal
        {
          this.editor = CodeMirror(document.getElementById('query-editor'), {
            value: self.query,
            lineNumbers: true,
            firstLineNumber: 1,
            theme: 'eclipse'
          });

          var mode = 'text/plain';
          if (/^\s*(select|insert|update|delete)/.test(self.query.toLowerCase()))
          {
            mode = 'text/x-sql';
          }
          else if (/^\s*[{[]/.test(self.query))
          {
            mode = 'application/json';
          }
          else if (/^\s*</.test(self.query))
          {
            mode = 'application/xml';
          }
          this.editor.setOption('mode', mode);
          this.clip = new Clipboard(this.attr['clipboard'], {
            text: function(trigger)
            {
              if (trigger.id == self.attr['clipboard-editor'].replace('#', ''))
                return self.editor.getValue();
            }
          });
          this.clip.on('success', function(e)
          {
            var trig = $(e.trigger);
            trig.tooltip('show');
            setTimeout(function()
            {
              trig.tooltip('hide');
            }, 1000);
          });
          this.select('tooltip').tooltip({
            trigger: 'manual',
            title: 'Copied!'
          });
        }
      };

      this.addQuery = function(e, d)
      { // triggered by 'new query' button and
        // row-click
        this.select('params-wrapper').hide();
        this.restoreFooter();
        this.renderEditor(e, d);
        var self = this;
        if (this.edit !== 'update') // new
        {
          if ($.fn.DataTable.isDataTable(this.attr.params))
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
          this.select('panel').hide();
          this.select('qname-box').removeClass('input-group');
          this.qname = '';
          this.comments = '';
          this.edit = 'new';
          setTimeout(function()
          {
            self.select('qname').focus();
          }, 100);
        }
        else
        {
          this.select('params-wrapper').show();
          this.select('panel').show();
          this.select('container-title').text('Edit query');
        }
      };

      this.clearQuery = function(e, d)
      { // triggered by 'cancel' buttons
        if ($(e.target).prop('id') == 'query-editor-container')
        {
          this.edit = '';
          this.query = '';
          this.comments = '';
          this.query = '';
          this.select('qname').val('');
          this.select('query-comments').val('');

          $('#query-editor').empty();
          this.select('policy-group').remove();
          if (this.select('policy-group').length == 0)
            this.select('security-panel').append(this.policyGroup);
          $('#secure-query-ckbx').removeAttr('checked')
        }
        this.select('alert').remove();
      };

      this.saveQuery = function(e, d)
      {
        var self = this, 
        query = this.editor.getDoc().getValue(), 
        comments = this.select('query-comments').val(), 
        date = new Date(), 
        d = date.getDate() < 10 ? "0" + date.getDate() : date.getDate(), 
        m = (date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1), 
        y = date.getFullYear(), h = date.getHours() < 10 ? "0" + date.getHours() : date.getHours(), 
        M = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes(), 
        s = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds(),

        // update yada_query set qname=?v, query=?v, modified_by=?v,
        // modified=?d where qname=?v and app=?v
        editData = {
          QNAME: self.qname,
          QUERY: query,
          CREATED_BY: self.loggedUser(),
          CREATED: y + '-' + m + '-' + d + ' ' + h + ':' + M + ':' + s,
          MODIFIED_BY: self.loggedUser(),
          MODIFIED: y + '-' + m + '-' + d + ' ' + h + ':' + M + ':' + s,
          APP: self.app,
          ACCESS_COUNT: 0,
          COMMENTS: comments
        },

        j = [ {
          qname: 'YADA ' + self.edit + ' query',
          DATA: [ editData
          ]
        }
        ];

        // save parameters
        if (_.filter(this.select('nest').data('defaultParams'), {
          TARGET: self.qname
        }).length > 0)
        {
          if (self.edit == 'delete')
            $('.fa-remove').trigger('click');
          else
            $('.fa-save').trigger('click');
        }

        // save changes
        $.ajax({
          type: 'POST',
          data: {
            j: JSON.stringify(j)
          },
          success: function(data)
          {
            if (data.RESULTSET === null || data.RESULTSET === undefined)
              $(document).trigger('save-error', {
                'error': data,
                'selector': self.attr['container-body']
              });
            else
            {
              $(document).trigger('save-success', {
                'qname': self.qname,
                'selector': self.attr['container-body']
              });
            }
            $('.collapse').collapse('hide');
          },
          error: function(data)
          {
            $(document).trigger('save-error', {
              'error': data,
              'selector': self.attr['container-body']
            });
          }

        });
      };

      this.copyQuery = function(e, d)
      { // triggered by 'copy' and 'rename'
        // buttons
        this.restoreFooter();
        var self = this, qname = this.qname, query = this.query, 
            action = $(e.target).prop('id').replace('button-','');
        this.select('button-cancel').click();
        this.select('qname-copy').modal('show').data({
          'qname': qname,
          'query': query,
          'action': action
        });

        if (action === 'rename')
        {
          this.select('qname-copy-title').text('Rename query');
          this.select('qname-copy-incl').parent().hide();
        }
        else
        {
          this.select('qname-copy-title').text('Copy query');
          this.select('qname-copy-incl').removeAttr('disabled').removeAttr('checked').parent().show();
        }

        var jParams = _.filter(this.select('nest').data('defaultParams'), {
          TARGET: qname
        });
        if (jParams.length > 0)
        {
          if (action === 'rename')
            self.select('qname-copy-incl').attr('checked', 'checked');
        }
        else
        {
          self.select('qname-copy-incl').removeAttr('checked').attr('disabled', 'disabled');
        }

        var placeholder = action == 'rename' ? 'RENAME' : 'Copy of';
        self.select('qname-copy-input').val(self.app + ' ' + placeholder + ' ' + qname.replace(self.app + ' ', ''));
      };

      this.prepareToCopy = function(e, d)
      { // triggered by 'save' button on
        // copy/rename form
        var self = this;
        var qname = this.select('qname-copy-input').val();
        var query = this.select('qname-copy').data('query');
        var action = this.select('qname-copy').data('action');
        var inclParam = this.select('qname-copy-incl').is(':checked');
        var check = $.ajax({
          type: 'POST',
          data: {
            j:JSON.stringify([{qname:self.attr.q_unique,DATA:[{QNAME:qname,APP:self.app}]}]),
          }
        });
        var resolve = function(data)
        {
          if (data.RESULTSET.ROWS[0].count == 0)
            self.execCopyQuery(qname, query, action, inclParam);
          else
            $(document).trigger('save-error', {
              'error': 'A query with this name already exists',
              'selector': self.attr['qname-copy-body']
            });
        };
        var reject = function(errData)
        {
          $(document).trigger('save-error', {
            'error': errData,
            'selector': self.attr['qname-copy-body']
          });
        };
        check.then(resolve, reject);
      };

      this.execCopyQuery = function(qname, query, action, inclParam)
      {
        var self = this, origQname = this.select('qname-copy').data('qname'), date = new Date(), d = date.getDate() < 10 ? "0"
            + date.getDate()
            : date.getDate(), m = (date.getMonth() + 1) < 10 ? "0" + (date.getMonth() + 1) : (date.getMonth() + 1), y = date
            .getFullYear(), h = date.getHours() < 10 ? "0" + date.getHours() : date.getHours(), M = date.getMinutes() < 10 ? "0"
            + date.getMinutes()
            : date.getMinutes(), s = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds(),

        // update yada_query set qname=?v, query=?v, modified_by=?v,
        // modified=?d where qname=?v and app=?v
        editData = {
          QNAME: qname,
          QUERY: query,
          CREATED_BY: self.loggedUser(),
          CREATED: y + '-' + m + '-' + d + ' ' + h + ':' + M + ':' + s,
          MODIFIED_BY: self.loggedUser(),
          MODIFIED: y + '-' + m + '-' + d + ' ' + h + ':' + M + ':' + s,
          APP: self.app,
          ACCESS_COUNT: 0,
          COMMENTS: ''
        }, j = [ {
          qname: this.attr.q_new_query,
          DATA: [ editData
          ]
        }
        ];
        // j = [{qname:'YADA new query',DATA:[editData]}];

        if (inclParam)
        {
          var jParams = _.filter(this.select('nest').data('defaultParams'), {
            TARGET: origQname
          });
          jParams = _.map(jParams, function(el)
          {
            el.TARGET = qname;
            return el;
          });
          j.push({
            qname: self.attr.q_new_param,
            DATA: jParams
          });
          // j.push({qname:'YADA insert default param',DATA:jParams});
        }

        // save changes
        var submit = $.ajax({
          type: 'POST',
          data: {
            j: JSON.stringify(j)
          }
        });

        var resolve = function(data)
        {
          self.trigger('save-success', {
            'qname': self.qname,
            'selector': self.attr['qname-copy-body']
          });
          if (action === 'rename')
            self.headlessDelete(origQname);
        };
        var reject = function(data)
        {
          self.trigger('save-error', {
            'error': data,
            'selector': self.attr['qname-copy-body']
          });
        };

        submit.then(resolve, reject);
      };

      this.headlessDelete = function(qname)
      {
        var self = this;
        // var j = [{'qname':'YADA delete
        // query','DATA':[{'APP':this.app,'QNAME':qname}]}];
        var j = [ {
          'qname': this.attr.q_delete_query,
          'DATA': [ {
            'APP': this.app,
            'QNAME': qname
          }
          ]
        }
        ];
        var params = _.filter(this.select('nest').data('defaultParams'), {
          TARGET: qname
        });
        if (params.length > 0)
        {
          j.push({
            'qname': self.attr.q_delete_param,
            'DATA': $.extend({},params,{APP:self.app})
          });
        }
        var del = $.ajax({
          type: 'POST',
          data: {
            j: JSON.stringify(j)
          }
        });
        var resolve = function(data)
        {
          self.trigger('app-requested', {
            app: self.app
          });
        };
        var reject = function(data)
        {
          self.trigger('save-error', {
            'error': data,
            'selector': self.attr['qname-copy-body']
          });
        }
        del.then(resolve, reject);
      };

      this.deleteQuery = function()
      { // triggered by 'delete' button
        if (confirm('Are you sure you want to delete the query "' + this.qname + '"?'))
        {
          this.edit = 'delete';
          this.saveQuery();
        }
        else
        {
          return false;
        }
      };

      this.setQname = function()
      {
        this.qname = this.select('qname').val();
      };

      this.error = function(e, d)
      {
        var msg = 'There was a problem saving your query.';
        var details = '';
        this.edit = '';

        if (d.error.responseJSON.Exception != undefined && d.error.responseJSON.StackTrace != undefined)
        {
          details = '<div id="error-details">';
          if (d.error.responseJSON.Exception != undefined)
            details += "<br/>Exception:" + d.error.responseJSON.Exception;
          if (d.error.responseJSON.StackTrace != undefined)
          {
            for (var i = 0; i < 5; i++)
            {
              details += "<br/>" + d.error.responseJSON.StackTrace[i];
            }
            details += "<br/>...";
          }
          details += '</div>';
          msg += ' <a href="#" id="view-error-details" class="alert-link">View Details</a>'

        }
        else if (typeof d.error == 'string')
        {
          msg += ' ' + d.error;
        }
        var html = '<div class="alert alert-danger" role="alert">';
        html += '<strong>Uh Oh!</strong> ' + msg + '</div>';
        $(html).prependTo(d.selector);
        $(details).appendTo('.alert').hide();
        this.alertFooter();
      };

      this.alertFooter = function()
      {
        this.select('button').hide();
        this.select('cancel-button').text('Close').show();
      };

      this.restoreFooter = function()
      {
        this.select('cancel-button').text('Cancel');
        this.select('button').show();
      };

      this.viewErrorDetails = function(e, d)
      {
        this.select('error-details').show();
      };

      this.saveSuccess = function(e, d)
      {
        var action = this.edit == 'copy' ? 'copied' : this.edit == 'new' ? 'saved' : this.edit + 'd';
        this.edit = '';
        var msg = 'Query <span style="font-family:Monaco, monospace"><mark>' + d.qname + '</mark></span> was '
            + action + ' successfully.'
        var html = '<div class="alert alert-success" role="alert">';
        html += '<strong>Hooray!</strong> ' + msg + '</div>';
        $(html).prependTo(d.selector);
        this.alertFooter();
        this.select('query-table').DataTable().ajax.reload();
      };

      this.toggleView = function(e, d)
      {
        var table = this.select('query-table').DataTable();
        table.column(2).visible(!table.column(2).visible());
        table.column(3).visible(!table.column(3).visible());
      };

      this.backup = function(e, d)
      {
        var data = $('#query-table').DataTable().data();
        var len = data.length;
        var queries = _.values(data).slice(0, len); // TODO might have to
        // transform date
        var params = this.select('nest').data('defaultParams');
        // var jp = [{"qname":"YADA new query","DATA":queries}];
        var jp = [ {
          "qname": this.attr.q_new_query,
          "DATA": queries
        }
        ];
        if (params.length > 0)
        {
          jp.push({
            "qname": this.attr.q_new_param,
            "DATA": params
          });
          // jp.push({"qname":"YADA insert default param","DATA":params});
        }
        var json = JSON.stringify(jp);
        var blob = new Blob([ json
        ], {
          type: "application/json"
        });
        var url = URL.createObjectURL(blob);

        var a = document.createElement('a');
        a.id = "backup-link";
        a.download = "YADA_" + this.app + "_backup.json";
        a.href = url;
        a.textContent = " ";
        document.getElementsByClassName('nest')[0].appendChild(a);
        var evt = new MouseEvent('click', {
          view: window,
          bubbles: true,
          cancelable: true
        });
        a.dispatchEvent(evt);
      };
      
      
      // Security to dos:

      //TODO Security wizard documentation
      //TODO Security wizard tooltips
      //TODO Security wizard links to docs

      this.policyTypeChangeHandler = function(e, d)
      {
        var U_def = 'auth.path.rx=^(https?:\/\/)?.+$', 
            U_rx = /auth.path.rx=.+/, 
            T_def = 'No argument required', 
            EC_def = 'execution.policy.columns=', 
            EI_def = 'execution.policy.indices=', 
            EI_rx = /execution.policy.(indices|columns)=.*/, 
            C_def = 'content.policy.predicate=', 
            C_rx = 'content.policy.predicate=.+', 
            $tgt = $(e.target), 
            type = $tgt.val(), 
            $polGrp = $tgt.parents(this.attr['policy-group']),
            $argstr = $polGrp.find(this.attr['arg-string']), 
            $textarea = $tgt.parents('.row').find('textarea'), 
            $secOpt = $tgt.parents(this.attr['security-options']),
            config = "", 
            rx;

        $textarea.removeAttr('disabled');
        if (type === "U")
        {
          config = U_def;
          rx = U_rx;
        }
        else if (type === "T")
        {
          $textarea.attr('disabled', 'disabled');
          config = T_def;
        }
        else if (type === "EC")
        {
          config = EC_def;
          rx = EI_rx;
        }
        else if (type === "EI")
        {
          config = EI_def;
          rx = EI_rx;
          
        }
        else if (type === "C")
        {
          config = C_def;
          rx = C_rx;
        }
        $textarea.val(config);
        if(type == 'EC' || type == 'EI')
        {
          // add field to security option
          this.policyAddProtector($secOpt);
          $secOpt.find(this.attr['policy-protector']).data('status','new');
        }
        
        if(type !== "T")
          this.policyUpdateArgString($argstr);
      };
      

      this.policyUpdateArgString = function($argstr)
      {
        var $polGrp = $argstr.parents('.policy-group');
        var E_rx = /^execution\.policy\..+$/;
        var C_rx = /^content\.policy\..+$/;
        var hasExecutionPolicy = false;
        var hasContentPolicy = false;
        var i = 0;
        var args = "";
        $polGrp.find('textarea').each(function()
        {
          if (i > 0)
            args += ",";
          var arg = $(this).val();
          args += arg;
          i = i + 1;
          if (E_rx.test(arg))
            hasExecutionPolicy = true;
          else if (C_rx.test(arg))
            hasContentPolicy = true;
        });
        if (hasExecutionPolicy && !hasContentPolicy)
          args += ",content.policy=void";
        else if (!hasExecutionPolicy && hasContentPolicy)
          args += ",execution.policy=void";

        $argstr.text(args.replace(/No argument required,?/,""));
      };

      this.policyArgChangeHandler = function(e, d)
      {
        var args = "", 
            i = 0, 
            $tgt = $(e.target), 
            $polGrp = $tgt.parents(this.attr['policy-group']), 
            $argStr = $polGrp.find(this.attr['arg-string']);
        this.policyUpdateArgString($argStr);
      };

      this.policyActionChangeHandler = function(e, d)
      {
        var self = this,
            $tgt = $(e.target), 
            action = $tgt.val(), 
            $polGrp = $tgt.parents(this.attr['policy-group']),
            $argstr = $polGrp.find(this.attr['arg-string']);
        
        if (action === 'save')
        {
          var id      = $polGrp.find(this.attr['policy-id']).val();
          var target  = this.select('qname').val();
          var name    = 'pl';
          var val     = $polGrp.find(this.attr['policy-plugin']).val() + "," + $argstr.text();
          var rule    = 1;
          var params  = {ID:id,TARGET:target,NAME:name,VALUE:val,RULE:rule};
          var qaction = 'insert';
          var protectors_new = [];
          var protectors_update = [];
          var protectors_delete = [];
          var j = [];

          // are we updating or inserting (default)?
          
          //  get the parameter properties to find out
          var plParam = _.filter(self.select('nest').data('defaultParams'),{
            TARGET:target,NAME:name
          });
          var plProp  = _.each(plParam,function(p) {_.filter(self.select('nest').data('properties'),{
            TARGET:p.TARGET+'-'+p.ID, NAME:'protected', VALUE:'true'
          });});

          // if plProp > 0 it means there are parameter-targeted properties in the db.
          // this means we've saved at least once already
          if(plProp.length > 0)
          {
            qaction = 'update';
          }
          
          // jsonparams (jp) data object to insert or update the parameter
          j.push({qname:'YADA '+qaction+' default param',DATA:[params]});
          
          if(qaction == 'insert')
          {
            // jp data object to insert param-targeted properties
            var propData = [{TARGET:self.qname+'-'+id,NAME:'protected',VALUE:'true',APP:self.app}];
            
            // do we need to insert the query properties too?
            var qProp   = _.filter(self.select('nest').data('properties'),{
              TARGET:target, NAME:'protected', VALUE:'true'
            });
            if(qProp.length == 0)
              propData.push({TARGET:target, NAME:'protected', VALUE:'true',APP:self.app});
            
            j.push({qname:self.attr.q_new_prop,DATA:propData});
          }
          
          // do we need to insert/update/delete a protector query?
          self.select('policy-protector-group').each(function(el) {
            var $this  = $(this);
            var $prot  = $this.find(self.attr['policy-protector']);
            var qname  = $prot.val()
            var type   = $this.find(self.attr['policy-protector-type']).val()
            var status = $prot.data('status');
            if(status == 'new')
              protectors_new.push({TARGET:self.qname,QNAME:qname,TYPE:type});
            else
              protectors_update.push({TARGET:self.qname,QNAME:qname,TYPE:type});
          });
          
          protectors_delete = self.select('nest').data('protector-obsolete'); 
          if(protectors_delete != null && protectors_delete.length > 0)
          {
            j.push({qname:self.attr.q_delete_protector,DATA:protectors_delete});
          }
          
          if(protectors_new.length > 0)
            j.push({qname:self.attr.q_new_protector,DATA:protectors_new});
          if(protectors_update.length > 0)
            j.push({qname:self.attr.q_update_protector,DATA:protectors_update});
          //TODO we might have an issue here with the update query as qname appears twice, 
          //     but it is the same value for both so probably not an issue
          
          $.ajax({
            type:'POST',
            data:{
              j:JSON.stringify(j)
            },
            success: function(data) {
              $.when(self.getDefaultParams(),self.getProperties())
               .then(function(a,b) {
                   self.trigger(self.select('params'), 'show-params', {
                   qname: self.qname,
                   params: _.filter(self.select('nest').data('defaultParams'),{TARGET:self.qname})
                 });
                 self.select('secure-checkbox').prop('checked','checked');//.addClass('disabled');
             });
            }
          });
        }
        else if (action === 'remove')
        {
          $polGrp.find('input.remove-policy:checked').parents(this.attr['security-options']).each(function(el) {
            var $this = $(this);
            var protector = $this.find(self.attr['policy-protector']);
            if(protector.length > 0)
            {
              protectors_delete = self.select('nest').data('protector-obsolete');
              if(protectors_delete == null)
              {
                protectors_delete = [];
                self.select('nest').data('protector-obsolete',protectors_delete);
              }
              protectors_delete.push({TARGET:self.qname,QNAME:protector.val()});
            }
            $this.remove();
          });
          
          if ($polGrp.find(this.attr['security-options']).length == 0)
            $polGrp.remove();
          if (this.select('security-options').length == 0)
            this.select('security-panel').append(this.policyGroup);
          this.policyUpdateArgString($argstr);
        }
        else if (action === 'add-same')
        {
          $polGrp.append(this.securityOptions);
          this.select('remove-policy').removeAttr('disabled');
        }
        else if (action === 'add-new')
        {
          this.select('remove-policy').removeAttr('disabled');
          $polGrp.parent().append(this.policyGroup);
        }
        $tgt.val($tgt.find('option:first-child').val());
      };

      this.policySecureCkbxChangeHandler = function(e, d)
      {
        var self = this;
        if (this.select('secure-ckbx').is(':checked'))
        {
          var prop = _.filter(this.select('nest').data('properties'), {
            TARGET: this.qname,
            NAME: 'protected',
            VALUE: 'true'
          });
          if (prop.length == 0) // no property exists yet
          {
            $.ajax({
              type: 'POST',
              data: {
                j:JSON.stringify([{qname:self.attr.q_new_prop,DATA:[{TARGET:self.qname,NAME:'protected',VALUE:'true',APP:self.app}]}]),
              },
              success: function(data) {
                self.getProperties();
              }
            });
          }
        }
        else
        {
          var prop = _.filter(this.select('nest').data('properties'), function(p) {
            return p.TARGET.match(self.qname) != null && p.NAME == 'protected';
          });
          if (prop.length > 0)
          {
            $.ajax({
              type: 'POST',
              data: {
                j:JSON.stringify([{qname:this.attr.q_delete_prop,DATA:[{TARGET:self.qname,NAME:'protected',VALUE:'true',APP:self.app}]}]),
              },
              success: function(data) {
                self.getProperties();
              }
            });
          }
        }
        
      };
      
      this.policySecureAppCkbxChangeHandler = function(e, d) 
      {
        var self = this;
        if (this.select('secure-app-ckbx').is(':checked'))
        {
          var prop = _.filter(this.select('nest').data('properties'), {
            TARGET: self.app,
            NAME: 'protected',
            VALUE: 'true'
          });
          if (prop.length == 0)
          {
            $.ajax({
              type: 'POST',
              data: {
                j:JSON.stringify([{qname:self.attr.q_new_prop,DATA:[{TARGET:self.app,NAME:'protected',VALUE:'true',APP:self.app}]}]),
              },
              success: function(data) {
                self.getProperties();
              }
            });
          }
        }
        else
        {
          var prop = _.filter(this.select('nest').data('properties'), function(p) {
            return p.TARGET.match(self.app) != null && p.NAME == 'protected';
          });
          if (prop.length > 0)
          {
            $.ajax({
              type: 'POST',
              data: {
                j:JSON.stringify([{qname:this.attr.q_delete_prop,DATA:[{TARGET:self.app,NAME:'protected',VALUE:'true',APP:self.app}]}]),
              },
              success: function(data) {
                self.getProperties();
              }
            });
          }
        }
      }
      
      this.policyUpdateSecurityPanel = function(e,d) {
        var self = this;
        
        this.select('policy-group').remove();
        this.select('security-panel').append(this.policyGroup);
        
        $.when(self.getDefaultParams(),self.getProperties())
        .then(function(a,b) {
            var defaultParams = self.select('nest').data('defaultParams');
            self.trigger(self.select('params'), 'show-params', {
              qname: self.qname,
              params: _.filter(defaultParams,{TARGET:self.qname})
            });
            var params = _.filter(defaultParams,{TARGET:self.qname});
            self.policyPopulateSecurityPanel(params);
        });
      };
      
      this.policyPopulateSecurityPanel = function(params)
      {
        this.select('secure-app-ckbx-lbl').text("Mark all '"+this.app+"' queries as secure:");
        var self = this;
        var props = [];
        var properties = this.select('nest').data('properties');
        
        // secure queries for app ckbx
        if(_.filter(properties,{
          TARGET:self.app,
          NAME:'protected',
          VALUE:'true'
         }).length > 0)
          self.select('secure-app-ckbx').prop('checked', 'checked');
        else
          self.select('secure-ckbx').removeProp('checked');
        
        // secure query ckbx
        if(_.filter(properties,{
          TARGET:self.select('qname').val(),
          NAME:'protected',
          VALUE:'true'
        }).length > 0)
        {
          self.select('secure-ckbx').prop('checked', 'checked');//.addClass('disabled');
        }
        else
          self.select('secure-ckbx').removeProp('checked');
        
        if(params.length > 0)
        {

          // iter over qname params
          _.each(params, function(param)
          {
            // props for qname params
            _.each(_.filter(properties, {
                TARGET: param.TARGET + '-' + param.ID,
                NAME: 'protected',
                VALUE: 'true'
              }), function(prop) { props.push(prop); }
            );
          });
        }
        
        if (props.length == 0) // one property per plugin config
        {
          var table = $.fn.DataTable.isDataTable( '#default-params' ) ? self.select('params').DataTable() : null;
          var idCol = table !== null ? table.column('ID:name') : null; 
          var id    = idCol !== null && idCol.length > 0 ? idCol.data().sort().reverse()[0] + 1 : 1;
          this.select('policy-id').val(id);
        }
        else
        {
          _.each(props, function(prop) {
            var fields = prop.TARGET.split('-');
            var param = _.filter(params, {
              ID: fields[1],
              TARGET: fields[0]
            });
            var plugIndex = 0;
            var plugCfg, args, plugin;
            _.each(param, function(p) {  // only security params are in this array
              if(p.NAME == 'pl')
              {
                var polGrp = $(self.attr['policy-group']+':eq('+plugIndex+')');
                var argStr = polGrp.find(self.attr['arg-string']);

                // handle args
                plugCfg = p.VALUE.split(",");
                plugin  = plugCfg[0];
                args    = plugCfg.slice(1);
                var typeIndex = 0;
                
                // set id field
                $(self.attr['policy-id']+':eq('+plugIndex+')').val(p.ID);
                
                // set plugin field
                $(self.attr['policy-plugin']+':eq('+plugIndex+')').val(plugin);
                
                // set arg dropdowns
                for(var i=0;i<args.length;i++)
                {
                  
                  // handle to security options
                  var polTypes =  polGrp.find(self.attr['policy-type']);
                  
                  // current option var
                  var type     = '';
                  var skip     = false;
                      
                  // parse the arg array and populate
                  var arg = args[i].split('=');
                  if(arg[0] == 'auth.path.rx')
                  {
                    type = "U";
                  }
                  else if(arg[0] == 'execution.policy.columns')
                  {
                    type = "EC";
                  }
                  else if(arg[0] == 'execution.policy.indices')
                  {
                    type = "EI";
                  }
                  else if(arg[0] == 'content.policy.predicate')
                  {
                    type = "C";
                  }
                  //else if(arg[1] == 'void')
                  else
                  {
                    skip = true;
                  }

                  if(!skip)
                  {
                    // add a new policy type div if necessary
                    if(typeIndex + 1 > polTypes.length)
                      polGrp.append(self.securityOptions);
                    
                    polGrp.find(self.attr['policy-type']).eq(typeIndex).val(type);
                    polGrp.find(self.attr['policy-arg']).eq(typeIndex).val(args[i]);
                    
                    // YADA_A11N integration
                    if(type == 'EC' || type == 'EI')
                    {
                      // add field to security option
                      self.policyAddProtector(polGrp.find(self.attr['security-options']).eq(typeIndex));
                      // get protector query
                      var prot = self.select('nest').data('protectors')[0];
                      if(prot != null)
                      {
                        // populate fields
                        polGrp.find(self.attr['policy-protector']).eq(typeIndex).val(prot.QNAME);
                        polGrp.find(self.attr['policy-protector-type']).eq(typeIndex).val(prot.TYPE);
                        // set status (insert/update)
                        polGrp.find(self.attr['policy-protector']).eq(typeIndex).data('status','update');
                      }
                      else
                        polGrp.find(self.attr['policy-protector']).eq(typeIndex).data('status','new');
                    }
                    typeIndex = typeIndex + 1;
                  }
                }
                self.policyUpdateArgString(argStr);
                plugIndex = plugIndex + 1;
              }
               
              
            })
          }); 
        }
      };
      
      this.policyAddProtector = function(securityOption) 
      {
        if(securityOption.find('.policy-protector-group').length == 0)
        {
          var protectorHtml = 
            '<div class="row">'+
            ' <div>'+
            '  <div class="policy-protector-group">'+
            '   <div class="col-sm-9">'+
            '    <label for="policy-protector">Protector Qname</label>'+
            '    <input type="text" class="policy-protector form-control" placeholder="Enter protector qname..."/>'+
            '   </div>'+
            '   <div class="col-sm-3 form-group">'+
            '    <label for="policy-protector-type">Type</label>'+
            '    <select class="policy-protector-type form-control">'+
            '     <option value="whitelist" selected>whitelist</option>'+
            '     <option value="blacklist">blacklist</option>'+
            '    </select>'+
            '   </div>'+
            '  </div>'+
            ' </div>'+
            '</div>';
          securityOption.append(protectorHtml);
        }
      }

      this.defaultAttrs({
        'q_queries': 'YADA queries',
        'q_unique': 'YADA check uniqueness',
        'q_params': 'YADA select default params for app',
        'q_props': 'YADA select props like target',
        'q_protectors': 'YADA select protectors for target',
        'q_new_query': 'YADA new query',
        'q_new_param': 'YADA insert default param',
        'q_new_prop': 'YADA insert prop',
        'q_new_protector': 'YADA insert protector for target',
        'q_update_protector': 'YADA update protector for target',
        'q_update_query': 'YADA update query',
        'q_delete_query': 'YADA delete query',
        'q_delete_param': 'YADA delete default param',
        'q_delete_prop' : 'YADA delete prop',
        'q_delete_protector': 'YADA delete protector for target',
        'policy-plugin' : '.policy-plugin',
        'policy-id'     : '.policy-id',
        'policy-type': '.policy-type',
        'policy-action': '.policy-action',
        'policy-protector': '.policy-protector',
        'policy-protector-type':'.policy-protector-type',
        'policy-protector-group':'.policy-protector-group',
        'arg-string': '.arg-string',
        'policy-arg': '.policy-arg',
        'policy-group': '.policy-group',
        'remove-policy': '.remove-policy',
        'security-panel': '#collapseTwo .panel-body',
        'security-options': '.security-options',
        'secure-ckbx': '#secure-query-ckbx',
        'secure-app-ckbx' : '#secure-app-ckbx',
        'secure-app-ckbx-lbl' : 'label[for="secure-app-ckbx"]',
        'params': '#default-params',
        'params-wrapper': '#default-params_wrapper',
        'nest': '.nest',
        'new-query': '#new-query',
        'toggle-view': '#toggle-view',
        'backup': '#backup',
        'button': '.btn',
        'has-alert': '.has-alert',
        'alert': '.alert',
        'cancel-button': '.btn-call-to-action',
        'button-save': '#query-editor-container .btn-primary',
        'button-delete': '#query-editor-container .btn-danger',
        'button-cancel': '#query-editor-container .btn-call-to-action',
        'button-copy': '#query-editor-container #button-copy',
        'button-rename': '#query-editor-container #button-rename',
        'panel': '.panel:gt(0)',
        'container': '#query-editor-container',
        'container-title': '#query-editor-container .modal-title',
        'container-body': '#query-editor-container .modal-body',
        'query-table'       : '#query-table',
        'query-table-body'  : '#query-table tbody',
        'qname'             : '#query-name',
        'qname-box'         : '#query-name-box',
        'query-comments'    : '#query-comments',
        'clipboard'         : 'span.glyphicon-copy',
        'qname-copy'        : '#qname-copy',
        'qname-copy-input'  : '#uniq-name',
        'qname-copy-incl'   : '#incl-param',
        'qname-copy-save'   : '#qname-copy .btn-primary',
        'qname-copy-cancel' : '#qname-copy .btn-call-to-action',
        'qname-copy-title'  : '#qname-copy .modal-title',
        'qname-copy-body'   : '#qname-copy .modal-body',
        'view-error-details': '#view-error-details',
        'error-details'     : '#error-details',
        'clipboard-editor'  : '#query-code-copy',
        'code'              : '#query-editor textarea',
        'tooltip'           : '#query-editor-container [data-toggle="tooltip"]'
      });

      this.after('initialize', function()
      {
        var self = this;
        this.app = this.$node.data('app');
        this.enrich();
        // event handlers
        this.on('change', {
          'policy-type': this.policyTypeChangeHandler,
          'policy-action': this.policyActionChangeHandler,
          'secure-ckbx': this.policySecureCkbxChangeHandler,
          'secure-app-ckbx': this.policySecureAppCkbxChangeHandler
        });
        this.on('keyup', {
          'policy-arg': this.policyArgChangeHandler
        });
        this.on('update-security-panel',this.policyUpdateSecurityPanel);
        this.on('app-requested', this.reloadTable);
        this.on('view.ya.query-table', this.refresh);
        this.on('destroy.ya.query-table', this.destroy);
        this.on('hide.bs.modal', this.clearQuery);
        this.on('save-error', this.error);
        this.on('save-success', this.saveSuccess);
        this.on('shown.bs.modal', this.addQuery);
        this.on('xhr.dt', this.getAdditionalAttributes);
        this.on('click', {
          'backup': this.backup,
          'toggle-view': this.toggleView,
          'query-table-body': this.editQuery,
          'button-save': this.saveQuery,
          'button-delete': this.deleteQuery,
          'button-copy': this.copyQuery,
          'button-rename': this.copyQuery,
          'qname-copy-save': this.prepareToCopy,
          'view-error-details': this.viewErrorDetails
        });
        this.on('focusout', {
          'qname': this.setQname
        });
        Params.attachTo(this.select('params'));
      });
    }
  }
);
