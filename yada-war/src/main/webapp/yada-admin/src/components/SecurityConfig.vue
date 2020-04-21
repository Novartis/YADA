<template>
  <div class="ui list security config">
    <div class="item">
      <i class="plug icon"></i>
      <div class="content">
        <div class="header">Security Plugin <i class="question circle outline icon"></i></div>
        <div class="description hidden">
          A preprocessor plugin is a java class or script accessible to the YADA
          server and compliant with the Plugin API. A preprocessor plugin will
          execute after a YADA request is deconstructed and its query or queries
          are prepared for execution, but before the queries are executed.

          Preproccesor plugins can be attached to a request or query.
          <span style="font-style:italic">Security-related preprocessor plugins are attached to queries only.</span>

          As alluded to above, the security preprocessor plugin api has five
          methods protecting queries from unauthorized execution and for
          pre-filtering data by amending queries before execution:

          <ul>
            <li><code>validateURL</code> for url pattern matching, to limit access to requests <em>for</em> authorized URLs (e.g., with secure gateways)</li>
            <li><code>validateToken</code> for authentication token retrieval and verification</li>
            <li><code>authorize</code> to prohibit unauthorized query execution primarily with token-validated grants, claims, etc.</li>
            <li><code>applyExecutionPolicy</code> to prohibit unauthorized query execution using protector queries, data, or other arbitrary methedo </li>
            <li><code>applyContentPolicy</code> to apply "row level filtering" by amending query criteria (i.e., where clauses, dynamic predicate amendment) before execution</li>
          </ul>

          The specifics of your security plugin implementation will determine how to enter the configuration data in this form.
        </div>
        <div class="ui input security">
          <input type="text"
                 name="plugin"
                 placeholder="FQCN or local classname, e.g., Gatekeeper, com.novartis.opensource.yada.plugin.Gatekeeper..."
                 :value="secParam['plugin']"
                 @input="modify"
                 @focusout="updateModel"/>
        </div>
      </div>
    </div>
    <div class="item">
      <i class="globe icon"></i>
      <div class="content">
        <div class="header">Authorized URL Pattern <i class="question circle outline icon"></i></div>
        <div class="description hidden">
          A regular expression which the requesting URL must match to allow query execution
        </div>
        <div class="ui input security">
          <input type="text"
                 name="auth.path.rx"
                 placeholder="Regular Expression..."
                 :value="secParam['auth.path.rx']"
                 @input="modify"
                 @focusout="updateModel"/>
        </div>
      </div>
    </div>
    <div class="item">
      <i class="key icon"></i>
      <div class="content">
        <div class="header">Token Validation <i class="question circle outline icon"></i></div>
        <div class="description hidden">
          Validate a user by comparing an authenication token passed covertly in a request
          with a retrieved value, or retrieve such a token using some
          other credential. The token validation is only configurable as a method
          in a security plugin. A security plugin configuration is required, but no other configuration option is necessary or available in the admin tool.
        </div>
      </div>
    </div>
    <div class="item">
      <i class="lock icon"></i>
      <div class="content">
        <div class="header">Execution Policy <i class="question circle outline icon"></i></div>
        <div class="description hidden">
          Execution policies prevent unauthorized execution of queries directly from
          http requests or from user interfaces.
        </div>
        <div class="list">
          <!-- AUTHORIZATION POLICY -->
          <div class="item">
            <i class="user secret icon"></i>
              <div class="content authorization policy">
              <div class="header">Authorization Policy <i class="question circle outline icon"></i></div>
              <div class="description hidden">
                Authorization policies take the form of qualifier, such as a user role,
                group membership, or claim from a 3rd-party crendential store. The security
                plugin must match the qualifiers in YADA_A11N with those mapped to the user
                in the credential store, for the targeted query.  A successful whitelist
                authorization would return a match. A blacklist authorization would return 0 matches.
              </div>
              <div class="ui form">
                <div class="inline fields">
                  <div class="two wide field">
                    <div class="ui selection dropdown policy authorization-policy">
                      <input type="hidden" name="authorization.policy.type"  :value="secParam['authorization.policy.type']" @input="modify">
                      <i class="dropdown icon"></i>
                      <!-- <div v-if="typeof secParam['authorization.policy.type'] === 'undefined' || secParam['authorization.policy.type'] === ''"
                           class="default text">Policy Type</div> -->
                      <div class="text">{{secParam['authorization.policy.type']}}</div>
                      <div class="menu">
                        <div class="item"
                             :class="{active: secParam['authorization.policy.type'] == '',
                                      disabled: secParam['execution.policy.type'] != '' || secParam['execution.policy.query'] != '' || secParam['execution.policy.config'] != ''}"
                             data-value=""></div>
                          <div class="item"
                               :class="{active: secParam['authorization.policy.type'] == 'whitelist',
                                        disabled: secParam['execution.policy.type'] != '' || secParam['execution.policy.query'] != '' || secParam['execution.policy.config'] != ''}"
                               data-value="whitelist">whitelist</div>
                          <div class="item"
                               :class="{active: secParam['authorization.policy.type'] == 'blacklist',
                                        disabled: secParam['execution.policy.type'] != '' || secParam['execution.policy.query'] != '' || secParam['execution.policy.config'] != ''}"
                               data-value="blacklist">blacklist</div>
                      </div>
                    </div>
                  </div>
                  <div class="seven wide field">
                    <div class="ui input policy">
                      <input v-if="secParam['execution.policy.type'] == '' && secParam['execution.policy.query'] == '' && secParam['execution.policy.config'] == ''"
                             type="text"
                             name="authorization.policy.grant"
                             placeholder="Qualifier..."
                             :value="secParam['authorization.policy.grant']"
                             @input="modify"
                             @focusout="updateModel"/>
                      <input v-else
                             readonly
                             type="text"
                             name="authorization.policy.grant"
                             placeholder="Qualifier..."
                             :value="secParam['authorization.policy.grant']"
                             @input="modify"
                             @focusout="updateModel"/>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- ExECUTION POLICY -->
          <div class="item">
            <i class="user secret icon"></i>
            <div class="content protector policy">
              <div class="header">Protector Policy <i class="question circle outline icon"></i></div>
              <div class="description hidden">
                Protector policies map YADA queries to 'protector' queries. These
                protector queries should return at least one row when used for a
                whitelist, and maximum 0 rows for a blacklist.&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              </div>
              <div class="ui form">
                <div class="inline fields">
                  <div class="two wide field">
                    <div class="ui selection dropdown policy execution-policy">
                      <input type="hidden" name="execution.policy.type" :value="secParam['execution.policy.type']" @input="modify">
                      <i class="dropdown icon"></i>
                      <!-- <div v-if="typeof secParam['execution.policy.type'] === 'undefined' || secParam['execution.policy.type'] === ''"
                           class="default text">Policy Type</div> -->
                      <div class="text">{{secParam['execution.policy.type']}}</div>
                      <div class="menu">
                        <div class="item"
                          :class="{active: secParam['execution.policy.type'] == '', disabled: secParam['authorization.policy.grant'] !== ''}"
                          data-value=""></div>
                        <div class="item"
                          :class="{active: secParam['execution.policy.type'] == 'whitelist', disabled: secParam['authorization.policy.grant'] !== ''}"
                          data-value="whitelist">whitelist</div>
                        <div class="item"
                          :class="{active: secParam['execution.policy.type'] == 'blacklist',  disabled: secParam['authorization.policy.grant'] !== ''}"
                          data-value="blacklist">blacklist</div>
                      </div>
                    </div>
                  </div>
                  <div class="seven wide field">
                    <div class="ui input policy">
                      <input v-if="secParam['authorization.policy.grant'] == '' || !secParam.hasOwnProperty('authorization.policy.grant')"
                             type="text"
                             name="execution.policy.query"
                             placeholder="Protector Query..."
                             :value="secParam['execution.policy.query']"
                             @input="modify"
                             @focusout="updateModel"/>
                      <input v-else
                             readonly
                             class="disabled"
                             type="text"
                             name="execution.policy.query"
                             placeholder="Protector Query..."
                             :value="secParam['execution.policy.query']"
                             @input="modify"
                             @focusout="updateModel"/>
                    </div>
                  </div>
                </div>
                <!-- EXECUTION POLICY CONFIG -->
                <div class="inline fields">
                  <div class="two wide field">

                  </div>
                  <div class="seven wide field right floated protector query config ">
                    <div class="ui input policy">
                      <input v-if="secParam['authorization.policy.grant'] == '' || !secParam.hasOwnProperty('authorization.policy.grant')"
                             type="text"
                             name="execution.policy.config"
                             placeholder="Protecor Query Config..."
                             :value="secParam['execution.policy.config']"
                             @input="modify"
                             @focusout="updateModel"/>
                      <input v-else
                             readonly
                             type="text"
                             name="execution.policy.config"
                             placeholder="Protecor Query Config..."
                             :value="secParam['execution.policy.config']"
                             @input="modify"
                             @focusout="updateModel"/>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="item">
      <i class="file code icon"></i>
      <div class="content">
        <div class="header">Content Policy <i class="question circle outline icon"></i></div>
        <div class="description hidden">
          Content policies are the YADA way to implement row-level security for
          any authorized query execution.  A user may be permitted to execute a
          query, but restricted to only a subset of data. Using a dynamic predicate,
          a query can be modified on-the-fly to retrieve only a privileged subset
          of data. This is more secure than filtering a complete data set, even on
          the server side, after retrieval.

          Content policies consist of SQL where conditions or predicates,
          dynamically applied to existing where clauses, or comprising new ones.

          Content policies can also be applied to REST queries, but not in any
          standardized way.
        </div>
        <div class="ui input security">
          <input type="text"
                 name="content.policy.predicate"
                 placeholder="Content policy..."
                 :value="secParam['content.policy.predicate']"
                 @input="modify"
                 @focusout="updateModel"/>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Vue from 'vue'
