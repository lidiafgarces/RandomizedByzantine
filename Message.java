import java.util.UUID;

public class Message{

  private int round;
  private UUID senderId;
  private int v;

  public Message(int round, UUID senderId, int v){
    this.round = round;
    this.senderId = senderId;
    this.v = v;
  }

  public int getRound(){
    return this.round;
  }

  public UUID getSenderId(){
    return this.senderId;
  }

  public int getV(){
    return this.v;
  }
}
