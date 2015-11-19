package org.Dentur.Toastcarsten.Util;

import java.util.ArrayList;

/**
 * Created by Sebastian V on 11/9/2015.
 */
public interface IToastcarstenCS {
    public void login(String name);
    public void logout();
    public ArrayList<String> userlist();
    public void send();

}
