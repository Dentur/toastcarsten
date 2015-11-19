package org.Dentur.Toastcarsten.Util;

import java.nio.channels.SocketChannel;

/**
 * Created by Sebastian V on 11/18/2015.
 */
public interface ICommandInterpreter {

    public void interpret(String command, SocketChannel sChannel);
}
