#!/bin/env python

import urllib, urllib2, json, argparse, sys
import cookielib

parser = argparse.ArgumentParser(description="Export Yada index")

parser.add_argument('--yada', default='http://localhost:8080/yada.jsp', nargs='?', help='URL-Path to the corresponding yada.jsp servlet (source)')
parser.add_argument('--yimport', nargs='?', help='for import of query collection, yada.jsp url of target environment')

parser.add_argument('--user', '-u', nargs='?', help='user in Yada-Admin for the export')
parser.add_argument('--password', '-p', nargs='?')
parser.add_argument('--load', nargs='?', help='use this former export file to list queries instead of retrieving from YADA environment')

parser.add_argument('--apps', '-a', nargs='*', help='subset of apps to concentrate on (default: all apps)', default=[])
parser.add_argument('--out', '-o', nargs='?', help='the file for the export output of the Yada index, which can later be used with --load')
parser.add_argument('--httpdebug', default=0)

parser.add_argument('--post', nargs='?', help='a file where to write the POSTable jsonParams (eg via curl) to reload an app (no authentication) [this option should be depecrated]')
parser.add_argument('--json', action='store_true', help='for --post, save as JSON instead of postable text (avoid application/x-www-form-urlencoded) [perhaps intended for testing this tool]')

parser.add_argument('--scm', nargs='?', help='save a file inerrently sorted so that it is easy to compare in source-control (git diff, svn diff etc)')
parser.add_argument('--defn', action='store_true', help='also save the APP definition, including Hikari pool definition (jdbc connection)')
parser.add_argument('--unmask', '--um', action='store_true', help='avoid masking the jdbc connection password for app definition (see --defn)')
parser.add_argument('--ts', action='store_true', help='with --scm, save the creation and modification timestamps user Ids')
parser.add_argument('--ls', action='store_true', help='with --scm, output as query definition and params as list of lists rather than list of objects')
parser.add_argument('--legacy', action='store_true', help='the intended Yada system where this export would be loaded is a legacy system that does not allow for complete deletion of a query\'s params')
parser.add_argument('--tgt7', action='store_true', help='the intended target Yada system is on Yada-7')
parser.add_argument('--src7', action='store_true', help='the source Yada system is on Yada-7')
parser.add_argument('--sync', action='store_true', help='query the target system before feeding it, so as to do updates and no do deletes/inserts of all queries')



args = parser.parse_args()



def checkOnArgs(args):
	warnings = []
	errors = []
	if(args.tgt7 and args.yimport is None): warnings.append("Option --tgt7 is useless without a --yimport ... Perhaps you want to use --legacy")
	# if(args.legacy and args.out is None and args)
	if(args.tgt7 and args.defn): errors.append("Using --defn with --tgt7 is not possible, as Yada 7 cannot define apps on the fly")
	if( (args.ls or args.ts) and args.scm is None): warnings.append("Options --ls or --ts are useless without option --scm")
	if(args.scm is not None and args.defn): warnings.append("Using --defn with --scm, please bear in mind that the app definition is not saved in SCM")
	if(args.sync and args.yimport is None): warnings.append("Using --sync is useless without --yimport")
	if(args.defn and args.unmask and args.yimport is None): warnings.append("Please note that --defn and --unmask will cause the JDBC connections to leak into the output file")

	if(len(errors)>0):
		print >>sys.stderr, errors

	if(len(warnings)>0):
		print >>sys.stderr, "Warnings:\n"
		print >>sys.stderr, warnings


	if(len(errors)>0):
		sys.exit(99)



checkOnArgs(args)

