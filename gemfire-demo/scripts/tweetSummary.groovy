import org.springframework.xd.demo.gemfire.TweetSummary
import groovy.json.JsonSlurper
import org.joda.time.*
import org.joda.time.format.*

if (payload instanceof String) {
	
	JsonSlurper slurper = new JsonSlurper()
	def result = slurper.parseText(payload)
	DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
	DateTime dt = DateTime.parse(result.created_at,formatter);

	TweetSummary tweetSummary = new TweetSummary(
		id:result.id,
		text: result.text,
		hashTags:result.entities?.hashtags*.text,
		lang:result.lang,
		createdAt: dt.getMillis()
	)
	return tweetSummary
}
new TweetSummary(payload)