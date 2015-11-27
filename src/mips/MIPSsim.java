//
//	On my honor, I have neither given nor received unauthorized aid on this assignment
//
//	Name: Apurv Mahajan
//	Project 1 - MIPS Simulator
//	
//

package mips;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class MIPSsim {
	private static int LO = 0;
	private static int HI = 0;
	private static boolean loUsed = false;
	private static boolean hiUsed = false;
	private static boolean mulIssued = false;
	private static boolean divIssued = false;
	private static boolean ifWait = false;			//True = Waiting || False = Executed
	private static boolean ifExecuted = false;
	private static String instrFetch = null;
	
	private static ArrayList<String> buf1 = new ArrayList<String>(8);
	private static ArrayList<String> buf2 = new ArrayList<String>(2);
	private static ArrayList<String> buf3 = new ArrayList<String>(2);
	private static ArrayList<String> buf4 = new ArrayList<String>(2);
	private static ArrayList<String> buf5 = new ArrayList<String>(2);
	
	private static String buf6 = null;
	private static String buf7 = null;
	private static String buf8 = null;
	private static String buf9 = null;
	private static String buf10 = null;
	private static String buf11 = null;
	private static String buf12 = null;
	private static String divInstr = null;
	private static String loadMem = null;
	//private static boolean[] clearBuf = new boolean[13];
	
	private static ArrayList<String> writeBack = new ArrayList<String>(8);
	private static HashSet<Integer> regWrite = new HashSet<Integer>();
	private static HashSet<Integer> regRead = new HashSet<Integer>();
	private static HashSet<Integer> regOrder = new HashSet<Integer>();

	private static boolean instrFlag = true;
	private static int cycle = 0;
	private static int dataCntr = -1;
	private static int nextPosCntr = 256;		// For keeping track of branch instructions
	private static int[] regArr = new int[32];
	private static HashMap<Integer, String> hm = new HashMap<Integer, String>();
	private static HashMap<Integer, Integer> dataMap =  new HashMap<Integer, Integer>();
	
	public static void main(String args[]){
		long data;
		String nextInstr = null;
		String fileName = args[0];
		File file = new File("");
		String currentDirectory = file.getAbsolutePath();
		BufferedReader br = null;
		try {
			//********* Create output files *********
			File disassemblyFile = new File("disassembly.txt");
			File simulationFile = new File("simulation.txt");
			if (!disassemblyFile.exists()) {
				disassemblyFile.createNewFile();
			}
			if (!simulationFile.exists()) {
				simulationFile.createNewFile();
			}
			br = new BufferedReader(new FileReader(currentDirectory + "/" + fileName));
			FileWriter fw1 = new FileWriter(disassemblyFile.getAbsoluteFile());
			BufferedWriter bw1 = new BufferedWriter(fw1);
			
			//********* Save data and instructions in HashMap *********
			
			int posCntr = 256;
			while((nextInstr = br.readLine()) != null){
				hm.put(posCntr, nextInstr);
				posCntr += 4;
			}
			posCntr = 256;
			
			
			//********* Disassemble the input file (for disassembly.txt output) *********
			
			
			while(hm.containsKey(posCntr)){
				nextInstr = hm.get(posCntr);
				if(instrFlag){
					bw1.write(nextInstr + "\t" + posCntr + "\t" + disassembleInstruction(nextInstr) + "\n");
					if (nextInstr.compareTo("00011000000000000000000000000000") == 0)
						instrFlag = false;
					}
				else {
					//********* Read input data *********
					if (dataCntr == -1)
						dataCntr = posCntr;
					data = Long.parseLong(nextInstr, 2);
					if (data > 2147483647)
						data = data - 4294967296l;
					dataMap.put(posCntr, (int) data);
					bw1.write(nextInstr +"\t" + posCntr +"\t" + data + "\n");
					}
				posCntr += 4;
			}
			bw1.close();
			
			
			//********* Simulate instruction execution (for simulation.txt output) *********
			
			
			int divCount = 0;
			int ifCount = 0;
			FileWriter fw2 = new FileWriter(simulationFile.getAbsoluteFile());
			BufferedWriter bw2 = new BufferedWriter(fw2);
			nextPosCntr = posCntr = 256;
			
			//**************** Start Execution Cycle ****************
			while(nextPosCntr < dataCntr){
				boolean bufFull2 = false;
				boolean bufFull3 = false;
				boolean bufFull4 = false;
				boolean bufFull5 = false;
				regOrder.clear();
				if(buf12 != null){
					writeBack.add(buf12);
					loUsed = false;
					buf12 = null;
				}
				if(buf11 != null){
					buf12 = buf11;
					buf11 = null;
				}
				if(buf10 != null){
					loadMem = buf10;
					buf10 = null;	
				}
				if(loadMem != null){
					writeBack.add(loadMem);
					loadMem = null;
				}
				if(buf9 != null){
					writeBack.add(buf9);
					buf9 = null;					
				}
				if(buf8 != null){
					buf11 = buf8;
					buf8 = null;						
				}
				if(buf7 != null){
					writeBack.add(buf7);
					buf7 = null;						
				}
				if(divInstr != null){
					divCount++;
					if(divCount == 4){
						buf7 = divInstr;
						divInstr = null;
					}
				}
				if(buf6 != null){
					//System.out.println("Load/Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
					if(buf6.substring(0, 6).equals("000100")){
						//System.out.println("Store:: " + disassembleInstruction(buf6) + "\t" + buf6);
						writeBack.add(buf6);
					}
					else if (buf6.substring(0, 6).equals("000101")){
						//System.out.println("Load:: " + disassembleInstruction(buf6) + "\t" + buf6);
						buf10 = buf6;
					}
					buf6 = null;						
				}
				if(!buf5.isEmpty()){
					if(buf5.size() == 2)
						bufFull5 = true;
					buf9 = buf5.get(0);
					buf5.remove(0);			
				}
				if(!buf4.isEmpty()){
					if(buf4.size() == 2)
						bufFull4 = true;
					buf8 = buf4.get(0);
					buf4.remove(0);
				}				
				if(!buf3.isEmpty() && divInstr == null){
					if(buf3.size() == 2)
						bufFull3 = true;
					divInstr = buf3.get(0);
					divCount = 0;
					buf3.remove(0);
					loUsed = false;
				}
				if(!buf2.isEmpty()){
					if(buf2.size() == 2)
						bufFull2 = true;
					buf6 = buf2.get(0);
					buf2.remove(0);					
				}
				if(!buf1.isEmpty()){
					Iterator<String> it = buf1.iterator();
					while (it.hasNext()) {
						String bufInstr = it.next();
						String instrType = bufInstr.substring(0, 6);
						if(instrType.equals("000100") || instrType.equals("000101")){
							// ALU2 Unit
							if(!bufFull2 && buf2.size() < 2 && executeInstruction(bufInstr, false)){
								buf2.add(bufInstr);
								it.remove();
							}
						}
						else if(instrType.equals("011001")){
							// Div Instruction
							divIssued = true;
							if(!bufFull3 && buf3.size() < 2 && loUsed == false && executeInstruction(bufInstr, false)){
								loUsed = true;
								hiUsed = true;
								buf3.add(bufInstr);
								it.remove();
							}
						}
						else if(instrType.equals("011000")){
							// Mult Instruction
							mulIssued = true;
							if(!bufFull4 && buf4.size() < 2 && loUsed == false && executeInstruction(bufInstr, false)){
								loUsed = true;
								buf4.add(bufInstr);
								it.remove();
							}
						}
						else {
							// ALU1 Unit
							if(!bufFull5 && buf5.size() < 2 && executeInstruction(bufInstr, false)){
								buf5.add(bufInstr);
								it.remove();
							}							
						}
					}
				}
				if(instrFetch != null && instrFetch.substring(0, 6).equals("000000")){
					//System.out.println("JUMP" + posCntr);
					instrFetch = null;
					ifExecuted = false;
					ifWait = false;
				}
				if(instrFetch != null){
					/*String bits = instrFetch.substring(6);
					String opCode = instrFetch.substring(3, 6);
					int src1 = Integer.parseInt(bits.substring(0, 5), 2);
					int src2 = Integer.parseInt(bits.substring(5, 10), 2);
					int offset = Integer.parseInt(bits.substring(10), 2) << 2;
					if ((opCode.equals("011") && !regRead.contains(src1))
							|| (!opCode.equals("011") && !regRead.contains(src1) && !regRead.contains(src2)){
						
					}*/
					if (regRead.size() == 0 && regWrite.size() == 0) {
						posCntr = processCategoryJumps(posCntr, instrFetch);
						//System.out.println("Category Jump:: " + instrFetch + "\tposCntr: " + posCntr + "\tnextPosCntr: " + nextPosCntr);
						if(posCntr == -1){
							posCntr = nextPosCntr;
							ifWait = true;	
						}
						else {
							if(ifExecuted){
								ifWait = false;
								instrFetch = null;
								if (posCntr == nextPosCntr)
									nextPosCntr += 4;
								else
									nextPosCntr = posCntr;
							}
							else {
								posCntr = nextPosCntr;
								ifExecuted = true;
							}
						}
					}
				}
				ifCount = 0;
				while(ifCount < 4 && buf1.size() < 8 && ifWait == false){
					posCntr = nextPosCntr;
					nextInstr = hm.get(posCntr);
					//System.out.println(posCntr + "\t\t" + nextInstr + "\t\t" + disassembleInstruction(nextInstr));
					if(nextInstr.compareTo("00011000000000000000000000000000") == 0){
						instrFetch = nextInstr;
						ifWait = true;
						ifExecuted = true;
						nextPosCntr = dataCntr;
						break;
					}
					if(nextInstr.substring(0, 4).equals("0000")){
						instrFetch = nextInstr;
						ifWait = true;
						if(instrFetch.substring(0, 6).equals("000000")){
							//System.out.println("JUMP");
							nextPosCntr = processCategoryJumps(posCntr, instrFetch);
							ifExecuted = true;
						}
						else
							ifExecuted = false;
						break;
					}
					buf1.add(nextInstr);
					ifCount++;
					if (posCntr == nextPosCntr) 
						nextPosCntr += 4;
				}
				if(!writeBack.isEmpty()){
					Iterator<String> wb = writeBack.iterator();
					while(wb.hasNext()){
						String s = wb.next();
						executeInstruction(s, true);
						wb.remove();
					}
				}
				//**************** End Execution Cycle ****************
				//System.out.println(posCntr + "\t\t" + nextInstr + "\t\t" + disassembleInstruction(nextInstr));
				printSimulationState(bw2);
			}
			bw2.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		finally {
			try {
				if (br!=null) 
					br.close();
			} 
			catch (IOException e){
				e.printStackTrace();
			}
		}
		// END of main function
	}	

	
//****************************** Execute Instructions ******************************
	private static boolean executeInstruction(String nextInstr, boolean execute) {
		// TODO Auto-generated method stub
		String instrCategory = nextInstr.substring(0, 3);
		switch (instrCategory) {
			case "000":		//********* Category 1
				return processCategoryOne(nextInstr, execute);
			case "001":		//********* Category 2
				return processCategoryTwo(nextInstr, execute);
			case "010":		//********* Category 3
				return processCategoryThree(nextInstr, execute);
			case "011":		//********* Category 4
				return processCategoryFour(nextInstr, execute);				
			case "100":		//********* Category 5
				return processCategoryFive(nextInstr, execute);										
			default:		//********* Input Error 
				//********* should not execute (errors not handled) *********
				System.out.println(nextInstr + " : Invalid Instruction");
				return false;	//Exit program		
		}
	}


//****************************** Category Jumps ******************************
	private static int processCategoryJumps(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-1
		String opCode = mipsInstr.substring(3, 6);
		String bits = mipsInstr.substring(6);
		//int data = 0;
		int src1 = Integer.parseInt(bits.substring(0, 5), 2);
		int src2 = Integer.parseInt(bits.substring(5, 10), 2);
		int offset = Integer.parseInt(bits.substring(10), 2) << 2;
		switch (opCode) {
			case "000":
				// J	[JUMP]
				posCntr = (posCntr+4) & 0xf0000000;
				posCntr |= Integer.parseInt(bits, 2) << 2;				
				break;
			case "001":
				// BEQ
				if(regWrite.contains(src1) || regWrite.contains(src2))
					return -1;
				if (regArr[src1] == regArr[src2]){
					posCntr = posCntr + 4 + offset;
				}
				break;
			case "010":
				// BNE
				if(regWrite.contains(src1) || regWrite.contains(src2))
					return -1;
				if (regArr[src1] != regArr[src2]){
					posCntr = posCntr + 4 + offset;
				}
				break;
			case "011":
				// BGTZ
				if(regWrite.contains(src1))
					return -1;
				if (regArr[src1] > 0){
					posCntr = posCntr + 4 + offset;
				}
				break;
		}
		return posCntr;
	}	
	

//****************************** Category One ******************************
	private static boolean processCategoryOne(String mipsInstr, boolean execute) {
		// Format of Instructions in Category-1
		String opCode = mipsInstr.substring(3, 6);
		String bits = mipsInstr.substring(6);
		//int data = 0;
		int src1 = Integer.parseInt(bits.substring(0, 5), 2);
		int src2 = Integer.parseInt(bits.substring(5, 10), 2);
		int offset = Integer.parseInt(bits.substring(10), 2) << 2;
		switch (opCode) {
			case "100":
				// SW
				if (execute){
					//System.out.println("Store -- DataMap: " + ((offset >> 2) + regArr[src1]) + "\toffset " + src1 + "\t" + regArr[src1] + "\tReg " + src2 + "\tRegVal " + regArr[src2]);
					dataMap.put((offset >> 2) + regArr[src1], regArr[src2]);
					regRead.remove(src1);
					regRead.remove(src2);
				}
				else {
					regOrder.add(src1);
					regOrder.add(src2);
					if (regWrite.contains(src1) || regWrite.contains(src2))
						return false;
					regRead.add(src1);
					regRead.add(src2);
				}
				break;
			case "101":
				// LW
				if (execute){
					regArr[src2] = dataMap.get((offset >> 2)+ regArr[src1]);
					//System.out.println("Load:: "+ mipsInstr + "\t" + src2 + "\t" + regArr[src2] + "\t" + ((offset >> 2) + regArr[src1]));
					regRead.remove(src1);
					regWrite.remove(src2);
				}
				else {
					if (regRead.contains(src2) || regWrite.contains(src2))
						return false;
					regRead.add(src1);
					regWrite.add(src2);
					}
				break;
		}
		return true;
	}

	
//****************************** Category Two ******************************
	private static boolean processCategoryTwo(String mipsInstr, boolean execute) {
		// Format of Instructions in Category-2
		String opCode = mipsInstr.substring(3, 6);
		int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
		int src2 = Integer.parseInt(mipsInstr.substring(16, 21), 2);
		switch (opCode) {
			case "000":
				//ADD
				if (execute){
					regArr[dest] = regArr[src1] + regArr[src2];
					regRead.remove(src1);
					regRead.remove(src2);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2) || 
								regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regRead.add(src2);
					regWrite.add(dest);
				}
				break;
			case "001":
				//SUB
				if (execute){
					regArr[dest] = regArr[src1] - regArr[src2];
					regRead.remove(src1);
					regRead.remove(src2);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2) || 
							regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regRead.add(src2);
					regWrite.add(dest);
				}
				break;
			case "010":
				//AND
				if (execute){
					regArr[dest] = regArr[src1] & regArr[src2];
					regRead.remove(src1);
					regRead.remove(src2);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2) || 
							regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regRead.add(src2);
					regWrite.add(dest);
				}
				break;
			case "011":
				//OR
				if (execute){
					regArr[dest] = regArr[src1] | regArr[src2];
					regRead.remove(src1);
					regRead.remove(src2);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2) || 
							regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regRead.add(src2);
					regWrite.add(dest);
				}
				break;
			case "100":
				//SRL
				if (execute){
					regArr[dest] = regArr[src1] >>> 2;
					regRead.remove(src1);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) || 
							regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regWrite.add(dest);
				}
				break;
			case "101":
				//SRA
				if (execute){
					regArr[dest] = regArr[src1] >> 2;
					regRead.remove(src1);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) ||
							regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regWrite.add(dest);
				}
				break;
		}
		return true;
	}

	
