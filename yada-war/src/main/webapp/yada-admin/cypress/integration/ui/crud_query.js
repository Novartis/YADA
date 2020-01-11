import * as util from '../../support/utils.js'

describe('Login', function() {
  it('logs in and sets cookies', function() {
    util.login()
  })
})

context('Create Query', function() {
  let count = 0

  before(() => {
    cy.cleanYADAIndex()
    util.visit()
    util.createApp(0).then(() => {
      cy.get('body').type('{meta}S')
    })
  })

  beforeEach(() => {
    // reload
    util.visit()
    util.getAppsTab().click().then(() => {
      cy.get('#app-list').contains('.applistitem',`CYP0`)
      .then($el => {cy.wrap($el[0]).click()})
      .then(() => { util.createQuery(count)})  // all queries for CYP0
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
          cy.wait(500).get('.query-list > tbody > tr').contains('td', val.replace(/CYP0 /,''))
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

context('Edit Query', function() {
  let count = 0

  before(() => {
    cy.cleanYADAIndex()
    util.visit()
    util.createApp(0, false)
    .then(() => { cy.get('body').type('{meta}S').wait(500)})
  })

  beforeEach(() => {
    // reload
    util.getAppsTab().click()
    .then(() => {
      cy.get('#app-list').contains('.applistitem',`CYP0`)
      .then($el => {cy.wrap($el[0]).click()})
      .then(() => { util.createQuery(count)})
      .then(() => { cy.get('body').type('{meta}S').wait(500)})
      .then(() => { util.getAppsTab().click().then(() => {
          // confirm query has been saved well and app is back in steady state
          cy.getState().its('unsavedChanges').should('eq',0)
          cy.getState().its('creating').should('eq',false)
          cy.get('.background.unsaved').should('not.exist')
          // select app
          cy.get('#app-list').contains('.applistitem',`CYP0`).then($el => {
            cy.wrap($el[0]).click()
          })
        }) // all queries for CYP0
      })
    })
  })

  afterEach(() => { count++ })

  after(() => { cy.cleanYADAIndex() })

  // All these change tests should have both Save and Cancel counterparts

  // Edit query code
  it('Edits Query Code', function() {
    // select query
    util.getQueryListTab().click().then(() => {
    cy.get('.query-list > tbody > tr').contains('td:first-child()', `QNAME${count}`)
    .then($el => {cy.wrap($el[0]).click()})
    .then(() => {
      // confirm state
      cy.getState().its('activeTab').should('eq','query-edit-tab')
      // make changes to code
      cy.window().then(win => {
        const val = win.cm.getValue()
        win.cm.setValue(`${val}
  -- QNAME${count} code mods test`)
        cy.get('body').type('{meta}S').wait(500).then(() => {
          util.getQueryListTab().click().then(() => {
            cy.get('.query-list > tbody > tr').contains('td:first-child()', `QNAME${count}`).click().then(() => {
              cy.getState().its('activeTab').should('eq','query-edit-tab')
              cy.wrap(win.cm.getValue()).should('eq',`-- Replace with YADA markup
  -- QNAME0 code mods test`)
              })
            })
          })
        })
      })
    })
  })

  // Edit query comments
  it('Edits Query Comments', function() {
    // select query
    util.getQueryListTab().click().then(() => {
    cy.get('.query-list > tbody > tr').contains('td:first-child()', `QNAME${count}`)
    .then($el => {cy.wrap($el[0]).click()})
    .then(() => {
      // confirm state
      cy.getState().its('activeTab').should('eq','query-edit-tab')
      // make changes to comments
      cy.get('.comment div').click().wait(500)
      cy.get('textarea.comment').clear()
      cy.get('h5.title').contains('Comments').click()
      cy.get('textarea.comment').type(`Comments mod test`).wait(500)
      cy.get('body').type('{meta}S').wait(500).then(() => {
        cy.confirmSave(count)
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td:first-child()', `QNAME${count}`).click().then(() => {
              cy.getState().its('activeTab').should('eq','query-edit-tab')
              cy.get('.comment div').should('have.text',`Comments mod test`)
            })
          })
        })
      })
    })
  })

  // Rename query
  // Clone query
  // Add parameter
  // Delete parameter
  // Reorder parameters
  // Modify parameter name
  // Modify parameter value
  // Modify parameter mutability


})
