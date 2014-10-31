#!/usr/bin/perl -w

use SOAP::Lite;

my $serverId = 'server1';
my $millisBeforeExpire = 600000;
my $user = 'admin';
my $password = 'admin';

my $loginURI = "http://localhost:8080/sakai-axis/SakaiLogin.jws?wsdl";
my $scriptURI = "http://localhost:8080/sakai-axis/SakaiScript.jws?wsdl";

my $loginsoap = SOAP::Lite->proxy($loginURI)->uri($loginURI);

my $scriptsoap = SOAP::Lite
    ->proxy($scriptURI)
    -> on_fault(sub { my($soap, $res) = @_;
         die ref $res ? $res->faultstring : $soap->transport->status, "\n";
       })
    ->uri($scriptURI);


#START

#get session
my $session = $loginsoap->login($user, $password)->result;

my $result = $scriptsoap->getSessionCountForServer($session, $serverId, $millisBeforeExpire)->result;

print $result , "\n";

$result = $scriptsoap->getSessionTotalCount($session, $millisBeforeExpire)->result;

print $result , "\n";

# logout
my $logout = $loginsoap->logout($session)->result;
exit;
