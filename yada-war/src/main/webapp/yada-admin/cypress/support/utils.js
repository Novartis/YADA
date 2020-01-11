
export const visit = () => {
  cy.visit('https://localhost.qdss.io:8082/')
}

export const login = (u,p) => {

  cy.getCookie('yadagroups').then((yadagroups) => {
    cy.getCookie('yadajwt').then((yadajwt) => {
      if(yadagroups == null || yadajwt == null)
      {
        let username = Cypress.env('YADA_USER')
        let password = Cypress.env('YADA_PASS')
        if(typeof u !== 'undefined')
          username = u
        if(typeof p !== 'undefined')
          password = p

        cy.clearCookies()
        cy.visit('https://yada-test.qdss.io/yada-admin/')

        cy.get('body').then($body => {
          if($body.find('#username').length)
          {
            cy.get('#username').type(username)
            cy.get('#password').type(password)
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
        })
      }
      cy.getCookie('yadagroups').should('exist')
    })
  })
}

export const getAppsPanel = () => cy.get('#apps-panel')
export const getConfPanel = () => cy.get('#conf-panel')
export const getQueryListPanel = () => cy.get('#query-list-panel')
export const getQueryEditPanel = () => cy.get('#query-edit-panel')
export const getAppsTab = () => cy.get('#apps-tab')
export const getConfTab = () => cy.get('#conf-tab')
export const getQueryListTab = () => cy.get('#query-list-tab')
export const getQueryEditTab = () => cy.get('#query-edit-tab')
export const getMainMenu = () => cy.get('.main-menu')
export const getFilter = () => cy.get('.filter')
export const getFilterLabel = () => cy.get('.filter .label')
export const getAppListItems = () => cy.get('#app-list .applistitem',{timeout:10000})
export const getQueryListItems = () => cy.get('#query-list table>tbody>tr',{timeout:10000})
export const hasCorrectMenuItems = () => {
  cy.getState()
  .then($state => $state.tabs[$state['activeTab'].replace(/-tab/,'')].menuitems)
  .then($menuNames => {
    cy.get('span.value').each(($el, index, $list) => {
      cy.wrap($el).should('have.text',$menuNames[index].value)
    })
  })
}
export const openMenu = () => {
  cy.get('.main-menu > .menu > .item').click()
}
export const chooseMenuOption = (option) => {
  openMenu()
  return cy.get('.main-menu > .menu > .item > .menu.visible > div.item').contains(option).click()
}

export const createApp = (count,edit) => {
  return chooseMenuOption('Add App').then(() => {
    // confirm conf tab is working and rename the new app
    cy.getState().its('activeTab').should('eq','conf-tab')
    cy.getState().its('creating').should('eq',true)
    cy.getState().its('app').should('match',/APP[0-9]+/)
    cy.get('input[name="app"]').should('not.have.attr','readonly')
    cy.get('input[name="app"]').invoke('val').then(val => {
      cy.getState().its('app').should('eq',val)
      cy.getState().its('unsavedChanges').should('be.gt',0)
    })
    cy.get('.background.unsaved').should('exist')
    getAppsTab().should('not.have.class','disabled')
    getQueryListTab().should('have.class','disabled')
    getQueryEditTab().should('have.class','disabled')
    cy.get('input[name="app"]').clear().type(`CYP${count}`)
    if(typeof edit === 'undefined' || edit === null || edit)
    {
      cy.get('#conf-panel .CodeMirror textarea').type(`#CYP${count} Configuration content test`,{force:true})
      cy.get('input[name="name"]').clear().type(`CYP${count} Name content`)
      cy.get('input[name="descr"]').clear().type(`CYP${count} Description content test`)
    }
  })
}

export const createQuery = (count) => {
  return chooseMenuOption('Add Query').then(() => {
    // confirm conf tab is working and rename the new app
    cy.getState().its('activeTab').should('eq','query-edit-tab')
    cy.getState().its('creating').should('eq',true)
    cy.getState().its('renaming').should('eq',false)
    cy.getState().its('qname').should('match',/CYP0 [0-9]+/)
    cy.get('input[name="qname"]').should('not.have.attr','readonly')
    cy.get('input[name="qname"]').clear().type(`QNAME${count}`)
    cy.get('input[name="qname"]').invoke('val').then(val => {
      cy.getState().its('qname').should('eq',`CYP0 ${val}`)
      cy.getState().its('unsavedChanges').should('be.gt',0)
    })
    cy.get('.background.unsaved').should('exist')
    // getAppsTab().should('not.have.class','disabled')
    // getQueryListTab().should('not.have.class','disabled')
    // getQueryEditTab().should('not.have.class','disabled')

    // cy.get('.query-editor-view .CodeMirror textarea').type(`-- Replace with YADA markup`,{force:true})
  })
}

export const confirmConfig = (count) => {
  getConfTab().click().then(() => {
    cy.get('#conf-panel .CodeMirror textarea',{timeout:10000}).should('have.value',`#CYP${count} Configuration content test`)
    cy.get('input[name="app"]').should('have.value',`CYP${count}`)
    cy.get('input[name="name"]').should('have.value',`CYP${count} Name content`)
    cy.get('input[name="descr"]').should('have.value',`CYP${count} Description content test`)

  })
}
