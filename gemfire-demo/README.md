Gemfire RT Analytics Demo
==

Initial Prototype hashTag counter.
Data loaded via an XD stream 

	twittersearch --query=#Java | gemfire-json-server --regionName=tweets --keyExpression=payload.getField('id')"
	
Also requires starting the (Spring backed)Gemfire server in $XD_HOME/gemfire using config/twitter-demo.xml

	
CachInspector loads tweets from a cache server and invokes HashTagAnalyzer.  HTA uses a FieldValueCounter to loop through every cache entry and count the occurence of associated hashTags for tweets containing hashtags there are multiple hashTags. 

So 

	new HashTagAnalyzer().aggregateAssociatedHashTags(data "java") 

yields something like: So of all tweets containing #java, 15 also contained #appengine interesting…
	
	appengine=15
	coffee=6
	tassimo=4	
	jobs=4
	smiles=4
	happy=4
	goodmorning=4
	mmmm=4
	programming=3
	job=3
	unamcert=3
	exploit=3
	cs=3
	engineering=3
	indonesia=3
	love=3
	web=3
	logic=3
	computers=3
	kopdaristura=2
	beach=2
	android=2
	rest=2
	software=2

This kind of thing is suitable to implement as a GemFire remote function. Could create a Gemfire tap and set cache entries to expire in 30 min for example to look at related trends.