import { visit,
  getAppsPanel,
  getConfPanel,
  getQueryListPanel,
  getQueryEditPanel,
  getAppsTab,
  getConfTab,
  getQueryListTab,
  getQueryEditTab,
  getMainMenu,
  hasVisibleFilter,
  getFilterLabel,
  getAppListItems,
  getQueryListItems,
  getStateValue,
  hasCorrectMenuItems
 } from '../support/utils.js'

describe('Login', function() {
  it('logs into test', function() {

    cy.clearCookies()
    cy.visit('https://yada-test.qdss.io/yada-admin/')

    cy.get('body').then($body => {
      if($body.find('#username').length)
      {
        cy.get('#username').type('dvaron@analgesicsolutions.com')
        cy.get('#password').type('Th3ma0twp.')
        cy.get('.login-form .ui.button').click()

        cy.location().should(loc => {
          expect(loc.pathname).to.eq('/yada-admin/index.html')
        })
      }
      else
      {
        cy.location().should(loc => {
          expect(loc.pathname).to.eq('/yada-admin/')
        })
        cy.get('#app').should('exist')
      }
      cy.getCookie('yadagroups').should('exist')
    })



  })
})

describe('UI Controls', function() {
  before(() => { visit() })


  describe('Default State', function() {

    describe('Default Elements', function() {
      beforeEach(() => {
        cy.isInState({activeTab:'apps-tab'})
      })
      afterEach(() => {
        cy.isInState({activeTab:'apps-tab'})
      })

      it('has tabs', function() {
        getAppsTab().should('exist')
        getConfTab().should('exist')
        getQueryListTab().should('exist')
        getQueryEditTab().should('exist')
      })

      it('has tab panels',function() {
        getAppsPanel().should('exist')
        getConfPanel().should('exist')
        getQueryListPanel().should('exist')
        getQueryEditPanel().should('exist')
      })

      it('has menu', function() {
        getMainMenu().should('exist')
      })

      it('has visible filter', function() {
        hasVisibleFilter().should('be.visible')
      })

      it('has app list', function() {
        getAppListItems().should('have.length.of.at.least',1)
      })

      it('has correct tabs in disabled state', function() {
        getAppsTab().should('not.have.class','disabled')
        getConfTab().should('have.class','disabled')
        getQueryListTab().should('have.class','disabled')
        getQueryEditTab().should('have.class','disabled')
      })

      it('has correct filter label',function() {
        getFilterLabel().then($label => {
          getAppListItems().its('length').should('equal', parseInt($label.text()))
        })
      })

      it('has correct menu options', function() {
        hasCorrectMenuItems()
      })
    })

    describe('Default Clickables', function() {
      beforeEach(() => {
        visit()
        cy.isInState({activeTab:'apps-tab'})
      })

      afterEach(() => {
        cy.isInState({activeTab:'apps-tab'})
      })

      it('has active apps tab', () => {
        getAppsTab().click().then($tab => cy.wrap($tab).should('have.class','active'))
      })

      it('has inactive conf tab', () => {
        getConfTab().click().then($tab => cy.wrap($tab).should('not.have.class','active'))
      })

      it('has inactive query list tab', () => {
        getQueryListTab().click().then($tab => cy.wrap($tab).should('not.have.class','active'))
      })

      it('has inactive query edit tab', () => {
        getQueryEditTab().click().then($tab => cy.wrap($tab).should('not.have.class','active'))
      })
    })
  })



  describe('Choose App', function() {

    describe('App selection', function() {

      beforeEach(() => {
        visit()
        cy.isInState({activeTab:'apps-tab'})
        .then(() => getAppListItems().then($items => $items[2].click()))
      })

      describe('Confirm query list tab state', function() {

        after(() => {
          cy.isInState({
            activeTab:'query-list-tab',
            nextTab:'query-list-tab',
            app:'BB',
            queries: [0,1,2],
            config: {
              APP: 'BB',
              ACTIVE: '1',
              NAME: 'BitBucket',
              DESCR: 'BitBucket API',
              CONF: 'https://api.bitbucket.org/2.0'
            }
          })
        })

        it('has visible filter', function() {
          hasVisibleFilter().should('be.visible')
        })

        it('has correct filter label',function() {
          getFilterLabel().then($label => {
            getQueryListItems().its('length').should('equal', parseInt($label.text()))
          })
        })

        it('has correct menu options', function() {
          hasCorrectMenuItems()
        })

        it('has correct tabs in disabled state', function() {
          getAppsTab().should('not.have.class','disabled')
          getConfTab().should('not.have.class','disabled')
          getQueryListTab().should('not.have.class','disabled')
          getQueryEditTab().should('have.class','disabled')
        })

      })

      describe('Go to config tab', function() {
        after(() => {
          cy.isInState({
            activeTab:'conf-tab',
            nextTab:'conf-tab',
            app:'BB',
            queries: [0,1,2],
            config: {
              APP: 'BB',
              ACTIVE: '1',
              NAME: 'BitBucket',
              DESCR: 'BitBucket API',
              CONF: 'https://api.bitbucket.org/2.0'
            }
          })
        })

        it('has enabled conf tab', function() {
          getConfTab().click()
          .then($tab => cy.wrap($tab).should('have.class','active'))
        })
      })

      describe('Go to query edit tab (disabled)', function() {

        after(() => {
          cy.isInState({
            activeTab:'query-list-tab',
            nextTab:'query-list-tab',
            app:'BB',
            queries: [0,1,2],
            config: {
              APP: 'BB',
              ACTIVE: '1',
              NAME: 'BitBucket',
              DESCR: 'BitBucket API',
              CONF: 'https://api.bitbucket.org/2.0'
            }
          })
        })

        it('has disabled query edit tab', function() {
          getQueryEditTab().click()
          .then($tab => cy.wrap($tab).should('not.have.class','active'))
        })
      })

      describe('Go to apps tab (reset everything)', function() {

        before(() => {
          cy.isInState({
            activeTab:'query-list-tab',
            nextTab:'query-list-tab',
            app:'BB',
            queries: [0,1,2],
            config: {
              APP: 'BB',
              ACTIVE: '1',
              NAME: 'BitBucket',
              DESCR: 'BitBucket API',
              CONF: 'https://api.bitbucket.org/2.0'
            }
          })
        })

        after(() => {
          cy.isInState({activeTab:'apps-tab',nextTab:'apps-tab'})
        })

        it('has active apps tab', function() {
          getAppsTab().click().then($tab => cy.wrap($tab).should('have.class','active'))
        })

        // check tabs
        it('has correct tabs in disabled state', function() {
          getAppsTab().should('not.have.class','disabled')
          getConfTab().should('have.class','disabled')
          getQueryListTab().should('have.class','disabled')
          getQueryEditTab().should('have.class','disabled')
        })
      })
    })
  })

  describe('Choose Query', function() {

    describe('Query Selection then reversion', function() {

      beforeEach(() => {
        visit()
        .then(() => getAppListItems().then($items => $items[2].click()))
        .then(() => getQueryListItems().then($items => $items[2].click()))
      })

      describe('Query Edit Tab State', function() {
        after(() => {
          cy.isInState({
            activeTab:'query-edit-tab',
            nextTab:'query-edit-tab',
            app:'BB',
            queries: [0,1,2],
            qname: 'Repository',
            qnameOrig: 'Repository',
            params: [],
            renderedParams: [],
            props: [],
            protectors: [],
            query: {},
            config: {
              APP: 'BB',
              ACTIVE: '1',
              NAME: 'BitBucket',
              DESCR: 'BitBucket API',
              CONF: 'https://api.bitbucket.org/2.0'
            }
          })
        })

        // app name
        it('has read only app name label', function() {

        })
        // query name
        it('has read only query name input', function() {

        })
        // comments
        it('has read only comments field', function() {

        })
        // params
        it('has params table', function() {

        })
        // security (pfft)

        // menues
        it('has correct menu options', function() {
          hasCorrectMenuItems()
        })

        // tabs are not disabled
        it('has all tabs enabled', function() {
          getAppsTab().should('have.class','disabled')
          getConfTab().should('have.class','disabled')
          getQueryListTab().should('have.class','disabled')
          getQueryEditTab().should('have.class','disabled')
        })

      })

      describe('Go to query list tab (reset query)', function() {

        after(() => {
          cy.isInState({
            activeTab:'query-list-tab',
            nextTab:'query-list-tab',
            app:'BB',
            queries: [0,1,2],
            config: {
              APP: 'BB',
              ACTIVE: '1',
              NAME: 'BitBucket',
              DESCR: 'BitBucket API',
              CONF: 'https://api.bitbucket.org/2.0'
            }
          })
        })

        // click query list tab
        it('clicks query list tab', function() {
          getQueryListTab().click().then($tab => cy.wrap($tab).should('have.class','active'))
        })

        // check tabs
        it('has correct tabs in disabled state', function() {
          getAppsTab().should('not.have.class','disabled')
          getConfTab().should('not.have.class','disabled')
          getQueryListTab().should('not.have.class','disabled')
          getQueryEditTab().should('have.class','disabled')
        })
      })

      describe('Go to config tab (reset query) ', function() {
        after(() => {
          cy.isInState({
            activeTab:'query-list-tab',
            nextTab:'query-list-tab',
            app:'BB',
            queries: [0,1,2],
            config: {
              APP: 'BB',
              ACTIVE: '1',
              NAME: 'BitBucket',
              DESCR: 'BitBucket API',
              CONF: 'https://api.bitbucket.org/2.0'
            }
          })
        })

        // click query list tab
        it('clicks config tab', function() {
          getConfTab().click().then($tab => cy.wrap($tab).should('have.class','active'))
        })

        // check tabs
        it('has correct tabs in disabled state', function() {
          getAppsTab().should('not.have.class','disabled')
          getConfTab().should('not.have.class','disabled')
          getQueryListTab().should('not.have.class','disabled')
          getQueryEditTab().should('have.class','disabled')
        })
      })

      describe('Go to apps tab (reset everything)', function() {

        after(() => {
          cy.isInState({activeTab:'apps-tab',nextTab:'apps-tab'})
        })

        it('has active apps tab', function() {
          getAppsTab().click().then($tab => cy.wrap($tab).should('have.class','active'))
        })

        // check tabs
        it('has correct tabs in disabled state', function() {
          getAppsTab().should('not.have.class','disabled')
          getConfTab().should('have.class','disabled')
          getQueryListTab().should('have.class','disabled')
          getQueryEditTab().should('have.class','disabled')
        })
      })
    })

  })

})
