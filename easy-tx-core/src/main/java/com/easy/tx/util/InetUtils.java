package com.easy.tx.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class InetUtils {
    private static final Log log = LogFactory.getLog(InetUtils.class);


    public static InetUtils.HostInfo findFirstNonLoopbackHostInfo() {
        InetAddress address = findFirstNonLoopbackAddress();
        return convertAddress(address);
    }

    public static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface
                    .getNetworkInterfaces(); nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    log.trace("Testing interface: " + ifc.getDisplayName());
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else if (result != null) {
                        continue;
                    }
                    for (Enumeration<InetAddress> addrs = ifc
                            .getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        if (address instanceof Inet4Address
                                && !address.isLoopbackAddress()) {
                            log.trace("Found non-loopback interface: "
                                    + ifc.getDisplayName());
                            result = address;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            log.error("Cannot get first non-loopback address", ex);
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.warn("Unable to retrieve localhost");
        }

        return null;
    }

    public static InetUtils.HostInfo convertAddress(final InetAddress address) {
        InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
        hostInfo.setHostname(address.getHostName());
        hostInfo.setIpAddress(address.getHostAddress());
        return hostInfo;
    }

    public static class HostInfo {
        public boolean override;
        private String ipAddress;
        private String hostname;

        public HostInfo(String hostname) {
            this.hostname = hostname;
        }

        public HostInfo() {
        }

        public int getIpAddressAsInt() {
            InetAddress inetAddress = null;
            String host = this.ipAddress;
            if (host == null) {
                host = this.hostname;
            }

            try {
                inetAddress = InetAddress.getByName(host);
            } catch (UnknownHostException var4) {
                throw new IllegalArgumentException(var4);
            }

            return ByteBuffer.wrap(inetAddress.getAddress()).getInt();
        }

        public boolean isOverride() {
            return this.override;
        }

        public void setOverride(boolean override) {
            this.override = override;
        }

        public String getIpAddress() {
            return this.ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getHostname() {
            return this.hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
    }

    public static void main(String[] args) {
        String hostName = InetUtils.findFirstNonLoopbackHostInfo().getIpAddress();

        System.out.println(hostName);
    }

}
