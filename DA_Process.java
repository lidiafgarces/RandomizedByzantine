import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DA_Process extends UnicastRemoteObject implements DA_Process_RMI{

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private final UUID id = UUID.randomUUID();

	private static final long serialVersionUID = 6384248030531941625L;
	private Registry registry;
	public int number;
	private String name;
	private DA_Process_RMI[] rp;
	private int n =0;
	private int f =0;
	public static final int FACTOR = 1;
	private int[] vectorClock = new int[3];
	private static final String NAMING = "proc";
	private boolean decided = false;
	private boolean ready= false;

	private HashMap<int,ArrayList<Message>> notificationsQueue = new HashMap<int,ArrayList<Message>>();
	private HashMap<int,ArrayList<Message>> proposalsQueue = new HashMap<int,ArrayList<Message>>();
	private int round =0;

	protected DA_Process(int n) throws RemoteException{
		super();
		this.number = n;
	}

	public void setRegistry(Registry registry){
		this.registry = registry;
	}

	public void setName(String name){
		this.name = name;
	}

	public int getProcessNumber() throws RemoteException{
		return number;
	}

	public void createProcesses(ArrayList<String> addresses) throws RemoteException{
		try {
			rp = new DA_Process_RMI[addresses.size()];
			for(int i=0; i<addresses.size();i++){
				rp[i]=(DA_Process_RMI)Naming.lookup(addresses.get(i));
			}
		} catch (MalformedURLException mue){
			System.out.println("Your URL is malformed!");
		} catch (Exception e) {
			long time = System.currentTimeMillis();
			while(System.currentTimeMillis()-time <5000){}
			System.out.println("polling...");
			createProcesses(addresses);
		}
		this.n = rp.length;
		this.f = (n-1)/5;
		synchronize();
	}

	private void synchronize() throws RemoteException{
		System.out.println("Synchronizing...");
		ready = true;
		for(DA_Process_RMI process: rp){
			while(!process.isReady()){
				try{
					Thread.sleep(1000);
				}
				catch(Exception e){

				}
			}
		}
	}

	public void receiveNotification(Message notification){
		int notificationRound = notification.getRound();
		if(notificationRound > round) return;
		if(notificationsQueue.get(notificationRound)==null){
			notificationsQueue.put(notificationsQueue,new ArrayList<Message>);
		}
		notificationsQueue.get(notificationRound).add(notification);
		if(notificationsQueue.get(round).size()>=(n-f)){
			ArrayList<Message> notificationsThisRound = notificationsQueue.get(round);
			notification(notificationsThisRound);
		}
	}

	public void receiveProposal(Message proposal){
			int proposalRound = proposal.getRound();
			if(proposalRound > round) return;
			if(proposalsQueue.get(proposalRound)==null){
				proposalsQueue.put(proposalsQueue,new ArrayList<Message>);
			}
			proposalsQueue.get(proposalRound).add(proposal);
			if(proposalsQueue.get(round).size()>=(n-f)){
				ArrayList<Message> proposalsThisRound = proposalsQueue.get(round);
				proposal(proposalsThisRound);
			}
		}
	
	public void notification(){
		int numberOf0s = 0;
		int numberOf1s = 0;
		
		for(Notification[] notification: notification){
			if(notification.v == 0) numberOf0s++;
			else if(notification.v == 1) numberOf1s++; 
		}
		
		if(numberOf0s > ((n+f)/2)) broadcast('r',1);
		else if(numberOf1s > ((n+f)/2)) broadcast('r', 0);
		else broadcast('r', -1);
	}
	
	public void proposal(){
		int numberOf0s = 0;
		int numberOf1s = 0;
		
		for(Proposal[] proposal: proposals){
			if(proposal.v == 0) numberOf0s++;
			else if(proposal.v == 1) numberOf1s++; 
		}
		
		if(numberOf0s > ((n+f)/2)) {
			v=0;
			if (numberOf0s > (3*f)) {
				decision=0;
				decided=true;
			}
		} else if(numberOf1s > ((n+f)/2)) {
			v=1;
			if (numberOf1s > (3*f)) {
				decision=1;
				decided=true;
			}
		} else v=(Math.random()<0.5)?0:1;
		
		round++;
		notifications.remove(round-1);
		proposals.remove(round-1);
		broadcast('p', v);
	}
	
	public void broadcast(String type, int value){
		
	}

	public boolean isReady() throws RemoteException{
		return ready;
	}


	private long idToLong(UUID id){
		if(id == null) return Long.MIN_VALUE;
		return id.getLeastSignificantBits();
	}

	private void setVectorClock(int [] newVector) {
		this.vectorClock = newVector;
	}

	private int[] getVectorClock() {
		return vectorClock;
	}

}
