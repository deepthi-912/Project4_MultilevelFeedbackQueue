package MultiLevelQueue;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;


public class MultilevelFeedbackQueue {

	static long[] burstTime=new long[10001];
	static long[] completionTime=new long[10001];
	static long[] waitingTime=new long[10001];
	static long[] turnaroundTime=new long[10001];
	long[] arrivalTime = new long[10001];
	static long totalClocks;
	static long[] p=new long[10001];
	static int totalIncomingProcesses=0;
	static double totalTurnaroundTime=0, totalWaitTime=0;	
	static long endTime;
	static long idleTime=0;
	static long totalExecutionTime=0;
	public static void main(String[] args) throws IOException {
		MultilevelFeedbackQueue multilevelqueue = new MultilevelFeedbackQueue();
	    Scanner sc = new Scanner(System.in); 
	    System.out.println("Enter Input: ");

	    //Take the inputs from user
	    String fileName = sc.next();
	    int demotionCriteria = sc.nextInt();
	    int dispatchRatio = sc.nextInt();
		multilevelqueue.inputReader(fileName);
		multilevelqueue.roundRobinScheduling(demotionCriteria, dispatchRatio);
		// Calculating waiting time and turn around time for all the process in low priority queue
		for (int i = 0; i < totalClocks; i++) {
			if(burstTime[i]>0) {
				turnaroundTime[i]=completionTime[i]-i;
				waitingTime[i]=turnaroundTime[i]-burstTime[i];
				totalWaitTime+=waitingTime[i];
				totalTurnaroundTime+=turnaroundTime[i];
			}

		}
		System.out.println("End Time: " + endTime);
		totalExecutionTime = endTime- idleTime;
		System.out.println("Processes Completed/Executed: " + totalIncomingProcesses);
		System.out.println("Total Execution Time: " + totalExecutionTime);
		System.out.println("Idle Time: " + idleTime);
		System.out.println("Wait time Average: " + (int)totalWaitTime/totalIncomingProcesses);
		System.out.println("Turnaround Time average : " + (int)totalTurnaroundTime/totalIncomingProcesses);
	}

	//Round robin scheduling with multilevel queue A and queue B
	public void roundRobinScheduling (int demotionCriteria, int dispatchRatio) {

		Queue<Integer> queueA = new LinkedList<>(); // processes in queue A
		Queue<Integer> queueB = new LinkedList<>(); // processes in queue B
		List<Integer> processes = new ArrayList<>(); // all processes in Queue A and Queue B.
		long currentClock=0;

		//Add the first process encountered to queue A.
		for (int i = 0;i <totalClocks; i++) {
			if(burstTime[i]>0) {
				queueA.add(Integer.valueOf(i));
				processes.add(Integer.valueOf(i));
				currentClock=i+1;
				break;
			} else {
				idleTime++;
			}
		}

		//Store remaining burst time of each processes. For idle clock take the remaining burst time as 0.
		long remainingBurstTimes[]= new long[(int) totalClocks];
		for (long i = 0 ; i < totalClocks; i++)
		{
			remainingBurstTimes[(int) i] =  burstTime[(int) i];           
		}
		int currentDispatchNumber=0;
		boolean cpuPriorityflag=true; // checks if the next CPU execution should be done from queue A (true) or queue B (false)
		
		//Keep the scheduling running until all the processes are executed from both queues.
		while (currentClock<totalClocks || !queueA.isEmpty() || !queueB.isEmpty())
		{
			int wt=0;
			//add the process to queue A.
			if(currentClock<totalClocks && burstTime[(int) currentClock]>0) 
			{
				queueA.add(Integer.valueOf((int)currentClock));
				processes.add(Integer.valueOf((int)currentClock));
			}
			
			if(isQueueAExecution(queueA, queueB, cpuPriorityflag)) {
				Integer top = queueA.remove();
				processes.remove(top);
				int i= Integer.valueOf(top);
				if (remainingBurstTimes[i] > 0)
				{ 
					// If burst time is greater than quantum time of A i.e 5
					if (remainingBurstTimes[i] > 5)
					{
						currentClock = updateClockForQa(queueA, queueB, processes, currentClock, remainingBurstTimes, wt, top, i, demotionCriteria);
					}

					// If burst time is smaller than or equal to quantum time of A. 
					//Last cycle for this process executes in CPU.
					else
					{
						currentClock = updateClockForLessQTime(queueA, processes, currentClock, remainingBurstTimes, wt, i);
						
					}
					currentDispatchNumber++;
					if(currentDispatchNumber>=dispatchRatio) {
						currentDispatchNumber=0;
						cpuPriorityflag=false;
					}
				}
			} else if(isQueueBExecution(queueA, queueB, cpuPriorityflag)) {
				Integer top = queueB.remove();
				processes.remove(top);
				int i= Integer.valueOf(top);
				if (remainingBurstTimes[i] > 0)
				{                       
					// If burst time is greater than quantum time of B i.e 40
					if (remainingBurstTimes[i] > 40)
					{
						// Increase the value of t i.e. shows how much time a process has been processed
						currentClock = updateClockForQb(queueA, queueB, processes, currentClock, remainingBurstTimes, wt, top, i);
					}

					// If burst time is smaller than or equal to quantum time of B. 
					//Last cycle for this process executes in CPU.
					else
					{
						currentClock = updateClockForLessQTime(queueA, processes, currentClock, remainingBurstTimes, wt, i);

					}
					currentDispatchNumber=0;
					cpuPriorityflag=true;
				}
			} else {
				idleTime++;
				currentClock++;
			}
		}
		endTime=currentClock;

	}

