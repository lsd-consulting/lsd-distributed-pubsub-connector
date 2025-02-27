package io.lsdconsulting.lsd.distributed.pubsub.util

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.common.util.concurrent.MoreExecutors.directExecutor
import lsd.logging.log

// Below was adapted from:
// https://github.com/googleapis/java-pubsub/blob/main/README.md?plain=1#L142-L150
object PubsubPublishCallback : ApiFutureCallback<String> {
    override fun onSuccess(messageId: String) {
        log().info("Published with message id: $messageId")
    }

    override fun onFailure(t: Throwable) {
        log().error("Failed to publish", t)
    }
}

fun ApiFuture<String>.addPubsubPublishCallback() {
    ApiFutures.addCallback(this, PubsubPublishCallback, directExecutor())
}