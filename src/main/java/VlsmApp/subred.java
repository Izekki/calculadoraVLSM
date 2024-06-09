// subred.java
package VlsmApp;

public class subred {

    private int ipRed;
    private int hosts;
    private int broadcast;
    private int mascara;
    private int nmasc;

    public subred(int ip, int mascara) {
        this.ipRed = ip;
        this.nmasc = mascara;
        this.mascara = calcularMascara(mascara);
        hosts = (int) Math.pow(2, (32 - mascara)) - 2;
        broadcast = ip + hosts + 1;
    }

    private int calcularMascara(int nbits) {
        int mascara = 0;
        for (int j = 0; j < nbits; j++) {
            mascara += 1 << 31 - j;
        }
        return mascara;
    }

    public int getBroadcast() {
        return broadcast;
    }

    public int getHosts() {
        return hosts;
    }

    public int getIpRed() {
        return ipRed;
    }

    public int getMascara() {
        return mascara;
    }

    public int getNmasc() {
        return this.nmasc;
    }
}
