package com.pokeskies.skiesskins.utils;

import ca.landonjw.gooeylibs2.api.data.UpdateEmitter;
import ca.landonjw.gooeylibs2.api.page.Page;

public abstract class RefreshableGUI extends UpdateEmitter<Page> implements Page {
    public abstract void refresh();
}
