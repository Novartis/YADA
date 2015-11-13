#!/usr/bin/perl
#
# Copyright 2015 Novartis Institutes for BioMedical Research Inc.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


# call with an url like the following: 
# http://example.com/yada.jsp?q=YADA%20test%20SELECT&pr=scriptPluginPreTest.pl,YADA%20test%20validate%20preproc&c=false

my $qname = $ARGV[0];

# The string returned must be a representation of the
# ServletRequest.getParameterMap() return value, with 
# String objects for keys, and String arrays for values.

print '{ "qname" : ["'.$qname.'"] }';
