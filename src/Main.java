import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by raeleneg on 3/1/17.
 */

public class Main {

    // Segment Table w Page Tables
    public static Frame[] PM = new Frame[1024];
    public static int[] isOccupied =  new int[1024];
    public static ArrayList<TLB> tlb = new ArrayList<TLB>();
    public static ArrayList<Segment> seg = new ArrayList<Segment>(); // list of PageTables of segments
    public static Segment currentSeg; // current Page Table of segment
    public static Page currentPage;
    public static int w;
    public static int p;
    public static int s;
    public static PrintWriter writer1;
    public static PrintWriter writer2;

    public static void main(String[] args) throws IOException {
        Scanner reader = new Scanner(System.in);
        System.out.print("Enter VM input 1 file path: ");
        String input1 = reader.nextLine();
        System.out.print("Enter VA input 2 file path: ");
        String input2 = reader.nextLine();
        System.out.print("Enter output 1 file path: ");
        String output1 = reader.nextLine();
        System.out.print("Enter TLB output 2 file path: ");

        String output2 = reader.nextLine();
        File file1 = new File(output1);
        File file2 = new File(output2);

        writer1 = new PrintWriter(output1);
        writer2 = new PrintWriter(output2);

        // init Segment Table (ST)
        isOccupied[0] = 1;

        // reads Page Tables of ST
        BufferedReader buff1 = new BufferedReader(new FileReader(input1));
        String[] read1 = buff1.readLine().split(" ");
        for (int i = 0; i < read1.length - 1 ; i += 2){
            Segment newSegment = new Segment();
            newSegment.segment = Integer.parseInt(read1[i]);
            newSegment.address = Integer.parseInt(read1[i + 1]);
            newSegment.pages = new ArrayList<Page>();
            seg.add(newSegment);

            int PA = newSegment.address / 512;
            isOccupied[PA] = 1;
            isOccupied[PA + 1] = 1;
        }

        // reads Page Table
        String[] read2 = buff1.readLine().split(" ");

        for (int i = 0; i < read2.length - 1; i += 3){
            for (Segment segment: seg){
                if (segment.segment == Integer.parseInt(read2[i + 1])){
                    Page newPage = new Page();
                    newPage.page = Integer.parseInt(read2[i]);
                    newPage.address = Integer.parseInt(read2[i + 2]);
                    segment.pages.add(newPage);

                    if (newPage.address != -1){
                        isOccupied[newPage.address / 512] = 1;
                    }
                    break;
                }
            }
        }



        // reads instructions
        BufferedReader buff2 = new BufferedReader(new FileReader(input2));
        String[] read3 = buff2.readLine().split(" ");
        for (int i = 0; i < read3.length - 1; i += 2){
            readInstruction(Integer.parseInt(read3[i]), Integer.parseInt(read3[i + 1]));
        }

        writer1.close();
        writer2.close();
    }

    public static void readInstruction(int read_write, int VA){
        w = getLastNBits(VA, 9);
        p = getLastNBits(VA >> 9, 10);
        s = getLastNBits(VA >> 19, 9);

        if (read_write != 0 && read_write != 1){
            printOutput("err ");
            printTLBOutput("m err ");
        } else {
            carryInstruction(read_write);
        }
    }

    public static int getLastNBits(int VA, int n){
        return ((1 << n + 1) - 1) & VA;
    }

    public static void carryInstruction(int read_write){
        for (TLB entry: tlb){
            if (s == entry.s && p == entry.p){
                currentPage.address = entry.address;
                printTLBOutput("h ");
                successfulInstruction(read_write, false);
                return;
            }
        }
        printTLBOutput("m ");

        currentSeg = new Segment();
        for (Segment segment: seg){
            if (segment.segment == s){
                currentSeg = segment;
                break;
            }
        }

        currentPage = new Page();
        if (currentSeg.address > 0){
            for (Page page: currentSeg.pages) {
                if (page.page == p) {
                    currentPage = page;
                    break;
                }
            }
        } else {
            if (currentSeg.address == 0) {
                segPageMiss(read_write);
            }
            if (currentSeg.address == -1){
                printOutput("pf ");
                printTLBOutput("pf ");
            }
            return;
        }

        if (currentPage.address == 0){
            segPageMiss(read_write);
        } else if (currentPage.address == -1){
            printOutput("pf ");
            printTLBOutput("pf ");
        } else {
            successfulInstruction(read_write, true);
        }


    }

    public static void segPageMiss(int read_write){
        boolean addPageTable = false;

        if (read_write == 0) { // read, 0
            printOutput("err ");
            printTLBOutput("err ");
        } else {
            //make new page and segment
            if (currentSeg.address == 0) {
                addPageTable = true;
                for (int i = 0; i < isOccupied.length - 1; i++) {
                    if (isOccupied[i] != 1 && isOccupied[i + 1] != 1){
                        currentSeg.address = i*512;
                        currentSeg.pages = new ArrayList<Page>();
                        currentSeg.segment = s;
                        break;
                    }
                }
            }
            for (int i = 0; i < isOccupied.length - 1; i++) {
                if (isOccupied[i] != 1){
                    currentPage.address = i*512;
                    currentPage.page = p;
                    break;
                }
            }

            if (addPageTable){
                currentSeg.pages.add(currentPage);
                seg.add(currentSeg);
            } else {
                for (Segment segment: seg){
                    if (segment.segment == s){
                        segment.pages.add(currentPage);
                    }
                }
            }

            successfulInstruction(read_write, true);
        }
    }

    public static void successfulInstruction(int read_write, boolean miss){
        if (miss) {
            TLB newTLB = new TLB();
            newTLB.address = currentPage.address;
            newTLB.s = s;
            newTLB.p = p;

            tlb.add(newTLB);
            if (tlb.size() > 4) {
                tlb.remove(0);
            }
        }
        printOutput(String.valueOf(currentPage.address + w) + " ");
        printTLBOutput(String.valueOf(currentPage.address + w) + " ");
    }


    public static void printOutput(String output){
        writer1.print(output);
    }
    public static void printTLBOutput(String output){
        writer2.print(output);
    }


}
