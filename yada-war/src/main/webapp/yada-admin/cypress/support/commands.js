// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
import '@4tw/cypress-drag-drop'

const stateChecker = ($val, obj, key, defval, deep) => {
  // debugger
  let k = key, match = /(.+)\.(.+)/.exec(key), attr = false
  if (match !== null)
  {
    k = match[1]
    attr = match[2]
  }
  if (typeof obj !== 'undefined' && typeof obj[k] !== 'undefined')
  {
    if (deep)
    {
      expect($val).to.deep.equal(obj[k])
    }
    else
    {
      if (!!attr)
      {
        expect($val).to.equal(obj[k][attr])
      }
      else
      {
        expect($val).to.equal(obj[k])
      }
    }
  }
  else
  {
    // defaults
    if (deep)
    {
      expect($val).to.deep.equal(defval)
    }
    else
    {
      expect($val).to.equal(defval)
    }
  }
}

Cypress.Commands.add('getState', () => {
  return cy.window().its('vue.$store.state')
})


Cypress.Commands.add("isInState", (obj) => {
    cy.log('Checking state...')
    cy.getState().its('saving').should(($val) => { stateChecker($val, obj, 'saving', false) })
    cy.getState().its('creating').should(($val) => { stateChecker($val, obj, 'checker', false) })
    cy.getState().its('renaming').should(($val) => { stateChecker($val, obj, 'renaming', false) })
    cy.getState().its('cloning').should(($val) => { stateChecker($val, obj, 'cloning', false) })
    cy.getState().its('loading').should(($val) => { stateChecker($val, obj, 'loading', false) })
    cy.getState().its('unsavedChanges').should(($val) => { stateChecker($val, obj, 'unsavedChanges', 0) })
    cy.getState().its('unsavedParams').should(($val) => { stateChecker($val, obj, 'unsavedParams', 0) })
    cy.getState().its('app').should(($val) => { stateChecker($val, obj, 'app', null) })
    cy.getState().its('param').should(($val) => { stateChecker($val, obj, 'param', null) })
    cy.getState().its('qname').should(($val) => { stateChecker($val, obj, 'qname', null) })
    cy.getState().its('qnameOrig').should(($val) => { stateChecker($val, obj, 'qnameOrig', null) })
    cy.getState().its('query.QUERY').should(($val) => { stateChecker($val, obj, 'query.QUERY', null ) })
    cy.getState().its('config').should(($val) => { stateChecker($val, obj, 'config', null, true) })
    cy.getState().its('queries.length').should(($val) => { stateChecker($val, obj, 'queries.length', 0) })
    cy.getState().its('params.length').should(($val) => { stateChecker($val, obj, 'params.length', 0) })
    cy.getState().its('renderedParams.length').should(($val) => { stateChecker($val, obj, 'renderedParams.length', 0) })
    cy.getState().its('props.length').should(($val) => { stateChecker($val, obj, 'props.length', 0) })
    cy.getState().its('protectors.length').should(($val) => { stateChecker($val, obj, 'protectors.length', 0) })
    cy.getState().its('activeTab').should(($val) => { stateChecker($val, obj, 'activeTab', null) })
    cy.getState().its('nextTab').should(($val) => { stateChecker($val, obj, 'nextTab', null) })
    cy.getState().its('confirmAction').should(($val) => { stateChecker($val, obj, 'confirmAction', null) })
})

Cypress.Commands.add("cleanYADAIndex",() => {
  cy.log('Cleaning YADA index...')
  cy.exec(`psql -U yada -w -h signals-test.qdss.io -c \
          "delete from yada_query_conf where app like 'CYP%';\
           delete from yada_ug where app like 'CYP%';\
           delete from yada_query where app like 'CYP%';\
           delete from yada_param where target like 'CYP%';\
           delete from yada_prop where target like 'CYP%';\
           delete from yada_a11n where target like 'CYP%';"`)
})

