import * as util from '../../support/utils.js'

describe('Login', function () {
  it('logs in and sets cookies', function () {
    util.login()
  })
})

context('Create Query', function () {
  let count = 0

  before(() => {
    cy.cleanYADAIndex()
    util.visit()
    util.createApp(0).then(() => {
      cy.save()
    })
  })

  beforeEach(() => {
    // reload
    util.visit()
    util.getAppsTab().click().then(() => {
      cy.get('#app-list').contains('.applistitem', `CYP0`)
        .then($el => { cy.wrap($el[0]).click() })
        .then(() => { util.createQuery(count) })  // all queries for CYP0
    })
  })

  afterEach(() => { count++ })

  after(() => { cy.cleanYADAIndex() })

  it('Adds Query and Saves by Click', function () {
    util.chooseMenuOption('Save').then(val => {
      cy.getState().its('unsavedChanges').should('eq', 0)
      cy.getState().its('creating').should('eq', false)
      cy.get('.background.unsaved').should('not.exist')
      cy.getState().its('qname').then(val => {
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td:first-child()', val.replace(/CYP0 /, ''))
            .then($el => { cy.wrap($el[0]).click() })
            .then(() => {
              cy.getState().its('activeTab').should('eq', 'query-edit-tab')
            })
        })
      })
    })
  })

  it('Adds Query and Saves by Shortcut', function () {
    cy.save().then((val) => {
      cy.getState().its('qname').then(val => {
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td:first-child()', val.replace(/CYP0 /, ''))
            .then($el => { cy.wrap($el[0]).click() })
            .then(() => {
              cy.getState().its('activeTab').should('eq', 'query-edit-tab')
            })
        })
      })
    })
  })

  it('Adds Query, Clicks to Query List tab, and Saves by Dialog', function () {
    util.getQueryListTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.getState().its('qname').then(val => {
        cy.get('.confirm .ui.positive.button').click().then(() => {
          cy.getState().its('activeTab').should('eq', 'query-list-tab')
          util.getQueryListTab().click().then(() => {
            cy.get('.query-list > tbody > tr').contains('td', val.replace(/CYP0 /, ''))
              .then($el => { cy.wrap($el[0]).click() })
              .then(() => {
                cy.getState().its('activeTab').should('eq', 'query-edit-tab')
              })
          })
        })
      })
    })
  })

  it('Adds Query, Clicks to Config tab, and Saves by Dialog', function () {
    util.getConfTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.getState().its('qname').then(val => {
        cy.get('.confirm .ui.positive.button').click().then(() => {
          cy.getState().its('activeTab').should('eq', 'conf-tab')
          util.getQueryListTab().click().then(() => {
            cy.wait(500).get('.query-list > tbody > tr').contains('td', val.replace(/CYP0 /, ''))
              .then($el => { cy.wrap($el[0]).click() })
              .then(() => {
                cy.getState().its('activeTab').should('eq', 'query-edit-tab')
              })
          })
        })
      })
    })
  })

  it('Adds Query, Clicks to Apps tab, and Saves by Dialog', function () {
    util.getAppsTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.getState().its('qname').then(val => {
        cy.get('.confirm .ui.positive.button').click().then(() => {
          cy.getState().its('activeTab').should('eq', 'apps-tab')
          cy.get('#app-list').contains('.applistitem', `CYP0`)
            .then($el => { cy.wrap($el[0]).click() })
            .then(() => {
              cy.get('.query-list > tbody > tr').contains('td', val.replace(/CYP0 /, ''))
                .then($el => { cy.wrap($el[0]).click() })
                .then(() => {
                  cy.getState().its('activeTab').should('eq', 'query-edit-tab')
                })
            })
        })
      })
    })
  })

  it('Adds Query, Clicks to Query List tab, Declines by Dialog', function () {
    util.getQueryListTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.deny.button').click().then(() => {
        cy.getState().its('activeTab').should('eq', 'query-list-tab')
        cy.get('.query-list > tbody > tr').contains('td', `QNAME${count}`).should('not.exist')
      })
    })
  })

  it('Adds Query, Clicks to Config tab, Declines by Dialog', function () {
    util.getConfTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.deny.button').click().then(() => {
        cy.getState().its('activeTab').should('eq', 'conf-tab')
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td', `QNAME${count}`).should('not.exist')
        })
      })
    })
  })

  it('Adds Query, Clicks to Apps tab, Declines by Dialog', function () {
    util.getAppsTab().click().then(() => {
      cy.get('.ui.dimmer.visible.active').should('exist')
      cy.get('.confirm.visible.active').should('exist')
      cy.get('.ui.deny.button').click().then(() => {
        cy.getState().its('activeTab').should('eq', 'apps-tab')
        cy.get('#app-list').contains('.applistitem', `CYP0`)
          .then($el => { cy.wrap($el[0]).click() })
          .then(() => {
            cy.get('.query-list > tbody > tr').contains('td', `QNAME${count}`).should('not.exist')
          })
      })
    })
  })
})

