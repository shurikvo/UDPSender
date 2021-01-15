public class UDPSenderMain {
    public static void main(String[] args){
        int RC;

        if(args.length < 3) {
            System.err.println("Call me: UDPSender <address> <port> <file>");
            System.exit(0);
        }

        UDPSender sndr = new UDPSender(args[0], args[1], args[2]);
        RC = sndr.send();
        if(RC < 0)
            System.out.println("ERROR");
        else
            System.out.println("OK");

    }
}
