package com.rosan.installer;

import android.content.ComponentName;

interface IPrivilegedService {
    void delete(in String[] paths);

    void setDefaultInstaller(in ComponentName component, boolean enable);
}
