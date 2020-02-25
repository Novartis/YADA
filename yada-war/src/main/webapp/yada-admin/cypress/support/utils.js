
export const visit = () => {
  cy.visit('https://localhost.qdss.io:8082/')
}

export const login = (u,p) => {
  cy.getCookies().then((cooks) => {
    console.log(cooks)
    if(cooks.reduce((a,c) => {
        console.log(c.name)
        return a += /yada(?:groups|jwt)/.test(c.name) ? 1 : 0
      },0) < 2)
    {
      cy.clearCookie('yadajwt')
      cy.clearCookie('yadagroups')
      let username = Cypress.env('YADA_USER')
      let password = Cypress.env('YADA_PASS')
      if(typeof u !== 'undefined')
        username = u
      if(typeof p !== 'undefined')
        password = p

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
  })
  // cy.getCookie('yadagroups').then((yadagroups) => {
  //   console.log(yadagroups)
  //   cy.getCookie('yadajwt').then((yadajwt) => {
  //     console.log(yadajwt)
  //     if(yadagroups == null || yadajwt == null)
  //     {
  //       let username = Cypress.env('YADA_USER')
  //       let password = Cypress.env('YADA_PASS')
  //       if(typeof u !== 'undefined')
  //         username = u
  //       if(typeof p !== 'undefined')
  //         password = p
  //
  //       cy.visit('https://yada-test.qdss.io/yada-admin/')
  //
  //       cy.get('body').then($body => {
  //         if($body.find('#username').length)
  //         {
  //           cy.get('#username').type(username)
  //           cy.get('#password').type(password)
  //           cy.get('.login-form .ui.button').click()
  //
  //           cy.location().should(loc => {
  //             expect(loc.pathname).to.eq('/yada-admin/index.html')
  //           })
  //         }
  //         else
  //         {
  //           cy.location().should(loc => {
  //             expect(loc.pathname).to.eq('/yada-admin/')
  //           })
  //           cy.get('#app').should('exist')
  //         }
  //       })
  //     }
  //     cy.getCookie('yadagroups').should('exist')
  //   })
  // })
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
export const getAppListItems = () => cy.get('#app-list').find('.applistitem',{timeout:10000})
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

export const addParameter = (count) => {
  const qname = `QNAME${count}`
  return getQueryListTab().click().then(() => {
    cy.get('.query-list > tbody > tr').contains('td:first-child()', qname)
    .then($el => {cy.wrap($el[0]).click()})
    .then(() => {
      cy.getState().its('activeTab').should('eq','query-edit-tab')
      chooseMenuOption('Add Param').then(() => {
        cy.get('.params>table>tbody>tr',{timeout:10000}).its('length').should('be.gt',0)
        cy.getState().its('unsavedChanges').should('be.gt',0)
        cy.get('.background.unsaved').should('exist')
      })
    })
  })
}

export const prepParamForEdit = (count) => {
  return cy.get('body').type('{meta}S').wait(500).then(() => {
    cy.getState().its('unsavedChanges').should('eq',0)
    cy.get('.background.unsaved').should('not.exist')
    cy.confirmParamSave(count).then(result => {
      cy.wrap(Array.isArray(JSON.parse(result.stdout))).should('eq',false)
      cy.wrap(typeof (JSON.parse(result.stdout))).should('eq','object')
    })
  })
}

export const prepMultiParamForEdit = (count) => {
  return cy.get('body').type('{meta}S').wait(500).then(() => {
    cy.getState().its('unsavedChanges').should('eq',0)
    cy.get('.background.unsaved').should('not.exist')
    cy.confirmParamSave(count).then(result => {
      const reso = result.stdout.replace(/\n/g,',')
      cy.wrap(Array.isArray(JSON.parse(`[${reso}]`))).should('eq',true)
      // cy.wrap(typeof (JSON.parse(result.stdout))).should('eq','object')
    })
  })
}

export const setOneOfManyParameter = (count,row,type,name,value,dfault) => {
  return prepMultiParamForEdit(count).then(() => {
    _setParameter(count,row,type,name,value,dfault)
  })
}

export const setParameter = (count,row,type,name,value,dfault) => {
  return prepParamForEdit(count).then(() => {
    _setParameter(count,row,type,name,value,dfault)
  })
}

export const _setParameter = (count,row,type,name,value,dfault) => {
  return cy.get(`.params>table>tbody>tr:nth-child(${row})>td.param-name`).click().then(td => {
    cy.wrap(td).find(`div[data-value="${name}"]`).click().then(el => {
      cy.wrap(td).find('.value').should('have.text',name)
      cy.wrap(td).next().find('.value').should('contain',dfault)
      cy.getState().its('unsavedParams').should('be.gt',0)

      cy.get(`.params>table>tbody>tr:nth-child(${row})>td.param-val`).click().then(td => {
        if(type !== 'boolean')
        {
          cy.wrap(td).find('input').type(value).blur()
        }
        cy.getState().its('unsavedParams').should('be.gt',0)

        cy.save().then(() => {
          cy.get('.background.unsaved').should('not.exist')
          cy.confirmParamSave(count,name,value,1,row).then(result => {
            const reso = JSON.parse(result.stdout)
            cy.wrap(Array.isArray(reso)).should('eq',false)
            cy.wrap(typeof reso).should('eq','object')
          })
        })
      })
    })
  })
}

export const setInvalidParameter = (count,row,type,name,value,dfault) => {
  return prepParamForEdit(count).then(() => {
    cy.get(`.params>table>tbody>tr:nth-child(${row})>td.param-name`).click().then(td => {
      cy.get(`div[data-value="${name}"]`).click().then(el => {
        cy.wrap(td).find('.value').should('have.text',name)
        cy.wrap(td).next().find('.value').should('contain',dfault)
        cy.getState().its('unsavedParams').should('be.gt',0)
      })
    })
    cy.get(`.params>table>tbody>tr:nth-child(${row})>td.param-val`).click().then(td => {
      if(type == 'number')
      {
        cy.wrap(td).find('input').type(value).blur()
      }
      else if(type == 'string')
      {
        cy.wrap(td).find('input').blur()
      }
      cy.getState().its('unsavedParams').should('be.gt',0)
    })
    cy.get('body').type('{meta}S').wait(500).then(() => {
      cy.getState().its('unsavedChanges').should('be.gt',0)
      cy.getState().its('unsavedParams').should('be.gt',0)
      cy.getState().its('showWarning').should('eq',true)
      cy.get('.background.unsaved').should('exist')
      cy.get('.error').should('exist')
      cy.get('.warn .ui.button.dang').should('be.visible')
      cy.get('.warn .ui.button.dang').click()
      cy.getState().its('unsavedChanges').should('be.gt',0)
      cy.getState().its('unsavedParams').should('be.gt',0)
      cy.getState().its('showWarning').should('eq',false)
      cy.get('.background.unsaved').should('exist')
      cy.get('.error').should('exist')
    })
  })
}

export const createMultipleParams = (count,names) => {
  return setOneOfManyParameter(count,1,'boolean',names[0],'false','false')
  .then(() => {
    setOneOfManyParameter(count,2,'string',names[1],'testing','Change me...')
  })
  .then(() => {
      setOneOfManyParameter(count,3,'number',names[2],'-1','20')
  })
}

export const testParameterDeletion = (count,names,row) => {
  cy.get(`.params>table>tbody>tr:nth-child(${row})>td.param-action`).trigger('mouseenter')
  .then((td) => {
    cy.wrap(td).find('button').then((button) => {
      cy.wrap(button).trigger('click').wait(500).then(() => {
        cy.get('.ui.dimmer.visible.active').should('exist')
        cy.get('.confirm.visible.active').should('exist')
        cy.get('.confirm .ui.positive.button').click().then(() => {
          cy.get('.params>table>tbody>tr').then(trs => {
            cy.wrap(trs).should('have.length',2)
            cy.wrap(trs[0]).find('td.param-id').should('contain', row != 1 ? 1 : 2)
            cy.wrap(trs[0]).find('td.param-name').should('contain',row != 1 ? names[0] : names[1])
            cy.wrap(trs[1]).find('td.param-id').should('contain', row != 3 ? 3 : 2)
            cy.wrap(trs[1]).find('td.param-name').should('contain',row != 3 ? names[2] : names[1])
            cy.save().then(() => {
              cy.confirmParamSave(count).then(result => {
                const reso = result.stdout.replace(/\n/g,',')
                const array  = JSON.parse(`[${reso}]`)
                cy.wrap(Array.isArray(array)).should('eq',true)
                cy.wrap(array).should('have.length',2)
                cy.wrap(array[0].name).should('eq',row != 1 ? names[0] : names[1])
                cy.wrap(array[1].name).should('eq',row != 3 ? names[2] : names[1])
              })
            })
          })
        })
      })
    })
  })
}

export const testParamDnD = (count,names,src,tgt) => {
  let source = `.params>table>tbody>tr:nth-child(${src})>td.param-id`
  let target = `.params>table>tbody>tr:nth-child(${tgt})>td.param-id`
  cy.get(source).drag(target).then(() => {
    cy.save().then(() => {
      cy.confirmParamSave(count).then(result => {
        const reso = result.stdout.replace(/\n/g,',')
        const array  = JSON.parse(`[${reso}]`)
        cy.wrap(Array.isArray(array)).should('eq',true)
        cy.wrap(array).should('have.length',3)
        if(src == 1 && tgt == 2)
        {
          cy.wrap(array.filter(o => o.id == 1)[0].name).should('eq',names[1])
          cy.wrap(array.filter(o => o.id == 2)[0].name).should('eq',names[0])
          cy.wrap(array.filter(o => o.id == 3)[0].name).should('eq',names[2])
        }
        else if(src == 1 && tgt == 3)
        {
          cy.wrap(array.filter(o => o.id == 1)[0].name).should('eq',names[2])
          cy.wrap(array.filter(o => o.id == 2)[0].name).should('eq',names[1])
          cy.wrap(array.filter(o => o.id == 3)[0].name).should('eq',names[0])
        }
        else if(src == 2 && tgt == 1)
        {
          cy.wrap(array.filter(o => o.id == 1)[0].name).should('eq',names[1])
          cy.wrap(array.filter(o => o.id == 2)[0].name).should('eq',names[0])
          cy.wrap(array.filter(o => o.id == 3)[0].name).should('eq',names[2])
        }
        else if(src == 2 && tgt == 3)
        {
          cy.wrap(array.filter(o => o.id == 1)[0].name).should('eq',names[0])
          cy.wrap(array.filter(o => o.id == 2)[0].name).should('eq',names[2])
          cy.wrap(array.filter(o => o.id == 3)[0].name).should('eq',names[1])
        }
        else if(src == 3 && tgt == 1)
        {
          cy.wrap(array.filter(o => o.id == 1)[0].name).should('eq',names[2])
          cy.wrap(array.filter(o => o.id == 2)[0].name).should('eq',names[1])
          cy.wrap(array.filter(o => o.id == 3)[0].name).should('eq',names[0])
        }
        else if(src == 3 && tgt == 2)
        {
          cy.wrap(array.filter(o => o.id == 1)[0].name).should('eq',names[0])
          cy.wrap(array.filter(o => o.id == 2)[0].name).should('eq',names[2])
          cy.wrap(array.filter(o => o.id == 3)[0].name).should('eq',names[1])
        }
      })
    })
  })
}
