#!/usr/bin/perl
use Config::Properties;

my $file = 'propertylist.txt';

`perl findPropertyFile.pl > $file`;

open DATA, "$file" or die "can't open $file $!";
my @array_of_data = <DATA>;
print "delete from sakai_message_bundle where USER_MODIFIED = '0' \n";
foreach my $line (@array_of_data)
{
   my @columns = split(/,/, $line);
   my $package = $columns[0];
   $columns[1] =~  m/\.\/(\w*)\//;
   my $module = $1;
   foreach my $propertyfile  ($columns[1])
   {       
    open PROPS, "< $propertyfile"
       or die "unable to open configuration file";

    my $properties = new Config::Properties();
    $properties->load(*PROPS);
    foreach $key ($properties->propertyNames) {
       $value = $properties->getProperty($key);
       print "insert into sakai_message_bundle (module_name, basename, prop_name, locale, default_value)  values (\"" . escape($module) . "\", \"" . escape($package) . "\", \"" . escape($key) ."\", \"en\", \"" . escape($value) . "\");\n";
   }
   close(PROPS);
}
close (DATA);
}

sub escape{
my ($string) = $_[0];
$string =~ s/\'/\\\'/g; # single quotes
$string =~ s/\"/\\\"/g; # double quotes
return $string;
}


