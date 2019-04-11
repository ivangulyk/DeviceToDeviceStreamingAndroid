package d2d.testing.net.threads.workers;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Locale;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import d2d.testing.helpers.Logger;
import d2d.testing.net.handlers.StreamHandler;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.threads.selectors.AbstractSelector;
import d2d.testing.streaming.Session;
import d2d.testing.streaming.rtsp.RtspRequest;
import d2d.testing.streaming.rtsp.RtspResponse;
import d2d.testing.streaming.rtsp.UriParser;

public class RTSPServerWorker extends AbstractWorker {

    // RTSP Server Name
    public static String SERVER_NAME = "D2D RTSP Server";

    // Parse method & uri
    public static final Pattern regexMethod = Pattern.compile("(\\w+) (\\S+) RTSP",Pattern.CASE_INSENSITIVE);
    // Parse a request header
    public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)",Pattern.CASE_INSENSITIVE);

    protected HashMap<SelectableChannel, Session> mSessions = new HashMap<>();

    private StreamHandler mStream = null;

    /** Credentials for Basic Auth */
    private String mUsername;
    private String mPassword;

    @Override
    protected void processData(DataPacket dataPacket, AbstractSelector selector, SelectableChannel channel) {


    }

    public RtspResponse processRequest(RtspRequest request, SelectableChannel channel) throws IllegalStateException, IOException {
        Session requestSession = mSessions.get(channel);
        RtspResponse response = new RtspResponse(request);

        Socket requestSocket = ((SocketChannel) channel).socket();


        //Ask for authorization unless this is an OPTIONS request
        if(!isAuthorized(request) && !request.method.equalsIgnoreCase("OPTIONS"))
        {
            response.attributes = "WWW-Authenticate: Basic realm=\""+SERVER_NAME+"\"\r\n";
            response.status = RtspResponse.STATUS_UNAUTHORIZED;
        }
        else
        {
            /* ********************************************************************************** */
            /* ********************************* Method DESCRIBE ******************************** */
            /* ********************************************************************************** */
            if (request.method.equalsIgnoreCase("DESCRIBE")) {

                // Parse the requested URI and configure the session
                requestSession = handleRequest(request.uri, requestSocket);
                mSessions.put(channel, requestSession);
                requestSession.syncConfigure();

                String requestContent = requestSession.getSessionDescription();
                String requestAttributes =
                        "Content-Base: " + requestSocket.getLocalAddress().getHostAddress() + ":" + requestSocket.getLocalPort() + "/\r\n" +
                                "Content-Type: application/sdp\r\n";

                response.attributes = requestAttributes;
                response.content = requestContent;

                // If no exception has been thrown, we reply with OK
                response.status = RtspResponse.STATUS_OK;

            }

            /* ********************************************************************************** */
            /* ********************************* Method OPTIONS ********************************* */
            /* ********************************************************************************** */
            else if (request.method.equalsIgnoreCase("OPTIONS")) {
                response.status = RtspResponse.STATUS_OK;
                response.attributes = "Public: DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE\r\n";
                response.status = RtspResponse.STATUS_OK;
            }

            /* ********************************************************************************** */
            /* ********************************** Method SETUP ********************************** */
            /* ********************************************************************************** */
            else if (request.method.equalsIgnoreCase("SETUP")) {
                Pattern p;
                Matcher m;
                int p2, p1, ssrc, trackId, src[];
                String destination;

                p = Pattern.compile("trackID=(\\w+)", Pattern.CASE_INSENSITIVE);
                m = p.matcher(request.uri);

                if (!m.find()) {
                    response.status = RtspResponse.STATUS_BAD_REQUEST;
                    return response;
                }

                trackId = Integer.parseInt(m.group(1));

                if (!requestSession.trackExists(trackId)) {
                    response.status = RtspResponse.STATUS_NOT_FOUND;
                    return response;
                }

                p = Pattern.compile("client_port=(\\d+)(?:-(\\d+))?", Pattern.CASE_INSENSITIVE);
                m = p.matcher(request.headers.get("transport"));

                if (!m.find()) {
                    int[] ports = requestSession.getTrack(trackId).getDestinationPorts();
                    p1 = ports[0];
                    p2 = ports[1];
                } else {
                    p1 = Integer.parseInt(m.group(1));
                    if (m.group(2) == null) {
                        p2 = p1+1;
                    } else {
                        p2 = Integer.parseInt(m.group(2));
                    }
                }

                ssrc = requestSession.getTrack(trackId).getSSRC();
                src = requestSession.getTrack(trackId).getLocalPorts();
                destination = requestSession.getDestination();

                requestSession.getTrack(trackId).setDestinationPorts(p1, p2);

                requestSession.syncStart(trackId);

                response.attributes = "Transport: RTP/AVP/UDP;" + (InetAddress.getByName(destination).isMulticastAddress() ? "multicast" : "unicast") +
                        ";destination=" + requestSession.getDestination() +
                        ";client_port=" + p1 + "-" + p2 +
                        ";server_port=" + src[0] + "-" + src[1] +
                        ";ssrc=" + Integer.toHexString(ssrc) +
                        ";mode=play\r\n" +
                        "Session: " + "1185d20035702ca" + "\r\n" +
                        "Cache-Control: no-cache\r\n";
                response.status = RtspResponse.STATUS_OK;

                // If no exception has been thrown, we reply with OK
                response.status = RtspResponse.STATUS_OK;

            }

            /* ********************************************************************************** */
            /* ********************************** Method PLAY *********************************** */
            /* ********************************************************************************** */
            else if (request.method.equalsIgnoreCase("PLAY")) {
                String requestAttributes = "RTP-Info: ";
                if (requestSession.trackExists(0))
                    requestAttributes += "url=rtsp://" + requestSocket.getLocalAddress().getHostAddress() + ":" + requestSocket.getLocalPort() + "/trackID=" + 0 + ";seq=0,";
                if (requestSession.trackExists(1))
                    requestAttributes += "url=rtsp://" + requestSocket.getLocalAddress().getHostAddress() + ":" + requestSocket.getLocalPort() + "/trackID=" + 1 + ";seq=0,";
                requestAttributes = requestAttributes.substring(0, requestAttributes.length() - 1) + "\r\nSession: 1185d20035702ca\r\n";

                response.attributes = requestAttributes;

                // If no exception has been thrown, we reply with OK
                response.status = RtspResponse.STATUS_OK;

            }

            /* ********************************************************************************** */
            /* ********************************** Method PAUSE ********************************** */
            /* ********************************************************************************** */
            else if (request.method.equalsIgnoreCase("PAUSE")) {
                response.status = RtspResponse.STATUS_OK;
            }

            /* ********************************************************************************** */
            /* ********************************* Method TEARDOWN ******************************** */
            /* ********************************************************************************** */
            else if (request.method.equalsIgnoreCase("TEARDOWN")) {
                response.status = RtspResponse.STATUS_OK;
            }

            /* ********************************************************************************** */
            /* ********************************* Unknown method ? ******************************* */
            /* ********************************************************************************** */
            else {
                Logger.e("Command unknown: " + request);
                response.status = RtspResponse.STATUS_BAD_REQUEST;
            }
        }
        return response;

    }

    @Override
    protected void parsePackets(DataReceived dataReceived) {
        //TODO POSIBILIDAD DE QUE LOS PAQUETES SE QUEDEN ABIERTOS...!!! HAY QUE CONTROLAR
        RtspResponse response = new RtspResponse();
        RtspRequest request = new RtspRequest();
        String line = null;
        Matcher matcher;

        BufferedReader inputReader = new BufferedReader(new StringReader(new String(dataReceived.getData())));
        // Parsing request method & uri

        try {
            if ((line = inputReader.readLine())==null) {
                //todo para nosotros no es desconectado... simplemente no hay una linea completa?
                throw new SocketException("Client disconnected");
            }

            matcher = regexMethod.matcher(line);
            matcher.find();
            request.method = matcher.group(1);
            request.uri = matcher.group(2);

            // Parsing headers of the request
            while ( (line = inputReader.readLine()) != null && line.length()>3 ) {
                matcher = rexegHeader.matcher(line);
                matcher.find();
                request.headers.put(matcher.group(1).toLowerCase(Locale.US),matcher.group(2));
            }

            /*if (line==null) {
                //todo para nosotros no es desconectado... simplemente no hay una linea completa?
                throw new SocketException("Client disconnected");
            }*/

            // It's not an error, it's just easier to follow what's happening in logcat with the request in red
            Logger.e(request.method+" "+request.uri);
            inputReader.close();

            response = processRequest(request, dataReceived.getSocket());

        } catch (IOException e) {
            response.status = RtspResponse.STATUS_BAD_REQUEST;
            e.printStackTrace();
        } catch (IllegalStateException e) {
            response.status = RtspResponse.STATUS_BAD_REQUEST;
            Logger.e("illegal state with line" + line.toString());
            e.printStackTrace();
        }

        try {
            dataReceived.getSelector().send((SocketChannel) dataReceived.getSocket(), response.build().getBytes());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Check if the request is authorized
     * @param request
     * @return true or false
     */
    private boolean isAuthorized(RtspRequest request) {
        String auth = request.headers.get("authorization");
        if(mUsername == null || mPassword == null || mUsername.isEmpty())
            return true;

        if(auth != null && !auth.isEmpty())
        {
            String received = auth.substring(auth.lastIndexOf(" ")+1);
            String local = mUsername+":"+mPassword;
            String localEncoded = Base64.encodeToString(local.getBytes(),Base64.NO_WRAP);
            if(localEncoded.equals(received))
                return true;
        }

        return false;
    }

    /**
     * By default the RTSP uses {@link UriParser} to parse the URI requested by the client
     * but you can change that behavior by override this method.
     * @param uri The uri that the client has requested
     * @param client The socket associated to the client
     * @return A proper session
     */
    protected Session handleRequest(String uri, Socket client) throws IllegalStateException, IOException {
        Session session = UriParser.parse(uri);
        Logger.d("handlerequest: socket addr" + client.getInetAddress().getHostAddress());
        session.setOrigin(client.getInetAddress().getHostAddress());
        if (session.getDestination()==null) {
            session.setDestination(client.getInetAddress().getHostAddress());
        }
        return session;
    }

    public void onClientDisconnected(SocketChannel socketChannel) {
        //boolean streaming = isStreaming();
        Session requestSession = mSessions.get(socketChannel);

        if(requestSession != null) {
            if (requestSession.isStreaming()) {
                requestSession.syncStop();
            }

            requestSession.release();
        }
    }
}
