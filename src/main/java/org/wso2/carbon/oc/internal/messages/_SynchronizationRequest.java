/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.oc.internal.messages;

import org.wso2.carbon.user.api.Tenant;

/**
 * Created by jayanga on 11/10/14.
 */
public class _SynchronizationRequest {
    private double freeMemory;
    private double idleCpuUsage;
    private double systemCpuUsage;
    private double userCpuUsage;
    private String adminServiceUrl;
    private String serverUpTime;
    private int threadCount;
    private double systemLoadAverage;
    private long timestamp;
    private Tenant tenants[];


    public double getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(double freeMemory) {
        this.freeMemory = freeMemory;
    }

    public double getIdleCpuUsage() {
        return idleCpuUsage;
    }

    public void setIdleCpuUsage(double idleCpuUsage) {
        this.idleCpuUsage = idleCpuUsage;
    }

    public double getSystemCpuUsage() {
        return systemCpuUsage;
    }

    public void setSystemCpuUsage(double systemCpuUsage) {
        this.systemCpuUsage = systemCpuUsage;
    }

    public double getUserCpuUsage() {
        return userCpuUsage;
    }

    public void setUserCpuUsage(double userCpuUsage) {
        this.userCpuUsage = userCpuUsage;
    }

    public String getAdminServiceUrl() {
        return adminServiceUrl;
    }

    public void setAdminServiceUrl(String adminServiceUrl) {
        this.adminServiceUrl = adminServiceUrl;
    }

    public String getServerUpTime() {
        return serverUpTime;
    }

    public void setServerUpTime(String serverUpTime) {
        this.serverUpTime = serverUpTime;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Tenant[] getTenants() { return tenants; }

    public void setTenants(Tenant[] tenants) { this.tenants = tenants; }
}
