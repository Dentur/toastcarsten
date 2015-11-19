package org.Dentur.Toastcarsten.Server;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import org.Dentur.Toastcarsten.Util.ChannelRW;
import org.Dentur.Toastcarsten.Util.ICommandInterpreter;
import org.Dentur.Toastcarsten.Util.eChatErrors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by Sebastian V on 11/9/2015.
 */
public class Server implements ICommandInterpreter{
    Selector events = null;
    ServerSocketChannel listenChannel;
    HashMap<String, String> userMap;


    public Server(Integer port) {
        userMap = new HashMap<String, String>(250);
        try {
            events = Selector.open();

            listenChannel = ServerSocketChannel.open();
            listenChannel.configureBlocking(false);
            listenChannel.socket().bind(
                    new InetSocketAddress(port));
            listenChannel.register(events, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ChannelRW.init(this);
    }

    public Server() {
        this(81);
    }

    private void processRead(SelectionKey key) {
        SocketChannel talkChannel = null;
        try {
            talkChannel = (SocketChannel) key.channel();
            //talkChannel.socket().getRemoteSocketAddress().toString();
            ChannelRW.recvTextMessage(talkChannel);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            try {
                talkChannel.close();
            } catch (IOException ignore) {
            }
        }
    }

    private boolean SocketLoggedIn(SocketChannel ch) {
        System.out.println("Checkin if logged in for: " + ch.socket().getRemoteSocketAddress().toString());
        return userMap.containsKey(ch.socket().getRemoteSocketAddress().toString());
    }

    private boolean SocketCheckRights(SocketChannel ch) throws IOException {
        System.out.println("Checkin rights for: " + ch.socket().getRemoteSocketAddress().toString());
        if (!SocketLoggedIn(ch)) {
            ChannelRW.sendTextMessage(ch, eChatErrors.CommandNotAllowed.name());
            return false;
        }
        return true;
    }

    private void SendToAllOthers(SocketChannel current, String message) {
        for (SelectionKey key : events.keys()) {
            if (key.channel() instanceof SocketChannel) {
                SocketChannel ch = (SocketChannel) key.channel();
                if(SocketLoggedIn(ch)) {
                    if (!(ch.socket().getRemoteSocketAddress().toString().equals(current.socket().getRemoteSocketAddress().toString()))) {
                        try {
                            ChannelRW.sendTextMessage(ch, message);
                        } catch (IOException e) {
                            //TODO handle exeptions
                        }
                    }
                }
            }
        }
    }

    public void interpret(String text, SocketChannel current) {
        text.trim();
        System.out.println("Processing Command: " + text + " by: " + current.socket().getRemoteSocketAddress().toString());
        String command = "";
        if (text.contains(" "))
            command = text.substring(0, text.indexOf(" "));
        else
         command  = text;
        try {
            switch (command) {
                case "/login":
                    String name = text.substring(text.indexOf(" "));
                    if (userMap.containsValue(name)) {
                        ChannelRW.sendTextMessage(current, eChatErrors.NameAlreadyInUse.name());
                        return;
                    }
                    if (userMap.containsKey(current.socket().toString()))
                    {
                        ChannelRW.sendTextMessage(current, eChatErrors.CommandNotAllowed.name());
                        return;
                    }
                    userMap.put(current.socket().getRemoteSocketAddress().toString(), text.substring(text.indexOf(" ")+1));
                    SendToAllOthers(current, "/userjoined "+text.substring(text.indexOf(" ")+1) );

                    ChannelRW.sendTextMessage(current, "/welcome Dies ist eine satanische willkommens nachricht");
                case "/userlist":
                    if (!SocketCheckRights(current)) return;
                    String ans = "/userlist ";
                    int size = events.keys().size();
                    int index = 0;
                    for (SelectionKey key : events.keys())
                    {
                        if (key.channel() instanceof SocketChannel)
                        {
                            SocketChannel ch = (SocketChannel) key.channel();
                            if(SocketLoggedIn(ch))
                            {
                                ans += userMap.get(ch.socket().getRemoteSocketAddress().toString());
                                if (index != (size - 2)) {
                                    ans += ", ";
                                }
                            }
                            index++;

                        }
                    }
                    ChannelRW.sendTextMessage(current, ans);
                    break;

                case "/logout":
                    if (!SocketCheckRights(current)) return;
                    SendToAllOthers(current, "/userleft " + userMap.get(current.socket().getRemoteSocketAddress().toString()) + " logout");
                    userMap.remove(current.socket().getRemoteSocketAddress().toString());
                    //ChannelRW.removeSocket(current);
                    break;
                default:
                    if (!SocketCheckRights(current)) return;
                    SendToAllOthers(current, userMap.get(current.socket().getRemoteSocketAddress().toString()) + ": " + text);
            }
        } catch (IOException e) {
            System.out.println("error in command procession");
            //TODO: handle exeption
        }
    }

    private void processAccept() {
        System.out.println("process Accept");
        SocketChannel talkChannel = null;
        try {
            talkChannel = listenChannel.accept();
            talkChannel.configureBlocking(false);
            talkChannel.register(events, SelectionKey.OP_READ);
            ChannelRW.addSocket(talkChannel);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            try {
                talkChannel.close();
            } catch (IOException ignore) {
            }
        }
    }

    public void run() throws IOException {
        Iterator<SelectionKey> selKeys;
        while (true) {
            events.select();
            selKeys = events.selectedKeys().iterator();
            while (selKeys.hasNext()) {
                SelectionKey selKey = selKeys.next();
                selKeys.remove();
                if (selKey.isReadable()) {
                    processRead(selKey);
                } else if (selKey.isAcceptable()) {
                    processAccept();
                } else {
                    System.out.println("Unknown event occurred");
                }
            }
        }
    }
}
