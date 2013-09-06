/*
 * Compatible with twittersearch ("hashTags") or twitterstream("hashtags")
 */
if (payload.getValue('entities')) {
	if (payload.getValue('entities')['hashtags'] != null) {
		return payload.getValue('entities')['hashtags'].size() > 0
	}
	if (payload.getValue('entities')['hashTags'] != null) {
		return payload.getValue('entities')['hashTags'].size() > 0
	}
} 
false
