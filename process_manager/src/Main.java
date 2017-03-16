/**
 * Created by raeleneg on 1/22/17.
 */

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Scanner;
import java.io.PrintWriter;
import java.io.File;

public class Main {
    static Process first;
    static Process last;
    static ArrayList<Process> prior1;
    static ArrayList<Process> prior2;
    static int[] resources;
    static PrintWriter writer;

    public static Process running(){
        if (!prior2.isEmpty() && !isBlocked(prior2)){
            if (prior2.get(0).status.equals("blocked")){
                timeout();
            }
            writer.print(prior2.get(0).name + " ");
            return prior2.get(0);
        } else if (!prior1.isEmpty() && !isBlocked(prior1)){
            if (prior1.get(0).status.equals("blocked")){
                timeout();
            }
            writer.print(prior1.get(0).name + " ");
            return prior1.get(0);
        } else {
            writer.print("init ");
        }
        return new Process("fakeh20", first, 0);
    }

    public static void start(){
        first = new Process("init", null, 0);
        last = first;
        prior1 = new ArrayList<Process>();
        prior2 = new ArrayList<Process>();
        resources = new int[]{1, 2, 3, 4};
        //System.out.print("init ");
        running();
    }

    public static void create(String name, int priority){
        Process new_process = new Process(name, last, priority);
        for (Process process: prior1){
            if (process.name.equals(name)){
                writer.print("error ");
                return;
            }
        }
        for (Process process: prior2){
            if (process.name.equals(name)){
                writer.print("error ");
                return;
            }
        }
        if (priority == 1){
            prior1.add(new_process);
        } else if (priority == 2){
            prior2.add(new_process);
        } else {
            writer.print("error ");
            return;
        }
        last.children.add(new_process);
        last = new_process;
        //System.out.print("cr" + name + " ");
        running();
    }

    public static void delete(String name){
        @SuppressWarnings("unused")
        Process found = remove_prior(name);
        //found.parent.children.remove(found);
        //System.out.print("del" + name + " ");
        for (Process process: found.children){
            delete(process.name);
        }
    }

    public static Process remove_prior(String name){
        //Process found;
        for (Process process: prior1){
            if (process.name.equals(name)){
                prior1.remove(process);
                release_process(process);
                return process;
            }
        }
        for (Process process: prior2){
            if (process.name.equals(name)){
                prior2.remove(process);
                release_process(process);
                return process;
            }
        }
        return new Process("fakeh20", first, 0);
    }

    public static void release_process(Process process){
        for (int i = 0; i < 4; i++){
            if (process.has[i] > 0){
                resources[i] += process.has[i];
            }
        }
    }

    public static boolean isBlocked(ArrayList<Process> processes){
        for (Process process: processes){
            if (process.status.equals("ready")){
                return false;
            }
        }
        return true;
    }

    public static boolean exists(String name){
        for (Process process: prior1){
            if (process.name.equals(name)){
                return true;
            }
        }
        for (Process process: prior2){
            if (process.name.equals(name)){
                return true;
            }
        }
        return false;
    }

    public static void timeout(){
        if (!prior2.isEmpty()){
            if (!isBlocked(prior2)) {
                prior2.add(prior2.remove(0));
                if (prior2.get(0).status.equals("blocked")) {
                    timeout();
                }
            }
        }
        if (!prior1.isEmpty()) {
            if (!isBlocked(prior1)) {
                prior1.add(prior1.remove(0));
                if (prior1.get(0).status.equals("blocked")){
                    timeout();
                }
            }
        }
    }

    public static void request(int resource, int requested){
        if (resource > 4){
            writer.print("error ");
            return;
        }
        if (resource < requested){
            writer.print("error ");
            return;
        }
        //System.out.print("req" + Integer.toString(resource) + " ");
        try {
            if (!prior2.isEmpty() && !isBlocked(prior2)) {
                if (prior2.get(0).needed[resource - 1] + prior1.get(0).has[resource - 1] + requested > resource) {
                    writer.print("error ");
                    return;
                }
                prior2.get(0).needed[resource - 1] += requested;
                prior2.get(0).status = "blocked";
            } else if (!prior1.isEmpty() && !isBlocked(prior1)) {
                if (prior1.get(0).needed[resource - 1] + prior1.get(0).has[resource - 1] + requested > resource) {
                    writer.print("error ");
                    return;
                }
                prior1.get(0).needed[resource - 1] += requested;
                prior1.get(0).status = "blocked";
            } else {
                writer.print("error ");
                return;
            }
        } catch (IndexOutOfBoundsException e){
            writer.print("error ");
        }
        available();
        running();
    }

    public static void available(){
        for (Process process: prior2){
            if (process.status.equals("blocked")){
                feed(process);
            }
        }
        for (Process process: prior1){
            if (process.status.equals("blocked")){
                feed(process);
            }
        }
    }

    public static void feed(Process process){
        for (int i = 0; i < 4; i++){
            if (process.needed[i] > 0 && resources[i] >= process.needed[i]){
                resources[i] = resources[i] - process.needed[i]; //take from resources
                process.has[i] = process.needed[i]; //mark as given
                process.needed[i] = 0; //give to process
            }
        }
        if (Arrays.equals(process.needed, new int[]{0,0,0,0})){
            process.status = "ready";
        }
    }

    public static void release(int resource, int released){
        if (resource > 4){
            writer.print("error ");
            return;
        }
        //System.out.print("rel" + Integer.toString(resource) + " ");
        if (resources[resource - 1] == resource || resource < released + resources[resource - 1]){
           writer.print("error ");
        } else if (!isBlocked(prior1) && !isBlocked(prior2)){
            Process process = running();
            if (process.has[resource - 1] != 0 && process.has[resource - 1] >= released){ //take back all of released
                process.has[resource - 1] -= released;
                resources[resource - 1] += released;
                //running();
            } else {
                writer.print("error ");
            }
        }
            //running();
    }

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.print("Enter input file path: ");
        String fileName = reader.nextLine();
        System.out.print("Enter output file path: ");
        String outputName = reader.nextLine();
        File file = new File(outputName);
        try {
            writer = new PrintWriter(outputName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for(String line; (line = br.readLine()) != null; ) {
                String[] spl = line.split(" ");
                if (spl[0].equals("init")) {
                    writer.print("\n");
                    start();
                } else if (spl[0].equals("cr")) {
                    create(spl[1], Integer.parseInt(spl[2]));
                } else if (spl[0].equals("de")) {
                    if (!exists(spl[1])){
                        writer.print("error ");
                    } else {
                        delete(spl[1]);
                        available();
                        running();
                    }
                } else if (spl[0].equals("to")) {
                    timeout();
                    running();
                } else if (spl[0].equals("req")) {
                    request(Integer.parseInt(spl[1].substring(1)), Integer.parseInt(spl[2]));
                } else if (spl[0].equals("rel")) {
                    release(Integer.parseInt(spl[1].substring(1)), Integer.parseInt(spl[2]));
                } else if (spl[0].equals("...")){
                    writer.print("...");
                    writer.close();
                    return;
                } else if (spl[0].equals("")){
                    continue;
                }
                else {
                    writer.print("error ");
                }
                available(); //checks if any processes need feeding
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

}