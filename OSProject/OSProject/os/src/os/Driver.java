package os;

import java.io.*;
import java.util.*;

public class Driver  {

	static Scanner input = new Scanner(System.in);
	static List<PCB> Q1 = new ArrayList<>();
	static List<PCB> Q2 = new ArrayList<>();
	static List<PCB> gantChart = new ArrayList<>();
	static int processID = 0;
    static int currentTime = 0;
    static boolean terminated = true;//i think no need for this boolean variable
    
	public static void main(String[] args) {

		int choice;
		try {
			do {
				System.out.println("Menu:");
				System.out.println("1- Enter process information.");
				System.out.println("2- Report detailed information about each process and different scheduling criteria.");
				System.out.println("3- Exit the program");
				System.out.print("Enter your choice: ");

				choice = input.nextInt();

				switch (choice) {
				case 1:
					processInfo();
					break;
				case 2:
					ReportDetailedInform();
					break;
				case 3:
					System.out.println("The menu has terminated!");
					break;

				default:
					System.out.println("Invalid input!");
				}

			} while (choice != 3);

		} catch (InputMismatchException e) {
			System.out.println("Only integers allowed. Please enter again.");
			input.next();
		}
	}

	public static void processInfo() {

		System.out.print("\nEnter the number of processes: ");
		int num = 0;
		try {
			num = input.nextInt();
			if (num < 0)
				throw new IllegalArgumentException("Number of processes cannot be negative.");
		} catch (InputMismatchException e) {
			System.out.println("Only integers allowed. Please enter again.");
			input.next();
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid input: " + e.getMessage());
			while (num < 0) {
				System.out.print("Illegal number of number of processes,reEnter:");
				num = input.nextInt();
			}
		}

		for (int i = 0; i < num; i++) {
			System.out.println("\nEnter process-" + (processID + 1) + " information:");

			try {
				System.out.print("Enter process-" + (processID + 1) + " priority (select 1 or 2): ");
				int priority = input.nextInt();
				while (priority != 1 && priority != 2) {
					System.out.print("Illegal number of priority,reEnter:");
					priority = input.nextInt();
				}

				System.out.print("Enter process-" + (processID + 1) + " arrival time: ");
				int arrivalTime = input.nextInt();
				while (arrivalTime < 0) {
					System.out.print("Illegal number of arrivalTime,reEnter:");
					arrivalTime = input.nextInt();
				}

				System.out.print("Enter process-" + (processID + 1) + " CPU burst: ");
				int cpuBurst = input.nextInt();
				while (cpuBurst < 0) {
					System.out.print("Illegal number of cpuBurst,reEnter:");
					cpuBurst = input.nextInt();
				}

				PCB process = new PCB("P" + ++processID, priority, arrivalTime, cpuBurst);

				if (priority == 1)
					Q1.add(process);
				else if (priority == 2)
					Q2.add(process);

			} catch (InputMismatchException e) {
				System.out.println("Some fields has input mismatch! Only integers allowed. Please enter again.");
				input.next();
				i--;
			}

		}
		System.out.println();

	}

	public static void ReportDetailedInform() {
		if (Q1.isEmpty() && Q2.isEmpty()) {//if there is no process yet in the system
			System.out.println("\nThere is no processes yet!");
		} else {//if there is processes in the system do scheduling
			//sort Q1 & Q2 arrival time to make sure the are in appropriate order
			Collections.sort(Q1, Comparator.comparingInt(process -> process.getArrivalTime()));
			Collections.sort(Q2, Comparator.comparingInt(process -> process.getArrivalTime()));

			//PCB SJFprocess = null;
			StringBuilder sb = new StringBuilder();
			sb.append("\nScheduling Order: [ ");
			
			
			while (!Q1.isEmpty() || !Q2.isEmpty()) {
				if (!Q1.isEmpty() && Q1.get(0).getArrivalTime() <= currentTime) {//do RR algorithm in Q1
					RounRobin(sb);
				} else if (!Q2.isEmpty() && Q2.get(0).getArrivalTime() <= currentTime) {//do SJF algorithm in Q1
					sjfWithPreemptive(sb);
					terminated = true;
				} else {
					sb.append("idle | ");
					currentTime++;
				}
			}
			int lastIndex = sb.length() - 2;
			if (lastIndex >= 0 && sb.charAt(lastIndex) == '|') {
				sb.deleteCharAt(lastIndex);
			}
			sb.append("]\n");
			
			//print process info
			printProcessInfo(sb);
			writeDetailsToFile(sb);
		} // end of else
	}// end of method

