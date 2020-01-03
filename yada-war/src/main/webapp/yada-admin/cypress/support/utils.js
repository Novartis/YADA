
export const visit = () => {
  cy.visit('https://localhost.qdss.io:8082/')
}

export const login = (u,p) => {
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
    cy.getCookie('yadagroups').should('exist')
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
export const getAppListItems = () => cy.get('#app-list .applistitem')
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