//****************************** Category Three ******************************
	private static boolean processCategoryThree(String mipsInstr, boolean execute) {
		// Format of Instructions in Category-3
		String opCode = mipsInstr.substring(3, 6);
		int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
		int data = Integer.parseInt(mipsInstr.substring(16), 2);
		switch (opCode) {
			case "000":
				//ADDI
				if (execute){
					regArr[dest] = regArr[src1] + data;
					regRead.remove(src1);
					regWrite.remove(dest);
				}
				else {
					if(regOrder.contains(src1) || regOrder.contains(dest))
						return false;
					if (regWrite.contains(src1) || regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regWrite.add(dest);
				}
				break;
			case "001":
				//ANDI
				if (execute){
					regArr[dest] = regArr[src1] & data;
					regRead.remove(src1);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) ||
							regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regWrite.add(dest);
				}
				break;
			case "010":
				//ORI
				if (execute){
					regArr[dest] = regArr[src1] | data;
					regRead.remove(src1);
					regWrite.remove(dest);
				}
				else {
					if (regWrite.contains(src1) ||
							regWrite.contains(dest) || regRead.contains(dest))
						return false;
					regRead.add(src1);
					regWrite.add(dest);
				}
				break;
		}
		return true;
	}

	
//****************************** Category Four ******************************
	private static boolean processCategoryFour(String mipsInstr, boolean execute) {
		// Format of Instructions in Category-4
		String opCode = mipsInstr.substring(3, 6);
		int src1 = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		int src2 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
		//String data = "#" + Integer.parseInt(mipsInstr.substring(16), 2);
		String instrName = "";
		switch (opCode) {
			case "000":
				//MULT
				if (execute){
					LO = regArr[src1] * regArr[src2];
					regRead.remove(src1);
					regRead.remove(src2);
					loUsed = false;
					mulIssued = false;
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2) || loUsed == true)
						return false;
					loUsed = true;
					regRead.add(src1);
					regRead.add(src2);
				}
				break;
			case "001":
				//DIV
				if (execute){
					LO = regArr[src1] / regArr[src2];
					HI = regArr[src1] % regArr[src2];
					regRead.remove(src1);
					regRead.remove(src2);
					loUsed = false;
					hiUsed = false;
					divIssued = false;
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2) || loUsed == true || hiUsed == true)
						return false;
					loUsed = true;
					hiUsed = true;
					regRead.add(src1);
					regRead.add(src2);
				}
				break;
		}
		return true;
	}

	
