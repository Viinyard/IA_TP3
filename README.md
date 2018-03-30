Add the lib "spmf.jar" to your maven repo before packaging in a new machine like :

mvn install:install-file -Dfile=spmf.jar -DgroupId=com.philippe-fournier-viger.spmf -DartifactId=spmf -Dversion=1.0 -Dpackaging=jar -DlocalRepositoryPath=lib/

