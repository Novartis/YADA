#!/usr/bin/perl

open INFILE, $ARGV[0] || die "oops";
while (<INFILE>) {
 chomp;
 $_ =~ s/RESULTSET/PLUGINWASHERE/;
 print $_;
}
