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

	public boolean isFaulty=false;

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
	private int decision;
	private boolean ready= false;
	private boolean proposalStarted = false;
	private boolean notificationStarted = false;

	private HashMap<Integer,ArrayList<Message>> notificationsQueue = new HashMap<Integer,ArrayList<Message>>();
	private HashMap<Integer,ArrayList<Message>> proposalsQueue = new HashMap<Integer,ArrayList<Message>>();
	private int round =0;
	private int v = (Math.random()<0.5)?0:1;

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
	public int getRound(){
		return round;
	}

	public int getV(){
		return v;
	}

	public void setV(int v){
		this.v = v;
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
			//System.out.println("polling...");
			createProcesses(addresses);
		}
		this.n = rp.length+1;
		this.f = (n-1)/5;
		synchronize();
	}

	private void synchronize() throws RemoteException{
		System.out.println("Process "+this.number+" Synchronizing...");
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

	synchronized public void receiveNotification(int round, UUID senderId, int v) throws RemoteException{
		randomDelay();
		Message notification = new Message(round,senderId,v);
		int notificationRound = notification.getRound();
		if(notificationRound < round) return;
		if(notificationStarted && notificationRound == round) return;
		if(notificationsQueue.get(notificationRound)==null){
			notificationsQueue.put(notificationRound,new ArrayList<Message>());
		}
		notificationsQueue.get(notificationRound).add(notification);
		if(notificationsQueue.get(round).size()>=(n-f)){
			ArrayList<Message> notificationsThisRound = notificationsQueue.remove(round);
			notificationStarted = true;
			notification(notificationsThisRound);
		}
	}

	synchronized public void receiveProposal(int round, UUID senderId, int v) throws RemoteException{
		randomDelay();
		if(decided&&!isFaulty){
			return;
		}
		Message proposal = new Message(round,senderId,v);
		int proposalRound = proposal.getRound();
		if(proposalRound < round) return;
		if(proposalStarted && proposalRound == round) return;
		if(proposalsQueue.get(proposalRound)==null){
			proposalsQueue.put(proposalRound,new ArrayList<Message>());
		}
		proposalsQueue.get(proposalRound).add(proposal);
		if(proposalsQueue.get(round).size()>=(n-f)){
			ArrayList<Message> proposalsThisRound = proposalsQueue.remove(round);
			proposalStarted = true;
			proposal(proposalsThisRound);
		}
	}

	synchronized public void notification(ArrayList<Message> notifications){
		randomDelay();
		int numberOf0s = 0;
		int numberOf1s = 0;

		for(Message notification: notifications){
			if(notification.getV() == 0) numberOf0s++;
			else if(notification.getV() == 1) numberOf1s++;
		}

		if(numberOf0s > ((n+f)/2)) broadcast("proposal", round, 0);
		else if(numberOf1s > ((n+f)/2)) broadcast("proposal", round, 1);
		else broadcast("proposal", round, -1);
		notificationStarted = false;
	}

	synchronized public void proposal(ArrayList<Message> proposals){
		randomDelay();
		int numberOf0s = 0;
		int numberOf1s = 0;

		if(decided){
			return;
		}

		for(Message proposal: proposals){
			if(proposal.getV() == 0) numberOf0s++;
			else if(proposal.getV() == 1) numberOf1s++;
		}

		if(numberOf0s > f) {
			v=0;
			if (numberOf0s > (3*f)) {
				decision=0;
				if(!isFaulty && !decided)
					System.out.println("Process Nr.:"+this.number+", decided on: "+0+", in Round: "+round);
				decided=true;
			}
		} else if(numberOf1s > f) {
			v=1;
			if (numberOf1s > (3*f)) {
				decision=1;
				if(!isFaulty && !decided)
					System.out.println("Process Nr.:"+this.number+", decided on: "+1+", in Round: "+round);
					decided=true;
			}
		} else v=(Math.random()<0.5)?0:1;
		round++;
		notificationsQueue.remove(round-1);
		proposalsQueue.remove(round-1);
		broadcast("notification", round, v);
		proposalStarted = false;
	}

	public void broadcast(String type, int round, int value){
		if(isFaulty){
			if(Math.random()<0.5) return;
			value = (Math.random()<0.5)?0:1;
		}
		Message message = new Message(round,this.id, value);
		if(type.toLowerCase().equals("notification")){
			for(DA_Process_RMI proc : rp){
				randomDelay();
				executor.submit (() ->{
					try{
						proc.receiveNotification(message.getRound(),message.getSenderId(),message.getV());
					}
					catch(RemoteException rme){
						rme.printStackTrace();
					}});
			}
			randomDelay();
			executor.submit (() ->{
				try{
					this.receiveNotification(message.getRound(),message.getSenderId(),message.getV());
				}
				catch(RemoteException rme){
					rme.printStackTrace();
				}});
		}
		if(type.toLowerCase().equals("proposal")){
			for(DA_Process_RMI proc : rp){
				randomDelay();
				executor.submit (() ->{
								try{
									proc.receiveProposal(message.getRound(),message.getSenderId(),message.getV());
								}
								catch(RemoteException rme){
								}});
			}
			randomDelay();
			executor.submit (() ->{
							try{
								this.receiveProposal(message.getRound(),message.getSenderId(),message.getV());
							}
							catch(RemoteException rme){
								rme.printStackTrace();
							}});
		}
	}

	public int randomDelay(){
		int min = 0;
		int max = 100;
		int time = java.util.concurrent.ThreadLocalRandom.current().nextInt(min, max + 1);
		try{
			Thread.sleep(time);
		}
		catch(InterruptedException ie){}
		return time;
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
