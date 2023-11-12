package be.kuleuven.Instances;

public class Entry {

    private String name;
    private BulletinEntry bulletinEntry;

    public Entry(String name, BulletinEntry bulletinEntry) {
        this.name = name;
        this.bulletinEntry = bulletinEntry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BulletinEntry getBulletinEntry() {
        return bulletinEntry;
    }

    public void setBulletinEntry(BulletinEntry bulletinEntry) {
        this.bulletinEntry = bulletinEntry;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "name='" + name + '\'' +
                ", bulletinEntry=" + bulletinEntry +
                '}';
    }
}
