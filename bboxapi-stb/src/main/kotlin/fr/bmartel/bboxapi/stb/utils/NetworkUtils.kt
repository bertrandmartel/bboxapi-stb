package fr.bmartel.bboxapi.stb.utils

import java.net.NetworkInterface
import java.util.*

class NetworkUtils {

    companion object {
        /**
         * Get IP address from first non-localhost interface
         * @param useIPv4   true=return ipv4, false=return ipv6
         * @return  address or empty string
         * https://stackoverflow.com/a/13007325/2614364
         */
        fun getIPAddress(useIPv4: Boolean): String {
            try {
                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    val addrs = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val sAddr = addr.hostAddress
                            //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                            val isIPv4 = sAddr.indexOf(':') < 0

                            if (useIPv4) {
                                if (isIPv4)
                                    return sAddr
                            } else {
                                if (!isIPv4) {
                                    val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                    return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                                }
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
            // for now eat exceptions
            return ""
        }
    }
}