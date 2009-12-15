
JBoss Microcontainer Integration


== What's it all about ==

JBoss Microcontainer integration (MC integration for short) makes it possible to inject components deployed as MC beans into components deployed as exo-kernel service components. Exo-kernel is the bottom layer on top of which GateIn si built as a set of services. Exo-kernel uses picocontainer as extremely lightweight dependency-injection (DI) container. All eXo Platform and GateIn components are configured and deployed as exo-kernel services. When deployed to JBossAS, there is another DI container available - JBoss Microcontainer which is the bottom layer through which all JBossAS service components are configured and deploy. MC Integration makes it possible apply JBoss Microcontainer dependency-injection to GateIn components deployed through exo-kernel.

There is no support for MC integration in Tomcat deployment. If a component annotated with @InterceptMC is deployed, a warning is logged, but the component will be deployed anyway - without injections being applied.


== How to use ==


There is an annotation @InterceptMC, that you annotate your service component with, to turn on MC integration for that component. MC integration only works in JBossAS, so any components that rely on it have to provide alternative injection mechanisms when deployed in Tomcat or somewhere else (i.e. using exo-kernel configuration for injections), or wire up components manually - hardcoded in a component that does just that.

Sometimes you may want to inject JBossAS components per service instance. If you annotate a class, then all instances of this class will share the same configuration, you can't have one instance of the class having one configuration, and another instance of the same class having a different configuration.

There is an xml file based configuration mechanism that allows you to enable MC integration for a service (per class, as well as per instance), and configure injection points and injected values.

For an example of MC integration by using annotations see: http://anonsvn.jboss.org/repos/exo-jcr/kernel/branches/mc-int-branch/exo.kernel.mc-integration/exo.kernel.mc-int-demo/src/main/java/org/exoplatform/kernel/demos/mc/InjectingBean.java

For an example of MC integration by using external configuration see: http://anonsvn.jboss.org/repos/exo-jcr/kernel/branches/mc-int-branch/exo.kernel.mc-integration/exo.kernel.mc-int-demo/src/main/java/org/exoplatform/kernel/demos/mc/ExternallyControlledInjectingBean.java

Both these examples use exo-kernel configuration file for creation of service instances: http://anonsvn.jboss.org/repos/exo-jcr/kernel/branches/mc-int-branch/exo.kernel.mc-integration/exo.kernel.mc-int-demo/src/main/resources/conf/configuration.xml

That's eXo kernel configuration file, with eXo configurator specific syntax, used to configure DI performed by eXo kernel when configuring and instantiating service components. All GateIn services are configured through this.


The external configuration example also uses injection configuration file: http://anonsvn.jboss.org/repos/exo-jcr/kernel/branches/mc-int-branch/exo.kernel.mc-integration/exo.kernel.mc-int-demo/src/main/resources/conf/mc-int-config.xml

The configuration format is the same as for jboss-beans.xml files. With a few exception:
 - 'bean' element's 'name' attribute refers to configuration.xml 'component' element's 'key' attribute
 - elements for aop configuration, constructor injections, and many others have no effect

Annotation configuration in mc-int-config.xml overrides any compiled annotations on classes. That means, every instance of the same service class can be specifically targeted for MC injections, and any existing hardwired class annotation can be nullified through configuration.

Only when a component is marked with @InterceptMC annotation, will it have its mc-kernel injection annotations process by mc-kernel, as part of MC integration.


== MC integration demo ==


The demo demonstrates POJO creation via exo-kernel and use of MC injection annotations (or xml configuration) to have some services injected into our exo-kernel-deployed POJO by MC.

We have three service objects - InjectedBean, InjectingBean, and ExternallyControlledInjectingBean, packaged in a single jar.

InjectedBeans gets instantiated by JBossAS deployer via META-INF/jboss-beans.xml. InjectingBean and ExternallyControlledInjectingBean on the other hand gets instantiated by exo-kernel via conf/configuration.xml.

Exo-kernel knows nothing about InjectedBean, but because InjectingBean is annotated with @InterceptMC it is processed by MC integration logic when instantiated by exo-kernel. The processing pipes it through MC-kernel logic which processes MC-kernel injection annotations, and performs injection of components. The same goes for ExternallyControlledInjectingBean.

