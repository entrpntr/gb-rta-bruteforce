package mrwint.gbtasgen.tools.deasm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import mrwint.gbtasgen.tools.deasm.specialCallHandler.SpecialCallHandler;
import mrwint.gbtasgen.util.Util;


public class DFS {

	public static class DFSStackElem {
		public int address;
		public CPUState s;

		public DFSStackElem(int address, CPUState s) {
			this.address = address;
			this.s = s;
		}
	}

	public ROM rom;
	public Stack<DFSStackElem> dfsStack;
	public SpecialCallHandler sch;

	public DFS(ROM rom, SpecialCallHandler sch) {
		this.rom = rom;
		this.sch = sch;
		this.sch.init(this);
		this.dfsStack = new Stack<DFSStackElem>();
	}

	public void dfs(int... entries) {

		sch.handleDFSInit();

		for(int entry : entries)
			dfsStack.push(new DFSStackElem(entry, new CPUState()));

		while(!dfsStack.isEmpty()) {
			DFSStackElem e = dfsStack.pop();
			visit(e.address,e.s);
		}
	}

	public void visit(int address, CPUState s) {

		//System.out.println("visit "+Integer.toHexString(address));

		if(rom.type[address] != ROM.UNKNOWN)
			return;
		rom.type[address] = ROM.CODE;
		sch.handleBeforeOp(address,s);
		//rom.comment[address] = s.getCPUStateInfo();

		int nextAddress = address;
		int opCodeValue = rom.data[nextAddress++] & 0xFF;
		int opData = 0;
		OpCode opCode;

		// fetch OpCode
		if(opCodeValue == 0xCB) {
			opData = rom.data[nextAddress++] & 0xFF;
			opCode = OpCode.opCodesCB[opData];
		}
		else {
			opCode = OpCode.opCodes[opCodeValue];

			// fetch optional payload data
			for(int i=0;i<opCode.extraBytes; i++)
				opData += (rom.data[nextAddress++] & 0xFF) << (i << 3); // * 2^(8*i) (little endian)
		}

		//System.out.println("visit "+opCode.name);

		boolean vetoContinue = false;

		CPUState ns = new CPUState(s);
		if(OpCode.explicitAbsoluteJump.contains(opCodeValue)) { // 16 bit absolute
			ns.prepareJump(opCodeValue,opData);
			int fullJumpAddress = handleJumpBankRelative(address,opData & 0xFFFF,ns,ROM.LABEL_FUNCTION);
			ns = new CPUState(s);
			vetoContinue = ns.prepareForgoJump(opCodeValue,address,fullJumpAddress, sch);
		}
		if(OpCode.explicitRelativeJump.contains(opCodeValue)) { // 8 bit signed relative
			ns.prepareJump(opCodeValue,opData);
			int fullJumpAddress = handleJumpFull(address,nextAddress + ((byte)opData),ns,ROM.LABEL_RELATIVE);
			ns = new CPUState(s);
			vetoContinue = ns.prepareForgoJump(opCodeValue,address,fullJumpAddress, sch);
		}
		if(OpCode.reset.contains(opCodeValue)) { // fixed address jump
			int fullJumpAddress = handleJumpFull(address,(opCodeValue - 0xc7),new CPUState(s),ROM.LABEL_FUNCTION);
			ns = new CPUState(s);
			vetoContinue = ns.prepareForgoJump(opCodeValue,address,fullJumpAddress, sch);
		}
		if(!OpCode.noContinue.contains(opCodeValue)) {
			if(vetoContinue) {
				rom.comment[address] = "call does not return";
			} else {
				ns.prepareContinue(opCodeValue,opData,address);
				dfsStack.push(new DFSStackElem(nextAddress, ns));
			}
		}
	}