def parseUserPassUrl(url, args):
	""" read URL, username and password for Yada Admin Login from args
	    if the url is of the form http://user:pass@server/ ...
		then the user & pass values will be used rather than args.user and args.password """
	import re

	nakedUrl = url
	nakedUser = args.user
	nakedPass = args.password

	pat1 = re.compile(r"(https?)://([^:]+):(.+)@([^@]+)/(.*)")
	pat2 = re.compile(r"(https?)://([^@]+)@([^@]+)/(.*)")

	m1 = pat1.match(url)
	m2 = pat2.match(url)
	# the naked are going to be covered...
	if(m1 is not None):
		nakedUrl = "{0}://{1}/{2}".format(m1.groups()[0], m1.groups()[3], m1.groups()[4]);
		nakedUser = m1.groups()[1]
		nakedPass = m1.groups()[2]
	elif(m2 is not None):
		nakedUrl = "{0}://{1}/{2}".format(m2.groups()[0], m2.groups()[2], m2.groups()[3])
		nakedUser = m1.groups()[1]

	return (nakedUser, nakedPass, nakedUrl)






def yadaLogin(yada, args):
	""" ask Yada to authenticate the yada admin credentials we have, return cookies if successful
	    Authentication is done using the YADA check credentials / YADA Gatekeeper scheme. """

	uu,pp,url = parseUserPassUrl(yada, args)

	uri = "{2}?q=YADA+check+credentials&p={0},{1}".format(uu,pp,url)

	cookies, content = getSockContent(uri, None)
	rs = json.loads( content )
	version = 'unknown'

	if(rs.has_key('version')):
		version = rs['version']

	if(rs.has_key('RESULTSET') and rs['RESULTSET'].has_key('ROWS') and len(rs['RESULTSET']['ROWS'])==1 and rs['RESULTSET']['ROWS'][0].has_key('AUTH') and rs['RESULTSET']['ROWS'][0]['AUTH']):
		return cookies

	raise RuntimeException("not authenticated with provided credentials...")


def getSock(uri, cookies,method='GET', data=None):
	""" prepare the 'socket' object for consumption.
            add cookies if provided

	    handle POSTS with application/x-www-form-urlencoded (assumed UTF8)
	    if data is provided, pass it in the body (not tested with GETs)
		This provides the 'socket' object for a consumer to get info() and read()  then close() """
	req = urllib2.Request(uri) if(data is None) else urllib2.Request(uri, data=urllib.urlencode(data))
	req.get_method = lambda: method
	req.add_header('Accept', 'application/json')

	if(method == 'POST'):
		req.add_header('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')

	if(cookies is not None):
		req.add_header('Cookie', cookies)

	opener = urllib2.build_opener(urllib2.HTTPHandler(debuglevel=args.httpdebug))
	sock   = opener.open(req)
	return sock



def getSockContent(uri, cookies,method='GET', data=None):
	""" prepare the the yada request, issue the request, return the raw content body, return new cookies if any (old cookies otherwise)"""
	#print uri
	sock = getSock(uri, cookies,method,data)
	sockInfo = sock.info()
	#print sockInfo

	ncookies = sockInfo['Set-Cookie'] if(sockInfo.has_key('Set-Cookie')) else cookies
	content = sock.read()
	sock.close()
	#print (cookies, ncookies)

	return ncookies, content



def listYadaApps(yada,args, cookies, legacy7=False):
	""" for the logged on user, list the apps they have in which they have visibility"""
	yoyada = parseUserPassUrl(yada, args)[2]
	myapps = ("{0}?q=YADA+apps&pz=-1&p="+args.apps[0].lower()) if(legacy7) else "{0}?q=YADA+select+apps&pz=-1&s=a.app"
	uri= myapps.format( yoyada )
	ncookies, content = getSockContent(uri, cookies)
	rows = json.loads(content)['RESULTSET']['ROWS']
	if(legacy7):
		rows = map(lambda it:  {"APP": it["LABEL"]}, rows)
	return ncookies, rows

