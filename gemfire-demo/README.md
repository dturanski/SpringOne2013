Spring XD and GemFire Real Time Analytics Demo
=

Description
---
The _gemfire-demo_ project illustrates a simple use case for using [GemFire](http://gopivotal.com/pivotal-products/pivotal-data-fabric/pivotal-gemfire) with [Spring XD](http://projects.spring.io/spring-xd/) for real time analytics. It taps a twitter feed or twitter search and loads summary data along with the hash tags into GemFire for every tweet in the feed. 

The [HashTagAnalyzerFunction](https://github.com/dturanski/SpringOne2013/blob/master/gemfire-demo/hashtag-analyzer/src/main/java/org/springframework/xd/demo/gemfire/function/HashTagAnalyzerFunction.java) is an example of a complex aggregation algorithm for which a GemFire [function execution](http://pubs.vmware.com/vfabric53/topic/com.vmware.vfabric.gemfire.7.0/developing/function_exec/chapter_overview.html) is appropriate. It accepts a target hash tag, e.g. "java" and iterates over all cached entries returning an aggregate count of associated hash tags. That is, it computes the number of occurences of other hashtags contained in all tweets containing the target hash tag. For example: 

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
	
Running this as a GemFire function means this logic is co-located with the data and may be executed as a single call to GemFire. This is much more efficient than pulling all the cached entries over the network to a remote process that does the calculation. This is especially true for large data sets. Additionally, using a [partitioned region](http://pubs.vmware.com/vfabric53/topic/com.vmware.vfabric.gemfire.7.0/developing/partitioned_regions/how_partitioning_works.html), this work may be distributed among cache members and run in parallel; the final results from each member aggregated in the node that invoked the function execution. This is similar to the way map-reduce works. 


Set Up
----

Running the demo requires

* Download the [Spring XD distribution](http://repo.springsource.org/libs-milestone/org/springframework/xd/spring-xd/1.0.0.M2/spring-xd-1.0.0.M2.zip) (TODO: This demo may not work under M2 - a susequent fix was added to avoid a NPE), if you haven't done so already

* Build the gemfire-demo project and install required artifacts to XD

		$ cd gemfire-demo
		$ ./gradlew jar
		$ ./install

* Start the Gemfire server included with the XD distribution

     	$ cd $XD_HOME/gemfire
     	$ bin/gemfire-server config/twitter-demo.xml

* Start the XD SingleNode server

		$ cd $XD_HOME/xd
		$ bin/xd-singlenode 
		
* Start the XD Shell
		
		$cd $XD_HOME/shell
		$bin/xd-shell

* Create a feed
	* Create a mock twitter feed

			$xd> stream create tweets --definition "tail --name=$FILE --fromEnd=false | randomDelay --max=200 | log" 
			
			where _$FILE_ is gemfire-demo/../data/javatweets.out in this case.

		
	*	Create a real twitter feed
	
			xd:> stream create tweets --definition "twittersearch --query='#spring+OR+#java+OR+#groovy+OR+#grails+OR+#javascipt+OR+#s12gx' | log"

* Set up a gemfire tap

		xd:> tap create hashtags --definition "tap tweets | json-to-tuple | filter --script=hashtags.groovy | tweetToSummary | gemfire-server --keyExpression=payload['id']"

		(NOTE: See the XD documentation re. twitter authorization requirements)

So now we have started the twitter feed and populating the cache.  The _hashtags_ tap converts each  twitter payload to an XD Tuple (a generic map like structure). Tweets are filtered for those containing at least one hash tag using the [hashtags.groovy](https://github.com/dturanski/SpringOne2013/tree/master/gemfire-demo/scripts/hashtags.groovy) script. The filter is not really necessary if using the twittersearch source, since the Twitter API has already done the filtering. 
 
Note the [tweetToSummary](https://github.com/dturanski/SpringOne2013/tree/master/gemfire-demo/modules/processors/tweetToSummary.xml) processor used in the tap. This converts filtered payloads to a [TweetSummary](https://github.com/dturanski/SpringOne2013/blob/master/gemfire-demo/hashtag-analyzer/src/main/java/org/springframework/xd/demo/gemfire/TweetSummary.java) containing selected fields. This is backed by the [TweetToTweetSummaryTransformer](https://github.com/dturanski/SpringOne2013/blob/master/gemfire-demo/hashtag-analyzer/src/main/java/org/springframework/xd/demo/gemfire/TweetToTweetSummaryTransformer.java) in the _hashtag-analyzer_ project. The custom module, groovy scripts, and the jar containing the transformer and any other required classes was deployed to XD before via the install command.

The jar is also copied to GemFire's classpath since [HashTagAnalyzerFunction](https://github.com/dturanski/SpringOne2013/blob/master/gemfire-demo/hashtag-analyzer/src/main/java/org/springframework/xd/demo/gemfire/function/HashTagAnalyzerFunction.java) is configured as a GemFire remote function and will run in the cache server process when invoked. Additionally, the function references _TweetSummary_ so that must be on GemFire's classpath as well.
	
* Start the hashtag REST service by running [Application](https://github.com/dturanski/SpringOne2013/blob/master/gemfire-demo/hashtag-rest/src/main/java/org/springframework/xd/demo/gemfire/Application.java) in the _hashtag-rest_ project:

	* Build an executable jar:
 		
 			$ ./gradlew build
 			$ java -jar hashtag-rest/build/libs/hashtag-rest.jar --server.port=8081
 	
 	* Run in Eclipse. (NOTE: I had to fix the classpath by setting the run configuration classpath to only include exported entries. Then select the following exported jars from hashtag-analyzer properties: spring-data-gemfire, gemfire, spring-data-commons, spring-tx )
 
 			$ ./gradlew eclipse
	    

The REST service was built with [Spring Boot](http://blog.springsource.org/2013/08/06/spring-boot-simplifying-spring-for-everyone/) and should run on a non-default port, e.g., localhost:8081. Start the service and point your browser to [http://localhost:8081/associatedhashtags/java](http://localhost:8081/associatedhashtags/java)  where _java_ is a path variable containing the target hashtag. This will invoke the remote funciton and return the results as JSON. Something like:

	{"jobs":12,"appengine":12,"job":11,"php":4,"soudev":4,"sql":4,"js":4,"html5":3,"jobboard":	3,"css":3,"opdrachten":3,"ios":3,"desarrolladores":3,"braziljs":3,"javascript":3,"j2se":	2,"desktop":2,"programadores":2,"hibernate":2,"asp":2,"development":2,"computers":	2,"programming":2,"c":2,"framework":2,"developer":2,"contratando":2,"jetty":2,"nuevoleￃﾳn":	2,"vacatures":2,"xml":2,"logic":2,"cs":2,"mￃﾩxico":2,"android":2,"engineering":	2,"windowsazure":	2,"monterrey":2,"game":2,"html":2,"shell":1,"feinabarcelona":1,"caffeine":	1,"j2ee":1,"cafￃﾩ":	1,"stringstextinputoutputi":1,"lambda":1,"brew":1,"batiktulis":1,"songket":	1,"servlets":1,"bag":	1,"like":1,"like4like":1,"startup":1,"7u40":1,"karawitan":	1,"gesformexico":1,"rijobs":1,"net":	1,"spring":1,"javajob":1,"crafts":1,"vacantes":	1,"telecommunications":1,"giftfromjogja":	1,"pictofme":1,"cupofjoe":1,"java8":1,"bali":	1,"consulting":1,"morningmud":1,"littforsinket":	1,"traditional":1,"web":1,"developers":1,"istimewa":1,"testing":1,"kansas":1,"ca":1,"struts":	1,"love":1,"python":1,"automotive":1,"software":1,"sofwaredeveloper":1,"javaee":1,"processing":	1,"fields":1,"organigrama":1,"espresso":1,"coffee":1,"class":1,"likeaboss":1,"picture":	1,"empleo":1,"rest":1,"reflection":1,"sublimetext":1,"ipad":1,"rh":1,"routine":1,"bcnjobs":	1,"vmware":1,"oracle":1,"scala":1,"programador":1,"network":1,"eclipse":1,"ejb":1,"creativeblog":	1,"cappuccino":1,"sumatra":1,"latte":1,"develop":1,"anny":1,"sketch":1,"engineer":1,"instrument":	1,"iphone":1,"database":1,"employeeit":1,"estructurastablasconsusrelaciones":1,"group":	1,"engineers":1,"energy":1}
	
The REST API also includes 

[http://localhost:8081/hashtagcounts](http://localhost:8081/hashtagcounts) to return the accumulated total hashtag counts (Analogous to XD field-value-counter but provided here as a convenience)

[http://localhost:8081/watchhashtag/{target}](http://localhost:8081/watchhashtag/java) which 
illustrates the use of GemFire's Continuous Query capability. This is implemented for long polling using Spring MVC's asynchronous support. The initial invocation for a new target hash tag will return the the current result set and create a continuous query so subsequent invocations will return any new tweets matching the target  
