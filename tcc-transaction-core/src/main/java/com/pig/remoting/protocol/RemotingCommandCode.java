package com.pig.remoting.protocol;

public enum RemotingCommandCode {
    SERVICE_REQ((byte) 0, true),
    SERVICE_RESP((byte) 1, false),

    HEARTBEAT_REQ((byte) 5, true),
    HEARTBEAT_RESP((byte) 6, false),
    SYSTEM_BUSY_RESP((byte) 7, false),
    SYSTEM_EXCEPTION_RESP((byte) 8, false);

    private byte value;

    private boolean isRequestCode;

    private RemotingCommandCode(byte value, boolean isRequestCode) {
        this.value = value;
        this.isRequestCode = isRequestCode;
    }

    public static RemotingCommandCode valueOf(byte value) {
        if (value == SERVICE_REQ.value) {
            return SERVICE_REQ;
        } else if (value == SERVICE_RESP.value) {
            return SERVICE_RESP;
        } else if (value == HEARTBEAT_REQ.value) {
            return HEARTBEAT_REQ;
        } else if (value == HEARTBEAT_RESP.value) {
            return HEARTBEAT_RESP;
        } else if (value == SYSTEM_BUSY_RESP.value()) {
            return SYSTEM_BUSY_RESP;
        } else if (value == SYSTEM_EXCEPTION_RESP.value()) {
            return SYSTEM_EXCEPTION_RESP;
        }

        throw new RuntimeException(String.format("unknown RemotingCommand Type of value :%d", value));
    }

    public byte value() {
        return this.value;
    }

    public boolean isRequestCode() {
        return isRequestCode;
    }
}