	private boolean isQueueAExecution(Queue<Integer> queueA, Queue<Integer> queueB, boolean cpuPriorityflag) {
		return (!cpuPriorityflag && !queueA.isEmpty() && queueB.isEmpty()) || (cpuPriorityflag && !queueA.isEmpty());
	}

	private boolean isQueueBExecution(Queue<Integer> queueA, Queue<Integer> queueB, boolean cpuPriorityflag) {
		return (cpuPriorityflag && queueA.isEmpty() && !queueB.isEmpty()) || (!cpuPriorityflag && !queueB.isEmpty());
	}
	
	private void updateWaitingTimesEachClock(List<Integer> processes, long[] remainingBurstTimes, long clock) {
		for(Integer i: processes) {
			if(i.intValue() != clock && remainingBurstTimes[i.intValue()]>0 && burstTime[i.intValue()]>0) {
				waitingTime[i.intValue()]++;
			}
		}
	}

	private long updateClockForQa(Queue<Integer> qa, Queue<Integer> qb, List<Integer> processes, long clock,
			long[] rem_bt, int quantumA, Integer top, int i, int demotionCriteria) {
		while(quantumA<5) {
			updateWaitingTimesEachClock(processes, rem_bt, clock);
		// Increase the value of clock by 1 sec
			clock += 1;
		// Decrease the burst_time of current process by 1 sec
			rem_bt[i] -= 1;
			quantumA++;
			//check at current clock if any process is incoming. If so add it to queue A.
			if(clock<totalClocks && burstTime[(int) clock]>0) 
			{
				qa.add(Integer.valueOf((int)clock));
				processes.add(Integer.valueOf((int)clock));
			}
		}
		//If at the current clock if the demotion criteria is reached, demote the process to queue B.
		if(burstTime[i]-(demotionCriteria*5)>=rem_bt[i]) {
			qb.add(top);
		} else {
			qa.add(top);
		}
		processes.add(top);
		return clock;
	}

	private long updateClockForQb(Queue<Integer> qa, Queue<Integer> qb, List<Integer> processes, long clock, long[] remainingBurstTime, long quantumB,
			Integer top, int i) {
		while(quantumB<40) {
			updateWaitingTimesEachClock(processes, remainingBurstTime, clock);
			clock +=1;
		// Decrease the remaining burst time of current process by 1
			remainingBurstTime[i] -=1;
			quantumB++;
			if(clock<totalClocks && burstTime[(int) clock]>0) 
			{
				qa.add(Integer.valueOf((int)clock));
				processes.add(Integer.valueOf((int)clock));
			}
		}
		qb.add(top);
		processes.add(top);
		return clock;
	}

	private long updateClockForLessQTime(Queue<Integer> qa, List<Integer> processes, long clock, long[] rem_bt, long wt, long i) {
		while(wt<rem_bt[(int) i]) {
			updateWaitingTimesEachClock(processes, rem_bt, clock);
			clock = clock + 1;
			wt++;
			//check at current clock if any process is incoming. If so add it to queue A.
			if(clock<totalClocks && burstTime[(int) clock]>0) 
			{
				qa.add(Integer.valueOf((int)clock));
				processes.add(Integer.valueOf((int)clock));
			}
		}
		//mark the completion time of the current process.
		completionTime[(int) i]=(int) clock;
		//mark the remaining burst time as 0.
		rem_bt[(int) i] = 0;
		return clock;
	}


	public void inputReader(String filepath) throws IOException {
		FileReader filereader = new FileReader(filepath);
        Scanner sc = new Scanner(filereader);
        int i=0;
          
	    while (sc.hasNextLine()) {
			String input=sc.nextLine();
			// If current line is idle, there is no process incoming
			if(!input.equals("idle")) {
				burstTime[i]=Integer.parseInt(input);
				totalIncomingProcesses++;
			} else {
				burstTime[i]=0;
			}
			i++;
		}
	    totalClocks=i;
	    filereader.close();
	}

}