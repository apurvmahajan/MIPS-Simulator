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

public class MIPSsim {
	static int d = -1;
	static int instrNum = 0;
	static int dataCntr = -1;
	static int cycle = 0;
	static boolean instrFlag = true;
	static int posCntr = 252;
	static int[] dataArr = new int[1000];
	static int[] regArr = new int[1000];
	static String[] instrLine = new String[1000];
	
	public static void main(String args[]){
		String fileName = args[0];
		File file = new File("");
		String currentDirectory = file.getAbsolutePath();
		File disassemblyFile = new File("disassembly.txt");
		File simulationFile = new File("simulation.txt");		
		BufferedReader br = null;
		try {
			long data;
			String readLine;
			String instrCategory;
			br = new BufferedReader(new FileReader(currentDirectory + "\\" + fileName));
			while((readLine = br.readLine()) != null){
				posCntr += 4;
				instrLine[instrNum] = readLine;
				instrNum++;
				if(instrFlag){
					if (readLine.compareTo("00011000000000000000000000000000") == 0)
						instrFlag = false;
				}
				else {
					if (dataCntr == -1)
						dataCntr = posCntr;
					data = Long.parseLong(readLine, 2);
					if (data > 2147483647)
						data = data - 4294967296l;
					dataArr[++d] = (int) data;
				}
			}
			posCntr = 252;
			for(int i=0; i<instrNum; i++){
				posCntr += 4;
				readLine = instrLine[i];
				instrCategory = readLine.substring(0, 3);
				//opCode = mipsInstr.substring(3, 6);
				//System.out.println(instrCategory + "\t" + opCode);
				if (posCntr < dataCntr){
					switch (instrCategory) {
						case "000":
							processCategoryOne(posCntr, readLine);
							break;
						case "001":
							processCategoryTwo(posCntr, readLine);
							break;
						case "010":
							processCategoryThree(posCntr, readLine);
							break;
						case "011":
							processCategoryFour(posCntr, readLine);
							break;					
						case "100":
							processCategoryFive(posCntr, readLine);					
							break;					
						default:
							System.out.println(readLine + " : Invalid Instruction");
							break;				
					}
				}
				else {
					data = Long.parseLong(readLine, 2);
					if (data > 2147483647)
						data = data - 4294967296l;
					System.out.format("%-34s%-5d%-20d%n", readLine, posCntr, data);
				}
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

	private static void printState(String instrName, String instr) {
		cycle++;
		System.out.println("--------------------");
		System.out.println("Cycle " + cycle + ":\t" + posCntr + "\t" + instr + "\n");
		System.out.print("Registers");
		for(int i=0; i<32; i++){
			if (i%8 == 0){
				System.out.println();
				System.out.format("R%2d:", i);
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
				System.out.format("%3d:", dataReg);
			}
			System.out.print("\t" + dataArr[i]);
			dataReg++;
		}
		System.out.println();
		System.out.println();
	}
	
	private static void processCategoryOne(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-1
		String instrCategory = mipsInstr.substring(0, 3);
		String opCode = mipsInstr.substring(3, 6);
		String bits = mipsInstr.substring(6);	
		//String data = "#" + Integer.parseInt(mipsInstr.substring(6), 2);
		String instrName = "";
		switch (opCode) {
		case "000":
			instrName = "J ";
			break;
		case "001":
			instrName = "BEQ ";
			break;
		case "010":
			instrName = "BNE ";
			break;
		case "011":
			instrName = "BGTZ ";
			break;
		case "100":
			instrName = "SW ";
			break;
		case "101":
			instrName = "LW ";
			break;
		case "110":
			instrName = "BREAK ";
			instrFlag = false;
			break;
		default:
			System.out.println(mipsInstr + " : Invalid OPCODE for Category 1");			
			break;
		}
		//instrName += dest + ", " + src1 + ", " + src2;
		System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
		//printState(mipsInstr, instrName);
	}

	private static void processCategoryTwo(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-2
		String instrCategory = mipsInstr.substring(0, 3);
		String opCode = mipsInstr.substring(3, 6);

		String dest = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
		String src1 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
		String src2 = "R" + Integer.parseInt(mipsInstr.substring(16, 21), 2);

		String bits = mipsInstr.substring(21);
		String instrName = "";
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
			break;
		case "101":
			instrName += "SRA ";
			break;
		default:
			System.out.println(mipsInstr + " : Invalid OPCODE for Category 2");			
			break;
		}		
		instrName += dest + ", " + src1 + ", " + src2;
		System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
		//printState(mipsInstr, instrName);
	}

	private static void processCategoryThree(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-3
		String instrCategory = mipsInstr.substring(0, 3);
		String opCode = mipsInstr.substring(3, 6);
		String dest = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
		String src1 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
		String data = "#" + Integer.parseInt(mipsInstr.substring(16), 2);
		String instrName = "";
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
		default:
			System.out.println(mipsInstr + " : Invalid OPCODE for Category 3");			
			break;
		}
		instrName += dest + ", " + src1 + ", " + data;
		System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
		//printState(mipsInstr, instrName);
	}

	private static void processCategoryFour(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-4
		String instrCategory = mipsInstr.substring(0, 3);
		String opCode = mipsInstr.substring(3, 6);
		String src1 = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
		String src2 = "R" + Integer.parseInt(mipsInstr.substring(11, 16), 2);
		//String data = "#" + Integer.parseInt(mipsInstr.substring(16), 2);
		String instrName = "";
		switch (opCode) {
		case "000":
			instrName = "MULT ";
			break;
		case "001":
			instrName = "DIV ";
			break;
		default:
			System.out.println(mipsInstr + " : Invalid OPCODE for Category 4");			
			break;
		}		
		instrName += src1 + ", " + src2;// + ", " + data;
		System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
		//printState(mipsInstr, instrName);
	}

	private static void processCategoryFive(int posCntr, String mipsInstr) {
		// Format of Instructions in Category-5
		String instrCategory = mipsInstr.substring(0, 3);
		String opCode = mipsInstr.substring(3, 6);
		String dest = "R" + Integer.parseInt(mipsInstr.substring(6, 11), 2);
		String bits = mipsInstr.substring(11);
		String instrName = "";
		switch (opCode) {
		case "000":
			instrName = "MFHI ";
			break;
		case "001":
			instrName = "MFLO ";
			break;
		default:
			System.out.println(mipsInstr + " : Invalid OPCODE for Category 5");			
			break;
		}
		instrName += dest;// + ", ";
		System.out.format("%-34s%-5d%-20s%n", mipsInstr, posCntr, instrName);
		//printState(mipsInstr, instrName);
	}
}
