package lucee.runtime.config;

import lucee.runtime.type.Struct;

public class AdminSyncNullObject implements AdminSync {

    @Override
    public void broadcast(Struct attributes, Config config) {
        // Do nothing.
    }
}