def listYadaAppQueriesAndMore(yada, args, cookies, app):
	""" for the logged on user, and an app, list the queries, default params and props"""
	menu = {"queries": {"qname":"YADA queries", "DATA":"APP"},
		"params" : {"qname":"YADA select default params for app", "DATA":"APP"},
		"props"  : {"qname":"YADA select props like target", "DATA":"TARGET"} }

	info = {}

	for k in menu.keys():
		yada = parseUserPassUrl(yada,args)[2]
		uri = "{0}?{1}".format(yada, urllib.urlencode({"pz": "-1", "p": app, "qname": menu[k]})) if(type(menu[k]) is str) else None
		uriData = None
		if(uri is None):
			jp = {"qname": menu[k]["qname"], "DATA": [{}]}
			jp["DATA"][0][ menu[k]["DATA"] ] = app
			uriData = {"pz": "-1", "c": "false", "j": json.dumps([jp],indent=0)}
			uri = yada
			#print (jp, uri,uriData)

		ncookies, content = getSockContent(uri, cookies, 'POST', uriData)
		info[k] = json.loads(content)['RESULTSET']['ROWS']
		cookies = ncookies

	info['app'] = app

	return cookies, info



def maskPassword(conf):
	""" parse the Hikari Pool configuration string (similar to java Properties) to mask the password """
	import re
	passmatch = re.compile(r"\s*password\s*=", re.IGNORECASE)
	lconf = re.split(r"(\r?\n)", conf) # note: the use of matching group is critical to avoid removing newlines from the list
	lconf = map(lambda txt: "password = **********" if(passmatch.match(txt)) else txt, lconf)
	return "".join(lconf)

def obtainYadaQueriesFromEnvironment(yada, args, env='source'):
	if(env not in ['source', 'target']): raise ValueError("env must be either source or target, not {0}".format(env))
	# obtains cookies, but only for "Yada 8 system (and beyond?)"
	cooks = yadaLogin(yada, args) if((env == 'target' and not args.tgt7) or (env=='source' and not args.src7)) else None
	cooks, apps = listYadaApps(yada,args,cooks, legacy7 = ((env=='target' and args.tgt7) or (env=='source' and args.src7)))
	selectedApps = []


	for app in apps:
		if(len(args.apps)>0 and app["APP"] not in args.apps):
			print >>sys.stderr, "{0}: {1} app is not selected.".format(app["APP"],env)
			continue
		else:
			print >>sys.stderr, "{0}: {1} app is selected...".format(app["APP"], env)

		ncooks, info = listYadaAppQueriesAndMore(yada, args,cooks,app['APP'])
		if(args.defn):
			info["conf"] = app['CONF'] if(args.unmask) else maskPassword(app['CONF'])
			info["name"] = app['NAME']
			info["description"] = app['DESCR']
			info["active"] = app['ACTIVE'] if(args.unmask) else "0"
		selectedApps.append(info)
		cooks = ncooks

	return selectedApps



def prepareSCMdump(args, exportedApps):
	"""neatly align all queries with sensible sorting etc so that a JSON output in Source Control Managment is a good way to see/study difference in versions"""
	import re

	def stuffIt(src, danse):
		hout = {}
		lout = []
		for k in danse:
			hout[k] = src[k]
			lout.append([k, src[k]])
		return hout, lout


	exportedApps = sorted(exportedApps, key = lambda  x: x["app"])
	scmExport = []
	for app in exportedApps:
		queries = sorted(app["queries"], key = lambda x: x["QNAME"])
		appq= []
		appql=[]
		for q in queries:
			danse = ["QNAME","QUERY","COMMENTS", "DEFAULT_PARAMS"]
			if(args.ts):
				danse += ["CREATED", "CREATED_BY", "MODIFIED", "MODIFIED_BY"]

			qq, qpl = stuffIt(q, danse)

			qname = q["QNAME"]
			qparams = sorted(filter(lambda p: p["TARGET"] == qname, app["params"]),  key=lambda p: int(p["ID"]))
			qps = []
			qpsl= []
			danse = ["TARGET", "NAME", "VALUE", "RULE", "ID"]
			for qp in qparams:
				qqp, qqpl = stuffIt(qp, danse)
				qps.append(qqp)
				qpsl.append(qqpl)

			qq["params"] = qps
			qpl.append(["params", qpsl])


			qnameRgx = re.compile(qname + r"(-\d+)?$")
			qprops = sorted(filter(lambda p: qnameRgx.match(p["TARGET"]) is not None, app["props"]), key= lambda p: p["TARGET"])
			qprs = []
			qprsl= []
			danse = ["TARGET", "NAME", "VALUE"]
			for qp in qprops:
				qqp,qqpl = stuffIt(qp, danse)
				qprs.append(qqp)
				qprsl.append(qqpl)

			qq["props"] = qprs
			qpl.append(["props", qprsl])

			appq.append(qq)
			appql.append(qpl)


		if(args.ls):
			scmExport.append([["app", app["app"]],["details", appql]])
		else:
			z = {}
			z[app["app"]]=appq
			scmExport.append(z)

	return scmExport


