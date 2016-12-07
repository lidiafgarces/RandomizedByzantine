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
	public static final int FACTOR = 1;
	private int[] vectorClock = new int[3];
	private static final String NAMING = "proc";
	private boolean decided = false;
	private boolean ready= false;

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