	private int handleJumpBankRelative(int address, int jumpAddress, CPUState s, int jumpType) {
		if(jumpAddress < 0x4000)             // home bank jump
			return handleJumpFull(address,jumpAddress,s, jumpType);
		else if(jumpAddress >= 0x8000)       // non-ROM jump
			System.out.println("jump to non-ROM address "+Integer.toHexString(jumpAddress)+" at "+Integer.toHexString(address));
		else {
			int bank = address / 0x4000;    // test for intra-bank jump
			if(s.loadedBank != -1) {
				if(bank > 0 && bank != s.loadedBank)
					System.err.println("ERROR: loadedBank "+Integer.toHexString(s.loadedBank)+" does not match address bank "+Integer.toHexString(bank)+" at "+Integer.toHexString(address));
				else
					bank = s.loadedBank;
			}
			if(bank > 0)
				return handleJumpFull(address, jumpAddress+(bank-1)*0x4000, s, jumpType);
			else
				System.out.println("cannot determine ROM bank for jump address "+Integer.toHexString(jumpAddress)+" at "+Integer.toHexString(address));
		}
		return -1;
	}
	private int handleJumpFull(int address, int jumpAddress, CPUState s, int jumpType) {
		rom.addAccess(address,jumpAddress);
		rom.payloadAsAddress[address] = jumpAddress;
		if(rom.label[jumpAddress] == null)
			rom.labelType[jumpAddress] = Math.max(rom.labelType[jumpAddress], jumpType);
		dfsStack.push(new DFSStackElem(jumpAddress, s));
		return jumpAddress;
	}

	public void addIndirectJump(int address, int jumpAddress, CPUState s, int jumpType) {
		rom.indirectJumpTo[address] = jumpAddress;
		rom.payloadAsAddress[address] = jumpAddress;
		if(rom.label[jumpAddress] == null)
			rom.labelType[jumpAddress] = Math.max(rom.labelType[jumpAddress], jumpType);
		dfsStack.push(new DFSStackElem(jumpAddress, s));
	}

	public DFS addJumpTable(int address, int length) {
		return addJumpTable(address, length, null);
	}
	public DFS addJumpTable(int address, int length, String label) {

		if (label != null)
			rom.label[address] = label;
		if(rom.label[address] == null)
			rom.labelType[address] = 3;

		for(int i=0; i<length; i++) {
			int ca = address + 2*i;
			rom.type[ca] = ROM.DATA_JUMPPOINTER;
			if (i % 8 == 0)
				rom.comment[ca] = "$" + Util.toHex(i);

			int goalAddress = ((rom.data[ca]&0xFF) | ((rom.data[ca+1]&0xFF) << 8)) & 0xFFFF;
			int bank = -1;
			if(address >= 0x4000)
				bank = address/0x4000;
			if(goalAddress < 0x4000)
				bank = 1; // it's 0, but 1 makes calculation easier

			if(goalAddress >= 0x8000)
				System.out.println("jump tabel entry to non-RAM location "+Integer.toHexString(goalAddress)+" at "+Integer.toHexString(address)+" element "+i);
			else if(bank == -1)
				System.err.println("unable to infer bank for jump table at "+Integer.toHexString(address)+" element "+i);
			else {
				int fullAddress = goalAddress + (bank-1)*0x4000;
				//System.out.println("addJump "+Integer.toHexString(fullAddress));
				rom.payloadAsAddress[ca] = fullAddress;
				if(rom.label[fullAddress] == null)
					rom.labelType[fullAddress] = Math.max(rom.labelType[fullAddress], 3);
				dfsStack.push(new DFSStackElem(fullAddress, new CPUState()));
			}
		}

		return this;
	}

	public DFS addRawBytes(int address, int length) {

		for(int i = 0; i < length; i++) {
			int ca = address + i;
			rom.type[ca] = ROM.DATA_BYTEARRAY;
			rom.format[ca] = ROM.FORMAT_HEX;
			rom.width[ca] = Math.min(length-i, 8);
		}

		return this;
	}

