/***************************************************************************
 *   Copyright (C) 2007 by Sindre Aam�s                                    *
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
#include <gambatte.h>
#include <pakinfo.h>
#include "array.h"
#include "rateest.h"
#include <SDL.h>
#include <cstdlib>
#include <cstring>
#include <cstdio>
#include <string>
#include <sstream>
#include <iostream>
#include <algorithm>
#include <memory>
#include <vector>
#include <map>
#ifdef __APPLE__
#include <dlfcn.h>  //To make it work on mac
#endif

#include "blitterwrapper.h"
#include "mrwint_gbtasgen_Gb.h"

namespace {

using namespace gambatte;


class GetInput : public InputGetter {
public:
	unsigned is;
	
	GetInput() : is(0) {}
	unsigned operator()() { return is; }
};

class SdlIniter {
	bool failed;
public:
	BlitterWrapper blitter;

	SdlIniter() : failed(false) {}
    void init(int numScreens) {
#ifdef __APPLE__
        pre_init();
#endif

		const int scaleOption = 1;
		if (SDL_Init(SDL_INIT_VIDEO) < 0) {
			std::fprintf(stderr, "Unable to init SDL: %s\n", SDL_GetError());
			failed = true;
		}

		blitter.setScale(scaleOption);
		blitter.setYuv(false);
		blitter.setVideoFilter(0);

		blitter.init(numScreens);
		SDL_ShowCursor(SDL_DISABLE);
		SDL_WM_SetCaption("Gambatte SDL", NULL);
	}
	~SdlIniter() { SDL_Quit(); }

#ifdef __APPLE__
    void pre_init()
    {
        void* cocoa_lib;

        cocoa_lib = dlopen( "/System/Library/Frameworks/Cocoa.framework/Cocoa", RTLD_LAZY );
        void (*nsappload)(void);
        nsappload = (void(*)()) dlsym( cocoa_lib, "NSApplicationLoad");
        nsappload();
    }
#endif
	bool isFailed() const { return failed; }
};

SdlIniter sdl;
bool debug = false;

class GambatteSdl {
public:
	Array<Sint16> inBuf;
	GB gambatte;
	
	GetInput inputGetter;
	std::map<SDLKey,unsigned> keyMap;
	
	unsigned overflowSamples;
	bool init(const char* romName);
	int screen;
	bool equalLengthFrames;
	
	GambatteSdl(int screen_, bool equalLengthFrames_) : inBuf((35112 + 2064) * 2),overflowSamples(0),screen(screen_),equalLengthFrames(equalLengthFrames_) {}
	void step();
	void saveState(std::vector<char>& data);
	void loadState(const std::vector<char>& data);
	void handleInput();

	bool keepRunning;
};

bool GambatteSdl::init(const char* romName) {
        //std::printf("Gambatte SDL SVN\n");

	keepRunning = true;
	
	if (sdl.isFailed())
		return 1;
	
	{
		const bool gbaCgbOption = false;
		const bool forceDmgOption = false;
		const bool multicartCompatOption = false;

		gambatte.loadGBCBios("roms/gbc_bios.bin");
		
		if (LoadRes const error =
				gambatte.load(romName,
				                gbaCgbOption          * GB::GBA_CGB
				              + forceDmgOption        * GB::FORCE_DMG
				              + multicartCompatOption * GB::MULTICART_COMPAT)) {
			std::printf("failed to load ROM %s: %s\n", romName, to_string(error).c_str());
			return error;
		}

		{
			PakInfo const &pak = gambatte.pakInfo();
			//std::puts(gambatte.romTitle().c_str());
			//std::printf("GamePak type: %s rambanks: %u rombanks: %u\n",
			//            pak.mbc().c_str(), pak.rambanks(), pak.rombanks());
			//std::printf("header checksum: %s\n", pak.headerChecksumOk() ? "ok" : "bad");
			//std::printf("cgb: %d\n", gambatte.isCgb());
		}

		unsigned const gbbuts[8] = {
			InputGetter::START, InputGetter::SELECT,
			InputGetter::A, InputGetter::B,
			InputGetter::UP, InputGetter::DOWN,
			InputGetter::LEFT, InputGetter::RIGHT
		};
		SDLKey const inputKeys[] = {
			SDLK_RETURN, SDLK_RSHIFT,
			SDLK_d, SDLK_c,
			SDLK_UP, SDLK_DOWN,
			SDLK_LEFT, SDLK_RIGHT};
		
		for (unsigned i = 0; i < 8; ++i)
			keyMap.insert(std::pair<SDLKey,unsigned>(inputKeys[i], gbbuts[i]));
	}
	
	gambatte.setInputGetter(&inputGetter);
	
	return 0;
}

void GambatteSdl::handleInput() {
	SDL_Event e;
	
	while (SDL_PollEvent(&e)) {
		switch (e.type) {
		case SDL_KEYDOWN:
			if (e.key.keysym.mod & KMOD_CTRL) {
				switch (e.key.keysym.sym) {
				case SDLK_f:
					sdl.blitter.toggleFullScreen();
					break;
				case SDLK_r:
					//gambatte.reset();
					break;
				default: break;
				}
			} else {
				switch (e.key.keysym.sym) {
				case SDLK_ESCAPE: keepRunning = false; return;
				case SDLK_F5: gambatte.saveState(sdl.blitter.inBuf(screen).pixels, sdl.blitter.inBuf(screen).pitch); break;
				case SDLK_F6: gambatte.selectState(gambatte.currentState() - 1); break;
				case SDLK_F7: gambatte.selectState(gambatte.currentState() + 1); break;
				case SDLK_F8: gambatte.loadState(); break;
				case SDLK_0: gambatte.selectState(0); break;
				case SDLK_1: gambatte.selectState(1); break;
				case SDLK_2: gambatte.selectState(2); break;
				case SDLK_3: gambatte.selectState(3); break;
				case SDLK_4: gambatte.selectState(4); break;
				case SDLK_5: gambatte.selectState(5); break;
				case SDLK_6: gambatte.selectState(6); break;
				case SDLK_7: gambatte.selectState(7); break;
				case SDLK_8: gambatte.selectState(8); break;
				case SDLK_9: gambatte.selectState(9); break;
				default: break;
				}
			}
			// fallthrough
		case SDL_KEYUP:
			if(keyMap.count(e.key.keysym.sym)) {
				if (e.key.state)
					inputGetter.is |= keyMap[e.key.keysym.sym];
				else
					inputGetter.is &= ~keyMap[e.key.keysym.sym];
			}
			
			break;
		case SDL_QUIT:
			keepRunning = false; return;
		}
	}
}

const unsigned SAMPLES_PER_FRAME = 35112;


void GambatteSdl::step() {
	const BlitterWrapper::Buf &vbuf = sdl.blitter.inBuf(screen);

	while (true) {
		std::size_t emusamples = SAMPLES_PER_FRAME - overflowSamples;
		if (gambatte.runFor(vbuf.pixels, vbuf.pitch,
				reinterpret_cast<gambatte::uint_least32_t*>(inBuf.get()), emusamples) >= 0) {
            if(debug) {
		        sdl.blitter.draw();
			    sdl.blitter.present();
			}
		}

		overflowSamples += emusamples;

		if(gambatte.p_->cpu.hitInterruptAddress != 0) { // go into frame
			break;
		}

		if(!equalLengthFrames) { // old frame timing
			overflowSamples = 0; // completed frame
			gambatte.p_->cpu.numFrames++;
			break;
		}

		if (overflowSamples >= SAMPLES_PER_FRAME) { // new frame timing
			overflowSamples -= SAMPLES_PER_FRAME;
			gambatte.p_->cpu.numFrames++;
			break;
		}
	}
}

void GambatteSdl::saveState(std::vector<char>& data) {
	if (gambatte.p_->cpu.loaded()) {
		loadsave_save l;
		gambatte.p_->cpu.loadOrSave(l);
		l(overflowSamples);
		l.get(data);
	}
}

void GambatteSdl::loadState(const std::vector<char>& data) {
	if (gambatte.p_->cpu.loaded()) {
		loadsave_load l(data);
		gambatte.p_->cpu.loadOrSave(l);
		l(overflowSamples);
	}
}
} // anon namespace


const int STEP = 32; // could be 1024 for GB
unsigned dualTarget = STEP;

void dualGbStep(GambatteSdl* gbL, GambatteSdl* gbR, bool cableconnected) {
	const BlitterWrapper::Buf &vbufL = sdl.blitter.inBuf(gbL->screen);
	const BlitterWrapper::Buf &vbufR = sdl.blitter.inBuf(gbR->screen);

	// slowly step our way through the frame, while continually checking and resolving link cable status
	while (true)
	{
		// gambatte_runfor() aborts early when a frame is produced, but we don't want that, hence the while()
		while (gbL->overflowSamples < dualTarget)
		{
			std::size_t emusamples = dualTarget - gbL->overflowSamples;
			if (gbL->gambatte.runFor(vbufL.pixels, vbufL.pitch,
					reinterpret_cast<gambatte::uint_least32_t*>(gbL->inBuf.get()), emusamples) >= 0) {
				sdl.blitter.draw();
				sdl.blitter.present();
			}
			gbL->overflowSamples += emusamples;

			if(gbL->gambatte.p_->cpu.hitInterruptAddress != 0) { // go into frame
				return;
			}
		}
		while (gbR->overflowSamples < dualTarget)
		{
			std::size_t emusamples = dualTarget - gbR->overflowSamples;
			if (gbR->gambatte.runFor(vbufR.pixels, vbufR.pitch,
					reinterpret_cast<gambatte::uint_least32_t*>(gbR->inBuf.get()), emusamples) >= 0) {
				sdl.blitter.draw();
				sdl.blitter.present();
			}
			gbR->overflowSamples += emusamples;

			if(gbR->gambatte.p_->cpu.hitInterruptAddress != 0) { // go into frame
				return;
			}
		}

		// poll link cable statuses, but not when the cable is disconnected
		if (!cableconnected)
			continue;

		if (gbL->gambatte.p_->cpu.mem_.linkStatus(256) != 0) // ClockTrigger
		{
			gbL->gambatte.p_->cpu.mem_.linkStatus(257); // ack
			int lo = gbL->gambatte.p_->cpu.mem_.linkStatus(258); // GetOut
			int ro = gbR->gambatte.p_->cpu.mem_.linkStatus(258);
			gbL->gambatte.p_->cpu.mem_.linkStatus(ro & 0xff); // ShiftIn
			gbR->gambatte.p_->cpu.mem_.linkStatus(lo & 0xff); // ShiftIn
		}
		if (gbR->gambatte.p_->cpu.mem_.linkStatus(256) != 0) // ClockTrigger
		{
			gbR->gambatte.p_->cpu.mem_.linkStatus(257); // ack
			int lo = gbL->gambatte.p_->cpu.mem_.linkStatus(258); // GetOut
			int ro = gbR->gambatte.p_->cpu.mem_.linkStatus(258);
			gbL->gambatte.p_->cpu.mem_.linkStatus(ro & 0xff); // ShiftIn
			gbR->gambatte.p_->cpu.mem_.linkStatus(lo & 0xff); // ShiftIn
		}

		if (dualTarget >= SAMPLES_PER_FRAME)
			break;

		dualTarget += STEP;
		if (dualTarget > SAMPLES_PER_FRAME)
			dualTarget = SAMPLES_PER_FRAME; // don't run for slightly too long depending on step
	}
	gbL->overflowSamples = gbL->overflowSamples - SAMPLES_PER_FRAME;
	gbR->overflowSamples = gbR->overflowSamples - SAMPLES_PER_FRAME;
        dualTarget = STEP;
}

void saveDualState(GambatteSdl* gbL, GambatteSdl* gbR, std::vector<char>& data) {
  loadsave_save l;
	if (gbL->gambatte.p_->cpu.loaded()) {
		gbL->gambatte.p_->cpu.loadOrSave(l);
		l(gbL->overflowSamples);
	}
	if (gbR->gambatte.p_->cpu.loaded()) {
		gbR->gambatte.p_->cpu.loadOrSave(l);
		l(gbR->overflowSamples);
	}
  l(dualTarget);
  l.get(data);
}

void loadDualState(GambatteSdl* gbL, GambatteSdl* gbR, const std::vector<char>& data) {
  loadsave_load l(data);
	if (gbL->gambatte.p_->cpu.loaded()) {
		gbL->gambatte.p_->cpu.loadOrSave(l);
		l(gbL->overflowSamples);
	}
	if (gbR->gambatte.p_->cpu.loaded()) {
		gbR->gambatte.p_->cpu.loadOrSave(l);
		l(gbR->overflowSamples);
	}
  l(dualTarget);
}


#define UNUSED(x)  (void)(x)


// initSdl
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_initSdl
    (JNIEnv *env, jclass clazz, jint numScreens) {
  UNUSED(env);UNUSED(clazz);

  sdl.init(numScreens);
}


// createGb
JNIEXPORT jlong JNICALL Java_mrwint_gbtasgen_Gb_createGb
    (JNIEnv *env, jclass clazz, jint screen, jboolean equalLengthFrames) {
  UNUSED(env);UNUSED(clazz);

  GambatteSdl* gb = new GambatteSdl(screen, equalLengthFrames);
  return (jlong)gb;
}

// startEmulator
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_startEmulator
    (JNIEnv *env, jclass clazz, jlong gb, jstring str, jboolean debug_, jint rtcOffset_) {
  UNUSED(clazz);

  ((GambatteSdl*)gb)->init(env->GetStringUTFChars(str, 0));
  debug = (bool) debug_;
  ((GambatteSdl*)gb)->gambatte.p_->cpu.rtcOffset = (int) rtcOffset_;
}

JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_getCycleCount
    (JNIEnv *env, jclass clazz, jlong gb){
  UNUSED(env);UNUSED(clazz);

  int cc = ((GambatteSdl*)gb)->gambatte.p_->cpu.cycleCounter_;
  return cc;
}


// ninitDualGb
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_ninitDualGb
    (JNIEnv *env, jclass clazz, jlong gbL, jlong gbR) {
  UNUSED(env);UNUSED(clazz);

  // connect link cable
  ((GambatteSdl*)gbL)->gambatte.p_->cpu.mem_.linkStatus(259);
  ((GambatteSdl*)gbR)->gambatte.p_->cpu.mem_.linkStatus(259);
}

// nstepDual
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_nstepDual
    (JNIEnv *env, jclass clazz, jlong gbL, jlong gbR, jint keymaskL, jint keymaskR) {
  UNUSED(env);UNUSED(clazz);

  if(keymaskL == -1)
    ((GambatteSdl*)gbL)->handleInput();
  else
    ((GambatteSdl*)gbL)->inputGetter.is = keymaskL;
  ((GambatteSdl*)gbL)->gambatte.p_->cpu.numInterruptAddresses = 0; // no interrupts

  if(keymaskR == -1)
    ((GambatteSdl*)gbR)->handleInput();
  else
    ((GambatteSdl*)gbR)->inputGetter.is = keymaskR;
  ((GambatteSdl*)gbR)->gambatte.p_->cpu.numInterruptAddresses = 0; // no interrupts

  dualGbStep((GambatteSdl*)gbL, (GambatteSdl*)gbR, true);
}

// nstepDualUntil
JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_nstepDualUntil
    (JNIEnv *env, jclass clazz, jlong gbL, jlong gbR, jint keymaskL, jint keymaskR, jintArray arrL, jintArray arrR) {
  UNUSED(env);UNUSED(clazz);

  if(keymaskL == -1)
    ((GambatteSdl*)gbL)->handleInput();
  else
    ((GambatteSdl*)gbL)->inputGetter.is = keymaskL;

  jsize lenL = env->GetArrayLength(arrL);
  jint* addressesL = env->GetIntArrayElements(arrL,0);
  ((GambatteSdl*)gbL)->gambatte.p_->cpu.numInterruptAddresses = lenL; // no interrupts
  ((GambatteSdl*)gbL)->gambatte.p_->cpu.interruptAddresses = addressesL; // set up interrupts

  if(keymaskR == -1)
    ((GambatteSdl*)gbR)->handleInput();
  else
    ((GambatteSdl*)gbR)->inputGetter.is = keymaskR;

  jsize lenR = env->GetArrayLength(arrR);
  jint* addressesR = env->GetIntArrayElements(arrR,0);
  ((GambatteSdl*)gbR)->gambatte.p_->cpu.numInterruptAddresses = lenR; // no interrupts
  ((GambatteSdl*)gbR)->gambatte.p_->cpu.interruptAddresses = addressesR; // set up interrupts

  dualGbStep((GambatteSdl*)gbL, (GambatteSdl*)gbR, true);

  env->ReleaseIntArrayElements(arrL,addressesL,0);
  env->ReleaseIntArrayElements(arrR,addressesR,0);
  if (((GambatteSdl*)gbL)->gambatte.p_->cpu.hitInterruptAddress)
    return ((GambatteSdl*)gbL)->gambatte.p_->cpu.hitInterruptAddress;
  return ((GambatteSdl*)gbR)->gambatte.p_->cpu.hitInterruptAddress;
}


// nstep
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_nstep
    (JNIEnv *env, jclass clazz, jlong gb, jint keymask) {
  UNUSED(env);UNUSED(clazz);

  if(keymask == -1)
    ((GambatteSdl*)gb)->handleInput();
  else
    ((GambatteSdl*)gb)->inputGetter.is = keymask;

  ((GambatteSdl*)gb)->gambatte.p_->cpu.numInterruptAddresses = 0; // no interrupts
  ((GambatteSdl*)gb)->step();
}

// nstepUntil
JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_nstepUntil
    (JNIEnv *env, jclass clazz, jlong gb, jint keymask, jintArray arr) {
  UNUSED(env);UNUSED(clazz);

  if(keymask == -1)
    ((GambatteSdl*)gb)->handleInput();
  else
    ((GambatteSdl*)gb)->inputGetter.is = keymask;

  jsize len = env->GetArrayLength(arr);
  jint* addresses = env->GetIntArrayElements(arr,0);

  ((GambatteSdl*)gb)->gambatte.p_->cpu.numInterruptAddresses = len;
  ((GambatteSdl*)gb)->gambatte.p_->cpu.interruptAddresses = addresses; // set up interrupts
  ((GambatteSdl*)gb)->step();

  env->ReleaseIntArrayElements(arr,addresses,0);
  return ((GambatteSdl*)gb)->gambatte.p_->cpu.hitInterruptAddress;
}

// nreset
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_nreset
    (JNIEnv *env, jclass clazz, jlong gb) {
  UNUSED(env);UNUSED(clazz);

  ((GambatteSdl*)gb)->gambatte.reset(0);
}

// saveState
JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_saveState
    (JNIEnv *env, jclass clazz, jlong gb, jobject buffer, jint size){
  UNUSED(clazz);

  char* buffer_address = ((char*) env->GetDirectBufferAddress(buffer));
  std::vector<char> data;
  ((GambatteSdl*)gb)->saveState(data);

  if((int)data.size() > size)
    std::cout << "too big! " << data.size() << " vs. " << size << std::endl;

  std::copy(data.begin(), data.end(), buffer_address);

  return data.size();
}

// loadState
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_loadState
    (JNIEnv *env, jclass clazz, jlong gb, jobject buffer, jint size){
  UNUSED(clazz);

  char* buffer_address = ((char*) env->GetDirectBufferAddress(buffer));

  std::vector<char> data(buffer_address,buffer_address+size);

  ((GambatteSdl*)gb)->loadState(data);
}


// saveDualState
JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_saveDualState
    (JNIEnv *env, jclass clazz, jlong gbL, jlong gbR, jobject buffer, jint size) {
  UNUSED(clazz);

  char* buffer_address = ((char*) env->GetDirectBufferAddress(buffer));
  std::vector<char> data;
  saveDualState((GambatteSdl*)gbL, (GambatteSdl*)gbR, data);

  if((int)data.size() > size)
    std::cout << "too big! " << data.size() << " vs. " << size << std::endl;

  std::copy(data.begin(), data.end(), buffer_address);

  return data.size();
}

// loadDualState
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_loadDualState
    (JNIEnv *env, jclass clazz, jlong gbL, jlong gbR, jobject buffer, jint size) {
  UNUSED(clazz);

  char* buffer_address = ((char*) env->GetDirectBufferAddress(buffer));

  std::vector<char> data(buffer_address,buffer_address+size);

  loadDualState((GambatteSdl*)gbL, (GambatteSdl*)gbR, data);
}


// getROMSize
JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_getROMSize
    (JNIEnv *env, jclass clazz, jlong gb){
  UNUSED(env);UNUSED(clazz);

  return ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.memptrs_.numRombanks*0x4000;
}

// getROM
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_getROM
    (JNIEnv *env, jclass clazz, jlong gb, jintArray arr){
  UNUSED(env);UNUSED(clazz);UNUSED(arr);

  int size = ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.memptrs_.numRombanks*0x4000;
  jint *rom_store = env->GetIntArrayElements(arr, 0);
  for(int i=0;i<size;i++)
    rom_store[i] = ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.memptrs_.romdata()[i];
  env->ReleaseIntArrayElements(arr, rom_store, 0);
}

// getRegisters
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_getRegisters
    (JNIEnv *env, jclass clazz, jlong gb, jintArray arr){
  UNUSED(env);UNUSED(clazz);UNUSED(arr);

  jint *registers_store = env->GetIntArrayElements(arr, 0);

  registers_store[0] = ((GambatteSdl*)gb)->gambatte.p_->cpu.pc_;
  registers_store[1] = ((GambatteSdl*)gb)->gambatte.p_->cpu.sp;
  registers_store[2] = ((GambatteSdl*)gb)->gambatte.p_->cpu.a_ << 8;
  registers_store[3] = (((GambatteSdl*)gb)->gambatte.p_->cpu.b << 8) + ((GambatteSdl*)gb)->gambatte.p_->cpu.c;
  registers_store[4] = (((GambatteSdl*)gb)->gambatte.p_->cpu.d << 8) + ((GambatteSdl*)gb)->gambatte.p_->cpu.e;
  registers_store[5] = (((GambatteSdl*)gb)->gambatte.p_->cpu.h << 8) + ((GambatteSdl*)gb)->gambatte.p_->cpu.l;

  env->ReleaseIntArrayElements(arr, registers_store, 0);
}

// getMemory
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_getMemory
    (JNIEnv *env, jclass clazz, jlong gb, jintArray arr){
  UNUSED(env);UNUSED(clazz);UNUSED(arr);

  jint *mem_store = env->GetIntArrayElements(arr, 0);
  int cc = ((GambatteSdl*)gb)->gambatte.p_->cpu.cycleCounter_;
  for(int i=0;i<0x10000;i++)
    mem_store[i] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, i);
//    mem_store[i] = ((GambatteSdl*)gb)->gambatte.p_->cpu.memory.read(i,cc);
  env->ReleaseIntArrayElements(arr, mem_store, 0);
}

JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_getInterestingMemory
    (JNIEnv *env, jclass clazz, jlong gb, jintArray arr){
  UNUSED(env);UNUSED(clazz);UNUSED(arr);

  jint *mem_store = env->GetIntArrayElements(arr, 0);

  for(int i=0;i<0x10;i++)
	  mem_store[i] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xc208 + i*0x10);

  mem_store[0x10] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xCC4B);
  mem_store[0x11] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xD362);
  mem_store[0x12] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xD361);
  mem_store[0x13] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xD35E);
  mem_store[0x14] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xFFD3);
  mem_store[0x15] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xFFD4);
  mem_store[0x16] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xD057);
  mem_store[0x17] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xCFE5);
  mem_store[0x18] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xCFF3);
  mem_store[0x19] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xCFF1);
  mem_store[0x1A] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xCFF2);
  mem_store[0x1B] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xFFDA);
  mem_store[0x1C] = Java_mrwint_gbtasgen_Gb_readMemory(env, clazz, gb, 0xD730);
  env->ReleaseIntArrayElements(arr, mem_store, 0);
}

// readMemory
JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_readMemory
    (JNIEnv *env, jclass clazz, jlong gb, jint address){
  UNUSED(env);UNUSED(clazz);

  {
	const unsigned P = address;
	if (P < 0x8000)
		return ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.romdata(P >> 14)[P];

	if (P < 0xA000)
		return ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.vrambankptr()[P];

	if (P < 0xC000) {
		if (((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.rsrambankptr())
			return ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.rsrambankptr()[P];
		else {
			std::printf("error: cart.rsrambankptr() not present");
			return ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.rtcRead();
		}
	}

	if (P < 0xFE00)
		return ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.cart_.wramdata(P >> 12 & 1)[P & 0xFFF];

	return ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.ioamhram_[P - 0xFE00];
  }

//  return ((GambatteSdl*)gb)->gambatte.p_->cpu.memory.read(address,((GambatteSdl*)gb)->gambatte.p_->cpu.cycleCounter_);
}

// writeMemory
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_writeMemory
    (JNIEnv *env, jclass clazz, jlong gb, jint address, jint value){
  UNUSED(env);UNUSED(clazz);

  ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.write(address,value,((GambatteSdl*)gb)->gambatte.p_->cpu.cycleCounter_);
}

// getDivState
JNIEXPORT jint JNICALL Java_mrwint_gbtasgen_Gb_getDivState
    (JNIEnv *env, jclass clazz, jlong gb){
  UNUSED(env);UNUSED(clazz);

  int cc = ((GambatteSdl*)gb)->gambatte.p_->cpu.cycleCounter_;
  int divOff = cc - ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.divLastUpdate_;
  int div = ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.ioamhram_[0x104];
  return (((div << 8) + divOff) >> 2) & 0x3FFF;
}

// offsetDiv
JNIEXPORT void JNICALL Java_mrwint_gbtasgen_Gb_offsetDiv
    (JNIEnv *env, jclass clazz, jlong gb, jint offset){
  UNUSED(env);UNUSED(clazz);

  ((GambatteSdl*)gb)->gambatte.p_->cpu.mem_.divLastUpdate_ -= (offset << 2);
}

