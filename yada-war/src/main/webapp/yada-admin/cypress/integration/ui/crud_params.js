import * as util from '../../support/utils.js'

describe('Login', function () {
  it('logs in and sets cookies', function () {
    util.login()
  })
})

// Add parameter
context('Default Query Parameters', function () {
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
          .then($el => {
            cy.wrap($el[0]).click()
              .then(() => {
                util.createQuery(count)
                  .then(() => {
                    cy.save()
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
          })
      })
  })

  afterEach(() => { count++ })

  after(() => { cy.cleanYADAIndex() })

  context('Single Parameter', function () {
    beforeEach(() => { util.addParameter(count) })
    // select query
    it('Adds Parameter', function () {
      cy.save().then(() => {
        cy.confirmParamSave(count).then(result => {
          cy.wrap(Array.isArray(JSON.parse(result.stdout))).should('eq', false)
          cy.wrap(typeof (JSON.parse(result.stdout))).should('eq', 'object')
        })
        cy.get('.params>table>tbody>tr>td.param-id').invoke('text').then((txt) => cy.wrap(txt).should('match', /\s+1\s+/))
        cy.get('.params>table>tbody>tr>td.param-name>div:nth-child(1)').invoke('text').then((txt) => cy.wrap(txt).should('eq', ''))
        cy.get('.params>table>tbody>tr>td.param-val').invoke('text').then((txt) => cy.wrap(txt).should('match', /\s+/))
        cy.get('.params>table>tbody>tr>td.param-rule input[name="mutability-0"]').invoke('val').should('eq', 'on')
        cy.get('.params>table>tbody>tr>td.param-rule input[name="mutability-0"]:checked').should('not.exist')
        cy.get('.params>table>tbody>tr>td.param-action button.hidden').should('exist')
      })
    })

    it('Sets Boolean Parameter', function () {
      util.setParameter(count, 1, 'boolean', 'c', 'false', 'false')
    })

    it('Sets String Parameter', function () {
      util.setParameter(count, 1, 'string', 'ck', 'testing', 'Change me...')
    })

    it('Sets Number Parameter', function () {
      util.setParameter(count, 1, 'number', 'pz', '-1', '20')
    })

    it('Sets Mutable Boolean Parameter', function () {
      const name = 'c'
      const value = 'false'
      const dfault = 'false'
      util.prepParamForEdit(count).then(() => {
        cy.get('.params>table>tbody>tr>td.param-name').click().then(td => {
          cy.get(`div[data-value="${name}"]`).click().then(el => {
            cy.wrap(td).find('.value').should('have.text', name)
            cy.wrap(td).next().find('.value').should('contain', dfault)
            cy.getState().its('unsavedParams').should('be.gt', 0)
          })
        })
        // mutability change
        cy.get('.params>table>tbody>tr>td.param-rule').click().then(td => {
          cy.wrap(td).find('input:checked').should('exist')
          cy.getState().its('unsavedParams').should('be.gt', 0)
        })
        // save and confirm
        cy.save().then(() => {
          cy.confirmParamSave(count, name, value, '0', '1').then(result => {
            const reso = JSON.parse(result.stdout)
            cy.wrap(Array.isArray(reso)).should('eq', false)
            cy.wrap(typeof reso).should('eq', 'object')
          })
        })
      })
    })

    // invalid parameter tests
    it('Sets Invalid String Parameter', function () {
      const invalid =  'value will not be typed'
      const valid = 'testing'
      const name = 'ck'
      util.setInvalidParameter(count, 1, 'string', name, invalid, 'Change me...').then(() => {
        cy.get('.params>table>tbody>tr:first-child>td.param-val').click().then(td => {
          cy.wrap(td).find('input').type(valid).blur()
          cy.getState().its('unsavedChanges').should('be.gt', 0)
          cy.getState().its('unsavedParams').should('be.gt', 0)
          cy.getState().its('showWarning').should('eq', false)
          cy.get('.background.unsaved').should('exist')
          cy.get('.error').should('not.exist')
          cy.save().then(() => {
            cy.confirmParamSave(count, name, valid, '1', '1').then(result => {
              const reso = JSON.parse(result.stdout)
              cy.wrap(Array.isArray(reso)).should('eq', false)
              cy.wrap(typeof reso).should('eq', 'object')
            })
          })
        })
      })
    })

    it('Sets Invalid Number Parameter', function () {
      const invalid =  '-2'
      const valid = '-1'
      const name = 'pz'
      util.setInvalidParameter(count, 1, 'number', name, invalid, '20').then(() => {
        cy.get('.params>table>tbody>tr:first-child>td.param-val').click().then(td => {
          cy.wrap(td).find('input').type(valid).blur()
          cy.getState().its('unsavedChanges').should('be.gt', 0)
          cy.getState().its('unsavedParams').should('be.gt', 0)
          cy.getState().its('showWarning').should('eq', false)
          cy.get('.background.unsaved').should('exist')
          cy.get('.error').should('not.exist')
          cy.save().then(() => {
            cy.confirmParamSave(count, name, valid, '1', '1').then(result => {
              const reso = JSON.parse(result.stdout)
              cy.wrap(Array.isArray(reso)).should('eq', false)
              cy.wrap(typeof reso).should('eq', 'object')
            })
          })
        })
      })
    })
  })
  // Multi parameter tests:
  context('Multiple Parameters', function () {
    const names = ['c', 'ck', 'pz']

    context('Delete parameters', function () {
      beforeEach(() => {
        util.getQueryListTab().click().then(() => {
          cy.get('.query-list > tbody > tr').contains('td:first-child()', `QNAME${count}`)
            .then($el => { cy.wrap($el[0]).click() })
            .then(() => {
              for (let i = 0; i < 3; i++)
              {
                util.chooseMenuOption('Add Param')
              }
            }
            )
            .then(() => {
              util.createMultipleParams(count, names)
            })
        })
      })

      afterEach(() => {
        cy.deleteParameters(count)
      })
      //  Delete parameter
      it('Deletes the first of three parameters', function () {
        util.testParameterDeletion(count, names, 1)
      })

      it('Deletes the second of three parameters', function () {
        util.testParameterDeletion(count, names, 2)
      })

      it('Deletes the third of three parameters', function () {
        util.testParameterDeletion(count, names, 3)
      })
    })

    context('Reorder parameters', function () {
      context('Swap adjacent parameters', function () {
        beforeEach(() => {
          util.getQueryListTab().click().then(() => {
            cy.get('.query-list > tbody > tr').contains('td:first-child()', `QNAME${count}`)
              .then($el => { cy.wrap($el[0]).click() })
              .then(() => {
                for (let i = 0; i < 3; i++)
                {
                  util.chooseMenuOption('Add Param')
                }
              }
              )
              .then(() => {
                util.createMultipleParams(count, names)
              })
          })
        })

        afterEach(() => {
          cy.deleteParameters(count)
        })
        //  Reorder parameters
        it('Swaps parameter one and two', function () {
          util.testParamDnD(count, names, 1, 2)
        })

        it('Swaps parameter two and one', function () {
          util.testParamDnD(count, names, 2, 1)
        })

        it('Swaps parameter two and three', function () {
          util.testParamDnD(count, names, 2, 3)
        })

        it('Swaps parameter three and two', function () {
          util.testParamDnD(count, names, 3, 2)
        })
      })

      context.skip('Swap non-adjacent parameters', function () {
        beforeEach(() => {
          util.getQueryListTab().click().then(() => {
            cy.get('.query-list > tbody > tr').contains('td:first-child()', `QNAME${count}`)
              .then($el => { cy.wrap($el[0]).click() })
              .then(() => {
                for (let i = 0; i < 3; i++)
                {
                  util.chooseMenuOption('Add Param')
                }
              }
              )
              .then(() => {
                util.createMultipleParams(count, names)
              })
          })
        })

        afterEach(() => {
          cy.deleteParameters(count)
        })
        //  Reorder parameters
        it('Swaps parameter one and three', function () {
          util.testParamDnD(count, names, 1, 3)
        })

        it('Swaps parameter three and one', function () {
          util.testParamDnD(count, names, 3, 1)
        })
      }) // swap non-adjacent
    }) // reorder
  }) // multiple
})
