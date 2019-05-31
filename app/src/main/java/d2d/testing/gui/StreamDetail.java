package d2d.testing.gui;


public class StreamDetail {
    private String name;
    private String ip;

    public StreamDetail(String name, String ip){
        this.ip = ip;
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if(o instanceof StreamDetail) {
            StreamDetail streamDetail = (StreamDetail) o;

            return streamDetail.ip.equals(this.ip) && streamDetail.name.equals(this.name);
        }
        return false;
    }
}
