<?php 
error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", 1);

// Load up the LTI Support code
require_once 'util/lti_util.php';
require_once 'util/json_indent.php';  // Until all PHP's are > 5.4
require_once 'tp_messages.php';

session_start();
header('Content-Type: text/html; charset=utf-8'); 

// Initialize, all secrets are 'secret', do not set session, and do not redirect
$context = new BLTI("secret", false, false);

$lti_message_type = $_POST["lti_message_type"];

global $div_id;
$div_id = 1;

function togglePre($title, $content) {
    global $div_id;
	echo('<h4>'.$title);
	echo(' (<a href="#" onclick="dataToggle('."'".$div_id."'".');return false;">Toggle</a>)</h4>'."\n");
	echo('<pre id="'.$div_id.'" style="display:none; border: solid 1px">'."\n");
	echo($content);
	echo("</pre>\n");
	$div_id = $div_id + 1;
}

?>
<html>
<head>
  <title>Sakai External Tool API Test Harness 2.0</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script language="javascript"> 
function dataToggle(divName) {
    var ele = document.getElementById(divName);
    if(ele.style.display == "block") {
        ele.style.display = "none";
    }
    else {
        ele.style.display = "block";
    }
} 
  //]]> 
</script>
</head>
<body style="font-family:sans-serif; background-color:#add8e6">
<?php
echo("<p><b>Sakai LTI 2.0 Test Harness</b></p>\n");

ksort($_POST);
$output = "";
foreach($_POST as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    $output = $output . htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}
togglePre("Raw POST Parameters", $output);


$output = "";
ksort($_GET);
foreach($_GET as $key => $value ) {
    if (get_magic_quotes_gpc()) $value = stripslashes($value);
    $output = $output . htmlent_utf8($key) . "=" . htmlent_utf8($value) . " (".mb_detect_encoding($value).")\n";
}
if ( strlen($output) > 0 ) togglePre("Raw GET Parameters", $output);

echo("<pre>\n");

if ( $lti_message_type == "ToolProxyReregistrationRequest" ) {
	$reg_key = $_POST['oauth_consumer_key'];
	$reg_password = "secret";
} else if ( $lti_message_type == "ToolProxyRegistrationRequest" ) {
	$reg_key = $_POST['reg_key'];
	$reg_password = $_POST['reg_password'];
} else {
	echo("</pre>");
	die("lti_message_type not supported ".$lti_message_type);
}

$launch_presentation_return_url = $_POST['launch_presentation_return_url'];

if ( $lti_message_type == "ToolProxyReregistrationRequest" && ! $context->valid ) {
	print "Base string:\n";
	print htmlent_utf8($context->basestring);
	print "Context dump:\n";
    print htmlent_utf8($context->dump());
	die("Signature mismatch");
}

$tc_profile_url = $_POST['tc_profile_url'];
if ( strlen($tc_profile_url) > 1 ) {
	echo("Retrieving profile from ".$tc_profile_url."\n");
    $tc_profile_json = do_get($tc_profile_url);
	echo("Retrieved ".strlen($tc_profile_json)." characters.\n");
	echo("</pre>\n");
    togglePre("Retrieved Consumer Profile",$tc_profile_json);
    $tc_profile = json_decode($tc_profile_json);
	if ( $tc_profile == null ) {
		die("Unable to parse tc_profile error=".json_last_error());
	}
} else {
    die("We must have a tc_profile_url to continue...");
}

// Find the registration URL

echo("<pre>\n");
$tc_services = $tc_profile->service_offered;
echo("Found ".count($tc_services)." services profile..\n");
if ( count($tc_services) < 1 ) die("At a minimum, we need the service to register ourself - doh!\n");

// var_dump($tc_services);
$endpoint = false;
$formats = Array();
foreach ($tc_services as $tc_service) {
   $format = $tc_service->{'format'};
    echo("Service: ".$format."\n");
   if ( in_array($format, $formats) ) die("Format=".$format." cannot occur multiple times in the service list");
   $formats[] = $format;
   if ( $format != "application/vnd.ims.lti.v2.ToolProxy+json" ) continue;
   // var_dump($tc_service);
   $endpoint = $tc_service->endpoint;
}

