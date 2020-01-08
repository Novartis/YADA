import * as util from '../../support/utils.js'

describe('Login', function() {
  it('logs in and sets cookies', function() {
    util.login()
  })
})

context.only('Create Query', function() {
  let count = 0

  before(() => { cy.cleanYADAIndex() })

  beforeEach(() => {
    // reload
    util.visit()
    util.createApp(0).then(() => {
      cy.get('body').type('{meta}S').wait(1000).then(val => {
        util.getAppsTab().click().then(() => {
          cy.get('#app-list').contains('.applistitem',`CYP0`)
          .then($el => {cy.wrap($el[0]).click()})
          .then(() => { util.createQuery(count)})  // all queries for CYP0
        })
      })
    })
  })

  afterEach(() => { count++ })

  after(() => { cy.cleanYADAIndex() })

  it('Adds Query and Saves by Click', function() {
    util.chooseMenuOption('Save').then(val => {
      cy.getState().its('unsavedChanges').should('eq',0)
      cy.getState().its('creating').should('eq',false)
      cy.get('.background.unsaved').should('not.exist')
      cy.getState().its('qname').then(val => {
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td:first-child()', val.replace(/CYP0 /,''))
          .then($el => {cy.wrap($el[0]).click()})
          .then(() => {
            cy.getState().its('activeTab').should('eq','query-edit-tab')
          })
        })

      })
    })
  })

  it('Adds Query and Saves by Shortcut', function() {
    cy.get('body').type('{meta}S').then(val => {
      cy.getState().its('unsavedChanges').should('eq',0)
      cy.getState().its('creating').should('eq',false)
      cy.get('.background.unsaved').should('not.exist')
      cy.getState().its('qname').then(val => {
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td:first-child()', val.replace(/CYP0 /,''))
          .then($el => {cy.wrap($el[0]).click()})
          .then(() => {
            cy.getState().its('activeTab').should('eq','query-edit-tab')
          })
        })

      })
    })
  })

  it('Adds Query, Clicks to Query List tab, and Saves by Dialog', function() {
    util.getQueryListTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.getState().its('qname').then(val => {
        cy.get('.ui.positive.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','query-list-tab')
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td', val.replace(/CYP0 /,''))
            .then($el => {cy.wrap($el[0]).click()})
            .then(() => {
              cy.getState().its('activeTab').should('eq','query-edit-tab')
            })
          })
        })
      })
    })
  })

  it('Adds Query, Clicks to Config tab, and Saves by Dialog', function() {
    util.getConfTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.getState().its('qname').then(val => {
        cy.get('.ui.positive.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','conf-tab')
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td', val.replace(/CYP0 /,''))
            .then($el => {cy.wrap($el[0]).click()})
            .then(() => {
              cy.getState().its('activeTab').should('eq','query-edit-tab')
            })
          })
        })
      })
    })
  })

  it('Adds Query, Clicks to Apps tab, and Saves by Dialog', function() {
    util.getAppsTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.getState().its('qname').then(val => {
        cy.get('.ui.positive.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','apps-tab')
        cy.get('#app-list').contains('.applistitem',`CYP0`)
        .then($el => {cy.wrap($el[0]).click()})
        .then(() => {
          cy.get('.query-list > tbody > tr').contains('td', val.replace(/CYP0 /,''))
            .then($el => {cy.wrap($el[0]).click()})
            .then(() => {
              cy.getState().its('activeTab').should('eq','query-edit-tab')
            })
          })

        })
      })
    })
  })

  it('Adds Query, Clicks to Query List tab, Declines by Dialog', function() {
    util.getQueryListTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.deny.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','query-list-tab')
        cy.get('.query-list > tbody > tr').contains('td',`QNAME${count}`).should('not.exist')
      })
    })
  })

  it('Adds Query, Clicks to Config tab, Declines by Dialog', function() {
    util.getConfTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.deny.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','conf-tab')
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td',`QNAME${count}`).should('not.exist')
        })
      })
    })
  })

  it('Adds Query, Clicks to Apps tab, Declines by Dialog', function() {
    util.getAppsTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.deny.button').click().then(() => {
        cy.getState().its('activeTab').should('eq','apps-tab')
        cy.get('#app-list').contains('.applistitem',`CYP0`)
        .then($el => {cy.wrap($el[0]).click()})
        .then(() => {
          cy.get('.query-list > tbody > tr').contains('td',`QNAME${count}`).should('not.exist')
        })
      })
    })
  })

})
