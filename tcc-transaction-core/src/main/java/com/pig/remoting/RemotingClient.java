package com.pig.remoting;

import com.pig.remoting.protocol.RemotingCommand;

public interface RemotingClient<T> extends RemotingService<T> {
    RemotingCommand invokeSync(final String addr, final RemotingCommand request, final long timeoutMillis);

    void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis);
}
