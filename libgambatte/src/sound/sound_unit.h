//
//   Copyright (C) 2007 by sinamas <sinamas at users.sourceforge.net>
//
//   This program is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License version 2 as
//   published by the Free Software Foundation.
//
//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License version 2 for more details.
//
//   You should have received a copy of the GNU General Public License
//   version 2 along with this program; if not, write to the
//   Free Software Foundation, Inc.,
//   51 Franklin St, Fifth Floor, Boston, MA  02110-1301, USA.
//

#ifndef SOUND_UNIT_H
#define SOUND_UNIT_H

#include "../loadsave.h"

namespace gambatte {

class SoundUnit {
public:
	enum { counter_max = 0x80000000u, counter_disabled = 0xFFFFFFFFu };
	SoundUnit() : counter_(counter_disabled) {}
	virtual ~SoundUnit() {}
	virtual void event() = 0;

	virtual void resetCounters(unsigned long /*oldCc*/) {
		if (counter_ != counter_disabled)
			counter_ -= counter_max;
	}
	void loadOrSave2(loadsave& state) {
		state(counter_);
	}

	unsigned long counter() const { return counter_; }

protected:
	unsigned long counter_;
};

}

#endif
