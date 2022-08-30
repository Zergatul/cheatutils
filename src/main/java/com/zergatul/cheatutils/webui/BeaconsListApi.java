package com.zergatul.cheatutils.webui;

public class BeaconsListApi extends ApiBase {

    @Override
    public String getRoute() {
        return "beacons-list";
    }

    /*@Override
    public String get() throws HttpException {
        return gson.toJson(ConfigStore.instance.getConfig().beaconConfig.entries);
    }

    @Override
    public String post(String body) throws HttpException {
        BeaconConfig.BeaconEntry entry = gson.fromJson(body, BeaconConfig.BeaconEntry.class);
        if (entry.name == null) {
            throw new HttpException("Name cannot be null.");
        }

        BeaconConfig.BeaconEntry other = ConfigStore.instance.getConfig().beaconConfig.entries.stream().filter(e -> entry.name.equals(e.name)).findFirst().orElse(null);
        if (other != null) {
            throw new HttpException("Entry with the same name already exists.");
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            throw new HttpException("You have to be logged in, so we set correct dimension for beacon.");
        }

        entry.dimension = mc.level.dimension().location().toString();
        ConfigStore.instance.getConfig().beaconConfig.entries.add(entry);
        ConfigStore.instance.requestWrite();
        return gson.toJson(entry);
    }

    @Override
    public String delete(String id) throws HttpException {
        if (ConfigStore.instance.getConfig().beaconConfig.entries.removeIf(e -> id.equals(e.name))) {
            ConfigStore.instance.requestWrite();
        }
        return "true";
    }*/
}