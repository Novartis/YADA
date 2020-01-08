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
})
