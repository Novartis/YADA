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

const stateChecker = ($val, obj, key, defval, deep) => {
  // debugger
  let k = key, match = /(.+)\.(.+)/.exec(key), attr = false
  if(match !== null)
  {
    k = match[1]
    attr = match[2]
  }
  if(typeof obj !== 'undefined' && typeof obj[k] !== 'undefined')
  {
    if(deep)
    {
      expect($val).to.deep.equal(obj[k])
    }
    else
    {
      if(!!attr)
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
    if(deep)
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
  cy.exec(`psql -U yada -w -h signals-test.qdss.io -c \
          "delete from yada_query_conf where app like 'CYP%';\
           delete from yada_ug where app like 'CYP%';\
           delete from yada_query where app like 'CYP%';\
           delete from yada_param where target like 'CYP%';\
           delete from yada_prop where target like 'CYP%';\
           delete from yada_a11n where target like 'CYP%';"`)
})

Cypress.Commands.add("confirmSave", (count) => {
  cy.exec(`psql -U yada -w -h signals-test.qdss.io -c \
          "select * from yada_query where qname = 'CYP0 QNAME${count}';"`)
})