//****************************** Category Five ******************************
//****************************** Category Five ******************************	
	private static boolean processCategoryFive(String mipsInstr, boolean execute) {
		// Format of Instructions in Category-5
		String opCode = mipsInstr.substring(3, 6);
		int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		switch (opCode) {
			case "000":
				//MFHI
				if (execute){
					regArr[dest] = HI; 
					//HI = 0;
					hiUsed = false;
					regWrite.remove(dest);
				}
				else {
					if (regRead.contains(dest) || regWrite.contains(dest) || hiUsed == true
							  || divIssued == true)
						return false;
					regWrite.add(dest);
					hiUsed = true;
				}
				break;
			case "001":
				//MFLO
				if (execute){
					//System.out.println("MFLO:: "+ mipsInstr + "\t" + dest + "\tLO Value: " + LO);
					regArr[dest] = LO; 
					//LO = 0;
					loUsed = false;
					regWrite.remove(dest);
				}
				else {
					if (regRead.contains(dest) || regWrite.contains(dest) || loUsed == true 
							|| mulIssued == true  || divIssued == true)
						return false;
					regWrite.add(dest);
					loUsed = true;
				}
				break;
		}
		return true;
	}


//****************************** Print the simulation.txt output file *******************************
	
	
//****************************** Print Simulation State ******************************
	private static void printSimulationState(BufferedWriter bw) {
		cycle++;
		//if(cycle > 100)
		//	System.exit(0);
		//System.out.println("Cycle:  " + cycle);
		try {
			bw.write("--------------------");
			bw.write("\nCycle " + cycle + ":");
			bw.write("\n\nIF:");
			if(!ifWait){
				bw.write("\n\tWaiting:");			
				bw.write("\n\tExecuted:");
			}
			else {
				if (ifExecuted){
					bw.write("\n\tWaiting:");
					bw.write("\n\tExecuted: [" + disassembleInstruction(instrFetch) + "]");
				}
				else {
					bw.write("\n\tWaiting: [" + disassembleInstruction(instrFetch) + "]");	
					bw.write("\n\tExecuted:");
					
				}
			}
			//******************************
			bw.write("\nBuf1:");
			int loop = 0;
			for(loop=0; loop<buf1.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf1.get(loop)) + "]");
			}
			while(loop<8){
				bw.write("\n\tEntry " + loop++ + ":");
			}
			//******************************
			bw.write("\nBuf2:");
			for(loop=0; loop<buf2.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf2.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ":");
			}
			//******************************
			bw.write("\nBuf3:");
			int loop2 = 0;
			for(loop=0; loop<buf3.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf3.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ":");
			}
			//******************************
			bw.write("\nBuf4:");
			for(loop=0; loop<buf4.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf4.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ":");
			}
			//******************************
			bw.write("\nBuf5:");
			for(loop=0; loop<buf5.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf5.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ":");
			}
			bw.write("\nBuf6:");
			if(buf6 != null)
				bw.write(" [" + disassembleInstruction(buf6) + "]");
			bw.write("\nBuf7:");
			if(buf7 != null){
				int src1 = Integer.parseInt(buf12.substring(6, 11), 2);
				int src2 = Integer.parseInt(buf12.substring(11, 16), 2);
				bw.write(" [" + (regArr[src1] % regArr[src2]) + ", " + (regArr[src1] / regArr[src2]) + "]");
			}
				
			bw.write("\nBuf8:");
			if(buf8 != null)
				bw.write(" [" + disassembleInstruction(buf8) + "]");
			bw.write("\nBuf9:");
			if(buf9 != null){
				bw.write(" [" + printBuf9(buf9) + "]");
			}
				
			bw.write("\nBuf10:");
			if(buf10 != null){
				String bits = buf10.substring(6);
				int src1 = Integer.parseInt(bits.substring(0, 5), 2);
				int src2 = Integer.parseInt(bits.substring(5, 10), 2);
				int offset = Integer.parseInt(bits.substring(10), 2) << 2;
				regArr[src2] = dataMap.get((offset >> 2)+ regArr[src1]);
				bw.write(" [" + dataMap.get((offset >> 2)+ regArr[src1])+ ", R" + src2 + "]");
			}
				
			bw.write("\nBuf11:");
			if(buf11 != null)
				bw.write(" [" + disassembleInstruction(buf11) + "]");
			bw.write("\nBuf12:");
			if(buf12 != null){
				int src1 = Integer.parseInt(buf12.substring(6, 11), 2);
				int src2 = Integer.parseInt(buf12.substring(11, 16), 2);
				bw.write(" [" + (regArr[src1] * regArr[src2]) + "]");
			}
			bw.write("\n\nRegisters");
			for(int i=0; i<32; i++){
				if (i%8 == 0){
					bw.write("\n");
					bw.write(String.format("R%02d:", i));
				}
				bw.write("\t" + regArr[i]);
			}
			bw.write("\nHI:\t" + HI);
			bw.write("\nLO:\t" + LO);			
			bw.write("\n\n");
			bw.write("Data");
			int dataReg = dataCntr;
			for(int i=0; i<16; i++){
				if (i%8 == 0){
					bw.write("\n");
					bw.write(String.format("%03d:", dataReg));
				}
				bw.write("\t" + dataMap.get(dataReg));
				dataReg += 4;
			}
			//bw.write("\nRead : " + regRead.toString());	
			//bw.write("\nWrite: " + regWrite.toString());
			bw.write("\n");
			bw.flush();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}	
	
	