context('Edit Query', function () {
  const names = ['c', 'ck', 'pz']
  let count = 0

  before(() => {
    cy.cleanYADAIndex()
    util.visit()
    util.createApp(0, false)
      .then(() => {
        cy.save()
      })
  })

  beforeEach(() => {
    // reload
    util.getAppsTab().click()
      .then(() => {
        cy.get('#app-list').contains('.applistitem', `CYP0`)
          .then($el => { cy.wrap($el[0]).click() })
          .then(() => { util.createQuery(count) })
          .then(() => {
            cy.save()
          })
          .then(() => {
            util.getAppsTab().click().then(() => {
              // confirm query has been saved well and app is back in steady state
              cy.getState().its('creating').should('eq', false)
              // select app
              cy.get('#app-list').contains('.applistitem', `CYP0`).then($el => {
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
  it('Edits Query Code', function () {
    // select query
    const qname = `QNAME${count}`
    util.getQueryListTab().click().then(() => {
      cy.get('.query-list > tbody > tr').contains('td:first-child()', qname)
        .then($el => { cy.wrap($el[0]).click() })
        .then(() => {
          // confirm state
          cy.getState().its('activeTab').should('eq', 'query-edit-tab')
          // make changes to code
          cy.window().then(win => {
            const val = `${win.cm.getValue()}
-- ${qname} code mods test`
            win.cm.setValue(val)
            cy.save().then(() => {
              cy.confirmQuerySave(count, 'query', val).then(result => {
                cy.wrap(JSON.parse(result.stdout)).its('query').should('match', new RegExp(val))
                util.getQueryListTab().click().then(() => {
                  cy.get('.query-list > tbody > tr').contains('td:first-child()', qname).click().then(() => {
                    cy.getState().its('activeTab').should('eq', 'query-edit-tab')
                    cy.wrap(win.cm.getValue()).should('eq', val)
                  })
                })
              })
            })
          })
        })
    })
  })

  // Edit query comments
  it('Edits Query Comments', function () {
    // select query
    const qname = `QNAME${count}`
    const val = 'Comments mod test'
    cy.get('.query-list > tbody > tr').contains('td:first-child()', qname)
      .then($el => { cy.wrap($el[0]).click() })
      .then(() => {
      // confirm state
        cy.getState().its('activeTab').should('eq', 'query-edit-tab')
        // make changes to comments
        cy.get('.query-editor-view div.comment.active').click().then(() => {
          cy.get('textarea.comment').type(`{selectall}${val}`).then(() => {
            cy.save().then(() => {
              cy.confirmQuerySave(count, 'comments', val).then(result => {
                cy.wrap(JSON.parse(result.stdout)).its('comments').should('match', new RegExp(val))
                util.getQueryListTab().click().then(() => {
                  cy.get('.query-list > tbody > tr').contains('td:first-child()', qname).click().then(() => {
                    cy.getState().its('activeTab').should('eq', 'query-edit-tab')
                    cy.get('.comment div').should('have.text', val)
                  })
                })
              })
            })
          })
        })
      })
  })

  // Rename query
  it('Renames a query', function () {
    const qname = `QNAME${count}`
    util.getQueryListTab().click().then(() => {
      cy.get('.query-list > tbody > tr').contains('td:first-child()', qname)
        .then($el => { cy.wrap($el[0]).click() })
        .then(() => {
        // confirm state
          cy.getState().its('activeTab').should('eq', 'query-edit-tab')
          util.chooseMenuOption('Rename').then(() => {
            cy.getState().its('renaming').should('eq', true)
            cy.get('.query-editor-view input[name=qname]').type(' RENAMING TEST')
            cy.getState().its('unsavedChanges').should('be.gt', 0)
            cy.save()
          })
        })
    })
  })

  it('Renames a query with parameters', function () {
    const qname = `QNAME${count}`
    util.getQueryListTab().click().then(() => {
      cy.get('.query-list > tbody > tr').contains('td:first-child()', qname)
        .then($el => { cy.wrap($el[0]).click() })
        .then(() => {
          for (let i = 0; i < 3; i++)
          {
            util.chooseMenuOption('Add Param')
          }
        })
        .then(() => {
          util.createMultipleParams(count, names)
        })
        .then(() => { cy.save() })
        .then(() => {
          util.chooseMenuOption('Rename').then(() => {
            cy.getState().its('renaming').should('eq', true)
            cy.get('.query-editor-view input[name=qname]').type(' RENAMING TEST')
            cy.getState().its('unsavedChanges').should('be.gt', 0)
            cy.save().then(() => {
              cy.confirmRenameSave(count, `QNAME${count}`, `QNAME${count} RENAMING TEST`).then(result => {
                const reso = result.stdout.replace(/\n/g, ',')
                const array  = JSON.parse(`[${reso}]`)
                cy.wrap(Array.isArray(array)).should('eq', true)
                cy.wrap(array.every(el => el.qname === `CYP0 QNAME${count} RENAMING TEST`)).should('eq', true)
              })
            })
          })
        })
    })
  })

  it('Clones a query', function () {
    const qname = `QNAME${count}`
    util.getQueryListTab().click().then(() => {
      cy.get('.query-list > tbody > tr').contains('td:first-child()', qname)
        .then($el => { cy.wrap($el[0]).click() })
        .then(() => {
          // confirm state
          cy.getState().its('activeTab').should('eq', 'query-edit-tab')
          util.chooseMenuOption('Clone').then(() => {
            cy.getState().its('cloning').should('eq', true)
            cy.getState().its('creating').should('eq', true)
            cy.getState().its('unsavedChanges').should('be.gt', 0)
            // cy.save().then(() => {
            cy.confirmCloneSave(count, `QNAME${count}`, `QNAME${count} CLONE`).then(result => {
              const reso = result.stdout.replace(/\n/g, ',')
              const array  = JSON.parse(`[${reso}]`)
              cy.wrap(Array.isArray(array)).should('eq', true)
              console.log(array)
              // cy.wrap(array.every(el => el.qname === `CYP0 QNAME${count} RENAMING TEST`)).should('eq',true)
            })
            cy.get('input[name="qname"]').invoke('val').should('eq', `QNAME${count} CLONE`)
            // })
          })
        })
    })
  })

  it('Clones a query with parameters', function () {
    const qname = `QNAME${count}`
    util.getQueryListTab().click().then(() => {
      cy.get('.query-list > tbody > tr').contains('td:first-child()', qname)
        .then($el => { cy.wrap($el[0]).click() })
        .then(() => {
          for (let i = 0; i < 3; i++)
          {
            util.chooseMenuOption('Add Param')
          }
        })
        .then(() => {
          util.createMultipleParams(count, names)
        })
        .then(() => {
          cy.save()
            .then(() => {
              util.chooseMenuOption('Clone').then(() => {
                cy.getState().its('cloning').should('eq', true)
                cy.getState().its('creating').should('eq', true)
                cy.getState().its('unsavedChanges').should('be.gt', 0)
                // cy.save().then(() => {
                cy.confirmCloneSave(count, `QNAME${count}`, `QNAME${count} CLONE`).then(result => {
                  const reso = result.stdout.replace(/\n/g, ',')
                  const array  = JSON.parse(`[${reso}]`)
                  cy.wrap(Array.isArray(array)).should('eq', true)
                  console.log(array)
                })
                cy.get('input[name="qname"]').invoke('val').should('eq', `QNAME${count} CLONE`)
                cy.confirmParamSave(count, 'c', 'false', '1', '1').then(result => {
                  const reso = result.stdout.replace(/\n/g, ',')
                  const array = JSON.parse(`[${reso}]`)
                  console.log(array)
                  cy.wrap(Array.isArray(array)).should('eq', true)
                  cy.wrap(array).should('have.length', 2)
                })
                cy.confirmParamSave(count, 'ck', 'testing', '1', '2').then(result => {
                  const reso = result.stdout.replace(/\n/g, ',')
                  const array = JSON.parse(`[${reso}]`)
                  cy.wrap(Array.isArray(array)).should('eq', true)
                  cy.wrap(array).should('have.length', 2)
                })
                cy.confirmParamSave(count, 'pz', '-1', '1', '3').then(result => {
                  const reso = result.stdout.replace(/\n/g, ',')
                  const array = JSON.parse(`[${reso}]`)
                  cy.wrap(Array.isArray(array)).should('eq', true)
                  cy.wrap(array).should('have.length', 2)
                })
              })
            })
        })
    })
  })
})
