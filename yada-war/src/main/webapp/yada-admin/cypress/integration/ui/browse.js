import * as util from '../../support/utils.js'

context('Browse', function () {
  before(() => {
    util.login()
    cy.cleanYADAIndex()
    util.visit()
  })

  after(() => {
    cy.clearCookies()
  })

  describe('Defaults', function () {
    describe('Default Elements', function () {
      beforeEach(() => {
        cy.isInState({activeTab: 'apps-tab'})
      })
      afterEach(() => {
        cy.isInState({activeTab: 'apps-tab'})
      })

      it('has tabs', function () {
        util.getAppsTab().should('exist')
        util.getConfTab().should('exist')
        util.getQueryListTab().should('exist')
        util.getQueryEditTab().should('exist')
      })

      it('has tab panels', function () {
        util.getAppsPanel().should('exist')
        util.getConfPanel().should('exist')
        util.getQueryListPanel().should('exist')
        util.getQueryEditPanel().should('exist')
      })

      it('has menu', function () {
        util.getMainMenu().should('exist')
      })

      it('has visible filter', function () {
        util.getFilter().should('be.visible')
      })

      it('has app list', function () {
        util.getAppListItems().should('have.length.of.at.least', 1)
      })

      it('has correct tabs in disabled state', function () {
        util.getAppsTab().should('not.have.class', 'disabled')
        util.getConfTab().should('have.class', 'disabled')
        util.getQueryListTab().should('have.class', 'disabled')
        util.getQueryEditTab().should('have.class', 'disabled')
      })

      it('has correct filter label', function () {
        util.getFilterLabel().then($label => {
          util.getAppListItems().its('length').should('equal', parseInt($label.text()))
        })
      })

      it('has correct menu options', function () {
        util.hasCorrectMenuItems()
      })
    })

    describe('Default Clicks', function () {
      beforeEach(() => {
        util.visit()
        cy.isInState({activeTab: 'apps-tab'})
      })

      afterEach(() => {
        cy.isInState({activeTab: 'apps-tab'})
      })

      it('has active apps tab', () => {
        util.getAppsTab().click().then($tab => cy.wrap($tab).should('have.class', 'active'))
      })

      it('has inactive conf tab', () => {
        util.getConfTab().click().then($tab => cy.wrap($tab).should('not.have.class', 'active'))
      })

      it('has inactive query list tab', () => {
        util.getQueryListTab().click().then($tab => cy.wrap($tab).should('not.have.class', 'active'))
      })

      it('has inactive query edit tab', () => {
        util.getQueryEditTab().click().then($tab => cy.wrap($tab).should('not.have.class', 'active'))
      })
    })
  })

  describe('Select App', function () {
    describe('App selection', function () {
      beforeEach(() => {
        util.visit()
        cy.isInState({activeTab: 'apps-tab'})
          .then(() => {
            util.getAppListItems().filter(':contains("RESTTEST")').click()
          })
      })

      describe('Confirm query list tab state', function () {
        after(() => {
          cy.isInState({
            activeTab: 'query-list-tab',
            // nextTab:'query-list-tab',
            app: 'RESTTEST',
            queries: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
            config: {
              APP: 'RESTTEST',
              ACTIVE: '1',
              NAME: '',
              DESCR: '',
              CONF: 'http://jsonplaceholder.typicode.com'
            }
          })
        })

        it('has visible filter', function () {
          util.getFilter().should('be.visible')
        })

        it('has correct filter label', function () {
          util.getQueryListItems().then($items => {
            util.getFilterLabel().then($label => {
              cy.wrap($items).its('length').should('equal', parseInt($label.text()))
            })

          // util.getFilterLabel().then($label => {
          //   util.getQueryListItems().its('length').should('equal', parseInt($label.text()))
          // })
          })
        })

        it('has query table', function () {
          util.getQueryListPanel().find('table.query-list').should('exist')
        })

        it('has details', function () {
          util.getQueryListPanel().find('table.query-list > tbody > tr:nth-child(2) > td:nth-child(3)').then($td => {
            cy.getState().then($state => {
              const qname = $state.queries[1].QNAME.toLowerCase().replace(/\s/g, '-')
              cy.wrap($td).find(`#popup-info-${qname}`).should('exist')
              cy.wrap($td).find(`#popup-comments-${qname}`).should('exist')
              cy.wrap($td).find(`#popup-security-${qname}`).should('exist')
            })
          })
        })

        it('has correct menu options', function () {
          util.hasCorrectMenuItems()
        })

        it('has correct tabs in disabled state', function () {
          util.getAppsTab().should('not.have.class', 'disabled')
          util.getConfTab().should('not.have.class', 'disabled')
          util.getQueryListTab().should('not.have.class', 'disabled')
          util.getQueryEditTab().should('have.class', 'disabled')
        })
      })

      describe('Go to config tab', function () {
        after(() => {
          cy.isInState({
            activeTab: 'conf-tab',
            // nextTab:'conf-tab',
            app: 'RESTTEST',
            queries: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
            config: {
              APP: 'RESTTEST',
              ACTIVE: '1',
              NAME: '',
              DESCR: '',
              CONF: 'http://jsonplaceholder.typicode.com'
            }
          })
        })

        beforeEach(() => {
          util.getConfTab().click()
        })

        it('has enabled conf tab', function () {
          util.getConfPanel().should('have.class', 'active')
        })

        it('has readonly name', function () {
          util.getConfPanel().find('input[name="app"]').should('have.attr', 'readonly', 'readonly')
          util.getConfPanel().find('input[name="app"]').invoke('val').should('eq', 'RESTTEST')
        })

        it('is active', function () {
          util.getConfPanel().find('input[name="active"]:checked').should('exist')
        })

        it('has name', function () {
          util.getConfPanel().find('input[name="name"]').invoke('val').should('eq', '')
        })

        it('has description', function () {
          util.getConfPanel().find('input[name="descr"]').invoke('val').should('eq', '')
        })

        it('has a single configuration editor', function () {
          util.getConfPanel().find('.CodeMirror').its('length').should('eq', 1)
        })
      })

      describe('Go to query edit tab (disabled)', function () {
        after(() => {
          cy.isInState({
            activeTab: 'query-list-tab',
            // nextTab:'query-list-tab',
            app: 'RESTTEST',
            queries: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
            config: {
              APP: 'RESTTEST',
              ACTIVE: '1',
              NAME: '',
              DESCR: '',
              CONF: 'http://jsonplaceholder.typicode.com'
            }
          })
        })

        it('has disabled query edit tab', function () {
          util.getQueryEditTab().click()
            .then($tab => cy.wrap($tab).should('not.have.class', 'active'))
        })
      })

      describe('Go to apps tab (reset everything)', function () {
        beforeEach(() => {
          cy.isInState({
            activeTab: 'query-list-tab',
            // nextTab:'query-list-tab',
            app: 'RESTTEST',
            queries: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
            config: {
              APP: 'RESTTEST',
              ACTIVE: '1',
              NAME: '',
              DESCR: '',
              CONF: 'http://jsonplaceholder.typicode.com'
            }
          })
        })

        afterEach(() => {
          cy.isInState({activeTab: 'apps-tab'}) //, nextTab:'apps-tab'})
        })

        it('has active apps tab', function () {
          util.getAppsTab().click()
            .then($tab => cy.wrap($tab).should('have.class', 'active'))
        })

        // check tabs
        it('has correct tabs in disabled state', function () {
          util.getAppsTab().click()
            .then(() => {
              util.getAppsTab().should('not.have.class', 'disabled')
              util.getConfTab().should('have.class', 'disabled')
              util.getQueryListTab().should('have.class', 'disabled')
              util.getQueryEditTab().should('have.class', 'disabled')
            })
        })
      })
    })
  })

  describe('Select App then Query', function () {
    describe('Query Selection then reversion', function () {
      // selects an app then a query
      // each test starts with query edit tab active

      beforeEach(() => {
        util.visit()
        util.getAppListItems().filter(':contains("RESTTEST")').click()
        util.getQueryListItems()
          .then($items => cy.wrap($items[1]).find('td:eq(0)').click().wait(500))
      })

      describe('Query Edit Tab State', function () {
        after(() => {
          cy.isInState({
            activeTab: 'query-edit-tab',
            // nextTab:'query-edit-tab',
            app: 'RESTTEST',
            queries: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
            qname: 'RESTTEST DELETE with param',
            qnameOrig: 'RESTTEST DELETE with param',
            params: [],
            renderedParams: [],
            props: [],
            protectors: [],
            query: {'CREATED_BY': 'UNKNOWN1', 'APP': 'RESTTEST', 'CREATED': '2018-09-15 21:44:34', 'MODIFIED_BY': 'UNKNOWN1', 'QNAME': 'RESTTEST Repository', 'QUERY': '/posts/?v', 'LAST_ACCESS': '2019-05-16 15:35:37.049000', 'DEFAULT_PARAMS': '0', 'COMMENTS': '', 'MODIFIED': '2018-09-18 18:41:42', 'ACCESS_COUNT': '1', 'IS_SECURE': 'f'},
            config: {
              APP: 'RESTTEST',
              ACTIVE: '1',
              NAME: '',
              DESCR: '',
              CONF: 'http://jsonplaceholder.typicode.com'
            }
          })
        })

        // app name
        it('has app name label', function () {
          util.getQueryEditPanel().find('.labeled.input .label').contains('RESTTEST')
        })
        // query name
        it('has read only query name input', function () {
          util.getQueryEditPanel().find('.labeled.input input').should('have.attr', 'readonly', 'readonly')
        })
        // code
        it('has codemirror editing field', function () {
          util.getQueryEditPanel().find('.query-editor .codemirror .CodeMirror').should('exist')
        })
        // comments
        it('has read only comments field', function () {
          util.getQueryEditPanel().find('.comment div').should('have.text', 'This is a comment')
        })
        // params
        it('has params table', function () {
          util.getQueryEditPanel().find('.params table.paramtab').should('exist')
        })

        // security (pfft)
        it('has security wizard', function () {
          util.getQueryEditPanel().find('.security')
        })

        // menues
        it('has correct menu options', function () {
          util.hasCorrectMenuItems()
        })

        // tabs are not disabled
        it('has all tabs enabled', function () {
          util.getAppsTab().should('not.have.class', 'disabled')
          util.getConfTab().should('not.have.class', 'disabled')
          util.getQueryListTab().should('not.have.class', 'disabled')
          util.getQueryEditTab().should('not.have.class', 'disabled')
        })
      })

      describe('Go to query list tab (reset query)', function () {
        after(() => {
          cy.isInState({
            activeTab: 'query-list-tab',
            // nextTab:'query-list-tab',
            app: 'RESTTEST',
            queries: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
            config: {
              APP: 'RESTTEST',
              ACTIVE: '1',
              NAME: '',
              DESCR: '',
              CONF: 'http://jsonplaceholder.typicode.com'
            }
          })
        })

        beforeEach(() => {
          util.getQueryListTab().click()
        })

        // click query list tab
        it('clicks query list tab', function () {
          util.getQueryListTab().should('have.class', 'active')
        })

        // check tabs
        it('has correct tabs in disabled state', function () {
          util.getAppsTab().should('not.have.class', 'disabled')
          util.getConfTab().should('not.have.class', 'disabled')
          util.getQueryListTab().should('not.have.class', 'disabled')
          util.getQueryEditTab().should('have.class', 'disabled')
        })
      })

      describe('Go to config tab (reset query) ', function () {
        after(() => {
          cy.isInState({
            activeTab: 'conf-tab',
            // nextTab:'conf-tab',
            app: 'RESTTEST',
            queries: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
            config: {
              APP: 'RESTTEST',
              ACTIVE: '1',
              NAME: '',
              DESCR: '',
              CONF: 'http://jsonplaceholder.typicode.com'
            }
          })
        })

        beforeEach(() => {
          util.getConfTab().click()
        })

        // click query list tab
        it('clicks config tab', function () {
          util.getConfTab().should('have.class', 'active')
        })

        // check tabs
        it('has correct tabs in disabled state', function () {
          util.getAppsTab().should('not.have.class', 'disabled')
          util.getConfTab().should('not.have.class', 'disabled')
          util.getQueryListTab().should('not.have.class', 'disabled')
          util.getQueryEditTab().should('have.class', 'disabled')
        })
      })

      describe('Go to apps tab (reset everything)', function () {
        after(() => {
          cy.isInState({activeTab: 'apps-tab'}) //, nextTab:'apps-tab'})
        })

        beforeEach(() => {
          util.getAppsTab().click()
        })

        it('has active apps tab', function () {
          util.getAppsTab().should('have.class', 'active')
        })

        // check tabs
        it('has correct tabs in disabled state', function () {
          util.getAppsTab().should('not.have.class', 'disabled')
          util.getConfTab().should('have.class', 'disabled')
          util.getQueryListTab().should('have.class', 'disabled')
          util.getQueryEditTab().should('have.class', 'disabled')
        })
      })
    })
  })
})
