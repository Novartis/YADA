import * as util from '../../support/utils.js'

describe('Login', function() {
  it('logs in and sets cookies', function() {
    util.login()
  })
})

context('Create App', function() {

  let count = 0

  before(() => { cy.cleanYADAIndex() })

  beforeEach(() => {
    // reload
    util.visit()
    util.createApp(count)
  })

  afterEach(() => { count++ })

  after(() => { cy.cleanYADAIndex() })

  it('Adds App and Saves by Click', function() {
    util.chooseMenuOption('Save').then(val => {
      cy.getState().its('unsavedChanges').should('eq',0)
      cy.getState().its('creating').should('eq',false)
      cy.get('.background.unsaved').should('not.exist')
      util.getAppsTab().click().then(() => {
        cy.get('#app-list').contains('.applistitem',`CYP${count}`)
        .then($el => {cy.wrap($el[0]).click()})
        .then(() => {
          cy.getState().its('activeTab').should('eq','query-list-tab')
          util.confirmConfig(count)
        })
      })
    })
  })

  it('Adds App and Saves by Shortcut', function() {
    cy.get('body').type('{meta}S').then(val => {
      cy.getState().its('unsavedChanges').should('eq',0)
      cy.getState().its('creating').should('eq',false)
      cy.get('.background.unsaved').should('not.exist')
      util.getAppsTab().click().then(() => {
        cy.get('#app-list').contains('.applistitem',`CYP${count}`)
        .then($el => {cy.wrap($el[0]).click()})
        .then(() => {
          cy.getState().its('activeTab').should('eq','query-list-tab')
          util.confirmConfig(count)
        })
      })
    })
  })

  it('Adds App, Clicks to Apps tab, and Saves by Dialog', function() {
    util.getAppsTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.positive.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','apps-tab')
        cy.get('#app-list').contains('.applistitem',`CYP${count}`)
        .then($el => {cy.wrap($el[0]).click()})
        .then(() => {
          cy.getState().its('activeTab').should('eq','query-list-tab')
          util.confirmConfig(count)
        })
      })
    })
  })


  it('Adds App, Clicks to Apps tab, Declines by Dialog', function() {
    util.getAppsTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.deny.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','apps-tab')
        cy.get('#app-list').contains('.applistitem',`CYP${count}`).should('not.exist')
      })
    })
  })

  it('Creates then Mods App and Saves by Shortcut', function() {
    // save app
    cy.get('body').type('{meta}S').then(val => {
      // assert state
      cy.getState().its('unsavedChanges').should('eq',0)
      cy.getState().its('creating').should('eq',false)
      cy.get('.background.unsaved').should('not.exist')
      // go to apps tab
      util.getAppsTab().click().then(() => {
        // go to CYP app
        cy.get('#app-list').contains('.applistitem',`CYP${count}`)
        .then($el => {cy.wrap($el[0]).click()})
        .then(() => {
          // assert state
          cy.getState().its('activeTab').should('eq','query-list-tab')
          // go to conf tab and assert state
          util.confirmConfig(count)
          // mod fields
          cy.get('#conf-panel .CodeMirror textarea').type(` #CYP${count} Configuration mods test`,{force:true})
          cy.get('input[name="name"]').clear().type(`CYP${count} Name mods`)
          cy.get('input[name="descr"]').clear().type(`CYP${count} Description mods test`)
          // save changes
          cy.get('body').type('{meta}S').then(() => {
            // assert state
            cy.wait(500).getState().its('unsavedChanges').should('eq',0)
            cy.getState().its('creating').should('eq',false)
            cy.get('.background.unsaved').should('not.exist')
            // go to apps tab
            util.getAppsTab().click().then(() => {
              // go to CYP app
              cy.get('#app-list').contains('.applistitem',`CYP${count}`).click().then(() => {
                // go to conf tab
                util.getConfTab().click().then(() => {
                  // assert state
                  cy.get('#conf-panel .CodeMirror',{timeout:10000}).contains('pre',`#CYP${count} Configuration content test #CYP${count} Configuration mods test`).should('exist')
                  cy.get('input[name="app"]').should('have.value',`CYP${count}`)
                  cy.get('input[name="name"]').should('have.value',`CYP${count} Name mods`)
                  cy.get('input[name="descr"]').should('have.value',`CYP${count} Description mods test`)
                })
              })
            })
          })
        })
      })
    })
  })
})
