package com.pig.serializer;

import com.pig.remoting.exception.RemotingCommandException;
import com.pig.remoting.protocol.RemotingCommand;
import com.pig.remoting.protocol.RemotingCommandCode;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TccRemotingCommandSerializer implements RemotingCommandSerializer {
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    @Override
    public byte[] serialize(RemotingCommand remotingCommand) {
        byte[] remarkBytes = null;
        int remarkLength = 0;
        if (remotingCommand.getRemark() != null && remotingCommand.getRemark().length() > 0) {
            remarkBytes = remotingCommand.getRemark().getBytes(CHARSET_UTF8);
            remarkLength = remarkBytes.length;
        }
        int bodyLength = 0;
        if (remotingCommand.getBody() != null && remotingCommand.getBody().length > 0) {
            bodyLength = remotingCommand.getBody().length;
        }

        int totalLength = calTotalLength(remarkLength, bodyLength);

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalLength);

        byteBuffer.put(remotingCommand.getCode().value());
        byteBuffer.putInt(remotingCommand.getRequestId());
        byteBuffer.putInt(remotingCommand.getServiceCode());
        if (remarkBytes != null) {
            byteBuffer.putInt(remarkLength);
            byteBuffer.put(remarkBytes);
        } else {
            byteBuffer.putInt(0);
        }

        if (remotingCommand.getBody() != null) {
            byteBuffer.putInt(bodyLength);
            byteBuffer.put(remotingCommand.getBody());
        } else {
            byteBuffer.putInt(0);
        }
        return byteBuffer.array();
    }

    @Override
    public RemotingCommand deserialize(byte[] bytes) {
        RemotingCommand remotingCommand = new RemotingCommand();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        remotingCommand.setCode(RemotingCommandCode.valueOf(byteBuffer.get()));
        remotingCommand.setRequestId(byteBuffer.getInt());
        remotingCommand.setServiceCode(byteBuffer.getInt());
        int remarkLength = byteBuffer.getInt();
        if (remarkLength > 0) {
            if (remarkLength > bytes.length) {
                throw new RemotingCommandException("TCC remoting protocol decoding failed, remark length: " + remarkLength + ", but total length: " + bytes.length);
            }
            byte[] remarkContent = new byte[remarkLength];
            byteBuffer.get(remarkContent);
            remotingCommand.setRemark(new String(remarkContent, CHARSET_UTF8));
        }
        int bodyLength = byteBuffer.getInt();
        if (bodyLength > 0) {
            if (bodyLength > bytes.length) {
                throw new RemotingCommandException("TCC Remoting protocol decoding failed, body length: " + bodyLength + ", but total length " + bytes.length);
            }
            byte[] body = new byte[bodyLength];
            byteBuffer.get(body);
            remotingCommand.setBody(body);
        }
        return remotingCommand;
    }

    private int calTotalLength(int remarkLength, int bodyLength) {
        return 1
                // request id
                + 4
                // service code
                + 4
                // string remark
                + 4 + remarkLength
                // HashMap<String,String> extFields
                + 4 + bodyLength;
    }

    @Override
    public RemotingCommand clone(RemotingCommand original) {
        if (original == null) {
            return null;
        }
        RemotingCommand cloned = new RemotingCommand();
        cloned.setCode(original.getCode());
        cloned.setRequestId(original.getRequestId());
        cloned.setServiceCode(original.getServiceCode());
        cloned.setRemark(original.getRemark());
        cloned.setBody(original.getBody());
        return cloned;
    }
}
