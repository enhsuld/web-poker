package com.tandilserver.poker_room.registerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tandilserver.poker_intercom.customTypes.LimitTypes;
import com.tandilserver.poker_intercom.customTypes.ServerTypes;
import com.tandilserver.poker_intercom.handshake.AuthorizationBearer;
import com.tandilserver.poker_intercom.handshake.ServerInfo;

@Component
@PropertySources(value = {@PropertySource("classpath:roomRegisterService.properties")})
public class RegisterServiceThread  implements Runnable, ApplicationListener<ContextRefreshedEvent>{
	
	public static Logger logger = LoggerFactory.getLogger(RegisterServiceThread.class);
	private Thread thread;
	
	@Value("${room.register.listenPort}")
    private volatile int remoteListenerPort;
	@Value("${room.register.updateMilis}")
    private volatile int updateMiliseconds;
	@Value("${room.register.name}")
    private volatile String roomName;
	@Value("${room.register.bigBlind}")
    private volatile int bigBlind;
	@Value("${room.register.minBet}")
    private volatile int minBet;
	@Value("${room.register.serverIdentityHash}")
	private volatile String serverIdentityHash;
	
	
	@Autowired
	private ServerDataBlock srvDataBlock;
	
	BufferedReader socketBufferReader;
	PrintWriter socketBufferOutput;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// TODO Auto-generated method stub
		if(this.thread == null) {
			this.thread = new Thread(this);
			this.thread.start();
			logger.debug("RegisterService Thread created.");
		}
	}

	@Override
	public void run() {
		InetAddress address = null;
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException ukHostException) {
			// TODO Auto-generated catch block
			logger.error("Unknown Host Exception", ukHostException);
			return ;
		}
        Socket s1=null;
        BufferedReader is=null;
        try {
            s1=new Socket(address, this.remoteListenerPort); // You can use static final constant PORT_NUM
            socketBufferReader = new BufferedReader(new InputStreamReader(System.in));
            is=new BufferedReader(new InputStreamReader(s1.getInputStream()));
            socketBufferOutput = new PrintWriter(s1.getOutputStream());
        }
        catch (IOException e){
            e.printStackTrace();
            System.err.print("IO Exception");
        }
        
        String response=null;
        try{
        	logger.info("RoomClient address : " + address);
        	logger.debug("Configurating server...");
        	logger.info("[*] Server name: " + roomName);
        	logger.info("[*] Server Type: Sit&Go");
	        logger.info("[*] Big Blind: " + bigBlind);
	        logger.info("[*] Short Blind: " + bigBlind/2);
	        logger.info("[*] Minimum Bet: " + minBet);
	        logger.info("[*] Limit bet: NO LIMIT");
	        logger.debug("[>] Loggin room: ");
	        ServerInfo srvInfo = new ServerInfo();
	        srvInfo.ip = address.getHostAddress();
	        srvInfo.server_type = ServerTypes.SIT_N_GO;
	        srvInfo.server_name = roomName;
	        srvInfo.blind = bigBlind;
	        srvInfo.min_bet = minBet;
	        srvInfo.limit_bet = LimitTypes.NO_LIMIT;
	        srvInfo.players = 0;
	        srvInfo.port = this.remoteListenerPort;
	        ObjectMapper oM = new ObjectMapper();
	        srvDataBlock.setSrvInfo(srvInfo);
	        socketBufferOutput.println(oM.writeValueAsString(srvInfo));
	        socketBufferOutput.flush();
	        logger.debug("[!] Waiting for response...");
	        response = is.readLine();
	        logger.debug("[<] Response: " + response);
	        AuthorizationBearer auth = oM.readValue(response, AuthorizationBearer.class);
	        if (auth.valid) {
	        	authorized(auth);
	        } else {
	        	logger.debug("[!] Authorization required, plesae write the last Server Identity Hash:");
	        	auth.server_identity_hash = serverIdentityHash;
	        	logger.info("[>] Sending Server Identity Hash "+serverIdentityHash+"...");
	        	socketBufferOutput.println(oM.writeValueAsString(auth));
	        	socketBufferOutput.flush();
	        	logger.debug("[!] Waiting for response...");
	        	response = is.readLine();
	        	logger.debug("[<] Response: " + response);
	        	auth = oM.readValue(response, AuthorizationBearer.class);
	        	if(auth.valid) {
	        		authorized(auth);
	        	} else {
	        		logger.warn("[!] Authorization rejected.");
	        	}
	        }
        } catch(IOException e){
            e.printStackTrace();
            logger.error("Socket read Error", e);
        } finally{
            try {
				is.close();
				socketBufferOutput.close();
				socketBufferReader.close();
	            s1.close();
	            logger.error("Connection Closed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	public void authorized(AuthorizationBearer auth) throws IOException {
    	logger.debug("[*] Access Granted");
    	logger.info("[*] New Server Identity Hash: "+auth.server_identity_hash);
    	logger.debug("!!! Please save this identity for next access");
    	try {
    		ObjectMapper oM = new ObjectMapper();
    		while(true) {
    			Thread.sleep(updateMiliseconds);
    			// send updated information
    			logger.debug("Sending updated information.");
    			socketBufferOutput.println(oM.writeValueAsString(srvDataBlock.getSrvInfo()));
    			socketBufferOutput.flush();
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Thread RegisterService Interrumped, ",e);
		}
    }

}
