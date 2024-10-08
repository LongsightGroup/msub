<?php
error_reporting(E_ALL & ~E_NOTICE & ~E_DEPRECATED);
ini_set("display_errors", 1);
header('Content-Type: text/html; charset=utf-8');
session_start();
?>
<html>
<head>
  <title>IMS Learning Tools Interoperability</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body style="font-family:sans-serif;">
<?php
echo("<p><b>Sakai Unit Tests for IMS LTI 1.1 Consumer Launch</b></p>\n");
echo("<p>This is a very simple reference implementaton of the LMS side (i.e. consumer) for IMS LTI 1.1.</p>\n");

require_once("util/lti_util.php");
require_once("cert.php");

$cert_num = 0;
if ( isset($_REQUEST['cert_num']) ) $cert_num = $_REQUEST['cert_num'] + 0;

$cur_url = curPageURL();
echo("<p>Certification launch data sets: ");
for ( $i=0; $i < count($lmsdata_cert); $i++) {
   echo('<a href="'.$curPageUrl.'?cert_num='.$i.'">'.$i."</a>\n");
}
if ( isset($_REQUEST['cert_num']) ) {
    echo('<br/>Before running the certification tests, you must first <a href="http://www.imsglobal.org/developers/alliance/LTI/cert/index.php" target="_blank">Login</a> and set up the tests using your IMS membership credentials.'."\n");
}
echo("</p>");

$lmsdata = $lmsdata_cert[$cert_num];

foreach ($lmsdata as $k => $val ) {
    if ( $_POST[$k] && strlen($_POST[$k]) > 0 ) {
      $lmsdata[$k] = $_POST[$k];
    }
}

$key = "12345";
if ( isset($_SESSION["key"]) ) $key = $_SESSION["key"];
if ( isset($_REQUEST["key"]) ) $key = trim($_REQUEST["key"]);
$_SESSION["key"] = $key;

$secret = "secret";
if ( isset($_SESSION["secret"]) ) $secret = $_SESSION["secret"];
if ( isset($_REQUEST["secret"]) ) $secret = trim($_REQUEST["secret"]);
$_SESSION["secret"] = $secret;

$endpoint = trim($_REQUEST["endpoint"]);
if ( ! $endpoint ) {
    if ( isset($_REQUEST['cert_num']) ) {
        $endpoint = "http://www.imsglobal.org/developers/alliance/LTI/cert/tc_tool.php?x=With%20Space&y=yes";
    } else {
        $endpoint = str_replace("lms.php","tool.php",$cur_url);
    }
}
$cssurl = str_replace("lms.php","lms.css",$cur_url);

$b64 = base64_encode($key.":::".$secret.":::".uniqid());
$outcomes = str_replace("lms.php","common/tool_consumer_outcome.php",$cur_url);
$outcomes .= "?b64=" . $b64;

$tool_consumer_instance_guid = $lmsdata['tool_consumer_instance_guid'];
$tool_consumer_instance_description = $lmsdata['tool_consumer_instance_description'];

?>
<script language="javascript"> 
  //<![CDATA[ 
function lmsdataToggle() {
    var ele = document.getElementById("lmsDataForm");
    if(ele.style.display == "block") {
        ele.style.display = "none";
    }
    else {
        ele.style.display = "block";
    }
} 
  //]]> 
</script>
<a id="displayText" href="javascript:lmsdataToggle();">Toggle Resource and Launch Data</a>
<?php
  if ( isset($_REQUEST["cert_num"]) && $secret != "secret" ) {
      echo("<div id=\"lmsDataForm\" style=\"display:none\">\n");
  } else {
      echo("<div id=\"lmsDataForm\" style=\"display:block\">\n");
  }
  echo("<form method=\"post\">\n");
  echo("<input type=\"submit\" value=\"Recompute Launch Data\">\n");
  echo("<input type=\"submit\" name=\"reset\" value=\"Reset\">\n");
  echo("<fieldset><legend>LTI Resource</legend>\n");
  $disabled = '';
  echo("Launch URL: <input size=\"120\" type=\"text\" $disabled name=\"endpoint\" value=\"$endpoint\">\n");
  echo("<br/>Key: <input type\"text\" name=\"key\" $disapbled size=\"90\" value=\"$key\">\n");
  echo("<br/>Secret: <input type\"text\" name=\"secret\" $disabled size=\"90\" value=\"$secret\">\n");
  echo("</fieldset><p>");
  echo("<fieldset><legend>Launch Data</legend>\n");
  foreach ($lmsdata as $k => $val ) {
      echo($k.": <input type=\"text\" size=\"60\" name=\"".$k."\" value=\"");
      echo(htmlspec_utf8($val));
      echo("\"><br/>\n");
  }
  echo("</fieldset>\n");
  echo("</form>\n");
  echo("</div>\n");
  echo("<hr>");

  $parms = $lmsdata;
  // Cleanup parms before we sign
  foreach( $parms as $k => $val ) {
    if (strlen(trim($parms[$k]) ) < 1 ) {
       unset($parms[$k]);
    }
  }

  // Add oauth_callback to be compliant with the 1.0A spec
  $parms["oauth_callback"] = "about:blank";
  $parms["lis_outcome_service_url"] = $outcomes;
  $parms["lis_result_sourcedid"] = '{"zap" : "Siân JSON 1234 Sourcedid <>&lt;"}';
    
if ( strpos($cur_url, "localhost" ) === FALSE ) $parms['launch_presentation_css_url'] = $cssurl;

addCustom($parms, array(
    "simple_key" => "custom_simple_value",
    "Complex!@#$^*(){}[]KEY" => "Complex!@#$^*(){}[]½Value"
));

$parms = signParameters($parms, $endpoint, "POST", $key, $secret, 
"Press to Launch", $tool_consumer_instance_guid, $tool_consumer_instance_description);

  $content = postLaunchHTML($parms, $endpoint, true, 
     "width=\"100%\" height=\"900\" scrolling=\"auto\" frameborder=\"1\" transparency");
  print($content);

?>
