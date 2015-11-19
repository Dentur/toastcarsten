package org.Dentur.Toastcarsten.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Sebastian V on 11/16/2015.
 */
public class ChannelRW {
    static HashMap<SocketChannel, ArrayList<Byte>> mapBuffer;
    static ICommandInterpreter intr;

    public static void init(ICommandInterpreter interpreter)
    {
        mapBuffer = new HashMap<SocketChannel, ArrayList<Byte>>(255);
        intr = interpreter;
    }

    public static void addSocket(SocketChannel channel)
    {
        mapBuffer.put(channel, new ArrayList<Byte>());
    }

    public static void removeSocket(SocketChannel channel)
    {
        mapBuffer.remove(channel);
    }
    static public void sendTextMessage(SocketChannel sChannel, String s) throws IOException
    {
        if(s.charAt(s.length()-1)!= '\n')
            s+="\r\n";
        sChannel.write(ByteBuffer.wrap(s.getBytes("UTF-8")));
    }

    static public void recvTextMessage(SocketChannel sChannel) throws IOException
    {
        if(!mapBuffer.containsKey(sChannel))
        {
            addSocket(sChannel);
        }
        ByteBuffer recvBuffer = ByteBuffer.allocate(1024);
        int numBytesRead = sChannel.read(recvBuffer);
        switch (numBytesRead)
        {
            case(-1):
                throw new IOException(("Connection unexpextedly closed"));
            case 0:
                //return "";
            default:
                //if(recvBuffer.get(numBytesRead-1)!='\n')
                //    throw new IOException(("Messsage Frame Error"));
                boolean foundEnd =false;
                ArrayList<Byte> tmp = mapBuffer.get(sChannel);
                for(int index = 0; index < recvBuffer.array().length; index++)
                {
                    if(recvBuffer.array()[index]==0) break;
                    tmp.add(recvBuffer.array()[index]);

                }
                String currentBuffer;
                byte[] tmpBuff = new byte[tmp.size()];
                for(int index = 0; index < tmp.size(); index++)
                {
                    tmpBuff[index] = tmp.get(index);
                }
                currentBuffer = new String(tmpBuff, "UTF-8");
                if(currentBuffer.endsWith("\n"))
                {
                    intr.interpret(currentBuffer.trim(), sChannel);
                    mapBuffer.get(sChannel).clear();
                }
                //return new String(recvBuffer.array(), "UTF-8").trim();
        }
    }
}
