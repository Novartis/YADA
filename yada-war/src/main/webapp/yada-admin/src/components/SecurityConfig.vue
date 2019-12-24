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

          As alluded to above, the security preprocessor plugin api has four
          methods protecting queries from unauthorized execution and for
          pre-filtering data by amending queries before execution:

          <ul>
            <li>validateURL for url pattern matching</li>
            <li>validateToken for token retrieval and verification</li>
            <li>authorize to prohibit unauthorized query execution</li>
            <li>applyExecutionPolicy to prohibit unauthorized query execution</li>
            <li>applyContentPolicy to amend query criteria (i.e., where clauses,) before queries are executed, to enable row-level filtering</li>
          </ul>

          The specifics of your security plugin implementation will determine how to enter the configuration data in this form.
        </div>
        <div class="ui fluid input">
          <input type="text" placeholder="FQCN or local classname, e.g., Gatekeeper, com.novartis.opensource.yada.plugin.Gatekeeper...">
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
        <div class="ui fluid input">
          <input type="text" placeholder="Regular Expression...">
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
          in a security plugin. There is no configuration option available in the admin tool.
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
                    <div class="ui selection dropdown">
                      <input type="hidden" name="gender">
                      <i class="dropdown icon"></i>
                      <div class="default text">Policy Type</div>
                      <div class="menu">
                          <div class="item" data-value="whitelist">Whitelist</div>
                          <div class="item" data-value="blacklist">Blacklist</div>
                      </div>
                    </div>
                  </div>
                  <div class="fourteen wide field">
                    <div class="ui input">
                      <input type="text" placeholder="Qualifier...">
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
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
                    <div class="ui selection dropdown">
                      <input type="hidden" name="gender">
                      <i class="dropdown icon"></i>
                      <div class="default text">Policy Type</div>
                      <div class="menu">
                          <div class="item" data-value="whitelist">Whitelist</div>
                          <div class="item" data-value="blacklist">Blacklist</div>
                      </div>
                    </div>
                  </div>
                  <div class="fourteen wide field">
                    <div class="ui input">
                      <input type="text" placeholder="Protector Query...">
                    </div>
                  </div>
                </div>
                <div class="inline fields">
                  <div class="two wide field">

                  </div>
                  <div class="fourteen wide field right floated protector query config ">
                    <div class="ui input">
                      <input type="text" placeholder="Protecor Query Config...">
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
        <div class="ui fluid input">
          <input type="text" placeholder="Content policy...">
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

  },
  computed: {

  },
  watch: {

  },
  updated() {
  },
  mounted() {
    $('.ui.dropdown').dropdown()

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