if ( $endpoint == false ) die("Must have an application/vnd.ims.lti.v2.ToolProxy+json service available.");

echo("\nFound an application/vnd.ims.lti.v2.ToolProxy+json service - nice for us...\n");
echo("Optional money collection phase complete...\n");
echo("<hr/>");

$cur_url = curPageURL();
$cur_base = str_replace("tp.php","",$cur_url);

$tp_profile = json_decode($tool_proxy);
if ( $tp_profile == null ) {
	togglePre("Tool Proxy Raw",htmlent_utf8($tool_proxy));
    $body = json_encode($tp_profile);
    $body = json_indent($body);
    togglePre("Tool Proxy Parsed",htmlent_utf8($body));
    die("Unable to parse our own internal Tool Proxy (DOH!) error=".json_last_error()."\n");
}

// Tweak the stock profile
$tp_profile->tool_consumer_profile = $tc_profile_url;

// Re-register
$tp_profile->tool_profile->message[0]->path = $cur_url;
$tp_profile->tool_profile->product_instance->product_info->product_family->vendor->website = $cur_base;
$tp_profile->tool_profile->product_instance->product_info->product_family->vendor->timestamp = "2013-07-13T09:08:16-04:00";

// I want this *not* to be unique per instance
$tp_profile->tool_profile->product_instance->guid = "urn:sakaiproject:unit-test";

$tp_profile->tool_profile->product_instance->service_provider->guid = "http://www.sakaiproject.org/";

// Launch Request
$tp_profile->tool_profile->resource_handler[0]->message[0]->path = "tool.php";
$tp_profile->tool_profile->resource_handler[0]->resource_type = "sakai-api-test-01";

$tp_profile->tool_profile->base_url_choice[0]->secure_base_url = $cur_base;
$tp_profile->tool_profile->base_url_choice[0]->default_base_url = $cur_base;

$tp_profile->security_contract->shared_secret = 'secret';
$tp_profile->security_contract->tool_service = $tc_services;
// print_r($tp_profile);

$body = json_encode($tp_profile);
$body = json_indent($body);

echo("Registering....\n");
echo("Endpoint=".$endpoint."\n");
echo("Key=".$reg_key."\n");
echo("Secret=".$reg_password."\n");
echo("</pre>\n");

if ( strlen($endpoint) < 1 || strlen($reg_key) < 1 || strlen($reg_password) < 1 ) die("Cannot call endpoint - insufficient data...\n");

togglePre("Registration Request",htmlent_utf8($body));

$response = sendOAuthBodyPOST("POST", $endpoint, $reg_key, $reg_password, "application/vnd.ims.lti.v2.ToolProxy+json", $body);

togglePre("Registration Request Headers",htmlent_utf8(get_post_sent_debug()));

global $LastOAuthBodyBaseString;
togglePre("Registration Request Base String",$LastOAuthBodyBaseString);

togglePre("Registration Response Headers",htmlent_utf8(get_post_received_debug()));

togglePre("Registration Response",htmlent_utf8(json_indent($response)));

if ( $last_http_response == 201 || $last_http_response == 200 ) {
  echo('<p><a href="'.$launch_presentation_return_url.'">Continue to launch_presentation_url</a></p>'."\n");
  exit();
}

echo("Registration failed, http code=".$last_http_response."\n");

// Check to see if they slid us the base string...
$responseObject = json_decode($response);
if ( $responseObject != null ) {
	$base_string = $responseObject->base_string;
	if ( strlen($base_string) > 0 && strlen($LastOAuthBodyBaseString) > 0 && $base_string != $LastOAuthBodyBaseString ) {
		$compare = compare_base_strings($LastOAuthBodyBaseString, $base_string);
		togglePre("Compare Base Strings (ours first)",htmlent_utf8($compare));
	}
}

?>
