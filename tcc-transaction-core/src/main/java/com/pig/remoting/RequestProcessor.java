package com.pig.remoting;

import com.pig.remoting.protocol.RemotingCommand;

public interface RequestProcessor<T> {
    RemotingCommand processRequest(T context, RemotingCommand remotingCommand);
}