import * as types from '../store/vuex-types'
import { mapState } from 'vuex';
export default {
  props: ['rowData'],
  data() {
    return {

    }
  },
  methods: {
    modify: function(e) {
      this.unsaved()
      // this.$store.commit(types.SET_UNSAVEDPARAMS,this.unsavedParams+1)
      // this.updateModel(e)
    },
    updateModel: function(e) {
      let that   = this
      let plugin = document.querySelector('input[name="plugin"]').value
      let authUrl = document.querySelector('input[name="auth.path.rx"]').value
      let authPolType = document.querySelector('input[name="authorization.policy.type"]').value
      let authPolicy  = document.querySelector('input[name="authorization.policy.grant"]').value
      let execPolType = document.querySelector('input[name="execution.policy.type"]').value
      let execPolicyQuery  = document.querySelector('input[name="execution.policy.query"]').value
      let execPolicyConf   = document.querySelector('input[name="execution.policy.config"]').value
      let contentPolicy    = document.querySelector('input[name="content.policy.predicate"]').value
      let conf = {
        "plugin": plugin,
        "auth.path.rx" : authUrl,
        "authorization.policy.grant" : authPolicy,
        "authorization.policy.type" : authPolType,
        "execution.policy.query" : execPolicyQuery,
        "execution.policy.config" : execPolicyConf,
        "execution.policy.type" : execPolType,
        "content.policy.predicate" : contentPolicy
      }
      this.$store.commit(types.SET_SECCONF, conf)
      if(!this.hasSecurityPlugin()
          && !!plugin)
          // && this.secconf !== null
          // && Object.entries(conf).toString() !== Object.entries(this.secconf).toString() )
      {
        this.$store.dispatch(types.ADD_SECPARAM)
        .then(() => {
          this.$store.dispatch(types.MOD_SECPARAM)
          console.log(`create: ${JSON.stringify(this.secconf)}`)
        })
      }
      else
      {
        this.$store.dispatch(types.MOD_SECPARAM)
        console.log(`update: ${JSON.stringify(this.secconf)}`)
      }



    },
    isSecurityPlugin: function(param) {
      return param.SPP === 'true'
    },
    hasSecurityPlugin: function() {
      return this.params.some(p => this.isSecurityPlugin(p))
        || this.renderedParams.some(p => this.isSecurityPlugin(p))
    },
    securityPlugin: function() {
      let spp
      if(typeof this.params !== 'undefined' && this.params.length > 0)
      {
        spp = this.params.filter(p => this.isSecurityPlugin(p))
      }
      if(typeof spp === 'undefined'
         && typeof this.renderedParams !== 'undefined'
         && this.renderedParams.length > 0)
      {
        spp = this.renderedParams.filter(p => this.isSecurityPlugin(p))[0]
      }
      if(typeof spp !== 'undefined')
        return spp[0]

      return spp
    },
    // hasExecutionPolicy: function(secPl) {
    //   return secPl['POLICY'] == 'E'
    //         || secPl.hasOwnProperty('execution.policy.columns')
    //         || secPl.hasOwnProperty('execution.policy.indices')
    //         || secPl.hasOwnProperty('execution.policy.type')
    // },
    // hasAuthorizationPolicy: function(secPl) {
    //   return secPl['POLICY'] == 'A'
    //         || secPl.hasOwnProperty('authorization.policy.grant')
    //         || secPl.hasOwnProperty('authorization.policy.type')
    // }
  },
  computed: {
    ...mapState(['paramlist','renderedParams','qname','unsavedChanges','confirmAction','unsavedParams','secconf']),
    params() { return this.renderedParams },
    secParam() {
      if(this.secconf !== null)
      {
        return this.secconf
      }
      else
      {
        return {
          "plugin": "",
          "auth.path.rx": "",
          "authorization.policy.type": "",
          "authorization.policy.grant": "",
          "execution.policy.type": "",
          "execution.policy.query": "",
          "execution.policy.config": "",
          "content.policy.predicate": ""
        }
      }
    }
    //   const secPl = this.securityPlugin()
    //   if(typeof secPl !== 'undefined')
    //   {
    //     let conf =  secPl['VALUE'].split(/,/).reduce((a,c) => {
    //       if(/=/.test(c))
    //       {
    //         let pair = c.split(/=/)
    //         a[pair[0]] = pair[1]
    //       }
    //       else
    //       {
    //         a['plugin'] = c
    //       }
    //       return a
    //     },{})
    //
    //     if(this.hasExecutionPolicy(conf))
    //     {
    //       conf['execution.policy.type'] = secPl['TYPE']
    //       conf['execution.policy.query'] = secPl['QNAME']
    //     }
    //     else if(secPl['POLICY'] == 'A')
    //     {
    //       conf['authorization.policy.type'] = secPl['TYPE']
    //       conf['authorization.policy.grant'] = secPl['QNAME']
    //     }
    //
    //
    //     console.log(conf)
    //     return conf
    //   }
    //   return {}
    // }
  },
  watch: {

  },
  updated() {
  },
  beforeUpdate() {

  },
  mounted() {
    $('.ui.dropdown.execution-policy').dropdown({'debug':true,
      onChange:function(v,t,c) {
        console.log(v,t,c)
        $('.ui.dropdown.authorization-policy').dropdown('clear')
      }
    })
    $('.ui.dropdown.authorization-policy').dropdown({'debug':true,
      onChange:function(v,t,c) {
        console.log(v,t,c)
        $('.ui.dropdown.execution-policy').dropdown('clear')
      }
    })

    Array.from(document.querySelectorAll('.security.config .header i')).forEach(el => {
      el.addEventListener('click',e => {
        let descr = el.parentElement.nextElementSibling
        if(descr.classList.contains('hidden'))
          descr.classList.remove('hidden')
        else
          descr.classList.add('hidden')
      })
    })
  }

}
</script>
<style>
  code {
    background-color: #EEE;
    padding: 1px 3px;
  }

  .ui.list > .item .description.hidden,
  .ui.list .list > .item .description.hidden {
    color: white;
    max-height: 0; /* setting display:none breaks semantic.css spec, max-height required for transition */
    transition: max-height .5s ease, color 1s ease-in;
  }

  .ui.list > .item .description,
  .ui.list .list > .item .description {
    color: black;
    max-height: 500px;
    transition: max-height .5s ease, color .5s ease-in;
  }

  .ui.selection.dropdown.policy {
    border: 1px solid rgba(34, 36, 38, 0.15);
  }

  .ui.form .field .input.policy {
    padding-left: 10px;
  }

  .ui.input.security {
    width: 56.5%;
  }

  .protector.query.config {
    /* padding-right: 1em; */
  }

  .security.config .description {
    margin-bottom: 7px;
  }

  .security.config .item {
    margin-top: 7px;
  }

  .ui.list .list > .item .header,
  .ui.list > .item .header {
    margin-bottom: 7px !important;
  }
</style>
