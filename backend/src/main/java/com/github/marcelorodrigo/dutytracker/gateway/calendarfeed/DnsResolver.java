package com.github.marcelorodrigo.dutytracker.gateway.calendarfeed;

import java.net.InetAddress;
import java.net.UnknownHostException;

interface DnsResolver {
    InetAddress[] resolve(String host) throws UnknownHostException;
}