def syncHelper(reloadPayloadJson, sourceEnvExportedApps, targetEnvExportedApps, addDefns, targetUser):
	"""for a JSON payload to reload an app, knowing the current state of queries in the target
	   environment, change delete/insert orders for overlapping queries into updates of target yada index
	   returns 2-uple (bootJson, reloadJson) where bootJson is (optionally) creating the yada app in the target environment
	   and reloadJson is for creating/updating queries"""

	qnames = set()
	apps = set()
	for app in targetEnvExportedApps:
		apps.add(app["app"])
		for q in app["queries"]:
			qnames.add( q["QNAME"])

	qUpdates = []
	alteredPayload = []
	bootPayload = []
	for jpi in reloadPayloadJson:
		qn = jpi["qname"] if(jpi.has_key("qname")) else jpi["q"]
		if(qn == "YADA delete query"):
			njpidata = filter(lambda it: it["QNAME"] not in qnames, jpi["DATA"])
			if(len(njpidata)>0): alteredPayload.append({"qname":qn, "DATA": njpidata })

		elif(qn == "YADA new query"):
			news = filter(lambda it: it["QNAME"] not in qnames, jpi["DATA"])
			for o in (filter(lambda it: it["QNAME"] in qnames, jpi["DATA"])):
				qUpdates.append(o)

			if(len(news)>0): alteredPayload.append({"qname":qn, "DATA": news})
			if(len(qUpdates)>0): alteredPayload.append({"qname": "YADA update query", "DATA": qUpdates})
		elif(qn != "YADA new app" and qn != "YADA new app admin"):
			alteredPayload.append(jpi)

	if(addDefns):
		makeApps = []
		assignApps = []
		for app in sourceEnvExportedApps:
			if(app["app"] not in apps):
				if(app.has_key("conf") and app.has_key("name")
				   and app.has_key("active") and app.has_key("description")):
					makeApps.append({"APP": app["app"], "NAME": app["name"], "DESCR": app["description"], "ACTIVE": app["active"], "CONF":app["conf"]})
					assignApps.append({"APP":app["app"], "USERID": "YADA"})
					if(targetUser is not None and targetUser != "YADA"):
						assignApps.append({"APP": app["app"], "USERID": targetUser})

		if(len(makeApps)>0):
			bootPayload.append({"qname": "YADA new app", "DATA":makeApps})
			bootPayload.append({"qname": "YADA new app admin", "DATA": assignApps})

	return bootPayload, alteredPayload




