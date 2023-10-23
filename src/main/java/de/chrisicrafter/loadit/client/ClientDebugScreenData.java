package de.chrisicrafter.loadit.client;

import de.chrisicrafter.loadit.utils.BeaconData;

public class ClientDebugScreenData {
    private static BeaconData data;

    public static void set(BeaconData data) {
        ClientDebugScreenData.data = data;
    }

    public static BeaconData getBeaconData() {
        return data;
    }
}
