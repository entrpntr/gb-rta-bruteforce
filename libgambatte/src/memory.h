/***************************************************************************
 *   Copyright (C) 2007 by Sindre Aamås                                    *
 *   sinamas@users.sourceforge.net                                         *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License version 2 as     *
 *   published by the Free Software Foundation.                            *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License version 2 for more details.                *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   version 2 along with this program; if not, write to the               *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
#ifndef MEMORY_H
#define MEMORY_H

static unsigned char const agbOverride[0xD] = { 0xFF, 0x00, 0xCD, 0x03, 0x35, 0xAA, 0x31, 0x90, 0x94, 0x00, 0x00, 0x00, 0x00 };

#include "mem/cartridge.h"
#include "interrupter.h"
#include "pakinfo.h"
#include "sound.h"
#include "tima.h"
#include "video.h"
#include "loadsave.h"
#include <fstream>

namespace gambatte {
class InputGetter;
class FilterInfo;

class Memory {
public:
	Cartridge cart;
	unsigned char ioamhram[0x200];
	unsigned char cgbBios[0x900];
    unsigned char dmgBios[0x100];
	InputGetter *getInput;
	unsigned long divLastUpdate;
	unsigned long lastOamDmaUpdate;
	
	InterruptRequester intreq;
	Tima tima;
	LCD display;
	PSG sound;
	Interrupter interrupter;
	
	unsigned short dmaSource;
	unsigned short dmaDestination;
	unsigned char oamDmaPos;
	unsigned char serialCnt;
	bool blanklcd;
	bool biosMode_;
    bool cgbSwitching_;
    bool agbMode_;
    bool gbIsCgb_;

	bool LINKCABLE;
	bool linkClockTrigger;
#ifdef GAMBATTELOG
	std::ofstream logout;
	void log_init();
	void log_write(unsigned P, unsigned data, unsigned long cycleCounter);
#endif

	void updateInput();

	unsigned char* cgbBiosBuffer() { return (unsigned char*) cgbBios; }
    unsigned char* dmgBiosBuffer() { return (unsigned char*) dmgBios; }
    bool gbIsCgb() { return gbIsCgb_; }

	void decEventCycles(MemEventId eventId, unsigned long dec);

	void oamDmaInitSetup();
	void updateOamDma(unsigned long cycleCounter);
	void startOamDma(unsigned long cycleCounter);
	void endOamDma(unsigned long cycleCounter);
	const unsigned char * oamDmaSrcPtr() const;
	
	unsigned nontrivial_ff_read(unsigned P, unsigned long cycleCounter);
	unsigned nontrivial_read(unsigned P, unsigned long cycleCounter);
	void nontrivial_ff_write(unsigned P, unsigned data, unsigned long cycleCounter);
	void nontrivial_write(unsigned P, unsigned data, unsigned long cycleCounter);
	
	void updateSerial(unsigned long cc);
	void updateTimaIrq(unsigned long cc);
	void updateIrqs(unsigned long cc);
	
	bool isDoubleSpeed() const { return display.isDoubleSpeed(); }

	explicit Memory(const Interrupter &interrupter);
	
	bool loaded() const { return cart.loaded(); }
	char const * romTitle() const { return cart.romTitle(); }
	PakInfo const pakInfo(bool multicartCompat) const { return cart.pakInfo(multicartCompat); }

	void loadOrSave(loadsave& state);

	void setStatePtrs(SaveState &state);
	unsigned long saveState(SaveState &state, unsigned long cc);
	void loadState(const SaveState &state/*, unsigned long oldCc*/);
	void loadSavedata() { cart.loadSavedata(); }
	void saveSavedata() { cart.saveSavedata(); }
	const std::string saveBasePath() const { return cart.saveBasePath(); }
	
	void setOsdElement(std::auto_ptr<OsdElement> osdElement) {
		display.setOsdElement(osdElement);
	}

	unsigned long stop(unsigned long cycleCounter);
	bool isCgb() const { return display.isCgb(); }
	bool ime() const { return intreq.ime(); }
	bool halted() const { return intreq.halted(); }
	unsigned long nextEventTime() const { return intreq.minEventTime(); }
	
	bool isActive() const { return intreq.eventTime(END) != DISABLED_TIME; }
	
	long cyclesSinceBlit(const unsigned long cc) const {
		return cc < intreq.eventTime(BLIT) ? -1 : static_cast<long>((cc - intreq.eventTime(BLIT)) >> isDoubleSpeed());
	}

	void halt() { intreq.halt(); }
	void ei(unsigned long cycleCounter) { if (!ime()) { intreq.ei(cycleCounter); } }

	void di() { intreq.di(); }

	unsigned readBios(unsigned p) {
        if(gbIsCgb_) {
			if(agbMode_ && p >= 0xF3 && p < 0x100) {
				return (agbOverride[p-0xF3] + cgbBios[p]) & 0xFF;
			}
			return cgbBios[p];
		}
		return dmgBios[p];
	}
	unsigned ff_read(const unsigned P, const unsigned long cycleCounter) {
		return P < 0xFF80 ? nontrivial_ff_read(P, cycleCounter) : ioamhram[P - 0xFE00];
	}

	unsigned read(const unsigned p, const unsigned long cycleCounter) {
		if(biosMode_ && ((!gbIsCgb_ && p < 0x100) || (gbIsCgb_ && p < 0x900 && (p < 0x100 || p >= 0x200)))) {
    		return readBios(p);
        }
		return cart.rmem(p >> 12) ? cart.rmem(p >> 12)[p] : nontrivial_read(p, cycleCounter);
	}

	void write(const unsigned P, const unsigned data, const unsigned long cycleCounter) {
		if (cart.wmem(P >> 12)) {
			cart.wmem(P >> 12)[P] = data;
		} else
			nontrivial_write(P, data, cycleCounter);
	}
	
	void ff_write(const unsigned P, const unsigned data, const unsigned long cycleCounter) {
		if (P - 0xFF80u < 0x7Fu) {
			ioamhram[P - 0xFE00] = data;
		} else
			nontrivial_ff_write(P, data, cycleCounter);
	}

	unsigned long event(unsigned long cycleCounter);
	unsigned long resetCounters(unsigned long cycleCounter);

	LoadRes loadROM(const std::string &romfile, bool forceDmg, bool multicartCompat);
	void setSaveDir(const std::string &dir) { cart.setSaveDir(dir); }

	void setInputGetter(InputGetter *getInput) {
		this->getInput = getInput;
	}

	void setEndtime(unsigned long cc, unsigned long inc);
	
	void setSoundBuffer(uint_least32_t *const buf) { sound.setBuffer(buf); }
	unsigned fillSoundBuffer(unsigned long cc);
	
	void setVideoBuffer(uint_least32_t *const videoBuf, const int pitch) {
		display.setVideoBuffer(videoBuf, pitch);
	}
	
	void setDmgPaletteColor(unsigned palNum, unsigned colorNum, unsigned long rgb32);
	void setGameGenie(const std::string &codes) { cart.setGameGenie(codes); }
	void setGameShark(const std::string &codes) { interrupter.setGameShark(codes); }

	void setRTCCallback(std::time_t (*callback)()) {
		cart.setRTCCallback(callback);
	}

	int linkStatus(int which);
};

}

#endif
