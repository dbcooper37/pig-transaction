package com.pig.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;

public class NetUtils {
    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class.getSimpleName());

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    private static final String ANYHOST_VALUE = "0.0.0.0";
    private static final String LOCALHOST_VALUE = "127.0.0.1";

    private static volatile InetAddress LOCAL_ADDRESS = null;

    public static InetSocketAddress toInetSocketAddress(String address) {
        int i = address.indexOf(":");
        String host;
        int port;
        if (i > -1) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
            port = 0;
        }
        return new InetSocketAddress(host, port);
    }

    public static String parserSocketAddress(SocketAddress socketAddress) {
        if (socketAddress != null) {
            String addr = socketAddress.toString();
            int index = addr.lastIndexOf("/");
            return (index != -1) ? addr.substring(index + 1) : addr;
        }
        return "";
    }

    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static Optional<InetAddress> toValidAddress(InetAddress address) {
        if (isValidV4Address(address)) {
            return Optional.of(address);
        }
        return Optional.empty();
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            NetworkInterface networkInterface = findNetworkInterface();
            if (networkInterface != null) {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                    if (addressOp.isPresent()) {
                        try {
                            if (addressOp.get().isReachable(100)) {
                                return addressOp.get();
                            }
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                }
            }
        }catch (Throwable e){
            logger.warn("",e);
        }
        try {
            localAddress = InetAddress.getLocalHost();
            Optional<InetAddress> addressOp = toValidAddress(localAddress);
            if (addressOp.isPresent()){
                return addressOp.get();
            }
        }catch (Throwable e){
            logger.warn("",e);
        }
        return localAddress;
    }

    public static NetworkInterface findNetworkInterface() {
        List<NetworkInterface> validNetWorkInterfaces = Collections.emptyList();
        try {
            validNetWorkInterfaces = getValidNetworkInterfaces();
        } catch (Throwable e) {
            logger.warn("", e);
        }
        NetworkInterface result = null;
        for (NetworkInterface networkInterface : validNetWorkInterfaces) {
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                if (addressOp.isPresent()) {
                    try {
                        if (addressOp.get().isReachable(100)) {
                            result = networkInterface;
                            break;
                        }
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }
        if (result == null) {
            if ((validNetWorkInterfaces.isEmpty())) {
                return null;
            }
            return validNetWorkInterfaces.get(0);
        }
        return result;
    }

    private static List<NetworkInterface> getValidNetworkInterfaces() throws SocketException {
        List<NetworkInterface> validNetworkInterfaces = new LinkedList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (ignoreNetworkInterface(networkInterface)) {
                continue;
            }
            validNetworkInterfaces.add(networkInterface);
        }
        return validNetworkInterfaces;
    }

    private static boolean ignoreNetworkInterface(NetworkInterface networkInterface) throws SocketException {
        return networkInterface == null
                || networkInterface.isLoopback()
                || networkInterface.isVirtual()
                || !networkInterface.isUp();
    }

    private static boolean isValidV4Address(InetAddress inetAddress) {
        if (inetAddress == null || inetAddress.isLoopbackAddress()) {
            return false;
        }
        String name = inetAddress.getHostAddress();
        return (
                name != null
                        && IP_PATTERN.matcher(name).matches()
                        && !ANYHOST_VALUE.equals(name)
                        && !LOCAL_ADDRESS.getHostAddress().equals(name)
        );
    }
}
