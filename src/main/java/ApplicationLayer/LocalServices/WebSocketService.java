package ApplicationLayer.LocalServices;


//import com.sun.org.apache.xpath.internal.operations.String;
import ApplicationLayer.AppComponents.AppComponent;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.json.simple.JSONObject;
import java.util.HashMap;


/**
 * Se encarga de enviar la información de los componentes al servidor web para su visualización.
 * Para ello se basa en el sistema de eventos + JSONs de Websockets.
 * Debe haber un listener para cada AppComponent, con nombre ID del AppComponent.
 */
public class WebSocketService extends Service{
    SocketIOServer server; // Actuamos como servidor, Cliente es la App en Vuejs
    Configuration config;

    HashMap<String, JSONObject> map;

    /**
     * Constructor con valores default de parámetros
     */
    public WebSocketService(){
        this(3000, "localhost");
    }

    /**
     * Constructor con parámetros no default
     * @param PORT Puerto del hostname
     * @param HOSTNAME Host a donde enviar eventos
     */
    public WebSocketService(int PORT, String HOSTNAME) {
        super();
        this.config = new Configuration();
        this.config.setHostname(HOSTNAME);
        this.config.setPort(PORT);
        this.server = new SocketIOServer(config);
        this.server.start();
        this.map = new HashMap<>();
    }

    /**
     * WebScoketService manda eventos del estilo ("BMS", {"name": "BMS", "data": [ ... ]})
     * Al host donde se deberán escuchar los eventos de nombre de cada componente
     * @param c AppComponent a consumir
     */
    @Override
    protected void serve(AppComponent c) {
        try {
            if (map.containsKey(c.getID())) {
                //map.put(c.getID(), (JSONObject) map.get(c.getID()).put("data", c.getValoresRealesActuales()));
                //server.getBroadcastOperations().sendEvent(c.getID(), map.get(c.getID())); // Enviar evento a WebSocket del componente específico
                //System.out.println("Bradcast de: " + c.getID()+".");
                JSONObject obj = new JSONObject();
                map.put(c.getID(), obj);
                for(int i = 0; i < c.getValoresRealesActuales().length; i++) {
                    obj.put(c.nombreParametros[i], c.valoresRealesActuales[i]);
                }
                JSONObject g_obj = new JSONObject();
                g_obj.put("data", obj);
                map.put(c.getID(), g_obj);
                // System.out.println(c.getID()+" broadcast");
                server.getBroadcastOperations().sendEvent(c.getID(), c.valoresRealesActuales); 

            }else{
                JSONObject obj = new JSONObject();
                map.put(c.getID(), obj);
                for(int i = 0; i < c.getValoresRealesActuales().length; i++) {
                    obj.put(c.nombreParametros[i], c.valoresRealesActuales[i]);
                }
                JSONObject g_obj = new JSONObject();
                g_obj.put("data", obj);
                map.put(c.getID(), g_obj);
                server.getBroadcastOperations().sendEvent(c.getID(), c.valoresRealesActuales); // Enviar evento a WebSocket del componente específico
                //System.out.println("Bradcast de: " + c.getID());
            }
            Thread.sleep(10);
        }catch (Exception e){
            e.printStackTrace(); // Sólo se hace print, el sistema no se puede caer
        }
    }
}
