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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import d2d.testing.helpers.Logger;
import d2d.testing.net.handlers.StreamHandler;
import d2d.testing.net.packets.DataPacket;
import d2d.testing.net.packets.DataReceived;
import d2d.testing.net.threads.selectors.AbstractSelector;
import d2d.testing.streaming.ServerSession;
import d2d.testing.streaming.Session;
import d2d.testing.streaming.rtsp.RtspRequest;
import d2d.testing.streaming.rtsp.RtspResponse;
import d2d.testing.streaming.rtsp.UriParser;

public class RTSPServerWorker extends AbstractWorker {

    private static final String TAG = "RTSPServerWorker";
    // RTSP Server Name
    public static String SERVER_NAME = "D2D RTSP Server";

    // Parse method & uri
    public static final Pattern regexMethod = Pattern.compile("(\\w+) (\\S+) RTSP",Pattern.CASE_INSENSITIVE);
    // Parse a request header
    public static final Pattern rexegHeader = Pattern.compile("(\\S+):(.+)",Pattern.CASE_INSENSITIVE);

    protected HashMap<SelectableChannel, Session> mSessions = new HashMap<>();
    protected HashMap<SelectableChannel, ServerSession> mServerSessions = new HashMap<>();

    private StreamHandler mStream = null;

    /** Credentials for Basic Auth */
    private String mUsername;
    private String mPassword;

    @Override
    protected void processData(DataPacket dataPacket, AbstractSelector selector, SelectableChannel channel) {


    }

    public RtspResponse processRequest(RtspRequest request, SelectableChannel channel) throws IllegalStateException, IOException {
        Session requestSession = mSessions.get(channel);
        ServerSession serverSession = mServerSessions.get(channel);
        RtspResponse response = new RtspResponse(request);


        //Ask for authorization unless this is an OPTIONS request
        if(!isAuthorized(request) && !request.method.equalsIgnoreCase("OPTIONS"))
        {
            response.attributes = "WWW-Authenticate: Basic realm=\""+SERVER_NAME+"\"\r\n";
            response.status = RtspResponse.STATUS_UNAUTHORIZED;
        }
        else
        {
            switch (request.method) {
                case "OPTIONS":
                    response.status = RtspResponse.STATUS_OK;
                    response.attributes = "Public: DESCRIBE,SETUP,TEARDOWN,PLAY,PAUSE\r\n";
                    break;
                case "DESCRIBE":
                    return DESCRIBE(request, channel);
                case "SETUP":
                    return SETUP(request, requestSession);
                case "TEARDOWN":
                    return TEARDOWN();
                case "ANNOUNCE":
                    return ANNOUNCE(request, channel);
                case "REDIRECT":
                    return REDIRECT();
                case "PLAY":
                    return PLAY(requestSession, channel);
                case "RECORD":
                    return RECORD(serverSession, channel);
                case "PAUSE":
                    return PAUSE();
                default:
                    Logger.e("Command unknown: " + request);
                    response.status = RtspResponse.STATUS_BAD_REQUEST;
            }
        }
        return response;

    }

    /*
    todo record para sesiones que recibimos, es similar al PLAY
RECORD rtsp://192.169.6.151:1935/live/android_test RTSP/1.0
Range: npt=0.000-
CSeq: 4
Content-Length: 0
Session: 902878796
Authorization: Digest username="User1",realm="Streaming Server",nonce="9d9e1266ebfe1e5d8f48432af4b97669",uri="rtsp://192.169.6.151:1935/live/android_test",response="93271d1b62097f92d85b34f65cdd89af"

RTSP/1.0 200 OK
CSeq: 4
Server: Wowza Streaming Engine 4.4.0 build17748
Cache-Control: no-cache
Range: npt=now-
Session: 902878796;timeout=60
     */
    private RtspResponse RECORD(ServerSession serverSession, SelectableChannel channel) {
        RtspResponse response = new RtspResponse();
        return response;
    }

    private RtspResponse DESCRIBE(RtspRequest request, SelectableChannel channel) throws IOException {
        RtspResponse response = new RtspResponse();
        // Parse the requested URI and configure the session

        //todo hay que mirar en la uri si es /sessionID para REDIRIGIR LOS PUERTOS EN VEZ DE CREAR UN STREAM
        //todo creamos una session differente RebroadCastSession

        Session requestSession = handleRequest(request.uri, ((SocketChannel) channel).socket());
        mSessions.put(channel, requestSession);
        requestSession.syncConfigure();

        String requestContent = requestSession.getSessionDescription();
        String requestAttributes =
                "Content-Base: " + (((SocketChannel) channel).socket()).getLocalAddress().getHostAddress()
                        + ":" + ((SocketChannel) channel).socket().getLocalPort() + "/\r\n" +
                        "Content-Type: application/sdp\r\n";

        response.attributes = requestAttributes;
        response.content = requestContent;

        // If no exception has been thrown, we reply with OK
        response.status = RtspResponse.STATUS_OK;

        return response;
    }

