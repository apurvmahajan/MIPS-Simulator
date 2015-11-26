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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class MIPSsim {
	private static int LO = 0;
	private static int HI = 0;
	
	private static boolean ifWait = false;			//True = Waiting || False = Executed
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
	
	//private static boolean[] clearBuf = new boolean[13];
	
	private static ArrayList<String> writeBack = new ArrayList<String>(8);
	private static HashSet<Integer> regWrite = new HashSet<Integer>();
	private static HashSet<Integer> regRead = new HashSet<Integer>();

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
				if(buf12 != null){
					writeBack.add(buf12);
					//executeInstruction(buf12, true);
					buf12 = null;
				}
				if(buf11 != null){
					buf12 = buf11;
					buf11 = null;
				}
				if(buf10 != null){
					writeBack.add(buf10);
					//executeInstruction(buf10, true);
					buf10 = null;						
				}
				if(buf9 != null){
					writeBack.add(buf9);
					//executeInstruction(buf9, true);
					buf9 = null;					
				}
				if(buf8 != null){
					buf11 = buf8;
					buf8 = null;						
				}
				if(buf7 != null){
					divCount++;
					if(divCount == 4){
						writeBack.add(buf12);
						//executeInstruction(buf7, true);
						buf7 = null;							
					}
				}
				if(buf6 != null){
					buf10 = buf6;
					buf6 = null;						
				}
				if(!buf5.isEmpty()){
					buf9 = buf5.get(0);
					buf5.remove(0);			
				}
				if(!buf4.isEmpty()){
					buf8 = buf4.get(0);
					buf4.remove(0);
				}				
				if(!buf3.isEmpty()){
					if(buf7 == null){
						divCount = 0;
						buf7 = buf3.get(0);
						buf3.remove(0);
					}
				}
				if(!buf2.isEmpty()){
					buf6 = buf2.get(0);
					buf2.remove(0);					
				}
				if(!buf1.isEmpty()){
					Iterator<String> it = buf1.iterator();
					while (it.hasNext()) {
						String bufInstr = it.next();
						String instrType = bufInstr.substring(0, 6);
						/*if (!bufInstr.substring(0, 4).equals("0000") && !executeInstruction(nextInstr, false))
							break;*/
						if(instrType.equals("000100") || instrType.equals("000101")){
							//if(buf2.size() < 2 && flag2 == false){
							if(buf2.size() < 2){
								//flag2 = true;
								buf2.add(bufInstr);
								it.remove();
							}
						}
						else if(instrType.equals("011001")){
							if(buf3.size() < 2){
								buf3.add(bufInstr);
								//flag3 = true;
								it.remove();
							}
						}
						else if(instrType.equals("011000")){
							if(buf4.size() < 2){
								buf4.add(bufInstr);
								//flag4 = true;
								it.remove();
							}
						}
						else {
							if(buf5.size() < 2){
								buf5.add(bufInstr);
								//flag5 = true;
								it.remove();
							}							
						}
					}
				}
				if(instrFetch != null && regRead.size() == 0 && regWrite.size() == 0){
					posCntr = processCategoryJumps(posCntr, instrFetch);
					System.out.println("Category Jump=>> " + instrFetch + "\tposCntr: " + posCntr + "\tnextPosCntr: " + nextPosCntr);
					if(posCntr == -1){
						posCntr = nextPosCntr;
						ifWait = true;	
					}
					else {
						ifWait = false;
						instrFetch = null;
						if (posCntr == nextPosCntr)
							nextPosCntr += 4;
						else
							nextPosCntr = posCntr;
					}
					continue;
				}
				ifCount = 0;
				while(ifCount < 4 && buf1.size() < 8 && ifWait == false){
					posCntr = nextPosCntr;
					nextInstr = hm.get(posCntr);
					System.out.println(posCntr + "\t\t" + nextInstr + "\t\t" + disassembleInstruction(nextInstr));
					if(nextInstr.compareTo("00011000000000000000000000000000") == 0){
						nextPosCntr = dataCntr;
						break;
					}
					if(nextInstr.substring(0, 4).equals("0000")){
						instrFetch = nextInstr;
						ifWait = true;
						break;
					}
					buf1.add(nextInstr);
					executeInstruction(nextInstr, false);
					ifCount++;
					if (posCntr == nextPosCntr) 
						nextPosCntr += 4;
				}
				if(!writeBack.isEmpty()){
					Iterator<String> wb = writeBack.iterator();
					while(wb.hasNext()){
						String s = wb.next();
						executeInstruction(s, true);
					}
				}
				//**************** End Execution Cycle ****************
				System.out.println(posCntr + "\t\t" + nextInstr + "\t\t" + disassembleInstruction(nextInstr));
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
					dataMap.put((offset >> 2) + regArr[src1], regArr[src2]);
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2))
						return false;
				}
				break;
			case "101":
				// LW
				if (execute){
					regArr[src2] = dataMap.get((offset >> 2)+ regArr[src1]);
					regWrite.remove(src2);
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2))
						return false;
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
					if (regWrite.contains(src1) ||
							regWrite.contains(dest) || regRead.contains(dest))
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
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2))
						return false;
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
				}
				else {
					if (regWrite.contains(src1) || regWrite.contains(src2))
						return false;
					regRead.add(src1);
					regRead.add(src2);
				}
				break;
		}
		return true;
	}

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
					HI = -1;
					regWrite.remove(dest);
				}
				else {
					if (regRead.contains(dest) || regWrite.contains(dest))
						return false;
					regWrite.add(dest);
				}
				break;
			case "001":
				//MFLO
				if (execute){
					regArr[dest] = LO; 
					LO = -1;
					regWrite.remove(dest);
				}
				else {
					if (regRead.contains(dest) || regWrite.contains(dest))
						return false;
					regWrite.add(dest);
				}
				break;
		}
		return true;
	}


