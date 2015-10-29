#!/usr/bin/perl

# call with an url like the following: 
# http://example.com/yada.jsp?q=YADA%20test%20SELECT&pr=scriptPluginPreTest.pl,YADA%20test%20validate%20preproc&c=false

my $qname = $ARGV[0];

# The string returned must be a representation of the
# ServletRequest.getParameterMap() return value, with 
# String objects for keys, and String arrays for values.

print '{ "qname" : ["'.$qname.'"] }';
