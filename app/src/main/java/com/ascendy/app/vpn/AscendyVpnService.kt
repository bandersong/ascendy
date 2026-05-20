package com.ascendy.app.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ascendy.app.AscendyApp
import com.ascendy.app.MainActivity
import com.ascendy.app.R
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.ui.theme.vocabFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Minimal DNS-sinkhole VPN. Routes only DNS traffic (UDP/53 to our fake server) through the
 * tunnel. Non-DNS traffic flows out the normal interfaces. For each DNS query we either:
 *   • return NXDOMAIN if the query name matches the blocked set (suffix-aware), or
 *   • forward verbatim to a real upstream DNS, write the response back into the tunnel.
 *
 * This catches anything that uses the system resolver. DoH-only clients (Chrome by default)
 * bypass us — the accessibility-based URL blocker stays in place for those.
 */
class AscendyVpnService : VpnService() {

    private var tun: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var tunnelJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            shutdown()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        startForegroundIfNeeded()
        if (tun == null) startTunnel()
        return START_STICKY
    }

    private fun startTunnel() {
        try {
            val builder = Builder()
                .setSession("Ascendy")
                .addAddress("10.10.10.1", 32)
                .addAddress(DNS_FAKE_SERVER_V6_ADDR, 128)
                .addDnsServer(DNS_FAKE_SERVER)
                .addDnsServer(DNS_FAKE_SERVER_V6)
                .addRoute(DNS_FAKE_SERVER, 32)
                .addRoute(DNS_FAKE_SERVER_V6, 128)
                .setMtu(1500)
                .setBlocking(true)
            // exclude our own package from the VPN so we don't loop
            try { builder.addDisallowedApplication(packageName) } catch (_: Exception) {}
            tun = builder.establish() ?: run {
                Log.w(TAG, "tunnel establish() returned null")
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "tunnel establish failed", e)
            return
        }
        val fd = tun ?: return
        tunnelJob = scope.launch { run(fd) }
    }

    private suspend fun run(fd: ParcelFileDescriptor) {
        val input = FileInputStream(fd.fileDescriptor)
        val output = FileOutputStream(fd.fileDescriptor)
        val buf = ByteArray(2048)
        while (scope.isActive) {
            val n = try { input.read(buf) } catch (_: Exception) { -1 }
            if (n < 28) continue
            try {
                handle(buf.copyOfRange(0, n), output)
            } catch (e: Exception) {
                Log.w(TAG, "packet handle error", e)
            }
        }
    }

    private fun handle(packet: ByteArray, output: FileOutputStream) {
        when (packet[0].toInt() and 0xF0) {
            0x40 -> handleIpv4(packet, output)
            0x60 -> handleIpv6(packet, output)
        }
    }

    private fun handleIpv4(packet: ByteArray, output: FileOutputStream) {
        val ihl = (packet[0].toInt() and 0x0F) * 4
        if (packet[9].toInt() and 0xFF != 17) return  // only UDP
        val udpStart = ihl
        val dstPort = ((packet[udpStart + 2].toInt() and 0xFF) shl 8) or
                      (packet[udpStart + 3].toInt() and 0xFF)
        if (dstPort != 53) return                      // only DNS

        val srcIp = packet.copyOfRange(12, 16)
        val dstIp = packet.copyOfRange(16, 20)
        val srcPort = ((packet[udpStart].toInt() and 0xFF) shl 8) or
                      (packet[udpStart + 1].toInt() and 0xFF)
        val dns = packet.copyOfRange(udpStart + 8, packet.size)

        val qname = DnsTools.parseQName(dns) ?: return
        val isBlocked = BlockState.isDomainBlocked(qname)
        Log.d(TAG, "dns q=$qname blocked=$isBlocked")
        val response = if (isBlocked) DnsTools.nxdomainResponse(dns) else forwardUpstream(dns) ?: return

        val replyPacket = DnsTools.buildIpv4UdpPacket(
            srcIp = dstIp, dstIp = srcIp,
            srcPort = dstPort, dstPort = srcPort,
            payload = response,
        )
        try { output.write(replyPacket) } catch (e: Exception) { Log.w(TAG, "write reply failed", e) }
    }

    private fun handleIpv6(packet: ByteArray, output: FileOutputStream) {
        // IPv6 fixed header is 40 bytes; need at least 40 + 8 (UDP) = 48
        if (packet.size < 48) return
        if (packet[6].toInt() and 0xFF != 17) return  // next header must be UDP
        val dstPort = ((packet[42].toInt() and 0xFF) shl 8) or (packet[43].toInt() and 0xFF)
        if (dstPort != 53) return

        val srcIp6 = packet.copyOfRange(8, 24)
        val dstIp6 = packet.copyOfRange(24, 40)
        val srcPort = ((packet[40].toInt() and 0xFF) shl 8) or (packet[41].toInt() and 0xFF)
        val dns = packet.copyOfRange(48, packet.size)

        val qname = DnsTools.parseQName(dns) ?: return
        val isBlocked = BlockState.isDomainBlocked(qname)
        Log.d(TAG, "dns6 q=$qname blocked=$isBlocked")
        val response = if (isBlocked) DnsTools.nxdomainResponse(dns) else forwardUpstream(dns) ?: return

        val replyPacket = DnsTools.buildIpv6UdpPacket(
            srcIp = dstIp6, dstIp = srcIp6,
            srcPort = dstPort, dstPort = srcPort,
            payload = response,
        )
        try { output.write(replyPacket) } catch (e: Exception) { Log.w(TAG, "write ipv6 reply failed", e) }
    }

    // Try IPv4 upstream first; fall back to IPv6 (needed on IPv6-only 5G networks).
    private fun forwardUpstream(query: ByteArray): ByteArray? =
        tryForward(query, UPSTREAM_DNS) ?: tryForward(query, UPSTREAM_DNS_V6)

    private fun tryForward(query: ByteArray, host: String): ByteArray? {
        return try {
            val upstream = InetAddress.getByName(host)
            val wildcard = if (upstream is java.net.Inet6Address) "::" else "0.0.0.0"
            val socket = DatagramSocket(null).also {
                it.bind(java.net.InetSocketAddress(InetAddress.getByName(wildcard), 0))
            }
            protect(socket)
            try {
                socket.soTimeout = 2000
                socket.send(DatagramPacket(query, query.size, upstream, 53))
                val replyBuf = ByteArray(4096)
                val reply = DatagramPacket(replyBuf, replyBuf.size)
                socket.receive(reply)
                replyBuf.copyOfRange(0, reply.length)
            } finally {
                socket.close()
            }
        } catch (e: Exception) {
            Log.w(TAG, "upstream $host failed: ${e.message}")
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shutdown()
    }

    private fun shutdown() {
        try { tunnelJob?.cancel() } catch (_: Exception) {}
        try { tun?.close() } catch (_: Exception) {}
        tun = null
        scope.cancel()
    }

    private fun startForegroundIfNeeded() {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            nm?.getNotificationChannel(CHANNEL_ID) == null) {
            nm?.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "VPN blocking", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Active when DNS sinkhole is enforcing website blocks."
                }
            )
        }
        val app = applicationContext as AscendyApp
        val vocab = vocabFor(app.currentVariant)
        val tap = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopPi = PendingIntent.getService(
            this, 1,
            Intent(this, AscendyVpnService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(vocab.vpnNotifTitle)
            .setContentText(vocab.vpnNotifText)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(tap)
            .addAction(0, "Stop", stopPi)
            .build()
        startForeground(NOTIF_ID, notif)
    }

    companion object {
        const val ACTION_STOP = "com.ascendy.app.VPN_STOP"
        private const val CHANNEL_ID = "ascendy.vpn"
        private const val NOTIF_ID = 4243
        private const val DNS_FAKE_SERVER = "10.10.10.10"
        private const val DNS_FAKE_SERVER_V6_ADDR = "fd00:1::1"   // VPN interface IPv6 address
        private const val DNS_FAKE_SERVER_V6 = "fd00:1::10"       // IPv6 fake DNS sink
        private const val UPSTREAM_DNS = "1.1.1.1"
        private const val UPSTREAM_DNS_V6 = "2606:4700:4700::1111"
        private const val TAG = "AscendyVpn"
    }
}
