#!/usr/bin/env node

const fs = require('fs')
const del = require('del')
const YADA_INDEX = '../conf/YADA_LIB'
const YADA_CONF = 'conf.json'

var argv = require('minimist')(process.argv.slice(2), {
  boolean:['r'],
  alias:{
    r: 'reset',
    h: 'help',
    h: '?'
  },
  defaults: {
    f: 'csv'
  }
})

if(!!argv.h || !!argv.help || !!argv['?'])
{
  console.log('Usage: rdbmsToJson.js [-r] yadaconf yadaquery')
  console.log('  Requirements:');
  console.log('    yadaconfs      a file containing the contents of the YADA_QUERY_CONF table')
  console.log('    yadaqueries    a file containing the contents of the YADA_QUERY,')
  console.log('                   YADA_PARAM, YADA_PROP, and YADA_A11N tables.')
  console.log('                   See below for details.')
  console.log()
  console.log('  Options:')
  console.log('    -h, --help, -? display this help')
  console.log('    -r, --reset    delete the "../conf/YADAIndex" folder to rerun')
  console.log('                   this script cleanly')
  console.log('')
  console.log('  Files:')
  console.log('    yadaconf       assumes tab-separated output from:')
  console.log('                   SELECT app,name,descr,conf FROM YADA_QUERY_CONF;')
  console.log('')
  console.log('    yadaquery      assumes tab-separated output from:')
  console.log('                   SELECT')
  console.log('                     q.app,')
  console.log('                     q.qname,')
  console.log('                     q.query,')
  console.log('                     p.id paramid,')
  console.log('                     p.name paramname, ')
  console.log('                     p.value paramvalue, ')
  console.log('                     p.rule paramrule, ')
  console.log('                     r.name propname, ')
  console.log('                     r.value propvalue, ')
  console.log('                     n.policy, ')
  console.log('                     n.qname qualifier, ')
  console.log('                     n.type ')
  console.log('                   FROM ')
  console.log('                     yada_query q')
  console.log('                     RIGHT JOIN yada_query_conf c ON q.app = c.app')
  console.log('                     LEFT JOIN yada_param p ON q.qname = p.target')
  console.log('                     LEFT JOIN yada_prop  r ON q.qname = r.target')
  console.log('                     LEFT JOIN yada_a11n  n ON q.qname = n.target')
  console.log('                   ORDER BY 1,2,3,4,5,6,7,8,10,11,12')
  process.exit()
}

if(!!argv.r || !!argv.reset)
{
  del.sync([YADA_INDEX],{force:true})
}

if(!fs.existsSync(YADA_INDEX))
{
  fs.mkdirSync(YADA_INDEX)
}

// list of files
const src = argv._

