Assignments 2 
=======================================================

.. document notes and metadata are at the bottom

:Author: Damian Sobieralski, Steven Githens

.. contents::



Overview
========

Assignments 2 is a Sakai Module for flexible use of assignments and 
work in the classroom, and can be used as alternative to the out of the
box Assignments module in Sakai.

Important links:

- `A2 Confluence Home <https://confluence.sakaiproject.org/display/ASNN/Home>`_
- `A2 Jira Bug Tracking <https://jira.sakaiproject.org/browse/ASNN>`_
- `A2 Subversion Source <https://source.sakaiproject.org/contrib/assignment2>`_
- `This README <https://source.sakaiproject.org/contrib/assignment2/trunk/README.html>`_
- `A1/A2 Gap List <https://confluence.sakaiproject.org/display/ASNN/Gap+Analysis+of+Assignments+and+Assignments+2>`_



Installation
============


Compatibility
-------------

Compatible with Sakai branches 2.7.x, 2.8.x and 2.9.x/trunk


Prepare Your Environment
------------------------

MySQL
`````

If you are using MySQL as your backend database for Sakai do verify 
that you have your collation set to a case sensitive one (say utf8_bin). 
Many OS distributions of MySQL default the collation to utf_general_ci.  
Renaming of assignments is a case sensitive thing and we need to make 
sure that the database is using case sensitive searching. 

Tools needed
````````````

You will need to have maven2, subversion and patch installed on your system.
Sed and cat will need to be available.  You will also need to be using the 
same local maven repository (by default $HOME/.m2/repository) that you 
used to build your Sakai branch.  Your Sakai branch needs to be built 
before you try to build/install Assignments 2.

Obtaining dependencies and patching pom.xmls
````````````````````````````````````````````

For Sakai branches 2.8.x and 2.7.x you will need to download a dependency
(taggable-2.9.x) and patch pom.xml files. The script 
prepare_for_sakai_env.sh script is included to accomplish this. The script 
will download into a subdirectory in assignments 2 named temp. It then compiles 
and installs into your local maven repository the dependency.  It will patch 
the dependency's pom.xml file as well as patch Assignments 2's pom.xml files 
to work w/ your branch of Sakai.  It will ONLY patch files within the 
Assignments 2's source tree. It does NOT touch anything outside of it so your 
base Sakai source tree is left untouched.

This Assignments 2 directory MUST be sitting inside your Sakai source 
tree before you try to run the script.

Compilation
```````````
After your install has been patched as above (if necessary), you can either
add Assignments 2 to your master pom file or build it with the usual.

::
  
  mvn -Dmaven.tomcat.home=/your/path/to/tomcat clean install sakai:deploy


.. Integrations
.. ============

.. Assignments 2 has a number of integrations, displayed in the matrix below.

.. This section of documentation is in progress.

.. ===============    =====  =====  ============
.. Sakai Version      2.7.x  2.8.x  2.9.x(trunk)
.. ---------------    -----  -----  ------------
.. Assignment 2   
.. OSP Matrix
.. OSP Evaluations
.. Gradebook
.. Gradebook 2
.. Turnitin CRS

.. OSP Matrix Integration 
.. -----------------------

.. OSP Evaluations
.. ---------------

.. Gradebook
.. ---------

.. Gradebook 2
.. -----------

.. Turnitin Content Review Service
.. -------------------------------


.. This document is written in restructured text, and at the moment I'm using the
.. lsr.css stylesheet for the html output.
.. The following is the order for header depths: = - ` : . ' " ~ ^ _ * + #
