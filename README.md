# individual properties contextual assertion
The plugin was developed by ODASE Team, for more information about us, see http://www.odaseontologies.com.  
The view is available from: Window -> Views -> Individual Views -> Properties contextual assertions.  
The view offers a more comfortable way to make individual properties assertions compared to the default
Protégé "Property Assertions" view. By default this view will show in domain properties with the possibility
to switch to all properties and the corresponding asserted and inferred individuals.  
If you click on one of the properties the list of asserted/inferred individual will be displayed.  
If the property is grayed, it means that the list of individuals is empty.   
The object/data properties assertions are available from the "add" button located next to each object/data properties.
If the "add" button is red (=disabled), it means you are dealing with a functional property already containing an individual. The object property
assertion can be done either by double click on one or multiple individuals in the displayed tree or by entering
their name directly (auto-completion is available) which will result in adding them to a temporary list.
The same double click allows to remove the individuals from the list. If the individual doesn't exist yet, it
can be created from the same window using a right click on one of the OWLClasses from tree view.

#### Prerequisites

To build and run the examples, you must have the following items installed:

+ Java 8 or higher
+ Apache's [Maven](http://maven.apache.org/index.html).
+ A Protege distribution (5.0 or higher). The Protege releases are [available](http://protege.stanford.edu/products.php#desktop-protege) from the main Protege website. 

#### Build and install plug-in

1. Type mvn clean package.  On build completion, the "target" directory will contain a
 org.odase.protege.plugin.individualPropertiesContextualAssertion-${version}.jar file.
2. Copy the JAR file from the target directory to the "plugins" subdirectory of your Protégé distribution.

#### Plug-in screenshots

![individualspluginmainview](https://user-images.githubusercontent.com/19971537/28918453-9b5cea54-7849-11e7-8779-8ac0ee373cdf.JPG)