	public DFS addIgnore(int address, int length) {
		for(int i = 0; i < length; i++)
			rom.type[address + i] = ROM.IGNORE;
		return this;
	}

	public DFS addSection(int address, String name) {
		rom.section[address] = name;
		return this;
	}

	public DFS addByteArray(int address, int length, String label, int... format) {

		if (label != null)
			rom.label[address] = label;

		for(int i = 0; i < length; i++) {
			for (int f = 0; f < format.length; f++) {
				int ca = address + format.length * i + f;
				rom.type[ca] = ROM.DATA_BYTEARRAY;
				rom.format[ca] = format[f];
				rom.width[ca] = format.length;
				if (i % 8 == 0 && f == 0)
					rom.comment[ca] = "$" + Util.toHex(i);
			}
		}

		return this;
	}

	public DFS addTraceFile(String fileName) throws Throwable {
		Set<Integer> s = new HashSet<Integer>();
		InputStream is = new BufferedInputStream(new FileInputStream(fileName));
		byte[] buf = new byte[14];
		int numRead = 0;
		int numInserted = 0;
		while(is.read(buf) != -1) {
			numRead++;
			if(numRead % 1000000 == 0)
				System.out.println("processed "+numRead+" ("+numInserted+")...");
			int add = ((buf[11]&0xFF) << 8) | (buf[10]&0xFF);
			int bank = (buf[12]&0xFF);
			if(add < 0x8000) {
				if(add >= 0x4000)
					add += (bank-1)*0x4000;
				if(!s.contains(add)) {
					s.add(add);
					dfsStack.push(new DFSStackElem(add, new CPUState()));
					numInserted++;
				}
			}
		}
		is.close();
		System.out.println("inserted "+numInserted+" of "+numRead+" traces");

		return this;
	}

	public DFS addExeLog(String fileName) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(fileName));
		while(true) {
			if(!sc.hasNextInt(16))
				break;
			int b = sc.nextInt(16);
			int hl = sc.nextInt(16);
			if(hl >= 0x8000) {
				System.out.println("skipping non-ROM address "+Integer.toHexString(hl));
				continue;
			}
			if(hl >= 0x4000)
				hl += (b-1)*0x4000;
			dfsStack.push(new DFSStackElem(hl, new CPUState()));
			//System.out.println("added "+Integer.toHexString(hl));
		}
		sc.close();

		return this;
	}

	public DFS addAddressFiles(String... fileNames) throws Throwable {
		Set<Integer> s = new HashSet<Integer>();
		byte[] buf = new byte[4];
		int numRead = 0;
		int numInserted = 0;
		for(String fileName : fileNames) {
			InputStream is = new BufferedInputStream(new FileInputStream(fileName));
			while(is.read(buf) != -1) {
				numRead++;
				if(numRead % 1000000 == 0)
					System.out.println("processed "+numRead+" ("+numInserted+")...");
				int add = ((buf[3]&0xFF) << 24) | ((buf[2]&0xFF) << 16) | ((buf[1]&0xFF) << 8) | (buf[0]&0xFF);
				if(!s.contains(add)) {
					s.add(add);
					dfsStack.push(new DFSStackElem(add, new CPUState()));
					numInserted++;
				}
			}
			is.close();
		}
		System.out.println("inserted "+numInserted+" of "+numRead+" traces");

		return this;
	}

	public DFS addInterrupts() {
		addFunction(0x40, "VBlankInterrupt");
		addFunction(0x48, "LCDInterrupt");
		addFunction(0x50, "TimerInterrupt");
		addFunction(0x58, "SerialInterrupt");
		addFunction(0x60, "JoypadInterrupt");

		return this;
	}

	public DFS addInit() {
		addFunction(0x100, "Init");

		return this;
	}

	public DFS addFunction(int address, String name) {
		dfsStack.push(new DFSStackElem(address, new CPUState()));
		rom.label[address] = name;

		return this;
	}
}
