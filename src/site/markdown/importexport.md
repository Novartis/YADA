
To facilitate the development process of a YADA powered app, one can use the import/export tool included in YADA to quickly:
- save the status of all queries in an app or a list of apps, from a live environment into a "portable file"
- load the queries from the "portable file" to another live environment
- load the definition of an app (connection details and queries) from a "portable file" into a newly spun environment
- compare the status of queries between 2 environments with a tool such as diff.


The tool is located in this code repository in yada-war/src/main/resources/scripts and is yadaindex-exp.py, a python script.
The script requires only basic Python libraries and was developed and tested with Python 2.7.

To extract the queries of your favorite app (ARLX) into the filesystem you would:

``python yadainx-exp.py --yada http://leroych2:lerpasswd220@yada-dev.na.novartis.net:8000/yada.jsp --apps ARLX --out arlx.queries.json``


To also extract the connection details for your app, use the --defn option. This will mask the password, although you can use the --unmask option to prevent masking the password.

``python yadainx-exp.py --yada http://leroych2:lerpasswd220@yada-dev.na.novartis.net:8000/yada.jsp --apps ARLX --out arlx.defn+queries.json --defn``

Instead of passing the username/password for your YadaAdmin credentials, in the yada URL, you can use the --user and --password options.

``python yadainx-exp.py --yada http://yada-dev.na.novartis.net:8000/yada.jsp --user leroych2 --password lerpasswd220 --apps ARLX --out arlx.queries.json``


To transform your "portable file" into a file easily compared in Source Control, you would load the portable file and saved it with the --scm option
``python yadainx-exp.py --load arlx.queries.json --scm arlx.queries.scm.json --out /dev/null ``
(the /dev/null output is to avoid getting the loaded file in the console)
(please note that the SCM file does not contain the connection details)


If you have extracted the definitions of the apps from the source environment with --defn, you can import it with the --defn options again when doing a --yimport.
The import process deletes all queries it imporst first and recreates them from scratch. 
Use the --sync option to update the queries pre-existing in the target environment (so as not lose the access-counts etc)
The --sync option does not delete queries in the target environment that are not found in the source.

``python yadainx.exp.py --load arlx.defn+queries.json --defn --yimport http://leroych2:tstpasswd220@yada-test.na.novartis.net:8088/yada.jsp --sync``

One can also do a brutal:

```python yadainx.exp.py --yada http://leroych2@lerpasswd220@yada-dev.na.novartis.net:8000/yada.jsp --defn --yimport http://leroych2:lerpassword220@yada-test.na.novartis.net:8088/yada.jsp --apps arlx --sync```

Please note that the --apps options can use several application tag, and are case-sensitive.




The tool will present some information on its options:
```
usage: yadainx-exp.py [-h] [--yada [YADA]] [--yimport [YIMPORT]]
                      [--user [USER]] [--password [PASSWORD]] [--load [LOAD]]
                      [--apps [APPS [APPS ...]]] [--out [OUT]]
                      [--httpdebug HTTPDEBUG] [--post [POST]] [--json]
                      [--scm [SCM]] [--defn] [--unmask] [--ts] [--ls]
                      [--legacy] [--tgt7] [--src7] [--sync]

Export Yada index

optional arguments:
  -h, --help            show this help message and exit
  --yada [YADA]         URL-Path to the corresponding yada.jsp servlet
                        (source)
  --yimport [YIMPORT]   for import of query collection, yada.jsp url of target
                        environment
  --user [USER], -u [USER]
                        user in Yada-Admin for the export
  --password [PASSWORD], -p [PASSWORD]
  --load [LOAD]         use this former export file to list queries instead of
                        retrieving from YADA environment
  --apps [APPS [APPS ...]], -a [APPS [APPS ...]]
                        subset of apps to concentrate on (default: all apps)
  --out [OUT], -o [OUT]
                        the file for the export output of the Yada index,
                        which can later be used with --load
  --httpdebug HTTPDEBUG
  --post [POST]         a file where to write the POSTable jsonParams (eg via
                        curl) to reload an app (no authentication) [this
                        option should be depecrated]
  --json                for --post, save as JSON instead of postable text
                        (avoid application/x-www-form-urlencoded) [perhaps
                        intended for testing this tool]
  --scm [SCM]           save a file inerrently sorted so that it is easy to
                        compare in source-control (git diff, svn diff etc)
  --defn                also save the APP definition, including Hikari pool
                        definition (jdbc connection)
  --unmask, --um        avoid masking the jdbc connection password for app
                        definition (see --defn)
  --ts                  with --scm, save the creation and modification
                        timestamps user Ids
  --ls                  with --scm, output as query definition and params as
                        list of lists rather than list of objects
  --legacy              the intended Yada system where this export would be
                        loaded is a legacy system that does not allow for
                        complete deletion of a query's params
  --tgt7                the intended target Yada system is on Yada-7
  --src7                the source Yada system is on Yada-7
  --sync                query the target system before feeding it, so as to do
                        updates and no do deletes/inserts of all queries
```





Please be careful with the import step, obviously.





