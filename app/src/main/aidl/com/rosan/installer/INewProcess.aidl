package com.rosan.installer;

import com.rosan.installer.IPrivilegedService;

interface INewProcess {
    void quit();

    IPrivilegedService getPrivilegedService();
}