//****************************** Print the simulation.txt output file *******************************
	private static void printSimulationState(BufferedWriter bw) {
		cycle++;
		if(cycle > 100)
			System.exit(0);
		//System.out.println("Cycle:  " + cycle);
		try {
			bw.write("--------------------");
			bw.write("\nCycle " + cycle + ":");
			bw.write("\n\nIF:");
			if(ifWait){
				bw.write("\n\tWaiting: [" + disassembleInstruction(instrFetch) + "]");				
				bw.write("\n\tExecuted: ");
			}
			else {
				bw.write("\n\tWaiting: ");
				if (instrFetch != null){
					bw.write("\n\tExecuted: [" + disassembleInstruction(instrFetch) + "]");
				}
				else {
					bw.write("\n\tExecuted: ");
				}
			}
			//******************************
			bw.write("\nBuf1:");
			int loop = 0;
			for(loop=0; loop<buf1.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf1.get(loop)) + "]");
			}
			while(loop<8){
				bw.write("\n\tEntry " + loop++ + ": ");
			}
			//******************************
			bw.write("\nBuf2:");
			for(loop=0; loop<buf2.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf2.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ": ");
			}
			//******************************
			bw.write("\nBuf3:");
			int loop2 = 0;
			for(loop=0; loop<buf3.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf3.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ": ");
			}
			//******************************
			bw.write("\nBuf4:");
			for(loop=0; loop<buf4.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf4.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ": ");
			}
			//******************************
			bw.write("\nBuf5:");
			for(loop=0; loop<buf5.size(); loop++){
				bw.write("\n\tEntry " + loop + ": [" + disassembleInstruction(buf5.get(loop)) + "]");
			}
			while(loop<2){
				bw.write("\n\tEntry " + loop++ + ": ");
			}
			bw.write("\nBuf6: ");
			if(buf6 != null)
				bw.write("[" + disassembleInstruction(buf6) + "]");
			bw.write("\nBuf7: ");
			if(buf7 != null)
				bw.write("[" + disassembleInstruction(buf7) + "]");
			bw.write("\nBuf8: ");
			if(buf8 != null)
				bw.write("[" + disassembleInstruction(buf8) + "]");
			bw.write("\nBuf9: ");
			if(buf9 != null)
				bw.write("[" + disassembleInstruction(buf9) + "]");
			bw.write("\nBuf10: ");
			if(buf10 != null)
				bw.write("[" + disassembleInstruction(buf10) + "]");
			bw.write("\nBuf11: ");
			if(buf11 != null)
				bw.write("[" + disassembleInstruction(buf11) + "]");
			bw.write("\nBuf12: ");
			if(buf12 != null)
				bw.write("[" + disassembleInstruction(buf12) + "]");
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
			bw.write("\nRead : ");	
			bw.write(regRead.toString());
			bw.write("\nWrite: ");
			bw.write(regWrite.toString());
			bw.write("\n");
		}
		catch (IOException e){
			e.printStackTrace();
		}
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