    /*
    ANNOUNCE

    The ANNOUNCE method serves two purposes:
    When sent from client to server, ANNOUNCE posts the description of a presentation or media object identified by the request URL to a server. When sent from server to client, ANNOUNCE updates the session description in real-time. If a new media stream is added to a presentation (e.g., during a live presentation), the whole presentation description should be sent again, rather than just the additional components, so that components can be deleted.

    C->S: ANNOUNCE rtsp://example.com/media.mp4 RTSP/1.0
        CSeq: 7
        Date: 23 Jan 1997 15:35:06 GMT
        Session: 12345678
        Content-Type: application/sdp
        Content-Length: 332

        v=0
        o=mhandley 2890844526 2890845468 IN IP4 126.16.64.4
        s=SDP Seminar
                i=A Seminar on the session description protocol
        u=http://www.cs.ucl.ac.uk/staff/M.Handley/sdp.03.ps
        e=mjh@isi.edu (Mark Handley)
                c=IN IP4 224.2.17.12/127
        t=2873397496 2873404696
        a=recvonly
        m=audio 3456 RTP/AVP 0
        m=video 2232 RTP/AVP 31

    S->C: RTSP/1.0 200 OK
        CSeq: 7
*/
    private RtspResponse ANNOUNCE(RtspRequest request, SelectableChannel channel) throws IOException {
        RtspResponse response = new RtspResponse();
        // Parse the requested URI and configure the session
        //Session requestSession = handleRequest(request.uri, ((SocketChannel) channel).socket());
        //mSessions.put(channel, requestSession);

        //todo cambios en la session, es de modo recibir y no enviar...
        //ponerle el origen
        ServerSession requestSession = new ServerSession();

        // local IP + PORT
        String baseUrl = (((SocketChannel) channel).socket()).getLocalAddress().getHostAddress()
                + ":" + ((SocketChannel) channel).socket().getLocalPort();
        String requestAttributes = "Content-Base: " + baseUrl + "/\r\n" +
                                    "Content-Type: application/sdp\r\n" +
                                    "Session: " + requestSession.getSessionID() + ";timeout=60\r\n";

        response.attributes = requestAttributes;

        // If no exception has been thrown, we reply with OK
        response.status = RtspResponse.STATUS_OK;
        return response;
    }

    private RtspResponse SETUP(RtspRequest request, Session requestSession) throws IOException {
        RtspResponse response = new RtspResponse();
        Pattern p;
        Matcher m;
        int p2, p1, ssrc, trackId, src[];
        String destination;

        if (requestSession== null) {
            response.status = RtspResponse.STATUS_BAD_REQUEST;
            return response;
        }

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
                "Session: " + requestSession.getSessionID() + "\r\n" +
                "Cache-Control: no-cache\r\n";
        response.status = RtspResponse.STATUS_OK;

        // If no exception has been thrown, we reply with OK
        response.status = RtspResponse.STATUS_OK;

        return response;
    }

    /* todo setup para las sessiones de recibir
SETUP rtsp://192.169.6.151:1935/live/android_test/trackID=1 RTSP/1.0
Transport: RTP/AVP/UDP;unicast;client_port=5002-5003;mode=receive
CSeq: 3
Content-Length: 0
Session: 902878796
Authorization: Digest username="User1",realm="Streaming Server",nonce="9d9e1266ebfe1e5d8f48432af4b97669",uri="rtsp://192.169.6.151:1935/live/android_test",response="93271d1b62097f92d85b34f65cdd89af"

RTSP/1.0 200 OK
CSeq: 3
Server: Wowza Streaming Engine 4.4.0 build17748
Cache-Control: no-cache
Expires: Fri, 4 Mar 2016 11:31:22 IST
Transport: RTP/AVP/UDP;unicast;client_port=5002-5003;mode=receive;source=192.169.6.151;server_port=6974-6975
Date: Fri, 4 Mar 2016 11:31:22 IST
Session: 902878796;timeout=60
    * */
    private RtspResponse SETUP_SERVER(RtspRequest request, ServerSession requestSession) throws IOException {
        RtspResponse response = new RtspResponse();
        return response;
    }

    private RtspResponse PLAY(Session requestSession, SelectableChannel channel) {
        RtspResponse response = new RtspResponse();
        Socket requestSocket = ((SocketChannel) channel).socket();
        String url = requestSocket.getLocalAddress().getHostAddress() + ":" + requestSocket.getLocalPort();

        String requestAttributes = "RTP-Info: ";
        if (requestSession.trackExists(0))
            requestAttributes += "url=rtsp://" + url + "/trackID=" + 0 + ";seq=0,";
        if (requestSession.trackExists(1))
            requestAttributes += "url=rtsp://" + url + "/trackID=" + 1 + ";seq=0,";
        response.attributes = requestAttributes.substring(0, requestAttributes.length() - 1)
                            + "\r\nSession: " + requestSession.getSessionID() +"\r\n";

        // If no exception has been thrown, we reply with OK
        response.status = RtspResponse.STATUS_OK;

        return response;
    }

    /*
        REDIRECT

        A REDIRECT request informs the client that it must connect to another server location. It contains the mandatory header Location,
        which indicates that the client should issue requests for that URL. It may contain the parameter Range, which indicates when the redirection takes effect.
        If the client wants to continue to send or receive media for this URI, the client MUST issue a TEARDOWN request for the current session and a SETUP for the
        new session at the designated host.

        S->C: REDIRECT rtsp://example.com/media.mp4 RTSP/1.0
            CSeq: 11
            Location: rtsp://bigserver.com:8001
            Range: clock=19960213T143205Z-
    */
    private RtspResponse REDIRECT() {
        RtspResponse response = new RtspResponse();
        response.status = RtspResponse.STATUS_OK;
        return response;
    }

    private RtspResponse TEARDOWN() {
        RtspResponse response = new RtspResponse();
        response.status = RtspResponse.STATUS_OK;
        return response;
    }

    private RtspResponse PAUSE() {
        RtspResponse response = new RtspResponse();
        response.status = RtspResponse.STATUS_OK;
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
        Log.d(TAG,"handlerequest: Origin" + client.getLocalAddress().getHostAddress());
        session.setOrigin(client.getLocalAddress().getHostAddress());
        if (session.getDestination()==null) {
            Log.d(TAG,"handlerequest: Destination" + client.getInetAddress().getHostAddress());
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
