<?php 
$tool_proxy = <<< EOF
{
  "@context": "http://www.imsglobal.org/imspurl/lti/v2/ctx/ToolProxy",
  "@type": "ToolProxy",
  "lti_version": "LTI-2p0",
  "tool_consumer_profile": "__TODO_SHOULD_THIS_BE_THE_WHOLE_PROFILE_OR_JUST_A_URL_SEE_5_6__",
  "nasty_json": "How do you handle these characters < > & ' ",
  "evil_json": "</script><script>alert('evil');</script>",
  "evil_json2": "\"</script><script>alert('evil');</script>",
  "tool_profile": {
    "product_instance": {
      "product_info": {
        "product_version": "0.3",
        "technical_description": {
          "default_value": "Simple LTI 2 PHP Implementation",
          "key": "tool.technical"
        },
        "product_name": {
          "default_value": "LTI2 PHP Test",
          "key": "tool.name"
        },
        "product_family": {
          "code": "assessment-tool",
          "vendor": {
            "website": "__REPLACE__",
            "code": "sakaiproject.org",
            "name": {
              "default_value": "Sakai",
              "key": "tool.vendor.name"
            },
            "timestamp": "2013-05-13T09:08:16-04:00",
            "contact": {
              "email": "info@sakaiproject.org"
            },
            "description": {
              "default_value": "Sakai does many awesome things that are open source.",
              "key": "tool.vendor.description"
            }
          }
        },
        "description": {
          "default_value": "This tool produces mostly debugging output and sample code.",
          "key": "tool.description"
        }
      },
      "support": {
        "email": "helpdesk@sakaiproject.org"
      },
      "guid": "fd75124a-140e-470f-944c-114d2d92bb40",
      "service_provider": {
        "support": {
          "email": "support@sakaiproject.org"
        },
        "provider_name": {
          "default_value": "Dr. Chuck",
          "key": "service_provider.name"
        },
        "guid": "18e7ea50-3d6d-4f6b-aff2-ed3ab577716c",
        "description": {
          "default_value": "An inexpensive PHP hosting environment from 1and1.",
          "key": "service_provider.description"
        },
        "timestamp": "2013-05-13T09:08:16-04:00"
      }
    },
    "lti_version": "LTI-2p0",
    "message": [
      {
        "message_type": [
          "ToolProxyRegistrationRequest",
          "ToolProxyReregistrationRequest"
        ],
        "path": "__LAUNCH_REGISTRATION__",
        "parameter": [
          {
            "variable": "ToolConsumerProfile.url",
            "name": "tc_profile_url"
          }
        ]
      }
    ],
    "resource_handler": [
      {
        "resource_type": "__REPLACE__urn:lti:ResourceType:acme.example.com/nitrolab/homework",
        "message": [
          {
            "path": "__LAUNCH_PATH__",
            "parameter": [
              {
                "fixed": "3.14159",
                "name": "pi"
              },
              {
                "variable": "Person.email.primary",
                "name": "user_primary_email"
              }
            ],
            "message_type": "basic-lti-launch-request"
          }
        ],
        "name": {
          "default_value": "Sakai PHP Unit Test",
          "key": "resource.name"
        },
        "short_name": {
          "default_value": "Sakai Unit",
          "key": "resource.name"
        },
        "description": {
          "default_value": "Sakai PHP Unit Test Decription",
          "key": "resource.description"
        }
      }
    ],
    "base_url_choice": [
      {
        "selector": {
          "applies_to": [
            "IconEndpoint",
            "MessageHandler"
          ]
        },
        "secure_base_url": "http://localhost:5000",
        "default_base_url": "http://localhost:5000"
      }
    ]
  },
  "security_contract": {
    "shared_secret": "__SECRET__",
    "tool_service": [
      {
        "@type": "RestService",
        "@id": "ltitcp:ToolProxy.collection",
        "service": "__TODOFROM_ID_INTC_PROFILE__http://localhost:4000/tools",
        "action": "POST",
        "format": "application/vnd.ims.lti.v2.ToolProxy+json"
      }
    ]
  }
}
EOF;
