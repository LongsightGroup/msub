#!/bin/sh

getSvn()
{
  echo "getting svn " $1
  svn checkout $1
}

compileInstall()
{
  echo "compiling installing"
  mvn clean install
}

compileDeploy()
{
  echo "compiling deploying"
  mvn clean install sakai:deploy
}

promptVersion()
{
  echo "Sakai version (the version in your Sakai master/pom.xml) [default: ${1}] > : \c"
  read sakaiVersion

  if [ ! $sakaiVersion  ]; then
      sakaiVersion=$1
  fi

}
        
# This downloads and compiles a version of taggable to prepopulate your maven repo.
# Currently we aren't doing this in favor of stubbing out the older versions of the signatures
# with a patch.  
prepareTaggable()
{
    getSvn "https://source.sakaiproject.org/svn/taggable/trunk temp/taggable-2.9.x"
    cd temp/taggable-2.9.x
    cat ../../patches/taggable.patch | sed -e "s/{SAKAI_VERSION}/${sakaiVersion}/g" | patch -p1
    compileInstall
    cd ../..
}

# This function removes the taggable implementation by replacing it with a mostly empty shell.
stuboutTaggable()
{
    echo "Stubbing out taggable for ..."
    patch -p0 < patches/remove-taggable-functionality-stub-false.patch
}

prepareAssignment2()
{
    if [ ! $1 ]; then
      promptVersion "2.8-SNAPSHOT"
    else
      sakaiVersion=$1
    fi

    echo "Preparing for a ${sakaiVersion} environment...."
    echo

    if [ "$sakaiVersion" != "2.9-SNAPSHOT" ]; then
        # prepareTaggable
        #echo "Commenting out taggable for now..."
        stuboutTaggable
    fi

    cat patches/assignment2.patch | sed -e "s/{SAKAI_VERSION}/${sakaiVersion}/g" | patch -p0
}

printDonePreparing()
{
    echo
    echo "Successfully prepared assignment2 for a Sakai ${sakaiVersion} environment."
    echo "You should be able to build and deploy assignment2 now."
    echo
}


while [ "$version" != "1" -a "$version" != "2" -a "$version" != "3" -a "$version" != "4" -a "$version" != "5" ] 
do
  clear
  echo
  echo "This script will download (into a directory in assignment2 named temp), compile and install into your"
  echo "local maven repository some dependencies.  It will patch the dependencies' pom.xml files as well as patch"
  echo "assignment2's pom.xml files to work w/ your branch of sakai.  It will ONLY patch files within the assignment2"
  echo "tree (it does NOT touch anything outside of it).  This assignment2 directory MUST be sitting inside"
  echo "your Sakai tree before you try to run this script."
  echo
  echo "(1) - Sakai branch 2.7.x"
  echo "(2) - Sakai branch 2.8.x"
  echo "(3) - Sakai branch 2.9.x/trunk"
  echo
  echo "(4) - Custom Sakai version (must be a derivitive of 2.7.x or 2.8.x)"
  echo 
  echo "(5) - Exit"
  echo
  echo "Please choose an option: \c "
  read version
  echo
done

case "$version" in
        1)
                prepareAssignment2 "2.7-SNAPSHOT"
                printDonePreparing
                break
                ;;

        2)
                prepareAssignment2 "2.8-SNAPSHOT"
                printDonePreparing
                break
                ;;

        3)
                prepareAssignment2 "2.9-SNAPSHOT"
                printDonePreparing
                break
                ;;

        4)
                prepareAssignment2
                printDonePreparing
                break
                ;;

        5)
                echo "Exit"
                echo 
                
                break
                ;;
        *)
                echo "Invalid entry"
                ;;
esac




