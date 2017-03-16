/**
 * Created by raeleneg on 1/22/17.
 */
import java.util.ArrayList;

public class Process {
    String name;
    int priority;
    ArrayList<Process> children;
    Process parent;
    int[] needed;
    int[] has;
    String status;

    public Process(String name, Process parent, int priority) {
        this.name = name;
        this.parent = parent;
        this.priority = priority;
        this.needed = new int[]{0,0,0,0};
        this.has = new int[]{0,0,0,0};
        this.children = new ArrayList<Process>();
        this.status = "ready";
    }



}
