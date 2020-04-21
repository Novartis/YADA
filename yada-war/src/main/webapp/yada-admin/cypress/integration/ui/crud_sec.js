import * as util from '../../support/utils.js'

describe('Login', function() {
  it('logs in and sets cookies', function() {
    util.login()
  })
})

// Add parameter
context('Security Parameters', function() {

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
      cy.get('#app-list').contains('.applistitem',`CYP0`)
      .then($el => {
        cy.wrap($el[0]).click()
        .then(() => { util.createQuery(count) })
      })
    })
  })

  afterEach(() => { count++ })

  after(() => {
    cy.cleanYADAIndex()
  })

  // Single param
  context('Single Param is Sec Param', function() {
    beforeEach(() => {
      util.getQueryEditPanel().then(() => {
        util.getDefaultSecParamPanel().click()
      })
    })

    afterEach(() => {
      cy.deleteParameters(count)
    })

    context('Before Save', function() {

      it('creates plugin', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const value = `${pl},execution.policy=void,content.policy=void`
        util.addSecPlugin(pl).then((input) => {
          cy.wrap(input).blur().then(() => {
            util.saveAndConfirmSecPlugin(count,'0',value)
          })
        })
      })

      it('creates plugin with url validation', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const authrx = 'testurl'
        const value = `${pl},auth.path.rx=${authrx},execution.policy=void,content.policy=void`
        util.addSecPlugin(pl).then(() => {
          cy.get('.security input[name="auth.path.rx"]').then(el1 => {
            let input = cy.wrap(el1)
            input.should('have.value', '')
            input.type(authrx)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,'0',value)
            })
          })
        })
      })

      it('creates plugin with url validation and execution policy columns whitelist', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const authrx = 'testurl'
        const policy = 'execution'
        const type = 'whitelist'
        const query = 'YADA view protector'
        const config = 'TEST:getTest()'
        const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy=void`
        util.addSecPlugin(pl).then(() => {
          cy.get('.security input[name="auth.path.rx"]').then(el1 => {
            let input = cy.wrap(el1)
            input.should('have.value', '')
            input.type(authrx)
            input.blur()
            util.addSecPolicy(policy,type,query,config)
            util.confirmSecPolicyRO('authorization')
            util.saveAndConfirmSecPlugin(count,'0',value,policy,type,query)
          })
        })
      })

      it('creates plugin with url validation and execution policy columns blacklist', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const authrx = 'testurl'
        const policy = 'execution'
        const type = 'blacklist'
        const query = 'YADA view protector'
        const config = 'TEST:getTest()'
        const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy=void`
        util.addSecPlugin(pl).then(() => {
          cy.get('.security input[name="auth.path.rx"]').then(el1 => {
            let input = cy.wrap(el1)
            input.should('have.value', '')
            input.type(authrx)
            input.blur()
            util.addSecPolicy(policy,type,query,config)
            util.confirmSecPolicyRO('authorization')
            util.saveAndConfirmSecPlugin(count,'0',value,policy,type,query)
          })
        })
      })

      it('creates plugin with url validation and execution policy indexes whitelist', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const authrx = 'testurl'
        const policy = 'execution'
        const type = 'whitelist'
        const query = 'YADA view protector'
        const config = '0 1 3'
        const value = `${pl},auth.path.rx=${authrx},execution.policy.indexes=${config},content.policy=void`
        util.addSecPlugin(pl).then(() => {
          cy.get('.security input[name="auth.path.rx"]').then(el1 => {
            let input = cy.wrap(el1)
            input.should('have.value', '')
            input.type(authrx)
            input.blur()
            util.addSecPolicy(policy,type,query,config)
            util.confirmSecPolicyRO('authorization')
            util.saveAndConfirmSecPlugin(count,'0',value,policy,type,query)
          })
        })
      })

      it('creates plugin with url validation and execution and content policies whitelist', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const authrx = 'testurl'
        const policy = 'execution'
        const type = 'whitelist'
        const query = 'YADA view protector'
        const config = 'TEST:getTest()'
        const contentPol = 'FOO BAR'
        const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy.predicate=${contentPol}`
        util.addSecPlugin(pl).then(() => {

          cy.get('.security input[name="auth.path.rx"]').then(el1 => {
            let input = cy.wrap(el1)
            input.should('have.value', '')
            input.type(authrx)
            util.addSecPolicy(policy,type,query,config)
            util.confirmSecPolicyRO('authorization')

            // enter content policy
            cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
              input = cy.wrap(el2)
              input.type(contentPol)
              input.blur().then(() => {
                util.saveAndConfirmSecPlugin(count,'0',value,policy,type,query)
              })
            })
          })
        })
      })

      it('creates plugin execution and content policies whitelist', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const policy = 'execution'
        const type = 'whitelist'
        const query = 'YADA view protector'
        const config = 'TEST:getTest()'
        const contentPol = 'FOO BAR'
        const value = `${pl},execution.policy.columns=${config},content.policy.predicate=${contentPol}`
        util.addSecPlugin(pl).then(() => {
          util.addSecPolicy(policy,type,query,config)
          util.confirmSecPolicyRO('authorization')
          // enter content policy
          cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
            let input = cy.wrap(el2)
            input.type(contentPol)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,'0',value,policy,type,query)
            })
          })
        })
      })

      it('creates plugin authorization and content policies whitelist', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const policy = 'authorization'
        const type = 'whitelist'
        const grant = 'TESTROLE'
        const contentPol = 'FOO BAR'
        const value = `${pl},authorization.policy.grant=${grant},execution.policy=void,content.policy.predicate=${contentPol}`
        util.addSecPlugin(pl).then(() => {
          util.addSecPolicy(policy,type,grant)
          util.confirmSecPolicyRO('execution')

          // enter content policy
          cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
            let input = cy.wrap(el2)
            input.type(contentPol)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,'0',value,policy,type,grant)
            })
          })
        })
      })

      it('creates plugin authorization and content policies blacklist', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const policy = 'authorization'
        const type = 'whitelist'
        const grant = 'TESTROLE'
        const contentPol = 'FOO BAR'
        const value = `${pl},authorization.policy.grant=${grant},execution.policy=void,content.policy.predicate=${contentPol}`
        util.addSecPlugin(pl).then(() => {
          util.addSecPolicy(policy,type,grant)
          util.confirmSecPolicyRO('execution')

          // enter content policy
          cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
            let input = cy.wrap(el2)
            input.type(contentPol)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,'0',value,policy,type,grant)
            })
          })
        })
      })
    })

    // Single param
    context('After Save', function() {

      it('creates plugin authurl execpol contentpol confirm load', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const authrx = 'testurl'
        const policy = 'execution'
        const type = 'whitelist'
        const query = 'YADA view protector'
        const config = 'TEST:getTest()'
        const contentPol = 'FOO BAR'
        const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy.predicate=${contentPol}`
        util.addSecPlugin(pl).then(() => {

          cy.get('.security input[name="auth.path.rx"]').then(el1 => {
            let input = cy.wrap(el1)
            input.should('have.value', '')
            input.type(authrx)
            util.addSecPolicy(policy,type,query,config)
            util.confirmSecPolicyRO('authorization')

            // enter content policy
            cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
              input = cy.wrap(el2)
              input.type(contentPol)
              input.blur().then(() => {
                util.saveAndConfirmSecPlugin(count,'0',value,policy,type,query)
                // confirm load after save
                util.confirmSecLoadAfterSave(count,'0',value)
              })
            })
          })
        })
      })

      it('creates plugin authurl authpol contentpol confirm load', function() {
        const name = 'pl'
        const pl = 'Gatekeeper'
        const authrx = 'testurl'
        const policy = 'authorization'
        const type = 'whitelist'
        const grant = 'TEST'
        const contentPol = 'FOO BAR'
        const value = `${pl},auth.path.rx=${authrx},authorization.policy.grant=${grant},execution.policy=void,content.policy.predicate=${contentPol}`
        util.addSecPlugin(pl).then(() => {

          cy.get('.security input[name="auth.path.rx"]').then(el1 => {
            let input = cy.wrap(el1)
            input.should('have.value', '')
            input.type(authrx)
            util.addSecPolicy(policy,type,grant)
            util.confirmSecPolicyRO('execution')

            // enter content policy
            cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
              input = cy.wrap(el2)
              input.type(contentPol)
              input.blur().then(() => {
                util.saveAndConfirmSecPlugin(count,'0',value,policy,type,grant)
                // confirm load after save
                util.confirmSecLoadAfterSave(count,'0',value)
              })
            })
          })
        })
      })
    })
  })

  // Multi parameter tests:
  context('Multiple Params with Sec Param', function() {

    const names = ['c','ck','pz']
    beforeEach(() => {
      for(let i=0;i<3;i++)
      {
        util.chooseMenuOption('Add Param')
      }

      util.createMultipleParams(count,names)
      .then((val) => {
        if(val != -1)
        {
          util.getQueryEditPanel().then(() => {
            util.getDefaultSecParamPanel().click()
          })
        }
      })
    })

    afterEach(() => {
      cy.deleteParameters(count)
    })

    it('creates plugin', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const value = `${pl},execution.policy=void,content.policy=void`
      util.addSecPlugin(pl).then((input) => {
        cy.wrap(input).blur().then(() => {
          util.saveAndConfirmSecPlugin(count,3,value)
        })
      })
    })

    it('creates plugin with url validation', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const value = `${pl},auth.path.rx=${authrx},execution.policy=void,content.policy=void`
      util.addSecPlugin(pl).then(() => {
        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          input.blur().then(() => {
            util.saveAndConfirmSecPlugin(count,3,value)
          })
        })
      })
    })

    it('creates plugin with url validation and execution policy columns whitelist', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const policy = 'execution'
      const type = 'whitelist'
      const query = 'YADA view protector'
      const config = 'TEST:getTest()'
      const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy=void`
      util.addSecPlugin(pl).then(() => {
        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          input.blur()
          util.addSecPolicy(policy,type,query,config)
          util.confirmSecPolicyRO('authorization')
          util.saveAndConfirmSecPlugin(count,3,value,policy,type,query)
        })
      })
    })

    it('creates plugin with url validation and execution policy columns blacklist', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const policy = 'execution'
      const type = 'blacklist'
      const query = 'YADA view protector'
      const config = 'TEST:getTest()'
      const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy=void`
      util.addSecPlugin(pl).then(() => {
        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          input.blur()
          util.addSecPolicy(policy,type,query,config)
          util.confirmSecPolicyRO('authorization')
          util.saveAndConfirmSecPlugin(count,3,value,policy,type,query)
        })
      })
    })

    it('creates plugin with url validation and execution policy indexes whitelist', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const policy = 'execution'
      const type = 'whitelist'
      const query = 'YADA view protector'
      const config = '0 1 3'
      const value = `${pl},auth.path.rx=${authrx},execution.policy.indexes=${config},content.policy=void`
      util.addSecPlugin(pl).then(() => {
        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          input.blur()
          util.addSecPolicy(policy,type,query,config)
          util.confirmSecPolicyRO('authorization')
          util.saveAndConfirmSecPlugin(count,3,value,policy,type,query)
        })
      })
    })

    it('creates plugin with url validation and execution and content policies whitelist', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const policy = 'execution'
      const type = 'whitelist'
      const query = 'YADA view protector'
      const config = 'TEST:getTest()'
      const contentPol = 'FOO BAR'
      const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy.predicate=${contentPol}`
      util.addSecPlugin(pl).then(() => {

        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          util.addSecPolicy(policy,type,query,config)
          util.confirmSecPolicyRO('authorization')

          // enter content policy
          cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
            input = cy.wrap(el2)
            input.type(contentPol)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,3,value,policy,type,query)
            })
          })
        })
      })
    })

    it('creates plugin execution and content policies whitelist', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const policy = 'execution'
      const type = 'whitelist'
      const query = 'YADA view protector'
      const config = 'TEST:getTest()'
      const contentPol = 'FOO BAR'
      const value = `${pl},execution.policy.columns=${config},content.policy.predicate=${contentPol}`
      util.addSecPlugin(pl).then(() => {
        util.addSecPolicy(policy,type,query,config)
        util.confirmSecPolicyRO('authorization')
        // enter content policy
        cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
          let input = cy.wrap(el2)
          input.type(contentPol)
          input.blur().then(() => {
            util.saveAndConfirmSecPlugin(count,3,value,policy,type,query)
          })
        })
      })
    })

    it('creates plugin authorization and content policies whitelist', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const policy = 'authorization'
      const type = 'whitelist'
      const grant = 'TESTROLE'
      const contentPol = 'FOO BAR'
      const value = `${pl},authorization.policy.grant=${grant},execution.policy=void,content.policy.predicate=${contentPol}`
      util.addSecPlugin(pl).then(() => {
        util.addSecPolicy(policy,type,grant)
        util.confirmSecPolicyRO('execution')

        // enter content policy
        cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
          let input = cy.wrap(el2)
          input.type(contentPol)
          input.blur().then(() => {
            util.saveAndConfirmSecPlugin(count,3,value,policy,type,grant)
          })
        })
      })
    })

    it('creates plugin authorization and content policies blacklist', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const policy = 'authorization'
      const type = 'whitelist'
      const grant = 'TESTROLE'
      const contentPol = 'FOO BAR'
      const value = `${pl},authorization.policy.grant=${grant},execution.policy=void,content.policy.predicate=${contentPol}`
      util.addSecPlugin(pl).then(() => {
        util.addSecPolicy(policy,type,grant)
        util.confirmSecPolicyRO('execution')

        // enter content policy
        cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
          let input = cy.wrap(el2)
          input.type(contentPol)
          input.blur().then(() => {
            util.saveAndConfirmSecPlugin(count,3,value,policy,type,grant)
          })
        })
      })
    })

    it('creates plugin authurl execpol contentpol confirm load after save', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const policy = 'execution'
      const type = 'whitelist'
      const query = 'YADA view protector'
      const config = 'TEST:getTest()'
      const contentPol = 'FOO BAR'
      const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy.predicate=${contentPol}`
      util.addSecPlugin(pl).then(() => {
        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          util.addSecPolicy(policy,type,query,config)
          util.confirmSecPolicyRO('authorization')

          // enter content policy
          cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
            input = cy.wrap(el2)
            input.type(contentPol)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,3,value,policy,type,query)
              // confirm load after save
              util.confirmSecLoadAfterSave(count,3,value)
            })
          })
        })
      })
    })

    it('creates plugin authurl authpol contentpol confirm load after save', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const policy = 'authorization'
      const type = 'whitelist'
      const grant = 'TEST'
      const contentPol = 'FOO BAR'
      const value = `${pl},auth.path.rx=${authrx},authorization.policy.grant=${grant},execution.policy=void,content.policy.predicate=${contentPol}`
      util.addSecPlugin(pl).then(() => {
        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          util.addSecPolicy(policy,type,grant)
          util.confirmSecPolicyRO('execution')

          // enter content policy
          cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
            input = cy.wrap(el2)
            input.type(contentPol)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,3,value,policy,type,grant)
              // confirm load after save
              util.confirmSecLoadAfterSave(count,3,value)
            })
          })
        })
      })
    })

    it('creates then deletes plugin authurl execpol contentpol confirm load after save', function() {
      const name = 'pl'
      const pl = 'Gatekeeper'
      const authrx = 'testurl'
      const policy = 'execution'
      const type = 'whitelist'
      const query = 'YADA view protector'
      const config = 'TEST:getTest()'
      const contentPol = 'FOO BAR'
      const value = `${pl},auth.path.rx=${authrx},execution.policy.columns=${config},content.policy.predicate=${contentPol}`
      util.addSecPlugin(pl).then(() => {
        cy.get('.security input[name="auth.path.rx"]').then(el1 => {
          let input = cy.wrap(el1)
          input.should('have.value', '')
          input.type(authrx)
          util.addSecPolicy(policy,type,query,config)
          util.confirmSecPolicyRO('authorization')

          // enter content policy
          cy.get('.security input[name="content.policy.predicate"]').then(el2 => {
            input = cy.wrap(el2)
            input.type(contentPol)
            input.blur().then(() => {
              util.saveAndConfirmSecPlugin(count,3,value,policy,type,query)
              // confirm load after save
              util.confirmSecLoadAfterSave(count,3,value)

              cy.get(`.params>table>tbody>tr:nth-child(4)>td.param-action`).trigger('mouseenter')
              .then((td) => {
                cy.wrap(td).find('button').then((button) => {
                  cy.wrap(button).trigger('click').wait(500).then(() => {
                    cy.get('.ui.dimmer.visible.active').should('exist')
                    cy.get('.confirm.visible.active').should('exist')
                    cy.get('.confirm .ui.positive.button').click().then(() => {
                      cy.save().then(() => {
                        cy.confirmParamSave(count).then((r1) => {
                          const reso = r1.stdout.replace(/\n/g,',')
                          cy.wrap(Array.isArray(JSON.parse(`[${reso}]`))).should('eq',true)
                          cy.wrap(JSON.parse(`[${reso}]`).length).should('eq',3)
                          cy.confirmA11nSave(count).then((r2) => {
                            cy.wrap(r2.stdout).should('be.empty')
                          })
                        })
                      })
                    })
                  })
                })
              })

            })
          })
        })
      })
    })
  })
})
