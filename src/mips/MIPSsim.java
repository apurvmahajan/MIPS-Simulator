//
//	“On my honor, I have neither given nor received unauthorized aid on this assignment
//
//	Name: Apurv Mahajan
//	Project 1 - MIPS Simulator
//	
//

package mips;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MIPSsim {
	static int LO = -1;
	static int HI = -1;
	
	static int d = -1;
	static int dataCntr = -1;
	static int cycle = 0;
	static boolean instrFlag = true;
	//static int[] dataArr = new int[1000];
	static int[] regArr = new int[1000];
	static HashMap<Integer, String> hm = new HashMap<Integer, String>();
	static HashMap<Integer, Integer> dataMap =  new HashMap<Integer, Integer>();
	
	public static void main(String args[]){
		long data;
		String nextInstr;
		String instrCategory;
		String fileName = args[0];
		File file = new File("");
		String currentDirectory = file.getAbsolutePath();
		//Create output files
		File disassemblyFile = new File("disassembly.txt");
		File simulationFile = new File("simulation.txt");		
		
		//Read input data
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(currentDirectory + "\\" + fileName));
			//Save data and instructions in HashMap
			int posCntr = 256;
			while((nextInstr = br.readLine()) != null){
				hm.put(posCntr, nextInstr);
				posCntr += 4;
			}
			posCntr = 256;
			//Disassemble the input file
			while(hm.containsKey(posCntr)){
				nextInstr = hm.get(posCntr);
				if(instrFlag){
					System.out.println(nextInstr + "\t" + posCntr + "\t" + disassembleInstruction(posCntr, nextInstr));
					if (nextInstr.compareTo("00011000000000000000000000000000") == 0)
						instrFlag = false;
					}
				else {
					data = Long.parseLong(nextInstr, 2);
					if (data > 2147483647)
						data = data - 4294967296l;
					dataMap.put(posCntr, (int) data);
					System.out.println(nextInstr +"\t" + posCntr +"\t" + data);
					}
				posCntr += 4;
			}
			//Start reading instructions and executing them
			posCntr = 256;
			instrFlag = true;
			int i = 0;
			int nextCntr = 256;
			while(instrFlag && i < 40){
					i++;
					posCntr = nextCntr;
					nextInstr = hm.get(posCntr);
					instrCategory = nextInstr.substring(0, 3);
					switch (instrCategory) {
						case "000":		//Category 1
							nextCntr = processCategoryOne(posCntr, nextInstr);
							break;	
						case "001":		//Category 2
							nextCntr = processCategoryTwo(posCntr, nextInstr);
							break;
						case "010":		//Category 3
							nextCntr = processCategoryThree(posCntr, nextInstr);
							break;
						case "011":		//Category 4
							nextCntr = processCategoryFour(posCntr, nextInstr);
							break;					
						case "100":		//Category 5
							nextCntr = processCategoryFive(posCntr, nextInstr);					
							break;					
						default:		//Input Error 
							System.out.println(nextInstr + " : Invalid Instruction");
							break;				
					}
					printState(posCntr, nextInstr, disassembleInstruction(posCntr, nextInstr));
					if (posCntr == nextCntr) 
						nextCntr += 4;
				}
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
	}	


	private static void printState(int posCntr, String instr, String instrName) {
		cycle++;
		System.out.println("--------------------");
		System.out.println("Cycle " + cycle + ":\t" + posCntr + "\t" + instrName + "\n");
		System.out.print("Registers");
		for(int i=0; i<32; i++){
			if (i%8 == 0){
				System.out.println();
				System.out.format("R%02d:", i);
			}
			System.out.print("\t" + regArr[i]);
		}
		System.out.println();
		System.out.println();
		System.out.print("Data");
		int dataReg = 316;
		for(int i=0; i<16; i++){
			if (i%8 == 0){
				System.out.println();
				System.out.format("%03d:", dataReg);
			}
			System.out.print("\t" + dataMap.get(dataReg));
			dataReg += 4;
		}
		System.out.println();
		System.out.println();
	}	
	
	
	private static String disassembleInstruction(int posCntr, String mipsInstr) {
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
						int temp = (posCntr+4) & 0xf0000000;
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
						instrName = "BREAK ";
						instrFlag = false;
						break;
				}			
				//instrName += dest + ", " + src1 + ", " + src2;
				//System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
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
						instrName += dest + ", " + src1 + ", " + src2;
						return instrName;
					case "101":
						instrName += "SRA ";
						src2 = "" + Integer.parseInt(mipsInstr.substring(16, 21), 2);
						instrName += dest + ", " + src1 + ", " + src2;
						return instrName;
				}
				instrName += dest + ", " + src1 + ", " + src2;
				//System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
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
				//System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
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
				instrName += src1 + ", " + src2;// + ", " + data;
				//System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
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
				instrName += dest;// + ", ";
				//System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
				break;									
		}
		return instrName;
	}	
	

	
	private static int processCategoryOne(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-1
		String opCode = mipsInstr.substring(3, 6);
		String bits = mipsInstr.substring(6);
		int data = 0;
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
				if (regArr[src1] == regArr[src2]){
					posCntr = posCntr + 4 + offset;
				}
				break;
			case "010":
				// BNE
				if (regArr[src1] != regArr[src2]){
					posCntr = posCntr + 4 + offset;
				}
				break;
			case "011":
				// BGTZ
				if (regArr[src1] > 0){
					posCntr = posCntr + 4 + offset;
				}
				break;
			case "100":
				// SW
/*				data = Long.parseLong(hm.get(), 2);
				if (data > 2147483647)
					data = data - 4294967296l;
				System.out.println(src1 + "  " + ((int)data));*/
				dataMap.put((offset >> 2) + regArr[src1], regArr[src2]);
				break;
			case "101":
				// LW
/*				data = Long.parseLong(hm.get((offset >> 2)), 2);
				if (data > 2147483647)
					data = data - 4294967296l;
				System.out.println(src1 + "  " + ((int)data));*/
				regArr[src2] = dataMap.get((offset >> 2)+ regArr[src1]) ;
				break;
			case "110":
				// BREAK
				instrFlag = false;
				break;
		}
		return posCntr;
	}

	private static int processCategoryTwo(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-2
		String opCode = mipsInstr.substring(3, 6);
		int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
		int src2 = Integer.parseInt(mipsInstr.substring(16, 21), 2);
		switch (opCode) {
			case "000":
				//ADD
				regArr[dest] = regArr[src1] + regArr[src2];
				break;
			case "001":
				//SUB
				regArr[dest] = regArr[src1] - regArr[src2];
				break;
			case "010":
				//AND
				regArr[dest] = regArr[src1] & regArr[src2];
				break;
			case "011":
				//OR
				regArr[dest] = regArr[src1] | regArr[src2];
				break;
			case "100":
				//SRL
				regArr[dest] = regArr[src1] >>> 2;
				break;
			case "101":
				//SRA
				regArr[dest] = regArr[src1] >> 2;
				break;
		}		
		return posCntr;
	}

	private static int processCategoryThree(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-3
		String opCode = mipsInstr.substring(3, 6);
		int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		int src1 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
		int data = Integer.parseInt(mipsInstr.substring(16), 2);
		switch (opCode) {
			case "000":
				//ADDI
				regArr[dest] = regArr[src1] + data;
				break;
			case "001":
				//ANDI
				regArr[dest] = regArr[src1] & data;
				break;
			case "010":
				//ORI
				regArr[dest] = regArr[src1] | data;
				break;
		}
		return posCntr;
	}

	private static int processCategoryFour(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-4
		String opCode = mipsInstr.substring(3, 6);
		int src1 = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		int src2 = Integer.parseInt(mipsInstr.substring(11, 16), 2);
		//String data = "#" + Integer.parseInt(mipsInstr.substring(16), 2);
		String instrName = "";
		switch (opCode) {
			case "000":
				//MULT
				LO = regArr[src1] * regArr[src2];
				break;
			case "001":
				//DIV
				LO = regArr[src1] / regArr[src2];
				HI = regArr[src1] % regArr[src2];
				break;
		}
		return posCntr;
	}

	private static int processCategoryFive(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-5
		String opCode = mipsInstr.substring(3, 6);
		int dest = Integer.parseInt(mipsInstr.substring(6, 11), 2);
		switch (opCode) {
			case "000":
				//MFHI
				regArr[dest] = HI; 
				HI = -1;
				break;
			case "001":
				//MFLO
				regArr[dest] = LO; 
				LO = -1;
				break;
		}
		return posCntr;
	}

//END PROGRAM
}