	public static void RounRobin(StringBuilder sb) {
		PCB currentProcess = Q1.remove(0);
		sb.append(currentProcess.getProcessID() + " | ");
		if (currentProcess.getStartTime() == -1) {
			currentProcess.setStartTime(currentTime);
		}
		if (currentProcess.getCopyCPUBurst() > 3) {
			currentTime += 3;
			currentProcess.setCopyArrivalTime(currentTime);
			currentProcess.setCopyCPUpuBurst(currentProcess.getCopyCPUBurst() - 3);
			Q1.add(currentProcess);
			Collections.sort(Q1, Comparator.comparingInt(process -> process.getCopyArrivalTime()));
		} else {
			currentTime += currentProcess.getCopyCPUBurst();
			currentProcess.setTerminationTime(currentTime);
			currentProcess.setTurnaroundTime(currentProcess.getTerminationTime() - currentProcess.getArrivalTime());
			currentProcess.setWaitingTime(currentProcess.getTurnaroundTime() - currentProcess.getCpuBurst());
			currentProcess.setResponseTime(currentProcess.getStartTime() - currentProcess.getArrivalTime());
			gantChart.add(currentProcess);
		}
	}

	public static void sjfWithPreemptive(StringBuilder sb) {//schedule Q2 process in empty spaces in Q1 with SJF non-primtive algorithm
		PCB SJFprocess = null;
		if (terminated == false) {
			SJFprocess = Q2.get(0);
		} else {
			if(Q2.get(0).getArrivalTime()<= currentTime) {
				SJFprocess = Q2.get(0);
				for(PCB process : Q2) {
					if(process.getArrivalTime()<= currentTime && process.getCopyCPUBurst()<SJFprocess.getCopyCPUBurst())
						SJFprocess = process;
				}
			}
		}
		
		if (SJFprocess != null) {
			Q2.remove(SJFprocess);
			sb.append(SJFprocess.getProcessID() + " | ");
			if (SJFprocess.getStartTime() == -1) {
				SJFprocess.setStartTime(currentTime);
			}
			SJFprocess.setResponseTime(SJFprocess.getStartTime() - SJFprocess.getArrivalTime());
			while ((Q1.isEmpty() || Q1.get(0).getArrivalTime() > currentTime) && SJFprocess.getCopyCPUBurst() > 0) {
				SJFprocess.setCopyCPUpuBurst(SJFprocess.getCopyCPUBurst() - 1);
				currentTime++;
			}
			if (SJFprocess.getCopyCPUBurst() > 0) {
				terminated = false;
				Q2.add(0, SJFprocess);
			} else {
				terminated = true;
				SJFprocess.setTerminationTime(currentTime);
				SJFprocess.setTurnaroundTime(SJFprocess.getTerminationTime() - SJFprocess.getArrivalTime());
				SJFprocess.setWaitingTime(SJFprocess.getTurnaroundTime() - SJFprocess.getCpuBurst());
				gantChart.add(SJFprocess);
			}
		}
	}

	public static StringBuilder printProcessInfo(StringBuilder sb) {
		// to print each processes info
		Collections.sort(gantChart, (obj1, obj2) -> obj1.getProcessID().compareTo(obj2.getProcessID()));
		for (PCB process : gantChart) {
			sb.append(process.toString());
		}

		// calculate average time and then print it
		double averageTurnaroundTime = 0;
		double averagegWaitingTime = 0;
		double averageResponseTime = 0;

		if (!gantChart.isEmpty()) {
			int sumTurnaround = 0;
			int sumWaiting = 0;
			int sumResponse = 0;

			for (PCB process : gantChart) {
				sumTurnaround += process.getTurnaroundTime();
				sumWaiting += process.getWaitingTime();
				sumResponse += process.getResponseTime();
			}
			averageTurnaroundTime = (double) sumTurnaround / gantChart.size();
			averagegWaitingTime = (double) sumWaiting / gantChart.size();
			averageResponseTime = (double) sumResponse / gantChart.size();
		}
		sb.append("Average[ Average Turnaround Time: " + averageTurnaroundTime + " ,Average Waiting Time: "
				+ averagegWaitingTime + " ,Average Response Time: " + averageResponseTime + " ]\n");

		System.out.println(sb);// print output
		return sb;
	}

	public static void writeDetailsToFile(StringBuilder sb) {//to print process info in Report.txt
		try {
			FileWriter fileWriter = new FileWriter("Report.txt");
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.println(sb);

			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}// end of class
