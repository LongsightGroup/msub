#!/usr/bin/perl

my $file = 'filelist.txt';

`find ../.. | egrep -v \.svn | egrep ".*/[a-zA-Z0-9]*(_en|_en_US)?\\.properties" > $file`;

open DATA, "<$file" or die $!;

my @array_of_data = <DATA>;

foreach my $line (@array_of_data)
{
     if ($line =~ m/(.*\/([a-zA-Z0-9]*))(_en|_en_US)?\.properties/i)
        {
#        print "egrep $1_.*.properties\n";
        @found = `find . | egrep "$1_.*.properties"`;
        if (@found > 0){
		print "$2,$line";    
        } 
        elsif ($1 =~ m/(messages)/i){
                print "$1,$line";
        }

     } 
} 


close (DATA);