// for each file
src.forEach(f => {
  // get the data
  const raw = fs.readFileSync(`./${f}`)
  // is it csv
  if(!/.+\.json/.test(f))
  {
    // encode it
    const data  = raw.toString()
    // split to array of lines
    const lines = data.split(/\n|\r|\r\n/)
    // define vars
    let appPath, appConfPath, app, lastapp, lastqname, sameapp, qname, sameqname, param, prop,
        qpath, name, descr, source, confPath, singleLineQuery, multiLineQueryStart, multiLineQueryEnd

    // are we eval a conf file?
    if(/yadaconf/.test(f))
    {
      // get the header
      let hdr    = lines[0].replace(/"/g,'').split(/\t/)
      // init the config
      let config = {}
      // iterate the lines
      lines.slice(1).forEach(line => {
        if(!/^$/.test(line))  // skip blank lines
        {
          // get the fields
          let fields = line.split(/\t/)

          // is it a full line?  if so it's not a property on a separate line
          if(fields.length == hdr.length)
          {
            // write the file from the previous iteration
            // this is necessary to ensure all properties belonging to
            // "previous" full line are written to the correct file
            if(typeof config['source'] !== 'undefined')
            {
              confPath = `${YADA_INDEX}/${app}/conf.json`
              if(!fs.existsSync(confPath))
              {
                fs.writeFileSync(confPath,JSON.stringify(config,null,2));
                // reset the config
                config = {}
              }
            }

            // set the app, appPath, create the directory
            app = fields[hdr.indexOf('app')]
            appPath = `${YADA_INDEX}/${app}`
            if(!fs.existsSync(appPath))
            {
              fs.mkdirSync(appPath)
            }

            // store the other values
            name = fields[hdr.indexOf('name')]
            descr = fields[hdr.indexOf('descr')]

            // set the source, could be jdbc or http[s]
            source = /^jdbc/.test(fields[hdr.indexOf('conf')])
                    ? fields[hdr.indexOf('conf')].split(/=/)[1]
                    : fields[hdr.indexOf('conf')]
            config['source'] = source
          }
          else
          {
            // it's a property line, create the props obj if necessary
            if(!('props' in config))
            {
              config['props'] = {}
            }
            // split and store the key/value pair
            let kvp = line.split(/=/)
            if(kvp.length == 2)
            {
              config['props'][kvp[0]] = kvp[1]
            }
          }

          // add non-null props to config
          if(!(typeof name === 'undefined' || name === null || name === "" || name === 'NULL'))
          {
            config['name'] = name
          }
          if(!(typeof descr === 'undefined' || descr === null || descr === "" || descr === 'NULL'))
          {
            config['descr'] = descr
          }
        }
      })
      // write the final iteration
      confPath = `${YADA_INDEX}/${app}/conf.json`
      if(!fs.existsSync(confPath))
      {
        fs.writeFileSync(confPath,JSON.stringify(config,null,2));
        config = {}
      }
    }
    else
    {
      // get the header
      let   hdr   = lines[0].split(/\t/)

      // set the query object -- will be reused for each query
      let   query = {}

      function processField(fields,col,c,i) {
        if(/param|policy|qualifier|type/.test(col)) // it's a param or spec column
        {
          if(/param/.test(col)) // its a param column
          {

            if(!('params' in query) ) // it may or may not be the first param for the query
            {
              query['params'] = []
            }

            if(/paramid/.test(col)) // we don't store the id anymore but it marks index
            {
              param = {}
            }

            else if(/paramrule/.test(col)) // last param column
            {
              param[col.replace(/param/,'')] = parseInt(c) // omit the 'param' part of the name
              if(param !== null)
              {
                query['params'].push(param)
              }
            }
            else if(/paramvalue/.test(col))
            {
              if(/,/.test(c))
              {
                let vals = c.split(/,/).reduce((a1,c1) => {
                  let p = c1.split(/=/)
                  if(typeof p[1] === 'undefined')
                  {
                    p.push(p[0])
                    p[0] = 'plugin'
                  }
                  a1[p[0]] =  p[1]
                  return a1
                },{})
                param['value'] = vals['plugin']
                param['spec'] = {}
                if('content.policy.predicate' in vals)
                {
                  param['spec']['predicate'] = vals['content.policy.predicate']
                  param['spec']['policy'] = 'C'
                }
                else if('execution.policy.columns' in vals)
                {
                  param['spec']['columns'] = vals['execution.policy.columns']
                }
                else if('execution.policy.indexes' in vals)
                {
                  param['spec']['indexes'] = vals['execution.policy.indexes']
                }
                else if('execution.policy.indices' in vals)
                {
                  param['spec']['indices'] = vals['execution.policy.indices']
                }
              }
              else
              {
                param['value'] = c
              }
            }
            else
            {
              param[col.replace(/param/,'')] = c
            }
          }
          else if(/policy|qualifier|type/.test(col)) // spec columns
          {
            if(/policy/.test(col) && param['name'] === 'pl')
            {
              if(typeof param['spec'] === 'undefined')
                param['spec'] = {}
              param['spec'][col] = c
            }
            else
            {
              if(typeof param['spec'] === 'undefined') // there's an exception in the data where 'policy' is null but 'type' isn't
              {
                param['spec'] = {}
              }

              // change the key val if necessary
              if(param['spec']['policy'] === 'E' && col === 'qualifier')
              {
                col = 'protector'
              }

              // conform the type vals
              if(col === 'type')
              {
                if(/whitelist/.test(c))
                  c = 'allow'
                else if(/blacklist/.test(c))
                  c = 'deny'
              }

              param['spec'][col] = c
            }
          }
        }
        else if(/prop/.test(col)) // its a prop column
        {
          if(!('props' in query)) // it may or may not be the first prop for the query
          {
            query['props'] = []
          }
          if(/name/.test(col))
          {
            prop = {}
            prop[col.replace(/prop/,'')] = c
          }
          else
          {
            prop[col.replace(/prop/,'')] = c
            if(prop !== null)
            {
              let propstr = JSON.stringify(prop)
              if(!query['props'].some(p => { return JSON.stringify(p) == propstr }))
                query['props'].push(prop)
            }
          }
        }
        else
        {
          query[col] = c
        }
      }

      // iterate over the lines
      lines.slice(1).forEach((line,lineno) => {
        // not blank lies
        if(!/^$/.test(line))
        {
          // grab the fields
          let fields = line.split(/\t/)

          // is it a full line, or the start of a long query? if so it's not a query fragment
          if(fields.length === hdr.length)
          {
            singleLineQuery = true
            multiLineQueryStart = false
            multiLineQueryEnd = false
          }
          else if(fields.length == 3)
          {
            singleLineQuery = false
            multiLineQueryStart = true
            multiLineQueryEnd = false
          }
          else if(fields.length > 1)
          {
            singleLineQuery = false
            multiLineQueryStart = false
            multiLineQueryEnd = true
          }
          else // fragment
          {
            singleLineQuery = false
            multiLineQueryStart = false
            multiLineQueryEnd = false
          }

          if(singleLineQuery || multiLineQueryStart)
          {
            // grab the app, qname
            // q.app
            // q.qname

            app     = fields[hdr.indexOf('app')]
            // get qname
            qname   = fields[hdr.indexOf('qname')]

            // assume false in both cases and flip to true as needed
            sameapp = false
            sameqname = false
            if(typeof lastapp !== 'undefined')
            {
              if(qname === lastqname)
              {
                sameqname = true // also another row for this qname
              }

              if(!sameqname ) // new query on current row, so write the old one
              {
                let rx_prefix
                if(typeof lastapp === 'undefined' || lastapp === app)
                {
                  appPath = `${YADA_INDEX}/${app}`
                  rx_prefix = new RegExp(`^${app}\\s`)
                }
                else
                {
                  appPath = `${YADA_INDEX}/${lastapp}`
                  rx_prefix = new RegExp(`^${lastapp}\\s`)
                }
                qPath   = typeof lastqname === 'undefined' || sameqname
                          ? `${appPath}/${qname.replace(rx_prefix,'')}.json`
                          : `${appPath}/${lastqname.replace(rx_prefix,'')}.json`
                if(!fs.existsSync(qPath))
                {
                  fs.writeFileSync(qPath,JSON.stringify(query,null,2));
                }
                query = {}
              }
            }

            // q.query
            // p.id
            // p.name
            // p.value
            // p.rule
            // r.name
            // r.value
            // n.policy
            // n.qname qualifier
            // n.type



            // process the other fields
            fields.slice(2).forEach((c,i) => {
              let curHdrIdx = i+2
              let col = hdr[curHdrIdx]
              if(c !== 'NULL' && c !== "")
              {
                processField(fields,col,c,i)
              }
            })
          }
          // it's the last query fragment followed by other fiedls
          else if(multiLineQueryEnd)
          {
            query['query'] = `${query.query}${fields[0]}`
            fields.slice(1).forEach((c,i) => {
              let colHdrIndex = i+3
              let col = hdr[colHdrIndex]
              if(c !== 'NULL' && c !== "")
              {
                processField(fields,col,c,i)
              }
            })
          }
          else
          {
            query['query'] = `${query.query}${fields[0]}`
          }
          lastapp = app
          lastqname = qname
        }
      }) // lines
    }
  }
  else //json
  {
    const data = JSON.parse(raw)
    if(/yadaconf/.test(f))
    {
      data.map(app => {
        const appPath = `${YADA_INDEX}/${app.app}`
        const appConfPath = `${appPath}/conf.json`

        if(!fs.existsSync(appPath))
        {
          fs.mkdirSync(appPath)
        }
        if(!fs.existsSync(appConfPath))
        {
          const conf = {"source":""}
          if(typeof app['name'] !== 'undefined' && app['name'] !== "")
            conf['name'] = app['name']
          if(typeof app['descr'] !== 'undefined' && app['descr'] !== "")
            conf['descr'] = app['descr']
          const appConf  = app['conf'] !== null ? app['conf'].split(/\n/) : ''
          if(/^jdbc.+$/.test(appConf[0]))
          {
            conf['source'] = appConf[0].split(/=/)[1]
            conf['props'] = appConf.slice(1).reduce((a,c) => {
              const pair = c.split(/=/)
              a[pair[0]] = pair[1]
              return a
            },{})
          }
          else
          {
            conf['source'] = app['conf']
          }
          fs.writeFileSync(appConfPath,JSON.stringify(conf,null,2));
        }
      })
    }
    else
    {
      data.forEach(yq => {
        yq.queries.map(q => {
          const prefixRx = new RegExp(`^${yq.app}\s`)
          const qPath = `${YADA_INDEX}/${yq.app}/${q.qname.replace(prefixRx,'')}.json`
          if(!fs.existsSync(qPath))
          {
            const query = {
              "query":q.query.replace(/\n+$/,''), //omit trailing line feeds
              "comments":q.comments
            }

            if(typeof q.params !== 'undefined' && q.params !== null)
            {
              query["params"] = q.params.reduce((a,c) => {
                for(const attr in c)
                {
                  // omit empty sec props
                  if(/policy|qualifier|type/.test(attr) && c[attr] === null)
                  {
                    delete c[attr]
                  }
                  // handle sec props
                  if('value' === attr && /policy/.test(c['value']))
                  {
                    const props = c[attr].split(/,/)
                    c[attr] = props[0]
                    props.slice(1).filter(prop => {
                      // columns, indexes, indices, predicate, qualifier
                      // need to check key:value pair for 'void'
                      // only include non-void
                      // also change name to valid property name
                      let kp = prop.split(/=/)
                      let key = ''
                      if(kp[1] !== 'void')
                      {
                        if(/execution/.test(kp[0]))
                        {
                          if(/columns/.test(kp[0]))
                          {
                            key = 'columns'
                          }
                          else if(/indexes/.test(kp[0]))
                          {
                            key = 'indexes'
                          }
                          else if(/indices/.test(kp[0]))
                          {
                            key = 'indices'
                          }
                        }
                        else if(/content/.test(kp[0]))
                        {
                          key = 'predicate'
                        }
                        else if(/url/.test(kp[0]))
                        {
                          key = 'urlpattern'
                        }
                        c[key] = kp[1]
                      }
                    })
                  }
                }
                a.push(c)
                return a
              },[])
            }

            if(typeof q.props !== 'undefined' && q.props !== null)
            {
              query["props"] = q.props
            }

            fs.writeFileSync(qPath,JSON.stringify(query,null,2))
          }
        })
      })
    }
  }
})
