/*
 * Compatible with twittersearch ("hashTags") or twitterstream("hashtags")
 */
payload.getValue('entities')?['hashtags']?.size()>0 || payload.getValue('entities')?['hashTags']?.size()>0