def prepareYadaReloadPayload(args, exportedApps):
	"""from the exported/selected apps of an environment, create a JSONParams JSON-payload
	   that would re-create all the queries from scratch. Brute force delete of all params and props,
	   delete of queries and re-create the queries and re-assign their params and props.
	   To avoid deleting pre-existing queries from the target environment, use the function syncHelper."""
	deleteProps= []
	deleteParams=[]
	deleteQueries=[]

	addQueries=[]
	addParams=[]
	addProps=[]

	for app in exportedApps:
		appK = app["app"]
		for q in app["queries"]:
			deleteQueries.append({"APP":appK, "QNAME": q["QNAME"]})
			addQueries.append(dict(q))

		for p in app["props"]:
			addProps.append(dict(p))

		import sets
		# we will use YADA delete prop for target - to clean everything completely (except --legacy)
		targets = list(set( map(lambda p: p["TARGET"], app["props"])))
		for t in targets:
			deleteProps.append({"TARGET":t})
			deleteParams.append({"TARGET":t})


		for p in app["params"]:
			if(args.legacy): deleteParams.append(dict(p))
			addParams.append(dict(p))

	JP = []
	JP.append({"qname": "YADA delete prop for target", "DATA": deleteProps})
	JP.append({"qname": ("YADA delete default param" if(args.legacy) else "YADA delete default param for target"), "DATA": deleteParams})
	JP.append({"qname": "YADA delete query", "DATA": deleteQueries});
	JP.append({"qname": "YADA new query", "DATA": addQueries})
	JP.append({"qname":"YADA insert default param", "DATA": addParams});
	JP.append({"qname":"YADA insert prop", "DATA": addProps});

	# keep only queries that have something to change:
	JP = filter(lambda item: len(item["DATA"])>0, JP)


	return JP


selectedApps = []

if(args.load is None):
	selectedApps = obtainYadaQueriesFromEnvironment(args.yada, args)

else:
	with open(args.load, 'rb') as fin:
		selectedApps = json.load(fin)
	if(len(args.apps)>0):
		# we load from --load file and --apps is passed, so we further sub-selected for these apps
		selectedApps = filter(lambda app: app["app"] in args.apps, selectedApps)

print >>sys.stderr, "Number of selected apps: {0}".format(len(selectedApps))

if(args.scm is not None):
	## with option --scm we will generate a well ordered list of queries - so as to facilicates diff-s in SCM (source control: svn, git...)
	scmx = prepareSCMdump(args, selectedApps)
	with open(args.scm, "wb") as fout:
		json.dump(scmx, fout, indent=1, sort_keys=(not args.ls))

if(args.post is not None or args.yimport is not None):
	## with option --post, we generate a file that can be passed to cUrl to reload an environment with the queries we've exported so far.
	##  --post should be deprecated, rather you should use this utility with --load and --yimport instead of cUrl with --post output
	## with option --yimport, we push the queries into the live target environment. Use --sync if you care to maintain the access-counts etc.
	reloadJson = prepareYadaReloadPayload(args, selectedApps)
	postable = "JSONParams=" + urllib.quote(json.dumps(reloadJson))
	if(args.post is not None):
		if(args.json): postable = json.dumps({"jp":reloadJson});
		with open(args.post,'wb') as fout:
			fout.write(postable)
	else:
		# since we are attempting to push this to an actual environment, let's be cautious...
		if(args.defn or args.sync):
			targetEnvAppExport = obtainYadaQueriesFromEnvironment(args.yimport, args, env='target')
			bootJson, reloadJson = syncHelper(reloadJson, selectedApps, targetEnvAppExport, args.defn, None)
			if(args.httpdebug>0):
				print >>sys.stderr, json.dumps(reloadJson, indent=1)
		postable = {"JSONParams" : json.dumps(reloadJson) }
		cookies = yadaLogin(args.yimport, args) if(not args.tgt7) else None
		yimport = parseUserPassUrl(args.yimport, args)[2]
		if(args.defn and len(bootJson)>0):
			ncookies, vroooom = getSockContent(yimport, cookies, method='POST', data={"JSONParams": json.dumps(bootJson)});
			print >> sys.stderr, json.dumps(vroooom, indent=1)
			cookies = ncookies;
		ncookies, vroooom = getSockContent(yimport, cookies, method='POST', data=postable)
		print json.dumps(vroooom, indent=1)



if(args.out is not None):
	with open(args.out, "wb") as fout:
		json.dump(selectedApps, fout, indent=1)
elif(args.yimport is None):
	print json.dumps(selectedApps, indent=1)