//****************************** Print Buffer 9 State ******************************
	private static String printBuf9(String buf9) {
		String result = "";
		if(buf9.substring(0, 3).equals("001")){
			String opCode = buf9.substring(3, 6);
			int dest = Integer.parseInt(buf9.substring(6, 11), 2);
			int src1 = Integer.parseInt(buf9.substring(11, 16), 2);
			int src2 = Integer.parseInt(buf9.substring(16, 21), 2);
			switch (opCode) {
				case "000":
					//ADD
					result += (regArr[src1] + regArr[src2]);
					break;
				case "001":
					//SUB
					result += (regArr[src1] - regArr[src2]);
					break;
				case "010":
					//AND
					result += (regArr[src1] & regArr[src2]);
					break;
				case "011":
					//OR
					result += (regArr[src1] | regArr[src2]);
					break;
				case "100":
					//SRL
					result += (regArr[src1] >>> 2);
					break;
				case "101":
					//SRA
					result += (regArr[src1] >> 2);
					break;
			}
			result += ", R" + dest;
		}
		else if(buf9.substring(0, 3).equals("010")) {
			String opCode = buf9.substring(3, 6);
			int dest = Integer.parseInt(buf9.substring(6, 11), 2);
			int src1 = Integer.parseInt(buf9.substring(11, 16), 2);
			int data = Integer.parseInt(buf9.substring(16), 2);
			switch (opCode) {
				case "000":
					//ADDI
					result += (regArr[src1] + data);
					break;
				case "001":
					//ANDI
					result += (regArr[src1] & data);
					break;
				case "010":
					//ORI
					result += (regArr[src1] | data);
					break;
			}
			result += ", R" + dest;			
		}
		else {
			String opCode = buf9.substring(3, 6);
			int dest = Integer.parseInt(buf9.substring(6, 11), 2);
			switch (opCode) {
				case "000":
					//MFHI
					result += (HI);
					break;
				case "001":
					//MFLO
					result += (LO);
					break;
			}
			result += ", R" + dest;
		}
		return result;
	}


