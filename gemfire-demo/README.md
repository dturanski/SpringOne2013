Spring XD and GemFire Real Time Analytics Demo
=

Description
---
The _gemfire-demo_ project illustrates a simple use case for using GemFire with [Spring XD](http://www.springsource.org/spring-xd) for real time analytics. It runs using a twitter feed or twitter search and loads summary data along with the hash tags into GemFire for every tweet in the feed. 

The _HashTagAnalyzerFunction_ is an example of a complex aggregation algorithm for which a GemFire function execution is appropriate. It accepts a target hash tag, e.g. "java" and iterates over all cached entries returning an aggregate count of associated hash tags. That is, it computes the number of occurences of other hashtags contained in all tweets containing the target hash tag. For example: 

	new HashTagAnalyzerFunction().aggregateAssociatedHashTags(data "java") 

yields a result like the following: So tweets containing the #java hashtag most frequently are about jobs. 12 also contained #appengine … interesting…
	
	jobs:12
	appengine:12
	job:11
	php:4
	soudev:4
	sql:4
	js:4
	html5:3
	jobboard:3
	css:3
	opdrachten:3
	ios:3
	desarrolladores:3
	braziljs:3
	javascript:3
	j2se:2
	desktop:2
	programadores:2
	hibernate:2
	asp:2
	development:2
	computers:2
	programming:2
	c:2
	framework:2
	developer:2
	contratando:2
	jetty:2
	…
	
Running this as a GemFire function means this logic is co-located with the data. This is much more efficient than pulling all the cached entries over the network to a remote process that does the calculation. This is especially true for large data sets. Additionally, using a partitioned region, this work may be distributed among cache members and run in parallel; the final results from each member aggregated in the node that invoked the function execution. This is similar to the way map-reduce works. 


Set Up
----

Running the demo requires

* Download the [Spring XD distribution](http://repo.springsource.org/libs-milestone/org/springframework/xd/spring-xd/1.0.0.M2/spring-xd-1.0.0.M2.zip), if you haven't done so already

* Build the gemfire-demo project and install required artifacts to XD

		$cd gemfire-demo
		$./gradlew jar
		$./install

* Start the Gemfire server included with the XD distribution

     	$ cd $XD_HOME/gemfire
     	$ bin/gemfire-server config/twitter-demo.xml

* Start the XD SingleNode server (use a non-default http port)

		$cd $XD_HOME/xd
		$bin/xd-singlenode --httpPort 8081

* Create a twitter feed and a gemfire tap. This project includes a _tweetsetup_ script for convenience:

		$cd gemfire-demo
		$./tweetsetup

This creates a mock twitter feed that reads tweets from a file and outputs to a log, along with a tap to feed the GemFire cache. This is equivalent to the following XD shell commands:

    xd>stream create --name tweets --definition ="tail --name=$FILE --fromEnd=false | log" --deploy false
    xd>tap create --name hashtags --definition "tap@tweets | tweetToMap | gemfire-server --keyExpression=payload['id']"
    xd>stream deploy tweets
 
Where _$FILE_ is gemfire-demo/data/tweets.out in this case.

To use a live twitter feed, you can create the "tweets" stream as:    

	"twittersearch --query=#Java | log"

In fact the tweets.out file was created by an XD stream:

	"twittersearch --query=#Java | file"

(NOTE: See the XD documentation re. twitter authorization requirements)

So now we have started the twitter feed and populating the cache. Note the _tweetToMap_ processor used in the tap. This converts Json rendered tweets to a Map containing selected fields. This is backed by the _TweetToMapTransformer_ in the _hashtag-analyzer_ project. The module and the jar containing the transformer was deployed to XD before via the install command.

The jar was also copied to GemFire's classpath since _HashTagAnalyzerFunction_ is configured as a GemFire remote function and will run in the cache server process when invoked.
	
* Start the hashtag REST service by running _Application_ in the _hashtag-rest_ project. This is a service built with [Spring Boot](http://blog.springsource.org/2013/08/06/spring-boot-simplifying-spring-for-everyone/) and runs by default on localhost:8080. Once the application starts, point your browser to [http://localhost:8080/associatedhashtags/java](http://localhost:8080/associatedhashtags/java)  where java is the target hashtag. This will invoke the remote funciton and return the results as JSON. Something like:

		{"jobs":12,"appengine":12,"job":11,"php":4,"soudev":4,"sql":4,"js":4,"html5":3,"jobboard":		3,"css":3,"opdrachten":3,"ios":3,"desarrolladores":3,"braziljs":3,"javascript":3,"j2se":		2,"desktop":2,"programadores":2,"hibernate":2,"asp":2,"development":2,"computers":		2,"programming":2,"c":2,"framework":2,"developer":2,"contratando":2,"jetty":2,"nuevoleￃﾳn":		2,"vacatures":2,"xml":2,"logic":2,"cs":2,"mￃﾩxico":2,"android":2,"engineering":	2,"windowsazure":	2,"monterrey":2,"game":2,"html":2,"shell":1,"feinabarcelona":1,"caffeine":	1,"j2ee":1,"cafￃﾩ":	1,"stringstextinputoutputi":1,"lambda":1,"brew":1,"batiktulis":1,"songket":	1,"servlets":1,"bag":	1,"like":1,"like4like":1,"startup":1,"7u40":1,"karawitan":	1,"gesformexico":1,"rijobs":1,"net":	1,"spring":1,"javajob":1,"crafts":1,"vacantes":	1,"telecommunications":1,"giftfromjogja":	1,"pictofme":1,"cupofjoe":1,"java8":1,"bali":	1,"consulting":1,"morningmud":1,"littforsinket":	1,"traditional":1,"web":1,"developers":1,"istimewa":1,"testing":1,"kansas":1,"ca":1,"struts":	1,"love":1,"python":1,"automotive":1,"software":1,"sofwaredeveloper":1,"javaee":1,"processing":	1,"fields":1,"organigrama":1,"espresso":1,"coffee":1,"class":1,"likeaboss":1,"picture":	1,"empleo":1,"rest":1,"reflection":1,"sublimetext":1,"ipad":1,"rh":1,"routine":1,"bcnjobs":	1,"vmware":1,"oracle":1,"scala":1,"programador":1,"network":1,"eclipse":1,"ejb":1,"creativeblog":	1,"cappuccino":1,"sumatra":1,"latte":1,"develop":1,"anny":1,"sketch":1,"engineer":1,"instrument":	1,"iphone":1,"database":1,"employeeit":1,"estructurastablasconsusrelaciones":1,"group":	1,"engineers":1,"energy":1} 
