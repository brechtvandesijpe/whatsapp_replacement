package be.kuleuven.Instances;

import java.util.*;

public class Group {
    private String groupId;
    private List<String> clientNames;

    public Group() {}

    public Group(String groupId) {
        clientNames = new ArrayList<>();
        this.groupId = groupId;
    }

    public Group(String groupId, List<String> clientNames) {
        clientNames = new ArrayList<>();
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getClientNames() {
        return clientNames;
    }

    public void setClientNames(List<String> clientNames) {
        this.clientNames = clientNames;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", clientNames=" + clientNames +
                '}';
    }
}