Of course, MC-kernel knows nothing about exo-kernel service objects - it can only inject components deployed through one of JBossAS deployers that use MC-kernel to instantiate - or at least register - components.

InjectingBean demonstrates several different ways in which you can perform injection of JBoss services into a component. If instead of using exo-kernel to deploy InjectingBean, you deploy it through JBossAS, these annotations will work just the same - JBossAS uses MC-kernel, and MC-kernel will process the annotations.

There are many annotations used in InjectingBean.java. Apart from @InterceptMC they are all mc-kernel annotations used to configure injections.

ExternallyControlledInjectingBean.java on the other hand contains no annotations at all. All injections are configured externally in conf/mc-int-config.xml.

@InterceptMC is introduced by MC integration to mark classes that should be integrated with MC. Through it you can also control what kind of injections you want MC to perform. By default, field injections are disabled by MC-kernel, as they are deemed an anti-pattern for service objects - they may make your code more difficult to maintain in the long run, as you are giving up the ability to intercept the setting of the field.

Service objects instantiated through exo-kernel don't become part of MC-kernel object repository. That means that dependency management can't be performed by MC-kernel, which means that all the dependencies InjectingBean needs have to be fulfilled before MC-kernel first learns about it. Usually MC-kernel makes several passes over the whole deployment (all the deployment archives), first learning about services and their dependencies, then starting up services that have no dependencies first, and services with dependencies later - when their dependencies are fulfilled.

When using MC integration, we need to make sure, that all the dependencies have already been installed before exo-kernel bootstrap happens. This can be done by deploying all jboss beans as .jars containing META-INF/jboss-beans.xml in deploy dir. JBossAS deploys .jars before .wars and .ears.


== Things to look out for ==

There are several exo-kernel bootstrap points that may be used when creating your GateIn portals:

Always use either:

exo-kernel/exo.kernel.container: org.exoplatform.container.web.PortalContainerConfigOwner

or

gatein/portal/trunk/component/web: org.exoplatform.web.GenericHttpListener


Never use PortalContainer or RootContainer before first making sure one of the above two proper GateIn initialization points are invoked.



== Building and deploying ==


 === exo-kernel ===

cd $REPO/exo-jcr/kernel/branches
svn co https://svn.jboss.org/repos/exo-jcr/kernel/branches/mc-int-branch
cd mc-int-branch
mvn install


 === packager ===

cd $REPO/gatein/tools/packager/branches
svn co https://svn.jboss.org/repos/gatein/tools/packager/branches/mc-integration
cd mc-integration
mvn install


 === portal ===

cd $REPO/gatein/portal/branches
svn co https://svn.jboss.org/repos/gatein/portal/branches/mc-integration
cd mc-integration
mvn install -Dmaven.test.skip=true -Dgatein.checkout.dir=$REPO/gatein/portal/branches/mc-integration



 === Packaging and running GateIn with demo and integration tests deployed ===

cd $REPO/gatein/portal/branches/mc-integration/packaging/pkg

FOR JBOSSAS:

   mvn install -Ppkg-jbossas-tests -Dexo.projects.directory.dependencies=REPLACE_WITH_YOUR_OWN_DIRECTORY -Dgatein.checkout.dir=$REPO/gatein/portal/branches/mc-integration
   cd $REPO/gatein/portal/branches/mc-integration/packaging/pkg/target/jboss/bin
   run

FOR TOMCAT (MC integration won't work, but integration tests should pass):

   mvn install -Ppkg-tomcat-tests -Dexo.projects.directory.dependencies=REPLACE_WITH_YOUR_OWN_DIRECTORY -Dgatein.checkout.dir=$REPO/gatein/portal/branches/mc-integration
   cd $REPO/gatein/portal/branches/mc-integration/packaging/pkg/target/tomcat/bin
   gatein run



 === Running the integration tests ===

cd $REPO/exo-jcr/kernel/branches/mc-int-branch/exo.kernel.mc-integration/exo.kernel.mc-int-tests/
mvn -Ptests



If you run your server on non-default IP:port use something like:
mvn -Ptests -DforkMode=none -Dserver.host=127.0.0.1 -Dserver.port=8000

(Don't forget -DforkMode=none otherwise system properties won't get propagated to the test class)



(eof)
