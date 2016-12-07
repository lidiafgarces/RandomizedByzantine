import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface DA_Process_RMI extends Remote{

		public void receiveNotification(int round, UUID senderId, int v) throws RemoteException;
		public void receiveProposal(int round, UUID senderId, int v) throws RemoteException;
  public boolean isReady() throws RemoteException;
}