//****************************** Print the disassembly.txt output file ******************************
	private static String disassembleInstruction(String mipsInstr) {
		String instrCategory = mipsInstr.substring(0, 3);
		String opCode = mipsInstr.substring(3, 6);
		String instrName = "";

		String dest;
		String src1;
		String src2;
		String data;
		String bits;
		int offset;
		
		switch (instrCategory) {
			case "000":		//Category 1
				bits = mipsInstr.substring(6);	
				src1 = "R" + Integer.parseInt(bits.substring(0, 5), 2);
				src2 = "R" + Integer.parseInt(bits.substring(5, 10), 2);
				offset = Integer.parseInt(bits.substring(10), 2) << 2;
				switch (opCode) {
					case "000":
						instrName = "J ";
						int temp = (nextPosCntr+4) & 0xf0000000;
						temp |= Integer.parseInt(bits, 2) << 2;
						instrName += "#" + temp;
						break;
					case "001":
						instrName = "BEQ ";
						instrName += src1 + ", " + src2 + ", #" + offset;
						break;
					case "010":
						instrName = "BNE ";
						instrName += src1 + ", " + src2 + ", #" + offset;
						break;
					case "011":
						instrName = "BGTZ ";
						instrName += src1 + ", #" + offset;
						break;
					case "100":
						instrName = "SW ";
						instrName += src2 + ", " + (offset >> 2) + "(" + src1 + ")";
						break;
					case "101":
						instrName = "LW ";
						instrName += src2 + ", " + (offset >> 2)+ "(" + src1 + ")";
						break;
					case "110":
						instrName = "BREAK";
						instrFlag = false;
						break;
				}			
				break;	
			case "001":		//Category 2
				dest = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
				src1 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
				src2 = "R" + Integer.parseInt(mipsInstr.substring(16, 21), 2);
				bits = mipsInstr.substring(21);
				switch (opCode) {
					case "000":
						instrName += "ADD ";
						break;
					case "001":
						instrName += "SUB ";
						break;
					case "010":
						instrName += "AND ";
						break;
					case "011":
						instrName += "OR ";
						break;
					case "100":
						instrName += "SRL ";
						src2 = "" + Integer.parseInt(mipsInstr.substring(16, 21), 2);
						instrName += dest + ", " + src1 + ", #" + src2;
						return instrName;
					case "101":
						instrName += "SRA ";
						src2 = "" + Integer.parseInt(mipsInstr.substring(16, 21), 2);
						instrName += dest + ", " + src1 + ", #" + src2;
						return instrName;
				}
				instrName += dest + ", " + src1 + ", " + src2;
				break;
			case "010":		//Category 3
				dest = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
				src1 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
				data = "#" + Integer.parseInt(mipsInstr.substring(16), 2);
				switch (opCode) {
					case "000":
						instrName = "ADDI ";
						break;
					case "001":
						instrName = "ANDI ";
						break;
					case "010":
						instrName = "ORI ";
						break;
				}
				instrName += dest + ", " + src1 + ", " + data;
				break;
			case "011":		//Category 4
				src1 = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
				src2 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
				switch (opCode) {
					case "000":
						instrName = "MULT ";
						break;
					case "001":
						instrName = "DIV ";
						break;
				}	
				instrName += src1 + ", " + src2;
				break;					
			case "100":		//Category 5
				dest = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
				bits = mipsInstr.substring(11);
				switch (opCode) {
					case "000":
						instrName = "MFHI ";
						break;
					case "001":
						instrName = "MFLO ";
						break;
				}				
				instrName += dest;
				break;									
		}
		return instrName;
	}	
	
//END PROGRAM
}
