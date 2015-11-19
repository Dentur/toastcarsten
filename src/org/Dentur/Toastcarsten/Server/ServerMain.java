package org.Dentur.Toastcarsten.Server;

import java.io.IOException;
import java.rmi.server.ExportException;

/**
 * Created by Sebastian V on 11/9/2015.
 */
public class ServerMain {public static void main(String[] args) {
    // write your code here
    System.out.println("Starting server...");
    Server s = new Server(4711);
    try {
        System.out.println("Entering Serer loop");
        s.run();
    }
    catch(IOException e)
    {
        System.out.println(e.getMessage());
    }
}

}
