
export const visit = skipWaiting => {
  console.log('visit this =', this)

  if (typeof skipWaiting !== 'boolean') {
    skipWaiting = false
  }

  const waitForInitialLoad = !skipWaiting
  console.log('visit will wait for initial todos', waitForInitialLoad)
  // if (waitForInitialLoad) {
  //   cy.server()
  //   cy.route('/todos').as('initialTodos')
  // }
  cy.visit('https://localhost.qdss.io:8082/')
  // console.log('cy.visit /')
  // if (waitForInitialLoad) {
  //   console.log('waiting for initial todos')
  //   cy.wait('@initialTodos')
  // }
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
export const hasVisibleFilter = () => cy.get('.filter')
export const getFilterLabel = () => cy.get('.filter .label')
export const getAppListItems = () => cy.get('#app-list .applistitem')
export const getQueryListItems = () => cy.get('#query-list table>tbody>tr')
export const hasCorrectMenuItems = () => {
  cy.getState()
  .then($state => $state.tabs[$state['activeTab'].replace(/-tab/,'')].menuitems)
  .then($menuNames => {
    cy.get('span.value').each(($el, index, $list) => {
      cy.wrap($el).should('have.text',$menuNames[index].value)
    })
  })
}