Cypress.Commands.add("confirmQuerySave", (count,column,value) => {
  cy.log('Confirming save...')
  let sql = `select row_to_json(yada_query) from yada_query where qname = 'CYP0 QNAME${count}'`
  if (typeof column !== 'undefined')
  {
    if (typeof value === 'undefined')
      value = ''
    sql = `${sql} AND ${column} = '${value}'`
  }
  let connect = `psql -U ${Cypress.env('YADA_INDEX_USER')} -w -h ${Cypress.env('YADA_INDEX_HOST')} -t -c "${sql};"`
  cy.exec(connect)
})

Cypress.Commands.add("confirmRenameSave", (count,old,neo) => {
  cy.log('Confirming rename saved...')
  let sql = `select row_to_json(row) from
  (select *
   From yada_query a
   join yada_param b on a.qname = b.target
   where a.app = 'CYP0' and a.qname in ('CYP0 ${old}','CYP0 ${neo}')) row`
  let connect = `psql -U ${Cypress.env('YADA_INDEX_USER')} -w -h ${Cypress.env('YADA_INDEX_HOST')} -t -c "${sql};"`
  cy.exec(connect)
})

Cypress.Commands.add("confirmCloneSave", (count,old,neo) => {
  cy.log('Confirming clone saved...')
  let sql = `select row_to_json(row) from
  (select *
   From yada_query a
   join yada_param b on a.qname = b.target
   where a.app = 'CYP0' and a.qname in ('CYP0 ${old}','CYP0 ${neo}')) row`
  let connect = `psql -U ${Cypress.env('YADA_INDEX_USER')} -w -h ${Cypress.env('YADA_INDEX_HOST')} -t -c "${sql};"`
  cy.exec(connect)
})

Cypress.Commands.add("confirmParamSave", (count,name,value,rule,id) => {
  cy.log('Confirming parameters saved...')
  let sql = `select row_to_json(yada_param) from yada_param where target like 'CYP0 QNAME${count}%'`
  if (typeof name !== 'undefined')
  {
    sql = `${sql} AND name = '${name}' AND value = '${value}' AND rule = '${rule}' AND id = '${parseInt(id)}'`
  }
  let connect = `psql -U ${Cypress.env('YADA_INDEX_USER')} -w -h ${Cypress.env('YADA_INDEX_HOST')} -t -c "${sql};"`
  cy.exec(connect)
})

Cypress.Commands.add("confirmA11nSave", (count,policy,type,qname) => {
  cy.log('Confirming authorization data saved...')
  let sql = `select row_to_json(yada_a11n) from yada_a11n where target like 'CYP0 QNAME${count}%'`
  if (typeof policy !== 'undefined')
  {
    sql = `${sql} AND policy = '${policy}' AND type = '${type}' AND qname = '${qname}'`
  }
  let connect = `psql -U ${Cypress.env('YADA_INDEX_USER')} -w -h ${Cypress.env('YADA_INDEX_HOST')} -t -c "${sql};"`
  cy.exec(connect)
})

Cypress.Commands.add("deleteParameters", (count) => {
  cy.log('Deleting parameters...')
  let sql = `delete from yada_param where target like 'CYP% QNAME${count}%'`
  let connect = `psql -U ${Cypress.env('YADA_INDEX_USER')} -w -h ${Cypress.env('YADA_INDEX_HOST')} -t -c "${sql};"`
  cy.exec(connect)
})

Cypress.Commands.add('save', () => {
  cy.log('Saving...')
  return cy.get('body').type('{meta}S').wait(1000).then((val) => {
    cy.log('Confirming state has been reset...')
    cy.getState().its('unsavedChanges').should('eq',0)
    cy.getState().its('unsavedParams').should('eq',0)
    cy.getState().its('renaming').should('eq',false)
    cy.getState().its('creating').should('eq',false)
    cy.getState().its('cloning').should('eq',false)
    cy.get('.background.unsaved').should('not.exist')
  })
})
