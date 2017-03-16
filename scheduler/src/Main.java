import java.awt.print.PrinterAbortException;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by raeleneg on 2/6/17.
 */
public class Main {

    static int time;
    static ArrayList<Integer> startingTimes;
    static ArrayList<Integer> endingTimes;
    static ArrayList<Integer> needed;
    static ArrayList<Integer> prior0;
    static ArrayList<Integer> prior1;
    static ArrayList<Integer> prior2;
    static ArrayList<Integer> prior3;
    static ArrayList<Integer> prior4;
    static ArrayList<Integer> prior5;

    static PrintWriter writer;

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.print("Enter input file path: ");
        String inputName = reader.nextLine();
        System.out.print("Enter output file path: ");
        String outputName = reader.nextLine();
        File file = new File(outputName);
        try {
            writer = new PrintWriter(outputName);
            BufferedReader buffReader = new BufferedReader(new FileReader(inputName));
            String[] input = buffReader.readLine().split(" ");

            FIFO(input);
            SJF(input);
            //SRT(input);
            MLF(input);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void restart() {
        time = 0;
        endingTimes = new ArrayList<Integer>();
        startingTimes = new ArrayList<Integer>();
        needed = new ArrayList<Integer>();
        prior0 = new ArrayList<Integer>();
        prior1 = new ArrayList<Integer>();
        prior2 = new ArrayList<Integer>();
        prior3 = new ArrayList<Integer>();
        prior4 = new ArrayList<Integer>();
        prior5 = new ArrayList<Integer>();
    }

    private static void printTimes() {
        writer.print(mean());
        for (int end: endingTimes) {
            writer.print(" ");
            writer.print(end);
        }
        writer.print("\n");
    }

    private static String mean() {
        int sum = 0;
        for (int i = 0; i < endingTimes.size(); i++){
            sum += (endingTimes.get(i));
        }
        if (sum != 0){
            //return String.format("%.2f", (double)(Math.round(((double) sum / (double) endingTimes.size()) * 100)) / 100);
            return (String.format("%.2f", (double) sum / (double) endingTimes.size()));
        }
        return "0";
    }
    private static void FIFO(String[] input){
        restart();
        for (int i = 0; i <= input.length - 2; i+=2){
            time += Integer.valueOf(input[i+1]);
            endingTimes.add(time - Integer.parseInt(input[i]));
            startingTimes.add(Integer.parseInt(input[i]));
        }
        printTimes();
    }

    private static void SJF(String[] input) {
        restart();
        for (int i = 0; i <= input.length - 2; i+=2){
            startingTimes.add(Integer.parseInt(input[i]));
            needed.add(Integer.parseInt(input[i+1]));
            endingTimes.add(0);
        }
        int current = startingTimes.get(0); //lowest time came first
        int i = 0;
        while (i <= startingTimes.size() - 1){
            if (startingTimes.get(i) == current){
                prior0.add(i); //add 0s to pq
            }
            if (startingTimes.get(i) != current) {
                while (!prior0.isEmpty()) { //get smallest out of first and runs
                    int min = needed.get(prior0.get(0));
                    int index = 0;
                    for (int x = 0; x <= prior0.size() - 1; x++) {
                        if (needed.get(prior0.get(x)) < min) {
                            index = x;
                            min = needed.get(prior0.get(x));
                        }
                    }
                    time += needed.get(prior0.get(index));
                    endingTimes.set(prior0.get(index), time - startingTimes.get(prior0.get(index)));
                    prior0.remove(index);
                }
                current = startingTimes.get(i);
                i--;
            }
            i++;
        }
        //do once for last time block (equal to current still)
        while (!prior0.isEmpty()) { //get smallest out of first and runs
            int min = needed.get(prior0.get(0));
            int index = 0;
            for (int x = 0; x <= prior0.size() - 1; x++) {
                if (needed.get(prior0.get(x)) < min) {
                    index = x;
                    min = needed.get(prior0.get(x));
                }
            }
            time += needed.get(prior0.get(index));
            endingTimes.set(prior0.get(index), time - startingTimes.get(prior0.get(index)));
            prior0.remove(index);
        }
        printTimes();
        //time += Integer.valueOf(input[i+1]);
        //endingTimes.add(time - Integer.parseInt(input[i]));
    }

    private static void SRT(String[] input) {
        restart();
        for (int i = 0; i <= input.length - 2; i+=2){
            startingTimes.add(Integer.parseInt(input[i]));
            needed.add(Integer.parseInt(input[i+1]));
            endingTimes.add(0);
        }
        for (int i = 0; i < startingTimes.size(); i++){
            prior0.add(i);
        }
        while (!prior0.isEmpty()) {
            for (int process: prior0) {
                if (startingTimes.get(process) <= time && !prior1.contains(process)) {
                    prior1.add(process);
                }
            }
            int index = 0;
            int min = needed.get(prior1.get(0));
            for (int process: prior1){ // get shortest process
                if (needed.get(process) < min ){
                    min = needed.get(process);
                    index = process;
                }
            }
            needed.set(index, needed.get(index) - 1);
            time++;
            if (needed.get(index) == 0) {
                prior0.remove(prior0.indexOf(index));
                prior1.remove(prior1.indexOf(index));
                endingTimes.set(index, time - startingTimes.get(index));
            }

        }
        printTimes();
    }

    private static void MLF(String[] input) {
        restart();
        ArrayList<Integer> added = new ArrayList<>();
        for (int i = 0; i <= input.length - 2; i+=2){
            startingTimes.add(Integer.parseInt(input[i]));
            needed.add(Integer.parseInt(input[i+1]));
            endingTimes.add(0);
        }
        for (int i = 0; i < startingTimes.size(); i++){
            prior0.add(i);
        }

        ArrayList<Integer>[] prior = new ArrayList[]{prior1, prior2, prior3, prior4, prior5};
        ArrayList<Integer> preempt = new ArrayList<>();
        while (!prior0.isEmpty()) {
            for (int process : prior0) {
                if (startingTimes.get(process) <= time && !added.contains(process)) {
                    prior1.add(process);
                    added.add(process);
                    preempt.add(process);
                }
            }
            for (int i = 0; i < prior.length; i++){
                if (!prior[i].isEmpty()){
                    boolean found = false;
                    for (int x = 0; x < prior[i].size(); x++){
                        if (preempt.contains(prior[i].get(x))) {
                            prior[i].add(0, prior[i].get(x));
                            prior[i].remove(x + 1);
                            found = true;
                            break;
                        }
                    }
                    if (!found && !preempt.isEmpty()){
                        continue;
                    }
                    preempt = new ArrayList<>();
                    preempt.add(prior[i].get(0));
                    int index = prior[i].get(0);
                    if (needed.get(index) - (int) Math.pow(2, i) < 0){
                        time += (needed.get(index));
                    } else {
                        time += (int) Math.pow(2, i);
                    }
                    needed.set(index, needed.get(index) - (int) Math.pow(2, i));
                    if (needed.get(index) <= 0) {
                        preempt = new ArrayList<>();
                        prior0.remove(prior0.indexOf(index));
                        prior[i].remove(prior[i].indexOf(index));
                        endingTimes.set(index, time - startingTimes.get(index));
                    } else {
                        if (i != prior.length - 1) {
                            prior[i].remove(prior[i].indexOf(index));
                            prior[i + 1].add(0, index);
                        }
                    }
                    break;
                }
            }
        }
        printTimes();
    }

}