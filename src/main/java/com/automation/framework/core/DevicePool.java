package com.automation.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages device pool for parallel test execution.
 * Reads device configurations from properties files and allocates devices to
 * threads.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public class DevicePool {
    private static final Logger logger = LoggerFactory.getLogger(DevicePool.class);

    private static final ThreadLocal<DeviceConfig> currentDevice = new ThreadLocal<>();
    private static final ThreadLocal<DeviceConfig> currentIOSDevice = new ThreadLocal<>();
    private static final AtomicInteger deviceIndex = new AtomicInteger(0);
    private static final AtomicInteger iosDeviceIndex = new AtomicInteger(0);
    private static List<DeviceConfig> devicePool = new ArrayList<>();
    private static List<DeviceConfig> iosDevicePool = new ArrayList<>();
    private static boolean initialized = false;

    /**
     * Device configuration holder
     */
    public static class DeviceConfig {
        private final String deviceName;
        private final String platformVersion;
        private final int index;

        public DeviceConfig(String deviceName, String platformVersion, int index) {
            this.deviceName = deviceName;
            this.platformVersion = platformVersion;
            this.index = index;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getPlatformVersion() {
            return platformVersion;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return String.format("Device[%d]: %s (OS: %s)", index, deviceName, platformVersion);
        }
    }

    private DevicePool() {
        // Private constructor
    }

    /**
     * Initialize device pools from configuration.
     * Reads device.{index}.name and device.{index}.platformVersion for Android.
     * Reads iosDevice.{index}.name and iosDevice.{index}.platformVersion for iOS.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        // Initialize Android device pool
        devicePool.clear();
        int index = 1;

        while (true) {
            String deviceName = ConfigManager.get("device." + index + ".name");
            String platformVersion = ConfigManager.get("device." + index + ".platformVersion");

            if (deviceName == null || platformVersion == null) {
                break; // No more devices in pool
            }

            DeviceConfig device = new DeviceConfig(deviceName, platformVersion, index);
            devicePool.add(device);
            logger.info("Added to Android device pool: {}", device);
            index++;
        }

        // Initialize iOS device pool
        iosDevicePool.clear();
        index = 1;

        while (true) {
            String deviceName = ConfigManager.get("iosDevice." + index + ".name");
            String platformVersion = ConfigManager.get("iosDevice." + index + ".platformVersion");

            if (deviceName == null || platformVersion == null) {
                break; // No more iOS devices in pool
            }

            DeviceConfig device = new DeviceConfig(deviceName, platformVersion, index);
            iosDevicePool.add(device);
            logger.info("Added to iOS device pool: {}", device);
            index++;
        }

        if (devicePool.isEmpty() && iosDevicePool.isEmpty()) {
            logger.warn("No device pools configured. Using default devices from config.");
        } else {
            if (!devicePool.isEmpty()) {
                logger.info("Android device pool initialized with {} devices", devicePool.size());
            }
            if (!iosDevicePool.isEmpty()) {
                logger.info("iOS device pool initialized with {} devices", iosDevicePool.size());
            }
        }

        initialized = true;
    }

    /**
     * Get the next available device from the pool for the current thread.
     * Uses round-robin allocation.
     * 
     * @return DeviceConfig for the current thread
     */
    public static DeviceConfig getDevice() {
        initialize();

        // Check if thread already has a device assigned
        DeviceConfig assigned = currentDevice.get();
        if (assigned != null) {
            return assigned;
        }

        // If no device pool, return null (will use default device from config)
        if (devicePool.isEmpty()) {
            return null;
        }

        // Allocate next device from pool (round-robin)
        int index = deviceIndex.getAndIncrement() % devicePool.size();
        DeviceConfig device = devicePool.get(index);
        currentDevice.set(device);

        logger.info("Thread {} allocated device: {}",
                Thread.currentThread().getName(), device);

        return device;
    }

    /**
     * Get device name for the current thread.
     * Falls back to default deviceName from config if no pool device assigned.
     * 
     * @return Device name
     */
    public static String getDeviceName() {
        DeviceConfig device = getDevice();
        if (device != null) {
            return device.getDeviceName();
        }
        // Fallback to default device from config
        return ConfigManager.get("deviceName", "emulator-5554");
    }

    /**
     * Get platform version for the current thread.
     * Falls back to default platformVersion from config if no pool device assigned.
     * 
     * @return Platform version
     */
    public static String getPlatformVersion() {
        DeviceConfig device = getDevice();
        if (device != null) {
            return device.getPlatformVersion();
        }
        // Fallback to default platform version from config
        return ConfigManager.get("platformVersion", "13");
    }

    /**
     * Get the next available iOS device from the pool for the current thread.
     * Uses round-robin allocation.
     * 
     * @return DeviceConfig for the current thread
     */
    public static DeviceConfig getIOSDevice() {
        initialize();

        // Check if thread already has an iOS device assigned
        DeviceConfig assigned = currentIOSDevice.get();
        if (assigned != null) {
            return assigned;
        }

        // If no iOS device pool, return null (will use default device from config)
        if (iosDevicePool.isEmpty()) {
            return null;
        }

        // Allocate next device from pool (round-robin)
        int index = iosDeviceIndex.getAndIncrement() % iosDevicePool.size();
        DeviceConfig device = iosDevicePool.get(index);
        currentIOSDevice.set(device);

        logger.info("Thread {} allocated iOS device: {}",
                Thread.currentThread().getName(), device);

        return device;
    }

    /**
     * Get iOS device name for the current thread.
     * Falls back to default iosDeviceName from config if no pool device assigned.
     * 
     * @return iOS device name
     */
    public static String getIOSDeviceName() {
        DeviceConfig device = getIOSDevice();
        if (device != null) {
            return device.getDeviceName();
        }
        // Fallback to default iOS device from config
        return ConfigManager.get("iosDeviceName", "iPhone 15 Pro");
    }

    /**
     * Get iOS platform version for the current thread.
     * Falls back to default iosPlatformVersion from config if no pool device
     * assigned.
     * 
     * @return iOS platform version
     */
    public static String getIOSPlatformVersion() {
        DeviceConfig device = getIOSDevice();
        if (device != null) {
            return device.getPlatformVersion();
        }
        // Fallback to default iOS platform version from config
        return ConfigManager.get("iosPlatformVersion", "17.0");
    }

    /**
     * Release device allocation for the current thread.
     * Should be called in @After hook.
     */
    public static void releaseDevice() {
        DeviceConfig device = currentDevice.get();
        if (device != null) {
            logger.info("Thread {} released device: {}",
                    Thread.currentThread().getName(), device);
            currentDevice.remove();
        }

        DeviceConfig iosDevice = currentIOSDevice.get();
        if (iosDevice != null) {
            logger.info("Thread {} released iOS device: {}",
                    Thread.currentThread().getName(), iosDevice);
            currentIOSDevice.remove();
        }
    }

    /**
     * Reset device pool (for testing purposes).
     */
    public static synchronized void reset() {
        devicePool.clear();
        iosDevicePool.clear();
        deviceIndex.set(0);
        iosDeviceIndex.set(0);
        currentDevice.remove();
        currentIOSDevice.remove();
        initialized = false;
        logger.info("Device pools reset");
    }

    /**
     * Get total number of devices in the Android pool.
     * 
     * @return Number of Android devices
     */
    public static int getPoolSize() {
        initialize();
        return devicePool.size();
    }

    /**
     * Get total number of devices in the iOS pool.
     * 
     * @return Number of iOS devices
     */
    public static int getIOSPoolSize() {
        initialize();
        return iosDevicePool.size();
    }
}
